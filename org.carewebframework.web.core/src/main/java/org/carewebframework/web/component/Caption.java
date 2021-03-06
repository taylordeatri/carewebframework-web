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

/**
 * Associates a label with another component.
 */
@Component(value = "caption", widgetClass = "Caption", parentTag = "*", childTag = @ChildTag("*"))
public class Caption extends BaseLabeledComponent<BaseLabeledComponent.LabelPositionAll> {

    public enum LabelAlignment {
        START, CENTER, END
    }

    private LabelAlignment alignment = LabelAlignment.START;

    private String labelStyle;

    private String labelClass = "label-default";

    public Caption() {
        setPosition(LabelPositionAll.LEFT);
    }

    /**
     * Returns the position of the label relative to its associated component. Defaults to 'left'.
     *
     * @return May be one of: left, right, top, or bottom.
     */
    @Override
    @PropertyGetter("position")
    public LabelPositionAll getPosition() {
        return super.getPosition();
    }

    /**
     * Sets the position of the label relative to its associated component.
     *
     * @param position May be one of: left, right, top, or bottom.
     */
    @Override
    @PropertySetter("position")
    public void setPosition(LabelPositionAll position) {
        super.setPosition(position);
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
        if (!areEqual(labelStyle = trimify(labelStyle), this.labelStyle)) {
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
        if (!areEqual(labelClass = trimify(labelClass), this.labelClass)) {
            sync("labelClass", this.labelClass = labelClass);
        }
    }

}
