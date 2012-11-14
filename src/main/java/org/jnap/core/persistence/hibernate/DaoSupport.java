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
package org.jnap.core.persistence.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.transform.Transformers;
import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.bean.paging.PagingDataHolder;


/**
 * <p>A base class for Hibernate powered data access objects (Dao).</p>
 * 
 * <p>Requires a {@link org.hibernate.SessionFactory} to be set. If only one is available on
 * the {@link org.springframework.context.ApplicationContext} it will be autowired. If you
 * have more than one {@code SessionFactory} then you must override the
 * {@link #setSessionFactory(SessionFactory)} to inject the proper factory for each 
 * {@code Dao} implementation or you can set it via Spring XML.</p>
 * 
 * @author Daniel Rochetti
 * @since 1.0
 *
 * @param <E> the {@code Entity} type handled by this {@code Dao}. It must implement {@link PersistentModel}.
 */
public abstract class DaoSupport<E extends PersistentModel> extends org.jnap.core.persistence.jpa.DaoSupport<E> {

	@Override
	public boolean exists(Serializable id) {
		return countBy("Id", id) != 0;
	}

	@Override
	public List<E> findBy(String dynaQuery, Object... params) {
		return findByCriteria(createDynaCriteria("findBy" + dynaQuery, params));
	}

	@Override
	public E findUniqueBy(String dynaQuery, Object... params) {
		return handleUniqueResult(findByCriteria(createDynaCriteria("findUniqueBy" + dynaQuery, params), false));
	}

	@Override
	public Long countBy(String dynaQuery, Object... params) {
		return ((Number) createDynaCriteria("countBy" + dynaQuery, params).uniqueResult()).longValue();
	}

	@Override
	public List<E> findByExample(E example) {
		Criteria criteria = createCriteria();
		criteria.add(Example.create(example).ignoreCase());
		if (getDefaultOrder() != null) {
			criteria.addOrder(convertToHibernateOrder());
		}
		return findByCriteria(criteria);
	}

	@Override
	public List<E> findAll() {
		Criteria criteria = createCriteria();
		if (getDefaultOrder() != null) {
			criteria.addOrder(convertToHibernateOrder());
		}
		return findByCriteria(criteria);
	}

	/**
	 * @return
	 */
	protected Order convertToHibernateOrder() {
		String property = getDefaultOrder().getExpression().toString();
		Order order = getDefaultOrder().isAscending() ?
				Order.asc(property) : Order.desc(property);
		return order;
	}

	@Override
	public E findUniqueByExample(E example) {
		return handleUniqueResult(findByExample(example));
	}

	@Override
	public void save(E entity) {
		getSession().saveOrUpdate(resolveEntityName(), entity);
	}

	/**
	 * 
	 * @param dynaQuery
	 * @param params
	 * @return
	 */
	protected Criteria createDynaCriteria(String dynaQuery, Object... params) {
		DynaQueryBuilder dynaQueryBuilder = new DynaQueryBuilder(getSession(), resolveEntityName(),
				dynaQuery, params);
		return dynaQueryBuilder.build();
	}

	/**
	 * 
	 * @param hql
	 * @param returnType
	 * @param params
	 * @return
	 */
	protected <T> List<T> find(String hql, Class<T> returnType, Object... params) {
		Query query = getSession().createQuery(hql);
		query.setResultTransformer(Transformers.aliasToBean(returnType));
		QueryUtils.setIndexedParameters(query, params);
		return query.list();
	}

	/**
	 * 
	 * @return
	 */
	protected Criteria createCriteria() {
		return getSession().createCriteria(resolveEntityName());
	}

	/**
	 * 
	 * @param criteria
	 * @return
	 */
	protected List<E> findByCriteria(Criteria criteria) {
		return findByCriteria(criteria, this.defaultPaging);
	}

	/**
	 * 
	 * @param criteria
	 * @param paging
	 * @return
	 */
	protected List<E> findByCriteria(Criteria criteria, boolean paging) {
		if (paging && PagingDataHolder.isPagingSet()) {
			doPaging(criteria);
		}
		return criteria.list();
	}

	/**
	 * 
	 * @return
	 */
	protected String getEntityName() {
		return null;
	}

	/**
	 * Gets the current session.
	 * 
	 * @return the current Hibernate {@link Session}.
	 * @see SessionFactory#getCurrentSession()
	 */
	protected Session getSession() {
		return entityManager.unwrap(Session.class);
	}

	/**
	 * 
	 * @param result
	 * @return
	 * @throws QueryException
	 */
	protected E handleUniqueResult(List<E> result) throws QueryException {
		E uniqueResult = null;
		if (result != null && result.size() > 0) {
			if (result.size() == 1) {
				uniqueResult = result.get(0);
			} else {
				throw new NonUniqueResultException(result.size());
			}
		}
		return uniqueResult;
	}

	protected final String resolveEntityName() {
		return getEntityName() != null ? getEntityName() : getEntityClass().getName();
	}

	/**
	 * Setup paging for the {@link Criteria}.
	 * 
	 * @param criteria The criteria that will be configured for pagination.
	 */
	protected void doPaging(Criteria criteria) {
		setupPaging(new CriteriaPagingSetup(criteria));
	}

}
