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

import com.alibaba.nacos.spring.util.PropertiesPlaceholderResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getGlobalPropertiesBean;
import static com.alibaba.nacos.spring.util.NacosUtils.isDefault;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * Resolve actual Nacos {@link Properties} including placeholders in values.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see @NacosProperties
 * @since 0.1.0
 */
public class NacosPropertiesResolver implements BeanFactoryAware, EnvironmentAware {

    private Properties globalNacosProperties;

    private PropertiesPlaceholderResolver resolver;

    public Properties resolve(NacosProperties nacosProperties) {

        if (isDefault(nacosProperties)) { // If all attributes are default , globalNacosProperties will be uesd.
            return globalNacosProperties;
        }

        Map<String, Object> attributes = getAnnotationAttributes(nacosProperties);

        return resolver.resolve(attributes);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.globalNacosProperties = getGlobalPropertiesBean(beanFactory);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.resolver = new PropertiesPlaceholderResolver(environment);
    }
}
