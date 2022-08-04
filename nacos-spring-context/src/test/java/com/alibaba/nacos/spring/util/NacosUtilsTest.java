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

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;

/**
 * {@link NacosUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosUtilsTest {

	@NacosInjected
	private Object object = new Object();

	@NacosInjected(properties = @NacosProperties(serverAddr = "test"))
	private Object object2 = new Object();
	
	
	Properties globalProperties;
	
	Properties currentProperties = new Properties();
	
	@Before
	public void setUp(){
		globalProperties = new Properties();
		globalProperties.setProperty("namespace","nacos_ns_playground");
		globalProperties.setProperty("username","nacos");
		globalProperties.setProperty("enableRemoteSyncConfig","true");
		globalProperties.setProperty("configLongPollTimeout","30000");
		globalProperties.setProperty("configRetryTime","2000");
		globalProperties.setProperty("encode","UTF-8");
		globalProperties.setProperty("serverAddr","http://test01-nacos.api.net:8080");
		globalProperties.setProperty("maxRetry","3");
		globalProperties.setProperty("password","nacos");
		
		currentProperties = new Properties();
		currentProperties.setProperty("password","nacos_test");
		currentProperties.setProperty("namespace","nacos_ns_playground");
		currentProperties.setProperty("encode","UTF-8");
		currentProperties.setProperty("serverAddr","http://test02-nacos.api.net:8848");
	}

	@Test
	public void testIsDefault() {

		testIsDefault("object", true);
		testIsDefault("object2", false);
	}

	private void testIsDefault(String fieldName, boolean expectedValue) {

		Field objectField = ReflectionUtils.findField(getClass(), fieldName);

		NacosInjected nacosInjected = objectField.getAnnotation(NacosInjected.class);

		NacosProperties nacosProperties = nacosInjected.properties();
		Assert.assertEquals(expectedValue, NacosUtils.isDefault(nacosProperties));
	}
	
	@Test
	public void merge() {
		NacosUtils.merge(globalProperties, currentProperties);
		Assert.assertEquals(globalProperties.getProperty("serverAddr"), "http://test02-nacos.api.net:8848");
		Assert.assertEquals(globalProperties.size(), 9);
	}
}
