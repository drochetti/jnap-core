package org.jnap.core.mvc.bind;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.WebUtils;

public class CookieParamArgumentResolver extends AnnotatedWebArgumentResolver<CookieParam> {

	public CookieParamArgumentResolver() {
		super(CookieParam.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, CookieParam annotation) {
		Cookie cookie = WebUtils.getCookie(webRequest.getNativeRequest(HttpServletRequest.class), annotation.value());
		if (cookie != null) {
			final Class<?> parameterType = methodParameter.getParameterType();
			if (parameterType.equals(javax.ws.rs.core.Cookie.class)) {
				return new javax.ws.rs.core.Cookie(cookie.getName(), cookie.getValue(),
						cookie.getPath(), cookie.getDomain(), cookie.getVersion());
			} else if (!parameterType.equals(Cookie.class)) {
				return cookie.getValue();
			}
		}
		return cookie;
	}

}
