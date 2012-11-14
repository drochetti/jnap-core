package org.jnap.core.mvc;

import java.util.Collection;

import org.jnap.common.bean.cloning.BeanCloner;
import org.jnap.common.bean.visitor.BeanPropertyFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * @author Daniel Rochetti
 */
public class ResponseModel extends ModelAndView {

	protected static final String DEFAULT_SUCCESS_MSG = "todo";

	private int statusCode = 200;

	protected ResponseModel(String viewName) {
		super(viewName);
		put("viewName", viewName);
	}

	protected ResponseModel(View view) {
		super(view);
		if (view != null) {
			put("viewType", view.getClass().getName());
		}
	}

	public static ResponseModel ok(String viewName) {
		return new ResponseModel(viewName).successMessage(DEFAULT_SUCCESS_MSG).withStatus(200);
	}

	public static ResponseModel ok() {
		return ok("success");
	}

	public static ResponseModel notFound() {
		return new ResponseModel("").withStatus(404); // TODO
	}
	
	public static ResponseModel fail() {
		return new ResponseModel("").withStatus(400); // TODO
	}

	public static ResponseModel ok(View view) {
		return new ResponseModel(view).successMessage(DEFAULT_SUCCESS_MSG).withStatus(200);
	}

	public static ResponseModel forward(String url) {
		return new ResponseModel("forward:" + url);
	}
	
	public static ResponseModel redirect(String url) {
		return new ResponseModel("redirect:" + url);
	}

	public ResponseModel put(Object value) {
		return this.put(null, value);
	}

	public ResponseModel put(String name, Object value) {
		return this.put(name, value, true);
	}
	
	public ResponseModel put(String name, Object value, boolean bypassFilter) {
		return this.put(name, value, bypassFilter ? BeanPropertyFilter.noFilter() : BeanPropertyFilter.getDefault());
	}

	public ResponseModel put(String name, Object value, BeanPropertyFilter filter) {
		Object v = value;
		if (filter != null && v != null) {
			BeanCloner cloner = new BeanCloner(filter);
			if (!cloner.isStandardType(value.getClass())) {
				v = cloner.clone(v);
			}
		}
		if (name != null) {
			this.addObject(name, v);
		} else {
			this.addObject(v);
		}
		return this;
	}

	public ResponseModel model(Object model) {
		return this.put("data", model);
	}

	public ResponseModel modelList(Collection<?> modelList) {
		return this.put("data", modelList);
	}

	public ResponseModel successMessage(String message) {
		return this.put("successMessage", message);
	}

	public ResponseModel withStatus(int status) {
		this.statusCode = status;
		return this;
	}

	public int getStatusCode() {
		return statusCode;
	}

}
