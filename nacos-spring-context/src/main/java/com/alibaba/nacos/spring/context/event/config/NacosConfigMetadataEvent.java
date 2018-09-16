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
package com.alibaba.nacos.spring.context.event.config;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Nacos Config Meta-Data {@link NacosConfigEvent event}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigMetadataEvent extends ApplicationEvent {

    private String dataId;

    private String groupId;

    private String beanName;

    private Object bean;

    private Class<?> beanType;

    private AnnotatedElement annotatedElement;

    private Resource xmlResource;

    private Properties nacosProperties;

    private Map<String, Object> nacosPropertiesAttributes;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source maybe {@link Annotation} or {@link Element XML element}
     * @see NacosConfigListener
     * @see NacosConfigurationProperties
     * @see NacosPropertySource
     * @see Element
     */
    public NacosConfigMetadataEvent(Object source) {
        super(source);
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public void setBeanType(Class<?> beanType) {
        this.beanType = beanType;
    }

    /**
     * {@link AnnotatedElement} maybe {@link Class}, {@link Method}
     *
     * @return maybe <code>null</code> if source from XML configuration
     * @see NacosConfigListener
     * @see NacosConfigurationProperties
     * @see NacosPropertySource
     */
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    /**
     * {@link Resource} for XML configuration
     *
     * @return maybe <code>null</code> if Annotated by somewhere
     */
    public Resource getXmlResource() {
        return xmlResource;
    }

    public void setXmlResource(Resource xmlResource) {
        this.xmlResource = xmlResource;
    }

    /**
     * Actual effective Nacos {@link Properties}
     *
     * @return non-null
     */
    public Properties getNacosProperties() {
        return nacosProperties;
    }

    public void setNacosProperties(Properties nacosProperties) {
        this.nacosProperties = nacosProperties;
    }

    /**
     * Nacos {@link Properties}'s attributes that may come frome {@link Annotation} or {@link Element XML element}
     *
     * @return non-null
     */
    public Map<String, Object> getNacosPropertiesAttributes() {
        return nacosPropertiesAttributes;
    }

    public void setNacosPropertiesAttributes(Map<String, Object> nacosPropertiesAttributes) {
        this.nacosPropertiesAttributes = nacosPropertiesAttributes;
    }
}
