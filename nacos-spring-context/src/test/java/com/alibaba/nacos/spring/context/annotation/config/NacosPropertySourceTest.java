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

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * {@link NacosPropertySource} {@link Value} Test
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @see NacosPropertySource
 * @see Value
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NacosPropertySourceTest.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, NacosPropertySourceTest.class })
@NacosPropertySources({
		@NacosPropertySource(dataId = NacosPropertySourceTest.DATA_ID, autoRefreshed = true) })
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = "${server.addr}", enableRemoteSyncConfig = "true", maxRetry = "5", configRetryTime = "2600", configLongPollTimeout = "26000"))
@Component
public class NacosPropertySourceTest
		extends AbstractNacosHttpServerTestExecutionListener {

	public static final String DATA_ID = "app";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String APP_NAME = "Nacos-Spring";
	private static final String ANOTHER_APP_NAME = "Nacos-Spring-1";
	private static final int VALUE_1 = 1;
	private static final int VALUE_2 = 2;
	private static final int VALUE_3 = 3;
	private static final int VALUE_4 = 4;
	private static final String VALUE_5 = "zhangsan";
	private static final String VALUE_6 = "lisi";
	private static final String VALUE_7 = "wangwu";
	private static final String VALUE_8 = "zhaoliu";
	private static final String DEFAULT_JSON_VALUE = "{\"0\":\"1\",\"10\":\"13\"}";
	private static final String NEW_STR_VALUE = "new json value";

	@NacosInjected
	private ConfigService configService;
	@Autowired
	private App app;
	@Autowired
	private Environment environment;

	@Override
	public void init(EmbeddedNacosHttpServer httpServer) {
		Map<String, String> config = new HashMap<String, String>(1);
		config.put(DATA_ID_PARAM_NAME, DATA_ID);
		config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);

		config.put(CONTENT_PARAM_NAME, "app.name=" + APP_NAME + LINE_SEPARATOR
				+ "app.nacosFieldIntValueAutoRefreshed=" + VALUE_1 + LINE_SEPARATOR
				+ "app.nacosMethodIntValueAutoRefreshed=" + VALUE_2 + LINE_SEPARATOR
				+ "app.nacosFieldListValueAutoRefreshed=" + VALUE_5 + "," + VALUE_6);
		httpServer.initConfig(config);
	}

	@Override
	protected String getServerAddressPropertyName() {
		return "server.addr";
	}

	@Bean
	public App app() {
		return new App();
	}

	@Test
	public void testValue() throws NacosException, InterruptedException {
		Assert.assertEquals(APP_NAME, app.name);

		Assert.assertEquals(APP_NAME, app.nameWithDefaultValue);

		Assert.assertEquals(APP_NAME, app.nacosNameAutoRefreshed);

		Assert.assertEquals(APP_NAME, app.nacosNameAutoRefreshedWithDefaultValue);

		Assert.assertEquals(APP_NAME, app.nacosNameNotAutoRefreshed);

		Assert.assertEquals(APP_NAME, environment.getProperty("app.name"));

		Assert.assertEquals(VALUE_1, app.nacosFieldIntValue);

		Assert.assertEquals(VALUE_2, app.nacosMethodIntValue);

		Assert.assertEquals(VALUE_5, app.nacosFieldListValue.get(0));
		Assert.assertEquals(VALUE_6, app.nacosFieldListValue.get(1));

		Assert.assertEquals(VALUE_1, app.nacosFieldIntValueAutoRefreshed);

		Assert.assertEquals(VALUE_2, app.nacosMethodIntValueAutoRefreshed);

		Assert.assertEquals(VALUE_5, app.nacosFieldListValueAutoRefreshed.get(0));
		Assert.assertEquals(VALUE_6, app.nacosFieldListValueAutoRefreshed.get(1));

		Assert.assertEquals(DEFAULT_JSON_VALUE, app.strWithDefaultJsonValue);

		configService.publishConfig(DATA_ID, DEFAULT_GROUP, "app.name=" + ANOTHER_APP_NAME
				+ LINE_SEPARATOR + "app.nacosFieldIntValueAutoRefreshed=" + VALUE_3
				+ LINE_SEPARATOR + "app.nacosMethodIntValueAutoRefreshed=" + VALUE_4
				+ LINE_SEPARATOR + "app.nacosFieldListValueAutoRefreshed=" + VALUE_7 + "," + VALUE_8
		        + LINE_SEPARATOR + "app.strWithDefaultJsonValue=" + NEW_STR_VALUE);

		Thread.sleep(1000);

		Assert.assertEquals(APP_NAME, app.name);

		Assert.assertEquals(APP_NAME, app.nameWithDefaultValue);

		Assert.assertEquals(ANOTHER_APP_NAME, app.nacosNameAutoRefreshed);

		Assert.assertEquals(ANOTHER_APP_NAME, app.nacosNameAutoRefreshedWithDefaultValue);

		Assert.assertEquals(APP_NAME, app.nacosNameNotAutoRefreshed);

		Assert.assertEquals(ANOTHER_APP_NAME, environment.getProperty("app.name"));

		Assert.assertEquals(VALUE_1, app.nacosFieldIntValue);

		Assert.assertEquals(VALUE_2, app.nacosMethodIntValue);

		Assert.assertEquals(VALUE_5, app.nacosFieldListValue.get(0));
		Assert.assertEquals(VALUE_6, app.nacosFieldListValue.get(1));

		Assert.assertEquals(VALUE_3, app.nacosFieldIntValueAutoRefreshed);

		Assert.assertEquals(VALUE_4, app.nacosMethodIntValueAutoRefreshed);

		Assert.assertEquals(VALUE_7, app.nacosFieldListValueAutoRefreshed.get(0));
		Assert.assertEquals(VALUE_8, app.nacosFieldListValueAutoRefreshed.get(1));

		Assert.assertEquals(NEW_STR_VALUE, app.strWithDefaultJsonValue);
	}

	public static class App {

		@Value("${app.name}")
		private String name;

		@Value("${app.name:Nacos}")
		private String nameWithDefaultValue;

		@NacosValue(value = "${app.name}", autoRefreshed = true)
		private String nacosNameAutoRefreshed;

		@NacosValue(value = "${app.name:Nacos}", autoRefreshed = true)
		private String nacosNameAutoRefreshedWithDefaultValue;

		@NacosValue("${app.name}")
		private String nacosNameNotAutoRefreshed;

		@NacosValue("${app.nacosFieldIntValue:" + VALUE_1 + "}")
		private int nacosFieldIntValue;

		@NacosValue(value = "${app.nacosFieldIntValueAutoRefreshed}", autoRefreshed = true)
		private int nacosFieldIntValueAutoRefreshed;

		private int nacosMethodIntValue;
		private int nacosMethodIntValueAutoRefreshed;

		@NacosValue(value = "#{'${app.nacosFieldListValue:" + VALUE_5 + "," + VALUE_6 + "}'.split(',')}")
		private List nacosFieldListValue;

		@NacosValue(value = "#{'${app.nacosFieldListValueAutoRefreshed}'.split(',')}", autoRefreshed = true)
		private List nacosFieldListValueAutoRefreshed;

		@NacosValue(value = "${app.strWithDefaultJsonValue:" + DEFAULT_JSON_VALUE + "}", autoRefreshed = true)
		private String strWithDefaultJsonValue;

		@NacosValue("${app.nacosMethodIntValue:" + VALUE_2 + "}")
		public void setNacosMethodIntValue(int nacosMethodIntValue) {
			this.nacosMethodIntValue = nacosMethodIntValue;
		}

		@NacosValue(value = "${app.nacosMethodIntValueAutoRefreshed}", autoRefreshed = true)
		public void setNacosMethodIntValueAutoRefreshed(
				int nacosMethodIntValueAutoRefreshed) {
			this.nacosMethodIntValueAutoRefreshed = nacosMethodIntValueAutoRefreshed;
		}
	}

}
