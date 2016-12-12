package org.carewebframework.web.event;

/**
 * Wrapper for a forwarded event.
 */
public class ForwardedEvent extends Event {
    
    private final Event originalEvent;
    
    public ForwardedEvent(String forwardType, Event originalEvent) {
        super(forwardType, originalEvent.getTarget(), originalEvent.getRelatedTarget(), originalEvent.getData());
        this.originalEvent = originalEvent;
    }
    
    public Event getOriginalEvent() {
        return originalEvent;
    }
}
