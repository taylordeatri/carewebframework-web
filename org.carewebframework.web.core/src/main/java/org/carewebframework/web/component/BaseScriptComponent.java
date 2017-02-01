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

import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;

public abstract class BaseScriptComponent extends Content {
    
    private static final String EVENT_DEFERRED = "deferredExecution";
    
    public static final String EVENT_EXECUTED = "scriptExecution";
    
    private boolean deferred;
    
    @Override
    protected void afterAttached() {
        super.afterAttached();
        
        if (deferred) {
            EventUtil.post(EVENT_DEFERRED, this, null);
        } else {
            doExecute();
        }
    }
    
    protected abstract Object execute();
    
    @PropertyGetter("deferred")
    public boolean isDeferred() {
        return deferred;
    }
    
    @PropertySetter("deferred")
    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }
    
    @EventHandler(value = EVENT_DEFERRED, syncToClient = false)
    private void onDeferredExecution() {
        doExecute();
    }
    
    private void doExecute() {
        Object result = execute();
        
        if (hasEventListener(EVENT_EXECUTED)) {
            fireEvent(new Event(EVENT_EXECUTED, this, result));
        }
    }
}
