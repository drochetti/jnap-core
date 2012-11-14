package org.jnap.core.mvc.bind;

import java.lang.annotation.Annotation;

import javax.ws.rs.DefaultValue;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 * @param <A>
 */
public abstract class AnnotatedWebArgumentResolver<A extends Annotation> extends BaseWebArgumentResolver {

	private Class<A> annotationType;
	private boolean requiredAnnotation = true;

	public AnnotatedWebArgumentResolver(Class<A> annotationType) {
		this.annotationType = annotationType;
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		A annotation = methodParameter.getParameterAnnotation(annotationType);
		if (annotation != null || !this.requiredAnnotation) {
			Object value = this.doResolveArgument(methodParameter, webRequest, annotation);
			DefaultValue defaultValue = methodParameter.getParameterAnnotation(DefaultValue.class);
			if (value == null && defaultValue != null) {
				value = defaultValue.value();
			}
			return value;
		}
		return UNRESOLVED;
	}

	protected abstract Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, A annotation) throws Exception;

	public boolean isRequiredAnnotation() {
		return requiredAnnotation;
	}

	public void setRequiredAnnotation(boolean requiredAnnotation) {
		this.requiredAnnotation = requiredAnnotation;
	}

}
