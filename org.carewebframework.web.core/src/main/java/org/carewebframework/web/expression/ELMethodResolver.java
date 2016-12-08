package org.carewebframework.web.expression;

import java.util.List;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.taglib.TagLibrary;
import org.carewebframework.web.taglib.TagLibrary.Function;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

public class ELMethodResolver extends ReflectiveMethodResolver {
    
    @Override
    public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
                                  List<TypeDescriptor> argumentTypes) throws AccessException {
        if (targetObject instanceof TagLibrary) {
            TagLibrary lib = (TagLibrary) targetObject;
            Function function = lib.getFunction(name);
            
            if (function != null) {
                try {
                    targetObject = Class.forName(function.getClassName());
                } catch (ClassNotFoundException e) {
                    throw MiscUtil.toUnchecked(e);
                }
                name = function.getMethodName();
            }
        }
        
        return super.resolve(context, targetObject, name, argumentTypes);
    }
}
