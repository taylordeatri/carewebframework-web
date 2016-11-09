package org.carewebframework.web.model;

import java.lang.reflect.Method;
import java.util.Collection;

import org.carewebframework.common.MiscUtil;

public class NestedModel<M> extends ListModel<M> implements INestedModel<M> {
    
    private final Method childGetter;
    
    public NestedModel(Method childGetter) {
        this.childGetter = childGetter;
    }
    
    public NestedModel(Collection<M> list, Method childGetter) {
        super(list);
        this.childGetter = childGetter;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public IListModel<M> getChildren(M parent) {
        try {
            return (ListModel<M>) childGetter.invoke(parent);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
