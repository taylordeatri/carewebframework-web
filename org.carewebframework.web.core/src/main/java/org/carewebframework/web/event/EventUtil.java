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
package org.carewebframework.web.event;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.annotation.EventParameterScanner;
import org.carewebframework.web.annotation.EventTypeScanner;
import org.carewebframework.web.client.ClientRequest;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;

/**
 * Static methods for manipulating events.
 */
public class EventUtil {
    
    /**
     * Sends an event to its designated target.
     * 
     * @param event The event to send. If the event's designated target is null, the event is sent
     *            to the page in the current execution context.
     */
    public static void send(Event event) {
        BaseComponent target = event.getCurrentTarget();
        send(event, target);
    }
    
    /**
     * Sends an event to the specified target.
     * 
     * @param event The event to send.
     * @param target The target for the event. If the target is null, the event is sent to the page
     *            in the current execution context.
     */
    public static void send(Event event, BaseComponent target) {
        target = target == null ? ExecutionContext.getPage() : target;
        target.fireEvent(event);
    }
    
    /**
     * Creates and sends an event to the specified target.
     * 
     * @param eventName The event name.
     * @param target The event target.
     * @param data Optional event data.
     * @return The newly created event.
     */
    public static Event send(String eventName, BaseComponent target, Object data) {
        Event event = new Event(eventName, target, data);
        send(event);
        return event;
    }
    
    /**
     * Queues an event for later processing.
     * 
     * @param event The event to queue.
     */
    public static void post(Event event) {
        post(ExecutionContext.getPage(), event);
    }
    
    /**
     * Queues an event for later processing.
     * 
     * @param page Page owning the target queue.
     * @param event The event to queue.
     */
    public static void post(Page page, Event event) {
        page.getEventQueue().queue(event);
    }
    
    /**
     * Creates and posts an event for later delivery.
     * 
     * @param eventName The event name.
     * @param target The event target.
     * @param data Optional event data.
     * @return The newly created event.
     */
    public static Event post(String eventName, BaseComponent target, Object data) {
        Event event = new Event(eventName, target, data);
        post(event);
        return event;
    }
    
    /**
     * Creates and posts an event for later delivery.
     * 
     * @param page Page owning the target queue.
     * @param eventName The event name.
     * @param target The event target.
     * @param data Optional event data.
     * @return The newly created event.
     */
    public static Event post(Page page, String eventName, BaseComponent target, Object data) {
        Event event = new Event(eventName, target, data);
        post(page, event);
        return event;
    }
    
    /**
     * Returns the implementation class for the specified event type. If there is no implementation
     * specific to the event type, the Event class will be returned.
     * 
     * @param eventType The event type (with or without the "on" prefix).
     * @return The implementation class for the event type (never null).
     */
    public static Class<? extends Event> getEventClass(String eventType) {
        return EventTypeScanner.getInstance().getEventClass(stripOn(eventType));
    }
    
    /**
     * Returns the event type given the implementation class.
     * 
     * @param eventClass The event class.
     * @return The corresponding event type, or null if none.
     */
    public static String getEventType(Class<? extends Event> eventClass) {
        return EventTypeScanner.getInstance().getEventType(eventClass);
    }
    
    /**
     * Strips the "on" prefix from an event type, if one is present.
     * 
     * @param eventType The event type.
     * @return The event type stripped of the "on" prefix.
     */
    public static String stripOn(String eventType) {
        return eventType.startsWith("on") ? StringUtils.uncapitalize(eventType.substring(2)) : eventType;
    }
    
    /**
     * Invokes an event handler.
     * 
     * @param handlerName The name of the handler method.
     * @param instance The instance implementing the handler method.
     * @param event The event.
     * @return True if the event was handled.
     */
    public static boolean invokeHandler(String handlerName, Object instance, Event event) {
        Method method = getHandler(handlerName, instance, event);
        
        if (method != null) {
            try {
                if (method.getParameterCount() == 1) {
                    method.invoke(instance, event);
                } else {
                    method.invoke(instance);
                }
                
                return true;
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        return false;
    }
    
    /**
     * Returns a suitable handler method for an event. First, it searches for a method with a single
     * argument that assignment-compatible with the event. Failing that, it searches for a method
     * with no arguments. Failing that, it returns null.
     * 
     * @param handlerName The name of the handler method (defaults to a method with the same name as
     *            the event type).
     * @param instance The instance implementing the handler method.
     * @param event The event.
     * @return The handler method, or null if a suitable method could not be found.
     */
    public static Method getHandler(String handlerName, Object instance, Event event) {
        handlerName = handlerName != null ? handlerName : event.getType();
        Method method = MethodUtils.getMatchingAccessibleMethod(instance.getClass(), handlerName,
            new Class<?>[] { event.getClass() });
        return method != null ? method
                : MethodUtils.getAccessibleMethod(instance.getClass(), handlerName, ArrayUtils.EMPTY_CLASS_ARRAY);
        
    }
    
    /**
     * Returns true if the client request contains an event.
     * 
     * @param request The client request.
     * @return True if the request contains an event.
     */
    public static boolean hasEvent(ClientRequest request) {
        return "event".equals(request.getType()) && request.getData() instanceof Map;
    }
    
    /**
     * Extracts an event from the client request.
     * 
     * @param request The client request.
     * @return The extracted event.
     * @exception IllegalArgumentException If the request does not contain an event.
     */
    public static Event toEvent(ClientRequest request) {
        if (hasEvent(request)) {
            String type = (String) request.getData(Map.class).get("type");
            return toEvent(getEventClass(type), request);
        }
        
        throw new IllegalArgumentException("Request does not contain an event");
    }
    
    /**
     * Creates an event from the specified event type. If an event class exists for the specified
     * type, will create an event of that class. Otherwise, will create a generic event.
     * 
     * @param eventType The event type.
     * @return The newly created event.
     */
    public static Event toEvent(String eventType) {
        eventType = stripOn(eventType);
        Class<? extends Event> eventClass = getEventClass(eventType);
        
        try {
            return eventClass == Event.class ? new Event(eventType) : eventClass.newInstance();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Extracts an event of the expected class from the client request.
     * 
     * @param clazz The expected class.
     * @param request The client request.
     * @return An event of the expected class, or null if the class is not a subclass of Event, or
     *         if a suitable constructor could not be found.
     */
    private static Event toEvent(Class<?> clazz, ClientRequest request) {
        try {
            if (Event.class.isAssignableFrom(clazz)) {
                Event event = (Event) clazz.newInstance();
                EventParameterScanner.wire(event, request);
                return event;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            return toEvent(clazz.getSuperclass(), request);
        }
        
        return null;
    }
    
    private EventUtil() {
    }
    
}
