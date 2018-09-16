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
package com.alibaba.nacos.spring.context.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static com.alibaba.nacos.spring.util.NacosUtils.resolveGenericType;
import static java.lang.reflect.Modifier.*;


/**
 * Listener {@link Method method} Processor
 * <p>
 * The target {@link Method method} must be
 * <ul>
 * <li><code>public</code></li>
 * <li>not <code>static</code></li>
 * <li>not <code>abstract</code></li>
 * <li>not <code>native</code></li>
 * <li><code>void</code></li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Method
 * @since 0.1.0
 */
public abstract class AnnotationListenerMethodProcessor<A extends Annotation> implements ApplicationListener<ContextRefreshedEvent> {

    protected final Log logger = LogFactory.getLog(getClass());
    private final Class<A> annotationType;

    public AnnotationListenerMethodProcessor() {
        this.annotationType = resolveGenericType(getClass());
    }

    /**
     * Must be
     * <ul>
     * <li><code>public</code></li>
     * <li>not <code>static</code></li>
     * <li>not <code>abstract</code></li>
     * <li>not <code>native</code></li>
     * <li><code>void</code></li>
     * </ul>
     *
     * @param method {@link Method}
     * @return if obey above rules , return <code>true</code>
     */
    static boolean isListenerMethod(Method method) {

        int modifiers = method.getModifiers();

        Class<?> returnType = method.getReturnType();

        return isPublic(modifiers)
                && !isStatic(modifiers)
                && !isNative(modifiers)
                && !isAbstract(modifiers)
                && void.class.equals(returnType)
                ;
    }

    @Override
    public final void onApplicationEvent(ContextRefreshedEvent event) {
        // Retrieve ApplicationContext from ContextRefreshedEvent
        ApplicationContext applicationContext = event.getApplicationContext();
        // Select those methods from all beans that annotated
        processBeans(applicationContext);
    }

    private void processBeans(ApplicationContext applicationContext) {

        Map<String, Object> beansMap = applicationContext.getBeansOfType(Object.class);

        processBeans(beansMap, applicationContext);

    }

    private void processBeans(Map<String, Object> beansMap, ApplicationContext applicationContext) {

        for (Map.Entry<String, Object> entry : beansMap.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            // Bean type
            if (bean != null) {
                Class<?> beanClass = AopUtils.getTargetClass(bean);
                processBean(beanName, bean, beanClass, applicationContext);
            }
        }

    }

    /**
     * Select those methods from bean that annotated
     *
     * @param beanName           Bean name
     * @param bean               Bean object
     * @param beanClass          the {@link Class} of Bean
     * @param applicationContext
     */
    private void processBean(final String beanName, final Object bean, final Class<?> beanClass, final ApplicationContext applicationContext) {

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                A annotation = AnnotationUtils.getAnnotation(method, annotationType);
                if (annotation != null && isCandidateMethod(bean, beanClass, annotation, method, applicationContext)) {
                    processListenerMethod(beanName, bean, beanClass, annotation, method, applicationContext);
                }
            }

        }, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                return isListenerMethod(method);
            }
        });

    }

    /**
     * Process Listener Method when {@link #isCandidateMethod(Object, Class, Annotation, Method, ApplicationContext)} returns <code>true</code>
     *
     * @param beanName           Bean name
     * @param bean               Bean object
     * @param beanClass          Bean Class
     * @param annotation         Annotation object
     * @param method             Method
     * @param applicationContext ApplicationContext
     */
    protected abstract void processListenerMethod(String beanName, Object bean, Class<?> beanClass, A annotation, Method method,
                                                  ApplicationContext applicationContext);

    /**
     * Subclass could override this method to determine current method is candidate or not
     *
     * @param bean               Bean object
     * @param beanClass          Bean Class
     * @param annotation         Annotation object
     * @param method             Method
     * @param applicationContext ApplicationContext
     * @return <code>true</code> as default
     */
    protected boolean isCandidateMethod(Object bean, Class<?> beanClass, A annotation, Method method,
                                        ApplicationContext applicationContext) {
        return true;
    }
}
