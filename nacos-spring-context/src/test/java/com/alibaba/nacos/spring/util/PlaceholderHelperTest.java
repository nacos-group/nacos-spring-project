package com.alibaba.nacos.spring.util;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author whq
 * @since 1.1.2
 */
public class PlaceholderHelperTest {

    @Test
    public void testFindPlaceholderKeys() {
        /**
         * find keys from placeholder
         * ${key} => "key"
         * xxx${key}yyy => "key"
         * ${key:${key2:1}} => "key", "key2"
         * ${${key}} => "key"
         * ${${key:100}} => "key"
         * ${${key}:${key2}} => "key", "key2"
         */
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("${key}"), Sets.newHashSet("key"));
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("xxx${key}yyy"), Sets.newHashSet("key"));
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("${key:${key2:1}}"), Sets.newHashSet("key", "key2"));
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("${${key}}"), Sets.newHashSet("key"));
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("${${key:100}}"), Sets.newHashSet("key"));
        Assert.assertEquals(PlaceholderHelper.findPlaceholderKeys("${${key}:${key2}}"), Sets.newHashSet("key", "key2"));

    }

}
