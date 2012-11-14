/*
 * AtmosphereResourceArgumentResolver.java created on 2011-11-21
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
package org.jnap.core.mvc.bind;

import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author Daniel Rochetti
 */
public class AtmosphereResourceArgumentResolver extends BaseWebArgumentResolver {

	public AtmosphereResourceArgumentResolver() {
		super();
		setSupportedTypes(AtmosphereResource.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		return this.getAtmosphereResource(webRequest, true);
	}

	protected AtmosphereResource getAtmosphereResource(NativeWebRequest webRequest, boolean session) {
		HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
		AtmosphereResource resource = null;

		if (session) {
			if ((Boolean) req.getAttribute(FrameworkConfig.SUPPORT_SESSION)) {
//				resource = (AtmosphereResource) req.getSession().getAttribute(AtmosphereFilter.SUSPENDED_RESOURCE); TODO check
			}
		}

		if (resource == null) {
			resource = (AtmosphereResource) req.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);
		}

		return resource;
	}

}
