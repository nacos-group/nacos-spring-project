package com.alibaba.nacos.spring.beans.factory.annotation;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource;

import java.util.Properties;

/**
 * {@link NamingMaintainService} Bean Builder
 *
 * @author liaochuntao
 * @since
 */
public class NamingMaintainServiceBeanBuilder extends AbstractNacosServiceBeanBuilder<NamingMaintainService>  {

    /**
     * The bean name of {@link NamingMaintainServiceBeanBuilder}
     */
    public static final String BEAN_NAME = "namingMaintainServiceBeanBuilder";

    protected NamingMaintainServiceBeanBuilder() {
        super(GlobalNacosPropertiesSource.DISCOVERY);
    }

    @Override
    protected NamingMaintainService createService(NacosServiceFactory nacosServiceFactory, Properties properties)
            throws NacosException {
        return nacosServiceFactory.createNamingMaintainService(properties);
    }

}
