/*
 * PersistentEnumFactory.java created on 2011-09-23
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
package org.jnap.core.bean.model;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * @author Daniel Rochetti
 */
public class PersistentEnumFactory {

	/**
	 * 
	 * @param value
	 * @param enumType
	 * @return
	 */
	public static <E extends Enum, V extends Serializable> E get(V value, Class<E> enumType) {
		Assert.isAssignable(PersistentEnum.class, enumType);
		E enumMatch = null;
		for (E enumValue : enumType.getEnumConstants()) {
			if (((PersistentEnum) enumValue).getValue().equals(value)) {
				enumMatch = enumValue;
				break;
			}
		}
		return enumMatch;
	}

}
