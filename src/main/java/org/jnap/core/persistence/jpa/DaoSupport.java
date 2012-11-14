/*
 * DaoSupport.java created on 2010-03-15
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnap.core.persistence.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.QueryException;
import org.hibernate.SessionFactory;
import org.jnap.core.bean.model.LogicalDelete;
import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.bean.paging.PagingDataHolder;
import org.jnap.core.bean.paging.PagingSetup;
import org.jnap.core.persistence.Dao;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;


/**
 * <p>A base class for JPA powered data access objects (Dao).</p>
 * 
 * <p>Requires a {@link javax.persistence.EntityManagerFactory} to be set. If only one is available on
 * the {@link org.springframework.context.ApplicationContext} it will be autowired. If you
 * have more than one {@code EntityManagerFactory} then you must override the
 * {@link #setSessionFactory(SessionFactory)} to inject the proper factory for each 
 * {@code Dao} implementation or you can set it via Spring XML.</p>
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E> the {@code Entity} type handled by this {@code Dao}. It must implement {@link PersistentModel}.
 */
public abstract class DaoSupport<E extends PersistentModel> implements Dao<E> {

	protected EntityManagerFactory entityManagerFactory;

	@PersistenceContext
	protected EntityManager entityManager;

	protected boolean defaultPaging = true;
	protected Order defaultOrder;
	protected Class<E> entityClass;

	@Override
	@PostConstruct
	public void validateState() {
		Assert.notNull(this.entityManagerFactory);
	}

	@PersistenceUnit
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void delete(E entity) {
		if (entity instanceof LogicalDelete) {
			((LogicalDelete) entity).delete();
			update(entity);
		} else {
			entityManager.remove(entity);
		}
	}

	@Override
	public Long countAll() {
		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<Long> count = builder.createQuery(Long.class);
		count.select(builder.count(count.from(getEntityClass())));
		return entityManager.createQuery(count).getSingleResult();
	}

