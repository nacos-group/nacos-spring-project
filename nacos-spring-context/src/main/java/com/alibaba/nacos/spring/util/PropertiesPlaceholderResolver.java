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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;

/**
 * Placeholder Resolver for {@link Properties properties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class PropertiesPlaceholderResolver {

    private final PropertyResolver propertyResolver;

    public PropertiesPlaceholderResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    /**
     * Resolve placeholders in specified {@link Annotation annotation}
     *
     * @param annotation {@link Annotation annotation}
     * @return Resolved {@link Properties source properties}
     */
    public Properties resolve(Annotation annotation) {
        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
        return resolve(attributes);
    }

    /**
     * Resolve placeholders in specified {@link Map properties}
     *
     * @param properties {@link Map source properties}
     * @return Resolved {@link Properties source properties}
     */
    public Properties resolve(Map<?, ?> properties) {
        Properties resolvedProperties = new Properties();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if (entry.getValue() instanceof CharSequence) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                String resolvedValue = propertyResolver.resolvePlaceholders(value);
                if (StringUtils.hasText(resolvedValue)) { // set properties if has test
                    resolvedProperties.setProperty(key, resolvedValue);
                }
            }
        }
        return resolvedProperties;
    }

}
