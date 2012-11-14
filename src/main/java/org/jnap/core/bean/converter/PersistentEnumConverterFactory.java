/*
 * PersistentEnumConverterFactory.java created on 2011-06-14
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
package org.jnap.core.bean.converter;

import org.jnap.core.bean.model.PersistentEnum;
import org.jnap.core.bean.model.PersistentEnumFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Daniel Rochetti
 *
 */
public class PersistentEnumConverterFactory implements ConverterFactory<String, Enum> {

	@Override
	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		return targetType.isAssignableFrom(PersistentEnum.class)
				? (Converter<String, T>) new StringToPersistentEnum((Class<? extends Enum>) targetType)
				: new StringToEnum<T>(targetType);
	}

	private static class StringToPersistentEnum<T extends Enum> implements Converter<String, T> {

		private final Class<T> enumType;

		StringToPersistentEnum(Class<T> enumType) {
			super();
			this.enumType = enumType;
		}

		@Override
		public T convert(String source) {
			return (T) PersistentEnumFactory.get(source, enumType);
		}
		
	}

	private class StringToEnum<T extends Enum> implements Converter<String, T> {

		private final Class<T> enumType;

		StringToEnum(Class<T> enumType) {
			this.enumType = enumType;
		}

		public T convert(String source) {
			if (source.length() == 0) {
				// It's an empty enum identifier: reset the enum value to null.
				return null;
			}
			return (T) Enum.valueOf(this.enumType, source.trim());
		}
	}

}
