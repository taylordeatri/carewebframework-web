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
package org.carewebframework.web.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.carewebframework.common.WeakMap;
import org.carewebframework.web.ancillary.ComponentException;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.client.Synchronizer;
import org.carewebframework.web.event.EventQueue;
import org.carewebframework.web.page.PageRegistry;

@Component(value = "page", widgetClass = "Page", content = ContentHandling.AS_CHILD, childTag = @ChildTag("*"))
public final class Page extends BaseComponent implements INamespace {
    
    public static final String ID_PREFIX = "_cwf_";
    
    private static final AtomicInteger uniqueId = new AtomicInteger();
    
    private Synchronizer synchronizer;
    
    private int nextId;
    
    private final Map<String, BaseComponent> ids = new WeakMap<>();
    
    private final EventQueue eventQueue = new EventQueue(this);
    
    private final Map<String, Object> browserInfo = new HashMap<>();
    
    private String title;
    
    private final String src;
    
    public static Page _create(String src) {
        return new Page(src);
    }
    
    public static void _init(Page page, Map<String, Object> browserInfo, Synchronizer synchronizer) {
        page.synchronizer = synchronizer;
        page.browserInfo.putAll(browserInfo);
        page._setPage(page);
    }
    
    public Page() {
        src = null;
    }
    
    private Page(String src) {
        this._setId(ID_PREFIX + Integer.toHexString(uniqueId.incrementAndGet()));
        this.src = src;
        PageRegistry.registerPage(this);
    }
    
    public Synchronizer getSynchronizer() {
        return synchronizer;
    }
    
    public EventQueue getEventQueue() {
        return eventQueue;
    }
    
    @Override
    public void setParent(BaseComponent parent) {
        throw new ComponentException(this, "Page cannot have a parent.");
    }
    
    public String getBrowserInfo(String key) {
        Object value = browserInfo.get(key);
        return value == null ? null : value.toString();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBrowserInfo(String key, Class<T> clazz) {
        return (T) browserInfo.get(key);
    }
    
    public Map<String, Object> getBrowserInfo() {
        return Collections.unmodifiableMap(browserInfo);
    }
    
    public String getSrc() {
        return src;
    }
    
    private String nextComponentId() {
        return getId() + "_" + Integer.toHexString(++nextId);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PageRegistry.unregisterPage(this);
        synchronizer.clear();
        eventQueue.clearAll();
    }
    
    /*package*/ void registerComponent(BaseComponent component, boolean register) {
        String id = component.getId();
        
        if (id == null) {
            id = nextComponentId();
            component._setId(id);
        }
        
        if (register) {
            ids.put(id, component);
        } else {
            ids.remove(id);
        }
    }
    
    public BaseComponent findById(String id) {
        int i = id.indexOf('-');
        return ids.get(i == -1 ? id : id.substring(0, i));
    }
    
    @PropertyGetter("title")
    public String getTitle() {
        return title;
    }
    
    @PropertySetter("title")
    public void setTitle(String title) {
        if (!areEqual(title = nullify(title), this.title)) {
            sync("title", this.title = title);
        }
    }
}
