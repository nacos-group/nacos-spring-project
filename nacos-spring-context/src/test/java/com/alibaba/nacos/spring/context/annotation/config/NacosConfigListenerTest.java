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

package com.alibaba.nacos.spring.context.annotation.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * NacosConfigListenerTest.
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, NacosConfigListenerTest.class })
@ContextConfiguration(classes = { NacosConfigListenerTest.NacosConfiguration.class,
		NacosConfigListenerTest.class })
public class NacosConfigListenerTest
		extends AbstractNacosHttpServerTestExecutionListener {

	@BeforeClass
	public static void beforeClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	@AfterClass
	public static void afterClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	private static volatile String content = "";

	private static volatile boolean receiveOne = false;

	private static volatile boolean receiveTwo = false;

	private static volatile boolean receiveThree = false;

	@NacosInjected
	private ConfigService configService;

	@Override
	protected String getServerAddressPropertyName() {
		return "server.addr";
	}

	@NacosConfigListener(dataId = "com.alibaba.nacos.example.properties", timeout = 2000L)
	public void onMessage(String config) {
		System.out.println("onMessage: " + config);
		receiveOne = true;
		content = config;
	}

	@NacosConfigListener(dataId = "convert_map.properties", timeout = 2000L)
	public void onMessage(Map config) {
		System.out.println("onMessage: " + config);
		receiveTwo = true;
	}

	@NacosConfigListener(dataId = "convert_map.yaml", timeout = 2000L)
	public void onMessageYaml(Map config) {
		System.out.println("onMessage: " + config);
		receiveThree = true;
	}

	@Before
	public void before() {

	}

	@Test
	public void testConfigListener() throws InterruptedException {

		final long currentTimeMillis = System.currentTimeMillis();

		boolean result = false;
		try {
			result = configService.publishConfig("com.alibaba.nacos.example.properties",
					"DEFAULT_GROUP", "" + currentTimeMillis);
			result = configService.publishConfig("convert_map.properties",
					"DEFAULT_GROUP", "this.is.test=true");
			result = configService.publishConfig("convert_map.yaml", "DEFAULT_GROUP",
					"routingMap:\n" + "  - aaa\n" + "  - bbb\n" + "  - ccc\n"
							+ "  - ddd\n" + "  - eee\n" + "endPointMap:\n" + "  - fff\n"
							+ "testMap:\n" + "  abc: def1");
		}
		catch (NacosException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(result);
		while (!receiveOne && !receiveTwo && !receiveThree) {
			TimeUnit.SECONDS.sleep(3);
		}
		Assert.assertEquals("" + currentTimeMillis, content);
	}

	@Configuration
	// 在命名空间详情处可以获取到 endpoint 和 namespace；accessKey 和 secretKey 推荐使用 RAM 账户的
	@EnableNacosConfig(readConfigTypeFromDataId = false, globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
	public static class NacosConfiguration {

	}
}
