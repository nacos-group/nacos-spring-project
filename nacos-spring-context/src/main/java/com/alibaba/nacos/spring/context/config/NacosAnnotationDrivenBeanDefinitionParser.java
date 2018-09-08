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

import com.alibaba.nacos.spring.context.annotation.NacosBeanDefinitionRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.env.Environment;
import org.w3c.dom.Element;

/**
 * Nacos Annotation Driven {@link BeanDefinitionParser} for XML element &lt;nacos:annotation-driven/&gt;
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanDefinitionParser
 * @since 0.1.0
 */
public class NacosAnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // Get Environment
        Environment environment = parserContext.getDelegate().getEnvironment();
        // Get BeanDefinitionRegistry
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        // Register Nacos Annotation Beans
        NacosBeanDefinitionRegistrar registrar = new NacosBeanDefinitionRegistrar();
        registrar.setEnvironment(environment);
        registrar.registerNacosAnnotationBeans(registry);
        return null;
    }
}
