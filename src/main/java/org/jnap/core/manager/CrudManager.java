package org.jnap.core.manager;

import java.io.Serializable;
import java.util.List;

import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.persistence.Dao;
import org.jnap.core.stereotype.BusinessManager;
import org.springframework.transaction.annotation.Transactional;

@BusinessManager
public abstract class CrudManager<E extends PersistentModel> {

	public List<E> findAll() {
		return getDao().findAll();
	}

	@Transactional(readOnly = true)
	public List<E> findByExample(E example) {
		return getDao().findByExample(example);
	}

	@Transactional(readOnly = true)
	public E findUniqueByExample(E example) {
		return getDao().findUniqueByExample(example);
	}

	@Transactional(readOnly = true)
	public E findById(Serializable id) {
		return getDao().findById(id);
	}

	@Transactional
	public void insert(E entity) {
		getDao().insert(entity);
	}

	@Transactional
	public void update(E entity) {
		getDao().update(entity);
	}
	
	@Transactional
	public void save(E entity) {
		getDao().save(entity);
	}

	@Transactional
	public void delete(E entity) {
		getDao().delete(entity);
	}

	public Long countAll() {
		return getDao().countAll();
	}

	protected abstract Dao<E> getDao();

}
