package org.jnap.core.mvc.bind;

import javax.servlet.ServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class ServletRequestArgumentResolver extends BaseWebArgumentResolver {

	public ServletRequestArgumentResolver() {
		setStrictTypeChecking(false);
		setSupportedTypes(ServletRequest.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		return webRequest.getNativeRequest(ServletRequest.class);
	}

}
