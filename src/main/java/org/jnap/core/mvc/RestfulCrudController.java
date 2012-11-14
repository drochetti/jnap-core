package org.jnap.core.mvc;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.manager.CrudManager;
import org.jnap.core.mvc.bind.annotation.ModelLoad;
import org.springframework.beans.BeanUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class RestfulCrudController<M extends PersistentModel> extends RestfulController {

	/**
	 * A constant representing the {@code show} view name.
	 */
	public static final String SHOW = "show";

	/**
	 * A constant representing the {@code editNew} view name.
	 */
	public static final String EDIT_NEW = "editNew";

	/**
	 * A constant representing the {@code edit} view name.
	 */
	public static final String EDIT = "edit";

	@Override
	@GET @Path("/")
	public ResponseModel index() {
		return ResponseModel.ok(INDEX).modelList(getCrudManager().findAll());
	}

	@GET @Path("/{id}")
	public ResponseModel show(@ModelLoad M model) {
		return ResponseModel.ok(SHOW).model(model);
	}

	@GET @Path("/new")
	public ResponseModel editNew() {
		return ResponseModel.ok(EDIT_NEW).model(resetModel());
	}

	@POST @Path("/")
	public ResponseModel create(@ModelAttribute("data") M model) {
		getCrudManager().insert(model);
		return ResponseModel.ok(EDIT_NEW).model(model);
	}

	@GET @Path("/{id}/edit")
	public ResponseModel edit(@ModelLoad M model) {
		return ResponseModel.ok(EDIT).model(model);
	}

	@PUT @Path("/{id}")
	public ResponseModel update(@ModelAttribute("data") M model) {
		getCrudManager().update(model);
		return ResponseModel.ok(EDIT).model(model);
	}

	@DELETE @Path("/{id}")
	public ResponseModel destroy(@ModelLoad M model) {
		getCrudManager().delete(model);
		return ResponseModel.ok(INDEX);
	}

	/**
	 * @return a new instance of the Model
	 */
	protected M resetModel() {
		final Class<?> modelType = GenericTypeResolver.resolveTypeArgument(getClass(), RestfulCrudController.class);
		return (M) BeanUtils.instantiate(modelType);
	}

	protected abstract CrudManager<M> getCrudManager();

}
