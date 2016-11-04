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
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

public abstract class BaseNumberboxComponent<T extends Number> extends BaseInputboxComponent<T> {
    
    private T min;
    
    private T max;
    
    private final Class<T> clazz;
    
    protected BaseNumberboxComponent(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    @PropertyGetter("min")
    public T getMin() {
        return min;
    }
    
    @PropertySetter("min")
    public void setMin(T min) {
        if (min != this.min) {
            sync("min", this.min = min);
        }
    }
    
    @PropertyGetter("max")
    public T getMax() {
        return max;
    }
    
    @PropertySetter("max")
    public void setMax(T max) {
        if (max != this.max) {
            sync("max", this.max = max);
        }
    }
    
    @Override
    protected String _toString(T value) {
        return value == null ? null : value.toString();
    }
    
    @Override
    protected T _toValue(String value) {
        value = value == null ? "" : StringUtils.trimAllWhitespace(value);
        return value.isEmpty() ? null : NumberUtils.parseNumber(value, clazz);
    }
    
}
