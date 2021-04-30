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

import com.alibaba.nacos.api.annotation.NacosProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;

import java.util.Iterator;
import java.util.Map;

/**
 * Logging {@link NacosConfigMetadataEvent} {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class LoggingNacosConfigMetadataEventListener
		implements ApplicationListener<NacosConfigMetadataEvent> {

	/**
	 * The bean name of {@link LoggingNacosConfigMetadataEventListener}
	 */
	public static final String BEAN_NAME = "loggingNacosConfigMetadataEventListener";
	private final static String LOGGING_MESSAGE = "Nacos Config Metadata : "
			+ "dataId='{}'" + ", groupId='{}'" + ", beanName='{}'" + ", bean='{}'"
			+ ", beanType='{}'" + ", annotatedElement='{}'" + ", xmlResource='{}'"
			+ ", nacosProperties='{}'" + ", nacosPropertiesAttributes='{}'"
			+ ", source='{}'" + ", timestamp='{}'";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(NacosConfigMetadataEvent event) {
		if (!logger.isInfoEnabled()) { 
			return;
		}
		logger.info(LOGGING_MESSAGE, event.getDataId(), event.getGroupId(),
				event.getBeanName(), event.getBean(), event.getBeanType(),
				event.getAnnotatedElement(), event.getXmlResource(),
				obscuresNacosProperties(event.getNacosProperties()), event.getNacosPropertiesAttributes(),
				event.getSource(), event.getTimestamp());
	}
	
	/**
	 * obscures some private field like password in {@link com.alibaba.nacos.api.annotation.NacosProperties}
	 * @param nacosProperties {@link com.alibaba.nacos.api.annotation.NacosProperties}
	 * @return the properties String after obscures
	 */
	private String obscuresNacosProperties(Map<Object, Object> nacosProperties) {
		String nacosPropertyStr;
		if (nacosProperties != null && nacosProperties.size() > 0) {
			StringBuilder sb = new StringBuilder("{");
			for (Map.Entry<Object, Object> e : nacosProperties.entrySet()) {
				Object key = e.getKey();
				Object value = e.getValue();
				sb.append(key);
				sb.append('=');
				// hide some private messages
				if (key != null && NacosProperties.PASSWORD.equals(key.toString())) {
					sb.append("******");
				} else {
					sb.append(value);
				}
				sb.append(", ");
			}
			sb.append("}");
			nacosPropertyStr = sb.toString();
		} else {
			nacosPropertyStr = "{}";
		}
		return nacosPropertyStr;
	}
}
