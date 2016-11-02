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
package org.carewebframework.web.model;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.model.IListModel.IListModelListener;
import org.carewebframework.web.model.IListModel.ListEventType;

public class ModelAndView<T extends BaseComponent, M> implements IListModelListener {
    
    private final BaseComponent parent;
    
    private IComponentRenderer<T, M> renderer;
    
    private IListModel<M> model;
    
    public ModelAndView(BaseComponent parent) {
        this.parent = parent;
    }
    
    public ModelAndView(BaseComponent parent, IListModel<M> model, IComponentRenderer<T, M> renderer) {
        this.parent = parent;
        setModel(model);
        setRenderer(renderer);
    }
    
    public IComponentRenderer<T, M> getRenderer() {
        return renderer;
    }
    
    public void setRenderer(IComponentRenderer<T, M> renderer) {
        if (renderer != this.renderer) {
            this.renderer = renderer;
            rerender();
        }
    }
    
    public IListModel<M> getModel() {
        return model;
    }
    
    public void setModel(IListModel<M> model) {
        if (this.model != null) {
            this.model.removeEventListener(this);
        }
        
        this.model = model;
        
        if (this.model != null) {
            this.model.addEventListener(this);
        }
        
        rerender();
    }
    
    public void rerender() {
        if (model != null) {
            parent.destroyChildren();
            
            for (int i = 0; i < model.size(); i++) {
                renderChild(i);
            }
        }
    }
    
    private void renderChild(int index) {
        if (renderer != null) {
            T child = renderer.render(model.get(index));
            parent.addChild(child, index);
        }
    }
    
    @Override
    public void onListChange(ListEventType type, int startIndex, int endIndex) {
        switch (type) {
            case ADD:
                for (int i = startIndex; i <= endIndex; i++) {
                    renderChild(i);
                }
                
                break;
            
            case DELETE:
                for (int i = endIndex; i >= startIndex; i--) {
                    parent.getChildAt(i).destroy();
                }
                break;
            
            case CHANGE:
                rerender();
                break;
            
            case REPLACE:
                onListChange(ListEventType.DELETE, startIndex, endIndex);
                onListChange(ListEventType.ADD, startIndex, endIndex);
                break;
            
            case SWAP:
                parent.swapChildren(startIndex, endIndex);
                break;
        }
        
    }
}