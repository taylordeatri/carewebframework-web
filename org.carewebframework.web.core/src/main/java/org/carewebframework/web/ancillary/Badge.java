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
package org.carewebframework.web.ancillary;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;

/**
 * Helper class for displaying a badge with numeric counter. Each time the counter changes, the
 * owning component and its ancestors are notified of the delta, allowing any components that are
 * capable of displaying a badge to update their cumulative counts.
 */
public class Badge {
    
    private final BaseComponent owner;
    
    private int count;
    
    /**
     * Creates a badge with an initial count of 0.
     * 
     * @param owner The component to associate with this badge.
     */
    public Badge(BaseComponent owner) {
        this(owner, 0);
    }
    
    /**
     * Creates a badge with a specified initial count.
     * 
     * @param owner The component to associate with this badge.
     * @param count The initial count for the badge.
     */
    public Badge(BaseComponent owner, int count) {
        this.owner = owner;
        updateCount(count);
    }
    
    /**
     * Returns the component associated with this badge.
     * 
     * @return The owning component.
     */
    public BaseComponent getOwner() {
        return owner;
    }
    
    /**
     * Returns the current count.
     * 
     * @return The current count.
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Sets the count to the specified value.
     * 
     * @param count The new count.
     */
    public void setCount(int count) {
        updateCount(count);
    }
    
    /**
     * Increments the count by the specified value.
     * 
     * @param increment The increment to apply to the current count.
     */
    public void incCount(int increment) {
        updateCount(count + increment);
    }
    
    /**
     * Updates the count to the specified value.
     * 
     * @param newCount The new value for the count.
     */
    private void updateCount(int newCount) {
        if (newCount != count) {
            int delta = newCount - count;
            count = newCount;
            Event event = new Event("badge", owner, delta);
            owner.notifyAncestors(event, true);
        }
    }
    
}
