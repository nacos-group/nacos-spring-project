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

import java.util.HashMap;
import java.util.Map;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.YamlApp;
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

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NacosPropertySourceYamlTest.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, NacosPropertySourceYamlTest.class })
@NacosPropertySources(value = {
		@NacosPropertySource(dataId = YamlApp.DATA_ID_YAML
				+ "_not_exist.yaml", autoRefreshed = true),
		@NacosPropertySource(dataId = YamlApp.DATA_ID_YAML
				+ ".yml", autoRefreshed = true) })
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosPropertySourceYamlTest
		extends AbstractNacosHttpServerTestExecutionListener {

	@BeforeClass
	public static void beforeClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	@AfterClass
	public static void afterClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	private String yaml = "students:\n" + "    - {name: lct-1,num: 12}\n"
			+ "    - {name: lct-2,num: 13}\n" + "    - {name: lct-3,num: 14}";

	private String configStr = "people:\n" + "  a: 1\n" + "  b: 1";

	private String except = "YamlApp{students=[Student{name='lct-1', num='12'}, Student{name='lct-2', num='13'}, Student{name='lct-3', num='14'}]}";

	@NacosInjected
	private ConfigService configService;

	@Autowired
	private YamlApp yamlApp;

	@Autowired
	@Qualifier(value = "myApp")
	private App app;

	@Override
	public void init(EmbeddedNacosHttpServer httpServer) {
		Map<String, String> config = new HashMap<String, String>(1);
		config.put(DATA_ID_PARAM_NAME, YamlApp.DATA_ID_YAML + ".yml");
		config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
		config.put(CONTENT_PARAM_NAME, configStr);

		httpServer.initConfig(config);
	}

	@Bean(name = "myApp")
	public App app() {
		return new App();
	}

	@Bean
	public YamlApp yamlApp() {
		return new YamlApp();
	}

	@Override
	protected String getServerAddressPropertyName() {
		return "server.addr";
	}

	@Test
	public void testValue() throws NacosException, InterruptedException {

		Assert.assertEquals("1", app.a);
		Assert.assertEquals("1", app.b);

		configService.publishConfig(YamlApp.DATA_ID_YAML + ".yml", DEFAULT_GROUP, yaml);

		Thread.sleep(2000);

		Assert.assertEquals(except, yamlApp.toString());

	}

	private static class App {

		@NacosValue("${people.a}")
		private String a;
		@NacosValue("${people.b}")
		private String b;

	}
}
