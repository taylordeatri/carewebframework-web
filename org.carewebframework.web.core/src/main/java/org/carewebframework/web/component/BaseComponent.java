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
import org.carewebframework.web.ancillary.ILabeled;
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
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.ForwardListener;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.StatechangeEvent;
import org.springframework.util.Assert;

/**
 * The abstract base class for all components.
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
     * An index of child component names maintained by a parent component.
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
            _validate(component.getName(), root, component);
            
            if (!(component instanceof INamespace)) {
                for (BaseComponent child : component.getChildren()) {
                    _validate(child, root);
                }
            }
        }
        
        private void validate(String name) {
            _validate(name, getNameRoot(), null);
        }
        
        private void _validate(String name, BaseComponent root, BaseComponent component) {
            if (name != null) {
                BaseComponent cmp = _find(name, root);
                
                if (cmp != null && cmp != component) {
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
    
    private String content;
    
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
        EventHandlerScanner.wire(this, this);
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
            invokeIfAttached("destroy");
        }
        
        id = DEAD_ID;
        fireEvent("destroy");
        eventListeners.removeAll();
    }
    
    @Override
    public void finalize() throws Throwable {
        super.finalize();
        
        if (id != null) {
            destroy();
        }
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
            Object value = attributes.get(name);
            return value == null ? dflt : dflt == null ? (T) value : (T) dflt.getClass().cast(value);
        } catch (Exception e) {
            return dflt;
        }
    }
    
    public <T> T getAttribute(String name, Class<T> clazz) {
        try {
            return ConvertUtil.convert(attributes.get(name), clazz, this);
        } catch (Exception e) {
            return null;
        }
    }
    
    public Object findAttribute(String name) {
        Object value = null;
        BaseComponent cmp = this;
        
        while (cmp != null && (value = cmp.attributes.get(name)) == null) {
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
    
    public void validateIsChild(BaseComponent child) {
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
        boolean noSync = child.getPage() == null && index < 0;
        child.validate();
        BaseComponent oldParent = child.getParent();
        
        if (oldParent != this) {
            validateChild(child);
            nameIndex.validate(child);
        }
        
        if (!child.validatePage(page)) {
            throw new ComponentException(this, "Child is already associated with a different page.");
        }
        
        if (oldParent == this) {
            int i = child.getIndex();
            
            if (i == index) {
                return;
            }
            
            if (index > i) {
                index--;
            }
        } else {
            child.beforeSetParent(this);
            beforeAddChild(child);
        }
        
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
            invokeIfAttached("addChild", child, index);
        }
        
        if (oldParent != this) {
            afterAddChild(child);
            child.afterSetParent(this);
        }
    }
    
    public void addChild(BaseComponent child, BaseComponent before) {
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
    
    public void addChildren(Collection<? extends BaseComponent> children) {
        for (BaseComponent child : children) {
            addChild(child);
        }
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
            invokeIfAttached("removeChild", child, destroy);
        }
        
        afterRemoveChild(child);
    }
    
    public void swapChildren(int index1, int index2) {
        BaseComponent child1 = children.get(index1);
        BaseComponent child2 = children.get(index2);
        children.set(index1, child2);
        children.set(index2, child1);
        invokeIfAttached("swapChildren", index1, index2);
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
    public <T extends BaseComponent> T getAncestor(Class<T> type) {
        return getAncestor(type, false);
    }
    
    /**
     * Return first ancestor that is of the requested type.
     *
     * @param <T> Expected return type.
     * @param type The type of ancestor sought.
     * @param includeSelf If true, include this component in the search.
     * @return The ancestor component of the requested type, or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAncestor(Class<T> type, boolean includeSelf) {
        BaseComponent cmp = includeSelf ? this : this.getParent();
        
        while (cmp != null) {
            if (type.isInstance(cmp)) {
                break;
            } else {
                cmp = cmp.getParent();
            }
        }
        
        return (T) cmp;
    }
    
    /**
     * Returns true if this component is the same as or an ancestor of the specified component.
     *
     * @param comp Component to test.
     * @return True if this component is the same as or an ancestor of the specified component.
     */
    public boolean isAncestor(BaseComponent comp) {
        while (comp != null && comp != this) {
            comp = comp.getParent();
        }
        
        return comp != null;
    }
    
    /**
     * Returns the index of this child within its parent.
     *
     * @return Index of this child within its parent. If the component has not parent, returns -1.
     */
    public int getIndex() {
        return getParent() == null ? -1 : getParent().children.indexOf(this);
    }
    
    /**
     * Moves this child to the specified index within its parent.
     *
     * @param index New index for this child.
     */
    public void setIndex(int index) {
        getParent().addChild(this, index);
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
        int i = getIndex();
        i = i == -1 ? -1 : i + offset;
        return i < 0 || i >= getParent().getChildCount() ? null : getParent().children.get(i);
    }
    
    /**
     * Returns the namespace to which this component belongs. May be null.
     *
     * @return The namespace to which this component belongs.
     */
    public BaseComponent getNamespace() {
        return (BaseComponent) getAncestor(INamespace.class, true);
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
    
    protected void onAttach(Page page) {
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
        
        for (BaseComponent child : getChildren()) {
            child._flushQueue();
        }
        
        onAttach(page);
        fireEvent("attach");
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
        props.put("nmsp", this instanceof INamespace ? true : null);
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
        invoke(this, function, args);
    }
    
    /**
     * Invoke a widget function on the client only if attached to a page.
     *
     * @param function The name of the function.
     * @param args Arguments for the function.
     */
    public void invokeIfAttached(String function, Object... args) {
        if (page != null) {
            invoke(function, args);
        }
    }
    
    /**
     * Invoke a widget or sub-widget function on the client.
     *
     * @param id The id of the widget or sub-widget.
     * @param function The name of the function.
     * @param args Arguments for the function.
     */
    public void invoke(IElementIdentifier id, String function, Object... args) {
        ClientInvocation invocation = new ClientInvocation(id, function, args);
        
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
                
                if (i != pcs.length) {
                    cmp = cmp == null ? null : cmp.getParent();
                    cmp = cmp == null ? null : cmp.getNamespace();
                }
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
     * Find the first child containing the specified data object.
     *
     * @param data The data object to find.
     * @return The child with the data object, or null if not found.
     */
    public BaseComponent findChildByData(Object data) {
        for (BaseComponent child : children) {
            if (ObjectUtils.equals(data, child.getData())) {
                return child;
            }
        }
        
        return null;
    }
    
    /**
     * Find the first child whose label matches the specified value. This will only examine children
     * that implement the ILabeled interface.
     *
     * @param label The label to find.
     * @return The first child whose label matches, or null if none found.
     */
    public BaseComponent findChildByLabel(String label) {
        for (BaseComponent comp : getChildren()) {
            if (comp instanceof ILabeled && label.equals(((ILabeled) comp).getLabel())) {
                return comp;
            }
        }
        
        return null;
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
    
    /**
     * Causes one or more events to be forwarded. Multiple entries must be separated by a space.
     *
     * @param forwards One or more forwarding directives, multiple entries separated by spaces. Each
     *            entry is of the form <code>event=target</code> where <code>event</code> is the
     *            name of the event to forward and <code>target</code> is the forwarding target. The
     *            target is the name of the forwarded event optionally prefixed by a resolvable
     *            component name or path. In the absence of such a prefix, the target is the
     *            component itself. For example, <code>click=window.listbox.change</code> would
     *            forward this component's <code>click</code> event as a <code>change</code> event
     *            targeting the component that is the result of resolving the
     *            <code>window.listbox</code> path.
     */
    @PropertySetter(value = "forward", defer = true)
    private void setForward(String forwards) {
        forwards = trimify(forwards);
        
        if (forwards != null) {
            for (String forward : forwards.split("\\ ")) {
                if (!forward.isEmpty()) {
                    int i = forward.indexOf("=");
                    
                    if (i <= 0) {
                        throw new IllegalArgumentException("Illegal forward directive:  " + forward);
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
                    
                    addEventForward(original, target, forward);
                }
            }
        }
    }
    
    public void addEventForward(String eventType, BaseComponent target, String forwardType) {
        addEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    public void addEventForward(Class<? extends Event> eventClass, BaseComponent target, String forwardType) {
        String eventType = getEventType(eventClass);
        addEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    public void removeEventForward(String eventType, BaseComponent target, String forwardType) {
        removeEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    public void removeEventForward(Class<? extends Event> eventClass, BaseComponent target, String forwardType) {
        String eventType = getEventType(eventClass);
        removeEventListener(eventType, createForwardListener(eventType, target, forwardType));
    }
    
    private ForwardListener createForwardListener(String eventType, BaseComponent target, String forwardType) {
        return new ForwardListener(forwardType == null ? eventType : forwardType, target == null ? this : target);
    }
    
    public boolean hasEventListener(String eventType) {
        return eventListeners.hasListeners(eventType);
    }
    
    public boolean hasEventListener(Class<? extends Event> eventClass) {
        return hasEventListener(getEventType(eventClass));
    }
    
    public void addEventListener(String eventType, IEventListener eventListener) {
        updateEventListener(eventType, eventListener, true, true);
    }
    
    public void addEventListener(Class<? extends Event> eventClass, IEventListener eventListener) {
        updateEventListener(eventClass, eventListener, true, true);
    }
    
    public void addEventListener(String eventType, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventType, eventListener, true, syncToClient);
    }
    
    public void addEventListener(Class<? extends Event> eventClass, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventClass, eventListener, true, syncToClient);
    }
    
    public void removeEventListener(String eventType, IEventListener eventListener) {
        updateEventListener(eventType, eventListener, false, true);
    }
    
    public void removeEventListener(Class<? extends Event> eventClass, IEventListener eventListener) {
        updateEventListener(eventClass, eventListener, false, true);
    }
    
    public void removeEventListener(String eventType, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventType, eventListener, false, syncToClient);
    }
    
    public void removeEventListener(Class<? extends Event> eventClass, IEventListener eventListener, boolean syncToClient) {
        updateEventListener(eventClass, eventListener, false, syncToClient);
    }
    
    private void updateEventListener(Class<? extends Event> eventClass, IEventListener eventListener, boolean register,
                                     boolean syncToClient) {
        updateEventListener(getEventType(eventClass), eventListener, register, syncToClient);
        
    }
    
    private void updateEventListener(String eventTypes, IEventListener eventListener, boolean register,
                                     boolean syncToClient) {
        for (String eventType : eventTypes.split("\\ ")) {
            boolean before = eventListeners.hasListeners(eventType);
            
            if (register) {
                eventListeners.add(eventType, eventListener);
            } else {
                eventListeners.remove(eventType, eventListener);
            }
            
            if (syncToClient && before != eventListeners.hasListeners(eventType)) {
                syncEventListeners(eventType, before);
            }
        }
    }
    
    private void syncEventListeners(String eventType, boolean remove) {
        invoke("forwardToServer", eventType, remove);
    }
    
    /**
     * Returns the event type given its implementation class, throwing an exception if not a
     * concrete class.
     *
     * @param eventClass The event class.
     * @return The event type (never null).
     */
    private String getEventType(Class<? extends Event> eventClass) {
        String eventType = EventUtil.getEventType(eventClass);
        
        if (eventType == null) {
            throw new IllegalArgumentException("Not a concrete event type: " + eventClass);
        }
        
        return eventType;
    }
    
    public void fireEvent(String eventType) {
        fireEvent(eventType, null);
    }
    
    protected void fireEvent(String eventType, BaseComponent relatedTarget) {
        if (hasEventListener(eventType)) {
            fireEvent(new Event(eventType, this, relatedTarget));
        }
    }
    
    /**
     * Send an event to this component's registered event listeners.
     *
     * @param event Event to send.
     */
    public void fireEvent(Event event) {
        eventListeners.invoke(event);
    }
    
    /**
     * Send an event to all the ancestors of this component. Event propagation stops if any
     * recipient invokes the <code>stopPropagation</code> method on the event.
     *
     * @param event Event to send.
     * @param includeThis If true, include this component in the recipient chain.
     */
    public void notifyAncestors(Event event, boolean includeThis) {
        BaseComponent next = includeThis ? this : getParent();
        
        while (next != null && !event.isStopped()) {
            next.fireEvent(event);
            next = next.getParent();
        }
    }
    
    /**
     * Wires a controller's annotated components and event handlers, using this component to resolve
     * name references.
     *
     * @param controller The controller to wire. If a string, is assumed to be the name of the
     *            controller's implementation class in which case an instance of that class is
     *            created.
     */
    @PropertySetter(value = "controller", defer = true)
    public void wireController(Object controller) {
        if (controller == null) {
            throw new ComponentException("Controller is null or could not be resolved.");
        }
        
        if (controller instanceof String) {
            try {
                controller = "self".equals(controller) ? this : Class.forName((String) controller).newInstance();
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
    
    /**
     * Override to cause a UI component to be brought to the forefront of the UI.
     */
    public void bringToFront() {
        if (getParent() != null) {
            getParent().bringToFront();
        }
    }
    
    /**
     * Converts empty string to null.
     *
     * @param value String value.
     * @return Original value or null if empty string.
     */
    protected String nullify(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
    
    /**
     * Trims whitespace from a string and nullifies it.
     *
     * @param value String value.
     * @return Trimmed and nullified value.
     */
    protected String trimify(String value) {
        return value == null ? null : nullify(value.trim());
    }
    
    /**
     * Returns the input value if it is not null, or the default value otherwise.
     *
     * @param <T> The argument class.
     * @param value The input value.
     * @param deflt The default value.
     * @return Original value or the default if the input value was null.
     */
    protected <T> T defaultify(T value, T deflt) {
        return value == null ? deflt : value;
    }
    
    /**
     * Returns true if two objects are equal, allowing for null values.
     *
     * @param obj1 First object to compare.
     * @param obj2 Second object to compare.
     * @return True if the objects are equal.
     */
    protected boolean areEqual(Object obj1, Object obj2) {
        return ObjectUtils.equals(obj1, obj2);
    }
    
    /**
     * Returns the data object associated with the component.
     *
     * @return The data object; may be null.
     */
    @PropertyGetter("data")
    public Object getData() {
        return data;
    }
    
    /**
     * Returns the data object associated with the component if it is of the specified type;
     * otherwise returns null.
     *
     * @param <T> Expected return type.
     * @param type The expected type.
     * @return The data object if it is of the expected type; null otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> type) {
        return type.isInstance(data) ? (T) data : null;
    }
    
    /**
     * Sets the data object to be associated with the component.
     *
     * @param data The data object.
     */
    @PropertySetter("data")
    public void setData(Object data) {
        this.data = data;
    }
    
    @PropertyGetter("#text")
    protected String getContent() {
        return content;
    }
    
    @PropertySetter("#text")
    protected void setContent(String content) {
        setContent(content, true);
    }
    
    protected void setContent(String content, boolean syncToClient) {
        if (!areEqual(content = nullify(content), this.content)) {
            this.content = content;
            
            if (syncToClient) {
                sync("content", content);
            }
        }
    }
    
    /**
     * Handle state change events from the client. These events cause the field whose name matches
     * the state name to be directly updated with the new value. This is the principal mechanism by
     * which the client communicates simple state changes to the server. It should NOT be used on
     * generically typed fields.
     *
     * @param event The state change event.
     */
    @EventHandler(value = "statechange", syncToClient = false)
    private void _onStateChange(StatechangeEvent event) {
        String state = event.getState();
        
        try {
            Field field = FieldUtils.getField(this.getClass(), state, true);
            field.set(this, ConvertUtil.convert(event.getValue(), field.getType(), this));
        } catch (Exception e) {
            throw new ComponentException(e, "Error updating state: " + state);
        }
    }
    
    /**
     * Returns basic information about this component for display purposes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        //@formatter:off
        sb.append(getClass().getName())
          .append(", ")
          .append("id: ")
          .append(id)
          .append(", ")
          .append("name: ")
          .append(name);
        //@formatter:on
        
        return sb.toString();
    }
}
