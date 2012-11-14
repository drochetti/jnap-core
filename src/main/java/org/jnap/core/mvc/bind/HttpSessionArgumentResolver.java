package org.jnap.core.mvc.bind;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class HttpSessionArgumentResolver extends BaseWebArgumentResolver {

	public HttpSessionArgumentResolver() {
		setStrictTypeChecking(false);
		setSupportedTypes(HttpSession.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		Object session = null;
		HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
		if (req != null) {
			session = req.getSession();
		}
		return session;
	}

}
