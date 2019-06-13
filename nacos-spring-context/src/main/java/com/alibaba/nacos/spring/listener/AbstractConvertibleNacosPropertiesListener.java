package com.alibaba.nacos.spring.listener;

import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.spring.convert.converter.NacosPropertySourceConverter;

/**
 * @author hylexus
 * Created At 2019-06-13 11:01
 */
public abstract class AbstractConvertibleNacosPropertiesListener extends AbstractListener {
    private NacosPropertySourceConverter converter;

    public AbstractConvertibleNacosPropertiesListener(NacosPropertySourceConverter converter) {
        this.converter = converter;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        String properties = this.converter.convert(configInfo);
        process(properties);
    }

    protected abstract void process(String converted);

    public void setConverter(NacosPropertySourceConverter converter) {
        this.converter = converter;
    }

    public NacosPropertySourceConverter getConverter() {
        return converter;
    }
}
