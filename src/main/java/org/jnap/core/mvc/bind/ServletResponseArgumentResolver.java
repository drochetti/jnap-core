package org.jnap.core.mvc.bind;

import javax.servlet.ServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class ServletResponseArgumentResolver extends BaseWebArgumentResolver {

	public ServletResponseArgumentResolver() {
		setStrictTypeChecking(false);
		setSupportedTypes(ServletResponse.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		return webRequest.getNativeResponse(ServletResponse.class);
	}

}
