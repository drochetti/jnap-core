package org.jnap.core.mvc.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.AbstractControllerUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import com.sun.jersey.api.uri.UriTemplate;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestfulControllerHandlerMapping extends
		AbstractControllerUrlHandlerMapping implements InitializingBean {

	public static final String CURRENT_VIEW_PATH_ATTRIBUTE = RestfulControllerHandlerMapping.class.getName() + ".currentViewPath";

	/**
	 * Common suffix at the end of controller implementation classes. Removed when generating the URL path.
	 */
	private static final String DEFAULT_SUFFIX = "Controller";

	/**
	 * 
	 */
	private static final String MAIN_CONTROLLER_PREFIX = "Main";


	private static final List<Class<? extends Annotation>> HTTP_METHOD_ANNOTATIONS = new ArrayList<Class<? extends Annotation>>();

	static {
		HTTP_METHOD_ANNOTATIONS.add(GET.class);
		HTTP_METHOD_ANNOTATIONS.add(POST.class);
		HTTP_METHOD_ANNOTATIONS.add(PUT.class);
		HTTP_METHOD_ANNOTATIONS.add(DELETE.class);
		HTTP_METHOD_ANNOTATIONS.add(HEAD.class);
		HTTP_METHOD_ANNOTATIONS.add(OPTIONS.class);
	}


	private RestfulMappingCache mappingCache;

	private String classNameSuffix = DEFAULT_SUFFIX;

	private String mainControllerPrefix = MAIN_CONTROLLER_PREFIX;

	private String basePackage;

	private boolean alsoMapExtensions = true;

	private PathNameTransformer pathNameTransformer = new SeoPathNameTransformer();

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.pathNameTransformer);
		Assert.notNull(this.mappingCache);
		// TODO warn if a basePackage is not set (not a good practice)
	}

	@Override
	protected String[] buildUrlsForHandler(final String beanName, final Class beanClass) {
		final Set<String> paths = new LinkedHashSet<String>();
		final String viewPath = this.buildViewPath(beanClass);
		final String pathPrefix = this.buildControllerPathPrefix(viewPath, beanClass);
		final RestfulMappingCache cache = this.mappingCache;
		ReflectionUtils.doWithMethods(beanClass, new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				final RestfulControllerHandlerMapping self = RestfulControllerHandlerMapping.this;
				String path = self.buildMethodPath(method);
				if (path != null) {
					RestfulMapping mapping = new RestfulMapping(beanName, beanClass, method);
					mapping.setControllerPath(pathPrefix);
					mapping.setViewPath(viewPath);
					path = pathPrefix + "/" + path;
					path = cleanPath(path);

					// adding url paths
					String mappedPath = new String(path);
//					if (paths.contains(mappedPath)) {
//						return;
//					}
					paths.add(mappedPath);
					mapping.addUrl(mappedPath);

					if (self.alsoMapExtensions) {
						if (!path.endsWith("/")) {
							mappedPath = path + "/";
							paths.add(mappedPath);
							mapping.addUrl(mappedPath);
						} else {
							path = path.substring(0, path.length() - 1);
							mappedPath = path;
							paths.add(mappedPath);
							mapping.addUrl(mappedPath);
						}
						mappedPath = path + ".*";
						paths.add(mappedPath);
						mapping.addUrl(mappedPath);
					}

					// getting supported http methods
					Set<String> httpMethods = self.extractHttpMethods(method);
					mapping.setHttpMethods(httpMethods);

					// adding to cache
					if (!cache.isMapped(mapping)) {
						cache.add(mapping.getMappingId(), mapping);
					}
				}
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		return paths.toArray(new String[paths.size()]);
	}

	protected String buildViewPath(Class<?> beanClass) {
		final StringBuilder path = new StringBuilder("/");
		String controllerPackage = beanClass.getPackage().getName();
		if (this.basePackage != null && controllerPackage.startsWith(this.basePackage)) {
			controllerPackage = controllerPackage.substring(this.basePackage.length());
			path.append(this.pathNameTransformer.transform(controllerPackage.replaceAll("\\.", "/")));
		}
		
		String controllerPath = this.buildControllerNamePath(beanClass);
		path.append("/").append(controllerPath);
		return cleanPath(path.toString());
	}
	
	protected String buildControllerNamePath(Class<?> beanClass) {
		String className = ClassUtils.getShortName(beanClass);
		if (className.endsWith(this.classNameSuffix)) {
			className = className.substring(0, className.indexOf(this.classNameSuffix));
			className = className.equals(this.mainControllerPrefix) ? "" : className;
		}
		return this.pathNameTransformer.transform(className);
	}

	private String buildControllerPathPrefix(String viewPath, Class<?> beanClass) {
		String controllerPath = viewPath;
		if (beanClass.isAnnotationPresent(Path.class)) {
			Path pathAnnotation = beanClass.getAnnotation(Path.class);
			controllerPath = controllerPath.replaceFirst("/[\\w-_]*$", "/" + pathAnnotation.value());
		}
		return cleanPath(controllerPath);
	}

	protected Set<String> extractHttpMethods(Method method) {
		Set<String> httpMethods = new TreeSet<String>();
		for (Class<? extends Annotation> annotationType : HTTP_METHOD_ANNOTATIONS) {
			Annotation methodAnnotation = AnnotationUtils.findAnnotation(method, annotationType);
			if (methodAnnotation != null) {
				httpMethods.add(methodAnnotation.annotationType().getAnnotation(HttpMethod.class).value());
			}
		}
		return new TreeSet<String>(httpMethods);
	}


	protected String buildMethodPath(Method method) {
		String path = null;
		Path pathAnnotation = AnnotationUtils.findAnnotation(method, Path.class);
		if (pathAnnotation != null) {
			path = pathAnnotation.value();
		}
		return path;
	}

	/**
	 * TODO better performance, caching, etc
	 */
	@Override
	protected void validateHandler(Object handler, HttpServletRequest request) throws Exception {
		final RestfulMappingCache cache = this.mappingCache;
//		cache.clearCurrentRequestMapping();
		String appRequestUri = this.urlPathHelper.getPathWithinApplication(request);
		final String requestedUri = appRequestUri.replaceFirst("\\.\\w*$", "");
		final String extension = StringUtils.getFilenameExtension(appRequestUri);

		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		if (!cache.isMappedHandler(handlerClass)) {
			throw new HttpServerErrorException(HttpStatus.NOT_FOUND);
		}

		Map<UriTemplate, RestfulMapping> urlMapping = new LinkedHashMap<UriTemplate, RestfulMapping>();
		// first pass: iterate all mappings for the given handler (controller)
		for (RestfulMapping mapping : cache.getMappingsByHandler(handlerClass)) {
			if (isValidHttpMethod(mapping.getHttpMethods(), request)) {
				for (UriTemplate url : mapping.getUrls()) {
					urlMapping.put(url, mapping);
				}
			}
		}

		// sort the uri templates (according to JSR-311 spec)
		List<UriTemplate> uriTemplates = new ArrayList<UriTemplate>();
		uriTemplates.addAll(urlMapping.keySet());
		Collections.sort(uriTemplates, UriTemplate.COMPARATOR);

		// find the best match
		RestfulMapping matchedMapping = null;
		for (UriTemplate uriTemplate : uriTemplates) {
			Map<String, String> uriVariables = new HashMap<String, String>();
			if (uriTemplate.match(requestedUri, uriVariables) && validateRequestExpressions(
					urlMapping.get(uriTemplate).getPathExpressions(), request)) {
				matchedMapping = urlMapping.get(uriTemplate);
				cache.setCurrentRequestMapping(matchedMapping);
				cache.setCurrentRequestUri(requestedUri);
				cache.setCurrentRequestUriExtension(extension);
				request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
				request.setAttribute(CURRENT_VIEW_PATH_ATTRIBUTE, matchedMapping.getViewPath());
				break;
			}
		}

		if (matchedMapping == null) {
			throw new HttpServerErrorException(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 
	 * @param pathExpressions
	 * @param request
	 * @return
	 */
	protected boolean validateRequestExpressions(Set<String> pathExpressions,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * 
	 * @param httpMethods
	 * @param request
	 * @return
	 */
	protected boolean isValidHttpMethod(final Set<String> httpMethods, HttpServletRequest request) {
		for (String httpMethod : httpMethods) {
			if (request.getMethod().equalsIgnoreCase(httpMethod)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param classNameSuffix
	 */
	public void setClassNameSuffix(String classNameSuffix) {
		this.classNameSuffix = classNameSuffix;
	}

	/**
	 * 
	 * @param mainControllerPrefix
	 */
	public void setMainControllerPrefix(String mainControllerPrefix) {
		this.mainControllerPrefix = mainControllerPrefix;
	}

	/**
	 * 
	 * @param basePackage
	 */
	public void setBasePackage(String basePackage) {
		if (basePackage != null && !basePackage.endsWith(".")) {
			basePackage += ".";
		}
		this.basePackage = basePackage;
	}

	/**
	 * 
	 * @param pathNameTransformer
	 */
	public void setPathNameTransformer(PathNameTransformer pathNameTransformer) {
		this.pathNameTransformer = pathNameTransformer;
	}

	/**
	 * 
	 * @param alsoMapExtensions
	 */
	public void setAlsoMapExtensions(boolean alsoMapExtensions) {
		this.alsoMapExtensions = alsoMapExtensions;
	}

	/**
	 * @param mappingCache
	 */
	public void setMappingCache(RestfulMappingCache mappingCache) {
		this.mappingCache = mappingCache;
	}

	/**
	 * Removes duplicated slashes on URL paths.
	 * @param path The original URL path.
	 * @return the cleaned path.
	 */
	private String cleanPath(String path) {
		return path.replaceAll("/{2,}", "/");
	}

}
