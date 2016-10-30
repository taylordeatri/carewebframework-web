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
package org.carewebframework.web.client;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.web.component.Page;

public class ExecutionContext {
    
    public static final String ATTR_REQUEST = "cwf_request";
    
    private static final ThreadLocal<Map<String, Object>> context = new InheritableThreadLocal<Map<String, Object>>() {
        
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };
    
    public static Object put(String key, Object value) {
        return context.get().put(key, value);
    }
    
    public static Object get(String key) {
        return context.get().get(key);
    }
    
    public static Object remove(String key) {
        return context.get().remove(key);
    }
    
    public static void clear() {
        context.get().clear();
    }
    
    public static boolean isEmpty() {
        return context.get().isEmpty();
    }
    
    public static ClientRequest getRequest() {
        return (ClientRequest) context.get().get(ATTR_REQUEST);
    }
    
    public static Session getSession() {
        ClientRequest request = getRequest();
        return request == null ? null : request.getSession();
    }
    
    public static Page getPage() {
        ClientRequest request = getRequest();
        return request == null ? null : request.getPage();
    }
}
