/*
 * CompositeLocaleResolver.java created on 2011-11-23
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

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.CookieGenerator;

/**
 * @author Daniel Rochetti
 */
public class CompositeLocaleResolver implements LocaleResolver, InitializingBean {

	private List<LocaleResolver> resolvers;

	private boolean throwExceptionIfReadOnly = false;

	private String defaultLocale;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(resolvers, "You must set at least one LocaleResolver strategy.");
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = null;
		for (LocaleResolver localeResolver : this.resolvers) {
			locale = localeResolver.resolveLocale(request);
			if (locale != null) {
				break;
			}
		}
		if (locale == null) {
			locale = defaultLocale != null ? StringUtils.parseLocaleString(defaultLocale) : Locale.getDefault();
		}
		CurrentLocaleHolder.set(locale);
		return locale;
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		for (LocaleResolver localeResolver : this.resolvers) {
			try {
				configCookieScope(localeResolver, request);
				localeResolver.setLocale(request, response, locale);
				break;
			} catch (UnsupportedOperationException e) {
				if (throwExceptionIfReadOnly) {
					throw e;
				}
			}
		}
	}

	/**
	 * TODO doc
	 * @param localeResolver
	 * @param request
	 */
	private void configCookieScope(LocaleResolver localeResolver, HttpServletRequest request) {
		if (localeResolver instanceof CookieGenerator) {
			CookieGenerator cookieGenerator = CookieGenerator.class.cast(localeResolver);
			cookieGenerator.setCookieDomain(request.getServerName());
			cookieGenerator.setCookiePath(request.getContextPath());
		}
	}

	public void setResolvers(List<LocaleResolver> resolvers) {
		this.resolvers = resolvers;
	}

	public void setThrowExceptionIfReadOnly(boolean throwExceptionIfReadOnly) {
		this.throwExceptionIfReadOnly = throwExceptionIfReadOnly;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

}
