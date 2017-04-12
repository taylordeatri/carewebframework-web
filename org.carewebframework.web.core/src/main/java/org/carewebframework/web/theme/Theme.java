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

import java.util.Map.Entry;
import java.util.Properties;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Theme implementation that can pull from multiple sources.
 */
public class Theme {

    private final String name;

    private final ObjectNode config;

    private final ObjectNode paths;
    
    public Theme(String name, ObjectNode config) {
        this.name = name;
        this.config = config;
        this.paths = (ObjectNode) config.get("paths");
    }
    
    public String getName() {
        return name;
    }

    protected void mergeConfig(Properties props) {
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            if (value.isEmpty()) {
                paths.remove(key);
            } else {
                ArrayNode node = paths.arrayNode();
                node.add(value);
                paths.replace(key, node);
            }
        }
    }
    
    public String getWebJarInit() {
        return config.toString();
    }

}
