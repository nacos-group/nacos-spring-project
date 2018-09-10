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

import com.alibaba.nacos.api.annotation.NacosConfigListener;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.convert.NacosConfigConverter;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.event.AnnotationListenerMethodProcessor;
import com.alibaba.nacos.spring.convert.converter.DefaultNacosConfigConverter;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NonBlockingNacosConfigListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource.CONFIG;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigListenerExecutor;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosServiceFactoryBean;
import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;
import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * {@link NacosConfigListener @NacosConfigListener} {@link Method method} Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosConfigListener
 * @see AnnotationListenerMethodProcessor
 * @see Method
 * @since 0.1.0
 */
public class NacosConfigListenerMethodProcessor extends AnnotationListenerMethodProcessor<NacosConfigListener>
        implements ApplicationContextAware {

    /**
     * The bean name of {@link NacosConfigListenerMethodProcessor}
     */
    public static final String BEAN_NAME = "nacosConfigListenerMethodProcessor";

    /**
     * The bean name of {@link ConversionService} for Nacos Configuration
     */
    public static final String NACOS_CONFIG_CONVERSION_SERVICE_BEAN_NAME = "nacosConfigConversionService";

    private Properties globalNacosProperties;

    private NacosServiceFactory nacosServiceFactory;

    private ConversionService conversionService;

    private ExecutorService nacosConfigListenerExecutor;

    @Override
    protected void processListenerMethod(final Object bean, Class<?> beanClass, final NacosConfigListener listener,
                                         final Method method, ApplicationContext applicationContext) {

        String dataId = listener.dataId();

        String groupId = listener.groupId();

        long timeout = listener.timeout();

        ConfigService configService = resolveConfigService(listener, applicationContext);

        try {

            configService.addListener(dataId, groupId, new NonBlockingNacosConfigListener(dataId, groupId,
                    nacosConfigListenerExecutor, timeout) {
                @Override
                protected void onUpdate(String config) {

                    Class<?> targetType = method.getParameterTypes()[0];

                    NacosConfigConverter configConverter = determineNacosConfigConverter(targetType, listener);

                    Object parameterValue = configConverter.convert(config);

                    // Execute target method
                    ReflectionUtils.invokeMethod(method, bean, parameterValue);

                }
            });

        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error("ConfigService can't add Listener for dataId : " + dataId + " , groupId : " + groupId, e);
            }
        }

    }

    private ConfigService resolveConfigService(NacosConfigListener listener, ApplicationContext applicationContext)
            throws BeansException {

        NacosProperties nacosProperties = listener.properties();

        Properties properties = resolveProperties(nacosProperties, applicationContext.getEnvironment(), globalNacosProperties);

        ConfigService configService = null;

        try {
            configService = nacosServiceFactory.createConfigService(properties);
        } catch (NacosException e) {
            throw new BeanCreationException(e.getErrMsg(), e);
        }

        return configService;
    }

    @Override
    protected boolean isCandidateMethod(Object bean, Class<?> beanClass, NacosConfigListener listener, Method method,
                                        ApplicationContext applicationContext) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1) { // Only one argument on method
            if (logger.isWarnEnabled()) {
                logger.warn("Listener method [" + method + "] parameters' count must be one !");
            }
            return false;
        }

        Class<?> targetType = parameterTypes[0];

        NacosConfigConverter configConverter = determineNacosConfigConverter(targetType, listener);

        if (!configConverter.canConvert(targetType)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Listener method [" + method + "] is not a candidate , thus its parameter type ["
                        + targetType + "] can't be converted , please check NacosConfigConverter implementation : "
                        + configConverter.getClass().getName());
            }
        }

        return true;
    }

    private NacosConfigConverter determineNacosConfigConverter(Class<?> targetType, NacosConfigListener listener) {

        Class<?> converterClass = listener.converter();

        NacosConfigConverter configConverter = null;

        if (NacosConfigConverter.class.equals(converterClass)) { // Use default implementation

            configConverter = new DefaultNacosConfigConverter(targetType, conversionService);

        } else { // Use customized implementation

            configConverter = (NacosConfigConverter) instantiateClass(converterClass);

        }

        return configConverter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        globalNacosProperties = CONFIG.getMergedGlobalProperties(applicationContext);
        nacosServiceFactory = getNacosServiceFactoryBean(applicationContext);
        conversionService = determineConversionService(applicationContext);
        nacosConfigListenerExecutor = getNacosConfigListenerExecutor(applicationContext);
    }

    private ConversionService determineConversionService(ApplicationContext applicationContext) {

        String beanName = NACOS_CONFIG_CONVERSION_SERVICE_BEAN_NAME;

        ConversionService conversionService = applicationContext.containsBean(beanName) ?
                applicationContext.getBean(beanName, ConversionService.class) : null;

        if (conversionService == null) {
            conversionService = new DefaultFormattingConversionService();
        }

        return conversionService;
    }

}
