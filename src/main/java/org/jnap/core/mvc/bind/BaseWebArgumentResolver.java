package org.jnap.core.mvc.bind;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jnap.core.mvc.support.RestfulHandlerAdapter;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

public abstract class BaseWebArgumentResolver implements WebArgumentResolver {

	protected static final int SCOPE_PATH_PARAM = -1;

	private Class<?>[] supportedTypes;

	private boolean strictTypeChecking = true;

	@Override
	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		if (checkParameterType(methodParameter.getParameterType())) {
			return this.doResolveArgument(methodParameter, webRequest);
		}
		return UNRESOLVED;
	}

	protected abstract Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception;
	
	protected boolean checkParameterType(Class<?> parameterType) {
		boolean supported = this.supportedTypes == null;
		if (!supported) {
			for (int i = 0; i < this.supportedTypes.length; i++) {
				Class<?> type = this.supportedTypes[i];
				if (this.strictTypeChecking) {
					supported = parameterType.equals(type);
				} else {
					supported = type.isAssignableFrom(parameterType);
				}
				if (supported) {
					break;
				}
			}
		}
		return supported;
	}

	/**
	 * 
	 * @param webRequest
	 * @param name
	 * @param scope
	 * @return
	 */
	protected Object findAttribute(NativeWebRequest webRequest, String name, int scope) {
		Object value = null;
		if (scope == SCOPE_PATH_PARAM) {
			Map<String, String> pathParams = (Map<String, String>) webRequest.getAttribute(
					HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
			if (pathParams != null) {
				value = pathParams.get(name);
			}
		} else {
			value = webRequest.getAttribute(name, scope);
		}
		return value;
	}

	/**
	 * @param webRequest
	 * @param paramName
	 * @return
	 */
	protected Object getParameter(NativeWebRequest webRequest,
			final String paramName) {
		if (StringUtils.trimToNull(webRequest.getParameter(paramName)) != null) {
			String[] values = webRequest.getParameterValues(paramName);
			return values != null && values.length == 1 ? values[0] : values;
		}
		return null;
	}

	/**
	 * 
	 * @param obj
	 * @param request
	 */
	protected void bind(Object obj, WebRequest request) {
		WebRequestDataBinder binder = new WebRequestDataBinder(obj);
		binder.bind(request);
	}

	protected Object getCurrentHandler(NativeWebRequest webRequest) {
		return webRequest.getAttribute(RestfulHandlerAdapter.CURRENT_HANDLER_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
	}

	public Class<?>[] getSupportedTypes() {
		return supportedTypes;
	}

	public void setSupportedTypes(Class<?>... supportedTypes) {
		this.supportedTypes = supportedTypes;
	}

	public boolean isStrictTypeChecking() {
		return strictTypeChecking;
	}

	public void setStrictTypeChecking(boolean strictTypeChecking) {
		this.strictTypeChecking = strictTypeChecking;
	}

}
