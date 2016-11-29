package org.carewebframework.web.component;

import java.util.List;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;

@Component(value = "messagewindow", widgetClass = "Messagewindow", parentTag = "page", childTag = @ChildTag("messagepane"))
public class MessageWindow extends BaseUIComponent {
    
    /**
     * Clears all messages.
     */
    public void clear() {
        destroyChildren();
    }
    
    /**
     * Clears messages within the specified category.
     * 
     * @param category Messages belonging to this category will be cleared.
     */
    public void clear(String category) {
        List<BaseComponent> children = getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            MessagePane pane = (MessagePane) children.get(i);
            
            if (areEqual(category, pane.getCategory())) {
                pane.destroy();
            }
        }
    }
    
}
