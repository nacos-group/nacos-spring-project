package com.alibaba.nacos.spring.factory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.spring.metadata.NacosServiceMetaData;

import java.util.Map;
import java.util.Properties;

class DelegatingNamingMaintainService implements NamingMaintainService, NacosServiceMetaData {

    private final NamingMaintainService delegate;

    private final Properties properties;

    public DelegatingNamingMaintainService(NamingMaintainService delegate, Properties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public void updateInstance(String serviceName, Instance instance) throws NacosException {
        delegate.updateInstance(serviceName, instance);
    }

    @Override
    public void updateInstance(String serviceName, String groupName, Instance instance) throws NacosException {
        delegate.updateInstance(serviceName, groupName, instance);
    }

    @Override
    public Service queryService(String serviceName) throws NacosException {
        return delegate.queryService(serviceName);
    }

    @Override
    public Service queryService(String serviceName, String groupName) throws NacosException {
        return delegate.queryService(serviceName, groupName);
    }

    @Override
    public void createService(String serviceName) throws NacosException {
        delegate.createService(serviceName);
    }

    @Override
    public void createService(String serviceName, String groupName) throws NacosException {
        delegate.createService(serviceName, groupName);
    }

    @Override
    public void createService(String serviceName, String groupName, float protectThreshold) throws NacosException {
        delegate.createService(serviceName, groupName, protectThreshold);
    }

    @Override
    public void createService(String serviceName, String groupName, float protectThreshold, String expression) throws NacosException {
        delegate.createService(serviceName, groupName, protectThreshold, expression);
    }

    @Override
    public void createService(Service service, AbstractSelector selector) throws NacosException {
        delegate.createService(service, selector);
    }

    @Override
    public boolean deleteService(String serviceName) throws NacosException {
        return delegate.deleteService(serviceName);
    }

    @Override
    public boolean deleteService(String serviceName, String groupName) throws NacosException {
        return delegate.deleteService(serviceName, groupName);
    }

    @Override
    public void updateService(String serviceName, String groupName, float protectThreshold) throws NacosException {
        delegate.updateService(serviceName, groupName, protectThreshold);
    }

    @Override
    public void updateService(String serviceName, String groupName, float protectThreshold, Map<String, String> metadata) throws NacosException {
        delegate.updateService(serviceName, groupName, protectThreshold, metadata);
    }

    @Override
    public void updateService(Service service, AbstractSelector selector) throws NacosException {
        delegate.updateService(service, selector);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

}
