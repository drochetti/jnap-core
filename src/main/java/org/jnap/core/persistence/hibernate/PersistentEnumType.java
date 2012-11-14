/*
 * PersistentEnumType.java created on 2011-01-11
 *
 * Copyright 2011 Brushing Bits, Inc.
 * http://www.brushingbits.com
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
package org.jnap.core.persistence.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.TypeResolver;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class PersistentEnumType implements UserType, ParameterizedType {

	public static final String TYPE = "org.jnap.core.persistence.hibernate.PersistentEnumType";

	public static final String ENUM_CLASS_PARAM = "enumClass";

	public static final String IDENTIFIER_METHOD_PARAM = "identifierMethod";

	private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "getValue";

	private Class<? extends Enum> enumClass;
	private Class<?> identifierType;
	private Method identifierMethod;
	private Method valuesMethod;
	private AbstractSingleColumnStandardBasicType type;
	private int[] sqlTypes;

	public void setParameterValues(Properties parameters) {
		String enumClassName = parameters.getProperty(ENUM_CLASS_PARAM);
		try {
			enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
		} catch (ClassNotFoundException cfne) {
			throw new HibernateException("Enum class not found", cfne);
		}

		String identifierMethodName = parameters.getProperty(
				IDENTIFIER_METHOD_PARAM, DEFAULT_IDENTIFIER_METHOD_NAME);

		try {
			identifierMethod = enumClass.getMethod(identifierMethodName, new Class[0]);
			identifierType = identifierMethod.getReturnType();
		} catch (Exception e) {
			throw new HibernateException("Failed to obtain identifier method", e);
		}

		type = (AbstractSingleColumnStandardBasicType) new TypeResolver().basic(identifierType.getName());

		if (type == null) {
			throw new HibernateException("Unsupported identifier type "	+ identifierType.getName());
		}

		sqlTypes = new int[] { type.getSqlTypeDescriptor().getSqlType() };

		try {
			valuesMethod = enumClass.getMethod("values", ArrayUtils.EMPTY_CLASS_ARRAY);
		} catch (Exception e) {
			throw new HibernateException("Failed to obtain values method", e);
		}
	}

	public Class returnedClass() {
		return enumClass;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		Object identifier = type.nullSafeGet(rs, names[0]);
		if (rs.wasNull()) {
			return null;
		}

		try {
			Enum[] enumValues = (Enum[]) valuesMethod.invoke(null);
			Enum matchedEnum = null;
			for (Enum enumValue : enumValues) {
				if (identifier.equals(identifierMethod.invoke(enumValue))) {
					matchedEnum = enumValue;
					break;
				}
			}
			if (matchedEnum == null) {
				// TODO warn, exception?
			}
			return matchedEnum;
		} catch (Exception e) {
			throw new HibernateException("Exception while invoking valueOf method '"
							+ valuesMethod.getName() + "' of enumeration class '" + enumClass + "'", e);
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		try {
			if (value == null) {
				st.setNull(index, type.getSqlTypeDescriptor().getSqlType());
			} else {
				Object identifier = identifierMethod.invoke(value, new Object[0]);
				type.nullSafeSet(st, identifier, index);
			}
		} catch (Exception e) {
			throw new HibernateException("Exception while invoking identifierMethod '"
							+ identifierMethod.getName() + "' of "
							+ "enumeration class '" + enumClass + "'", e);
		}
	}

	public int[] sqlTypes() {
		return sqlTypes;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return x == y;
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

}
