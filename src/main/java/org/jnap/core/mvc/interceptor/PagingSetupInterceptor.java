/*
 * PagingInterceptor.java created on 15/08/2011
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

import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnap.core.bean.paging.PagingDataHolder;
import org.jnap.core.bean.paging.PagingDataHolder.PagingData;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Daniel Rochetti
 */
public class PagingSetupInterceptor extends HandlerInterceptorAdapter {

	private static Log logger = LogFactory.getLog(PagingSetupInterceptor.class);

	private String startParamName = "start";
	private String limitParamName = "limit";

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		Map<String, Object> params = request.getParameterMap();
		if (params.containsKey(this.startParamName)	&& params.containsKey(this.limitParamName)) {

			String startParam = null;
			String limitParam = null;
			try {
				startParam = getParameterAsString(params, this.startParamName);
				limitParam = getParameterAsString(params, this.limitParamName);
				Integer start = Integer.parseInt(startParam);
				Integer limit = Integer.parseInt(limitParam);

				if (start != null && limit != null) {
					PagingDataHolder.setCurrentPage((start + limit) / limit);
					PagingDataHolder.setResultsPerPage(limit);
				}
			} catch (Exception e) {
				logger.warn(MessageFormat.format("Warning! Paging was not set because paging "
						+ "params were set, but in the wrong format: start={0}, limit={1}",
						startParam, limitParam), e);
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (PagingDataHolder.isPagingSet() && modelAndView != null) {
			final PagingData paging = PagingDataHolder.PagingData.getCurrent();
			modelAndView.getModel().put("pagingTotal", paging.getTotalResults());
			modelAndView.getModel().put("pagingCurrentPage", paging.getCurrentPage());
			modelAndView.getModel().put("pagingPageSize", paging.getResultsPerPage());
		}
	}

	/**
	 * 
	 * @param params
	 * @param paramName
	 * @return
	 */
	private String getParameterAsString(Map<String, Object> params,	String paramName) {
		String value = null;
		if (params.containsKey(paramName)) {
			Object objValue = params.get(paramName);
			if (objValue instanceof String) {
				value = objValue.toString();
			} else if (objValue instanceof String[]) {
				String[] values = (String[]) objValue;
				if (values.length > 0) {
					value = values[0];
				}
			}
		}
		return value;
	}

	public void setStartParamName(String startParamName) {
		this.startParamName = startParamName;
	}

	public void setLimitParamName(String limitParamName) {
		this.limitParamName = limitParamName;
	}

}
