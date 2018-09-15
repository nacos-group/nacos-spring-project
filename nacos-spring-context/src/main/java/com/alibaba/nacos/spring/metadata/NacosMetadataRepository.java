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
package com.alibaba.nacos.spring.metadata;

import com.alibaba.nacos.spring.metadata.config.NacosConfigMetadataSubscriber;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Nacos meta-data Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosMetadataRepository implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * The bean name of {@link NacosMetadataRepository}
     */
    public static final String BEAN_NAME = "nacosMetadataRepository";

    private final Collection<NacosConfigMetadataSubscriber> subscribers = new LinkedList<NacosConfigMetadataSubscriber>();

    /**
     * Get all {@link NacosConfigMetadataSubscriber meta-data} from subscribers.
     *
     * @return ready-only non-null
     */
    public Collection<NacosConfigMetadataSubscriber> getSubscribers() {
        return Collections.unmodifiableCollection(subscribers);
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

    }
}
