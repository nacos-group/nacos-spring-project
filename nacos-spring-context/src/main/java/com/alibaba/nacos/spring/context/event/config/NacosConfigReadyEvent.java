/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
package com.alibaba.nacos.spring.context.event.config;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.CompositePropertySource;

public class NacosConfigReadyEvent extends ApplicationEvent {

    private CompositePropertySource compositePropertySource;

    /**
     * @param compositePropertySource
     */
    public NacosConfigReadyEvent(Object source, CompositePropertySource compositePropertySource) {
        super(source);
        this.compositePropertySource = compositePropertySource;
    }

    public CompositePropertySource getCompositePropertySource() {
        return compositePropertySource;
    }
}
