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

import java.util.Date;

import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Progressbar;
import org.carewebframework.web.component.Timer;
import org.carewebframework.web.event.TimerEvent;

public class TimerController extends BaseController {
    
    @WiredComponent
    private Timer timer;
    
    @WiredComponent
    private Button btnToggleTimer;
    
    @WiredComponent
    private Progressbar pbTimer;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        setTimerButtonState(false);
    }
    
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
    
}
