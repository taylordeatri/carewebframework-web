package org.carewebframework.web.component;

import java.util.List;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.event.IEventListener;

/**
 * A component supporting a window that slides down from the top center of the viewport and can
 * display multiple messages.
 */
@Component(value = "messagewindow", widgetClass = "Messagewindow", parentTag = "page", childTag = @ChildTag("messagepane"))
public class MessageWindow extends BaseUIComponent {
    
    /**
     * Clears all messages.
     */
    public void clearMessages() {
        destroyChildren();
    }
    
    /**
     * Clears messages within the specified category.
     * 
     * @param category Messages belonging to this category will be cleared.
     */
    public void clearMessages(String category) {
        List<BaseComponent> children = getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            MessagePane pane = (MessagePane) children.get(i);
            
            if (areEqual(category, pane.getCategory())) {
                pane.destroy();
            }
        }
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @return The newly created message pane.
     * @param message Message to display.
     */
    public MessagePane showMessage(String message) {
        return showMessage(message, null);
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @param message Message to display.
     * @param caption Caption for message.
     * @return The newly created message pane.
     */
    public MessagePane showMessage(String message, String caption) {
        return showMessage(message, caption, null);
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @param message Message to display
     * @param caption Caption for message.
     * @param clazz CSS classes to apply.
     * @return The newly created message pane.
     */
    public MessagePane showMessage(String message, String caption, String clazz) {
        return showMessage(message, caption, clazz, 8000);
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @param message Message to display
     * @param caption Caption for message.
     * @param clazz CSS classes to apply.
     * @param duration Message duration in milliseconds.
     * @return The newly created message pane.
     */
    public MessagePane showMessage(String message, String caption, String clazz, int duration) {
        return showMessage(message, caption, clazz, duration, null, null);
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @param message Message to display
     * @param caption Caption for message.
     * @param clazz CSS classes to apply.
     * @param duration Message duration in milliseconds.
     * @param category Tag to classify message.
     * @return The newly created message pane.
     */
    public MessagePane showMessage(String message, String caption, String clazz, Integer duration, String category) {
        return showMessage(message, caption, clazz, duration, category, null);
    }
    
    /**
     * Convenience method for displaying a simple text message.
     * 
     * @param message Message to display
     * @param title Caption for message.
     * @param clazz CSS classes to apply.
     * @param duration Message duration in milliseconds.
     * @param category Tag to classify message.
     * @param actionListener Optional listener for action invocation.
     * @return The newly created message pane.
     */
    public MessagePane showMessage(String message, String title, String clazz, int duration, String category,
                                   IEventListener actionListener) {
        MessagePane pane = new MessagePane(title, category, duration, actionListener != null);
        pane.addClass(clazz);
        pane.addChild(new Cell(message));
        
        if (actionListener != null) {
            pane.addEventListener("action", actionListener);
        }
        
        addChild(pane);
        return pane;
    }
    
}
