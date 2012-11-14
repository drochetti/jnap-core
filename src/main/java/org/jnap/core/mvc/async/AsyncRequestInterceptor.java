/*
 * AsyncRequestInterceptor.java created on 2011-11-26
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnap.core.mvc.async;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.atmosphere.annotation.Broadcast;
import org.atmosphere.annotation.Cluster;
import org.atmosphere.annotation.Publish;
import org.atmosphere.annotation.Resume;
import org.atmosphere.annotation.Schedule;
import org.atmosphere.annotation.Subscribe;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.annotation.Suspend.SCOPE;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.AtmosphereServlet.Action;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.cpr.HeaderConfig;
import org.jnap.core.mvc.support.RestfulHandlerAdapter;
import org.jnap.core.stereotype.RestController;
import org.jnap.core.util.TimeUnitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

/**
 * @author Daniel Rochetti
 * @since 0.9.3
 */
public class AsyncRequestInterceptor extends HandlerInterceptorAdapter
		implements AtmosphereHandler<HttpServletRequest, HttpServletResponse>,
		ApplicationContextAware, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(AsyncRequestInterceptor.class);

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private ContentNegotiatingViewResolver viewResolver;

	@Autowired
	private LocaleResolver localeResolver;

	private ApplicationContext applicationContext;

	private boolean useWebSocket = true;

	private boolean useNative = true;

    private boolean useBlocking = false;

    private boolean useStream = true;

	private AtmosphereServlet atmosphere = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(servletContext);
		final ServletContext sc = servletContext;

		final Map<String, String> params = new HashMap<String, String>();
		params.put(ApplicationConfig.WEBSOCKET_SUPPORT, Boolean.toString(this.useWebSocket));
		params.put(ApplicationConfig.PROPERTY_NATIVE_COMETSUPPORT, Boolean.toString(this.useNative));
		params.put(ApplicationConfig.PROPERTY_BLOCKING_COMETSUPPORT, Boolean.toString(this.useBlocking));
		params.put(ApplicationConfig.PROPERTY_USE_STREAM, Boolean.toString(this.useStream));
