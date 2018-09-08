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
package com.alibaba.nacos.spring.context.config;

import com.alibaba.nacos.spring.context.annotation.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.NacosPropertySourceBeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.context.annotation.NacosPropertySource.*;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.registerNacosPropertySourceProcessor;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;

/**
 * Nacos Property Source {@link BeanDefinitionParser} for &lt;nacos:property-source ...&gt;
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceBeanDefinitionParser implements BeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurableEnvironment environment;

    private ConfigurableConversionService conversionService;

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        this.environment = (ConfigurableEnvironment) parserContext.getDelegate().getEnvironment();
        this.conversionService = environment.getConversionService();

        BeanDefinitionRegistry registry = parserContext.getRegistry();

        registerNacosPropertySourceProcessor(registry);

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
            registerNacosPropertySourceBeanDefinition(element, currentDataId, registry);
        }

        return null;
    }

    private void registerNacosPropertySourceBeanDefinition(Element element, String currentDataId,
                                                           BeanDefinitionRegistry beanDefinitionRegistry) {

        NacosPropertySourceBeanDefinition beanDefinition = new NacosPropertySourceBeanDefinition();

        // Nacos Metadata
        String groupId = getAttribute(element, "group-id", DEFAULT_GROUP);
        beanDefinition.setAttribute(DATA_ID_ATTRIBUTE_NAME, currentDataId);
        beanDefinition.setAttribute(GROUP_ID_ATTRIBUTE_NAME, groupId);
        // PropertySource Name
        beanDefinition.setAttribute(NAME_ATTRIBUTE_NAME, getAttribute(element, NAME_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE));
        // PropertySource Order
        beanDefinition.setAttribute(BEFORE_ATTRIBUTE_NAME, getAttribute(element, BEFORE_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE));
        beanDefinition.setAttribute(AFTER_ATTRIBUTE_NAME, getAttribute(element, AFTER_ATTRIBUTE_NAME, DEFAULT_STRING_ATTRIBUTE_VALUE));
        beanDefinition.setAttribute(FIRST_ATTRIBUTE_NAME, getAttribute(element, FIRST_ATTRIBUTE_NAME, DEFAULT_BOOLEAN_ATTRIBUTE_VALUE));
        // Auto-refreshed
        beanDefinition.setAttribute(AUTO_REFRESHED, getAttribute(element, "auto-refreshed", DEFAULT_BOOLEAN_ATTRIBUTE_VALUE));

        String beanName = beanDefinition.getClass().getSimpleName() + "#" + currentDataId + "#" + groupId;

        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

    private <T> T getAttribute(Element element, String name, T defaultValue) {
        String value = element.getAttribute(name);
        String resolvedValue = environment.resolvePlaceholders(value);
        T attributeValue = StringUtils.hasText(resolvedValue) ?
                (T) conversionService.convert(resolvedValue, defaultValue.getClass()) :
                defaultValue;
        return attributeValue;
    }

}
