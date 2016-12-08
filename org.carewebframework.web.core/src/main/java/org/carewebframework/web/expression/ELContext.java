package org.carewebframework.web.expression;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.page.PageElement;

/**
 * This serves as the context root for an EL expression evaluation.
 */
public class ELContext {
    
    private final BaseComponent component;
    
    private final BaseComponent parent;
    
    private final PageElement element;
    
    public ELContext(BaseComponent component, BaseComponent parent, PageElement element) {
        this.component = component;
        this.parent = parent;
        this.element = element;
    }
    
    public Object getValue(String name) {
        Object result = "this".equals(name) ? component : element.getTagLibrary(name);
        result = result != null ? result : component.getAttribute(name);
        result = result != null ? result : parent == null ? null : parent.findByName(name);
        return result;
    }
}
