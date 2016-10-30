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

/**
 * Component to facilitate adding a label to another component or component group.
 */
public class LabeledElement extends BaseLabeledComponent {
    
    public enum LabelAlignment {
        START, CENTER, END
    };
    
    public enum LabelPosition {
        LEFT, RIGHT, TOP, BOTTOM
    };
    
    private LabelAlignment alignment = LabelAlignment.START;
    
    private LabelPosition position = LabelPosition.LEFT;
    
    private String labelStyle;
    
    private String labelClass;
    
    /**
     * Returns the position of the label relative to the contained elements. Defaults to 'left'.
     * 
     * @return May be one of: left, right, top, or bottom.
     */
    @PropertyGetter("position")
    public LabelPosition getPosition() {
        return position;
    }
    
    /**
     * Sets the position of the label relative to the contained elements.
     * 
     * @param position May be one of: left, right, top, or bottom.
     */
    @PropertySetter("position")
    public void setPosition(LabelPosition position) {
        position = position == null ? LabelPosition.LEFT : position;
        
        if (position != this.position) {
            sync("position", this.position = position);
        }
    }
    
    /**
     * Returns the alignment of the label. Defaults to 'start'.
     * 
     * @return May be one of start, center, end.
     */
    @PropertyGetter("alignment")
    public LabelAlignment getAlignment() {
        return alignment;
    }
    
    /**
     * Sets the alignment of the label.
     * 
     * @param alignment May be one of: start, center, end.
     */
    @PropertySetter("alignment")
    public void setAlignment(LabelAlignment alignment) {
        alignment = alignment == null ? LabelAlignment.START : alignment;
        
        if (alignment != this.alignment) {
            sync("alignment", this.alignment = alignment);
        }
    }
    
    /**
     * Returns the style(s) associated with the label.
     * 
     * @return The label style(s).
     */
    public String getLabelStyle() {
        return labelStyle;
    }
    
    /**
     * Sets the style(s) of the label.
     * 
     * @param labelStyle The label style(s).
     */
    public void setLabelStyle(String labelStyle) {
        if (!areEqual(labelStyle, this.labelStyle)) {
            sync("labelStyle", this.labelStyle = labelStyle);
        }
    }
    
    /**
     * Returns the css class(es) associated with the label.
     * 
     * @return The label css class(es).
     */
    public String getLabelClass() {
        return labelClass;
    }
    
    /**
     * Sets the css class(es) of the label.
     * 
     * @param labelClass The label css class(es).
     */
    public void setLabelClass(String labelClass) {
        if (!areEqual(labelClass, this.labelClass)) {
            sync("labelClass", this.labelClass = labelClass);
        }
    }
    
}
