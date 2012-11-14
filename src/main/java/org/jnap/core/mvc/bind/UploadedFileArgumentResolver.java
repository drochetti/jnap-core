package org.jnap.core.mvc.bind;

import java.util.List;

import org.jnap.core.mvc.bind.annotation.UploadedFile;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class UploadedFileArgumentResolver extends AnnotatedWebArgumentResolver<UploadedFile> {

	public UploadedFileArgumentResolver() {
		super(UploadedFile.class);
		setStrictTypeChecking(false);
		setSupportedTypes(MultipartFile.class, List.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, UploadedFile annotation) {
		DefaultMultipartHttpServletRequest multiparRequest = webRequest.getNativeRequest(
				DefaultMultipartHttpServletRequest.class);
		if (multiparRequest != null) {
			return methodParameter.getParameterType().isAssignableFrom(List.class)
					? multiparRequest.getFiles(annotation.value())
					: multiparRequest.getFile(annotation.value());
		} else {
			// conditional exception? annotation parameter?
			throw new IllegalStateException(""); // TODO msg
		}
	}

}
