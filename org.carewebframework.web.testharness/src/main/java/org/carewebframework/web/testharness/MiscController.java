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

import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.OnFailure;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Popup;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.page.PageUtil;

public class MiscController extends BaseController {
    
    @WiredComponent(onFailure = OnFailure.IGNORE)
    private Div nomatch;
    
    @WiredComponent
    private BaseComponent dynamicContent;
    
    @WiredComponent
    private Popup contextMenu;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        log(nomatch == null, "Component 'nomatch' was correctly not wired.", "Component 'nomatch' as erroneously wired.");
        PageUtil.createPageFromContent("<button label='Dynamic Content' class='flavor:btn-danger'/>", dynamicContent);
    }
    
    /**
     * Controls whether or not application closure is challenged.
     * 
     * @param event The checkbox change event.
     */
    @EventHandler(value = "change", target = "chkPreventClosure")
    public void chkPreventClosureHandler(ChangeEvent event) {
        ClientUtil.canClose(!((Checkbox) event.getTarget()).isChecked());
    }
    
    @EventHandler(value = "click", target = "btnSaveAsFile")
    public void btnSaveAsFileHandler() {
        ClientUtil.saveToFile("This is test content", "text/plain", "testFile.txt");
    }
    
    @WiredComponent
    private Div divMaskTest;
    
    private boolean masked;
    
    @EventHandler(value = "click", target = "btnMaskTest")
    private void btnMaskTestClickHandler() {
        if (masked = !masked) {
            divMaskTest.addMask("Mask Test", contextMenu);
        } else {
            divMaskTest.removeMask();
        }
    }
    
    @WiredComponent
    private Button btnToggleBalloon;
    
    @EventHandler(value = "click", target = "@btnToggleBalloon")
    private void btnToggleBalloonClickHandler() {
        if (btnToggleBalloon.getBalloon() == null) {
            btnToggleBalloon.setBalloon("Balloon Text");
        } else {
            btnToggleBalloon.setBalloon(null);
        }
    }
}
