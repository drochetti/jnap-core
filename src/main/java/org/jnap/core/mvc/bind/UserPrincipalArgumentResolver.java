package org.jnap.core.mvc.bind;

import javax.servlet.http.HttpServletRequest;

import org.jnap.core.mvc.bind.annotation.UserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class UserPrincipalArgumentResolver extends AnnotatedWebArgumentResolver<UserPrincipal> {

	public UserPrincipalArgumentResolver() {
		super(UserPrincipal.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, UserPrincipal annotation) {
		HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
		return req.getUserPrincipal();
	}

}
