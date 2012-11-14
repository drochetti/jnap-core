/*
 * DefaultI18nTextProvider.java created on 2010-07-05
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnap.core.i18n;

import java.util.Locale;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
@Deprecated
public class DefaultI18nTextProvider implements I18nTextProvider {

	public Locale getLocale() {
		return Locale.getDefault();
	}

	public String getText(String key) {
		return key;
	}

	public String getText(String key, String... args) {
		return getText(key);
	}

	public String getText(String key, String defaultText, String... args) {
		return getText(key);
	}

}
