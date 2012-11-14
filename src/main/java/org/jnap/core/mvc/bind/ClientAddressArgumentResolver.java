package org.jnap.core.mvc.bind;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jnap.core.mvc.bind.annotation.ClientAddress;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class ClientAddressArgumentResolver extends AnnotatedWebArgumentResolver<ClientAddress> {

	public ClientAddressArgumentResolver() {
		super(ClientAddress.class);
		setSupportedTypes(String.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, ClientAddress annotation) {
		String clientAddress = webRequest.getHeader("X-Forwaded-For");
		if (StringUtils.isBlank(clientAddress)) {
			clientAddress = webRequest.getNativeRequest(HttpServletRequest.class).getRemoteAddr();
		}
		return clientAddress;
	}

}
