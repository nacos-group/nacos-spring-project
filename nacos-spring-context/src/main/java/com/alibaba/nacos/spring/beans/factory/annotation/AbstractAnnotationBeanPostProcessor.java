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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.spring.util.AnnotationUtils.getAnnotationAttributes;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

/**
 * Abstract common {@link BeanPostProcessor} implementation for customized annotation that annotated injected-object.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.3
 */
@SuppressWarnings("unchecked")
public abstract class AbstractAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered,
        BeanFactoryAware, BeanClassLoaderAware, EnvironmentAware, DisposableBean {

    private final static int CACHE_SIZE = Integer.getInteger("", 32);

    private final Log logger = LogFactory.getLog(getClass());

    private final Class<? extends Annotation>[] annotationTypes;

    private final ConcurrentMap<String, AnnotatedInjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<String, AnnotatedInjectionMetadata>(CACHE_SIZE);

    private final ConcurrentMap<String, Object> injectedObjectsCache = new ConcurrentHashMap<String, Object>(CACHE_SIZE);

    private ConfigurableListableBeanFactory beanFactory;

    private Environment environment;

    private ClassLoader classLoader;

    /**
     * make sure higher priority than {@link AutowiredAnnotationBeanPostProcessor}
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 3;

    /**
     * whether to turn Class references into Strings (for
     * compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     * preserve them as Class references
     *
     * @since 1.0.11
     */
    private boolean classValuesAsString = true;

    /**
     * whether to turn nested Annotation instances into
     * {@link AnnotationAttributes} maps (for compatibility with
     * {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     * Annotation instances
     *
     * @since 1.0.11
     */
    private boolean nestedAnnotationsAsMap = true;

    /**
     * whether ignore default value or not
     *
     * @since 1.0.11
     */
    private boolean ignoreDefaultValue = true;

    /**
     * whether try merged annotation or not
     *
     * @since 1.0.11
     */
    private boolean tryMergedAnnotation = true;

    /**
     * @param annotationTypes the multiple types of {@link Annotation annotations}
     */
    public AbstractAnnotationBeanPostProcessor(Class<? extends Annotation>... annotationTypes) {
        Assert.notEmpty(annotationTypes, "The argument of annotations' types must not empty");
        this.annotationTypes = annotationTypes;
    }

    private static <T> Collection<T> combine(Collection<? extends T>... elements) {
        List<T> allElements = new ArrayList<T>();
        for (Collection<? extends T> e : elements) {
            allElements.addAll(e);
        }
        return allElements;
    }

    /**
     * Annotation type
     *
     * @return non-null
     * @deprecated 2.7.3, uses {@link #getAnnotationTypes()}
     */
    @Deprecated
    public final Class<? extends Annotation> getAnnotationType() {
        return annotationTypes[0];
    }

    protected final Class<? extends Annotation>[] getAnnotationTypes() {
        return annotationTypes;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory,
                "AnnotationInjectedBeanPostProcessor requires a ConfigurableListableBeanFactory");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeanCreationException {

        InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @" + getAnnotationType().getSimpleName()
                    + " dependencies is failed", ex);
        }
        return pvs;
    }


    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated fields
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedFieldElement> findFieldAnnotationMetadata(final Class<?> beanClass) {

        final List<AnnotatedFieldElement> elements = new LinkedList<AnnotatedFieldElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {

                    AnnotationAttributes attributes = doGetAnnotationAttributes(field, annotationType);

                    if (attributes != null) {

                        if (Modifier.isStatic(field.getModifiers())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " is not supported on static fields: " + field);
                            }
                            return;
                        }

                        elements.add(new AnnotatedFieldElement(field, attributes));
                    }
                }
            }
        });

        return elements;

    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated methods
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedMethodElement> findAnnotatedMethodMetadata(final Class<?> beanClass) {

        final List<AnnotatedMethodElement> elements = new LinkedList<AnnotatedMethodElement>();

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

                Method bridgedMethod = findBridgedMethod(method);

                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }


                for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {

                    AnnotationAttributes attributes = doGetAnnotationAttributes(bridgedMethod, annotationType);

                    if (attributes != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                        if (Modifier.isStatic(method.getModifiers())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " annotation is not supported on static methods: " + method);
                            }
                            return;
                        }
                        if (method.getParameterTypes().length == 0) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " annotation should only be used on methods with parameters: " +
                                        method);
                            }
                        }
                        PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                        elements.add(new AnnotatedMethodElement(method, pd, attributes));
                    }
                }
            }
        });

        return elements;
    }

    /**
     * Get {@link AnnotationAttributes}
     *
     * @param annotatedElement {@link AnnotatedElement the annotated element}
     * @param annotationType   the {@link Class tyoe} pf {@link Annotation annotation}
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     * @since 1.0.11
     */
    protected AnnotationAttributes doGetAnnotationAttributes(AnnotatedElement annotatedElement,
                                                             Class<? extends Annotation> annotationType) {
        return getAnnotationAttributes(annotatedElement, annotationType, getEnvironment(),
                classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, tryMergedAnnotation);
    }

    private AnnotatedInjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
        Collection<AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
        Collection<AnnotatedMethodElement> methodElements = findAnnotatedMethodMetadata(beanClass);
        return new AnnotatedInjectionMetadata(beanClass, fieldElements, methodElements);
    }

    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        AnnotatedInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildAnnotatedMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect object class [" + clazz.getName() +
                                "] for annotation metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findInjectionMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void destroy() throws Exception {

        for (Object object : injectedObjectsCache.values()) {
            if (logger.isInfoEnabled()) {
                logger.info(object + " was destroying!");
            }

            if (object instanceof DisposableBean) {
                ((DisposableBean) object).destroy();
            }
        }

        injectionMetadataCache.clear();
        injectedObjectsCache.clear();

        if (logger.isInfoEnabled()) {
            logger.info(getClass() + " was destroying!");
        }

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    protected ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Gets all injected-objects.
     *
     * @return non-null {@link Collection}
     */
    protected Collection<Object> getInjectedObjects() {
        return this.injectedObjectsCache.values();
    }

    /**
     * Get injected-object from specified {@link AnnotationAttributes annotation attributes} and Bean Class
     *
     * @param attributes      {@link AnnotationAttributes the annotation attributes}
     * @param bean            Current bean that will be injected
     * @param beanName        Current bean name that will be injected
     * @param injectedType    the type of injected-object
     * @param injectedElement {@link InjectionMetadata.InjectedElement}
     * @return An injected object
     * @throws Exception If getting is failed
     */
    protected Object getInjectedObject(AnnotationAttributes attributes, Object bean, String beanName, Class<?> injectedType,
                                       InjectionMetadata.InjectedElement injectedElement) throws Exception {

        String cacheKey = buildInjectedObjectCacheKey(attributes, bean, beanName, injectedType, injectedElement);

        Object injectedObject = injectedObjectsCache.get(cacheKey);

        if (injectedObject == null) {
            injectedObject = doGetInjectedBean(attributes, bean, beanName, injectedType, injectedElement);
            // Customized inject-object if necessary
            injectedObjectsCache.putIfAbsent(cacheKey, injectedObject);
        }

        return injectedObject;

    }

    /**
     * Subclass must implement this method to get injected-object. The context objects could help this method if
     * necessary :
     * <ul>
     * <li>{@link #getBeanFactory() BeanFactory}</li>
     * <li>{@link #getClassLoader() ClassLoader}</li>
     * <li>{@link #getEnvironment() Environment}</li>
     * </ul>
     *
     * @param attributes      {@link AnnotationAttributes the annotation attributes}
     * @param bean            Current bean that will be injected
     * @param beanName        Current bean name that will be injected
     * @param injectedType    the type of injected-object
     * @param injectedElement {@link InjectionMetadata.InjectedElement}
     * @return The injected object
     * @throws Exception If resolving an injected object is failed.
     */
    protected abstract Object doGetInjectedBean(AnnotationAttributes attributes, Object bean, String beanName, Class<?> injectedType,
                                                InjectionMetadata.InjectedElement injectedElement) throws Exception;

    /**
     * Build a cache key for injected-object. The context objects could help this method if
     * necessary :
     * <ul>
     * <li>{@link #getBeanFactory() BeanFactory}</li>
     * <li>{@link #getClassLoader() ClassLoader}</li>
     * <li>{@link #getEnvironment() Environment}</li>
     * </ul>
     *
     * @param attributes      {@link AnnotationAttributes the annotation attributes}
     * @param bean            Current bean that will be injected
     * @param beanName        Current bean name that will be injected
     * @param injectedType    the type of injected-object
     * @param injectedElement {@link InjectionMetadata.InjectedElement}
     * @return Bean cache key
     */
    protected abstract String buildInjectedObjectCacheKey(AnnotationAttributes attributes, Object bean, String beanName,
                                                          Class<?> injectedType,
                                                          InjectionMetadata.InjectedElement injectedElement);

    /**
     * Get {@link Map} in injected field.
     *
     * @return non-null ready-only {@link Map}
     */
    protected Map<InjectionMetadata.InjectedElement, Object> getInjectedFieldObjectsMap() {

        Map<InjectionMetadata.InjectedElement, Object> injectedElementBeanMap =
                new LinkedHashMap<InjectionMetadata.InjectedElement, Object>();

        for (AnnotatedInjectionMetadata metadata : injectionMetadataCache.values()) {

            Collection<AnnotatedFieldElement> fieldElements = metadata.getFieldElements();

            for (AnnotatedFieldElement fieldElement : fieldElements) {

                injectedElementBeanMap.put(fieldElement, fieldElement.bean);

            }

        }

        return unmodifiableMap(injectedElementBeanMap);

    }

    /**
     * Get {@link Map} in injected method.
     *
     * @return non-null {@link Map}
     */
    protected Map<InjectionMetadata.InjectedElement, Object> getInjectedMethodObjectsMap() {

        Map<InjectionMetadata.InjectedElement, Object> injectedElementBeanMap =
                new LinkedHashMap<InjectionMetadata.InjectedElement, Object>();

        for (AnnotatedInjectionMetadata metadata : injectionMetadataCache.values()) {

            Collection<AnnotatedMethodElement> methodElements = metadata.getMethodElements();

            for (AnnotatedMethodElement methodElement : methodElements) {

                injectedElementBeanMap.put(methodElement, methodElement.object);

            }

        }

        return unmodifiableMap(injectedElementBeanMap);

    }

    /**
     * @param classValuesAsString whether to turn Class references into Strings (for
     *                            compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                            preserve them as Class references
     * @since 1.0.11
     */
    public void setClassValuesAsString(boolean classValuesAsString) {
        this.classValuesAsString = classValuesAsString;
    }

    /**
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @since 1.0.11
     */
    public void setNestedAnnotationsAsMap(boolean nestedAnnotationsAsMap) {
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    /**
     * @param ignoreDefaultValue whether ignore default value or not
     * @since 1.0.11
     */
    public void setIgnoreDefaultValue(boolean ignoreDefaultValue) {
        this.ignoreDefaultValue = ignoreDefaultValue;
    }

    /**
     * @param tryMergedAnnotation whether try merged annotation or not
     * @since 1.0.11
     */
    public void setTryMergedAnnotation(boolean tryMergedAnnotation) {
        this.tryMergedAnnotation = tryMergedAnnotation;
    }

    /**
     * {@link Annotation Annotated} {@link InjectionMetadata} implementation
     */
    private class AnnotatedInjectionMetadata extends InjectionMetadata {

        private final Collection<AnnotatedFieldElement> fieldElements;

        private final Collection<AnnotatedMethodElement> methodElements;

        public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<AnnotatedFieldElement> fieldElements,
                                          Collection<AnnotatedMethodElement> methodElements) {
            super(targetClass, combine(fieldElements, methodElements));
            this.fieldElements = fieldElements;
            this.methodElements = methodElements;
        }

        public Collection<AnnotatedFieldElement> getFieldElements() {
            return fieldElements;
        }

        public Collection<AnnotatedMethodElement> getMethodElements() {
            return methodElements;
        }
    }

    /**
     * {@link Annotation Annotated} {@link Method} {@link InjectionMetadata.InjectedElement}
     */
    private class AnnotatedMethodElement extends InjectionMetadata.InjectedElement {

        private final Method method;

        private final AnnotationAttributes attributes;

        private volatile Object object;

        protected AnnotatedMethodElement(Method method, PropertyDescriptor pd, AnnotationAttributes attributes) {
            super(method, pd);
            this.method = method;
            this.attributes = attributes;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = pd.getPropertyType();

            Object injectedObject = getInjectedObject(attributes, bean, beanName, injectedType, this);

            ReflectionUtils.makeAccessible(method);

            method.invoke(bean, injectedObject);

        }

    }

    /**
     * {@link Annotation Annotated} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        private final AnnotationAttributes attributes;

        private volatile Object bean;

        protected AnnotatedFieldElement(Field field, AnnotationAttributes attributes) {
            super(field, null);
            this.field = field;
            this.attributes = attributes;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = resolveInjectedType(bean, field);

            Object injectedObject = getInjectedObject(attributes, bean, beanName, injectedType, this);

            ReflectionUtils.makeAccessible(field);

            field.set(bean, injectedObject);

        }

        private Class<?> resolveInjectedType(Object bean, Field field) {
            Type genericType = field.getGenericType();
            if (genericType instanceof Class) { // Just a normal Class
                return field.getType();
            } else { // GenericType
                return resolveTypeArgument(getTargetClass(bean), field.getDeclaringClass());
            }
        }
    }
}