package org.carewebframework.web.component;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ModelAndView;

@Component(value = "rows", widgetPackage = "cwf-table", widgetClass = "Rows", parentTag = "table", childTag = @ChildTag("row"))
public class Rows extends BaseUIComponent {
    
    private final ModelAndView<Row, Object> modelAndView = new ModelAndView<>(this);
    
    @Override
    public void destroy() {
        super.destroy();
        modelAndView.destroy();
    }
    
    @SuppressWarnings("unchecked")
    public <M> IModelAndView<Row, M> getModelAndView(Class<M> clazz) {
        return (IModelAndView<Row, M>) modelAndView;
    }
    
}
