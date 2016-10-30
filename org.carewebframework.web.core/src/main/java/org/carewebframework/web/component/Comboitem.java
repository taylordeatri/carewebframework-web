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
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.SelectEvent;

@Component(value = "comboitem", widgetClass = "Comboitem", parentTag = "combobox")
public class Comboitem extends BaseLabeledComponent {
    
    private boolean selected;
    
    private String value;
    
    public Comboitem() {
        super();
    }
    
    public Comboitem(String label) {
        super(label);
    }
    
    @PropertyGetter("selected")
    public boolean isSelected() {
        return selected;
    }
    
    @PropertySetter("selected")
    public void setSelected(boolean selected) {
        _setSelected(selected, true, true);
    }
    
    @PropertyGetter("value")
    public String getValue() {
        return value;
    }
    
    @PropertySetter("value")
    public void setValue(String value) {
        if (!areEqual(value, this.value)) {
            sync("value", this.value = value);
        }
    }
    
    /*package*/ void _setSelected(boolean selected, boolean notifyClient, boolean notifyParent) {
        if (selected != this.selected) {
            this.selected = selected;
            
            if (notifyClient) {
                sync("selected", selected);
            }
            
            if (notifyParent && getParent() != null) {
                getCombobox()._updateSelected(this);
            }
        }
    }
    
    public Combobox getCombobox() {
        return (Combobox) getParent();
    }
    
    @EventHandler(value = "select", syncToClient = false)
    private void _onSelect(SelectEvent event) {
        _setSelected(event.isSelected(), false, true);
    }
}
