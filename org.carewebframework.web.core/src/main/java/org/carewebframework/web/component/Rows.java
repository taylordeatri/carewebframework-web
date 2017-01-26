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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ISupportsModel;
import org.carewebframework.web.model.ModelAndView;

@Component(value = "rows", widgetPackage = "cwf-table", widgetClass = "Rows", parentTag = "table", childTag = @ChildTag("row"))
public class Rows extends BaseUIComponent implements ISupportsModel<Row> {
    
    public enum Selectable {
        NO, SINGLE, MULTIPLE;
    }
    
    private Selectable selectable = Selectable.NO;
    
    private final List<Row> selected = new ArrayList<>();
    
    private final ModelAndView<Row, Object> modelAndView = new ModelAndView<>(this);
    
    @Override
    public void destroy() {
        super.destroy();
        modelAndView.destroy();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <M> IModelAndView<Row, M> getModelAndView(Class<M> clazz) {
        return (IModelAndView<Row, M>) modelAndView;
    }
    
    @PropertyGetter("selectable")
    public Selectable getSelectable() {
        return selectable;
    }
    
    @PropertySetter("selectable")
    public void setSelectable(Selectable selectable) {
        if ((selectable = defaultify(selectable, Selectable.NO)) != this.selectable) {
            sync("selectable", this.selectable = selectable);
            
            if (selectable != Selectable.MULTIPLE && !selected.isEmpty()) {
                unselect(selectable == Selectable.NO ? null : selected.get(0));
            }
        }
    }
    
    public List<Row> getSelected() {
        return Collections.unmodifiableList(selected);
    }
    
    private void unselect(Row excluded) {
        Iterator<Row> iter = selected.iterator();
        
        while (iter.hasNext()) {
            Row row = iter.next();
            
            if (row != excluded) {
                row._setSelected(false, true, false);
                iter.remove();
            }
        }
    }
    
    public int getSelectedCount() {
        return selected.size();
    }
    
    protected void _updateSelected(Row row) {
        if (row.isSelected()) {
            selected.add(row);
            
            if (selectable != Selectable.MULTIPLE) {
                unselect(selectable == Selectable.NO ? null : row);
            }
        } else {
            selected.remove(row);
        }
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        super.afterRemoveChild(child);
        selected.remove(child);
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        super.afterAddChild(child);
        Row row = (Row) child;
        
        if (row.isSelected()) {
            _updateSelected(row);
        }
    }
}
