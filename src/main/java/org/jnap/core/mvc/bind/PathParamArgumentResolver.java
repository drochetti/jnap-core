package org.jnap.core.mvc.bind;

import javax.ws.rs.PathParam;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class PathParamArgumentResolver extends AnnotatedWebArgumentResolver<PathParam> {

	public PathParamArgumentResolver() {
		super(PathParam.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, PathParam annotation) {
		return findAttribute(webRequest, annotation.value(), SCOPE_PATH_PARAM);
	}

}
