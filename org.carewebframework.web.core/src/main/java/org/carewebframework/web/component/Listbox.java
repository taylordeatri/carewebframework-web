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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ISupportsModel;
import org.carewebframework.web.model.ModelAndView;

@Component(value = "listbox", widgetClass = "Listbox", parentTag = "*", childTag = @ChildTag("listitem"))
public class Listbox extends BaseUIComponent implements ISupportsModel<Listitem> {
    
    private boolean multiple;
    
    private int size;
    
    private final ModelAndView<Listitem, ?> modelAndView = new ModelAndView<>(this);
    
    private final Set<Listitem> selected = new LinkedHashSet<>();
    
    @PropertyGetter("multiple")
    public boolean isMultiple() {
        return multiple;
    }
    
    @PropertySetter("multiple")
    public void setMultiple(boolean multiple) {
        if (multiple != this.multiple) {
            if (!multiple && selected.size() > 1) {
                unselect(null);
            }
            
            sync("multiple", this.multiple = multiple);
        }
    }
    
    @PropertyGetter("size")
    public int getSize() {
        return size;
    }
    
    @PropertySetter("size")
    public void setSize(int size) {
        if (size != this.size) {
            sync("size", this.size = size);
        }
    }
    
    public Set<Listitem> getSelected() {
        return Collections.unmodifiableSet(selected);
    }
    
    public int getSelectedCount() {
        return selected.size();
    }
    
    public Listitem getSelectedItem() {
        return selected.isEmpty() ? null : selected.iterator().next();
    }
    
    public void setSelectedItem(Listitem item) {
        validateIsChild(item);
        unselect(item);
        
        if (item != null) {
            item.setSelected(true);
        }
    }
    
    public int getSelectedIndex() {
        Listitem item = getSelectedItem();
        return item == null ? -1 : item.getIndex();
    }
    
    public void setSelectedIndex(int index) {
        setSelectedItem((Listitem) getChildAt(index));
    }
    
    protected void _updateSelected(Listitem item) {
        if (item.isSelected() != selected.contains(item)) {
            if (!multiple) {
                unselect(null);
            }
            
            if (item.isSelected()) {
                selected.add(item);
            } else {
                selected.remove(item);
            }
        }
    }
    
    public void clearSelected() {
        unselect(null);
    }
    
    private void unselect(Listitem skip) {
        for (Listitem item : selected) {
            if (item != skip) {
                item._setSelected(false, true, false);
            }
        }
        
        selected.clear();
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        _updateSelected((Listitem) child);
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        if (selected.remove(child)) {
            ((Listitem) child)._setSelected(false, true, false);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        modelAndView.destroy();
    }
    
    @Override
    public IModelAndView<Listitem, ?> getModelAndView() {
        return modelAndView;
    }
    
}
