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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.api.annotation.NacosProperties;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.*;
import static com.alibaba.nacos.spring.util.NacosUtils.merge;
import static java.util.Collections.emptyMap;

/**
 * The source enumeration of Global {@link NacosProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public enum GlobalNacosPropertiesSource {

    /**
     * Default Global {@link NacosProperties}
     */
    DEFAULT(GLOBAL_NACOS_PROPERTIES_BEAN_NAME),

    /**
     * Global {@link NacosProperties} for Nacos Config
     */
    CONFIG(CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME),

    /**
     * Global {@link NacosProperties} for Nacos discovery
     */
    DISCOVERY(DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME);


    private final String beanName;

    GlobalNacosPropertiesSource(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Get Merged {@link Properties} from {@link BeanFactory}
     *
     * @param beanFactory {@link BeanFactory}
     * @return Global {@link Properties} Bean
     */
    public Properties getMergedGlobalProperties(BeanFactory beanFactory) {
        Properties currentProperties = getProperties(beanFactory, beanName);
        Properties globalProperties = getProperties(beanFactory, GLOBAL_NACOS_PROPERTIES_BEAN_NAME);
        // merge
        merge(globalProperties, currentProperties);
        return globalProperties;
    }


    private Properties getProperties(BeanFactory beanFactory, String beanName) {
        Properties properties = new Properties();
        // If Bean is absent , source will be empty.
        Map<?, ?> propertiesSource = beanFactory.containsBean(beanName) ?
                beanFactory.getBean(beanName, Properties.class) : emptyMap();
        properties.putAll(propertiesSource);
        return properties;
    }
}
