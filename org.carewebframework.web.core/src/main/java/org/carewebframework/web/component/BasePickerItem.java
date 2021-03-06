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

import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * The base class for components that represent a single choice for a picker component.
 *
 * @param <T> The item type.
 */
public abstract class BasePickerItem<T> extends BaseComponent {
    
    private T value;
    
    public BasePickerItem() {
        
    }
    
    public BasePickerItem(T item) {
        setRawValue(item);
    }
    
    public T getValue() {
        return value;
    }
    
    @PropertySetter("value")
    public void setValue(String text) {
        setRawValue(_toValue(text));
    }
    
    public void setRawValue(T value) {
        if (!areEqual(value, this.value)) {
            this.value = value;
            sync("value", _toString(value));
        }
    }
    
    protected abstract String _toString(T value);
    
    protected abstract T _toValue(String text);
}
