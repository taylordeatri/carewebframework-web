/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
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
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.web.theme;

import org.carewebframework.common.JSONUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Theme implementation that can pull from multiple sources.
 */
public class Theme {

    private final String name;

    private final ObjectNode config;
    
    public Theme(String name, ObjectNode config) {
        this.name = name;
        this.config = config;
    }
    
    public String getName() {
        return name;
    }

    protected void mergeConfig(JsonNode config) {
        JSONUtil.merge(this.config, config);
    }
    
    public String getWebJarInit() {
        return config.toString();
    }

}