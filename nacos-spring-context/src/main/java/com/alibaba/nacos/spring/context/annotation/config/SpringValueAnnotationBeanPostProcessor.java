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

import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.alibaba.nacos.spring.util.PlaceholderHelper;
import com.alibaba.spring.beans.factory.annotation.AbstractAnnotationBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * @author wuhaoqiang
 * @since 1.1.2
 * @desc: spring @Value Processor
 **/
public class SpringValueAnnotationBeanPostProcessor
        extends AbstractAnnotationBeanPostProcessor implements BeanFactoryAware,
        EnvironmentAware, ApplicationListener<NacosConfigReceivedEvent> {

    /**
     * The name of {@link SpringValueAnnotationBeanPostProcessor} bean.
     */
    public static final String BEAN_NAME = "StringValueAnnotationBeanPostProcessor";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * placeholder, valueTarget.
     */
    private Map<String, List<StringValueTarget>> placeholderStringValueTargetMap = new HashMap<String, List<StringValueTarget>>();

    private ConfigurableListableBeanFactory beanFactory;

    private Environment environment;

    private BeanExpressionResolver exprResolver;

    private BeanExpressionContext exprContext;

    public SpringValueAnnotationBeanPostProcessor() {
        super(Value.class);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "StringValueAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
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
        if (StringUtils.isEmpty(event)) {
            return;
        }
        Map<String, Object> updateProperties = NacosUtils.toProperties(event.getContent(), event.getType());

        for (Map.Entry<String, List<StringValueTarget>> entry : placeholderStringValueTargetMap
                .entrySet()) {

            String key = environment.resolvePlaceholders(entry.getKey());
            // Process modified keys, excluding deleted keys
            if (!updateProperties.containsKey(key)) {
                continue;
            }
            String newValue = environment.getProperty(key);

            if (newValue == null) {
                continue;
            }
            List<StringValueTarget> beanPropertyList = entry.getValue();
            for (StringValueTarget target : beanPropertyList) {
                String md5String = MD5Utils.md5Hex(newValue, "UTF-8");
                boolean isUpdate = !target.lastMD5.equals(md5String);
                if (isUpdate) {
                    target.updateLastMD5(md5String);
                    Object evaluatedValue = resolveStringValue(target.stringValueExpr);
                    if (target.method == null) {
                        setField(target, evaluatedValue);
                    } else {
                        setMethod(target, evaluatedValue);
                    }
                }
            }
        }
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
                        Value annotation = getAnnotation(field, Value.class);
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
                        Value annotation = getAnnotation(method, Value.class);
                        doWithAnnotation(beanName, bean, annotation,
                                method.getModifiers(), method, null);
                    }
                });
    }

    private void doWithAnnotation(String beanName, Object bean, Value annotation,
                                  int modifiers, Method method, Field field) {
        if (annotation != null) {
            if (Modifier.isStatic(modifiers)) {
                return;
            }

            Set<String> placeholderList = PlaceholderHelper.findPlaceholderKeys(annotation.value());
            for (String placeholder : placeholderList) {
                StringValueTarget stringValueTarget = new StringValueTarget(bean, beanName,
                        method, field, annotation.value());
                put2ListMap(placeholderStringValueTargetMap, placeholder,
                        stringValueTarget);
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

    private void setMethod(StringValueTarget stringValueTarget, Object propertyValue) {
        Method method = stringValueTarget.method;
        ReflectionUtils.makeAccessible(method);
        try {
            method.invoke(stringValueTarget.bean,
                    convertIfNecessary(method, propertyValue));

            if (logger.isDebugEnabled()) {
                logger.debug("Update value with {} (method) in {} (bean) with {}",
                        method.getName(), stringValueTarget.beanName, propertyValue);
            }
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can't update value with " + method.getName()
                        + " (method) in " + stringValueTarget.beanName + " (bean)", e);
            }
        }
    }

    private void setField(final StringValueTarget stringValueTarget,
                          final Object propertyValue) {
        final Object bean = stringValueTarget.bean;

        Field field = stringValueTarget.field;

        String fieldName = field.getName();

        try {
            ReflectionUtils.makeAccessible(field);
            field.set(bean, convertIfNecessary(field, propertyValue));

            if (logger.isDebugEnabled()) {
                logger.debug("Update value of the {}" + " (field) in {} (bean) with {}",
                        fieldName, stringValueTarget.beanName, propertyValue);
            }
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can't update value of the " + fieldName + " (field) in "
                        + stringValueTarget.beanName + " (bean)", e);
            }
        }
    }

    private static class StringValueTarget {

        private final Object bean;

        private final String beanName;

        private final Method method;

        private final Field field;

        private String lastMD5;

        private final String stringValueExpr;

        StringValueTarget(Object bean, String beanName, Method method, Field field, String stringValueExpr) {
            this.bean = bean;

            this.beanName = beanName;

            this.method = method;

            this.field = field;

            this.lastMD5 = "";

            this.stringValueExpr = stringValueExpr;
        }

        protected void updateLastMD5(String newMD5) {
            this.lastMD5 = newMD5;
        }

    }

}
