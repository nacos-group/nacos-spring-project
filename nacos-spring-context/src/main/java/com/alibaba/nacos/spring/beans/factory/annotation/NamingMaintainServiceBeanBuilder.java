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
package com.alibaba.nacos.spring.beans.factory.annotation;

import java.util.Properties;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NamingMaintainServiceBeanBuilder
		extends AbstractNacosServiceBeanBuilder<NamingMaintainService> {

	/**
	 * The bean name of {@link NamingServiceBeanBuilder}
	 */
	public static final String BEAN_NAME = "namingMaintainServiceBeanBuilder";

	public NamingMaintainServiceBeanBuilder() {
		super(GlobalNacosPropertiesSource.MAINTAIN);
	}

	@Override
	protected NamingMaintainService createService(NacosServiceFactory nacosServiceFactory,
			Properties properties) throws NacosException {
		return nacosServiceFactory.createNamingMaintainService(properties);
	}
}
