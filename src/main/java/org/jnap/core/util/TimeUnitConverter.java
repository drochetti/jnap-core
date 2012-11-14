/*
 * TimeUnitConverter.java created on 2011-12-01
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
package org.jnap.core.util;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Rochetti
 * @since 0.9.3
 */
public final class TimeUnitConverter {

	private TimeUnitConverter() {}

	/**
	 * 
	 * @param period
	 * @param unit
	 * @return
	 */
	public static long convert(long period, TimeUnit unit) {
		if (period == -1 || TimeUnit.MILLISECONDS.equals(unit)) {
			return period;
		} else {
			return TimeUnit.MILLISECONDS.convert(period, unit);
		}
	}

}
