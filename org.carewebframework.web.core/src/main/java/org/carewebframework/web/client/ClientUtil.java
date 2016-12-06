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
package org.carewebframework.web.client;

import java.util.List;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;

public class ClientUtil {
    
    protected static boolean debugEnabled;
    
    /**
     * Invoke a function on the client.
     * 
     * @param function Name of the function to invoke.
     * @param args Arguments to pass to the function.
     */
    public static void invoke(String function, Object... args) {
        ClientInvocation invocation = new ClientInvocation(null, function, args);
        WebSocketHandler.send(invocation);
    }
    
    public static void redirect(String target, String window) {
        invoke("window.open", target, window);
    }
    
    public static void eval(String expression) {
        invoke("cwf.eval", expression);
    }
    
    public static void submit(BaseComponent form) {
        invoke("cwf.submit", form);
    }
    
    public static void busy(BaseUIComponent target, String message) {
        if (message == null || message.isEmpty()) {
            target.removeMask();
        } else {
            target.addMask(message);
        }
    }
    
    public static void canClose(boolean value) {
        invoke("cwf.canClose", value);
    }
    
    public static void saveToFile(String content, String mimeType, String fileName) {
        invoke("cwf.saveToFile", content, mimeType, fileName);
    }
    
    public static boolean debugEnabled() {
        return debugEnabled;
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors List of selectors whose content shall be printed.
     * @param styleSheets List of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(List<String> selectors, List<String> styleSheets, boolean preview) {
        invoke("cwf_print", null, selectors, styleSheets, preview);
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors Comma-delimited list of selectors whose content shall be printed.
     * @param styleSheets Comma-delimited list of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(String selectors, String styleSheets, boolean preview) {
        invoke("cwf_print", null, selectors, styleSheets, preview);
    }
    
    private ClientUtil() {
    }
}
