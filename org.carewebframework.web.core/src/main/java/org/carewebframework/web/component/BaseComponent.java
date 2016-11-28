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
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.IElementIdentifier;
import org.carewebframework.web.ancillary.INamespace;
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
    
    /**
     * An index of child component names for a parent component.
     */
    private class NameIndex {
        
        private Map<String, BaseComponent> names;
        
        public void add(BaseComponent component) {
            String name = component.getName();
            
            if (name != null) {
                names = names == null ? new HashMap<>() : names;
                names.put(name, component);
            }
        }
        
        public void remove(BaseComponent component) {
            String name = component.getName();
            
            if (name != null && names != null) {
                names.remove(name);
            }
        }
        
        private BaseComponent _get(String name) {
            return names == null ? null : names.get(name);
        }
        
        public void validate(BaseComponent component) {
            _validate(component, getNameRoot());
        }
        
        private void _validate(BaseComponent component, BaseComponent root) {
            _validate(component.getName(), root);
            
            for (BaseComponent child : component.getChildren()) {
                _validate(child, root);
            }
        }
        
        public void validate(String name) {
            _validate(name, getNameRoot());
        }
        
        private void _validate(String name, BaseComponent root) {
            if (name != null) {
                BaseComponent cmp = _find(name, root);
                
                if (cmp != null) {
                    throw new ComponentException("Name \"" + name + "\"already exists in current namespace");
                }
            }
        }
        
        private BaseComponent getNameRoot() {
            BaseComponent root = getNamespace();
            return root == null ? getRoot() : root;
        }
        
        public BaseComponent find(String name) {
            return _find(name, getNameRoot());
        }
        
        private BaseComponent _find(String name, BaseComponent root) {
            BaseComponent component = root.nameIndex._get(name);
            
            if (component != null) {
                return component;
            }
            
            for (BaseComponent child : root.getChildren()) {
                if (!(child instanceof INamespace)) {
                    component = _find(name, child);
                    
                    if (component != null) {
                        break;
                    }
                }
            }
            
            return component;
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
    
    private final ComponentDefinition componentDefinition;
    
    private final NameIndex nameIndex = new NameIndex();
    
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
        if (!areEqual(name = nullify(name), this.name)) {
            validateName(name);
            nameIndex.remove(this);
            this.name = name;
            nameIndex.add(this);
            sync("name", name);
        }
    }
    
    private void validateName(String name) {
        if (name != null) {
            if (!nameValidator.matcher(name).matches()) {
                throw new ComponentException(this, "Component name is not valid: " + name);
            }
            
            nameIndex.validate(name);
        }
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
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, T dflt) {
        try {
            T value = (T) attributes.get(name);
            return value == null ? dflt : value;
        } catch (ClassCastException e) {
            return dflt;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> clazz) {
        try {
            return (T) attributes.get(name);
        } catch (ClassCastException e) {
            return null;
        }
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
        nameIndex.validate(child);
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
        
        if (oldParent != null) {
            oldParent._removeChild(child, true, false);
        }
        
        if (index < 0) {
            children.add(child);
        } else {
            children.add(index, child);
        }
        
        child.parent = this;
        
        if (page != null) {
            child._attach(page);
        }
        
        nameIndex.add(child);
        
        if (!noSync) {
            invoke("addChild", child, index);
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
        nameIndex.remove(child);
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
    
    protected void beforeSetParent(BaseComponent newParent) {
    }
    
    protected void afterSetParent(BaseComponent oldParent) {
    }
    
    protected void beforeAddChild(BaseComponent child) {
    }
    
    protected void afterAddChild(BaseComponent child) {
    }
    
    protected void beforeRemoveChild(BaseComponent child) {
    }
    
    protected void afterRemoveChild(BaseComponent child) {
    }
    
    public List<BaseComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public <T extends BaseComponent> Iterable<T> getChildren(Class<T> type) {
        return MiscUtil.iterableForType(getChildren(), type);
    }
    
    /**
     * Returns the number of children.
     * 
     * @return The number of children.
     */
    public int getChildCount() {
        return children.size();
    }
    
    public int getChildCount(Class<? extends BaseComponent> type) {
        int count = 0;
        
        for (BaseComponent child : children) {
            if (type.isInstance(child)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Find the first child containing the specified data object.
     * 
     * @param data The data object to find.
     * @return The child with the data object, or null if not found.
     */
    public BaseComponent getChildByData(Object data) {
        for (BaseComponent child : children) {
            if (ObjectUtils.equals(data, child.getData())) {
                return child;
            }
        }
        
        return null;
    }
    
    /**
     * Returns true if this component may contain children.
     * 
     * @return True if this component may contain children.
     */
    public boolean isContainer() {
        return componentDefinition.childrenAllowed();
    }
    
    /**
     * Return the first child of the requested type.
     * 
     * @param <T> The type of child sought.
     * @param type The type of child sought.
     * @return The requested child, or null if none exist of the requested type.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T getChild(Class<T> type) {
        for (BaseComponent child : getChildren()) {
            if (type.isInstance(child)) {
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
    
    /**
     * Returns the first child of this component.
     * 
     * @return The first child, or null if no children.
     */
    public BaseComponent getFirstChild() {
        return getChildAt(0);
    }
    
    /**
     * Returns the last child of this component.
     * 
     * @return The last child, or null if no children.
     */
    public BaseComponent getLastChild() {
        return getChildAt(getChildCount() - 1);
    }
    
    /**
     * Return the root component of this component's hierarchy.
     * 
     * @return The root component of the hierarchy to which this component belongs.
     */
    public BaseComponent getRoot() {
        BaseComponent root = this;
        
        while (root.getParent() != null) {
            root = root.getParent();
        }
        
        return root;
    }
    
    /**
     * Return first ancestor that is of the requested type.
     * 
     * @param <T> The type of ancestor sought.
     * @param type The type of ancestor sought.
     * @return The ancestor component of the requested type, or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T getAncestor(Class<T> type) {
        return (T) getAncestorByType(type, false);
    }
    
    /**
     * Return first ancestor that is of the requested type.
     * 
     * @param type The type of ancestor sought.
     * @param includeSelf If true, include this component in the search.
     * @return The ancestor component of the requested type, or null if none found.
     */
    private BaseComponent getAncestorByType(Class<?> type, boolean includeSelf) {
        BaseComponent cmp = includeSelf ? this : this.getParent();
        
        while (cmp != null) {
            if (type.isInstance(cmp)) {
                break;
            } else {
                cmp = cmp.getParent();
            }
        }
        
        return cmp;
    }
    
    /**
     * Returns the index of this child within its parent.
     * 
     * @return Index of this child within its parent. If the component has not parent, returns -1.
     */
    public int indexOf() {
        return getParent() == null ? -1 : getParent().children.indexOf(this);
    }
    
    /**
     * Return the next sibling for this component.
     * 
     * @return The requested sibling, or null if not found.
     */
    public BaseComponent getNextSibling() {
        return getRelativeSibling(1);
    }
    
    /**
     * Return the previous sibling for this component.
     * 
     * @return The requested sibling, or null if not found.
     */
    public BaseComponent getPreviousSibling() {
        return getRelativeSibling(-1);
    }
    
    /**
     * Returns the sibling of this component at the specified offset.
     * 
     * @param offset Offset from this component. For example, 2 would mean the second sibling
     *            following this component.
     * @return The requested sibling, or null if none exists at the requested offset.
     */
    private BaseComponent getRelativeSibling(int offset) {
        int i = indexOf();
        i = i == -1 ? -1 : i + offset;
        return i < 0 || i >= getParent().getChildCount() ? null : getParent().children.get(i);
    }
    
    /**
     * Returns the namespace to which this component belongs. May be null.
     * 
     * @return The namespace to which this component belongs.
     */
    public BaseComponent getNamespace() {
        return getAncestorByType(INamespace.class, true);
    }
    
    /**
     * Returns the page to which this component belongs.
     * 
     * @return The owning page (may be null).
     */
    public Page getPage() {
        return page;
    }
    
    /**
     * Sets the page property for this component and its children.
     * 
     * @param page The owning page.
     */
    private void _setPage(Page page) {
        if (!validatePage(page)) {
            throw new ComponentException(this, "Component cannot be assigned to a different page");
        }
        
        this.page = page;
        page.registerComponent(this, true);
        Map<String, Object> props = new HashMap<>();
        _initProps(props);
        page.getSynchronizer().createWidget(parent, props, inits);
        inits = null;
        
        for (BaseComponent child : getChildren()) {
            child._setPage(page);
        }
    }
    
    /**
     * Validates that the specified page can be an owner of this component.
     * 
     * @param page The page to be tested.
     * @return True if the page can be an owner of this component.
     */
    protected boolean validatePage(Page page) {
        return page == this.page || this.page == null;
    }
    
    /**
     * Attach this component and its children to their owning page.
     * 
     * @param page Page to receive this component.
     */
    protected void _attach(Page page) {
        if (page != null && this.page != page) {
            _setPage(page);
            _flushQueue();
        }
    }
    
    /**
     * Creates this component's corresponding widget on the client.
     */
    private void _flushQueue() {
        if (invocationQueue != null) {
            page.getSynchronizer().processQueue(invocationQueue);
            invocationQueue = null;
        }
        
        EventHandlerScanner.wire(this, null);
        
        for (BaseComponent child : getChildren()) {
            child._flushQueue();
        }
    }
    
    /**
     * Initialize properties to be passed to widget factory. Override to add additional properties.
     * 
     * @param props Properties for widget factory.
     */
    protected void _initProps(Map<String, Object> props) {
        props.put("id", id);
        props.put("wclass", componentDefinition.getWidgetClass());
        props.put("wpkg", componentDefinition.getWidgetPackage());
        props.put("cntr", isContainer());
    }
    
    /**
     * Synchronize a state value to the client.
     * 
     * @param state The state name.
     * @param value The state value.
     */
    public void sync(String state, Object value) {
        if (getPage() == null) {
            if (inits == null) {
                inits = new HashMap<>();
            }
            
            inits.put(state, value);
        } else {
            page.getSynchronizer().invokeClient(this, "updateState", state, value, true);
        }
    }
    
    /**
     * Invoke a widget function on the client.
     * 
     * @param function The name of the function.
     * @param args Arguments for the function.
     */
    public void invoke(String function, Object... args) {
        ClientInvocation invocation = new ClientInvocation(this, function, args);
        
        if (page == null) {
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
     * @param name Component name or path. "^" in path means parent namespace.
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
            String pc = pcs[i++];
            
            if ("^".equals(pc)) {
                cmp = cmp.getNamespace();
                cmp = cmp == null ? null : cmp.getParent();
                cmp = cmp == null ? null : cmp.getNamespace();
            } else {
                cmp = cmp.nameIndex.find(pc);
            }
        }
        
        return cmp;
    }
    
    /**
     * Looks up a component of the specified type by its name within the namespace occupied by this
     * component.
     * 
     * @param <T> The expected return type.
     * @param name Component name or path.
     * @param type Expected return type.
     * @return The component sought, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T findByName(String name, Class<T> type) {
        return (T) findByName(name);
    }
    
    /**
     * Returns a subcomponent identifier.
     * 
     * @param subId The sub identifier.
     * @return A subcomponent object.
     */
    public SubComponent sub(String subId) {
        return new SubComponent(this, subId);
    }
    
    public void registerEventForward(String eventType, BaseComponent target, String forwardType) {
        registerEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    public void unregisterEventForward(String eventType, BaseComponent target, String forwardType) {
        unregisterEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    @PropertySetter(value = "forward", defer = true)
    private void setForward(String forwards) {
        forwards = trimify(forwards);
        
        if (forwards != null) {
            for (String forward : forwards.split("\\ ")) {
                if (!forward.isEmpty()) {
                    int i = forward.indexOf("=");
                    
                    if (i <= 0) {
                        throw new IllegalArgumentException(forward);
                    }
                    
                    String original = forward.substring(0, i);
                    forward = forward.substring(i + 1);
                    i = forward.lastIndexOf(".");
                    String name = i == -1 ? null : forward.substring(0, i);
                    forward = forward.substring(i + 1);
                    BaseComponent target = name == null ? this : findByName(name);
                    
                    if (target == null) {
                        throw new ComponentException(this, "No component named \"%s\" found", name);
                    }
                    
                    if (forward.isEmpty()) {
                        throw new IllegalArgumentException("No forward event specified");
                    }
                    
                    registerEventForward(original, target, forward);
                }
            }
        }
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
        
        if (controller instanceof IAutoWired) {
            ((IAutoWired) controller).afterInitialized(this);
        }
    }
    
    protected String nullify(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
    
    protected String trimify(String value) {
        return value == null ? null : nullify(value.trim());
    }
    
    protected <T> T defaultify(T value, T deflt) {
        return value == null ? deflt : value;
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
