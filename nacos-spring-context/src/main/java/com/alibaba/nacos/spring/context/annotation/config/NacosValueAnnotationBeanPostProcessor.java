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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
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

    // placeholder, beanFieldProperty
    private Map<String, List<BeanFieldProperty>> placeholderPropertyListMap = new HashMap<String, List<BeanFieldProperty>>();

    // beanFieldProperty, bean
    private Map<BeanFieldProperty, List<Object>> propertyBeanListMap = new HashMap<BeanFieldProperty, List<Object>>();

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    protected Object doGetInjectedBean(NacosValue annotation, Object bean, String beanName, Class<?> injectedType,
                                       InjectionMetadata.InjectedElement injectedElement) throws Exception {

        String value = annotation.value();
        return beanFactory.resolveEmbeddedValue(value);
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

        final Map<String, List<BeanFieldProperty>> beanNamePropertyListMap = new HashMap<String, List<BeanFieldProperty>>();

        doWithAnnotationFields(bean, beanName, beanNamePropertyListMap);

        List<BeanFieldProperty> beanPropertyList = beanNamePropertyListMap.get(beanName);
        if (beanPropertyList != null) {
            for (BeanFieldProperty beanFieldProperty : beanPropertyList) {
                put2ListMap(propertyBeanListMap, beanFieldProperty, bean);
            }
        }

        return super.postProcessBeforeInitialization(bean, beanName);
    }

    private void doWithAnnotationFields(Object bean, final String beanName,
                                        final Map<String, List<BeanFieldProperty>> beanNamePropertyListMap) {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {

                doWithNacosValueField(field);

            }

            private void doWithNacosValueField(Field field) {
                NacosValue annotation = getAnnotation(field, NacosValue.class);
                if (annotation != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        return;
                    }
                    if (annotation.autoRefreshed()) {
                        String placeHolder = annotation.value();
                        doWithListMap(field, placeHolder);
                    }
                }
            }

            private void doWithListMap(Field field, String placeholder) {
                if (!placeholder.startsWith(PLACEHOLDER_PREFIX)) {
                    return;
                }

                if (!placeholder.endsWith(PLACEHOLDER_SUFFIX)) {
                    return;
                }

                if (placeholder.length() <= PLACEHOLDER_PREFIX.length() + PLACEHOLDER_SUFFIX.length()) {
                    return;
                }

                String actualPlaceholder = resolveActualPlaceholder(placeholder);
                BeanFieldProperty beanFieldProperty = new BeanFieldProperty(beanName, field.getName(), actualPlaceholder);
                put2ListMap(beanNamePropertyListMap, beanName, beanFieldProperty);
                put2ListMap(placeholderPropertyListMap, actualPlaceholder, beanFieldProperty);
            }

            private String resolveActualPlaceholder(String placeholder) {
                int beginIndex = PLACEHOLDER_PREFIX.length();
                int endIndex = placeholder.length() - PLACEHOLDER_PREFIX.length() + 1;

                placeholder = placeholder.substring(beginIndex, endIndex);

                int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
                if (separatorIndex != -1) {
                    return placeholder.substring(0, separatorIndex);
                }

                return placeholder;
            }

        });
    }

    private <K, V> void put2ListMap(Map<K, List<V>> map, K key, V value) {
        List<V> valueList = map.get(key);
        if (valueList == null) {
            valueList = new ArrayList<V>();
        }
        valueList.add(value);
        map.put(key, valueList);
    }

    @Override
    public void onApplicationEvent(NacosConfigReceivedEvent event) {
        String content = event.getContent();
        if (content != null) {
            Map<Object, List<BeanFieldProperty>> map = new HashMap<Object, List<BeanFieldProperty>>();
            Properties configProperties = toProperties(content);
            for (Object key : configProperties.keySet()) {
                List<BeanFieldProperty> beanPropertyList = placeholderPropertyListMap.get(key.toString());
                if (beanPropertyList == null) {
                    continue;
                }
                for (BeanFieldProperty beanFieldProperty : beanPropertyList) {
                    List<Object> beanList = propertyBeanListMap.get(beanFieldProperty);
                    if (beanList == null) {
                        continue;
                    }
                    for (Object bean : beanList) {
                        beanFieldProperty.setKey((String)key);
                        put2ListMap(map, bean, beanFieldProperty);
                    }
                }
            }
            doBind(map, configProperties);
        }

    }

    private void doBind(Map<Object, List<BeanFieldProperty>> map,
                        Properties configProperties) {
        for (Map.Entry<Object, List<BeanFieldProperty>> entry : map.entrySet()) {
            Object bean = entry.getKey();
            List<BeanFieldProperty> beanPropertyList = entry.getValue();
            doWithFields(bean, configProperties, beanPropertyList);
        }
    }

    private void doWithFields(final Object bean, final Properties configProperties,
                              final List<BeanFieldProperty> beanPropertyList) {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {
                BeanFieldProperty beanFieldProperty = resolveBeanFieldProperty(field, beanPropertyList);
                if (beanFieldProperty == null) {
                    return;
                }

                if (configProperties.containsKey(beanFieldProperty.key)) {
                    String propertyValue = configProperties.getProperty(beanFieldProperty.key);
                    String fieldName = field.getName();
                    try {
                        setFieldValue(bean, field, propertyValue);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Update value of the {}" + " (field) in {} (bean) with {}", fieldName,
                                beanFieldProperty.beanName, propertyValue);
                        }
                    } catch (IllegalAccessException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error(
                                "Can't update value of the " + fieldName + " (field) in " + beanFieldProperty.beanName
                                    + " (bean)", e);
                        }
                    }
                }
            }
        });
    }

    private void setFieldValue(Object bean, Field field, Object value) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(bean, value);
        field.setAccessible(accessible);
    }

    private BeanFieldProperty resolveBeanFieldProperty(Field field, final List<BeanFieldProperty> beanPropertyList) {
        for (BeanFieldProperty beanFieldProperty : beanPropertyList) {
            if (field.getName().equals(beanFieldProperty.name)) {
                return beanFieldProperty;
            }
        }
        return null;
    }

    private static class BeanFieldProperty {
        private String beanName;
        private String name;
        private String value;
        private String key;

        BeanFieldProperty(String beanName, String name, String value) {
            this.beanName = beanName;
            this.name = name;
            this.value = value;
        }

        void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "BeanFieldProperty{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }

}
