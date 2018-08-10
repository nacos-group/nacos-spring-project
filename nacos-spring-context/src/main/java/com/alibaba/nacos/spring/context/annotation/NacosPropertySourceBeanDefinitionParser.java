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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
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
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1018</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceBeanDefinitionParser implements BeanDefinitionParser {

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        this.environment = (ConfigurableEnvironment) parserContext.getDelegate().getEnvironment();

        this.beanFactory = resolveBeanFactory(parserContext.getRegistry());

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

    private void addPropertySourceAttribute(Element element, String dataId) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(DATA_ID_ATTRIBUTE_NAME, dataId);

        setPropertyIfPresent(attributes, NAME_ATTRIBUTE_NAME, element.getAttribute("name"), DEFAULT_STRING_ATTRIBUTE_VALUE);
        setPropertyIfPresent(attributes, GROUP_ID_ATTRIBUTE_NAME, element.getAttribute("group-id"), DEFAULT_GROUP);
        setPropertyIfPresent(attributes, FIRST_ATTRIBUTE_NAME, element.getAttribute("first"), false);
        setPropertyIfPresent(attributes, BEFORE_ATTRIBUTE_NAME, element.getAttribute("before"), DEFAULT_STRING_ATTRIBUTE_VALUE);
        setPropertyIfPresent(attributes, AFTER_ATTRIBUTE_NAME, element.getAttribute("after"), DEFAULT_STRING_ATTRIBUTE_VALUE);

        NacosPropertySourceProcessor propertySourceProcessor = new NacosPropertySourceProcessor(beanFactory, environment);

        propertySourceProcessor.process(attributes);
    }

    private void setPropertyIfPresent(Map<String, Object> attributes, String name, String value, Object defaultValue) {
        String resolvedValue = environment.resolvePlaceholders(value);
        if (StringUtils.hasText(resolvedValue)) {
            attributes.put(name, resolvedValue);
        } else {
            if (resolvedValue != null) {
                attributes.put(name, defaultValue);
            }
        }
    }
}
