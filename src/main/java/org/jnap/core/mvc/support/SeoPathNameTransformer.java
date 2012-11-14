package org.jnap.core.mvc.support;

public class SeoPathNameTransformer implements PathNameTransformer {

	@Override
	public String transform(String text) {
		StringBuilder path = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isUpperCase(c) && path.length() > 0
					&& Character.isLowerCase(text.charAt(i - 1))) {
				path.append("-");
			}
			path.append(Character.toLowerCase(c));
		}
		return path.toString();
	}

}
