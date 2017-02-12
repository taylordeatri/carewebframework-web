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

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * Component for entering multiple lines of text.
 */
@Component(value = "memobox", widgetClass = "Memobox", parentTag = "*")
public class Memobox extends BaseInputboxComponent<String> {
    
    private boolean autoScroll;
    
    public Memobox() {
        super();
        addStyle("resize", "none");
    }
    
    @Override
    @PropertyGetter("synchronized")
    public boolean getSynchronized() {
        return super.getSynchronized();
    }
    
    @Override
    @PropertySetter("synchronized")
    public void setSynchronized(boolean synchronize) {
        super.setSynchronized(synchronize);
    }
    
    @PropertyGetter("autoScroll")
    public boolean isAutoScroll() {
        return autoScroll;
    }
    
    @PropertySetter("autoScroll")
    public void setAutoScroll(boolean autoScroll) {
        if (autoScroll != this.autoScroll) {
            sync("autoScroll", this.autoScroll = autoScroll);
        }
    }
    
    @Override
    protected String _toValue(String value) {
        return value;
    }
    
    @Override
    protected String _toString(String value) {
        return value;
    }
    
}
