Nacos Spring Project
=================================

[![Build Status](https://travis-ci.org/nacos-group/nacos-spring-project.svg?branch=master)](https://travis-ci.org/nacos-group/nacos-spring-project)

[Nacos](https://github.com/alibaba/nacos) is an open source project for discovering, configuring and managing cloud-native applications. Key features of Nacos include:

- Service Discovery and Service Health Check
- Dynamic Configuration Management
- Dynamic DNS Service
- Service and Metadata Management

[Nacos Spring Project](https://github.com/nacos-group/nacos-spring-project), which is based on [Nacos](https://github.com/alibaba/nacos), fully embraces the Spring ecosystem and is designed to help you build Spring applications rapidly. 

The project contains a core module named [`nacos-spring-context`](nacos-spring-context). It enables you to expand modern Java programming models in the following ways:

- [Annotation-Driven](#41-annotation-driven)
- [Dependency Injection](#42-dependency-injection)
- [Externalized Configuration](#43-externalized-configuration)
- [Event-Driven](#44-eventlistener-driven)

These features strongly depend on Spring Framework 3.2+ API, and can be seamlessly integrated with any Spring Stack, such as Spring Boot and Spring Cloud.

**Note:** We recommend that you use annotation-driven programming, even though XML-based features also work.

Content
===============================
<!-- TOC -->

- [1. Samples](#1-samples)
    - [1.1. Samples](#11-samples)
    - [1.2. How To Run the Samples](#12-how-to-run-the-samples)
- [2. Dependencies & Compatibility](#2-dependencies--compatibility)
- [3. Quickstart](#3-quickstart)
    - [3.1. Prerequisite](#31-prerequisite)
    - [3.2. Enable Nacos](#32-enable-nacos)
    - [3.3. Enable configuration service](#33-enable-configuration-service)
    - [3.4. Enable Service Discovery](#34-enable-service-discovery)
- [4. Core Features](#4-core-features)
    - [4.1. Annotation-Driven](#41-annotation-driven)
        - [4.1.1. Enable Nacos](#411-enable-nacos)
        - [4.1.2. Configure Change Listener method](#412-configure-change-listener-method)
            - [4.1.2.1. Type Conversion](#4121-type-conversion)
            - [4.1.2.2. Timeout of Execution](#4122-timeout-of-execution)
        - [4.1.3. Global and Special Nacos Properties](#413-global-and-special-nacos-properties)
        - [4.1.4. `@NacosProperties`](#414-nacosproperties)
    - [4.2. Dependency Injection](#42-dependency-injection)
    - [4.3. Externalized Configuration](#43-externalized-configuration)
    - [4.4. Event/Listener Driven](#44-eventlistener-driven)
- [5. Modules](#5-modules)
- [6. Relative Projects](#6-relative-projects)

<!-- /TOC -->


# 1. Samples

Included in this section are some samples for you to get a quick start with Nacos Spring.

## 1.1. Samples

- [Dependency Injection Sample of `@NacosInjected`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/NacosConfiguration.java)

- [Simple Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/SimpleNacosConfigListener.java)

- [Type Conversion Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/PojoNacosConfigListener.java)

- [Timeout Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/TimeoutNacosConfigListener.java)

- [Sample of `@NacosConfigurationProperties`/`@NacosProperty`/`@NacosIgnore`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/properties/NacosConfigurationPropertiesConfiguration.java)

- [Sample of `@NacosPropertySources`/`@NacosPropertySource`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/env/NacosPropertySourceConfiguration.java)

- [Event/Listener Sample](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/event/NacosEventListenerConfiguration.java)

## 1.2. How To Run the Samples

Take the Spring Web MVC project for example:

1. Check out the source code of `nacos-spring-project` :
   
	` $ git clone git@github.com:nacos-group/nacos-spring-project.git`

2. Build your source code with Maven:
    
	`$ mvn clean package`
    
3. Run Spring Web MVC Samples:

      `$ java -jar target/nacos-spring-webmvc-sample.war`

# 2. Dependencies & Compatibility

The following table shows the dependencies and compatabilities of Nacos Spring Project.

| Dependencies   | Compatibility |
| -------------- | ------------- |
| Java           | 1.6+         |
| Spring Context | 3.2+         |
| [Alibaba Spring Context Support](https://github.com/alibaba/spring-context-support) | 1.0.1+ |
| [Alibaba Nacos](https://github.com/alibaba/nacos) | 1.1.1+ |




# 3. Quickstart

This quickstart shows you how to enable Nacos and its service discovery and configuration management features in your Spring project.

## 3.1. Prerequisite

Before you configure your Spring project to use Nacos, you need to start a Nacos server in the backend. Refer to [Nacos Quick Start](https://nacos.io/en-us/docs/quick-start.html) for instructions on how to start a Nacos server.

## 3.2. Enable Nacos
Complete the following steps to enable Nacos for your Spring project.

1. Add [`nacos-spring-context`](nacos-spring-context) in your Spring application's dependencies:

	```xml
	    <dependencies>
	        ...
	        
	        <dependency>
	            <groupId>com.alibaba.nacos</groupId>
	            <artifactId>nacos-spring-context</artifactId>
	            <version>0.3.1</version>
	        </dependency>
	        
	        ...
	    </dependencies>
	```

**Note:** Support Spring 5 from version 0.2.3-RC1.

2. Add the `@EnableNacos` annotation in the `@Configuration` class of Spring and specify "\${host}:${port}" of your Nacos server in the `serverAddr` attribute:

	```java
	@Configuration
	@EnableNacos(
	        globalProperties =
	        @NacosProperties(serverAddr = "${nacos.server-addr:localhost:12345}")
	)
	public class NacosConfiguration {
	    ...
	}
	```


## 3.3. Enable configuration service
Now you would like to use the confguration service of Nacos. Simply use **Dependency Injection** to inject `ConfigService` instance in your Spring Beans when `@EnableNacos` is annotated, as shown below: 

```java
@Service
public class ConfigServiceDemo {

    @NacosInjected
    private ConfigService configService;
    
    public void demoGetConfig() {
        try {
            String dataId = "{dataId}";
            String group = "{group}";
            String content = configService.getConfig(dataId, groupId, 5000);
        	System.out.println(content);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }
    ...
}
```

`ConfigService` is the key interface of Nacos which helps you to get or publish configurations.

The following code achieves the same effect: 

```java
try {
    // Initialize the configuration service, and the console automatically obtains the following parameters through the sample code.
	String serverAddr = "{serverAddr}";
	String dataId = "{dataId}";
	String group = "{group}";
	Properties properties = new Properties();
	properties.put("serverAddr", serverAddr);
	ConfigService configService = NacosFactory.createConfigService(properties);
    // Actively get the configuration.
	String content = configService.getConfig(dataId, group, 5000);
	System.out.println(content);
} catch (NacosException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
}
```
## 3.4. Enable Service Discovery
If you would also like to use the service discovery feature of Nacos, you can inject a `NamingService` instance for service discovery:

```java
    @NacosInjected
    private NamingService namingService;
```

For details about the usages of `ConfigService` and `NamingService`, please refer to [Nacos SDK](https://nacos.io/en-us/docs/sdk.html).



# 4. Core Features

This section provides a detailed description of the key features of [`nacos-spring-context`](nacos-spring-context):

- [Annotation-Driven](#annotation-driven)
- [Dependency Injection](#dependency-injection)
- [Externalized Configuration](#externalized-configuration)
- [Event-Driven](#eventlistener-driven)



## 4.1. Annotation-Driven

### 4.1.1. Enable Nacos

`@EnableNacos` is a modular-driven annotation that enables all features of Nacos Spring, including **Service Discovery** and **Distributed Configuration**. It equals to  `@EnableNacosDiscovery` and 
`@EnableNacosConfig`, which can be configured separately and used in different scenarios.

### 4.1.2. Configure Change Listener method

Suppose there was a config in Nacos Server whose `dataId` is "testDataId" and `groupId` is default group("DEFAULT_GROUP"). Now you would like to change its content by using the `ConfigService#publishConfig` method:

```java
    @NacosInjected
    private ConfigService configService;

    @Test
    public void testPublishConfig() throws NacosException {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
    }
```

Then you would like to add a listener, which will be listening for the config changes. You can do this by adding a config change listener method into your Spring Beans:

```java
    @NacosConfigListener(dataId = DATA_ID)
    public void onMessage(String config) {
        assertEquals("mercyblitz", config); // asserts true
    }
```

The code below has the same effect:

```java
	configService.addListener(DATA_ID, DEFAULT_GROUP, new AbstractListener() {
        @Override
        public void receiveConfigInfo(String config) {
            assertEquals("9527", config); // asserts true
        }
    });
```

**Note:** `@NacosConfigListener` supports richer type conversions.

- See [Simple Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/SimpleNacosConfigListener.java)




#### 4.1.2.1. Type Conversion

The type conversion of `@NacosConfigListener` includes both build-in and customized implementations. By default, build-in type conversion is based on Spring `DefaultFormattingConversionService`, which means it covers most of the general cases as well as the rich features of the higher Spring framework. 

For example, the content "9527" in the preceding example can also be listened by a method with integer or the `Integer` argument:

```java
    @NacosConfigListener(dataId = DATA_ID)
    public void onInteger(Integer value) {
        assertEquals(Integer.valueOf(9527), value); // asserts true
    }

    @NacosConfigListener(dataId = DATA_ID)
    public void onInt(int value) {
        assertEquals(9527, value); // asserts true
    }
```

Of course, [`nacos-spring-context`](nacos-spring-context) provides elastic extension for developers. If you define a named `nacosConfigConversionService` Spring Bean whose type is `ConversionService` , the `DefaultFormattingConversionService` will be ignored. In addition, you can customize the implementation of  the `NacosConfigConverter` interface to specify a listener method for type conversion:

```java
public class UserNacosConfigConverter implements NacosConfigConverter<User> {

    @Override
    public boolean canConvert(Class<User> targetType) {
        return true;
    }

    @Override
    public User convert(String source) {
        return JSON.parseObject(source, User.class);
    }
}
```

The `UserNacosConfigConverter` class binds the `@NacosConfigListener.converter()` attribute:

```java
	@NacosInjected
    private ConfigService configService;

	@Test
    public void testPublishUser() throws NacosException {
        configService.publishConfig("user", DEFAULT_GROUP, "{\"id\":1,\"name\":\"mercyblitz\"}");
    }

    @NacosConfigListener(dataId = "user", converter = UserNacosConfigConverter.class)
    public void onUser(User user) {
        assertEquals(Long.valueOf(1L), user.getId()); 
        assertEquals("mercyblitz", user.getName());
    }
```




- See [Type Conversion Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/PojoNacosConfigListener.java)




#### 4.1.2.2. Timeout of Execution

As it might cost some time to run customized `NacosConfigConverter`, you can set  max execution time in the `@NacosConfigListener.timeout()` attribute to prevent it from blocking other listeners:

```java
@Configuration
public class Listeners {

    private Integer integerValue;

    private Double doubleValue;

    @NacosConfigListener(dataId = DATA_ID, timeout = 50)
    public void onInteger(Integer value) throws Exception {
        Thread.sleep(100); // timeout of execution
        this.integerValue = value;
    }

    @NacosConfigListener(dataId = DATA_ID, timeout = 200)
    public void onDouble(Double value) throws Exception {
        Thread.sleep(100); // normal execution
        this.doubleValue = value;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }
}
```

The `integerValue` of `Listeners` Bean is always `null` and will not be changed. Therefore, those asserts will be `true`:

```java
    @Autowired
    private Listeners listeners;

    @Test
    public void testPublishConfig() throws NacosException {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        assertNull(listeners.getIntegerValue()); // asserts true
        assertEquals(Double.valueOf(9527), listeners.getDoubleValue());   // asserts true
    }
```




- See [Timeout Sample of `@NacosConfigListener`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/listener/TimeoutNacosConfigListener.java)

### 4.1.3. Global and Special Nacos Properties

The `globalProperties` is a required attribute in any `@EnableNacos`, `@EnableNacosDiscovery` or `@EnableNacosConfig`, and its type is `@NacosProperties`. `globalProperties` initializes "**Global Nacos Properties**" that will be used by other annotations 
and components, e,g `@NacosInjected`. In other words, **Global Nacos Properties**" defines the global and default properties. It is set with the lowest priority and can be overridden if needed. The precedence of overiding rules is shown in the following table:

| Precedence Order | Nacos Annotation                                             | Required |
| ---------------- | ------------------------------------------------------------ | -------- |
| 1                | `*.properties()`                                             | N        |
| 2                | `@EnableNacosConfig.globalProperties()` or `@EnableNacosDiscovery.globalProperties()` | Y        |
| 3                | `@EnableNacos.globalProperties()`                            | Y        |


`*.properties()` defines special Nacos properties which come from one of the following:  

- `@NacosInjected.properties()` 
- `@NacosConfigListener.properties()`
- `@NacosPropertySource.properties()` 
- `@NacosConfigurationProperties.properties()`

Special Nacos properties are also configured by `@NacosProperties`. However, they are optional and are used to override Global Nacos Properties in special scenarios. If not defined, the Nacos Properties will 
try to retrieve properities from `@EnableNacosConfig.globalProperties()` or `@EnableNacosDiscovery.globalProperties()`, or 
`@EnableNacos.globalProperties()`.



### 4.1.4. `@NacosProperties`

`@NacosProperties` is a uniform annotation for global and special Nacos properties. It serves as a mediator between Java `Properties` and `NacosFactory` class.   `NacosFactory` is responsible for creating `ConfigService` or `NamingService` instances. 

The attributes of `@NacosProperties` completely support placeholders whose source is all kinds of `PropertySource` in Spring `Environment` abstraction, typically Java System `Properties` and OS environment variables. The prefix of all placeholders are `nacos.`.  The mapping between the attributes of `@NacosProperties` and Nacos properties are shown below: 

| Attribute       | Property       | Placeholder              | Description | Required  |
| --------------- | -------------- | ------------------------ | ----------- | --------- |
| `endpoint()`    | `endpoint`     | `${nacos.endpoint:}`     |             |     N     |
| `namespace()`   | `namespace`    | `${nacos.namespace:}`    |             |     N     |
| `accessKey()`   | `access-key`   | `${nacos.access-key:}`   |             |     N     |
| `secretKey()`   | `secret-key`   | `${nacos.secret-key:}`   |             |     N     |
| `serverAddr()`  | `server-addr`  | `${nacos.server-addr:}`  |             |     Y     |
| `contextPath()` | `context-path` | `${nacos.context-path:}` |             |     N     |
| `clusterName()` | `cluster-name` | `${nacos.cluster-name:}` |             |     N     |
| `encode()`      | `encode`       | `${nacos.encode:UTF-8}`  |             |     N     |


Note that there are some differences in the placeholders of `globalProperties()` between `@EnableNacosDiscovery` and `@EnableNacosConfig`:


| Attribute       | `@EnableNacosDiscovery`'s Placeholder                     |`@EnableNacosConfig`'s Placeholder  |
| --------------- | -------------------------------------------------------- | -------------------------------------------------    |
| `endpoint()`    | `${nacos.discovery.endpoint:${nacos.endpoint:}}`         |`${nacos.config.endpoint:${nacos.endpoint:}}`         |
| `namespace()`   | `${nacos.discovery.namespace:${nacos.namespace:}}`       |`${nacos.config.namespace:${nacos.namespace:}}`       |
| `accessKey()`   | `${nacos.discovery.access-key:${nacos.access-key:}}`     |`${nacos.config.access-key:${nacos.access-key:}}`     |
| `secretKey()`   | `${nacos.discovery.secret-key:${nacos.secret-key:}}`     |`${nacos.config.secret-key:${nacos.secret-key:}}`     |
| `serverAddr()`  | `${nacos.discovery.server-addr:${nacos.server-addr:}}`   | `${nacos.config.server-addr:${nacos.server-addr:}}`   |
| `contextPath()` | `${nacos.discovery.context-path:${nacos.context-path:}}` | `${nacos.config.context-path:${nacos.context-path:}}` |
| `clusterName()` | `${nacos.discovery.cluster-name:${nacos.cluster-name:}}` |`${nacos.config.cluster-name:${nacos.cluster-name:}}` |
| `encode()`      | `${nacos.discovery.encode:${nacos.encode:UTF-8}}`        |`${nacos.config.encode:${nacos.encode:UTF-8}}`        |



These placeholders of `@EnableNacosDiscovery` and `@EnableNacosConfig` are designed to isolate different Nacos servers, and are unnecessary in most scenarios.  By default, general placeholders will be reused.




## 4.2. Dependency Injection

`@NacosInjected` is a core annotation which is used to inject `ConfigService` or `NamingService` instance in your Spring Beans and make these instances **cacheable**. This means the instances will be the same if their `@NacosProperties` are equal, regargless of whether the properties come from global or special Nacos properties:

```java
    @NacosInjected
    private ConfigService configService;

    @NacosInjected(properties = @NacosProperties(encode = "UTF-8"))
    private ConfigService configService2;

    @NacosInjected(properties = @NacosProperties(encode = "GBK"))
    private ConfigService configService3;

    @NacosInjected
    private NamingService namingService;

    @NacosInjected(properties = @NacosProperties(encode = "UTF-8"))
    private NamingService namingService2;

    @NacosInjected(properties = @NacosProperties(encode = "GBK"))
    private NamingService namingService3;

    @Test
    public void testInjection() {

        Assert.assertEquals(configService, configService2);
        Assert.assertNotEquals(configService2, configService3);

        Assert.assertEquals(namingService, namingService2);
        Assert.assertNotEquals(namingService2, namingService3);
    }
```

The property `configService` uses `@EnableNacos#globalProperties()` or `@EnableNacosConfig#globalProperties()`, and because the default value of the `encode` attribute is “UTF-8”, therefore the `configService` instance and the `configService2` instance which is annotated by `@NacosProperties(encode = "UTF-8")` are the same. The same is true for `namingService` and `namingService2`.

More importantly, unlike the `ConfigService` instances created by the `NacosFactory.createConfigService()` method, the `ConfigService` instances created by the `@NacosInjected` annotation support Nacos Spring events. For instance, there will be an `NacosConfigPublishedEvent`  after an enhanced `ConfigService` invokes the `publishConfig()` method. Refer to the [Event/Listener Driven](#eventlistener-driven) section for more details.




- See [Dependency Injection Sample](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/NacosConfiguration.java)




## 4.3. Externalized Configuration

Externalized configuration is a concept introduced by Spring Boot, which allows applications to receive external property sources to control runtime behavior. Nacos Server runs an isolation process outside the application to maintain the application configurations. [`nacos-spring-context`](nacos-spring-context) provides properties features including object binding, dynamic configuration(auto-refreshed) and so on, and dependence on Spring Boot or Spring Cloud framework is required.

Here is a simple comparison between  [`nacos-spring-context`](nacos-spring-context) and Spring stack:

| Spring Stack               | Nacos Spring                    | Highlight                                      |
| -------------------------- | ------------------------------- | ---------------------------------------------- |
| `@Value`                   | `@NacosValue`                   | auto-refreshed                                 |
| `@ConfigurationProperties` | `@NacosConfigurationProperties` | auto-refreshed,`@NacosProperty`,`@NacosIgnore` |
| `@PropertySource`          | `@NacosPropertySource`          | auto-refreshed, precedence order control       |
| `@PropertySources`         | `@NacosPropertySources`         |                                                |




- See [Auto-Refreshed Sample of `@NacosConfigurationProperties`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/properties/NacosConfigurationPropertiesConfiguration.java)



- See [Sample of `@NacosPropertySources` and `@NacosPropertySource`](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/env/NacosPropertySourceConfiguration.java)



## 4.4. Event/Listener Driven

Nacos Event/Listener Driven is based on the standard Spring Event/Listener mechanism. The `ApplicationEvent` of Spring is an abstract super class for all Nacos Spring events:

| Nacos Spring Event                           | Trigger                                                      |
| -------------------------------------------- | ------------------------------------------------------------ |
| `NacosConfigPublishedEvent`                  | After `ConfigService.publishConfig()`                        |
| `NacosConfigReceivedEvent`                   | After`Listener.receiveConfigInfo()`                          |
| `NacosConfigRemovedEvent`                    | After `configService.removeConfig()`                         |
| `NacosConfigTimeoutEvent`                    | `ConfigService.getConfig()` on timeout                       |
| `NacosConfigListenerRegisteredEvent`         | After `ConfigService.addListner()` or `ConfigService.removeListener()` |
| `NacosConfigurationPropertiesBeanBoundEvent` | After `@NacosConfigurationProperties` binding                |
| `NacosConfigMetadataEvent`                   | After Nacos Config operations                                |

- See [Event/Listener Sample](https://github.com/nacos-group/nacos-spring-project/blob/master/nacos-spring-samples/nacos-spring-webmvc-sample/src/main/java/com/alibaba/nacos/samples/spring/event/NacosEventListenerConfiguration.java)







# 5. Modules

- [`nacos-spring-context`](nacos-spring-context)
  
- [`nacos-spring-samples`](nacos-spring-samples)



# 6. Relative Projects

- [Alibaba Nacos](https://github.com/alibaba/nacos)
- [Alibaba Spring Context Support](https://github.com/alibaba/spring-context-support)
