package org.jnap.core.mvc.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestfulMappingCache {


	private Map<String, RestfulMapping> cache;
	private Set<Class<?>> mappedHandlers;
	private ThreadLocal<RestfulMapping> currentRequestMapping;
	private ThreadLocal<String> currentRequestUriExtension;
	private ThreadLocal<String> currentRequestUri;

	public RestfulMappingCache() {
		this.cache = new HashMap<String, RestfulMapping>();
		this.mappedHandlers = new HashSet<Class<?>>();
		this.currentRequestMapping = new ThreadLocal<RestfulMapping>();
		this.currentRequestUriExtension = new ThreadLocal<String>();
		this.currentRequestUri = new ThreadLocal<String>();
	}

	public Collection<RestfulMapping> getMappings() {
		return this.cache.values();
	}

	public RestfulMapping get(String id) {
		return this.cache.get(id);
	}

	public void add(String id, RestfulMapping mapping) {
		this.cache.put(id, mapping);
		this.mappedHandlers.add(mapping.getBeanClass());
	}

	public boolean isMappedHandler(Class<?> handlerType) {
		return this.mappedHandlers.contains(handlerType);
	}

	public RestfulMapping getCurrentRequestMapping() {
		return currentRequestMapping.get();
	}

	void setCurrentRequestMapping(RestfulMapping currentMapping) {
		currentRequestMapping.set(currentMapping);
	}

	public Collection<RestfulMapping> getMappingsByHandler(Class<?> handlerClass) {
		Collection<RestfulMapping> mappings = new LinkedHashSet<RestfulMapping>();
		for (RestfulMapping restfulMapping : this.getMappings()) {
			if (restfulMapping.getBeanClass().equals(handlerClass)) {
				mappings.add(restfulMapping);
			}
		}
		return mappings;
	}

	public String getCurrentRequestUriExtension() {
		return currentRequestUriExtension.get();
	}

	void setCurrentRequestUriExtension(String extension) {
		currentRequestUriExtension.set(extension);
	}
	
	public String getCurrentRequestUri() {
		return currentRequestUri.get();
	}
	
	void setCurrentRequestUri(String uri) {
		currentRequestUri.set(uri);
	}

	public void clearCurrentRequestMapping() {
		currentRequestMapping.set(null);
		currentRequestUri.set(null);
		currentRequestUriExtension.set(null);
	}

	public boolean contains(RestfulMapping mapping) {
		return cache.values().contains(mapping);
	}

	public boolean isMapped(RestfulMapping mapping) {
		boolean mapped = false;
		for (RestfulMapping cachedMapping : this.cache.values()) {
			mapped = CollectionUtils.containsAny(cachedMapping.getUrls(), mapping.getUrls())
					&& CollectionUtils.containsAny(cachedMapping.getHttpMethods(), mapping.getHttpMethods());
			if (mapped) {
				break;
			}
		}
		return mapped;
	}

}
