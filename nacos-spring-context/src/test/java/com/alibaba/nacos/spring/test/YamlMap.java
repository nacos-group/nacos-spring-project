package com.alibaba.nacos.spring.test;

import java.util.List;
import java.util.Map;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2020/1/12 3:54 下午
 */
@NacosConfigurationProperties(dataId = "yaml_map.yml", autoRefreshed = true, ignoreNestedProperties = true, type = ConfigType.YAML)
public class YamlMap {

	private List<String> routingMap;
	private List<String> endPointMap;
	private Map<String, String> testMap;

	public List<String> getRoutingMap() {
		return routingMap;
	}

	public void setRoutingMap(List<String> routingMap) {
		this.routingMap = routingMap;
	}

	public List<String> getEndPointMap() {
		return endPointMap;
	}

	public void setEndPointMap(List<String> endPointMap) {
		this.endPointMap = endPointMap;
	}

	public Map<String, String> getTestMap() {
		return testMap;
	}

	public void setTestMap(Map<String, String> testMap) {
		this.testMap = testMap;
	}

	@Override
	public String toString() {
		return "YamlMap{" + "routingMap=" + routingMap + ", endPointMap=" + endPointMap
				+ ", testMap=" + testMap + '}';
	}
}
