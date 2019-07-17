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

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.context.config.xml.NacosPropertySourceXmlBeanDefinition;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.*;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_CONFIG_TYPE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;

/**
 * XML {@link NacosPropertySource} {@link AbstractNacosPropertySourceBuilder Builder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class XmlNacosPropertySourceBuilder extends
        AbstractNacosPropertySourceBuilder<NacosPropertySourceXmlBeanDefinition> {

    /**
     * The bean name of {@link XmlNacosPropertySourceBuilder}
     */
    public static final String BEAN_NAME = "xmlNacosPropertySourceBuilder";

    @Override
    protected Map<String, Object>[] resolveRuntimeAttributesArray(NacosPropertySourceXmlBeanDefinition beanDefinition,
                                                                  Properties globalNacosProperties) {
        Element element = beanDefinition.getElement();
        Map<String, Object> runtimeAttributes = new HashMap<String, Object>(4);
        // Nacos Metadata
        runtimeAttributes.put(DATA_ID_ATTRIBUTE_NAME, getAttribute(element, "data-id", DEFAULT_STRING_ATTRIBUTE_VALUE));
        runtimeAttributes.put(GROUP_ID_ATTRIBUTE_NAME, getAttribute(element, "group-id", DEFAULT_GROUP));
        // PropertySource Name
        runtimeAttributes.put(NAME_ATTRIBUTE_NAME, getAttribute(element, NAME_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE));
        // Config type
        String type = getAttribute(element, CONFIG_TYPE_ATTRIBUTE_NAME, DEFAULT_CONFIG_TYPE_VALUE);

        try {
            runtimeAttributes.put(CONFIG_TYPE_ATTRIBUTE_NAME, ConfigType.valueOf(type.toUpperCase()));
            // TODO support nested properties
            runtimeAttributes.put(PROPERTIES_ATTRIBUTE_NAME, new Properties());
            return new Map[]{runtimeAttributes};
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Now the config type just support [properties, json, yaml, xml, text, html]");
        }

    }

    @Override
    protected void initNacosPropertySource(NacosPropertySource nacosPropertySource,
                                           NacosPropertySourceXmlBeanDefinition beanDefinition, Map<String, Object> attributes) {
        Element element = beanDefinition.getElement();

        // Attributes Metadata
        initAttributesMetadata(nacosPropertySource, element);
        // Origin
        initOrigin(nacosPropertySource, beanDefinition.getXmlReaderContext());
        // Auto-refreshed
        initAutoRefreshed(nacosPropertySource, element);
        // PropertySource Order
        initOrder(nacosPropertySource, element);
    }

    private void initOrigin(NacosPropertySource nacosPropertySource, XmlReaderContext xmlReaderContext) {
        // Resource
        Resource resource = xmlReaderContext.getResource();

        nacosPropertySource.setOrigin(resource);
    }

    private void initAutoRefreshed(NacosPropertySource nacosPropertySource, Element element) {
        boolean autoRefreshed = getAttribute(element, "auto-refreshed", DEFAULT_BOOLEAN_ATTRIBUTE_VALUE);
        nacosPropertySource.setAutoRefreshed(autoRefreshed);
    }

    private void initAttributesMetadata(NacosPropertySource nacosPropertySource, Element element) {
        NamedNodeMap elementAttributes = element.getAttributes();
        int length = elementAttributes.getLength();
    }

    private void initOrder(NacosPropertySource nacosPropertySource, Element element) {
        // Order
        boolean first = getAttribute(element, FIRST_ATTRIBUTE_NAME, DEFAULT_BOOLEAN_ATTRIBUTE_VALUE);
        String before = getAttribute(element, BEFORE_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE);
        String after = getAttribute(element, AFTER_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE);
        nacosPropertySource.setFirst(first);
        nacosPropertySource.setBefore(before);
        nacosPropertySource.setAfter(after);
    }


    private <T> T getAttribute(Element element, String name, T defaultValue) {
        ConversionService conversionService = environment.getConversionService();
        String value = element.getAttribute(name);
        String resolvedValue = environment.resolvePlaceholders(value);
        T attributeValue = StringUtils.hasText(resolvedValue) ?
                (T) conversionService.convert(resolvedValue, defaultValue.getClass()) :
                defaultValue;
        return attributeValue;
    }


    @Override
    protected NacosConfigMetadataEvent createMetaEvent(NacosPropertySource nacosPropertySource,
                                                       NacosPropertySourceXmlBeanDefinition beanDefinition) {
        return new NacosConfigMetadataEvent(beanDefinition.getElement());
    }

    @Override
    protected void doInitMetadataEvent(NacosPropertySource nacosPropertySource,
                                       NacosPropertySourceXmlBeanDefinition beanDefinition, NacosConfigMetadataEvent metadataEvent) {
        metadataEvent.setXmlResource(beanDefinition.getXmlReaderContext().getResource());
    }
}
