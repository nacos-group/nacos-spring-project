/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.samples.spring.webmvc;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Nacos {@link ConfigService} {@link Controller}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Controller
public class ConfigServiceController {

    @NacosValue(value = "${people.enable:false}", autoRefreshed = true)
    private String enable;

    @NacosInjected
    private ConfigService configService;

    @RequestMapping(value = "/get", method = GET)
    @ResponseBody
    public String get(@RequestParam String dataId, @RequestParam(defaultValue = DEFAULT_GROUP) String groupId) throws NacosException {
        return configService.getConfig(dataId, groupId, TimeUnit.SECONDS.toMillis(1));
    }

    @RequestMapping()
    @ResponseBody
    public String value() {
        return enable;
    }

    @RequestMapping(value = "/publish", method = POST)
    @ResponseBody
    public boolean publish(@RequestParam String dataId, @RequestParam(defaultValue = DEFAULT_GROUP) String groupId,
                           @RequestParam String content) throws NacosException {
        return configService.publishConfig(dataId, groupId, content);
    }
}
