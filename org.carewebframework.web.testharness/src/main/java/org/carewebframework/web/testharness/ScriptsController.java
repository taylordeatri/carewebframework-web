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
import org.carewebframework.web.component.ServerScript;
import org.carewebframework.web.event.Event;

public class ScriptsController extends BaseController {
    
    @WiredComponent
    private ServerScript groovyscript;
    
    @WiredComponent
    private ServerScript jrubyscript;
    
    @EventHandler(value = "scriptExecution", target = "@groovyscript")
    private void groovyExecutionHandler(Event event) {
        log("Groovy script was executed: " + event.getData());
    }
    
    @EventHandler(value = "scriptExecution", target = "@jrubyscript")
    private void jrubyExecutionHandler(Event event) {
        log("JRuby script was executed: " + event.getData());
    }
    
}
