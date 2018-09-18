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
package com.alibaba.nacos.spring.core.env;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.util.*;

import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.*;

/**
 * Annotation {@link NacosPropertySource @NacosPropertySource} {@link AbstractNacosPropertySourceBuilder Builder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @see NacosPropertySources
 * @see AnnotatedBeanDefinition
 * @since 0.1.0
 */
public class AnnotationNacosPropertySourceBuilder extends AbstractNacosPropertySourceBuilder<AnnotatedBeanDefinition> {

    /**
     * The bean name of {@link AnnotationNacosPropertySourceBuilder}
     */
    public static final String BEAN_NAME = "annotationNacosPropertySourceBuilder";

    @Override
    protected Map<String, Object>[] resolveRuntimeAttributesArray(AnnotatedBeanDefinition beanDefinition, Properties globalNacosProperties) {
        // Get AnnotationMetadata
        AnnotationMetadata metadata = beanDefinition.getMetadata();

        Set<String> annotationTypes = metadata.getAnnotationTypes();

        List<Map<String, Object>> annotationAttributesList = new LinkedList<Map<String, Object>>();

        for (String annotationType : annotationTypes) {
            annotationAttributesList.addAll(getAnnotationAttributesList(metadata, annotationType));
        }

        return annotationAttributesList.toArray(new Map[0]);
    }

    private List<Map<String, Object>> getAnnotationAttributesList(AnnotationMetadata metadata, String annotationType) {

        List<Map<String, Object>> annotationAttributesList = new LinkedList<Map<String, Object>>();

        if (NacosPropertySources.class.getName().equals(annotationType)) {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationType);
            if (annotationAttributes != null) {
                annotationAttributesList.addAll(Arrays.asList((Map<String, Object>[]) annotationAttributes.get("value")));
            }
        } else if (com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.class.getName().equals(annotationType)) {
            annotationAttributesList.add(metadata.getAnnotationAttributes(annotationType));
        }
        return annotationAttributesList;
    }

    @Override
    protected void initNacosPropertySource(NacosPropertySource nacosPropertySource, AnnotatedBeanDefinition beanDefinition,
                                           Map<String, Object> annotationAttributes) {
        // AttributesMetadata
        initAttributesMetadata(nacosPropertySource, annotationAttributes);
        // Auto-Refreshed
        initAutoRefreshed(nacosPropertySource, annotationAttributes);
        // Origin
        initOrigin(nacosPropertySource, beanDefinition);
        // Order
        initOrder(nacosPropertySource, annotationAttributes);

    }

    private void initAttributesMetadata(NacosPropertySource nacosPropertySource, Map<String, Object> annotationAttributes) {
        nacosPropertySource.setAttributesMetadata(annotationAttributes);
    }

    private void initAutoRefreshed(NacosPropertySource nacosPropertySource, Map<String, Object> annotationAttributes) {
        boolean autoRefreshed = Boolean.TRUE.equals(annotationAttributes.get(AUTO_REFRESHED_ATTRIBUTE_NAME));
        nacosPropertySource.setAutoRefreshed(autoRefreshed);
    }

    private void initOrigin(NacosPropertySource nacosPropertySource, AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        nacosPropertySource.setOrigin(metadata.getClassName());
    }

    private void initOrder(NacosPropertySource nacosPropertySource, Map<String, Object> annotationAttributes) {
        boolean first = Boolean.TRUE.equals(annotationAttributes.get(FIRST_ATTRIBUTE_NAME));
        String before = (String) annotationAttributes.get(BEFORE_ATTRIBUTE_NAME);
        String after = (String) annotationAttributes.get(AFTER_ATTRIBUTE_NAME);
        nacosPropertySource.setFirst(first);
        nacosPropertySource.setBefore(before);
        nacosPropertySource.setAfter(after);
    }

    @Override
    protected NacosConfigMetadataEvent createMetaEvent(NacosPropertySource nacosPropertySource,
                                                       AnnotatedBeanDefinition beanDefinition) {
        return new NacosConfigMetadataEvent(beanDefinition.getMetadata());
    }

    @Override
    protected void doInitMetadataEvent(NacosPropertySource nacosPropertySource, AnnotatedBeanDefinition beanDefinition,
                                       NacosConfigMetadataEvent metadataEvent) {
        metadataEvent.setAnnotatedElement(metadataEvent.getAnnotatedElement());
    }

}
