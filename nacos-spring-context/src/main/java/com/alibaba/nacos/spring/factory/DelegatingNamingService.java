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
package com.alibaba.nacos.spring.factory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.spring.metadata.NacosServiceMetaData;

import java.util.List;
import java.util.Properties;

/**
 * Delegating {@link NamingService} with {@link NacosServiceMetaData}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NamingService
 * @see NacosServiceMetaData
 * @since 0.1.0
 */
class DelegatingNamingService implements NamingService, NacosServiceMetaData {

    private final NamingService delegate;

    private final Properties properties;

    DelegatingNamingService(NamingService delegate, Properties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port) throws NacosException {
        delegate.registerInstance(serviceName, ip, port);
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
        delegate.registerInstance(serviceName, ip, port, clusterName);
    }

    @Override
    public void registerInstance(String serviceName, Instance instance) throws NacosException {
        delegate.registerInstance(serviceName, instance);
    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {
        delegate.deregisterInstance(serviceName, ip, port);
    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
        delegate.deregisterInstance(serviceName, ip, port, clusterName);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        return delegate.getAllInstances(serviceName);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
        return delegate.getAllInstances(serviceName, clusters);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
        return delegate.selectInstances(serviceName, healthy);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException {
        return delegate.selectInstances(serviceName, clusters, healthy);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
        return delegate.selectOneHealthyInstance(serviceName);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
        return delegate.selectOneHealthyInstance(serviceName, clusters);
    }

    @Override
    public void subscribe(String serviceName, EventListener listener) throws NacosException {
        delegate.subscribe(serviceName, listener);
    }

    @Override
    public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
        delegate.subscribe(serviceName, clusters, listener);
    }

    @Override
    public void unsubscribe(String serviceName, EventListener listener) throws NacosException {
        delegate.unsubscribe(serviceName, listener);
    }

    @Override
    public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
        delegate.unsubscribe(serviceName, clusters, listener);
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize) throws NacosException {
        return delegate.getServicesOfServer(pageNo, pageSize);
    }

    @Override
    public List<ServiceInfo> getSubscribeServices() throws NacosException {
        return delegate.getSubscribeServices();
    }

    @Override
    public String getServerStatus() {
        return delegate.getServerStatus();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
