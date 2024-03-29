/*
 * QueryUtils.java created on 2011-12-26
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
package org.jnap.core.persistence.jpa;

import java.util.Map;

import javax.persistence.Query;

/**
 * General JPA {@link Query} utilities.
 * 
 * @author Daniel Rochetti
 * @since 0.9.4
 */
public final class QueryUtils {

	/**
	 * This is a helper class and should not be directly instantiated.
	 */
	private QueryUtils() {
	}

	/**
	 * 
	 * @param query
	 * @param params
	 */
	public static void setIndexedParameters(Query query, Object[] params) {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i + 1, params[i]);
			}
		}
	}

	/**
	 * 
	 * @param query
	 * @param params
	 */
	public static void setNamedParameters(Query query, Map<String, ?> params) {
		if (params != null && !params.isEmpty()) {
			for (String name : params.keySet()) {
				Object paramValue = params.get(name);
				query.setParameter(name, paramValue);
			}
		}
	}

	public static void setParameters(Query query, Object params) {
		if (params != null) {
			// testing if the query is using 'Named Parameters' or 'Indexed Parameters'
			if (params.getClass().isArray()) {
				QueryUtils.setIndexedParameters(query, (Object[]) params);
			} else if (params.getClass().isAssignableFrom(Map.class)) {
				QueryUtils.setNamedParameters(query, (Map<String, ?>) params);
			}
		}
	}

}
