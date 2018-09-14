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
package com.alibaba.nacos.spring.context.config.xml;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.registerNacosPropertySourcePostProcessor;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.registerXmlNacosPropertySourceBuilder;

/**
 * Nacos Property Source {@link BeanDefinitionParser} for &lt;nacos:property-source ...&gt;
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

        BeanDefinitionRegistry registry = parserContext.getRegistry();
        // Register Dependent Beans
        registerNacosPropertySourcePostProcessor(registry);
        registerXmlNacosPropertySourceBuilder(registry);

        NacosPropertySourceXmlBeanDefinition beanDefinition = new NacosPropertySourceXmlBeanDefinition();
        beanDefinition.setElement(element);
        beanDefinition.setXmlReaderContext(parserContext.getReaderContext());

        return beanDefinition;
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

}
