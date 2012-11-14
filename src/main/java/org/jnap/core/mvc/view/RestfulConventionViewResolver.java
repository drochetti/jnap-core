package org.jnap.core.mvc.view;

import java.util.Locale;
import java.util.regex.Pattern;

import org.jnap.core.mvc.support.RestfulControllerHandlerMapping;
import org.jnap.core.mvc.support.RestfulMappingCache;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestfulConventionViewResolver extends InternalResourceViewResolver {

	private static final Pattern PREFIX_FORWARD_VIEW_PATTERN = Pattern.compile("^(forward):");

	private static final Pattern PREFIX_REDIRECT_VIEW_PATTERN = Pattern.compile("^(redirect):");

	private RestfulMappingCache mappingCache;

	private String defaultSuffix = ".jsp";

	private String viewNameSeparator = "/";

	public RestfulConventionViewResolver() {
		super();
		setPrefix("/WEB-INF/views/");
		setSuffix("");
	}

	@Override
	public View resolveViewName(String viewName, Locale locale)	throws Exception {
		if (!isForwardOrRedirect(viewName)) {
			String extension = mappingCache.getCurrentRequestUriExtension();
			extension = extension == null ? this.defaultSuffix : extension;
			extension = extension.startsWith(".") ? extension : "." + extension;
			viewName = viewName + extension;

			String viewPath = (String) RequestContextHolder.getRequestAttributes().getAttribute(
					RestfulControllerHandlerMapping.CURRENT_VIEW_PATH_ATTRIBUTE,
					WebRequest.SCOPE_REQUEST);
			if (viewPath != null) {
				viewName = viewPath + this.viewNameSeparator + viewName;
			}
		}
		return handleSpecialViews(viewName, locale);
	}

	/**
	 * @param viewName
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	protected View handleSpecialViews(String viewName, Locale locale) throws Exception {
		if (PREFIX_FORWARD_VIEW_PATTERN.matcher(viewName).find()) {
			RequestContextHolder.getRequestAttributes().removeAttribute(
					RestfulControllerHandlerMapping.CURRENT_VIEW_PATH_ATTRIBUTE,
					WebRequest.SCOPE_REQUEST);
		}
		return super.resolveViewName(viewName, locale);
	}

	private boolean isForwardOrRedirect(String viewName) {
		return PREFIX_FORWARD_VIEW_PATTERN.matcher(viewName).find()
				|| PREFIX_REDIRECT_VIEW_PATTERN.matcher(viewName).find();
	}

	public void setMappingCache(RestfulMappingCache mappingCache) {
		this.mappingCache = mappingCache;
	}

	public void setViewNameSeparator(String viewNameSeparator) {
		this.viewNameSeparator = viewNameSeparator;
	}

	public void setDefaultSuffix(String defaultSuffix) {
		this.defaultSuffix = defaultSuffix;
	}

}
