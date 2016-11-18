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
package org.carewebframework.web.testharness;

import java.util.Comparator;

import org.apache.commons.lang.RandomStringUtils;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ListModel;

public class TablesController extends BaseController {
    
    private class RowModelObject {
        
        final String label;
        
        final int value;
        
        RowModelObject(String label, int value) {
            this.label = label;
            this.value = value;
        }
        
    }
    
    private final IComponentRenderer<Row, RowModelObject> renderer = new IComponentRenderer<Row, RowModelObject>() {
        
        @Override
        public Row render(RowModelObject model) {
            Row row = new Row();
            row.addChild(new Cell(model.label));
            row.addChild(new Cell(Integer.toString(model.value)));
            return row;
        }
        
    };
    
    private final Comparator<RowModelObject> comp1 = new Comparator<RowModelObject>() {
        
        @Override
        public int compare(RowModelObject o1, RowModelObject o2) {
            return o1.label.compareToIgnoreCase(o2.label);
        }
        
    };
    
    private final Comparator<RowModelObject> comp2 = new Comparator<RowModelObject>() {
        
        @Override
        public int compare(RowModelObject o1, RowModelObject o2) {
            return o1.value - o2.value;
        }
        
    };
    
    @WiredComponent
    private Table table;
    
    @WiredComponent
    private Column col1;
    
    @WiredComponent
    private Column col2;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        col1.setSortComparator(comp1);
        col2.setSortComparator(comp2);
        IModelAndView<Row, RowModelObject> mv = table.getRows().getModelAndView(RowModelObject.class);
        ListModel<RowModelObject> model = new ListModel<>();
        
        for (int i = 1; i < 11; i++) {
            model.add(new RowModelObject(RandomStringUtils.random(10, true, true), i));
        }
        
        mv.setModel(model);
        mv.setRenderer(renderer);
    }
    
}
