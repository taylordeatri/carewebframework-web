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

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * A timer component. Once started, fires a timer event at the specified interval and for the
 * specified number of repetitions.
 */
@Component(value = "timer", widgetClass = "Timer", parentTag = "*")
public class Timer extends BaseComponent {
    
    private long interval;
    
    private int repeat = -1;
    
    private boolean running;
    
    /**
     * Returns the interval, in milliseconds, between timer events.
     * 
     * @return The timer interval in milliseconds.
     */
    @PropertyGetter("interval")
    public long getInterval() {
        return interval;
    }
    
    /**
     * Sets the interval, in milliseconds, between timer events.
     * 
     * @param interval The timer interval in milliseconds. Any value less than or equal to zero will
     *            disable the timer.
     */
    @PropertySetter("interval")
    public void setInterval(long interval) {
        if (interval != this.interval) {
            sync("interval", this.interval = interval);
        }
    }
    
    /**
     * Returns the repetition count. This is the number of times the timer will fire additional
     * events after the initial event. A value of zero will cause the timer to fire only once (no
     * repetitions). A negative value will cause the timer to fire indefinitely.
     * 
     * @return The repetition count.
     */
    @PropertyGetter("repeat")
    public int getRepeat() {
        return repeat;
    }
    
    /**
     * Sets the repetition count. This is the number of times the timer will fire additional events
     * after the initial event. A value of zero will cause the timer to fire only once (no
     * repetitions). A negative value will cause the timer to fire indefinitely.
     * 
     * @param repeat The repetition count.
     */
    @PropertySetter("repeat")
    public void setRepeat(int repeat) {
        if (repeat != this.repeat) {
            sync("repeat", this.repeat = repeat);
        }
    }
    
    /**
     * Returns true if the timer is running.
     * 
     * @return True if the timer is running.
     */
    @PropertyGetter("running")
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Sets the run state of the timer.
     * 
     * @param running The run state of the timer. Set to true to start the timer, false to stop it.
     */
    @PropertySetter("running")
    public void setRunning(boolean running) {
        if (running != this.running) {
            sync("running", this.running = running);
        }
    }
    
    /**
     * A convenience method to start the timer.
     */
    public void start() {
        setRunning(true);
    }
    
    /**
     * A convenience method to stop the timer.
     */
    public void stop() {
        setRunning(false);
    }
    
    /**
     * A convenience method for restarting the timer.
     */
    public void restart() {
        stop();
        start();
    }
}
