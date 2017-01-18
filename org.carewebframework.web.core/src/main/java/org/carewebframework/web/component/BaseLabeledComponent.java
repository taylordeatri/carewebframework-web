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

public abstract class BaseLabeledComponent<P extends BaseLabeledComponent.ILabelPosition> extends BaseUIComponent {
    
    public interface ILabelPosition {};
    
    public enum LabelPositionHorz implements ILabelPosition {
        RIGHT, LEFT
    }
    
    public enum LabelPositionAll implements ILabelPosition {
        RIGHT, LEFT, TOP, BOTTOM
    }
    
    public enum LabelPositionNone implements ILabelPosition {}
    
    private String label;
    
    private P position;
    
    public BaseLabeledComponent() {
    }
    
    public BaseLabeledComponent(String label) {
        setLabel(label);
    }
    
    @PropertyGetter("label")
    public String getLabel() {
        return label;
    }
    
    @PropertySetter("label")
    public void setLabel(String label) {
        if (!areEqual(label = nullify(label), this.label)) {
            sync("label", this.label = label);
        }
    }
    
    protected P getPosition() {
        return position;
    }
    
    protected void setPosition(P position) {
        if (position != this.position) {
            sync("position", this.position = position);
        }
    }
    
}
