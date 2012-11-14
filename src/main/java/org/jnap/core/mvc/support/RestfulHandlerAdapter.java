package org.jnap.core.mvc.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.jnap.core.mvc.ResponseModel;
import org.jnap.core.mvc.bind.AtmosphereResourceArgumentResolver;
import org.jnap.core.mvc.bind.BaseWebArgumentResolver;
import org.jnap.core.mvc.bind.BroadcasterArgumentResolver;
import org.jnap.core.mvc.bind.BroadcasterFactoryArgumentResolver;
import org.jnap.core.mvc.bind.ClientAddressArgumentResolver;
import org.jnap.core.mvc.bind.CookieParamArgumentResolver;
import org.jnap.core.mvc.bind.FormParamArgumentResolver;
import org.jnap.core.mvc.bind.HeadParamArgumentResolver;
import org.jnap.core.mvc.bind.ModelLoadArgumentResolver;
import org.jnap.core.mvc.bind.PagingDataArgumentResolver;
import org.jnap.core.mvc.bind.PathParamArgumentResolver;
import org.jnap.core.mvc.bind.QueryParamArgumentResolver;
import org.jnap.core.mvc.bind.ServletRequestArgumentResolver;
import org.jnap.core.mvc.bind.ServletResponseArgumentResolver;
import org.jnap.core.mvc.bind.SessionIdArgumentResolver;
import org.jnap.core.mvc.bind.UploadedFileArgumentResolver;
import org.jnap.core.mvc.bind.UserPrincipalArgumentResolver;
import org.jnap.core.mvc.bind.WebRequestArgumentResolver;
import org.jnap.core.validation.ValidationConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestfulHandlerAdapter implements HandlerAdapter, Ordered, ApplicationContextAware {

	public static final String CURRENT_HANDLER_ATTRIBUTE = RestfulHandlerAdapter.class.getName() + ".currentHandler";

	public static final String CURRENT_HANDLER_METHOD_ATTRIBUTE = RestfulHandlerAdapter.class.getName() + ".currentMethodHandler";

	public static final String CURRENT_VIEW_PATH_ATTRIBUTE = RestfulHandlerAdapter.class.getName() + ".currentViewPath";


	private ApplicationContext applicationContext;

	private int order = Ordered.HIGHEST_PRECEDENCE;

	private RestfulMappingCache mappingCache;

	private WebArgumentResolver[] defaultArgumentResolvers;

	private WebArgumentResolver[] customArgumentResolvers;

	private ModelAndViewResolver[] customModelAndViewResolvers;

	private HttpMessageConverter<?>[] messageConverters;

	private ValidatorFactory validatorFactory;

	private boolean throwExceptionOnBindingError = false;

	private boolean autoRegisterArgumentResolvers = true;

	@PostConstruct
	public void initializeDefaultArgumentResolvers() {
		Class<?>[] resolverTypes = { PathParamArgumentResolver.class, ModelLoadArgumentResolver.class,
				QueryParamArgumentResolver.class, FormParamArgumentResolver.class,
				WebRequestArgumentResolver.class, ServletRequestArgumentResolver.class,
				ServletResponseArgumentResolver.class, CookieParamArgumentResolver.class,
				SessionIdArgumentResolver.class, ClientAddressArgumentResolver.class,
				PagingDataArgumentResolver.class,
				HeadParamArgumentResolver.class, UserPrincipalArgumentResolver.class,
				UploadedFileArgumentResolver.class,	AtmosphereResourceArgumentResolver.class,
				BroadcasterArgumentResolver.class, BroadcasterFactoryArgumentResolver.class };
		List<WebArgumentResolver> resolvers = new ArrayList<WebArgumentResolver>(resolverTypes.length);
		for (int i = 0; i < resolverTypes.length; i++) {
			WebArgumentResolver resolver = (WebArgumentResolver) createBean(resolverTypes[i]);
			resolvers.add(resolver);
		}

		if (this.autoRegisterArgumentResolvers) {
			Map<String, BaseWebArgumentResolver> userResolvers = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					applicationContext, BaseWebArgumentResolver.class);
			for (BaseWebArgumentResolver webArgumentResolver : userResolvers.values()) {
				resolvers.add(webArgumentResolver);
			}
		}
		this.defaultArgumentResolvers = resolvers.toArray(new WebArgumentResolver[resolvers.size()]);
	}

	private <T> T createBean(Class<T> beanType) {
		try {
			AutowireCapableBeanFactory autowireFactory = this.applicationContext.getAutowireCapableBeanFactory();
			return (T) autowireFactory.autowire(beanType, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		} catch (IllegalStateException e) {
			// TODO log not autowire capable bean factory
			return null;
		}
	}

	@Override
	public boolean supports(Object handler) {
		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		return mappingCache.isMappedHandler(handlerClass);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		RestfulMapping mapping = this.mappingCache.getCurrentRequestMapping();
		ModelAndView result = null;
		if (mapping != null) {
			result = this.invokeHandlerMethod(request, response, handler, mapping);
		}
		return result;
	}

	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, Object handler, RestfulMapping mapping)
			throws Exception {

		final NativeWebRequest webRequest = new ServletWebRequest(request, response);
		webRequest.setAttribute(CURRENT_HANDLER_ATTRIBUTE, handler, WebRequest.SCOPE_REQUEST);

		Method handlerMethod = BridgeMethodResolver.findBridgedMethod(mapping.getMethod());
		webRequest.setAttribute(CURRENT_HANDLER_METHOD_ATTRIBUTE, handlerMethod, WebRequest.SCOPE_REQUEST);
		MethodParameter[] methodParameters = this.resolveMethodParameters(handlerMethod);
		Class[] paramTypes = handlerMethod.getParameterTypes();
		Object[] args = new Object[methodParameters.length];
		ExtendedModelMap implicitModel = new BindingAwareModelMap();

		// merge default and custom argument resolvers
		final List<WebArgumentResolver> argumentResolvers = new LinkedList<WebArgumentResolver>();
		if (this.customArgumentResolvers != null) {
			argumentResolvers.addAll(Arrays.asList(this.customArgumentResolvers));
		}
		argumentResolvers.addAll(Arrays.asList(this.defaultArgumentResolvers));

		for (int i = 0; i < methodParameters.length; i++) {
			Object argValue = WebArgumentResolver.UNRESOLVED;
			MethodParameter methodParameter = methodParameters[i];
			boolean validate = methodParameter.getParameterAnnotation(Valid.class) != null;
			for (WebArgumentResolver webArgumentResolver : argumentResolvers) {
				argValue = webArgumentResolver.resolveArgument(methodParameter, webRequest);
				if (argValue != WebArgumentResolver.UNRESOLVED) {
					break;
				}
			}
			if (argValue == WebArgumentResolver.UNRESOLVED) {
				ModelAttribute modelAttribute = methodParameter.getParameterAnnotation(ModelAttribute.class);
				boolean isSimpleProperty = BeanUtils.isSimpleProperty(paramTypes[i]);
				if (modelAttribute != null || !isSimpleProperty) {
					String attrName = null;
					if (modelAttribute != null) {
						attrName = modelAttribute.value();
					}
					if (attrName == null || attrName.trim().length() == 0) {
						attrName = Conventions.getVariableNameForParameter(methodParameter);
					}
					WebDataBinder binder = this.resolveModelAttribute(attrName, methodParameter,
							implicitModel, webRequest, handler);
					Object target = binder.getTarget();
					if (target != null) {
						((WebRequestDataBinder) binder).bind(webRequest);
						if (validate) {
							boolean assignBindingResult = (args.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i]));
							this.validate(binder, AnnotationUtils.findAnnotation(handlerMethod, ValidationConfig.class));
							final BindingResult bindingResult = binder.getBindingResult();
							if (assignBindingResult) {
								args[++i] = bindingResult;
							} else if (bindingResult.hasErrors()) {
								if (this.throwExceptionOnBindingError) {
									throw new BindException(bindingResult);
								} else {
									ModelAndView bindingResultModel = new ModelAndView("error");
									bindingResultModel.addObject("result", bindingResult.getAllErrors());
									bindingResultModel.addObject("allErrors", bindingResult.getAllErrors());
									bindingResultModel.addObject("errorCount", bindingResult.getErrorCount());
									response.setStatus(400); // TODO 422?
									return bindingResultModel;
								}
							}
						}
					}
					argValue = target;
				} else if (isSimpleProperty) {
//					argValue = resolveRequestProperty(); TODO check before 1.0
				} else {
					argValue = null;
				}
			}

			// convert the value if necessary
			if (argValue != null) {
				WebDataBinder binder = new WebRequestDataBinder(null); // TODO name?
				argValue = binder.convertIfNecessary(argValue, methodParameter.getParameterType());
			}
			args[i] = argValue;
		}

		ReflectionUtils.makeAccessible(handlerMethod);
		Object result = handlerMethod.invoke(handler, args);
		webRequest.removeAttribute(CURRENT_HANDLER_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
		
		ModelAndView modelAndView = resolveModelAndView(handlerMethod, ClassUtils.getUserClass(handler),
				result, implicitModel, webRequest);
		if (modelAndView != null && ResponseModel.class.isInstance(modelAndView)) {
			ResponseModel responseModel = (ResponseModel) modelAndView;
			int status = responseModel.getStatusCode();
			if (status != 200) {
				response.setStatus(status);
			}
		}
		return modelAndView;
	}

	private WebDataBinder resolveModelAttribute(String attrName,
			MethodParameter methodParam, ExtendedModelMap implicitModel,
			NativeWebRequest webRequest, Object handler) throws Exception {

		String name = attrName;
		if ("".equals(name)) {
			name = Conventions.getVariableNameForParameter(methodParam);
		}
		Class<?> paramType = GenericTypeResolver.resolveParameterType(methodParam, handler.getClass());
		Object bindObject;
		if (implicitModel.containsKey(name)) {
			bindObject = implicitModel.get(name);
		} else {
			bindObject = BeanUtils.instantiateClass(paramType);
		}
		WebDataBinder binder = new WebRequestDataBinder(bindObject, name);
		// TODO initBinder - simpler interface method call (InitBinderAware?) instead of method discovery
		return binder;
	}

	protected void validate(WebDataBinder binder, ValidationConfig validationConfig) {
		Validator validator = this.validatorFactory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(binder.getTarget(),
				validationConfig != null ? validationConfig.groups() : new Class[] { Default.class });
		BindingResult result = binder.getBindingResult();
		for (ConstraintViolation<Object> constraintViolation : violations) {
			result.rejectValue(
					constraintViolation.getPropertyPath().toString(),
					constraintViolation.getMessage());
		}
	}

	private ModelAndView resolveModelAndView(Method handlerMethod, Class<?> handlerType, Object result,
			ExtendedModelMap implicitModel, NativeWebRequest webRequest) {
		ModelAndView modelAndView = null;
		if (this.customModelAndViewResolvers != null) {
			for (ModelAndViewResolver modelAndViewResolver : this.customModelAndViewResolvers) {
				modelAndView = modelAndViewResolver.resolveModelAndView(handlerMethod, handlerType,
						result, implicitModel, webRequest);
			}
		}
		if (modelAndView == null) {
			if (result instanceof ModelAndView) {
				modelAndView = ModelAndView.class.cast(result);
			} else if (result != null) {
				modelAndView = new ModelAndView();
				modelAndView.addObject(result);
			}
		}
		modelAndView.addAllObjects(implicitModel); // TODO check this
		return modelAndView;
	}

	private MethodParameter[] resolveMethodParameters(Method bridgeMethod) {
		Class<?>[] params = bridgeMethod.getParameterTypes();
		MethodParameter[] methodParameters = new MethodParameter[params.length];
		for (int i = 0; i < params.length; i++) {
			methodParameters[i] = new MethodParameter(bridgeMethod, i);
		}
		return methodParameters;
	}

	/**
	 * <p>Set one or more custom WebArgumentResolvers to use for special method
	 * parameter types.</p>
	 * <p>Any such custom WebArgumentResolver will kick in first, having a chance
	 * to resolve an argument value before the standard argument handling kicks
	 * in.</p>
	 */
	public void setCustomArgumentResolvers(WebArgumentResolver[] argumentResolvers) {
		this.customArgumentResolvers = argumentResolvers;
	}

	/**
	 * <p>Set a custom ModelAndViewResolvers to use for special method return
	 * types.</p>
	 * <p>Such a custom ModelAndViewResolver will kick in first, having a chance to
	 * resolve a return value before the standard ModelAndView handling kicks
	 * in.</p>
	 */
	public void setCustomModelAndViewResolver(
			ModelAndViewResolver customModelAndViewResolver) {
		this.customModelAndViewResolvers = new ModelAndViewResolver[] { customModelAndViewResolver };
	}

	/**
	 * <p>Set one or more custom ModelAndViewResolvers to use for special method
	 * return types.</p>
	 * <p>Any such custom ModelAndViewResolver will kick in first, having a chance
	 * to resolve a return value before the standard ModelAndView handling kicks
	 * in.</p>
	 */
	public void setCustomModelAndViewResolvers(
			ModelAndViewResolver[] customModelAndViewResolvers) {
		this.customModelAndViewResolvers = customModelAndViewResolvers;
	}

	/**
	 * <p>Set the message body converters to use.</p>
	 * <p>These converters are used to convert from and to HTTP requests and responses.</p>
	 */
	public void setMessageConverters(HttpMessageConverter<?>[] messageConverters) {
		this.messageConverters = messageConverters;
	}

	/**
	 * Return the message body converters that this adapter has been configured with.
	 */
	public HttpMessageConverter<?>[] getMessageConverters() {
		return messageConverters;
	}

	public void setMappingCache(RestfulMappingCache mappingCache) {
		this.mappingCache = mappingCache;
	}

	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}

	public void setThrowExceptionOnBindingError(boolean throwExceptionOnBindingError) {
		this.throwExceptionOnBindingError = throwExceptionOnBindingError;
	}

	public void setAutoRegisterArgumentResolvers(boolean autoRegisterArgumentResolvers) {
		this.autoRegisterArgumentResolvers = autoRegisterArgumentResolvers;
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
