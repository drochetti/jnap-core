package org.jnap.core.mvc.bind;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jnap.core.mvc.bind.annotation.SessionId;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author Daniel Rochetti
 */
public class SessionIdArgumentResolver extends AnnotatedWebArgumentResolver<SessionId> {


	public SessionIdArgumentResolver() {
		super(SessionId.class);
		setStrictTypeChecking(true);
		setSupportedTypes(String.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, SessionId annotation) {
		String value = null;
		HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
		if (req != null) {
			HttpSession session = req.getSession(annotation.canCreateNew());
			value = session != null ? session.getId() : null;
		}
		return value;
	}

}
