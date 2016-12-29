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
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.MessagePane;
import org.carewebframework.web.component.MessageWindow;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.component.Window.Mode;
import org.carewebframework.web.event.ResizeEvent;

public class WindowsController extends BaseController {
    
    @WiredComponent("^.messagewindow")
    private MessageWindow messagewindow;
    
    @WiredComponent("window1.window_div")
    private Div windowdiv1;
    
    @WiredComponent("window2.window_div")
    private Div windowdiv2;
    
    @WiredComponent("window3")
    private Window window3;
    
    @WiredComponent("window3.btnModal")
    private Button btnModal;
    
    private boolean isModal;
    
    private int messageClass = -1;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        log(windowdiv1 == null, "Component window1.window_div was NOT autowired.",
            "Component window1.window_div was autowired.");
        log(windowdiv2 == null, "Component window2.window_div was NOT autowired.",
            "Component window2.window_div was autowired.");
        log(windowdiv1 == windowdiv2, "window1.window_div and window2.window_div should not be the same.", null);
    }
    
    @EventHandler(value = "click", target = "window3.btnModal")
    private void btnModalHandler() {
        isModal = !isModal;
        btnModal.setLabel("Make " + (isModal ? "inline" : "modal"));
        window3.setMode(isModal ? Mode.MODAL : Mode.INLINE);
    }
    
    @EventHandler(value = "click", target = "window3.btnAlert")
    private void btnAlertHandler() {
        ClientUtil.invoke("cwf.alert", "This is a test alert", "TEST!", "danger");
    }
    
    private static final String[] MSG_CLASS = { "success", "warning", "danger", "info" };
    
    @EventHandler(value = "click", target = "window3.btnMessage")
    private void btnMessageHandler() {
        MessagePane pane = new MessagePane("Message Title", "category", 8000, false);
        messageClass++;
        messageClass = messageClass >= MSG_CLASS.length ? 0 : messageClass;
        pane.addClass("flavor: alert-" + MSG_CLASS[messageClass]);
        pane.addChild(new Label("This is a test " + MSG_CLASS[messageClass] + " message"));
        messagewindow.addChild(pane);
    }
    
    @EventHandler(value = "resize", target = "window3")
    private void resizeHandler(ResizeEvent event) {
        log("Resize event!!!");
    }
    
}
