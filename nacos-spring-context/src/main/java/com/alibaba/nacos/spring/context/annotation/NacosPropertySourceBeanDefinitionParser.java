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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.context.annotation.NacosPropertySource.*;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.resolveBeanFactory;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;

/**
 * Nacos Property Source {@link BeanDefinitionParser} for &lt;nacos:property-source ...&gt;
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceBeanDefinitionParser implements BeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        this.environment = (ConfigurableEnvironment) parserContext.getDelegate().getEnvironment();

        this.beanFactory = resolveBeanFactory(parserContext.getRegistry());

        this.applicationEventPublisher = resolveApplicationEventPublisher(parserContext);

        Set<String> dataIdSet = new LinkedHashSet<String>(1);

        String dataId = element.getAttribute("data-id");
        if (StringUtils.hasText(dataId)) {
            dataIdSet.add(dataId);
        }

        String dataIds = element.getAttribute("data-ids");
        if (dataIds != null) {
            String[] dataIdArray = StringUtils.commaDelimitedListToStringArray(dataIds);
            for (String currentDataId : dataIdArray) {
                if (StringUtils.hasText(currentDataId)) {
                    dataIdSet.add(currentDataId);
                }
            }
        }

        for (String currentDataId : dataIdSet) {
            addPropertySourceAttribute(element, currentDataId);
        }

        return null;
    }

    private ApplicationEventPublisher resolveApplicationEventPublisher(ParserContext parserContext) {
        ResourceLoader resourceLoader = parserContext.getReaderContext().getReader().getResourceLoader();
        try {
            return (ApplicationEventPublisher)resourceLoader;
        } catch (ClassCastException e) {
            logger.warn("The auto-refreshed of the <nacos:property-source> element will be invalidated due to {}",
                e.getMessage());
        }
        return null;
    }

    private void addPropertySourceAttribute(Element element, String dataId) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(DATA_ID_ATTRIBUTE_NAME, dataId);

        setProperty(attributes, NAME_ATTRIBUTE_NAME, element.getAttribute("name"), DEFAULT_STRING_ATTRIBUTE_VALUE);
        setProperty(attributes, GROUP_ID_ATTRIBUTE_NAME, element.getAttribute("group-id"), DEFAULT_GROUP);
        setProperty(attributes, BEFORE_ATTRIBUTE_NAME, element.getAttribute("before"), DEFAULT_STRING_ATTRIBUTE_VALUE);
        setProperty(attributes, AFTER_ATTRIBUTE_NAME, element.getAttribute("after"), DEFAULT_STRING_ATTRIBUTE_VALUE);

        setBooleanProperty(attributes, FIRST_ATTRIBUTE_NAME, element.getAttribute("first"), false);
        setBooleanProperty(attributes, AUTO_REFRESHED, element.getAttribute("auto-refreshed"), false);

        NacosPropertySourceProcessor propertySourceProcessor = new NacosPropertySourceProcessor(beanFactory,
            environment, applicationEventPublisher);

        propertySourceProcessor.process(attributes);
    }

    private void setProperty(Map<String, Object> attributes, String name, String value, Object defaultValue) {
        String resolvedValue = environment.resolvePlaceholders(value);
        if (StringUtils.hasText(resolvedValue)) {
            attributes.put(name, resolvedValue);
        } else {
            if (resolvedValue != null) {
                attributes.put(name, defaultValue);
            }
        }
    }

    private void setBooleanProperty(Map<String, Object> attributes, String name, String value, boolean defaultValue) {
        String resolvedValue = environment.resolvePlaceholders(value);
        if (StringUtils.hasText(resolvedValue)) {
            attributes.put(name, "true".equals(resolvedValue));
        } else {
            if (resolvedValue != null) {
                attributes.put(name, defaultValue);
            }
        }
    }
}
