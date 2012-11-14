package org.jnap.core.mvc.support;

public class UnchangedPathNameTransformer implements PathNameTransformer {

	@Override
	public String transform(String text) {
		return text;
	}

}
