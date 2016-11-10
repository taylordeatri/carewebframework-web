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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.OnFailure;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Memobox;
import org.carewebframework.web.component.Menu;
import org.carewebframework.web.component.Menuitem;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.component.Progressbar;
import org.carewebframework.web.component.Tab;
import org.carewebframework.web.component.Tabview;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Timer;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.component.Window.Mode;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DropEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.ResizeEvent;
import org.carewebframework.web.event.TimerEvent;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.page.PageUtil;

public class TestController implements IAutoWired {
    
    private interface IInitializer {
        
        void init();
    };
    
    /*********************** Initialization ***********************/
    
    List<IInitializer> initializers = new ArrayList<>();
    
    private Page page;
    
    @WiredComponent(onFailure = OnFailure.IGNORE)
    private Div nomatch;
    
    @WiredComponent("window1.window_div")
    private Div windowdiv1;
    
    @WiredComponent("window2.window_div")
    private Div windowdiv2;
    
    @WiredComponent("window3")
    private Window window3;
    
    @WiredComponent
    private Tabview tabview;
    
    @WiredComponent
    private Tab tabNoClose;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        page = root.getPage();
        log(windowdiv1 == null, "Component window1.window_div was NOT autowired.",
            "Component window1.window_div was autowired.");
        log(windowdiv2 == null, "Component window2.window_div was NOT autowired.",
            "Component window2.window_div was autowired.");
        log(windowdiv1 == windowdiv2, "window1.window_div and window2.window_div should not be the same.", null);
        log(nomatch == null, "Component 'nomatch' was correctly not wired.", "Component 'nomatch' as erroneously wired.");
        
        int tabIndex = NumberUtils.toInt(page.getQueryParam("tab"));
        tabview.setSelectedTab((Tab) tabview.getChildAt(tabIndex));
        
