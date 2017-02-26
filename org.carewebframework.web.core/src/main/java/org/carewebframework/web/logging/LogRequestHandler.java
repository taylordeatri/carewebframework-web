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
package org.carewebframework.web.logging;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.web.client.ClientRequest;
import org.carewebframework.web.websocket.WebSocketHandler.IRequestHandler;

/**
 * Handles a logging request from the client. This is effectively a bridge between the client's
 * logging framework and that of the server.
 */
public class LogRequestHandler implements IRequestHandler {
    
    private static final Log log = LogFactory.getLog(LogRequestHandler.class);
    
    @Override
    public void handleRequest(ClientRequest request) throws Exception {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = request.getData(Map.class);
        String level = (String) data.get("level");
        Object message = data.get("message");
        
        switch (LogUtil.toLevel(level)) {
            case DEBUG:
                log.debug(message);
                break;
            
            case ERROR:
                log.error(message);
                break;
            
            case FATAL:
                log.fatal(message);
                break;
            
            case INFO:
                log.info(message);
                break;
            
            case TRACE:
                log.trace(message);
                break;
            
            case WARN:
                log.warn(message);
                break;
            
            default:
                log.info("Unknown logging level: " + level);
                log.info(message);
                break;
        }
    }
    
    @Override
    public String getRequestType() {
        return "log";
    }
    
}
