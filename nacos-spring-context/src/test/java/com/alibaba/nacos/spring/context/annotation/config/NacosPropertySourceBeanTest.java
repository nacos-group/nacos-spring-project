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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.YamlApp;
import com.alibaba.nacos.spring.test.YamlBean;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.*;

/**
 * @author mai.jh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NacosPropertySourceBeanTest.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, NacosPropertySourceBeanTest.class })
@NacosPropertySources(value = {@NacosPropertySource(dataId = YamlBean.DATA_ID_YAML
				+ ".yml", autoRefreshed = true) })
@EnableNacosConfig(readConfigTypeFromDataId =  false, globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosPropertySourceBeanTest
		extends AbstractNacosHttpServerTestExecutionListener {

	private String yaml = "student:\n" +
			"    name: lct-1\n" +
			"    num: 12\n" +
			"    testApp: \n" +
			"       name: test";

	private String except = "YamlBean{student=Student{name='lct-1', num='12', testApp=TestApp{name='test'}}}";
	@NacosInjected
	private ConfigService configService;
	@Autowired
	private YamlBean yamlBean;

	@BeforeClass
	public static void beforeClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	@AfterClass
	public static void afterClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	@Override
	public void init(EmbeddedNacosHttpServer httpServer) {
		Map<String, String> config = new HashMap<String, String>(1);
		config.put(DATA_ID_PARAM_NAME, YamlBean.DATA_ID_YAML + ".yml");
		config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
		config.put(CONTENT_PARAM_NAME, yaml);

		httpServer.initConfig(config);
	}

	@Bean
	public YamlBean yamlBean() {
		return new YamlBean();
	}

	@Override
	protected String getServerAddressPropertyName() {
		return "server.addr";
	}

	@Test
	public void testValue() throws NacosException, InterruptedException {

		configService.publishConfig(YamlBean.DATA_ID_YAML + ".yml", DEFAULT_GROUP, yaml);

		Thread.sleep(2000);

		Assert.assertEquals(except, yamlBean.toString());

	}




}
