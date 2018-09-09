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

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.env.Environment;
import org.w3c.dom.Element;

import java.util.Properties;

import static com.alibaba.nacos.api.annotation.NacosProperties.*;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.registerGlobalNacosProperties;

/**
 * Nacos Global {@link Properties} {@link BeanDefinitionParser} for &lt;nacos:global-properties ...&gt;
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosBeanUtils#GLOBAL_NACOS_PROPERTIES_BEAN_NAME
 * @see NacosProperties
 * @see PropertyKeyConst
 * @since 0.1.0
 */
public class GlobalNacosPropertiesBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        Properties properties = new Properties();

        Environment environment = parserContext.getDelegate().getEnvironment();

        properties.setProperty(PropertyKeyConst.ENDPOINT, element.getAttribute(ENDPOINT));
        properties.setProperty(PropertyKeyConst.NAMESPACE, element.getAttribute(NAMESPACE));
        properties.setProperty(PropertyKeyConst.ACCESS_KEY, element.getAttribute(ACCESS_KEY));
        properties.setProperty(PropertyKeyConst.SECRET_KEY, element.getAttribute(SECRET_KEY));
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, element.getAttribute(SERVER_ADDR));
        properties.setProperty(PropertyKeyConst.CLUSTER_NAME, element.getAttribute(CLUSTER_NAME));
        properties.setProperty(PropertyKeyConst.ENCODE, element.getAttribute(ENCODE));

        BeanDefinitionRegistry registry = parserContext.getRegistry();

        // Register Global Nacos Properties as Spring singleton bean
        registerGlobalNacosProperties(properties, registry, environment, GLOBAL_NACOS_PROPERTIES_BEAN_NAME);

        return null;
    }

}
