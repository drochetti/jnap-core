/*
 * PersistenceProvider.java created on 2011-12-29
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

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.event.PreUpdateEventListener;

/**
 * Custom {@link javax.persistence.spi.PersistenceProvider} for Hibernate to allow configuration of Hibernate event
 * listeners (such as {@link PostInsertEventListener} and {@link PreDeleteEventListener} and other custom
 * Hibernate-specific configurations (such as package mapping, e.g. package-info.java).
 * 
 * TODO: limited to common (insert, update, delete and load) events. Collection events should be added in a near future.
 * 
 * @author Daniel Rochetti
 * @since 0.9.4
 */
public class PersistenceProvider extends HibernatePersistence {

	private String[] annotatedPackages;

	private PostInsertEventListener[] postInsertEventListeners;
	private PostDeleteEventListener[] postDeleteEventListeners;
	private PostLoadEventListener[] postLoadEventListeners;
	private PostUpdateEventListener[] postUpdateEventListeners;
	private PreInsertEventListener[] preInsertEventListeners;
	private PreDeleteEventListener[] preDeleteEventListeners;
	private PreLoadEventListener[] preLoadEventListeners;
	private PreUpdateEventListener[] preUpdateEventListeners;

	@SuppressWarnings("rawtypes")
	@Override
	public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
		Ejb3Configuration cfg = new Ejb3Configuration();
		setupConfiguration(cfg);
		Ejb3Configuration configured = cfg.configure(persistenceUnitName, properties);
		return configured != null ? configured.buildEntityManagerFactory() : null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		Ejb3Configuration cfg = new Ejb3Configuration();
		setupConfiguration(cfg);
		Ejb3Configuration configured = cfg.configure(info, properties);
		return configured != null ? configured.buildEntityManagerFactory() : null;
	}

	/**
	 * 
	 * @param cfg
	 */
	protected void setupConfiguration(Ejb3Configuration cfg) {
		if (this.annotatedPackages != null) {
			for (String annotatedPackage : this.annotatedPackages) {
				cfg.addPackage(annotatedPackage);
			}
		}

		// bind Hibernate listeners
		final EventListeners eventListeners = cfg.getEventListeners();
		if (postInsertEventListeners != null) {
			eventListeners.setPostInsertEventListeners(postInsertEventListeners);
		}
		if (postDeleteEventListeners != null) {
			eventListeners.setPostDeleteEventListeners(postDeleteEventListeners);
		}
		if (postLoadEventListeners != null) {
			eventListeners.setPostLoadEventListeners(postLoadEventListeners);
		}
		if (postUpdateEventListeners != null) {
			eventListeners.setPostUpdateEventListeners(postUpdateEventListeners);
		}
		if (preInsertEventListeners != null) {
			eventListeners.setPreInsertEventListeners(preInsertEventListeners);
		}
		if (preDeleteEventListeners != null) {
			eventListeners.setPreDeleteEventListeners(preDeleteEventListeners);
		}
		if (preLoadEventListeners != null) {
			eventListeners.setPreLoadEventListeners(preLoadEventListeners);
		}
		if (preUpdateEventListeners != null) {
			eventListeners.setPreUpdateEventListeners(preUpdateEventListeners);
		}
	}

	public void setAnnotatedPackages(String[] annotatedPackages) {
		this.annotatedPackages = annotatedPackages;
	}

	public void setPostInsertEventListeners(
			PostInsertEventListener[] postInsertEventListeners) {
		this.postInsertEventListeners = postInsertEventListeners;
	}

	public void setPostDeleteEventListeners(
			PostDeleteEventListener[] postDeleteEventListeners) {
		this.postDeleteEventListeners = postDeleteEventListeners;
	}

	public void setPostLoadEventListeners(
			PostLoadEventListener[] postLoadEventListeners) {
		this.postLoadEventListeners = postLoadEventListeners;
	}

	public void setPostUpdateEventListeners(
			PostUpdateEventListener[] postUpdateEventListeners) {
		this.postUpdateEventListeners = postUpdateEventListeners;
	}

	public void setPreInsertEventListeners(
			PreInsertEventListener[] preInsertEventListeners) {
		this.preInsertEventListeners = preInsertEventListeners;
	}

	public void setPreDeleteEventListeners(
			PreDeleteEventListener[] preDeleteEventListeners) {
		this.preDeleteEventListeners = preDeleteEventListeners;
	}

	public void setPreLoadEventListeners(
			PreLoadEventListener[] preLoadEventListeners) {
		this.preLoadEventListeners = preLoadEventListeners;
	}

	public void setPreUpdateEventListeners(
			PreUpdateEventListener[] preUpdateEventListeners) {
		this.preUpdateEventListeners = preUpdateEventListeners;
	}

}
