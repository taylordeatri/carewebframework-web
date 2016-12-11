package org.carewebframework.web.expression;

import org.carewebframework.web.component.BaseComponent;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * Property accessor for resolving property references during materialization.
 */
public class ContextAccessor implements PropertyAccessor {
    
    private static final Class<?>[] TARGET_CLASSES = { ELContext.class, BaseComponent.class };
    
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return TARGET_CLASSES;
    }
    
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return !(target instanceof BaseComponent) || ((BaseComponent) target).hasAttribute(name);
    }
    
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        Object result = null;
        
        if (target instanceof ELContext) {
            result = ((ELContext) target).getValue(name);
        } else if (target instanceof BaseComponent) {
            result = ((BaseComponent) target).getAttribute(name);
        }
        
        return new TypedValue(result);
    }
    
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }
    
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        throw new AccessException("Property source is read-only.");
    }
    
}
