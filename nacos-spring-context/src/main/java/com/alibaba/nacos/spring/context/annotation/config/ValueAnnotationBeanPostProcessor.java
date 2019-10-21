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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.client.config.utils.MD5;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.nacos.spring.util.ObjectUtils;
import com.alibaba.nacos.spring.util.Tuple;
import com.alibaba.spring.beans.factory.annotation.AnnotationInjectedBeanPostProcessor;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * Abstract @*Value annotation injection and refresh
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.4
 */
public abstract class ValueAnnotationBeanPostProcessor<A extends Annotation>
		extends AnnotationInjectedBeanPostProcessor<A> implements BeanFactoryAware,
		EnvironmentAware, ApplicationListener<NacosConfigReceivedEvent> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String PLACEHOLDER_PREFIX = "${";

	private static final String PLACEHOLDER_SUFFIX = "}";

	private static final String VALUE_SEPARATOR = ":";

	protected Map<String, List<NacosValueTarget>> placeholderNacosValueTargetMap = new HashMap<String, List<NacosValueTarget>>();

	protected ConfigurableListableBeanFactory beanFactory;
	protected Environment environment;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"ValueAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	private void put2ListMap(String key, NacosValueTarget value) {
		if (key == null || value == null) {
			return;
		}
		List<NacosValueTarget> valueList = placeholderNacosValueTargetMap.get(key);
		if (valueList == null) {
			valueList = new ArrayList<NacosValueTarget>();
		}
		valueList.add(value);
		placeholderNacosValueTargetMap.put(key, valueList);
	}

	protected void setMethod(NacosValueTarget nacosValueTarget, String propertyValue) {
		Method method = nacosValueTarget.method;
		ReflectionUtils.makeAccessible(method);
		try {
			method.invoke(nacosValueTarget.bean,
					ObjectUtils.convertIfNecessary(beanFactory, method, propertyValue));

			if (logger.isDebugEnabled()) {
				logger.debug("Update value with {} (method) in {} (bean) with {}",
						method.getName(), nacosValueTarget.beanName, propertyValue);
			}
		}
		catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error("Can't update value with " + method.getName()
						+ " (method) in " + nacosValueTarget.beanName + " (bean)", e);
			}
		}
	}

	protected void setField(final NacosValueTarget nacosValueTarget,
			final String propertyValue) {
		final Object bean = nacosValueTarget.bean;

		Field field = nacosValueTarget.field;

		String fieldName = field.getName();

		try {
			ReflectionUtils.makeAccessible(field);
			field.set(bean,
					ObjectUtils.convertIfNecessary(beanFactory, field, propertyValue));

			if (logger.isDebugEnabled()) {
				logger.debug("Update value of the {}" + " (field) in {} (bean) with {}",
						fieldName, nacosValueTarget.beanName, propertyValue);
			}
		}
		catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error("Can't update value of the " + fieldName + " (field) in "
						+ nacosValueTarget.beanName + " (bean)", e);
			}
		}
	}

	protected void doWithFields(final Object bean, final String beanName,
			final Class<A> aClass) {
		ReflectionUtils.doWithFields(bean.getClass(),
				new ReflectionUtils.FieldCallback() {
					@Override
					public void doWith(Field field) throws IllegalArgumentException {
						A annotation = getAnnotation(field, aClass);
						Tuple<String, NacosValueTarget> tuple = doWithAnnotation(beanName,
								bean, annotation, field.getModifiers(), null, field);
						put2ListMap(tuple.getFirst(), tuple.getSecond());
					}
				});
	}

	protected void doWithMethods(final Object bean, final String beanName,
			final Class<A> aClass) {
		ReflectionUtils.doWithMethods(bean.getClass(),
				new ReflectionUtils.MethodCallback() {
					@Override
					public void doWith(Method method) throws IllegalArgumentException {
						A annotation = getAnnotation(method, aClass);
						Tuple<String, NacosValueTarget> tuple = doWithAnnotation(beanName,
								bean, annotation, method.getModifiers(), method, null);
						put2ListMap(tuple.getFirst(), tuple.getSecond());
					}
				});
	}

	protected abstract Tuple<String, NacosValueTarget> doWithAnnotation(String beanName,
			Object bean, A annotation, int modifiers, Method method, Field field);

	protected String resolvePlaceholder(String placeholder) {
		if (!placeholder.startsWith(PLACEHOLDER_PREFIX)) {
			return null;
		}

		if (!placeholder.endsWith(PLACEHOLDER_SUFFIX)) {
			return null;
		}

		if (placeholder.length() <= PLACEHOLDER_PREFIX.length()
				+ PLACEHOLDER_SUFFIX.length()) {
			return null;
		}

		int beginIndex = PLACEHOLDER_PREFIX.length();
		int endIndex = placeholder.length() - PLACEHOLDER_PREFIX.length() + 1;
		placeholder = placeholder.substring(beginIndex, endIndex);

		int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
		if (separatorIndex != -1) {
			return placeholder.substring(0, separatorIndex);
		}

		return placeholder;
	}

	@Override
	public void onApplicationEvent(NacosConfigReceivedEvent event) {
		// In to this event receiver, the environment has been updated the
		// latest configuration information, pull directly from the environment
		// fix issue #142
		for (Map.Entry<String, List<NacosValueTarget>> entry : placeholderNacosValueTargetMap
				.entrySet()) {
			String key = environment.resolvePlaceholders(entry.getKey());
			String newValue = environment.getProperty(key);
			if (newValue == null) {
				continue;
			}
			List<NacosValueTarget> beanPropertyList = entry.getValue();
			for (NacosValueTarget target : beanPropertyList) {
				String md5String = MD5.getInstance().getMD5String(newValue);
				if (isChange(md5String, target)) {
					target.updateLastMD5(md5String);
					if (target.getMethod() == null) {
						setField(target, newValue);
					}
					else {
						setMethod(target, newValue);
					}
				}
			}
		}
	}

	public static class NacosValueTarget {

		private final Object bean;

		private final String beanName;

		private final Method method;

		private final Field field;

		private String lastMD5;

		private String annotationType;

		public Object getBean() {
			return bean;
		}

		public String getBeanName() {
			return beanName;
		}

		public Method getMethod() {
			return method;
		}

		public Field getField() {
			return field;
		}

		public String getLastMD5() {
			return lastMD5;
		}

		public void setLastMD5(String lastMD5) {
			this.lastMD5 = lastMD5;
		}

		public String getAnnotationType() {
			return annotationType;
		}

		public void setAnnotationType(String annotationType) {
			this.annotationType = annotationType;
		}

		NacosValueTarget(Object bean, String beanName, Method method, Field field) {
			this.bean = bean;

			this.beanName = beanName;

			this.method = method;

			this.field = field;

			this.lastMD5 = "";
		}

		protected void updateLastMD5(String newMD5) {
			this.lastMD5 = newMD5;
		}

	}

	protected static boolean isChange(String newMd5, NacosValueTarget target) {
		return !Objects.equal(newMd5, target.lastMD5);
	}

}
