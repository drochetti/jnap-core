/*
 * QueryPagingSetup.java created on 2010-06-06
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.jnap.core.bean.paging.PagingSetup;


/**
 * A {@link PagingSetup} adapter for {@link Criteria} queries.
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class CriteriaPagingSetup implements PagingSetup {

	private Criteria criteria;

	public CriteriaPagingSetup(Criteria criteria) {
		this.criteria = criteria;
	}

	public void setFirstResult(int first) {
		this.criteria.setFirstResult(first);
	}

	public void setResultsPerPage(int max) {
		this.criteria.setMaxResults(max);
	}

	public long countTotal() {
		// configure the count criteria
		CriteriaImpl criteriaImpl = (CriteriaImpl) this.criteria;
		ResultTransformer resultTransformer = criteriaImpl.getResultTransformer();
		Projection projection = criteriaImpl.getProjection();
		// remove 'order by' and keep them to re-add later
		List<CriteriaImpl.OrderEntry> orders = new ArrayList<CriteriaImpl.OrderEntry>();
		Iterator<CriteriaImpl.OrderEntry> iterator = criteriaImpl.iterateOrderings();
		while (iterator.hasNext()) {
			orders.add(iterator.next());
			iterator.remove();
		}

		// count the total
		criteria.setProjection(Projections.rowCount());
		long total = ((Number) criteria.uniqueResult()).longValue();

		// reset criteria
		criteria.setProjection(projection);
		criteria.setResultTransformer(resultTransformer);
		for (CriteriaImpl.OrderEntry orderEntry : orders) {
			criteria.addOrder(orderEntry.getOrder());
		}
		
		return total;
	}

}
