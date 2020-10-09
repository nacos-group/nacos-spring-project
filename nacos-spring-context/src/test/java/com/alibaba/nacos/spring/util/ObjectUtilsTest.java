package com.alibaba.nacos.spring.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ObjectUtilsTest {

	@Test
	public void test_map_clean() {
		TestObj obj = new TestObj();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		obj.setMap(map);
		Assert.assertNotNull(map.get("key"));
		Assert.assertNotNull(obj.map);
		ObjectUtils.cleanMapOrCollectionField(obj);
		map = obj.map;
		Assert.assertNull(map);
	}

	private static class TestObj {

		private Map<String, Object> map;

		public Map<String, Object> getMap() {
			return map;
		}

		public void setMap(Map<String, Object> map) {
			this.map = map;
		}
	}

}
