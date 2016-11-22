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
package org.carewebframework.web.event;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.springframework.util.Assert;

public class ForwardListener implements IEventListener {
    
    private final String forwardType;
    
    private final BaseComponent target;
    
    public ForwardListener(String forwardType, BaseComponent target) {
        Assert.notNull(this.forwardType = forwardType);
        Assert.notNull(this.target = target);
    }
    
    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(forwardType)) {
            if (event.getTarget() != this.target) {
                EventUtil.send(event, target);
            }
            
            return;
        }
        
        try {
            Event newEvent = new ForwardedEvent(forwardType, event);
            EventUtil.send(newEvent, target);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof ForwardListener) {
            ForwardListener fl = (ForwardListener) object;
            return fl.target == target && fl.forwardType.equals(forwardType);
        }
        
        return false;
    }
}
