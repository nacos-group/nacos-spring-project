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
package com.alibaba.nacos.spring.context.annotation.config;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.alibaba.nacos.spring.util.ObjectUtils;
import com.alibaba.nacos.spring.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Value;

/**
 * Support @ Value automatically refresh
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.4
 */
public class SpringValueAnnotationBeanPostProcessor
		extends ValueAnnotationBeanPostProcessor<Value> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The name of {@link SpringValueAnnotationBeanPostProcessor} bean
	 */
	public static final String BEAN_NAME = "springValueAnnotationBeanPostProcessor";

	@Override
	protected Object doGetInjectedBean(Value value, Object bean, String beanName,
			Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement)
			throws Exception {
		String annotationValue = value.value();
		String placeHolder = beanFactory.resolveEmbeddedValue(annotationValue);

		Member member = injectedElement.getMember();
		if (member instanceof Field) {
			return ObjectUtils.convertIfNecessary(beanFactory, (Field) member,
					placeHolder);
		}

		if (member instanceof Method) {
			return ObjectUtils.convertIfNecessary(beanFactory, (Method) member,
					placeHolder);
		}

		return null;
	}

	@Override
	protected String buildInjectedObjectCacheKey(Value value, Object bean,
			String beanName, Class<?> injectedType,
			InjectionMetadata.InjectedElement injectedElement) {
		return bean.getClass().getName() + value;
	}

	@Override
	protected Tuple<String, NacosValueTarget> doWithAnnotation(String beanName,
			Object bean, Value annotation, int modifiers, Method method, Field field) {
		if (annotation != null) {
			if (Modifier.isStatic(modifiers)) {
				return Tuple.empty();
			}

			if (bean.getClass().isAnnotationPresent(NacosRefresh.class)) {
				String placeholder = resolvePlaceholder(annotation.value());

				if (placeholder == null) {
					return Tuple.empty();
				}

				NacosValueTarget nacosValueTarget = new NacosValueTarget(bean, beanName,
						method, field);
				nacosValueTarget.setAnnotationType(getAnnotationType().getSimpleName());
				logger.debug("@Value register auto refresh");
				return Tuple.of(placeholder, nacosValueTarget);
			}
		}
		return Tuple.empty();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {

		doWithFields(bean, beanName, Value.class);

		doWithMethods(bean, beanName, Value.class);

		return super.postProcessBeforeInitialization(bean, beanName);
	}
}
