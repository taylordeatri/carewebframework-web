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
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ISupportsModel;
import org.carewebframework.web.model.ModelAndView;

/**
 * A component representing a combo box control.
 */
@Component(value = "combobox", widgetClass = "Combobox", parentTag = "*", childTag = @ChildTag("comboitem"))
public class Combobox extends BaseInputboxComponent<String> implements ISupportsModel<Comboitem> {
    
    private Comboitem selected;
    
    private boolean autoFilter;
    
    private final ModelAndView<Comboitem, Object> modelAndView = new ModelAndView<>(this);
    
    public Comboitem getSelectedItem() {
        return selected;
    }
    
    public void setSelectedItem(Comboitem item) {
        validateIsChild(item);
        
        if (item == null) {
            _updateSelected(item);
        } else {
            item.setSelected(true);
        }
    }
    
    public int getSelectedIndex() {
        Comboitem item = getSelectedItem();
        return item == null ? -1 : item.getIndex();
    }
    
    public void setSelectedIndex(int index) {
        setSelectedItem((Comboitem) getChildAt(index));
    }
    
    protected void _updateSelected(Comboitem item) {
        if (selected != null) {
            selected._setSelected(false, true, false);
        }
        
        selected = item;
        setValue(selected == null ? null : selected.getLabel());
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        Comboitem item = (Comboitem) child;
        
        if (item.isSelected()) {
            _updateSelected(item);
        }
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        if (child == selected) {
            selected = null;
        }
    }
    
    @PropertyGetter("autoFilter")
    public boolean getAutoFilter() {
        return autoFilter;
    }
    
    @PropertySetter("autoFilter")
    public void setAutoFilter(boolean autoFilter) {
        if (autoFilter != this.autoFilter) {
            sync("autoFilter", this.autoFilter = autoFilter);
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
    
    @Override
    public void destroy() {
        super.destroy();
        modelAndView.destroy();
    }
    
    @Override
    public IModelAndView<Comboitem, ?> getModelAndView() {
        return modelAndView;
    }
    
}
