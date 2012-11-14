/*
 * RestfulMappingCacheInterceptor.java created on 2012-04-19
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
package org.jnap.core.mvc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jnap.core.mvc.support.RestfulMappingCache;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Daniel Rochetti
 */
public class RestfulMappingCacheInterceptor extends HandlerInterceptorAdapter {

	private RestfulMappingCache mappingCache;

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		mappingCache.clearCurrentRequestMapping();
	}

	public void setMappingCache(RestfulMappingCache mappingCache) {
		this.mappingCache = mappingCache;
	}

}
