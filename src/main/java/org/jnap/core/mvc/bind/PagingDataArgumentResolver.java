/*
 * PagingDataArgumentResolver.java created on 2012-04-08
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

import org.jnap.core.bean.paging.PagingDataHolder.PagingData;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author drochetti
 *
 */
public class PagingDataArgumentResolver extends BaseWebArgumentResolver {

	public PagingDataArgumentResolver() {
		setSupportedTypes(PagingData.class);
	}

	@Override
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest) throws Exception {
		return PagingData.getCurrent();
	}

}
