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

import java.lang.annotation.Annotation;

import org.carewebframework.common.MiscUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Abstract base class for scanning class-level annotations.
 * 
 * @param <T> Type of target class.
 * @param <A> Type of annotation class.
 */
public abstract class AbstractClassScanner<T, A extends Annotation> {
    
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    
    private final Class<T> targetClass;
    
    private final Class<? extends Annotation> annotationClass;
    
    public AbstractClassScanner(Class<T> targetClass, Class<? extends Annotation> annotationClass) {
        this.targetClass = targetClass;
        this.annotationClass = annotationClass;
    }
    
    /**
     * Scan all classes belonging to the specified package.
     * 
     * @param pkg A package.
     */
    public void scan(Package pkg) {
        scan(pkg.getName());
    }
    
    /**
     * Scan all classes belonging to the specified package.
     * 
     * @param pkgName A package name.
     */
    public void scan(String pkgName) {
        try {
            for (Resource resource : resolver.getResources("classpath:" + pkgName.replace(".", "/") + "/*.class")) {
                String className = resource.getFilename().replace(".class", "");
                Class<?> clazz = Class.forName(pkgName + "." + className);
                scan(clazz);
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Creates and registers a component definition for a class by scanning the class and its
     * superclasses for method annotations.
     * 
     * @param clazz Class to scan.
     */
    @SuppressWarnings("unchecked")
    public void scan(Class<?> clazz) {
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            scan(innerClass);
        }
        
        if (!clazz.isAnnotationPresent(annotationClass)) {
            return;
        }
        
        if (!targetClass.isAssignableFrom(clazz)) {
            throw new RuntimeException(
                    annotationClass.getName() + " annotation only valid on " + targetClass.getName() + " subclass.");
        }
        
        scanClass((Class<T>) clazz);
    }
    
    protected abstract void scanClass(Class<T> clazz);
    
}
