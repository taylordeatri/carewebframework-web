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

import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.ChangeEvent;

public abstract class BaseInputComponent<T> extends BaseUIComponent {
    
    private T value;
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        _setValue(value, true);
    }
    
    public void clear() {
        setValue(null);
    }
    
    @PropertyGetter("value")
    protected String _getValue() {
        return value == null ? null : _toString(value);
    }
    
    @PropertySetter("value")
    protected void _setValue(String value) {
        setValue(_toValue(value));
    }
    
    protected void _setValue(T value, boolean notifyClient) {
        if (!areEqual(value, this.value)) {
            this.value = value;
            
            if (notifyClient) {
                sync("value", value == null ? null : _toClient(value));
            }
        }
    }
    
    protected abstract T _toValue(String value);
    
    protected abstract String _toString(T value);
    
    /**
     * Override to provide alternate serialization format for sending to client. Default action is
     * to serialize to string.
     * 
     * @param value
     * @return
     */
    protected Object _toClient(T value) {
        return _toString(value);
    }
    
    @EventHandler(value = "change", syncToClient = false)
    private void _onChange(ChangeEvent event) {
        _setValue(_toValue(event.getValue()), false);
    }
    
}
