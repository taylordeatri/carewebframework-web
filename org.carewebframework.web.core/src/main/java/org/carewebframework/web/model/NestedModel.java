package org.carewebframework.web.model;

import java.util.Collection;

public abstract class NestedModel<M> extends ListModel<M> implements INestedModel<M> {
    
    public NestedModel() {
    }
    
    public NestedModel(Collection<M> list) {
        super(list);
    }
    
}
