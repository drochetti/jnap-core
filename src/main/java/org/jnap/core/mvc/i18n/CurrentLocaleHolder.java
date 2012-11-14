/*
 * CurrentLocaleHolder.java created on 2012-04-02
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
package org.jnap.core.mvc.i18n;

import java.util.Locale;

/**
 * @author Daniel Rochetti
 */
public final class CurrentLocaleHolder {

	private static ThreadLocal<Locale> holder = new ThreadLocal<Locale>();

	public static Locale get() {
		return holder.get();
	}

	public static void set(Locale locale) {
		holder.set(locale);
	}

}
