package org.jnap.core.mvc.support;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.sun.jersey.api.uri.UriTemplate;



/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestfulMapping {

	private String id;
	private String beanName;
	private Class<?> beanClass;
	private Method method;
	private String viewPath;
	private String controllerPath;
	private Set<UriTemplate> urls;
	private Set<String> httpMethods;
	private Set<String> pathExpressions;
	private Set<String> consumes;
	private Set<String> produces;

	public RestfulMapping(String beanName, Class<?> beanClass, Method method) {
		this.beanName = beanName;
		this.beanClass = beanClass;
		this.method = method;
		// initialize all collections
		this.httpMethods = new TreeSet<String>();
		this.urls = new TreeSet<UriTemplate>(UriTemplate.COMPARATOR);
		this.pathExpressions = new TreeSet<String>();
		this.consumes = new LinkedHashSet<String>();
		this.produces = new LinkedHashSet<String>();
	}

	public String getMappingId() {
		if (this.id == null) {
			this.id = this.httpMethods + "." + this.beanClass.getName() + "." + this.method.toGenericString();
		}
		return this.id;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String controllerName) {
		this.beanName = controllerName;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Set<UriTemplate> getUrls() {
		return urls;
	}

	public void setUrls(Set<UriTemplate> urls) {
		this.urls = urls;
	}

	public String getViewPath() {
		return viewPath;
	}

	public void setViewPath(String viewPath) {
		this.viewPath = viewPath;
	}

	public String getControllerPath() {
		return controllerPath;
	}

	public void setControllerPath(String controllerPath) {
		this.controllerPath = controllerPath;
	}

	public Set<String> getHttpMethods() {
		return httpMethods;
	}

	public void setHttpMethods(Set<String> httpMethod) {
		this.httpMethods = httpMethod;
	}

	public Set<String> getPathExpressions() {
		return pathExpressions;
	}

	public void setPathExpressions(Set<String> pathExpressions) {
		this.pathExpressions = pathExpressions;
	}

	public Set<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(Set<String> consumes) {
		this.consumes = consumes;
	}

	public Set<String> getProduces() {
		return produces;
	}

	public void setProduces(Set<String> produces) {
		this.produces = produces;
	}

	public void addUrl(String url) {
		this.urls.add(new UriTemplate(url));
	}
	
	public void addHttpMethod(String httpMethod) {
		this.httpMethods.add(httpMethod.toUpperCase());
	}

	public void addPathExpression(String pathExpression) {
		this.pathExpressions.add(pathExpression);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other.getClass() != getClass()) {
			return false;
		}
		
		RestfulMapping otherMapping = (RestfulMapping) other;
		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(this.httpMethods, otherMapping.httpMethods)
				.append(this.urls, otherMapping.urls)
				.isEquals();
	}

}
