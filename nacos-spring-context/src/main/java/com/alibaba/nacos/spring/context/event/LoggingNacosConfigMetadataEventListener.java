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
package com.alibaba.nacos.spring.context.event;

import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

/**
 * Logging {@link NacosConfigMetadataEvent} {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class LoggingNacosConfigMetadataEventListener implements ApplicationListener<NacosConfigMetadataEvent> {

    /**
     * The bean name of {@link LoggingNacosConfigMetadataEventListener}
     */
    public static final String BEAN_NAME = "loggingNacosConfigMetadataEventListener";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String LOGGING_MESSAGE = "Nacos Config Metadata : " +
            "dataId='{}'" +
            ", groupId='{}'" +
            ", beanName='{}'" +
            ", bean='{}'" +
            ", beanType='{}'" +
            ", annotatedElement='{}'" +
            ", xmlResource='{}'" +
            ", nacosProperties='{}'" +
            ", nacosPropertiesAttributes='{}'" +
            ", source='{}'" +
            ", timestamp='{}'";

    @Override
    public void onApplicationEvent(NacosConfigMetadataEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info(LOGGING_MESSAGE,
                    event.getDataId(),
                    event.getGroupId(),
                    event.getBeanName(),
                    event.getBean(),
                    event.getBeanType(),
                    event.getAnnotatedElement(),
                    event.getXmlResource(),
                    event.getNacosProperties(),
                    event.getNacosPropertiesAttributes(),
                    event.getSource(),
                    event.getTimestamp()
            );
        }
    }
}
