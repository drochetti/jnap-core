package org.jnap.core.mvc.support;

public class JavaIdentifierPathNameTransformer implements PathNameTransformer {

	@Override
	public String transform(String text) {
		return Character.toLowerCase(text.charAt(0)) + text.substring(1);
	}

}
