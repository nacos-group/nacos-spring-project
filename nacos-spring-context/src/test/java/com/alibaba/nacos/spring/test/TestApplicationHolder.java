package com.alibaba.nacos.spring.test;

import com.alibaba.nacos.spring.factory.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@Configuration
public class TestApplicationHolder {

    @Bean(name = ApplicationContextHolder.BEAN_NAME)
    public ApplicationContextHolder applicationContextHolder(ApplicationContext applicationContext) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        applicationContextHolder.setApplicationContext(applicationContext);
        return applicationContextHolder;
    }

}
