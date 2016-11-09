package org.carewebframework.web.model;

import org.carewebframework.web.component.BaseComponent;

public interface IModelAndView<T extends BaseComponent, M> {
    
    IListModel<M> getModel();
    
    void setModel(IListModel<M> model);
    
    IComponentRenderer<T, M> getRenderer();
    
    void setRenderer(IComponentRenderer<T, M> renderer);
    
    void rerender();
}
