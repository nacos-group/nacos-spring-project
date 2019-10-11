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
package com.alibaba.nacos.spring.util;

import java.util.Properties;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public interface ConfigParse {

    /**
     * parse config context to map
     *
     * @param configText receive config context
     * @return {@link Properties}
     */
    Properties parse(String configText);

    /**
     * get this ConfigParse process config type
     *
     * @return this parse process type
     */
    String processType();

    /**
     * get config dataId
     *
     * @return dataId
     */
    String dataId();

    /**
     * get config group
     *
     * @return group
     */
    String group();

}
