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

import java.util.Iterator;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;

@Component(value = "treenode", widgetPackage = "cwf-treeview", widgetClass = "Treenode", parentTag = { "treeview",
        "treenode" }, childTag = @ChildTag("treenode"))
public class Treenode extends BaseLabeledImageComponent implements Iterable<Treenode> {
    
    /**
     * Iterates over nodes in a tree in a depth first search. Is not susceptible to concurrent
     * modification errors if tree composition changes during iteration.
     */
    protected static class TreenodeIterator implements Iterator<Treenode> {
        
        private Treenode current;
        
        private Treenode next;
        
        private int level;
        
        /**
         * Iterates all descendants of root node.
         * 
         * @param root The root node.
         */
        public TreenodeIterator(BaseComponent root) {
            next = root == null ? null : root.getChild(Treenode.class);
        }
        
        /**
         * Advances to next tree node following specified node.
         * 
         * @return Next tree node or null if no more.
         */
        private Treenode advance() {
            if (current == null || level < 0) {
                return null;
            }
            
            next = (Treenode) current.getFirstChild();
            
            if (next != null) {
                level++;
                return next;
            }
            
            next = (Treenode) current.getNextSibling();
            
            if (next != null) {
                return next;
            }
            
            next = current;
            
            while (--level >= 0) {
                BaseComponent parent = next.getParent();
                
                if (!(parent instanceof Treenode)) {
                    level = -1;
                } else {
                    next = (Treenode) parent.getNextSibling();
                    
                    if (next == null) {
                        next = (Treenode) parent;
                    } else {
                        break;
                    }
                }
            }
            
            return level < 0 ? null : next;
        }
        
        /**
         * Returns next tree node.
         * 
         * @return The next tree node.
         */
        private Treenode nextNode() {
            return next == null ? advance() : next;
        }
        
        /**
         * Returns true if iterator not at end.
         */
        @Override
        public boolean hasNext() {
            return nextNode() != null;
        }
        
        /**
         * Returns next tree item, preparing internal state to retrieve next node.
         */
        @Override
        public Treenode next() {
            current = nextNode();
            next = null;
            return current;
        }
        
    }
    
    private boolean collapsed;
    
    private boolean selected;
    
    private int badgeCounter;
    
    @PropertyGetter("selected")
    public boolean isSelected() {
        return selected;
    }
    
    @PropertySetter("selected")
    public void setSelected(boolean selected) {
        _setSelected(selected, true, true);
    }
    
    /*package*/ void _setSelected(boolean selected, boolean notifyClient, boolean notifyParent) {
        if (selected != this.selected) {
            this.selected = selected;
            
            if (notifyClient) {
                sync("selected", selected);
            }
            
            if (notifyParent) {
                Treeview treeview = getTreeview();
                
                if (treeview != null) {
                    if (selected) {
                        treeview.setSelectedNode(this);
                    } else if (treeview.getSelectedNode() == this) {
                        treeview.setSelectedNode(null);
                    }
                }
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
    protected void afterAddChild(BaseComponent child) {
        if (((Treenode) child).isSelected()) {
            _setTreeSelected((Treenode) child);
        }
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        if (((Treenode) child).isSelected()) {
            _setTreeSelected(null);
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
    
    @EventHandler(value = "change", syncToClient = false)
    private void _onChange(ChangeEvent event) {
        _setSelected(defaultify(event.getValue(Boolean.class), false), false, true);
        Treeview tree = getTreeview();
        
        if (tree != null) {
            tree.fireEvent(event);
        }
    }
    
    @EventHandler("badge")
    private void _onBadge(Event event) {
        int delta = (Integer) event.getData();
        
        if (delta != 0) {
            badgeCounter += delta;
            sync("badge", badgeCounter);
        }
    }
    
    @Override
    public Iterator<Treenode> iterator() {
        return new TreenodeIterator(this);
    }
}
