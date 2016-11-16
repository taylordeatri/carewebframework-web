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

import org.carewebframework.web.annotation.EventType;
import org.carewebframework.web.annotation.EventType.EventParameter;
import org.carewebframework.web.component.BaseComponent;

@EventType(TimerEvent.TYPE)
public class TimerEvent extends Event {
    
    public static final String TYPE = "timer";
    
    @EventParameter
    private int count;
    
    @EventParameter
    private boolean running;
    
    public TimerEvent() {
        super(TYPE);
    }
    
    public TimerEvent(BaseComponent target, Object data) {
        super(TYPE, target, data);
    }
    
    public int getCount() {
        return count;
    }
    
    public boolean isRunning() {
        return running;
    }
    
}
