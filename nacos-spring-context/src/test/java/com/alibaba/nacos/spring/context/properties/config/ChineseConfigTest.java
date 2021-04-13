package com.alibaba.nacos.spring.context.properties.config;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import org.junit.Assert;
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

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * Chinese in test configuration.
 *
 * @author klw(213539 @ qq.com)
 * @date 2021/4/13 14:14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ChineseConfigTest.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, ChineseConfigTest.class })
@NacosPropertySources(value = { @NacosPropertySource(dataId = ChineseConfigTest.DATA_ID, autoRefreshed = true) })
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}", enableRemoteSyncConfig = "true", maxRetry = "5", configRetryTime = "2600", configLongPollTimeout = "26000"))
@Component
public class ChineseConfigTest extends AbstractNacosHttpServerTestExecutionListener {

    public static final String DATA_ID = "chinese-config-test";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String CH1_VALUE= "测试111";

    private static final String CH2_VALUE= "测试222";

    @NacosInjected
    private ConfigService configService;

    @Autowired
    private CfgBean cfgBean;

    @Bean
    public CfgBean cfgBean() {
        return new CfgBean();
    }

    @Test
    public void testValue() throws InterruptedException, NacosException {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "cfg.ch1=" + CH1_VALUE + LINE_SEPARATOR
                + "cfg.ch2=" + CH2_VALUE);

        Thread.sleep(1000);

        Assert.assertEquals(CH1_VALUE, cfgBean.ch1);
        Assert.assertEquals(CH2_VALUE, cfgBean.ch2);
    }

    @Override
    public void init(EmbeddedNacosHttpServer httpServer) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, DATA_ID);
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);

        config.put(CONTENT_PARAM_NAME, "cfg.ch1=测试1" + LINE_SEPARATOR
                + "cfg.ch2=测试2");
        httpServer.initConfig(config);
    }

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    @NacosConfigurationProperties(dataId = DATA_ID, autoRefreshed = true, type = ConfigType.PROPERTIES)
    public class CfgBean {

        @NacosValue(value = "${cfg.ch1:中文1}", autoRefreshed = true)
        private String ch1;

        @NacosValue(value = "${cfg.ch2:中文2}", autoRefreshed = true)
        private String ch2;

    }

}
