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

/**
 * The base class for input box components that support the entry of numeric values.
 *
 * @param <T> The type of numeric value supported.
 */
public abstract class BaseNumberboxComponent<T extends Number> extends BaseInputboxComponent<T> {
    
    private T minValue;
    
    private T maxValue;
    
    private final Class<T> clazz;
    
    protected BaseNumberboxComponent(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    @PropertyGetter("minValue")
    public T getMinValue() {
        return minValue;
    }
    
    @PropertySetter("minValue")
    private void setMinValue(String minValue) {
        setMinValue(_toValue(minValue));
    }
    
    public void setMinValue(T minValue) {
        if (!areEqual(minValue, this.minValue)) {
            sync("minValue", this.minValue = minValue);
        }
    }
    
    @PropertyGetter("maxValue")
    public T getMaxValue() {
        return maxValue;
    }
    
    @PropertySetter("maxValue")
    private void setMaxValue(String maxValue) {
        setMaxValue(_toValue(maxValue));
    }
    
    public void setMaxValue(T maxValue) {
        if (!areEqual(maxValue, this.maxValue)) {
            sync("maxValue", this.maxValue = maxValue);
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
