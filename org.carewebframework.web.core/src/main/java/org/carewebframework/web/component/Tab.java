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

import java.util.function.BooleanSupplier;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.SelectEvent;

@Component(value = "tab", widgetPackage = "cwf-tabview", widgetClass = "Tab", content = ContentHandling.AS_CHILD, parentTag = "tabview", childTag = @ChildTag("*"))

public class Tab extends BaseLabeledImageComponent {
    
    private boolean closable;
    
    private boolean selected;
    
    private BooleanSupplier onCanClose;
    
    public Tab() {
    }
    
    public Tab(String label) {
        super(label);
    }
    
    @PropertyGetter("closable")
    public boolean isClosable() {
        return closable;
    }
    
    @PropertySetter("closable")
    public void setClosable(boolean closable) {
        if (closable != this.closable) {
            sync("closable", this.closable = closable);
        }
    }
    
    @PropertyGetter("selected")
    public boolean isSelected() {
        return selected;
    }
    
    @PropertySetter("selected")
    public void setSelected(boolean selected) {
        _setSelected(selected, true);
    }
    
    @EventHandler(value = "select", syncToClient = false)
    private void _onSelect(SelectEvent event) {
        setSelected(event.isSelected());
    }
    
    @EventHandler(value = "close", syncToClient = false)
    private void _onClose(Event event) {
        close();
    }
    
    /*package*/ void _setSelected(boolean selected, boolean notifyParent) {
        if (selected != this.selected) {
            sync("selected", this.selected = selected);
            
            if (notifyParent && getParent() != null) {
                getTabview().setSelectedTab(selected ? this : null);
            }
        }
    }
    
    public boolean close() {
        if (canClose()) {
            destroy();
            return true;
        }
        
        return false;
    }
    
    public boolean canClose() {
        return onCanClose == null || onCanClose.getAsBoolean();
    }
    
    public Tabview getTabview() {
        return (Tabview) getParent();
    }
    
    public BooleanSupplier getOnCanClose() {
        return onCanClose;
    }
    
    public void setOnCanClose(boolean canClose) {
        setOnCanClose(() -> canClose);
    }
    
    public void setOnCanClose(BooleanSupplier onCanClose) {
        this.onCanClose = onCanClose;
    }
    
    @Override
    public void bringToFront() {
        setSelected(true);
        super.bringToFront();
    }
}
