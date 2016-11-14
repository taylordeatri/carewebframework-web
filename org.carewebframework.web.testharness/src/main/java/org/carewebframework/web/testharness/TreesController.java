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

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.IListModel;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.NestedModel;

public class TreesController extends BaseController {
    
    private class TreeModelObject {
        
        final List<TreeModelObject> children = new ArrayList<>();
        
        final String label;
        
        TreeModelObject(String label) {
            this.label = label;
        }
        
    }
    
    private class TreeModel extends NestedModel<TreeModelObject> {
        
        public TreeModel(List<TreeModelObject> children) {
            super(children);
        }
        
        @Override
        public IListModel<TreeModelObject> getChildren(TreeModelObject parent) {
            return new TreeModel(parent.children);
        }
        
    }
    
    @WiredComponent
    private Treeview treeview;
    
    @WiredComponent
    private Treeview treeview2;
    
    @WiredComponent
    private Checkbox chkShowRoot;
    
    @WiredComponent
    private Checkbox chkShowLines;
    
    @WiredComponent
    private Checkbox chkShowToggles;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        TreeModelObject rootNode = new TreeModelObject(null);
        addChildren(rootNode, 1);
        IModelAndView<Treenode, TreeModelObject> mv = treeview2.getModelAndView(TreeModelObject.class);
        mv.setModel(new TreeModel(rootNode.children));
        mv.setRenderer(new IComponentRenderer<Treenode, TreeModelObject>() {
            
            @Override
            public Treenode render(TreeModelObject model) {
                Treenode node = new Treenode();
                node.setLabel(model.label);
                return node;
            }
            
        });
    }
    
    private void addChildren(TreeModelObject parent, int level) {
        for (int i = 1; i < 4; i++) {
            String label = parent.label == null ? Integer.toString(i) : parent.label + "." + i;
            TreeModelObject child = new TreeModelObject(label);
            parent.children.add(child);
            
            if (level < 3) {
                addChildren(child, level + 1);
            }
        }
    }
    
    @EventHandler(value = "change", target = "@chkShowRoot")
    public void chkShowRootHandler() {
        treeview.setShowRoot(chkShowRoot.isChecked());
    }
    
    @EventHandler(value = "change", target = "@chkShowLines")
    public void chkShowLinesHandler() {
        treeview.setShowLines(chkShowLines.isChecked());
    }
    
    @EventHandler(value = "change", target = "@chkShowToggles")
    public void chkShowTogglesHandler() {
        treeview.setShowToggles(chkShowToggles.isChecked());
    }
    
    @EventHandler(value = "click", target = "btnCollapseAll")
    public void collapseAllHandler() {
        treeview.collapseAll();
    }
    
    @EventHandler(value = "click", target = "btnExpandAll")
    public void expandAllHandler() {
        treeview.expandAll();
    }
    
}
