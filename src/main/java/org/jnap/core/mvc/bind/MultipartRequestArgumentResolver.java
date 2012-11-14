package org.jnap.core.mvc.bind;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartRequest;

@Deprecated
public class MultipartRequestArgumentResolver extends BaseWebArgumentResolver {

	public MultipartRequestArgumentResolver() {
		setStrictTypeChecking(false);
		setSupportedTypes(WebRequest.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		Object value = UNRESOLVED;
		MultipartRequest multipartRequest = webRequest.getNativeRequest(MultipartRequest.class);
		if (multipartRequest != null) {
			value = multipartRequest.getFileMap();
		}
		return value;
	}

}