	/**
	 * 
	 * @return
	 */
	protected CriteriaBuilder getCriteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}

	@Override
	public boolean exists(Serializable id) {
		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<Long> count = builder.createQuery(Long.class);
		Root<?> root = count.from(getEntityClass());
		count.select(builder.count(root));
		count.where(builder.equal(root.get("id"), id));
		return entityManager.createQuery(count).getSingleResult() == 1;
	}

	@Override
	public List<E> findBy(String dynaQuery, Object... params) {
		throw new UnsupportedOperationException(""); // TODO msg
	}

	@Override
	public E findUniqueBy(String dynaQuery, Object... params) {
		throw new UnsupportedOperationException(""); // TODO msg
	}

	@Override
	public Long countBy(String dynaQuery, Object... params) {
		throw new UnsupportedOperationException(""); // TODO msg
	}

	@Override
	public void delete(List<E> entities) {
		if (entities != null && !entities.isEmpty()) {
			for (E entity : entities) {
				delete(entity);
			}
		}
	}

	@Override
	public List<E> findAll() {
		CriteriaQuery criteria = createCriteriaQuery();
		if (getDefaultOrder() != null) {
			criteria.orderBy(getDefaultOrder());
		}
		return findByCriteria(criteria);
	}

	@Override
	public List<E> findByExample(E example) {
		throw new UnsupportedOperationException(""); // TODO msg
	}

	@Override
	public E findById(Serializable id) {
		return (E) entityManager.find(getEntityClass(), id);
	}

	@Override
	public E getById(Serializable id) {
		return (E) entityManager.getReference(getEntityClass(), id);
	}

	@Override
	public void insert(E entity) {
		entityManager.persist(entity);
	}

	@Override
	public void save(E entity) {
		entity = entityManager.merge(entity);
	}

	@Override
	public void update(E entity) {
		entityManager.refresh(entity);
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	protected CriteriaQuery createDynaQuery(String dynaQuery, Object... params) {
		throw new UnsupportedOperationException(""); // TODO msg
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	protected Long count(Query query) {
		Number quantity = (Number) query.getSingleResult();
		return quantity == null ? 0 : quantity.longValue();
	}

	/**
	 * 
	 * @param jpql
	 * @param params
	 * @return
	 */
	protected Long count(String jpql, Object... params) {
		Query query = entityManager.createQuery(jpql);
		QueryUtils.setIndexedParameters(query, params);
		return count(query);
	}

	/**
	 * 
	 * @param jpql
	 * @param params
	 * @return
	 */
	protected Long count(String jpql, Map<String, ?> params) {
		Query query = entityManager.createQuery(jpql);
		QueryUtils.setNamedParameters(query, params);
		return count(query);
	}

	/**
	 * 
	 * @param jpql
	 * @param paging
	 * @param params
	 * @return
	 */
	protected List<E> doQuery(Query query, boolean paging, Object params) {
		QueryUtils.setParameters(query, params);
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(query, params);
		}
		return query.getResultList();
	}

	/**
	 * 
	 * @param jpql
	 * @return
	 */
	protected List<E> find(String jpql) {
		return find(jpql, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 
	 * @param jpql
	 * @param paging
	 * @return
	 */
	protected List<E> find(String jpql, boolean paging) {
		return find(jpql, paging, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * 
	 * @param jpql
	 * @param paging
	 * @param namedParams
	 * @return
	 */
	protected List<E> find(String jpql, boolean paging, Map<String, ?> namedParams) {
		return doQuery(entityManager.createQuery(jpql), paging, namedParams);
	}

	/**
	 * 
	 * @param jpql
	 * @param paging
	 * @param params
	 * @return
	 */
	protected List<E> find(String jpql, boolean paging, Object... params) {
		return doQuery(entityManager.createQuery(jpql), paging, params);
	}

	/**
	 * 
	 * @param jpql
	 * @param params
	 * @return
	 */
	protected List<E> find(String jpql, List<?> params) {
		return find(jpql, params.toArray());
	}

	/**
	 * 
	 * @param jpql
	 * @param namedParams
	 * @return
	 */
	protected List<E> find(String jpql, Map<String, ?> namedParams) {
		return find(jpql, this.defaultPaging, namedParams);
	}

	/**
	 * 
	 * @param jpql
	 * @param params
	 * @return
	 */
	protected List<E> find(String jpql, Object... params) {
		return find(jpql, this.defaultPaging, params);
	}

	/**
	 * 
	 * @return
	 */
	protected CriteriaQuery<E> createCriteriaQuery() {
		final CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<E> criteria = builder.createQuery(getEntityClass());
		criteria.select(criteria.from(getEntityClass()));
		return criteria;
	}

	/**
	 * 
	 * @param criteria
	 * @return
	 */
	protected List<E> findByCriteria(CriteriaQuery criteria) {
		return findByCriteria(criteria, this.defaultPaging);
	}

	/**
	 * 
	 * @param criteria
	 * @param paging
	 * @return
	 */
	protected List<E> findByCriteria(CriteriaQuery criteria, boolean paging) {
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(criteria);
		}
		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * 
	 * @param jpql
	 * @param params
	 * @return
	 * @throws QueryException
	 */
	protected E findUnique(String jpql, Object... params) throws QueryException {
		return handleUniqueResult(find(jpql, false, params));
	}

	@Override
	public E findUniqueByExample(E example) {
		return handleUniqueResult(findByExample(example));
	}

	/**
	 * 
	 * @return
	 */
	protected Order getDefaultOrder() {
		return defaultOrder;
	}

	@Override
	public Class<E> getEntityClass() {
		if (this.entityClass == null) {
			this.entityClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), Dao.class);
		}
		return this.entityClass;
	}

	/**
	 * 
	 * @param result
	 * @return
	 * @throws QueryException
	 */
	protected E handleUniqueResult(List<E> result) throws PersistenceException {
		E uniqueResult = null;
		if (result != null && result.size() > 0) {
			if (result.size() == 1) {
				uniqueResult = result.get(0);
			} else {
				throw new NonUniqueResultException(); //TODO
			}
		}
		return uniqueResult;
	}

	/**
	 * Setup paging for the {@link CriteriaQuery}.
	 * 
	 * @param criteria The criteria that will be configured for pagination.
	 */
	protected void doPaging(CriteriaQuery criteria) {
//		setupPaging(new CriteriaPagingSetup(criteria)); TODO implement JPA criteria paging
	}

	/**
	 * Setup paging for the {@link Query}.
	 * 
	 * @param query The query that will be configured for pagination.
	 * @param queryParams The query parameters.
	 */
	protected void doPaging(Query query, Object queryParams) {
		setupPaging(new QueryPagingSetup(query, entityManager, queryParams));
	}

	/**
	 * 
	 * @param pagingSetup
	 */
	protected void setupPaging(PagingSetup pagingSetup) {
		PagingDataHolder.setTotal(pagingSetup.countTotal());
		final int resultsPerPage = PagingDataHolder.getResultsPerPage();
		int first = (PagingDataHolder.getCurrentPage() - 1) * resultsPerPage;
		pagingSetup.setFirstResult(first);
		pagingSetup.setResultsPerPage(resultsPerPage);
	}

}
