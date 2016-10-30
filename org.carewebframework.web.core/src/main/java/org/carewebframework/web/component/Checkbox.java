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
import org.carewebframework.web.event.ChangeEvent;

@Component(value = "checkbox", widgetClass = "Checkbox", parentTag = "*")
public class Checkbox extends BaseLabeledComponent {
    
    public enum LabelPosition {
        RIGHT, LEFT
    };
    
    private boolean checked;
    
    private LabelPosition position;
    
    public Checkbox() {
        this(null);
    }
    
    public Checkbox(String label) {
        super(label);
        setPosition(LabelPosition.RIGHT);
    }
    
    @PropertyGetter("checked")
    public boolean isChecked() {
        return checked;
    }
    
    @PropertySetter("checked")
    public void setChecked(boolean checked) {
        if (checked != this.checked) {
            sync("checked", this.checked = checked);
        }
    }
    
    @PropertyGetter("position")
    public LabelPosition getPosition() {
        return position;
    }
    
    @PropertySetter("position")
    public void setPosition(LabelPosition position) {
        position = position == null ? LabelPosition.RIGHT : position;
        
        if (position != this.position) {
            sync("position", this.position = position);
        }
    }
    
    @EventHandler(value = "change", syncToClient = false)
    private void _onChange(ChangeEvent event) {
        checked = "true".equals(event.getValue());
    }
    
}
