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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.properties.NacosConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * Nacos Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class NacosUtils {

    /**
     * Default value of {@link String} attribute for {@link Annotation}
     */
    public static final String DEFAULT_STRING_ATTRIBUTE_VALUE = "";

    /**
     * The separator
     */
    public static final String SEPARATOR = "|";

    /**
     * Default timeout for getting Nacos configuration
     */
    public static final long DEFAULT_TIMEOUT = Long.getLong("nacos.default.timeout", 5000L);

    private static final Logger logger = LoggerFactory.getLogger(NacosUtils.class);

    /**
     * Build The default name of {@link NacosConfigurationProperties @NacosPropertySource}
     *
     * @param dataId     data Id
     * @param groupId    group Id
     * @param properties Nacos Properties
     * @return non-null
     */
    public static String buildDefaultPropertySourceName(String dataId, String groupId, Map<String, Object> properties) {
        return build(dataId, groupId, identify(properties));
    }

    /**
     * Generate Id of {@link  NacosProperties Nacos Properties annotation}
     *
     * @param nacosProperties {@link  NacosProperties Nacos Properties annotation}
     * @return Id
     */
    public static String identify(NacosProperties nacosProperties) {
        return identify(getAnnotationAttributes(nacosProperties));
    }

    /**
     * Generate Id of {@link  NacosProperties Nacos Properties}
     *
     * @param properties {@link  Properties Nacos Properties}
     * @return Id
     */

    public static String identify(Map<?, ?> properties) {

        String namespace = (String) properties.get(NAMESPACE);
        String serverAddress = (String) properties.get(SERVER_ADDR);
        String contextPath = (String) properties.get(CONTEXT_PATH);
        String clusterName = (String) properties.get(CLUSTER_NAME);
        String endpoint = (String) properties.get(ENDPOINT);

        return build(namespace, clusterName, serverAddress, contextPath, endpoint);

    }

    private static String build(Object... values) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Object value : values) {

            String stringValue = value == null ? null : String.valueOf(value);
            if (StringUtils.hasText(stringValue)) {
                stringBuilder.append(stringValue).append(SEPARATOR);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Is {@link NacosProperties @NacosProperties} with default attribute values.
     *
     * @param nacosProperties {@link NacosProperties @NacosProperties}
     * @return If default values , return <code>true</code>,or <code>false</code>
     */
    public static boolean isDefault(final NacosProperties nacosProperties) {

        final List<Object> records = new LinkedList<Object>();

        ReflectionUtils.doWithMethods(nacosProperties.annotationType(), new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0) {
                    Object defaultValue = method.getDefaultValue();
                    if (defaultValue != null) {
                        try {
                            Object returnValue = method.invoke(nacosProperties);
                            if (!defaultValue.equals(returnValue)) {
                                records.add(returnValue);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        });

        return records.isEmpty();
    }


    public static Properties resolveProperties(NacosProperties nacosProperties, PropertyResolver propertyResolver,
                                               Properties defaultProperties) {

        Properties Properties = defaultProperties;

        if (isDefault(nacosProperties)) {

            return Properties;

        } else {

            Map<String, Object> attributes = getAnnotationAttributes(nacosProperties);

            Properties = resolveProperties(attributes, propertyResolver);

        }

        return Properties;

    }


    /**
     * Resolve {@link Properties} to be a new one
     *
     * @param Properties       THe properties
     * @param propertyResolver {@link PropertyResolver} instance, for instance, {@link Environment}
     * @return a new instance of {@link Properties} after resolving.
     */
    public static Properties resolveProperties(Map<String, Object> Properties, PropertyResolver propertyResolver) {

        Properties properties = new Properties();

        for (Map.Entry<String, Object> entry : Properties.entrySet()) {
            if (entry.getValue() instanceof CharSequence) {
                String key = entry.getKey();
                String value = String.valueOf(entry.getValue());
                String resolvedValue = propertyResolver.resolvePlaceholders(value);
                if (StringUtils.hasText(resolvedValue)) { // set properties if has test
                    properties.setProperty(key, resolvedValue);
                }
            }
        }

        return properties;
    }

    /**
     * Get content from {@link ConfigService} via dataId and groupId
     *
     * @param configService {@link ConfigService}
     * @param dataId        dataId
     * @param groupId       groupId
     * @return If available , return content , or <code>null</code>
     */
    public static String getContent(ConfigService configService, String dataId, String groupId) {
        String content = null;
        try {
            content = configService.getConfig(dataId, groupId, DEFAULT_TIMEOUT);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can't get content from dataId : " + dataId + " , groupId : " + groupId, e);
            }
        }
        return content;
    }

    public static <T> Class<T> resolveGenericType(Class<?> declaredClass) {
        ParameterizedType parameterizedType = (ParameterizedType) declaredClass.getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];
    }

    public static Properties toProperties(String text) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }
        return properties;
    }


}
