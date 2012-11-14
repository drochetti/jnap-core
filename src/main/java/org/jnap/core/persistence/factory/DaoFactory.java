/*
 * DaoFactory.java created on 2011-02-01
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
package org.jnap.core.persistence.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.jnap.core.bean.model.IndexedModel;
import org.jnap.core.bean.model.PersistentModel;
import org.jnap.core.persistence.Dao;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class DaoFactory implements BeanFactoryPostProcessor, Ordered  {

	private int order = Ordered.HIGHEST_PRECEDENCE;

	private Set<Class<? extends PersistentModel>> alreadyDefinedDaos;

	private Map<Class<? extends PersistentModel>, String> daoNameCache =
		new HashMap<Class<? extends PersistentModel>, String>();

	private BeanFactory beanFactory;

	/**
	 * @param entityClass
	 * @return
	 */
	private String buildDaoName(Class<? extends PersistentModel> entityClass) {
		String daoName = entityClass.getSimpleName() + "Dao";
		daoName = Character.toLowerCase(daoName.charAt(0)) + daoName.substring(1);
		return daoName;
	}

	private boolean isDaoDefinedForEntity(ConfigurableListableBeanFactory beanFactory,
			Class<? extends PersistentModel> entityClass) {
		if (alreadyDefinedDaos == null) {
			alreadyDefinedDaos = new HashSet<Class<? extends PersistentModel>>();
			for (String beanName : beanFactory.getBeanNamesForType(Dao.class, true, false)) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
				final Class<?> daoType = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(),
						DaoFactory.class.getClassLoader());
				Class daoEntityClass = GenericTypeResolver.resolveTypeArgument(daoType,	Dao.class);
				alreadyDefinedDaos.add(daoEntityClass);
				daoNameCache.put(daoEntityClass, beanName);
			}
		}
		return alreadyDefinedDaos.contains(entityClass);
	}

	protected BeanDefinition createDaoDefinition(Class<? extends PersistentModel> entityClass,
			EntityManagerFactory factory) {
		Class daoClass = IndexedModel.class.isAssignableFrom(entityClass)
				? GenericFullTextDao.class
				: GenericDao.class;
		return BeanDefinitionBuilder.genericBeanDefinition(daoClass)
				.addConstructorArgValue(entityClass)
				.addPropertyValue("entityManagerFactory", factory)
				.setScope(BeanDefinition.SCOPE_SINGLETON).getBeanDefinition();
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Assert.isAssignable(DefaultListableBeanFactory.class, beanFactory.getClass(),
				"The DaoFactory only works within a DefaultListableBeanFactory capable " +
				"BeanFactory. Your BeanFactory is " + beanFactory.getClass());
		this.beanFactory = beanFactory;
		final DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;

		String[] factoryNames = beanFactory.getBeanNamesForType(EntityManagerFactory.class);
		Set<EntityManagerFactory> factories = new HashSet<EntityManagerFactory>(factoryNames.length);
		for (String factoryName : factoryNames) {
			factories.add(beanFactory.getBean(factoryName, EntityManagerFactory.class));
		}

		for (EntityManagerFactory factory : factories) {
			factory.getMetamodel().getEntities();
			for (EntityType<?> entityMetadata : factory.getMetamodel().getEntities()) {
				Class<? extends PersistentModel> entityClass = (Class<? extends PersistentModel>) entityMetadata.getJavaType();
				if (entityClass != null && !isDaoDefinedForEntity(beanFactory, entityClass)) {
					String daoName = buildDaoName(entityClass);
					listableBeanFactory.registerBeanDefinition(daoName,	createDaoDefinition(entityClass, factory));
					daoNameCache.put(entityClass, daoName);
				}
			}
		}

		factories.clear();
		factories = null;
	}

	public int getOrder() {
		return order;
	}

	public Dao<? extends PersistentModel> getDaoFor(Class<? extends PersistentModel> modelType) {
		String beanName = this.daoNameCache.get(modelType);
		return (Dao<? extends PersistentModel>) (beanName != null && this.beanFactory.containsBean(beanName)
				? this.beanFactory.getBean(beanName)
				: null);
	}

}
