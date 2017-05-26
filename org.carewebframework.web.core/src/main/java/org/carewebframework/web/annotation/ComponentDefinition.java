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
package org.carewebframework.web.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;

import org.carewebframework.web.ancillary.ComponentException;
import org.carewebframework.web.ancillary.ConvertUtil;
import org.carewebframework.web.annotation.Component.AttributeProcessor;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseComponent.ComponentFactory;

/**
 * Stores metadata about a component, as derived from component annotations.
 */
public class ComponentDefinition {

    /**
     * Represents the cardinality of a child tag.
     */
    public static class Cardinality {

        private final int minimum;

        private final int maximum;

        Cardinality(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public int getMinimum() {
            return minimum;
        }

        public int getMaximum() {
            return maximum;
        }

        public boolean hasMinimum() {
            return minimum > 0;
        }

        public boolean hasMaximum() {
            return maximum != Integer.MAX_VALUE;
        }

        public boolean isValid(int count) {
            return count >= minimum && count <= maximum;
        }
    }

    /**
     * Stores a method invocation to be executed at a later time.
     */
    public static class DeferredSetter {

        private final Object instance;

        private final Method method;

        private final Object value;

        DeferredSetter(Object instance, Method method, Object value) {
            this.instance = instance;
            this.method = method;
            this.value = value;
        }

        public void execute() {
            ConvertUtil.invokeSetter(instance, method, value);
        }
    }

    private final ContentHandling contentHandling;

    private final String tag;

    private final Class<? extends BaseComponent> componentClass;

    private final String widgetModule;

    private final String widgetClass;

    private final Set<String> parentTags = new HashSet<>();

    private final Map<String, Cardinality> childTags = new HashMap<>();

    private final Map<String, Method> getters = new HashMap<>();

    private final Map<String, Method> setters = new HashMap<>();

    private final Map<String, Method> processors = new HashMap<>();

    private final Set<String> deferred = new HashSet<>();

    /**
     * Creates a component definition derived from annotation information within the specified
     * class.
     *
     * @param componentClass A component class.
     */
    public ComponentDefinition(Class<? extends BaseComponent> componentClass) {
        Component annot = componentClass.getAnnotation(Component.class);
        this.componentClass = componentClass;
        this.widgetModule = annot.widgetModule();
        this.widgetClass = annot.widgetClass();
        this.tag = annot.value();
        this.contentHandling = annot.content();

        for (String tag : annot.parentTag()) {
            addParentTag(tag);
        }

        for (ChildTag tag : annot.childTag()) {
            addChildTag(tag);
        }

    }

    /**
     * Returns The value of the named property.
     *
     * @param instance Instance to retrieve property from.
     * @param name Name of property.
     * @return The property value.
     * @throws Exception Unspecified exception
     */
    public Object getProperty(BaseComponent instance, String name) throws Exception {
        Method method = getters.get(name);

        if (method == null) {
            throw new RuntimeException("Property cannot be read: " + name);
        }

        return method.invoke(instance);
    }

    /**
     * Sets a property value or defers that operation if the property is marked as such.
     *
     * @param instance Instance containing the property or attribute map.
     * @param name Name of property or attribute. If prefixed with "@", is interpreted as an
     *            attribute name; otherwise as a property name.
     * @param value The value to set.
     * @return Null if the operation occurred, or a DeferredSetter object if deferred.
     */
    public DeferredSetter setProperty(BaseComponent instance, String name, Object value) {
        if (name.startsWith("@")) {
            instance.setAttribute(name.substring(1), value);
            return null;
        }

        Method method = setters.get(name);

        if (method == null) {
            if (processors.containsKey(name)) {
                return null;
            }

            String message = getters.containsKey(name) ? "Property is read-only" : "Property is not recognized";
            throw new ComponentException(message + ": " + name);
        }

        if (deferred.contains(name)) {
            return new DeferredSetter(instance, method, value);
        }

        ConvertUtil.invokeSetter(instance, method, value);
        return null;
    }

    /**
     * Returns the XML tag for this component type.
     *
     * @return An XML tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Returns the implementation class for this component type.
     *
     * @return Implementation class.
     */
    public Class<? extends BaseComponent> getComponentClass() {
        return componentClass;
    }

    /**
     * Returns the javascript module containing the widget class.
     *
     * @return Widget module.
     */
    public String getWidgetModule() {
        return widgetModule;
    }

    /**
     * Returns the javascript class for the widget.
     *
     * @return Widget class.
     */
    public String getWidgetClass() {
        return widgetClass;
    }

    /**
     * Returns the cardinality of a child tag.
     *
     * @param childTag A child tag.
     * @return Cardinality of the child tag, or null if the tag is not a valid child.
     */
    public Cardinality getCardinality(String childTag) {
        Cardinality cardinality = childTags.get(childTag);
        return cardinality == null ? childTags.get("*") : cardinality;
    }

