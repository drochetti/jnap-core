package org.jnap.core.mvc.bind;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

public class WebRequestArgumentResolver extends BaseWebArgumentResolver {

	public WebRequestArgumentResolver() {
		setStrictTypeChecking(false);
		setSupportedTypes(WebRequest.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		return webRequest;
	}

}
