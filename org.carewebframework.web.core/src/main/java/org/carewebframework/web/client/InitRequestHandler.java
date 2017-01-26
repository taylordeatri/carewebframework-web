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

import org.carewebframework.web.client.WebSocketHandler.IRequestHandler;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageDefinitionCache;

public class InitRequestHandler implements IRequestHandler {
    
    @Override
    public void handleRequest(ClientRequest request) throws Exception {
        Page page = request.getPage();
        PageDefinition pageDefinition = PageDefinitionCache.getInstance().get(page.getSrc());
        Synchronizer synchronizer = request.getSession().getSynchronizer();
        synchronizer.startQueueing();
        Page._init(page, request, synchronizer);
        WebSocketHandler.notifySessionTrackers(request.getSession(), true);
        
        try {
            pageDefinition.materialize(page);
            page.invoke("afterInitialize");
        } catch (Exception e) {
            synchronizer.clear();
            throw e;
        } finally {
            synchronizer.stopQueueing();
        }
    }
    
    @Override
    public String getRequestType() {
        return "init";
    }
    
}
