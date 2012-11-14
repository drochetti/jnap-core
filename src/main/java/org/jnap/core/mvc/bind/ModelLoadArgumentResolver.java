package org.jnap.core.mvc.bind;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.inject.Inject;

import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.mvc.bind.annotation.ModelLoad;
import org.jnap.core.mvc.support.RestfulHandlerAdapter;
import org.jnap.core.persistence.Dao;
import org.jnap.core.persistence.factory.DaoFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class ModelLoadArgumentResolver extends AnnotatedWebArgumentResolver<ModelLoad> {

	@Inject
	private DaoFactory daoFactory;

	public ModelLoadArgumentResolver() {
		super(ModelLoad.class);
		setStrictTypeChecking(false);
		setSupportedTypes(PersistentModel.class);
	}

	@Override
	@Transactional(readOnly = true)
	protected Object doResolveArgument(MethodParameter methodParameter,
			NativeWebRequest webRequest, ModelLoad annotation) {
		Object value = null;
		final String name = annotation.value();
		Object idParamValue = findAttribute(webRequest, name, SCOPE_PATH_PARAM);
		if (idParamValue == null) {
			idParamValue = webRequest.getParameter(name);
		}
		if (idParamValue == null) {
			idParamValue = webRequest.getAttribute(name, WebRequest.SCOPE_REQUEST);
		}
		if (idParamValue != null) {
			Class<?> handlerClass = ClassUtils.getUserClass(webRequest.getAttribute(
					RestfulHandlerAdapter.CURRENT_HANDLER_ATTRIBUTE, WebRequest.SCOPE_REQUEST));
			// TODO review entity type resolver
			if (handlerClass != null) {
				Type modelType = GenericTypeResolver.resolveParameterType(methodParameter, handlerClass);
				Class<? extends PersistentModel> persistentModelType = (Class<? extends PersistentModel>) modelType;
				Dao<? extends PersistentModel> dao = daoFactory.getDaoFor(persistentModelType);
				value = dao.findById(convertId(idParamValue, persistentModelType));
			}
		}
		return value;
	}

	private Serializable convertId(Object param, Class<? extends PersistentModel> modelType) {
		WebDataBinder binder = new WebRequestDataBinder(null);
		Method getIdMethod = ReflectionUtils.findMethod(PersistentModel.class, "getId");
		Assert.notNull(getIdMethod, "PersistentModel contract failed! The getId method is mandatory!");
		final Class<?> idType = GenericTypeResolver.resolveReturnType(getIdMethod, modelType);
		return (Serializable) binder.convertIfNecessary(param, idType);
	}

}
