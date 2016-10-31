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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.ancillary.ComponentException;
import org.carewebframework.web.ancillary.ConvertUtil;
import org.carewebframework.web.ancillary.IElementIdentifier;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.ancillary.NameRegistry;
import org.carewebframework.web.annotation.Component.AttributeProcessor;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.annotation.ComponentDefinition.FactoryContext;
import org.carewebframework.web.annotation.ComponentRegistry;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.EventHandlerScanner;
import org.carewebframework.web.annotation.WiredComponentScanner;
import org.carewebframework.web.client.ClientInvocation;
import org.carewebframework.web.client.ClientInvocationQueue;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventListeners;
import org.carewebframework.web.event.ForwardListener;
import org.carewebframework.web.event.IEventListener;
import org.springframework.util.Assert;

/**
 * Abstract base class for all components.
 */
public abstract class BaseComponent implements IElementIdentifier {
    
    /**
     * Reference to a subcomponent.
     */
    public static class SubComponent implements IElementIdentifier {
        
        private final BaseComponent component;
        
        private final String subId;
        
        private SubComponent(BaseComponent component, String subId) {
            this.component = component;
            this.subId = subId;
        }
        
        @Override
        public String getId() {
            return component.getId() + "-" + subId;
        }
    }
    
    private static final Pattern nameValidator = Pattern.compile("^[a-zA-Z$][a-zA-Z_$0-9]*$");
    
    private String name;
    
    private String id;
    
    private Page page;
    
    private BaseComponent parent;
    
    private Object data;
    
    private Map<String, Object> inits;
    
    private ClientInvocationQueue invocationQueue;
    
    private final List<BaseComponent> children = new LinkedList<>();
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    private final EventListeners eventListeners = new EventListeners();
    
    private final NameRegistry nameRegistry;
    
    private final ComponentDefinition componentDefinition;
    
    protected static boolean isDead(IElementIdentifier id) {
        return DEAD_ID.equals(id.getId());
    }
    
    protected static void validate(IElementIdentifier id) {
        if (id != null && isDead(id)) {
            throw new ComponentException("Element no longer exists: %s.", id);
        }
    }
    
    @AttributeProcessor("impl")
    private static void implProcessor(String value, FactoryContext context) throws ClassNotFoundException {
        context.setComponentClass(Class.forName(value));
    }
    
    @AttributeProcessor("if")
    private static void ifProcessor(boolean value, FactoryContext context) {
        if (!value) {
            context.terminate();
        }
    }
    
    @AttributeProcessor("unless")
    private static void unlessProcessor(boolean value, FactoryContext context) {
        if (value) {
            context.terminate();
        }
    }
    
    public BaseComponent() {
        componentDefinition = ComponentRegistry.getInstance().get(getClass());
        nameRegistry = this instanceof INamespace ? new NameRegistry() : null;
    }
    
    public ComponentDefinition getDefinition() {
        return componentDefinition;
    }
    
    @PropertyGetter("name")
    public String getName() {
        return name;
    }
    
    @PropertySetter("name")
    public void setName(String name) {
        name = nullify(name);
        
        if (name != null && !validateName(name)) {
            throw new ComponentException(this, "Component name is not valid: " + name);
        }
        
        if (!areEqual(this.name, name)) {
            String oldName = this.name;
            
            try {
                registerToNamespace(false, false);
                this.name = name;
                registerToNamespace(true, false);
            } catch (Exception e) {
                this.name = oldName;
                registerToNamespace(true, false);
                throw e;
            }
            
            sync("name", this.name);
        }
    }
    
    protected boolean validateName(String name) {
        return nameValidator.matcher(name).matches();
    }
    
    @Override
    @PropertyGetter("id")
    public String getId() {
        return id;
    }
    
    /*package*/ void _setId(String id) {
        Assert.isNull(this.id, "Unique id cannot be modified.");
        this.id = id;
    }
    
    public void detach() {
        setParent(null);
    }
    
    public void destroy() {
        if (isDead()) {
            return;
        }
        
        onDestroy();
        
        if (page != null) {
            page.registerComponent(this, false);
        }
        
        destroyChildren();
        
        if (parent != null) {
            parent._removeChild(this, false, true);
        } else {
            invoke("destroy");
        }
        
        id = DEAD_ID;
    }
    
