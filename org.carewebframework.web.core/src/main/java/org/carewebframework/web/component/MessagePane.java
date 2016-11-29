package org.carewebframework.web.component;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.EventHandler;

@Component(value = "messagepane", widgetClass = "Messagepane", content = ContentHandling.AS_CHILD, parentTag = "messagewindow", childTag = @ChildTag("*"))
public class MessagePane extends BaseUIComponent {
    
    private String title;
    
    private int duration = 8000;
    
    private String category;
    
    private boolean actionable;
    
    public MessagePane() {
        
    }
    
    public MessagePane(String title, String category, int duration, boolean actionable) {
        setTitle(title);
        setCategory(category);
        setDuration(duration);
        setActionable(actionable);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        if (!areEqual(title = nullify(title), this.title)) {
            sync("title", this.title = title);
        }
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        if (duration != this.duration) {
            sync("duration", this.duration = duration);
        }
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = nullify(category);
    }
    
    public boolean isActionable() {
        return actionable;
    }
    
    public void setActionable(boolean actionable) {
        if (actionable != this.actionable) {
            sync("actionable", this.actionable = actionable);
        }
    }
    
    @EventHandler(value = "close", syncToClient = false)
    private void _close() {
        destroy();
    }
}
