///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.nacos.spring.context.annotation.config;
//
//import com.alibaba.nacos.api.annotation.NacosInjected;
//import com.alibaba.nacos.api.annotation.NacosProperties;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
//import com.alibaba.nacos.spring.context.annotation.EnableNacos;
//import com.alibaba.nacos.spring.convert.converter.config.UserNacosConfigConverter;
//import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
//import com.alibaba.nacos.spring.test.User;
//import com.alibaba.nacos.spring.test.XmlApp;
//import com.alibaba.nacos.spring.test.YamlApp;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestExecutionListeners;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
//import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
//import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
//import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
//import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;
//import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;
//import static org.junit.Assert.assertEquals;
//
///**
// * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
// * @since
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {
//        NacosPropertySourceXMLTest.class
//})
//@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
//        DirtiesContextTestExecutionListener.class, NacosPropertySourceXMLTest.class})
//@NacosPropertySource(dataId = XmlApp.DATA_ID_XML, autoRefreshed = true, type = "xml")
//@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
//public class NacosPropertySourceXMLTest extends AbstractNacosHttpServerTestExecutionListener {
//
//    private String xml =
//                    "<xml>" +
//                    "<students>" +
//                    "<student>" +
//                    "<name>lct-1</name>" +
//                    "<num>1006010022</num>" +
//                    "</student>" +
//                    "<student>" +
//                    "<name>lct-2</name>" +
//                    "<num>1006010033</num>" +
//                    "</student>" +
//                    "<student>" +
//                    "<name>lct-3</name>" +
//                    "<num>1006010044</num>" +
//                    "</student>" +
//                    "<student>" +
//                    "<name>lct-4</name>" +
//                    "<num>1006010055</num>" +
//                    "</student>" +
//                    "</students>" +
//                    "</xml>";
//
//    private final String except = "XmlApp{students=[Student{name='lct-1', num='1006010022'}, Student{name='lct-3', num='1006010044'}, Student{name='lct-4', num='1006010055'}]}";
//
//    @Override
//    public void init(EmbeddedNacosHttpServer httpServer) {
//        Map<String, String> config = new HashMap<String, String>(1);
//        config.put(DATA_ID_PARAM_NAME, XmlApp.DATA_ID_XML);
//        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
//        config.put(CONTENT_PARAM_NAME, xml);
//
//        httpServer.initConfig(config);
//    }
//
//    @Override
//    protected String getServerAddressPropertyName() {
//        return "server.addr";
//    }
//
//    @Bean
//    public XmlApp xmlApp() {
//        return new XmlApp();
//    }
//
//    @NacosInjected
//    private ConfigService configService;
//
//    @Autowired
//    private XmlApp xmlApp;
//
//    @Test
//    public void testValue() throws NacosException, InterruptedException {
//
//        configService.publishConfig(XmlApp.DATA_ID_XML, GROUP_ID, xml);
//
//        Thread.sleep(2000);
//
//        Assert.assertEquals(except, xmlApp.toString());
//    }
//
//}
