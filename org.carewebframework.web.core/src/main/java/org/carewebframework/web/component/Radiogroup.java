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
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * A component that logically groups together multiple radio buttons. Selection of radio buttons
 * under the same radio group is mutually exclusive. Note that radio buttons do not have to be a
 * direct child of their group.
 */
@Component(value = "radiogroup", widgetClass = "Radiogroup", content = ContentHandling.AS_CHILD, parentTag = "*", childTag = @ChildTag("*"))
public class Radiogroup extends BaseUIComponent {
    
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }
    
    private Orientation orientation = Orientation.HORIZONTAL;
    
    public Radiobutton getSelected() {
        return getSelected(this);
    }
    
    private Radiobutton getSelected(BaseComponent parent) {
        for (BaseComponent child : parent.getChildren()) {
            if (child instanceof Radiobutton && ((Radiobutton) child).isChecked()) {
                return (Radiobutton) child;
            }
        }
        
        for (BaseComponent child : parent.getChildren()) {
            Radiobutton rb = child instanceof Radiogroup ? null : getSelected(child);
            
            if (rb != null) {
                return rb;
            }
        }
        
        return null;
    }
    
    @PropertyGetter("orientation")
    public Orientation getOrientation() {
        return orientation;
    }
    
    @PropertySetter("orientation")
    public void setOrientation(Orientation orientation) {
        orientation = orientation == null ? Orientation.HORIZONTAL : orientation;
        
        if (orientation != this.orientation) {
            sync("orientation", this.orientation = orientation);
        }
    }
}
