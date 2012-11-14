package org.jnap.core.mvc.bind;

import javax.ws.rs.FormParam;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class FormParamArgumentResolver extends AnnotatedWebArgumentResolver<FormParam> {

	public FormParamArgumentResolver() {
		super(FormParam.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, FormParam annotation) {
		return getParameter(webRequest, annotation.value());
	}

}
