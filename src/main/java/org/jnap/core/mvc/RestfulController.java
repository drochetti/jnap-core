package org.jnap.core.mvc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public abstract class RestfulController {

	/**
	 * A constant representing the {@code index} view name.
	 */
	public static final String INDEX = "index";

	/**
	 * A constant representing the {@code success} view name.
	 */
	public static final String SUCCESS = "success";

	/**
	 * A constant representing the {@code input} view name.
	 */
	public static final String INPUT = "input";

	@GET @Path("/")
	public ResponseModel index() {
		return ResponseModel.ok("index");
	}

}
