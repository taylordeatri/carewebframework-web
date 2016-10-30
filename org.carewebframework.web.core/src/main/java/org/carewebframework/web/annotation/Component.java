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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation to control deserialization of a cwf resource.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    
    public enum ContentHandling {
        ERROR, IGNORE, AS_ATTRIBUTE, AS_CHILD
    };
    
    /**
     * Marks a property getter.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PropertyGetter {
        
        /**
         * @return The property name.
         */
        String value();
        
        /**
         * @return If true, hide the getter method from the deserializer. Use this to hide a getter
         *         annotated in a superclass.
         */
        boolean hide() default false;
    }
    
    /**
     * Marks a property setter
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PropertySetter {
        
        /**
         * @return The property name.
         */
        String value();
        
        /**
         * @return If true, hide the setter method from the deserializer. Use this to hide a setter
         *         annotated in a superclass.
         */
        boolean hide() default false;
        
        /**
         * @return If true, defer invoking the setter until deserialization is complete.
         */
        boolean defer() default false;
        
    }
    
    /**
     * Identifies a special processor for an XML attribute. Processors must be static methods that
     * accept a parameter (the attribute value) and a FactoryContext instance. The processor may
     * modify the FactoryContext to influence component creation.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AttributeProcessor {
        
        /**
         * @return The attribute name.
         */
        String value();
        
    }
    
    /**
     * Represents a child tag and its cardinality.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    public @interface ChildTag {
        
        /**
         * @return The child tag.
         */
        String value();
        
        /**
         * @return Minimum number of occurrences.
         */
        int minimum() default 0;
        
        /**
         * @return Maximum number of occurrences.
         */
        int maximum() default Integer.MAX_VALUE;
        
    }
    
    /**
     * @return The XML tag corresponding to this component.
     */
    String value();
    
    /**
     * @return How to handle text content associated with the tag.
     */
    ContentHandling content() default ContentHandling.ERROR;
    
    /**
     * @return The allowable parent tag(s) for this component.
     */
    String[] parentTag() default {};
    
    /**
     * @return The allowable child tag(s) for this component, including cardinality.
     */
    ChildTag[] childTag() default {};
    
    /**
     * @return The JavaScript package containing the widget.
     */
    String widgetPackage() default "cwf-widget";
    
    /**
     * @return The JavaScript class for the widget.
     */
    String widgetClass();
    
}