        for (IInitializer initializer : initializers) {
            initializer.init();
        }
    }
    
    /*********************** Script Tab ***********************/
    
    /**
     * Handler for custom log events sent from client.
     * 
     * @param event The log event.
     */
    @EventHandler(value = "log", target = "@page")
    private void onInfo(Event event) {
        log((String) event.getData());
    }
    
    /*********************** Timer Tab ***********************/
    
    {
        initializers.add(new IInitializer() {
            
            @Override
            public void init() {
                setTimerButtonState(false);
            }
            
        });
    }
    
    @WiredComponent
    private Timer timer;
    
    @WiredComponent
    private Button btnToggleTimer;
    
    @WiredComponent
    private Progressbar pbTimer;
    
    /**
     * Toggle the timer run state.
     */
    @EventHandler(value = "click", target = "@btnToggleTimer")
    private void btnToggleTimerHandler() {
        if (timer.isRunning()) {
            timer.stop();
            log("Timer was stopped.");
        } else {
            pbTimer.setMaxValue(timer.getRepeat() + 1);
            setTimerProgressbarState(0);
            timer.start();
            log("Timer was started.");
        }
        
        setTimerButtonState(timer.isRunning());
    }
    
    /**
     * Handle the timer event.
     * 
     * @param event The timer event.
     */
    @EventHandler(value = "timer", target = "timer")
    public void onTimer(TimerEvent event) {
        int count = event.getCount();
        log("Timer event: " + event.getTarget().getName() + " # " + count + " @ " + new Date().toString());
        setTimerButtonState(event.isRunning());
        setTimerProgressbarState(count);
        
        if (!event.isRunning()) {
            log("Timer finished.");
        }
    }
    
    /**
     * Update the progress bar state.
     * 
     * @param count The timer count.
     */
    private void setTimerProgressbarState(int count) {
        pbTimer.setValue(count);
        pbTimer.setLabel(count + " of " + pbTimer.getMaxValue());
    }
    
    /**
     * Updates the state of the timer button.
     * 
     * @param running The desired state.
     */
    private void setTimerButtonState(boolean running) {
        if (!running) {
            btnToggleTimer.setLabel("Start");
            btnToggleTimer.addClass("flavor:btn-success");
        } else {
            btnToggleTimer.setLabel("Stop");
            btnToggleTimer.addClass("flavor:btn-danger");
        }
    }
    
    /*********************** Button Tab ***********************/
    
    @WiredComponent
    private Button btnWithEvent;
    
    /**
     * Sample button event handler.
     */
    @EventHandler(value = "click", target = "@btnWithEvent")
    public void btnEventHandler() {
        log("Button event handler was invoked");
    }
    
    /*********************** Input Boxes Tab ***********************/
    
    {
        initializers.add(new IInitializer() {
            
            @Override
            public void init() {
                ListModel<String> model = new ListModel<>();
                
                for (int i = 1; i < 6; i++) {
                    model.add("Rendered item #" + i);
                }
                
                lboxRender.getModelAndView(String.class).setModel(model);
                cboxRender.getModelAndView(String.class).setModel(model);
                
                lboxRender.getModelAndView(String.class).setRenderer(new IComponentRenderer<Listitem, String>() {
                    
                    @Override
                    public Listitem render(String model) {
                        return new Listitem(model);
                    }
                    
                });
                
                cboxRender.getModelAndView(String.class).setRenderer(new IComponentRenderer<Comboitem, String>() {
                    
                    @Override
                    public Comboitem render(String model) {
                        return new Comboitem(model);
                    }
                    
                });
            }
            
        });
    }
    
    @WiredComponent
    private Textbox txtSelect;
    
    @WiredComponent
    private Textbox txtInput;
    
    @WiredComponent
    private Listbox lboxRender;
    
    @WiredComponent
    private Combobox cboxRender;
    
    @EventHandler(value = "select", target = "tabInputBoxes")
    private void InputBoxTabSelectHandler() {
        txtInput.setValue("Value set programmatically");
        txtSelect.selectRange(2, 5);
        txtSelect.focus();
    }
    
    /*********************** Window Tab ***********************/
    
    @WiredComponent("window3.btnModal")
    private Button btnModal;
    
    private boolean isModal;
    
    @EventHandler(value = "click", target = "window3.btnModal")
    private void btnModalHandler() {
        isModal = !isModal;
        btnModal.setLabel("Make " + (isModal ? "inline" : "modal"));
        window3.setMode(isModal ? Mode.MODAL : Mode.INLINE);
    }
    
    @EventHandler(value = "click", target = "window3.btnAlert")
    private void btnAlertHandler() {
        ClientUtil.invoke("alert", "This is a test alert", "TEST!", "danger");
    }
    
    @EventHandler(value = "resize", target = "window3")
    private void resizeHandler(ResizeEvent event) {
        log("Resize event!!!");
    }
    
    /*********************** Menus Tab ***********************/
    
    @EventHandler(value = "click", target = { "menu1", "menu2", "menu3", "menu5_1", "menu5_2" })
    private void menuClickHandler(ClickEvent event) {
        log(event.getTarget().getName() + " clicked.");
    }
    
    @EventHandler(value = "click", target = "menu2")
    private void menuClickHandler2(ClickEvent event) {
        Menuitem item = (Menuitem) event.getTarget();
        item.setChecked(!item.isChecked());
    }
    
    @WiredComponent
    private Menu mainMenu;
    
    @EventHandler(value = "click", target = "btnToggleMenu")
    private void btnToggleMenuHandler() {
        mainMenu.setOpen(!mainMenu.isOpen());
    }
    
    /*********************** Miscellaneous Tab ***********************/
    
    {
        initializers.add(new IInitializer() {
            
            @Override
            public void init() {
                PageUtil.createPageFromContent("<button label='Dynamic Content' class='flavor:btn-danger'/>",
                    dynamicContent);
            }
            
        });
    }
    
    @WiredComponent
    BaseComponent dynamicContent;
    
    @WiredComponent
    Popupbox popupbox;
    
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
    
    @EventHandler(value = "close", target = "@popupbox")
    public void popupboxCloseHandler() {
        popupbox.setValue("Drop down closed!");
    }
    
    @EventHandler(value = "open", target = "@popupbox")
    public void popupboxOpenHandler() {
        popupbox.setValue("Drop down opened!");
    }
    
    @WiredComponent
    private Div divMaskTest;
    
    private boolean masked;
    
    @EventHandler(value = "click", target = "btnMaskTest")
    private void btnMaskTestClickHandler() {
        if (masked = !masked) {
            divMaskTest.addMask("Mask Test");
        } else {
            divMaskTest.removeMask();
        }
    }
    
    /*********************** Tabview Tab ***********************/
    
    {
        initializers.add(new IInitializer() {
            
            @Override
            public void init() {
                tabNoClose.setOnCanClose(() -> canCloseTab());
            }
            
        });
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
    
    /*********************** Treeview Tab ***********************/
    
    @WiredComponent
    private Treeview treeview;
    
    @WiredComponent
    private Checkbox chkShowRoot;
    
    @WiredComponent
    private Checkbox chkShowLines;
    
    @WiredComponent
    private Checkbox chkShowToggles;
    
    @EventHandler(value = "change", target = "@chkShowRoot")
    public void chkShowRootHandler() {
        treeview.setShowRoot(chkShowRoot.isChecked());
    }
    
    @EventHandler(value = "change", target = "@chkShowLines")
    public void chkShowLinesHandler() {
        treeview.setShowLines(chkShowLines.isChecked());
    }
    
    @EventHandler(value = "change", target = "@chkShowToggles")
    public void chkShowTogglesHandler() {
        treeview.setShowToggles(chkShowToggles.isChecked());
    }
    
    @EventHandler(value = "click", target = "btnCollapseAll")
    public void collapseAllHandler() {
        treeview.collapseAll();
    }
    
    @EventHandler(value = "click", target = "btnExpandAll")
    public void expandAllHandler() {
        treeview.expandAll();
    }
    
    /*********************** Drag and Drop ***********************/
    
    /**
     * Move dragged component to drop target.
     * 
     * @param event The drop event.
     */
    @EventHandler(value = "drop", target = { "dropTargetOriginal", "dropTargetA", "dropTargetD_X", "dropTargetX",
            "dropTargetALL", "dropTargetNONE" })
    public void dropHandler(DropEvent event) {
        event.getTarget().addChild(event.getRelatedTarget());
        log("Component dropped.");
    }
    
    /*********************** Status Log ***********************/
    
    @WiredComponent
    private Memobox statusLog;
    
    private int logCount;
    
    @EventHandler(value = "click", target = "btnClearLog")
    public void btnClearLogHandler() {
        statusLog.clear();
        logCount = 0;
    }
    
    @EventHandler(value = "change", target = "chkScrollLock")
    public void chkScrollLockHandler(ChangeEvent event) {
        statusLog.setAutoScroll(((Checkbox) event.getTarget()).isChecked());
    }
    
    private void log(String message) {
        if (message != null && !message.isEmpty()) {
            String value = statusLog.getValue();
            statusLog.setValue((value == null ? "" : value) + ++logCount + ". " + message + "\n\n");
        }
    }
    
    private void log(boolean condition, String messageIfTrue, String messageIfFalse) {
        log(condition ? messageIfTrue : messageIfFalse);
    }
    
}
