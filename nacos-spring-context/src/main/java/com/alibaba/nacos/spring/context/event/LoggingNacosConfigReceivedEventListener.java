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

import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

/**
 * @author Sayi
 */
public class LoggingNacosConfigReceivedEventListener implements ApplicationListener<NacosConfigReceivedEvent> {

	/**
	 * The bean name of {@link LoggingNacosConfigReceivedEventListener}
	 */
	public static final String BEAN_NAME = "LoggingNacosConfigReceivedEventListener";

	private final static String LOGGING_MESSAGE = "Nacos Config Received: dataId='{}', groupId='{}'"
			+ ", md5='{}', timestamp='{}'";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(NacosConfigReceivedEvent event) {
		if (!logger.isInfoEnabled()) {
			return;
		}
		logger.info(LOGGING_MESSAGE, event.getDataId(), event.getGroupId(), md5(event.getContent()),
				event.getTimestamp());
	}

	private String md5(String content) {
		if (null == content) {
			return null;
		}
		return DigestUtils.md5Hex(content);
	}

}
