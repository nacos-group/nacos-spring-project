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
package com.alibaba.nacos.spring.context.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.PropertySource;
import org.w3c.dom.Element;

/**
 * Nacos {@link PropertySource} XML {@link BeanDefinition}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosPropertySourceXmlBeanDefinition extends GenericBeanDefinition {

    private Element element;

    private XmlReaderContext xmlReaderContext;

    public NacosPropertySourceXmlBeanDefinition() {
        // Self type as Bean Class
        setBeanClass(getClass());
    }

    void setXmlReaderContext(XmlReaderContext xmlReaderContext) {
        this.xmlReaderContext = xmlReaderContext;
    }

    void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public XmlReaderContext getXmlReaderContext() {
        return xmlReaderContext;
    }

}