//		atmosphere = new AtmosphereServlet(true);
		atmosphere = new AtmosphereServlet();
		atmosphere.addAtmosphereHandler("/*", this);
		atmosphere.init(new ServletConfig() {
			
			@Override
			public String getServletName() {
				return AsyncRequestInterceptor.class.getSimpleName();
			}
			
			@Override
			public ServletContext getServletContext() {
				return sc;
			}
			
			@Override
			public Enumeration getInitParameterNames() {
				return new IteratorEnumeration(params.keySet().iterator());
			}
			
			@Override
			public String getInitParameter(String name) {
				return params.get(name);
			}
		});
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		Action action = null;
		if (isAsync(request, handler)) {
			action = atmosphere.doCometSupport(request, response);
		}
		return action == null || action.type != Action.TYPE.SUSPEND;
	}

	/**
	 * 
	 * @param request
	 * @param handler
	 * @return
	 */
	protected boolean isAsync(HttpServletRequest request, Object handler) {
		RestController controllerAnnotation = AnnotationUtils.findAnnotation(handler.getClass(), RestController.class);
		return (controllerAnnotation != null && controllerAnnotation.async())
				|| request.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT) != null
				|| request.getHeader("Sec-WebSocket-Version") != null;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (isAsync(request, handler)) {
			Method handlerMethod = (Method) request.getAttribute(RestfulHandlerAdapter.CURRENT_HANDLER_METHOD_ATTRIBUTE);
			if (handlerMethod != null) {
				LinkedList<AsyncResponseHandler> handlers = new LinkedList<AsyncResponseHandler>();
				AsyncResponseHandler responseHandler = null;

				if (AsyncResponseModel.class.isInstance(modelAndView)) {
					// TODO AsyncState.SUSPEND_RESPONSE
				}

				if (handlerMethod.isAnnotationPresent(Broadcast.class))  {
					Broadcast annotation = handlerMethod.getAnnotation(Broadcast.class);
					int delay = annotation.delay();
					Class[] suspendTimeout = annotation.value();

					AsyncState state = annotation.resumeOnBroadcast()
							? AsyncState.RESUME_ON_BROADCAST
							: AsyncState.BROADCAST;
					responseHandler = new AsyncResponseHandler(state, delay, 0,
							SCOPE.APPLICATION, true, suspendTimeout, null);
					handlers.addLast(responseHandler);

					if (handlerMethod.isAnnotationPresent(Cluster.class)) {
						// TODO add @Cluster support
					}
				}

				if (handlerMethod.isAnnotationPresent(Suspend.class)) {
					Suspend annotation = handlerMethod.getAnnotation(Suspend.class);
					long suspendTimeout = annotation.period();
					suspendTimeout = TimeUnitConverter.convert(suspendTimeout, annotation.timeUnit());

					Suspend.SCOPE scope = annotation.scope();
					boolean outputComments = annotation.outputComments();

					boolean trackable = false;
//					TODO add Trackable support
//					if (TrackableResource.class.isAssignableFrom(am.getMethod().getReturnType())) {
//						trackable = true;
//					}

					AsyncState state = annotation.resumeOnBroadcast()
							? AsyncState.SUSPEND_RESUME
							: AsyncState.SUSPEND;
					if (trackable) {
						state = AsyncState.SUSPEND_TRACKABLE;
					}
					responseHandler = new AsyncResponseHandler(state, suspendTimeout, 0, scope, outputComments);
					responseHandler.setListeners(createListeners(annotation.listeners()));
					handlers.addFirst(responseHandler);
				}

				if (handlerMethod.isAnnotationPresent(Subscribe.class)) {
					boolean trackable = false;
//					TODO add Trackable support
//					if (TrackableResource.class.isAssignableFrom(am.getMethod().getReturnType())) {
//						trackable = true;
//					}

					Subscribe annotation = handlerMethod.getAnnotation(Subscribe.class);
					AsyncState state = trackable ? AsyncState.SUBSCRIBE_TRACKABLE : AsyncState.SUBSCRIBE;
					String topic = annotation.value(); // TODO add SpEL support
					responseHandler = new AsyncResponseHandler(state, 30000, -1,
							Suspend.SCOPE.APPLICATION, false, null, topic);
					responseHandler.setListeners(createListeners(annotation.listeners()));

					handlers.addFirst(responseHandler);
				}

				if (handlerMethod.isAnnotationPresent(Publish.class)) {
					String topic = handlerMethod.getAnnotation(Publish.class).value(); // TODO add SpEL support
					responseHandler = new AsyncResponseHandler(AsyncState.PUBLISH, 30000, -1,
							Suspend.SCOPE.APPLICATION, false, null, topic);
					handlers.addFirst(responseHandler);
				}

				if (handlerMethod.isAnnotationPresent(Resume.class)) {
					handlers.addFirst(new AsyncResponseHandler(AsyncState.RESUME,
							handlerMethod.getAnnotation(Resume.class).value()));
				}

				if (handlerMethod.isAnnotationPresent(Schedule.class)) {
					Schedule annotation = handlerMethod.getAnnotation(Schedule.class);
					AsyncState state = annotation.resumeOnBroadcast()
							? AsyncState.SCHEDULE_RESUME
							: AsyncState.SCHEDULE;
					handlers.addFirst(new AsyncResponseHandler(state, annotation.period(), annotation.waitFor()));
				}

				for (AsyncResponseHandler asyncHandler : handlers) {
					asyncHandler.handle(request, response, modelAndView);
				}
			} else {
				logger.warn("Atmosphere annotation support disabled on this request.");
			}
		}
	}

	/**
	 * 
	 * @param listenerTypes
	 * @return
	 */
	protected AtmosphereResourceEventListener[] createListeners(
			Class<? extends AtmosphereResourceEventListener>[] listenerTypes) {
		AtmosphereResourceEventListener[] listeners = null;
		if (listenerTypes != null) {
			listeners = new AtmosphereResourceEventListener[listenerTypes.length];
			AtmosphereResourceEventListener listener = null;
			AutowireCapableBeanFactory autowireCapableBeanFactory = null;
			try {
				autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
			} catch (IllegalStateException e) {
				// ignore; listeners will not be inject with spring beans
			}
			for (int i = 0; i < listenerTypes.length; i++) {
				Class<? extends AtmosphereResourceEventListener> listenerType = listenerTypes[i];
				listener = BeanUtils.instantiate(listenerType);
				if (autowireCapableBeanFactory != null) {
					autowireCapableBeanFactory.autowireBean(listener);
				}
				listeners[i] = listener;
			}
		}
		return listeners;
	}

	/**
	 * @param request
	 * @return
	 */
	protected AtmosphereResource<HttpServletRequest, HttpServletResponse> getAtmosphereResource(
			HttpServletRequest request) {
		return (AtmosphereResource<HttpServletRequest, HttpServletResponse>) request.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);
	}

	@Override
	public void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource)	throws IOException {
		Assert.notNull(resource);
		resource.getRequest().setAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE, resource);
		resource.getRequest().setAttribute(FrameworkConfig.ATMOSPHERE_HANDLER, this);
	}

	@Override
	public void onStateChange(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) throws IOException {
		System.out.println("AsyncRequestInterceptor.onStateChange()");
		System.out.println(event.getMessage());
		HttpServletRequest req = event.getResource().getRequest();
		HttpServletResponse res = event.getResource().getResponse();
		try {
			ModelAndView mv = (ModelAndView) event.getMessage();
			View view = null;
			if (mv.isReference()) {
				view = this.viewResolver.resolveViewName(mv.getViewName(), this.localeResolver.resolveLocale(req));
			} else {
				view = mv.getView();
				if (view == null) {
					
				}
			}
			view.render(mv.getModelMap(), req, res);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public void destroy() {
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void setUseNative(boolean useNativeImplementation) {
		this.useNative = useNativeImplementation;
	}

	public void setUseBlocking(boolean useBlockingImplementation) {
		this.useBlocking = useBlockingImplementation;
	}

	public void setUseStream(boolean useStream) {
		this.useStream = useStream;
	}

	public void setViewResolver(ContentNegotiatingViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setUseWebSocket(boolean useWebSocket) {
		this.useWebSocket = useWebSocket;
	}

}
