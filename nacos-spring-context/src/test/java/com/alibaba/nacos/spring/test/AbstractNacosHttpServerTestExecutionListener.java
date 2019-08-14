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
package com.alibaba.nacos.spring.test;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Abstract Nacos HTTP Server {@link TestExecutionListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class AbstractNacosHttpServerTestExecutionListener extends AbstractTestExecutionListener {

    private EmbeddedNacosHttpServer httpServer;

    static {
        System.setProperty("nacos.standalone", "true");
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        initEnvironment(testContext);
        httpServer = new EmbeddedNacosHttpServer();
        init(httpServer);
        System.setProperty(getServerAddressPropertyName(), "127.0.0.1:" + httpServer.getPort());
        httpServer.start(true);
    }

    public void initEnvironment(TestContext testContext) {

    }

    @Override
    public final void afterTestClass(TestContext testContext) throws Exception {
        httpServer.stop();
        System.getProperties().remove(getServerAddressPropertyName());
    }

    /**
     * Initialize before test , this method just will be invoked once in current test case.
     *
     * @param server {@link EmbeddedNacosHttpServer}
     */
    protected void init(EmbeddedNacosHttpServer server) {

    }

    /**
     * The property name of Nacos HTTP Server Address
     *
     * @return non-null
     * @see NacosProperties#serverAddr()
     */
    protected abstract String getServerAddressPropertyName();

}
