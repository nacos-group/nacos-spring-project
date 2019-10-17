package com.alibaba.nacos.spring.context.event.config;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author dinglang
 * @since 2019-10-17 14:43
 */
public class FieldChangedEvent extends ApplicationEvent {
    private Map<String,String> properties;

    public FieldChangedEvent(Object source) {
        super(source);
    }

    public FieldChangedEvent(Object source,Map<String,String> properties) {
        super(source);
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
