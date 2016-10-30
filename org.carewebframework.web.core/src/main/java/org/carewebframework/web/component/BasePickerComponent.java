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

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

public abstract class BasePickerComponent<T> extends BaseInputboxComponent<T> {
    
    private boolean showText;
    
    private boolean showHints;
    
    private BasePickerItem<T> converter;
    
    protected BasePickerComponent(Class<? extends BasePickerItem<T>> itemClass) {
        try {
            converter = itemClass.newInstance();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @PropertyGetter("showText")
    public boolean getShowText() {
        return showText;
    }
    
    @PropertySetter("showText")
    public void setShowText(boolean showText) {
        if (showText != this.showText) {
            sync("showText", this.showText = showText);
        }
    }
    
    @PropertyGetter("showHints")
    public boolean getShowHints() {
        return showHints;
    }
    
    @PropertySetter("showHints")
    public void setShowHints(boolean showHints) {
        if (showHints != this.showHints) {
            sync("showHints", this.showHints = showHints);
        }
    }
    
    @Override
    protected T _toValue(String text) {
        return converter._toValue(text);
    }
    
    @Override
    protected String _toString(T value) {
        return converter._toString(value);
    }
    
}
