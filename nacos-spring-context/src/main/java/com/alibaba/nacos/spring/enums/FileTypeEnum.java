/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.spring.enums;

import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Config file type enum.
 *
 */
public enum FileTypeEnum {
    
    /**
     * Yaml file.
     */
    YML("yaml"),
    
    /**
     * Yaml file.
     */
    YAML("yaml"),
    
    /**
     * Text file.
     */
    TXT("text"),
    
    /**
     * Text file.
     */
    TEXT("text"),
    
    /**
     * Json file.
     */
    JSON("json"),
    
    /**
     * Xml file.
     */
    XML("xml"),
    
    /**
     * Html file.
     */
    HTM("html"),
    
    /**
     * Html file.
     */
    HTML("html"),
    
    /**
     * Properties file.
     */
    PROPERTIES("properties");
    
    /**
     * File type corresponding to file extension.
     */
    private String fileType;
    
    FileTypeEnum(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFileType() {
        return this.fileType;
    }
    
    
    /**
     * Get the corresponding FileTypeEnum by file extension or fileType. If not found FileTypeEnum.TEXT is returned
     *
     * @param extOrFileType file extension or fileType
     * @return
     */
    public static FileTypeEnum getFileTypeEnumByFileExtensionOrFileType(String extOrFileType) {
        if (StringUtils.isNotBlank(extOrFileType)) {
            String upperExtName = extOrFileType.trim().toUpperCase();
            for (FileTypeEnum value : VALUES) {
                if (value.name().equals(upperExtName)) {
                    return value;
                }
            }
        }
        return FileTypeEnum.TEXT;
    }
    
    private static final FileTypeEnum[] VALUES = FileTypeEnum.values();
}