    /**
     * Returns an immutable map of all child tags.
     *
     * @return Map of child tags.
     */
    public Map<String, Cardinality> getChildTags() {
        return Collections.unmodifiableMap(childTags);
    }

    public boolean childrenAllowed() {
        return childTags.size() > 0;
    }

    public void validateChild(ComponentDefinition childDefinition, IntSupplier childCount) {
        if (!childrenAllowed()) {
            throw new ComponentException(componentClass, "Children are not allowed");
        }

        childDefinition.validateParent(this);
        Cardinality cardinality = getCardinality(childDefinition.tag);

        if (cardinality == null) {
            throw new ComponentException(componentClass, "%s is not a valid child", childDefinition.componentClass);
        }

        if (cardinality.hasMaximum() && childCount.getAsInt() >= cardinality.getMaximum()) {
            throw new ComponentException(componentClass, "A maximum of %d children of type %s are allowed",
                    cardinality.getMaximum(), childDefinition.componentClass);
        }

    }

    public void validateParent(ComponentDefinition parentDefinition) {
        if (!isParentTag(parentDefinition.tag)) {
            throw new ComponentException(componentClass, "%s is not a valid parent", parentDefinition.componentClass);
        }
    }

    /**
     * Returns true if the tag is a valid parent tag.
     *
     * @param tag Tag to be tested.
     * @return True if the tag is a valid parent tag.
     */
    public boolean isParentTag(String tag) {
        return parentTags.contains(tag) || parentTags.contains("*");
    }

    /**
     * Returns an immutable set of parent tags.
     *
     * @return Set of valid parent tags.
     */
    public Set<String> getParentTags() {
        return Collections.unmodifiableSet(parentTags);
    }

    /**
     * Returns how to handle content for this component type.
     *
     * @return How to handle content.
     */
    public ContentHandling contentHandling() {
        return contentHandling;
    }

    // Processors for component annotations

    /**
     * Registers a parent tag.
     *
     * @param tag The tag, or "*" to indicate any parent tag is valid.
     */
    private void addParentTag(String tag) {
        parentTags.add(tag);
    }

    /**
     * Registers a child tag.
     *
     * @param tag A child tag.
     */
    private void addChildTag(ChildTag tag) {
        childTags.put(tag.value(), new Cardinality(tag.minimum(), tag.maximum()));
    }

    /**
     * Returns true if the method is static.
     *
     * @param method Method to test.
     * @return True if the method is static.
     */
    private boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Registers a property getter.
     *
     * @param getter {@literal @PropertyGetter} annotation
     * @param method The getter method.
     */
    /*package*/ void _addGetter(PropertyGetter getter, Method method) {
        String name = getter.value();

        if (!getters.containsKey(name)) {
            if (isStatic(method) || method.getReturnType() == Void.TYPE || method.getParameterTypes().length > 0) {
                throw new IllegalArgumentException("Bad signature for getter method: " + method.getName());
            }

            getters.put(name, getter.hide() ? null : method);
        }
    }

    /**
     * Returns an immutable map of getter methods.
     *
     * @return Map of getter methods.
     */
    public Map<String, Method> getGetters() {
        return Collections.unmodifiableMap(getters);
    }

    /**
     * Registers a property setter.
     *
     * @param setter {@literal @PropertySetter} annotation
     * @param method The setter method.
     */
    /*package*/ void _addSetter(PropertySetter setter, Method method) {
        String name = setter.value();

        if (!setters.containsKey(name)) {
            if (isStatic(method) || method.getParameterTypes().length != 1) {
                throw new IllegalArgumentException("Bad signature for setter method: " + method.getName());
            }

            setters.put(name, setter.hide() ? null : method);

            if (setter.defer()) {
                deferred.add(name);
            }
        }
    }

    /**
     * Returns an immutable map of setter methods.
     *
     * @return Map of setter methods.
     */
    public Map<String, Method> getSetters() {
        return Collections.unmodifiableMap(setters);
    }

    /**
     * Registers an attribute processor.
     *
     * @param processor {@literal @AttributeProcessor} annotation
     * @param method The static processor method.
     */
    /*package*/ void _addProcessor(AttributeProcessor processor, Method method) {
        String name = processor.value();

        if (!processors.containsKey(name)) {
            if (!isStatic(method) || method.getParameterTypes().length != 2
                    || method.getParameterTypes()[1] != ComponentFactory.class) {
                throw new IllegalArgumentException("Bad signature for attribute processor method: " + method.getName());
            }

            processors.put(name, method);
        }
    }

    /**
     * Returns an immutable map of attribute processors.
     *
     * @return Map of attribute processors.
     */
    public Map<String, Method> getProcessors() {
        return Collections.unmodifiableMap(processors);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ComponentDefinition && ((ComponentDefinition) object).componentClass == componentClass;
    }
}
