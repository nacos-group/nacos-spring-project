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

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.spring.beans.factory.annotation.AbstractAnnotationBeanPostProcessor;

/**
 * Injected {@link NacosValue}
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation.
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosValue
 * @since 0.1.0
 */
public class NacosValueAnnotationBeanPostProcessor
		extends AbstractAnnotationBeanPostProcessor implements BeanFactoryAware,
		EnvironmentAware, ApplicationListener<NacosConfigReceivedEvent> {

	/**
	 * The name of {@link NacosValueAnnotationBeanPostProcessor} bean.
	 */
	public static final String BEAN_NAME = "nacosValueAnnotationBeanPostProcessor";

	private static final String SPEL_PREFIX = "#{";

	private static final String PLACEHOLDER_PREFIX = "${";

	private static final String PLACEHOLDER_SUFFIX = "}";

	private static final String VALUE_SEPARATOR = ":";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * placeholder, nacosValueTarget.
	 */
	private Map<String, List<NacosValueTarget>> placeholderNacosValueTargetMap = new HashMap<String, List<NacosValueTarget>>();

	private ConfigurableListableBeanFactory beanFactory;

	private Environment environment;

	private BeanExpressionResolver exprResolver;

	private BeanExpressionContext exprContext;

	public NacosValueAnnotationBeanPostProcessor() {
		super(NacosValue.class);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"NacosValueAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
		this.exprResolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
		this.exprContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, null);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	protected Object doGetInjectedBean(AnnotationAttributes attributes, Object bean,
			String beanName, Class<?> injectedType,
			InjectionMetadata.InjectedElement injectedElement) throws Exception {
        Object value = resolveStringValue(attributes.getString("value"));
        Member member = injectedElement.getMember();
		if (member instanceof Field) {
			return convertIfNecessary((Field) member, value);
		}

		if (member instanceof Method) {
			return convertIfNecessary((Method) member, value);
		}

		return null;
	}

	@Override
	protected String buildInjectedObjectCacheKey(AnnotationAttributes attributes,
			Object bean, String beanName, Class<?> injectedType,
			InjectionMetadata.InjectedElement injectedElement) {
		return bean.getClass().getName() + attributes;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, final String beanName)
			throws BeansException {

		doWithFields(bean, beanName);

		doWithMethods(bean, beanName);

		return super.postProcessBeforeInitialization(bean, beanName);
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
				String md5String = MD5Utils.md5Hex(newValue, "UTF-8");
				boolean isUpdate = !target.lastMD5.equals(md5String);
				if (isUpdate) {
					target.updateLastMD5(md5String);
					Object evaluatedValue = resolveNotifyValue(target.nacosValueExpr, key, newValue);
					if (target.method == null) {
						setField(target, evaluatedValue);
					}
					else {
						setMethod(target, evaluatedValue);
					}
				}
			}
		}
	}

	private Object resolveNotifyValue(String nacosValueExpr, String key, String newValue) {
		String spelExpr = nacosValueExpr.replaceAll("\\$\\{" + key + PLACEHOLDER_SUFFIX, newValue);
		return resolveStringValue(spelExpr);
	}

	private Object resolveStringValue(String strVal) {
		String value = beanFactory.resolveEmbeddedValue(strVal);
		if (exprResolver != null && value != null) {
			return exprResolver.evaluate(value, exprContext);
		}
		return value;
	}

	private Object convertIfNecessary(Field field, Object value) {
		TypeConverter converter = beanFactory.getTypeConverter();
		return converter.convertIfNecessary(value, field.getType(), field);
	}

	private Object convertIfNecessary(Method method, Object value) {
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] arguments = new Object[paramTypes.length];

		TypeConverter converter = beanFactory.getTypeConverter();

		if (arguments.length == 1) {
			return converter.convertIfNecessary(value, paramTypes[0],
					new MethodParameter(method, 0));
		}

		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = converter.convertIfNecessary(value, paramTypes[i],
					new MethodParameter(method, i));
		}

		return arguments;
	}

	private void doWithFields(final Object bean, final String beanName) {
		ReflectionUtils.doWithFields(bean.getClass(),
				new ReflectionUtils.FieldCallback() {
					@Override
					public void doWith(Field field) throws IllegalArgumentException {
						NacosValue annotation = getAnnotation(field, NacosValue.class);
						doWithAnnotation(beanName, bean, annotation, field.getModifiers(),
								null, field);
					}
				});
	}

	private void doWithMethods(final Object bean, final String beanName) {
		ReflectionUtils.doWithMethods(bean.getClass(),
				new ReflectionUtils.MethodCallback() {
					@Override
					public void doWith(Method method) throws IllegalArgumentException {
						NacosValue annotation = getAnnotation(method, NacosValue.class);
						doWithAnnotation(beanName, bean, annotation,
								method.getModifiers(), method, null);
					}
				});
	}

	private void doWithAnnotation(String beanName, Object bean, NacosValue annotation,
			int modifiers, Method method, Field field) {
		if (annotation != null) {
			if (Modifier.isStatic(modifiers)) {
				return;
			}

			if (annotation.autoRefreshed()) {
				String placeholder = resolvePlaceholder(annotation.value());

				if (placeholder == null) {
					return;
				}

				NacosValueTarget nacosValueTarget = new NacosValueTarget(bean, beanName,
						method, field, annotation.value());
				put2ListMap(placeholderNacosValueTargetMap, placeholder,
						nacosValueTarget);
			}
		}
	}

	private String resolvePlaceholder(String placeholder) {
		if (!placeholder.startsWith(PLACEHOLDER_PREFIX) && !placeholder.startsWith(SPEL_PREFIX)) {
			return null;
		}

		if (!placeholder.endsWith(PLACEHOLDER_SUFFIX)) {
			return null;
		}

		if (placeholder.length() <= PLACEHOLDER_PREFIX.length()
				+ PLACEHOLDER_SUFFIX.length()) {
			return null;
		}
        int beginIndex = placeholder.indexOf(PLACEHOLDER_PREFIX);
		if (beginIndex == -1) {
		    return null;
        }
		beginIndex = beginIndex + PLACEHOLDER_PREFIX.length();
        int endIndex = placeholder.indexOf(PLACEHOLDER_SUFFIX, beginIndex);
		if (endIndex == -1) {
		    return null;
        }
		placeholder = placeholder.substring(beginIndex, endIndex);

		int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
		if (separatorIndex != -1) {
			return placeholder.substring(0, separatorIndex);
		}

		return placeholder;
	}

	private <K, V> void put2ListMap(Map<K, List<V>> map, K key, V value) {
		List<V> valueList = map.get(key);
		if (valueList == null) {
			valueList = new ArrayList<V>();
		}
		valueList.add(value);
		map.put(key, valueList);
	}

	private void setMethod(NacosValueTarget nacosValueTarget, Object propertyValue) {
		Method method = nacosValueTarget.method;
		ReflectionUtils.makeAccessible(method);
		try {
			method.invoke(nacosValueTarget.bean,
					convertIfNecessary(method, propertyValue));

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

	private void setField(final NacosValueTarget nacosValueTarget,
			final Object propertyValue) {
		final Object bean = nacosValueTarget.bean;

		Field field = nacosValueTarget.field;

		String fieldName = field.getName();

		try {
			ReflectionUtils.makeAccessible(field);
			field.set(bean, convertIfNecessary(field, propertyValue));

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

	private static class NacosValueTarget {

		private final Object bean;

		private final String beanName;

		private final Method method;

		private final Field field;

		private String lastMD5;

		private final String nacosValueExpr;

		NacosValueTarget(Object bean, String beanName, Method method, Field field, String nacosValueExpr) {
			this.bean = bean;

			this.beanName = beanName;

			this.method = method;

			this.field = field;

			this.lastMD5 = "";

			this.nacosValueExpr = resolveExpr(nacosValueExpr);
		}

		private String resolveExpr(String nacosValueExpr) {
			int replaceHolderBegin = nacosValueExpr.indexOf(PLACEHOLDER_PREFIX) + PLACEHOLDER_PREFIX.length();
			int replaceHolderEnd = nacosValueExpr.indexOf(PLACEHOLDER_SUFFIX, replaceHolderBegin);

			String replaceHolder = nacosValueExpr.substring(replaceHolderBegin, replaceHolderEnd);
			int separatorIndex = replaceHolder.indexOf(VALUE_SEPARATOR);
			if (separatorIndex != -1) {
				return nacosValueExpr.substring(0, separatorIndex + replaceHolderBegin) + nacosValueExpr.substring(replaceHolderEnd);
			}
			return nacosValueExpr;
		}

		protected void updateLastMD5(String newMD5) {
			this.lastMD5 = newMD5;
		}

	}

}
