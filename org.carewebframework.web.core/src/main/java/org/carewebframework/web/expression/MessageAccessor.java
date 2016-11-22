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

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * Allows SPEL to evaluate message references that have periods in the key name. It does this by
 * asserting itself as a property accessor for the MessageSource class. On the initial call, the
 * accessor returns a MessageContext object initialized with the first property name. Subsequent
 * calls return the same MessageContext object, with the property name concatenated to the previous
 * ones. Once all property names have been accumulated, a custom converter, the
 * MessageContextConverter, converts it to a string value by retrieving the message from the message
 * source using the accumulated property names.
 */
public class MessageAccessor implements PropertyAccessor {
    
    private static final Class<?>[] TARGET_CLASSES = { MessageSource.class, MessageContext.class };
    
    /**
     * Custom converter that converts a MessageContext to a String.
     */
    public static class MessageContextConverter implements Converter<MessageContext, String> {
        
        @Override
        public String convert(MessageContext source) {
            return source.toString();
        }
        
    }
    
    /**
     * Accumulates property names.
     */
    private static class MessageContext {
        
        private String name;
        
        private final MessageSource messageSource;
        
        MessageContext(MessageSource messageSource, String name) {
            this.messageSource = messageSource;
            this.name = name;
        }
        
        MessageContext addName(String name) {
            this.name += "." + name;
            return this;
        }
        
        @Override
        public String toString() {
            String message = getMessage(name);
            int i = 0;
            int k = 0;
            
            while ((i = message.indexOf("${")) > -1 && k++ < 20) {
                int j = message.indexOf("}", i);
                String repl = getMessage(message.substring(i + 2, j));
                message = message.substring(0, i) + repl + message.substring(j + 1);
            }
            
            return message;
        }
        
        private String getMessage(String name) {
            try {
                return messageSource.getMessage(name, null, LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException e) {
                return "";
            }
        }
        
    }
    
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return TARGET_CLASSES;
    }
    
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return true;
    }
    
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        MessageContext result = null;
        
        if (target instanceof MessageSource) {
            result = new MessageContext((MessageSource) target, name);
        } else if (target instanceof MessageContext) {
            result = ((MessageContext) target).addName(name);
        }
        
        return result == null ? null : new TypedValue(result);
    }
    
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }
    
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        throw new AccessException("Message source is read-only.");
    }
    
}
