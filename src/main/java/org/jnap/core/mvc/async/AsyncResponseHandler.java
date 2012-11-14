/*
 * AsyncResponseHandler.java created on 2011-11-30
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

import static org.atmosphere.cpr.HeaderConfig.LONG_POLLING_TRANSPORT;
import static org.atmosphere.cpr.HeaderConfig.WEBSOCKET_UPGRADE;
import static org.atmosphere.cpr.HeaderConfig.X_ATMOSPHERE_TRANSPORT;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereEventLifecycle;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.BroadcastFilter;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.ClusterBroadcastFilter;
import org.atmosphere.cpr.FrameworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Rochetti
 * @since 0.9.3
 */
public class AsyncResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(AsyncResponseHandler.class);

	private final AsyncState state;
	private final long timeout;
	private final int waitFor;
	private final Suspend.SCOPE scope;
	private final boolean outputComments;
	private final Class<BroadcastFilter>[] filters;
	private final String topic;

	private AtmosphereResourceEventListener[] listeners = null;
	private final ArrayList<ClusterBroadcastFilter> clusters = new ArrayList<ClusterBroadcastFilter>();


	public AsyncResponseHandler(AsyncState state) {
		this(state, -1);
	}

	public AsyncResponseHandler(AsyncState state, long timeout) {
		this(state, timeout, 0);
	}

	public AsyncResponseHandler(AsyncState state, long timeout, int waitFor) {
		this(state, timeout, waitFor, Suspend.SCOPE.APPLICATION);
	}

	public AsyncResponseHandler(AsyncState state, long timeout, int waitFor, Suspend.SCOPE scope) {
		this(state, timeout, waitFor, scope, true);
	}

	public AsyncResponseHandler(AsyncState state, long timeout, int waitFor,
			Suspend.SCOPE scope, boolean outputComments) {
		this(state, timeout, waitFor, scope, outputComments, null, null);
	}

	public AsyncResponseHandler(AsyncState state, long timeout, int waitFor,
			Suspend.SCOPE scope, boolean outputComments,
			Class<BroadcastFilter>[] filters, String topic) {
		this.state = state;
		this.timeout = timeout;
		this.waitFor = waitFor;
		this.scope = scope;
		this.outputComments = outputComments;
		this.filters = filters;
		this.topic = topic;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param modelAndView
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response,
			ModelAndView modelAndView) {

		AtmosphereResource<HttpServletRequest, HttpServletResponse> asyncResource =
				(AtmosphereResource<HttpServletRequest, HttpServletResponse>) request
				.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);

		boolean sessionSupported = (Boolean) request.getAttribute(FrameworkConfig.SUPPORT_SESSION);

		AsyncResponseModel asyncResponseModel = AsyncResponseModel.class.isInstance(modelAndView)
				? AsyncResponseModel.class.cast(modelAndView) : null;

		switch (state) {
		case SUSPEND_RESPONSE:
			Assert.notNull(asyncResponseModel); // TODO detailed message
			break;
		case SUBSCRIBE_TRACKABLE:
		case SUBSCRIBE:
		case SUSPEND_TRACKABLE:
		case SUSPEND:
		case SUSPEND_RESUME:

			boolean outputJunk = shouldOutputComments(request, outputComments);
			boolean resumeOnBroadcast = shouldResumeOnBroadcast(request, (state == AsyncState.SUSPEND_RESUME));
			bindEventListeners(asyncResource);

			Broadcaster broadcaster = null; // TODO (Broadcaster) servletReq.getAttribute(INJECTED_BROADCASTER);

			if (state == AsyncState.SUBSCRIBE) {
				broadcaster = resolveBroadcaster(request);
			}

			suspend(sessionSupported, resumeOnBroadcast, outputJunk,
					this.timeout, request, response, broadcaster,
					asyncResource, this.scope);

			break;
		case RESUME:
			break;
		case BROADCAST:
		case PUBLISH:
		case RESUME_ON_BROADCAST:

			
			break;
		case SCHEDULE:
		case SCHEDULE_RESUME:
		default:
			throw new IllegalStateException("");
		}
	}

	/**
	 * 
	 * @param sessionSupported
	 * @param resumeOnBroadcast
	 * @param comments
	 * @param timeout
	 * @param request
	 * @param response
	 * @param broadcaster
	 * @param resource
	 * @param localScope
	 */
	protected void suspend(boolean sessionSupported, boolean resumeOnBroadcast,
			boolean comments, long timeout, HttpServletRequest request,
			HttpServletResponse response, Broadcaster broadcaster,
			AtmosphereResource<HttpServletRequest, HttpServletResponse> resource,
			Suspend.SCOPE localScope) {

		BroadcasterFactory broadcasterFactory = (BroadcasterFactory) request.getAttribute(ApplicationConfig.BROADCASTER_FACTORY);

		resource.suspend(timeout, comments);
	}

	/**
	 * @param asyncResource
	 */
	protected void bindEventListeners(AtmosphereResource<HttpServletRequest, HttpServletResponse> asyncResource) {
		if (listeners != null && listeners.length > 0 &&  AtmosphereEventLifecycle.class.isInstance(asyncResource)) {
			AtmosphereEventLifecycle asyncEventLifecycle = AtmosphereEventLifecycle.class.cast(asyncResource);
			for (AtmosphereResourceEventListener listener : listeners) {
				asyncEventLifecycle.addEventListener(listener);
			}
		}
	}

	/**
	 * 
	 * @param request
	 * @param shouldResume
	 * @return
	 */
	protected boolean shouldResumeOnBroadcast(HttpServletRequest request, boolean shouldResume) {
		String transport = request.getHeader(X_ATMOSPHERE_TRANSPORT);
		if (transport != null && transport.equals(LONG_POLLING_TRANSPORT)) {
			return true;
		}
		return shouldResume;
	}

	/**
	 * 
	 * @param request
	 * @param output
	 * @return
	 */
	protected boolean shouldOutputComments(HttpServletRequest request, boolean output) {
		boolean webSocketEnabled = false;
		if (request.getHeaders("Connection") != null
				&& request.getHeaders("Connection").hasMoreElements()) {
			String[] e = ((Enumeration<String>) request.getHeaders("Connection")).nextElement().split(",");
			for (String upgrade : e) {
				if (upgrade.trim().equalsIgnoreCase(WEBSOCKET_UPGRADE)) {
					webSocketEnabled = true;
					break;
				}
			}
		}

		String transport = request.getHeader(X_ATMOSPHERE_TRANSPORT);
		if (webSocketEnabled
				|| (transport != null && transport.equals(LONG_POLLING_TRANSPORT))) {
			return false;
		}

		return output;
	}

	/**
	 * @param request
	 * @return 
	 */
	protected Broadcaster resolveBroadcaster(HttpServletRequest request) {
		Class<Broadcaster> broadcasterType = null;
		try {
			broadcasterType = (Class<Broadcaster>) Class.forName((String) request.getAttribute(
					ApplicationConfig.BROADCASTER_CLASS));
		} catch (Throwable e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return BroadcasterFactory.getDefault().lookup(broadcasterType, topic, true);
	}

	public void setListeners(AtmosphereResourceEventListener[] listeners) {
		this.listeners = listeners;
	}

}
