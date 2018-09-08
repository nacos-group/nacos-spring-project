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
package com.alibaba.nacos.spring.context.annotation;

import com.alibaba.nacos.api.annotation.NacosValue;
import com.alibaba.nacos.spring.context.event.NacosConfigReceivedEvent;
import com.alibaba.spring.beans.factory.annotation.AnnotationInjectedBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosValue
 * @since 0.1.0
 */
public class NacosValueAnnotationBeanPostProcessor extends AnnotationInjectedBeanPostProcessor<NacosValue>
    implements BeanFactoryAware, ApplicationListener<NacosConfigReceivedEvent> {

    /**
     * The name of {@link NacosValueAnnotationBeanPostProcessor} bean
     */
    public static final String BEAN_NAME = "nacosValueAnnotationBeanPostProcessor";

    private static final String PLACEHOLDER_PREFIX = "${";

    private static final String PLACEHOLDER_SUFFIX = "}";

    // placeholder, beanProperty
    private Map<String, List<BeanProperty>> placeholderPropertyListMap = new HashMap<String, List<BeanProperty>>();

    // beanProperty, bean
    private Map<BeanProperty, List<Object>> propertyBeanListMap = new HashMap<BeanProperty, List<Object>>();

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

        final Map<String, List<BeanProperty>> beanNamePropertyListMap = new HashMap<String, List<BeanProperty>>();

        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {
                NacosValue annotation = getAnnotation(field, NacosValue.class);
                if (annotation != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        return;
                    }
                    String placeHolder = annotation.value();
                    BeanProperty beanProperty = new BeanProperty(field.getName(), placeHolder);
                    put2ListMap(beanNamePropertyListMap, beanName, beanProperty);
                    put2ListMap(placeholderPropertyListMap, placeHolder, beanProperty);
                }
            }
        });

        List<BeanProperty> beanPropertyList = beanNamePropertyListMap.get(beanName);
        if (beanPropertyList != null) {
            for (BeanProperty beanProperty : beanPropertyList) {
                put2ListMap(propertyBeanListMap, beanProperty, bean);
            }
        }

        return super.postProcessBeforeInitialization(bean, beanName);
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
            Map<Object, List<BeanProperty>> map = new HashMap<Object, List<BeanProperty>>();
            Properties configProperties = toProperties(content);
            for (Object key : configProperties.keySet()) {
                String placeholder = PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX;
                List<BeanProperty> beanPropertyList = placeholderPropertyListMap.get(placeholder);
                if (beanPropertyList == null) {
                    continue;
                }
                for (BeanProperty beanProperty : beanPropertyList) {
                    List<Object> beanList = propertyBeanListMap.get(beanProperty);
                    if (beanList == null) {
                        continue;
                    }
                    for (Object bean : beanList) {
                        beanProperty.setKey((String)key);
                        put2ListMap(map, bean, beanProperty);
                    }
                }
            }
            doBind(map, configProperties);
        }

    }

    private void doBind(Map<Object, List<BeanProperty>> map,
                        Properties configProperties) {
        for (Map.Entry<Object, List<BeanProperty>> entry : map.entrySet()) {
            Object bean = entry.getKey();
            List<BeanProperty> beanPropertyList = entry.getValue();
            PropertyValues propertyValues = resolvePropertyValues(bean, configProperties, beanPropertyList);

            DataBinder dataBinder = new DataBinder(bean);
            dataBinder.bind(propertyValues);
        }
    }

    private PropertyValues resolvePropertyValues(Object bean, final Properties configProperties,
                                                 final List<BeanProperty> beanPropertyList) {
        final MutablePropertyValues propertyValues = new MutablePropertyValues();
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {
                String propertyName = resolvePropertyName(field, beanPropertyList);
                if (hasText(propertyName) && configProperties.containsKey(propertyName)) {
                    String propertyValue = configProperties.getProperty(propertyName);
                    propertyValues.add(field.getName(), propertyValue);
                }
            }
        });
        return propertyValues;
    }

    private String resolvePropertyName(Field field, final List<BeanProperty> beanPropertyList) {
        for (BeanProperty beanProperty : beanPropertyList) {
            if (field.getName().equals(beanProperty.name)) {
                return beanProperty.getKey();
            }
        }
        return null;
    }

    private static class BeanProperty {
        private String name;
        private String value;
        private String key;

        BeanProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "BeanProperty{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }

}
