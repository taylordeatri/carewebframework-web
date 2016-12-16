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
package org.carewebframework.web.testharness;

import org.carewebframework.web.ancillary.Badge;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Tab;

public class TabsController extends BaseController {
    
    @WiredComponent
    private Tab tabNoClose;
    
    @WiredComponent
    private Tab tabWithBadge;
    
    private Badge badge;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        tabNoClose.setOnCanClose(() -> canCloseTab());
        badge = new Badge(tabWithBadge);
    }
    
    /**
     * Prevent closure of a tab.
     * 
     * @return Always false.
     */
    public boolean canCloseTab() {
        log("Preventing tab from closing...");
        return false;
    }
    
    @EventHandler(value = "click", target = "btnIncBadge")
    private void incBadgeHandler() {
        badge.incCount(1);
    }
    
    @EventHandler(value = "click", target = "btnDecBadge")
    private void decBadgeHandler() {
        badge.incCount(-1);
    }
    
    @EventHandler(value = "click", target = "btnClrBadge")
    private void clickHandler() {
        badge.setCount(0);
    }
    
}
