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
package com.alibaba.nacos.embedded.web.servlet;

import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;

/**
 * {@link EmbeddedNacosHttpServer} {@link ServletContextListener Listener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class EmbeddedNacosHttpServerListener implements ServletContextListener {

    private static final String SERVER_ADDRESS_PROPERTY_NAME = "nacos.server-addr";

    private EmbeddedNacosHttpServer httpServer;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            if (!System.getProperties().contains(SERVER_ADDRESS_PROPERTY_NAME)) {
                httpServer = new EmbeddedNacosHttpServer();
                httpServer.start(false);
                System.setProperty(SERVER_ADDRESS_PROPERTY_NAME, "127.0.0.1:" + httpServer.getPort());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
