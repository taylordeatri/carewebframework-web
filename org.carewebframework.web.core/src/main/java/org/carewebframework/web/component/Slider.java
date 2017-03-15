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
import org.springframework.util.Assert;

/**
 * A slider component.
 */
@Component(value = "slider", widgetClass = "Slider", parentTag = "*")
public class Slider extends BaseUIComponent {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }
    
    private Orientation orientation = Orientation.HORIZONTAL;
    
    private int value;
    
    private int maxValue = 100;
    
    private int minValue;

    private int step = 1;

    private boolean synced;

    @PropertyGetter("value")
    public int getValue() {
        return value;
    }
    
    @PropertySetter("value")
    public void setValue(int value) {
        if (value != this.value) {
            sync("value", this.value = value);
        }
    }
    
    @PropertyGetter("maxValue")
    public int getMaxValue() {
        return maxValue;
    }
    
    @PropertySetter("maxValue")
    public void setMaxValue(int maxValue) {
        if (maxValue != this.maxValue) {
            sync("maxValue", this.maxValue = maxValue);
        }
    }
    
    @PropertyGetter("minValue")
    public int getMinValue() {
        return minValue;
    }

    @PropertySetter("minValue")
    public void setMinValue(int minValue) {
        if (minValue != this.minValue) {
            sync("minValue", this.minValue = minValue);
        }
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
    
    @PropertyGetter("step")
    public int getStep() {
        return step;
    }
    
    @PropertySetter("step")
    public void setStep(int step) {
        Assert.isTrue(step > 0, "Step value must be greater than zero.");

        if (step != this.step) {
            sync("step", this.step = step);
        }
    }

    @PropertyGetter("synchronized")
    public boolean getSynchronized() {
        return synced;
    }

    @PropertySetter("synchronized")
    protected void setSynchronized(boolean synced) {
        if (synced != this.synced) {
            sync("synced", this.synced = synced);
        }
    }

    @EventHandler(value = "change", syncToClient = false)
    private void _onChange(ChangeEvent event) {
        value = defaultify(event.getValue(Integer.class), value);
    }
}
