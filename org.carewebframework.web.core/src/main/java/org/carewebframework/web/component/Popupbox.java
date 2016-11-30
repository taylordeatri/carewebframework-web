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
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

@Component(value = "popupbox", widgetClass = "Popupbox", parentTag = "*", childTag = @ChildTag(value = "popup", maximum = 1))
public class Popupbox extends BaseInputboxComponent<String> {
    
    private Popup popup;
    
    private boolean open;
    
    public void open() {
        setOpen(true);
    }
    
    public void close() {
        setOpen(false);
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        super.afterAddChild(child);
        setPopup((Popup) child);
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        super.afterRemoveChild(child);
        
        if (child == popup) {
            setPopup(null);
        }
    }
    
    @PropertyGetter("popup")
    public Popup getPopup() {
        if (popup != null && popup.isDead()) {
            popup = null;
            sync("popup", popup);
        }
        
        return popup;
    }
    
    @PropertySetter(value = "popup", defer = true)
    public void setPopup(Popup popup) {
        BaseComponent child = this.getFirstChild();
        
        if (child != null && child != popup) {
            throw new IllegalArgumentException("You may not set a popup reference when a child popup is present.");
        }
        
        if (popup != getPopup()) {
            validate(popup);
            sync("popup", this.popup = popup);
            open = false;
        }
    }
    
    @PropertyGetter("open")
    public boolean isOpen() {
        return open;
    }
    
    @PropertySetter("open")
    public void setOpen(boolean open) {
        if (open != this.open) {
            sync("open", this.open = open);
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
