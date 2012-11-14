package org.jnap.core.persistence;

import java.io.Serializable;
import java.util.List;

import org.jnap.core.bean.model.IndexedModel;

/**
 * 
 * @author Daniel Rochetti
 *
 * @param <E>
 */
public interface FullTextDao<E extends IndexedModel> extends Dao<E> {

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByDocId(Serializable)}.
	 */
	public abstract E searchByDocId(Serializable docId);

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByKeywords(String)}.
	 */
	public abstract List<E> searchByKeywords(String keywords);

	/**
	 * Hibernate Search implementation for {@link FullTextDao#searchByKeywords(String, boolean)}.
	 */
	public abstract List<E> searchByKeywords(String keywords, boolean leadingWildcard);

}