package org.jnap.core.mvc.support;

public class LowerCasePathNameTransformer implements PathNameTransformer {

	@Override
	public String transform(String text) {
		return text.toLowerCase();
	}

}
