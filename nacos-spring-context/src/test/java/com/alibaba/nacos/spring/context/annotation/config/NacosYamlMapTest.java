package com.alibaba.nacos.spring.context.annotation.config;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.YamlMap;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
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
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2020/1/12 3:55 下午
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { NacosYamlMapTest.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, NacosYamlMapTest.class })

@NacosPropertySources(value = {
		@NacosPropertySource(dataId = "yaml_map"
				+ "_not_exist.yaml", autoRefreshed = true),
		@NacosPropertySource(dataId = "yaml_map" + ".yml", autoRefreshed = true) })
@EnableNacosConfig(readConfigTypeFromDataId =  false, globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosYamlMapTest extends AbstractNacosHttpServerTestExecutionListener {

	@BeforeClass
	public static void beforeClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	@AfterClass
	public static void afterClass() {
		NacosUtils.resetReadTypeFromDataId();
	}

	private String configStr = "routingMap:\n" + "  - aaa\n" + "  - bbb\n" + "  - ccc\n"
			+ "  - ddd\n" + "  - eee\n" + "endPointMap:\n" + "  - fff\n" + "testMap:\n"
			+ "  abc: def1";

	private String newConfigStr = "routingMap:\n" + "  - aaa\n" + "  - bbb\n"
			+ "  - ccc\n" + "  - ddd\n" + "endPointMap:\n" + "  - fff\n" + "testMap:\n"
			+ "  liaochuntao: def1";
	@Autowired
	private YamlMap yamlMap;
	@NacosInjected
	private ConfigService configService;

	@Override
	protected String getServerAddressPropertyName() {
		return "server.addr";
	}

	@Override
	public void init(EmbeddedNacosHttpServer httpServer) {
		Map<String, String> config = new HashMap<String, String>(1);
		config.put(DATA_ID_PARAM_NAME, "yaml_map" + ".yml");
		config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
		config.put(CONTENT_PARAM_NAME, configStr);

		httpServer.initConfig(config);
	}

	@Bean
	public YamlMap yamlMap() {
		return new YamlMap();
	}

	@Test
	public void testValue() throws NacosException, InterruptedException {

		System.out.println(yamlMap);

		configService.publishConfig("yaml_map" + ".yml", DEFAULT_GROUP, newConfigStr);

		Thread.sleep(2000);

		System.out.println(yamlMap);

	}

}
