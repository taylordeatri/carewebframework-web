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
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.SelectEvent;

@Component(value = "treenode", widgetPackage = "cwf-treeview", widgetClass = "Treenode", parentTag = { "treeview",
        "treenode" }, childTag = @ChildTag("treenode"))
public class Treenode extends BaseLabeledImageComponent {
    
    private boolean collapsed;
    
    private boolean selected;
    
    @PropertyGetter("selected")
    public boolean isSelected() {
        return selected;
    }
    
    @PropertySetter("selected")
    public void setSelected(boolean selected) {
        _setSelected(selected, true);
    }
    
    /*package*/ void _setSelected(boolean selected, boolean notifyParent) {
        if (selected != this.selected) {
            sync("selected", this.selected = selected);
            
            if (notifyParent) {
                _setTreeSelected(selected ? this : null);
            }
        }
    }
    
    private void _setTreeSelected(Treenode selectedNode) {
        Treeview treeview = getTreeview();
        
        if (treeview != null) {
            treeview.setSelectedNode(selectedNode);
        }
    }
    
    public Treeview getTreeview() {
        return getAncestor(Treeview.class);
    }
    
    @Override
    public void afterAddChild(BaseComponent child) {
        if (((Treenode) child).isSelected()) {
            _setTreeSelected((Treenode) child);
        }
    }
    
    @PropertyGetter("collapsed")
    public boolean isCollapsed() {
        return collapsed;
    }
    
    @PropertySetter("collapsed")
    public void setCollapsed(boolean collapsed) {
        if (collapsed != this.collapsed) {
            sync("collapsed", this.collapsed = collapsed);
        }
    }
    
    /**
     * Ensures that this node is visible (i.e., all of its parent tree nodes are expanded.
     */
    public void makeVisible() {
        BaseComponent node = getParent();
        
        while (node instanceof Treenode) {
            ((Treenode) node).setCollapsed(false);
            node = node.getParent();
        }
        
        scrollIntoView(false);
    }
    
    @EventHandler(value = "toggle", syncToClient = false)
    private void _onToggle() {
        collapsed = !collapsed;
    }
    
    @EventHandler(value = "select", syncToClient = false)
    private void _onSelect(SelectEvent event) {
        setSelected(event.isSelected());
    }
    
}
