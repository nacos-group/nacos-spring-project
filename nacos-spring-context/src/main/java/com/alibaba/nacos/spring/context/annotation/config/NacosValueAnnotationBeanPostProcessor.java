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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.spring.beans.factory.annotation.AnnotationInjectedBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosValue
 * @since 0.1.0
 */
public class NacosValueAnnotationBeanPostProcessor extends AnnotationInjectedBeanPostProcessor<NacosValue>
    implements BeanFactoryAware, ApplicationListener<NacosConfigReceivedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The name of {@link NacosValueAnnotationBeanPostProcessor} bean
     */
    public static final String BEAN_NAME = "nacosValueAnnotationBeanPostProcessor";

    private static final String PLACEHOLDER_PREFIX = "${";

    private static final String PLACEHOLDER_SUFFIX = "}";

    private static final String VALUE_SEPARATOR = ":";

    /**
     * placeholder, nacosValueTarget
     */
    private Map<String, List<NacosValueTarget>> placeholderNacosValueTargetMap
        = new HashMap<String, List<NacosValueTarget>>();

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    protected Object doGetInjectedBean(NacosValue annotation, Object bean, String beanName, Class<?> injectedType,
                                       InjectionMetadata.InjectedElement injectedElement) {
        String annotationValue = annotation.value();
        String value = beanFactory.resolveEmbeddedValue(annotationValue);

        Member member = injectedElement.getMember();
        if (member instanceof Field) {
            return convertIfNecessary((Field)member, value);
        }

        if (member instanceof Method) {
            return convertIfNecessary((Method)member, value);
        }

        return null;
    }

    @Override
    protected String buildInjectedObjectCacheKey(NacosValue annotation, Object bean, String beanName,
                                                 Class<?> injectedType,
                                                 InjectionMetadata.InjectedElement injectedElement) {
        return bean.getClass().getName() + annotation;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                "NacosValueAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
        }
        this.beanFactory = (ConfigurableListableBeanFactory)beanFactory;
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
        String content = event.getContent();
        if (content != null) {
            Properties configProperties = toProperties(event.getDataId(), event.getGroupId(), content, event.getType());

            for (Object key : configProperties.keySet()) {
                String propertyKey = (String)key;

                List<NacosValueTarget> beanPropertyList = placeholderNacosValueTargetMap.get(propertyKey);
                if (beanPropertyList == null) {
                    continue;
                }

                String propertyValue = configProperties.getProperty(propertyKey);
                for (NacosValueTarget nacosValueTarget : beanPropertyList) {
                    if (nacosValueTarget.method == null) {
                        setField(nacosValueTarget, propertyValue);
                    } else {
                        setMethod(nacosValueTarget, propertyValue);
                    }
                }
            }
        }
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
            return converter.convertIfNecessary(value, paramTypes[0], new MethodParameter(method, 0));
        }

        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = converter.convertIfNecessary(value, paramTypes[i], new MethodParameter(method, i));
        }

        return arguments;
    }

    private void doWithFields(final Object bean, final String beanName) {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {
                NacosValue annotation = getAnnotation(field, NacosValue.class);
                doWithAnnotation(beanName, bean, annotation, field.getModifiers(), null, field);
            }
        });
    }

    private void doWithMethods(final Object bean, final String beanName) {
        ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException {
                NacosValue annotation = getAnnotation(method, NacosValue.class);
                doWithAnnotation(beanName, bean, annotation, method.getModifiers(), method, null);
            }
        });
    }

    private void doWithAnnotation(String beanName, Object bean, NacosValue annotation, int modifiers, Method method,
                                  Field field) {
        if (annotation != null) {
            if (Modifier.isStatic(modifiers)) {
                return;
            }

            if (annotation.autoRefreshed()) {
                String placeholder = resolvePlaceholder(annotation.value());

                if (placeholder == null) {
                    return;
                }

                NacosValueTarget nacosValueTarget = new NacosValueTarget(bean, beanName, method, field);
                put2ListMap(placeholderNacosValueTargetMap, placeholder, nacosValueTarget);
            }
        }
    }

    private String resolvePlaceholder(String placeholder) {
        if (!placeholder.startsWith(PLACEHOLDER_PREFIX)) {
            return null;
        }

        if (!placeholder.endsWith(PLACEHOLDER_SUFFIX)) {
            return null;
        }

        if (placeholder.length() <= PLACEHOLDER_PREFIX.length() + PLACEHOLDER_SUFFIX.length()) {
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

    private <K, V> void put2ListMap(Map<K, List<V>> map, K key, V value) {
        List<V> valueList = map.get(key);
        if (valueList == null) {
            valueList = new ArrayList<V>();
        }
        valueList.add(value);
        map.put(key, valueList);
    }

    private void setMethod(NacosValueTarget nacosValueTarget, String propertyValue) {
        Method method = nacosValueTarget.method;
        ReflectionUtils.makeAccessible(method);
        try {
            method.invoke(nacosValueTarget.bean, convertIfNecessary(method, propertyValue));

            if (logger.isDebugEnabled()) {
                logger.debug("Update value with {} (method) in {} (bean) with {}",
                    method.getName(), nacosValueTarget.beanName, propertyValue);
            }
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                    "Can't update value with " + method.getName() + " (method) in "
                        + nacosValueTarget.beanName + " (bean)", e);
            }
        }
    }

    private void setField(final NacosValueTarget nacosValueTarget, final String propertyValue) {
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
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                    "Can't update value of the " + fieldName + " (field) in "
                        + nacosValueTarget.beanName + " (bean)", e);
            }
        }
    }

    private static class NacosValueTarget {

        private Object bean;

        private String beanName;

        private Method method;

        private Field field;

        NacosValueTarget(Object bean, String beanName, Method method, Field field) {
            this.bean = bean;

            this.beanName = beanName;

            this.method = method;

            this.field = field;
        }
    }

}
