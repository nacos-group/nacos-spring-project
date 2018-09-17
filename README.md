# Nacos Spring Project

[![Build Status](https://travis-ci.org/nacos-group/nacos-spring-project.svg?branch=master)](https://travis-ci.org/nacos-group/nacos-spring-project)

[Alibaba Nacos](https://github.com/alibaba/nacos) ships main core features of Cloud-Native application, 
including:

- Service Discovery and Service Health Check
- Dynamic Configuration Management
- Dynamic DNS Service
- Service and MetaData Management

[Nacos Spring Project](https://github.com/nacos-group/nacos-spring-project) is based on [it](https://github.com/alibaba/nacos) and embraces Spring ECO System so that developers could build Spring application rapidly. [`nacos-spring-context`](nacos-spring-context) is a core module that fully expands modern Java programming models:

- Annotation-Driven
- Externalized Configuration 
- Dependency Injection
- Event-Driven

Those features strongly depends Spring Framework 3.2+ API and seamlessly integrates any Spring Stack. 

We recommend developers to use annotation-driven programming, even though XML-based features also work.




## Dependencies & Compatibility

| Dependencies   | Compatibility |
| -------------- | ------------- |
| Java           | 1.6+         |
| Spring Context | 3.2+         |
| [Alibaba Spring Context Support](https://github.com/alibaba/spring-context-support) | 1.0.1+ |
| [Alibaba Nacos](https://github.com/alibaba/nacos) | 0.2.1+ |




## Quick Start

First, you have to start a Nacos Server in backend , If you don't know steps, you can learn about [quick start](https://nacos.io/en-us/docs/quick-start.html).

Suppose your Nacos Server is startup, you would add [`nacos-spring-context`](nacos-spring-context) in your  Spring application's dependencies :

```xml
    <dependencies>
        ...
        
        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-spring-context</artifactId>
            <version>${latest.version}</version>
        </dependency>
        
        ...
    </dependencies>
```

After that, you could annotate `@EnableNacos` in Spring `@Configuration` Class :

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

> `serverAddr` attribute configures "\${host}:${port}" of your Nacos Server

If you'd like to use "Distributed Configuration" features, `ConfigService` is a core service interface to get or publish config, you could use "Dependency Injection" to inject `ConfigService` instance in your Spring Beans when `@EnableNacos` is annotated:

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

above code equals below one: 

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

However, `@NacosInjected` will enhance the `ConfigService` instances that make them to be **cacheable** and **event-driven**, and also can inject any `NamingService` instance for "Service Discovery" scenario:

```java
    @NacosInjected
    private NamingService namingService;
```

If you were not familiar with usages of `ConfigService` and `NamingService`, please learn about [SDK](https://nacos.io/en-us/docs/sdk.html) first.



## Core Features

[`nacos-spring-context`](nacos-spring-context) is a core module of Nacos integrating with Spring Framework, which provides much rich features around Spring Stack, including Spring Boot and Spring Cloud. Their core features include:

- Annotation-Driven
- Externalized Configuration 
- Dependency Injection
- Event-Driven




### Annotation-Driven 




#### Enable Modular-Driven

`@EnableNacos` is a modular-driven annotation that enables all features of Nacos Spring, mainly includes "**Service Discovery**" and  "**Distributed Configuration**", it is equals to  `@EnableNacosDiscovery` and 
`@EnableNacosConfig`, they also could be configured separately, that means you could use them in different scenarios.




#### Global and Special Nacos Properties

The `globalProperties` is a required attribute in any `@EnableNacos`, `@EnableNacosDiscovery` or `@EnableNacosConfig`, 
whose type is `@NacosProperties`, which initializes "**Global Nacos Properties**" that will be used other annotations 
and components, e,g `@NacosInjected`. In other words, **Global Nacos Properties**" that is a global or default properties 
with lowest order can be override if need be. The precedence rules of override:

| Precedence Order | Nacos Annotation                                             | Required |
| ---------------- | ------------------------------------------------------------ | -------- |
| 1                | `*.proeprties()`                                             | N        |
| 2                | `@EnableNacosConfig.globalProperties()` or `@EnableNacosDiscovery.globalProperties()` | Y        |
| 3                | `@EnableNacos.globalProperties()`                            | Y        |


`*.proeprties()` is a Special Nacos Properties that comes from one of them :  

- `@NacosInjected.proeprties()` 
- `@NacosConfigListener.proeprties()`
- `@NacosPropertySource.proeprties()` 
- `@NacosConfigurationProperties.proeprties()`

Special Nacos Properties is also configured by `@NacosProperties`, however it's optional and used to override Global 
Nacos Properties for special scenarios, even though it is not required. If they are absent, The Nacos Properties will 
try to retrieve from `@EnableNacosConfig.globalProperties()` or `@EnableNacosDiscovery.globalProperties()`, or 
`@EnableNacos.globalProperties()`.



#### `@NacosProperties`

`@NacosProperties` is an uniform annotation of Global and Special Nacos Properties, which plays a mediator between Java `Properties` and `NacosFactory` class creating the instances of `ConfigService` or `NamingService`. 

`@NacosProperties`‘s attributes completely support placeholders whose source is all kinds of `PropertySource` in Spring `Environment` abstraction, typically Java System `Properties` and OS environment variables. The prefix of all placeholders are "`nacos.`", the mapping between `@NacosProperties`'s attributes and Nacos Properties as below: 

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


There are some different in `globalProperties()`'s placeholders between `@EnableNacosDiscovery` and `@EnableNacosConfig`:

| Attribute       | `@EnableNacosDiscovery`'s Placeholder                     |
| --------------- | -------------------------------------------------------- |
| `endpoint()`    | `${nacos.discovery.endpoint:${nacos.endpoint:}}`         |
| `namespace()`   | `${nacos.discovery.namespace:${nacos.namespace:}}`       |
| `accessKey()`   | `${nacos.discovery.access-key:${nacos.access-key:}}`     |
| `secretKey()`   | `${nacos.discovery.secret-key:${nacos.secret-key:}}`     |
| `serverAddr()`  | `${nacos.discovery.server-addr:${nacos.server-addr:}}`   |
| `contextPath()` | `${nacos.discovery.context-path:${nacos.context-path:}}` |
| `clusterName()` | `${nacos.discovery.cluster-name:${nacos.cluster-name:}}` |
| `encode()`      | `${nacos.discovery.encode:${nacos.encode:UTF-8}}`        |



| Attribute       | `@EnableNacosConfig`'s Placeholder                     |
| --------------- | ----------------------------------------------------- |
| `endpoint()`    | `${nacos.config.endpoint:${nacos.endpoint:}}`         |
| `namespace()`   | `${nacos.config.namespace:${nacos.namespace:}}`       |
| `accessKey()`   | `${nacos.config.access-key:${nacos.access-key:}}`     |
| `secretKey()`   | `${nacos.config.secret-key:${nacos.secret-key:}}`     |
| `serverAddr()`  | `${nacos.config.server-addr:${nacos.server-addr:}}`   |
| `contextPath()` | `${nacos.config.context-path:${nacos.context-path:}}` |
| `clusterName()` | `${nacos.config.cluster-name:${nacos.cluster-name:}}` |
| `encode()`      | `${nacos.config.encode:${nacos.encode:UTF-8}}`        |


Such placeholders of `@EnableNacosDiscovery` and `@EnableNacosConfig` are designed to isolate different Nacos servers, maybe a lot of scenarios are unnecessary, as well as re-use general placeholders as default.



#### Config Change Listener method

Suppose there was a config in Nacos Server whose `dataId` is "testDataId" and `groupId` is default group("DEFAULT_GROUP"), and then you'd like to change its' content by `ConfigService#publishConfig` method:

```java
    @NacosInjected
    private ConfigService configService;

    @Test
    public void testPublishConfig() throws NacosException {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
    }
```

This new content should be notified in somewhere, and your could add a config change listener method into your Spring Beans:

```java
    @NacosConfigListener(dataId = DATA_ID)
    public void onMessage(String config) {
        assertEquals("mercyblitz", config); // asserts true
    }
```

It's equals to below code :

```java
	configService.addListener(DATA_ID, DEFAULT_GROUP, new AbstractListener() {
        @Override
        public void receiveConfigInfo(String config) {
            assertEquals("9527", config); // asserts true
        }
    });
```

Howerver `@NacosConfigListener` supports richer type-conversion feature.



##### Type Conversion

`@NacosConfigListener`'s type-conversion includes build-in and customized implementations. Default, build-in type-conversion is based on Spring `DefaultFormattingConversionService`, that means it had involved most general cases and the higher Spring framework , the richer features. Thus, the content "9527" in above example also be listened by a method with integer or `Integer` argument:

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

Of couse, [`nacos-spring-context`](nacos-spring-context) provides elastic extension for devlopers. If you define a named `nacosConfigConversionService` Spring Bean whose type is `ConversionService` , the `DefaultFormattingConversionService` will be ignored. What's more, you could customize  `NacosConfigConverter` interface's implementation to specify a listener method for type conversion:

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

`UserNacosConfigConverter` class binds `@NacosConfigListener.converter()` attribute:

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



##### Timeout of Execution

Customized  `NacosConfigConverter` may cost must time, thus `@NacosConfigListener.timeout()` attribute could be set timeout that limits max execution time to prevent blocking other listeners:

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

`Listeners` Bean's `integerValue` will not be changed, always `null`, thus those asserts will be `true`:

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





### Externalized Configuration



### Dependency Injection



### Event/Listener Driven





## Modules

### [`nacos-spring-context`](nacos-spring-context)  




## Relative Projects

* [Alibaba Nacos](https://github.com/alibaba/nacos)
* [Alibaba Spring Context Support](https://github.com/alibaba/spring-context-support)

