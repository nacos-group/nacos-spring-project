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
package com.alibaba.nacos.spring.test;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;

import java.util.Map;

import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@NacosConfigurationProperties(dataId = DATA_ID, groupId = GROUP_ID, ignoreNestedProperties = true,
        autoRefreshed = true, yaml = true)
public class TestOrder {

    private String userName;
    private String address;
    private String phone;
    private String fOrderCreateTime;
    private String userUUID;
    private String fOrderCost;
    private String fOrderCostFake;
    private String fOrderMailCost;
    private String fOrderSubPrice;
    private String wxFdOrders;
    private Map<String, String> fdOrderFoodId;
    private String addId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getfOrderCreateTime() {
        return fOrderCreateTime;
    }

    public void setfOrderCreateTime(String fOrderCreateTime) {
        this.fOrderCreateTime = fOrderCreateTime;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getfOrderCost() {
        return fOrderCost;
    }

    public void setfOrderCost(String fOrderCost) {
        this.fOrderCost = fOrderCost;
    }

    public String getfOrderCostFake() {
        return fOrderCostFake;
    }

    public void setfOrderCostFake(String fOrderCostFake) {
        this.fOrderCostFake = fOrderCostFake;
    }

    public String getfOrderMailCost() {
        return fOrderMailCost;
    }

    public void setfOrderMailCost(String fOrderMailCost) {
        this.fOrderMailCost = fOrderMailCost;
    }

    public String getfOrderSubPrice() {
        return fOrderSubPrice;
    }

    public void setfOrderSubPrice(String fOrderSubPrice) {
        this.fOrderSubPrice = fOrderSubPrice;
    }

    public String getWxFdOrders() {
        return wxFdOrders;
    }

    public void setWxFdOrders(String wxFdOrders) {
        this.wxFdOrders = wxFdOrders;
    }

    public Map<String, String> getFdOrderFoodId() {
        return fdOrderFoodId;
    }

    public void setFdOrderFoodId(Map<String, String> fdOrderFoodId) {
        this.fdOrderFoodId = fdOrderFoodId;
    }

    public String getAddId() {
        return addId;
    }

    public void setAddId(String addId) {
        this.addId = addId;
    }

    @Override
    public String toString() {
        return "TestOrder{" +
                "userName='" + userName + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", fOrderCreateTime='" + fOrderCreateTime + '\'' +
                ", userUUID='" + userUUID + '\'' +
                ", fOrderCost='" + fOrderCost + '\'' +
                ", fOrderCostFake='" + fOrderCostFake + '\'' +
                ", fOrderMailCost='" + fOrderMailCost + '\'' +
                ", fOrderSubPrice='" + fOrderSubPrice + '\'' +
                ", wxFdOrders='" + wxFdOrders + '\'' +
                ", fdOrderFoodId=" + fdOrderFoodId +
                ", addId='" + addId + '\'' +
                '}';
    }
}