    public void destroyChildren() {
        while (!children.isEmpty()) {
            children.get(0).destroy();
        }
    }
    
    protected void onDestroy() {
    }
    
    public boolean isDead() {
        return isDead(this);
    }
    
    protected void validate() {
        validate(this);
    }
    
    public BaseComponent getParent() {
        return parent;
    }
    
    protected boolean validateParent(BaseComponent parent) {
        return parent == null || componentDefinition.isParentTag(parent.componentDefinition.getTag());
    }
    
    public void setParent(BaseComponent parent) {
        if (parent != this.parent) {
            if (parent == null) {
                this.parent.removeChild(this);
            } else if (validateParent(parent)) {
                parent.addChild(this);
            } else {
                throw new ComponentException(this, "Not a valid parent: " + parent.getClass().getName());
            }
        }
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public Object findAttribute(String name) {
        Object value = null;
        BaseComponent cmp = this;
        
        while ((value = cmp.attributes.get(name)) == null && getParent() != null) {
            cmp = cmp.getParent();
        }
        
        return value;
    }
    
    public Object setAttribute(String name, Object value) {
        return attributes.put(name, value);
    }
    
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }
    
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }
    
    protected void validateIsChild(BaseComponent child) {
        if (child != null && child.getParent() != this) {
            throw new ComponentException("Child does not belong to this parent.");
        }
    }
    
    protected void validateChild(BaseComponent child) {
        componentDefinition.validateChild(child.componentDefinition, () -> getChildCount(child.getClass()));
    }
    
    public void addChild(BaseComponent child) {
        addChild(child, -1);
    }
    
    public void addChild(BaseComponent child, int index) {
        _addChild(child, index, false);
    }
    
    public void _addChild(BaseComponent child, int index, boolean noSync) {
        child.validate();
        validateChild(child);
        BaseComponent before = index < 0 || index == children.size() ? null : children.get(index);
        
        if (child == before) {
            return;
        }
        
        if (!child.validatePage(page)) {
            throw new ComponentException(this, "Child is already associated with a different page.");
        }
        
        child.beforeSetParent(this);
        beforeAddChild(child);
        BaseComponent oldParent = child.getParent();
        int oldIndex = -1;
        
        if (oldParent != null) {
            child.registerToNamespace(false, true);
            oldIndex = oldParent.children.indexOf(child);
            oldParent._removeChild(child, true, false);
        }
        
        try {
            if (index < 0) {
                children.add(child);
            } else {
                children.add(index, child);
            }
            
            child.parent = this;
            
            if (page != null) {
                child._setPage(page);
            }
            
            child.registerToNamespace(true, true);
            
            if (!noSync) {
                invoke("addChild", child, index);
            }
        } catch (Exception e) {
            children.remove(child);
            child.parent = oldParent;
            
            if (oldParent != null) {
                oldParent.children.add(oldIndex, child);
                child.registerToNamespace(true, true);
            }
            
            throw e;
        }
        
        afterAddChild(child);
        child.afterSetParent(this);
    }
    
    public void addChildren(Collection<? extends BaseComponent> children) {
        for (BaseComponent child : children) {
            addChild(child);
        }
    }
    
    public void insertChild(BaseComponent child, BaseComponent before) {
        if (before == null) {
            addChild(child);
            return;
        }
        
        if (before.getParent() != this) {
            throw new ComponentException(this, "Before component does not belong to this parent.");
        }
        
        int i = children.indexOf(before);
        addChild(child, i);
    }
    
    public void removeChild(BaseComponent child) {
        _removeChild(child, false, false);
    }
    
    /*package*/ void _removeChild(BaseComponent child, boolean noSync, boolean destroy) {
        int index = children.indexOf(child);
        
        if (index == -1) {
            throw new ComponentException(this, "Child does not belong to this parent.");
        }
        
        beforeRemoveChild(child);
        child.registerToNamespace(false, true);
        child.parent = null;
        children.remove(child);
        
        if (!noSync) {
            invoke("removeChild", child, destroy);
        }
        
        afterRemoveChild(child);
    }
    
    public void swapChildren(int index1, int index2) {
        BaseComponent child1 = children.get(index1);
        BaseComponent child2 = children.get(index2);
        children.set(index1, child2);
        children.set(index2, child1);
        invoke("swapChildren", index1, index2);
    }
    
    public void beforeSetParent(BaseComponent newParent) {
    }
    
    public void afterSetParent(BaseComponent oldParent) {
    }
    
    public void beforeAddChild(BaseComponent child) {
    }
    
    public void afterAddChild(BaseComponent child) {
    }
    
    public void beforeRemoveChild(BaseComponent child) {
    }
    
    public void afterRemoveChild(BaseComponent child) {
    }
    
    public List<BaseComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public <T extends BaseComponent> Iterable<T> getChildren(Class<T> clazz) {
        return MiscUtil.iterableForType(getChildren(), clazz);
    }
    
    public int getChildCount() {
        return children.size();
    }
    
    public int getChildCount(Class<? extends BaseComponent> clazz) {
        int count = 0;
        
        for (BaseComponent child : children) {
            if (clazz.isInstance(child)) {
                count++;
            }
        }
        
        return count;
    }
    
    public boolean isContainer() {
        return componentDefinition.childrenAllowed();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T getChild(Class<T> clazz) {
        for (BaseComponent child : getChildren()) {
            if (clazz.isInstance(child)) {
                return (T) child;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the child at the specified index. If the index is out of bounds, returns null.
     * 
     * @param index The index of the child sought.
     * @return Child at the specified index or null if the index is out of bounds.
     */
    public BaseComponent getChildAt(int index) {
        return index < 0 || index >= children.size() ? null : children.get(index);
    }
    
    public BaseComponent getFirstChild() {
        return getChildAt(0);
    }
    
    public BaseComponent getLastChild() {
        return getChildAt(getChildCount() - 1);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T getAncestor(Class<T> clazz) {
        return (T) getAncestorByClass(clazz, false);
    }
    
    public BaseComponent getNextSibling() {
        return getRelativeSibling(1);
    }
    
    public BaseComponent getPreviousSibling() {
        return getRelativeSibling(-1);
    }
    
    /**
     * Returns the index of this child within its parent.
     * 
     * @return Index of this child within its parent. If the component has not parent, returns -1.
     */
    public int indexOf() {
        return getParent() == null ? -1 : getParent().children.indexOf(this);
    }
    
    private BaseComponent getRelativeSibling(int offset) {
        int i = indexOf();
        i = i == -1 ? -1 : i + offset;
        return i < 0 ? null : i >= getParent().getChildCount() ? null : getParent().children.get(i);
    }
    
    public BaseComponent getNamespace() {
        return getAncestorByClass(INamespace.class, true);
    }
    
    private BaseComponent getAncestorByClass(Class<?> clazz, boolean includeSelf) {
        BaseComponent cmp = includeSelf ? this : this.getParent();
        
        while (cmp != null) {
            if (clazz.isInstance(cmp)) {
                break;
            } else {
                cmp = cmp.getParent();
            }
        }
        
        return cmp;
    }
    
    public Page getPage() {
        return page;
    }
    
    protected void _setPage(Page page) {
        if (page == this.page) {
            return;
        }
        
        if (!validatePage(page)) {
            throw new ComponentException(this, "Component cannot be assigned to a different page");
        }
        
        this.page = page;
        page.registerComponent(this, true);
        _init();
        
        for (BaseComponent child : getChildren()) {
            child._setPage(page);
        }
    }
    
    protected boolean validatePage(Page page) {
        return page == this.page || this.page == null;
    }
    
    protected void _init() {
        Map<String, Object> props = new HashMap<>();
        _init(props);
        page.getSynchronizer().createWidget(parent, props, inits, invocationQueue);
        invocationQueue = null;
        inits = null;
        EventHandlerScanner.wire(this, null);
    }
    
    protected void _init(Map<String, Object> props) {
        props.put("id", id);
        props.put("wclass", componentDefinition.getWidgetClass());
        props.put("wpkg", componentDefinition.getWidgetPackage());
        props.put("cntr", isContainer());
    }
    
    public void sync(String state, Object value) {
        if (getPage() == null) {
            if (inits == null) {
                inits = new HashMap<>();
            }
            
            inits.put(state, value);
        } else {
            page.getSynchronizer().invokeClient(this, "updateState", state, value);
        }
    }
    
    public void invoke(String function, Object... args) {
        ClientInvocation invocation = new ClientInvocation(this, function, args);
        
        if (getPage() == null) {
            if (invocationQueue == null) {
                invocationQueue = new ClientInvocationQueue();
            }
            
            invocationQueue.queue(invocation);
        } else {
            page.getSynchronizer().sendToClient(invocation);
        }
    }
    
    /**
     * Looks up a component by its name within the namespace occupied by this component.
     * 
     * @param name Component name or path.
     * @return The component sought, or null if not found.
     */
    public BaseComponent findByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        String[] pcs = name.split("\\.");
        BaseComponent cmp = this;
        int i = 0;
        
        while (i < pcs.length && cmp != null) {
            BaseComponent namespace = cmp.getNamespace();
            cmp = namespace == null ? null : namespace.nameRegistry.get(pcs[i++]);
        }
        
        return cmp;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T findByName(String name, Class<T> clazz) {
        return (T) findByName(name);
    }
    
    public SubComponent sub(String subId) {
        return new SubComponent(this, subId);
    }
    
    private void registerToNamespace(boolean register, boolean recurse) {
        if (name == null) {
            return;
        }
        
        BaseComponent namespace = parent == null ? null : parent.getNamespace();
        
        if (namespace != null) {
            registerToNamespace(register, recurse, namespace.nameRegistry);
        }
    }
    
    private void registerToNamespace(boolean register, boolean recurse, NameRegistry nameRegistry) {
        nameRegistry.register(this, register);
        
        if (recurse && !(this instanceof INamespace)) {
            for (BaseComponent child : getChildren()) {
                child.registerToNamespace(register, recurse, nameRegistry);
            }
        }
    }
    
    public void registerEventForward(String eventType, BaseComponent target, String forwardType) {
        registerEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    public void unregisterEventForward(String eventType, BaseComponent target, String forwardType) {
        unregisterEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    private ForwardListener createForwardListener(String eventType, BaseComponent target, String forwardType) {
        return new ForwardListener(forwardType == null ? eventType : forwardType, target == null ? this : target);
    }
    
    public void registerEventListener(String eventType, IEventListener eventListener) {
        updateEventListener(eventType, eventListener, true, true);
    }
    
    public void registerEventListener(String eventType, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventType, eventListener, true, syncToClient);
    }
    
    public void unregisterEventListener(String eventType, IEventListener eventListener) {
        updateEventListener(eventType, eventListener, false, true);
    }
    
    public void unregisterEventListener(String eventType, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventType, eventListener, false, syncToClient);
    }
    
    private void updateEventListener(String eventType, IEventListener eventListener, boolean register,
                                     boolean syncToClient) {
        boolean before = eventListeners.hasListeners(eventType);
        
        if (register) {
            eventListeners.register(eventType, eventListener);
        } else {
            eventListeners.unregister(eventType, eventListener);
        }
        
        if (syncToClient && before != eventListeners.hasListeners(eventType)) {
            syncEventListeners(eventType, before);
        }
    }
    
    private void syncEventListeners(String eventType, boolean remove) {
        invoke("forwardToServer", eventType, remove);
    }
    
    public void fireEvent(Event event) {
        eventListeners.invoke(event);
    }
    
    @PropertySetter(value = "controller", defer = true)
    public void wireController(Object controller) {
        if (controller instanceof String) {
            try {
                controller = Class.forName((String) controller).newInstance();
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        WiredComponentScanner.wire(controller, this);
        EventHandlerScanner.wire(controller, this);
    }
    
    protected String nullify(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
    
    protected String trimify(String value) {
        return value == null ? null : nullify(value.trim());
    }
    
    protected boolean areEqual(Object s1, Object s2) {
        return ObjectUtils.equals(s1, s2);
    }
    
    public Object getData() {
        return data;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> type) {
        return type.isInstance(data) ? (T) data : null;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @EventHandler(value = "stateChange", syncToClient = false)
    private void _onStateChange(Event event) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) event.getData();
        String state = (String) params.get("state");
        
        try {
            Field field = FieldUtils.getField(this.getClass(), state, true);
            field.set(this, ConvertUtil.convert(params.get("value"), field.getType(), this));
        } catch (Exception e) {
            throw new RuntimeException("Error updating state: " + state, e);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        //@formatter:off
        sb.append(getClass().getName()).append(", ")
        .append("id: ").append(id).append(", ")
        .append("name: ").append(name);
        //@formatter:on
        
        return sb.toString();
    }
}
