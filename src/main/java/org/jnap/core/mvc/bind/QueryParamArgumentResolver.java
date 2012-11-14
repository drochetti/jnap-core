package org.jnap.core.mvc.bind;

import javax.ws.rs.QueryParam;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class QueryParamArgumentResolver extends AnnotatedWebArgumentResolver<QueryParam> {

	public QueryParamArgumentResolver() {
		super(QueryParam.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, QueryParam annotation) {
		return getParameter(webRequest, annotation.value());
	}

}
