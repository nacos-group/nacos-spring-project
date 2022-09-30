/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.spring.beans.factory.annotation;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.spring.beans.factory.annotation.AbstractAnnotationBeanPostProcessor;
import com.alibaba.spring.beans.factory.annotation.AnnotationInjectedBeanPostProcessor;
import com.alibaba.spring.util.BeanUtils;

/**
 * {@link AnnotationInjectedBeanPostProcessor} implementation is used to inject
 * {@link ConfigService} or {@link NamingService} instance into a Spring Bean If it's
 * attributes or properties annotated {@link NacosInjected @NacosInjected}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class AnnotationNacosInjectedBeanPostProcessor
		extends AbstractAnnotationBeanPostProcessor implements InitializingBean {

	/**
	 * The name of {@link AnnotationNacosInjectedBeanPostProcessor}
	 */
	public static final String BEAN_NAME = "annotationNacosInjectedBeanPostProcessor";

	private Map<Class<?>, AbstractNacosServiceBeanBuilder> nacosServiceBeanBuilderMap;

	public AnnotationNacosInjectedBeanPostProcessor() {
		super(NacosInjected.class);
	}

	@Override
	public final void afterPropertiesSet() {
		// Get beanFactory from super
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();

		initNacosServiceBeanBuilderMap(beanFactory);
	}

	private void initNacosServiceBeanBuilderMap(
			ConfigurableListableBeanFactory beanFactory) {

		Class<AbstractNacosServiceBeanBuilder> builderClass = AbstractNacosServiceBeanBuilder.class;

		String[] beanNames = BeanUtils.getBeanNames(beanFactory, builderClass);
		if (beanNames.length == 0) {
			throw new NoSuchBeanDefinitionException(builderClass,
					format("Please check the BeanDefinition of %s in Spring BeanFactory",
							builderClass));
		}

		Collection<AbstractNacosServiceBeanBuilder> serviceBeanBuilders = new ArrayList<AbstractNacosServiceBeanBuilder>(
				beanNames.length);
		for (String beanName : beanNames) {
			serviceBeanBuilders.add(beanFactory.getBean(beanName, builderClass));
		}

		if (serviceBeanBuilders.isEmpty()) {
			throw new NoSuchBeanDefinitionException(builderClass,
					format("Please check the BeanDefinition of %s in Spring BeanFactory",
							builderClass));
		}

		Map<Class<?>, AbstractNacosServiceBeanBuilder> builderMap = new HashMap<Class<?>, AbstractNacosServiceBeanBuilder>(
				serviceBeanBuilders.size());

		for (AbstractNacosServiceBeanBuilder serviceBeanBuilder : serviceBeanBuilders) {
			Class<?> type = serviceBeanBuilder.getType();
			builderMap.put(type, serviceBeanBuilder);
		}

		// Should not be modified in future
		this.nacosServiceBeanBuilderMap = unmodifiableMap(builderMap);
	}

	@Override
	protected Object doGetInjectedBean(AnnotationAttributes attributes, Object bean,
			String beanName, Class<?> injectedType,
			InjectionMetadata.InjectedElement injectedElement) throws Exception {
		AbstractNacosServiceBeanBuilder serviceBeanBuilder = nacosServiceBeanBuilderMap
				.get(injectedType);

		Map<String, Object> nacosProperties = getNacosProperties(attributes);

		return serviceBeanBuilder.build(nacosProperties);
	}

	@Override
	protected String buildInjectedObjectCacheKey(AnnotationAttributes attributes,
			Object bean, String beanName, Class<?> injectedType,
			InjectionMetadata.InjectedElement injectedElement) {
		StringBuilder keyBuilder = new StringBuilder(injectedType.getSimpleName());

		AbstractNacosServiceBeanBuilder serviceBeanBuilder = nacosServiceBeanBuilderMap
				.get(injectedType);

		if (serviceBeanBuilder == null) {
			throw new UnsupportedOperationException(format(
					"Only support to inject types[%s] instance , however actual injected type [%s] in member[%s]",
					nacosServiceBeanBuilderMap.keySet(), injectedType,
					injectedElement.getMember()));
		}

		Map<String, Object> nacosProperties = getNacosProperties(attributes);

		Properties properties = serviceBeanBuilder.resolveProperties(nacosProperties);

		keyBuilder.append(properties);

		return keyBuilder.toString();
	}

	private Map<String, Object> getNacosProperties(AnnotationAttributes attributes) {
		return (Map<String, Object>) attributes.get("properties");
	}
}
