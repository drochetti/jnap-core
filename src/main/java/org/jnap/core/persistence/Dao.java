package org.jnap.core.persistence;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jnap.core.bean.model.PersistentModel;

public interface Dao<E extends PersistentModel> {

	@PostConstruct
	public abstract void validateState();

	/**
	 * Delete the given {@code PersistentModel} instance.
	 * @param entity the model to delete.
	 */
	public abstract void delete(E entity);

	/**
	 * 
	 * @return
	 */
	public abstract Long countAll();

	/**
	 * Checks whether an instance of the model exists for the specified id.
	 * @param id The id of the instance.
	 * @return {@code true} if a persistent instance is found with the specified id
	 * or {@code false} otherwise.
	 */
	public abstract boolean exists(Serializable id);

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public abstract List<E> findBy(String dynaQuery, Object... params);

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public abstract E findUniqueBy(String dynaQuery, Object... params);

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	public abstract Long countBy(String dynaQuery, Object... params);

	/**
	 * 
	 * @param entities
	 */
	public abstract void delete(List<E> entities);

	/**
	 * 
	 * @return
	 */
	public abstract List<E> findAll();

	/**
	 * 
	 * @param example
	 * @return
	 */
	public abstract List<E> findByExample(E example);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public abstract E findById(Serializable id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public abstract E getById(Serializable id);

	/**
	 * 
	 * @param entity
	 */
	public abstract void insert(E entity);

	/**
	 * 
	 * @param entity
	 */
	public abstract void update(E entity);

	public void save(E entity);

	/**
	 * 
	 * @param example
	 * @return
	 */
	public abstract E findUniqueByExample(E example);

	/**
	 * 
	 * @return
	 */
	public abstract Class<E> getEntityClass();

}