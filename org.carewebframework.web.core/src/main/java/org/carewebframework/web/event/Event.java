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
package org.carewebframework.web.event;

import org.carewebframework.web.annotation.EventType.EventParameter;
import org.carewebframework.web.annotation.OnFailure;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;

/**
 * This is the base class for all events.
 */
public class Event {
    
    @EventParameter(onFailure = OnFailure.EXCEPTION)
    private String type;
    
    @EventParameter(onFailure = OnFailure.IGNORE)
    private BaseComponent target;
    
    @EventParameter(value = "target", onFailure = OnFailure.IGNORE)
    private String targetId;
    
    @EventParameter(onFailure = OnFailure.IGNORE)
    private BaseComponent currentTarget;
    
    @EventParameter(onFailure = OnFailure.IGNORE)
    private BaseComponent relatedTarget;
    
    @EventParameter(onFailure = OnFailure.IGNORE)
    private Object data;
    
    @EventParameter
    private Page page;
    
    private boolean stopPropagation;
    
    public Event() {
    }
    
    public Event(String type) {
        this(type, null, null);
    }
    
    public Event(String type, BaseComponent target) {
        this(type, target, null);
    }
    
    public Event(String type, BaseComponent target, Object data) {
        this(type, target, null, data);
    }
    
    public Event(String type, BaseComponent target, BaseComponent relatedTarget) {
        this(type, target, relatedTarget, null);
    }
    
    public Event(String type, BaseComponent target, BaseComponent relatedTarget, Object data) {
        this.type = type;
        this.target = target;
        this.targetId = target == null ? null : target.getId();
        this.data = data;
        this.currentTarget = target;
        this.relatedTarget = relatedTarget;
        this.page = target != null ? target.getPage() : null;
        this.page = this.page == null ? ExecutionContext.getPage() : this.page;
    }
    
    public Event(Event source) {
        this(source.type, source);
    }
    
    public Event(String type, Event source) {
        this.type = type;
        this.target = source.target;
        this.targetId = source.targetId;
        this.data = source.data;
        this.currentTarget = source.currentTarget;
        this.relatedTarget = source.relatedTarget;
        this.page = source.page;
    }
    
    public Page getPage() {
        return page;
    }
    
    public String getType() {
        return type;
    }
    
    public BaseComponent getTarget() {
        return target != null ? target : currentTarget != null ? currentTarget : page;
    }
    
    public BaseComponent getCurrentTarget() {
        return currentTarget != null ? currentTarget : target != null ? target : page;
    }
    
    public BaseComponent getRelatedTarget() {
        return relatedTarget;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public Object getData() {
        return data;
    }
    
    public void stopPropagation() {
        stopPropagation = true;
    }
    
    public boolean isStopped() {
        return stopPropagation;
    }
}
