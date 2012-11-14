package org.jnap.core.mvc.bind;

import javax.ws.rs.HeaderParam;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class HeadParamArgumentResolver extends AnnotatedWebArgumentResolver<HeaderParam> {

	public HeadParamArgumentResolver() {
		super(HeaderParam.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, HeaderParam annotation) {
		return webRequest.getHeader(annotation.value());
	}

}
