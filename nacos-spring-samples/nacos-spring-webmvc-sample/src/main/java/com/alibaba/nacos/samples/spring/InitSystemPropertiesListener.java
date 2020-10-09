package com.alibaba.nacos.samples.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * custom ServletContextListener，init nacos confg item to the System Property if you want
 * use the properties files to config the nacos , but not want use the JVM -D params in
 * your start shell
 * @author dinglang
 * @since 2019-09-19 18:23
 */
public class InitSystemPropertiesListener implements ServletContextListener {

	/**
	 * the ignoreResourceNotFound property for propertySourcesPlaceholderConfigurer
	 */
	public static final String IGNORE_RESOURCE_NOT_FOUND = "ignoreResourceNotFound";
	/**
	 * the ignoreUnresolvablePlaceholders property for
	 * propertySourcesPlaceholderConfigurer
	 */
	public static final String IGNORE_UNRESOLVABLE_PLACEHOLDERS = "ignoreUnresolvablePlaceholders";
	/**
	 * the nacos server address
	 */
	private static final String SERVER_ADDRESS_PROPERTY_NAME = "nacos.server-addr";
	/**
	 * the nacos namespace
	 */
	private static final String NAMESPACE_PROPERTY_NAME = "nacos.config.namespace";
	/**
	 * the nacos username
	 */
	private static final String USERNAME_PROPERTY_NAME = "nacos.username";
	/**
	 * the nacos password
	 */
	private static final String PASSWORD_PROPERTY_NAME = "nacos.password";
	private static final Logger logger = LoggerFactory
			.getLogger(InitSystemPropertiesListener.class);

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		logger.info("InitSystemPropertiesListener#contextInitialized,开始准备初始化系统变量");
		InputStream input = null;
		try {
			input = servletContextEvent.getServletContext()
					.getResourceAsStream("/WEB-INF/config.properties");
			Properties p = new Properties();
			p.load(input);

			String nacosServer = p.getProperty(SERVER_ADDRESS_PROPERTY_NAME);
			String namespace = p.getProperty(NAMESPACE_PROPERTY_NAME);
			String username = p.getProperty(USERNAME_PROPERTY_NAME);
			String password = p.getProperty(PASSWORD_PROPERTY_NAME);

			String ignoreResourceNotFound = p.getProperty(IGNORE_RESOURCE_NOT_FOUND);
			String ignoreUnresolvablePlaceholders = p
					.getProperty(IGNORE_UNRESOLVABLE_PLACEHOLDERS);

			logger.info("nacosServer:{},namespace:{}", nacosServer, namespace);
			if (StringUtils.isNotEmpty(nacosServer) && !System.getProperties()
					.containsKey(SERVER_ADDRESS_PROPERTY_NAME)) {
				System.setProperty(SERVER_ADDRESS_PROPERTY_NAME, nacosServer);
			}
			if (StringUtils.isNotEmpty(namespace)
					&& !System.getProperties().containsKey(NAMESPACE_PROPERTY_NAME)) {
				System.setProperty(NAMESPACE_PROPERTY_NAME, namespace);
			}
			if (StringUtils.isNotEmpty(username)
					&& !System.getProperties().containsKey(USERNAME_PROPERTY_NAME)) {
				System.setProperty(USERNAME_PROPERTY_NAME, username);
			}
			if (StringUtils.isNotEmpty(password)
					&& !System.getProperties().containsKey(PASSWORD_PROPERTY_NAME)) {
				System.setProperty(PASSWORD_PROPERTY_NAME, password);
			}
			if (StringUtils.isNotEmpty(ignoreResourceNotFound)
					&& !System.getProperties().containsKey(IGNORE_RESOURCE_NOT_FOUND)) {
				System.setProperty(IGNORE_RESOURCE_NOT_FOUND, ignoreResourceNotFound);
			}
			if (StringUtils.isNotEmpty(ignoreUnresolvablePlaceholders) && !System
					.getProperties().containsKey(IGNORE_UNRESOLVABLE_PLACEHOLDERS)) {
				System.setProperty(IGNORE_UNRESOLVABLE_PLACEHOLDERS,
						ignoreUnresolvablePlaceholders);
			}
		}
		catch (Exception e) {
			logger.error("初始化系统变量失败", e);
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
