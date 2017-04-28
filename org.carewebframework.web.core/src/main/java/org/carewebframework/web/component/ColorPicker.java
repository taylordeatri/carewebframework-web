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

import java.awt.Color;

import org.carewebframework.common.ColorUtil;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;

/**
 * A component for selecting a color from a palette of colors.
 */
@Component(value = "colorpicker", widgetModule = "cwf-picker", widgetClass = "Colorpicker", parentTag = "*", childTag = @ChildTag("colorpickeritem"))
public class ColorPicker extends BasePickerComponent<Color> {
    
    @Component(value = "colorpickeritem", widgetModule = "cwf-picker", widgetClass = "Colorpickeritem", parentTag = "colorpicker")
    public static class ColorPickeritem extends BasePickerItem<Color> {
        
        public ColorPickeritem() {
            super();
        }
        
        public ColorPickeritem(Color color) {
            super(color);
        }
        
        @Override
        protected String _toString(Color value) {
            return ColorUtil.toString(value);
        }
        
        @Override
        protected Color _toValue(String text) {
            return ColorUtil.toColor(text);
        }
        
    }
    
    public ColorPicker() {
        super(ColorPickeritem.class);
    }
    
}
