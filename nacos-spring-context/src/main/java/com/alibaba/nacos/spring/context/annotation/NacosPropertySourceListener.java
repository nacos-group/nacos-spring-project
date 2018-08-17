/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.spring.context.annotation;

import com.alibaba.nacos.spring.context.event.NacosConfigReceiveEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link NacosPropertySource @NacosPropertySource} Listener
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceListener implements BeanFactoryPostProcessor, BeanPostProcessor, PriorityOrdered,
    ApplicationListener<NacosConfigReceiveEvent> {

    /**
     * The bean name of {@link NacosPropertySourceListener}
     */
    public static final String BEAN_NAME = "nacosPropertySourceListener";

    private static final String PLACEHOLDER_PREFIX = "${";

    private static final String PLACEHOLDER_SUFFIX = "}";

    // beanName, beanProperty
    private Map<String, List<BeanProperty>> beanNamePropertyListMap = new HashMap<String, List<BeanProperty>>();

    // placeholder, beanProperty
    private Map<String, List<BeanProperty>> placeholderPropertyListMap = new HashMap<String, List<BeanProperty>>();

    // beanProperty, bean
    private Map<BeanProperty, List<Object>> propertyBeanListMap = new HashMap<BeanProperty, List<Object>>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (final String curName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(curName);
            PropertyValue[] propertyValues = beanDefinition.getPropertyValues().getPropertyValues();
            for (PropertyValue propertyValue : propertyValues) {
                Object value = propertyValue.getValue();
                if (value instanceof TypedStringValue) {
                    TypedStringValue typedStringValue = (TypedStringValue)value;
                    String placeHolder = typedStringValue.getValue();
                    BeanProperty beanProperty = new BeanProperty(propertyValue.getName(), placeHolder);
                    put2ListMap(beanNamePropertyListMap, curName, beanProperty);
                    put2ListMap(placeholderPropertyListMap, placeHolder, beanProperty);
                }
            }
        }
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
    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {

        doValueAnnotation(bean.getClass(), beanName);

        List<BeanProperty> beanPropertyList = beanNamePropertyListMap.get(beanName);
        if (beanPropertyList != null) {
            for (BeanProperty beanProperty : beanPropertyList) {
                put2ListMap(propertyBeanListMap, beanProperty, bean);
            }
        }
        return bean;
    }

    private void doValueAnnotation(Class beanClass, final String beanName) {
        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException {
                Value annotation = getAnnotation(field, Value.class);
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
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(NacosConfigReceiveEvent event) {
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
