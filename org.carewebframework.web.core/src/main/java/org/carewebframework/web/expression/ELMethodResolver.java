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
package org.carewebframework.web.expression;

import java.util.List;

import org.carewebframework.web.taglib.TagLibrary;
import org.carewebframework.web.taglib.TagLibraryFunction;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

/**
 * A subclass of the ReflectiveMethodResolver that can resolve methods declared in a tag library
 * definition file.
 */
public class ELMethodResolver extends ReflectiveMethodResolver {
    
    /**
     * If the target object is a tag library, replace the name and targetObject parameters with the
     * method name and implementing class, respectively, of the named tag library function before
     * calling the super method.
     */
    @Override
    public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
                                  List<TypeDescriptor> argumentTypes) throws AccessException {
        if (targetObject instanceof TagLibrary) {
            TagLibrary lib = (TagLibrary) targetObject;
            TagLibraryFunction function = lib.getFunction(name);
            
            if (function != null) {
                try {
                    targetObject = Class.forName(function.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new AccessException("Error evaluating " + function, e);
                }
                name = function.getMethodName();
            } else {
                throw new AccessException("Unknown function \"" + name + "\" in tag library " + lib.getUri());
            }
        }
        
        return super.resolve(context, targetObject, name, argumentTypes);
    }
}
