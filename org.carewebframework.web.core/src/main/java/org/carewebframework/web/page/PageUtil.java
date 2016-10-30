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
package org.carewebframework.web.page;

import java.util.List;
import java.util.Map;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.springframework.core.io.ByteArrayResource;

public class PageUtil {
    
    public static PageDefinition getPageDefinition(String url) {
        return PageDefinitionCache.getInstance().get(url);
    }
    
    public static List<BaseComponent> createPage(String url, BaseComponent parent) {
        return createPage(url, parent, null);
    }
    
    public static List<BaseComponent> createPage(String url, BaseComponent parent, Map<String, Object> args) {
        return createPage(getPageDefinition(url), parent, args);
    }
    
    public static List<BaseComponent> createPage(PageDefinition def, BaseComponent parent) {
        return createPage(def, parent, null);
    }
    
    public static List<BaseComponent> createPage(PageDefinition def, BaseComponent parent, Map<String, Object> args) {
        try {
            return def.materialize(parent, args);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    public static List<BaseComponent> createPageFromContent(String content, BaseComponent parent) {
        return createPageFromContent(content, parent, null);
    }
    
    public static List<BaseComponent> createPageFromContent(String content, BaseComponent parent, Map<String, Object> args) {
        ByteArrayResource resource = new ByteArrayResource(content.getBytes());
        return createPage(PageParser.getInstance().parse(resource), parent, args);
    }
    
    private PageUtil() {
        
    }
}
