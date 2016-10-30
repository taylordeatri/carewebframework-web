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
package org.carewebframework.web.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;

/**
 * Defines actions to be taken during annotation processing when an exception occurs.
 */
public enum OnFailure {
    /**
     * Ignore the failure.
     */
    IGNORE,
    /**
     * Throw an exception.
     */
    EXCEPTION,
    /**
     * Log the failure.
     */
    LOG;
    
    private final Log log = LogFactory.getLog(OnFailure.class);
    
    /**
     * Perform an action.
     * 
     * @param message Message describing the failure.
     * @param args Additional arguments for formatting the message.
     */
    void doAction(String message, Object... args) {
        switch (this) {
            case IGNORE:
                return;
            
            case EXCEPTION:
                message = StrUtil.formatMessage(message, args);
                throw new RuntimeException(message);
                
            case LOG:
                message = StrUtil.formatMessage(message, args);
                log.error(message);
                return;
        }
    }
    
    /**
     * Perform an action.
     * 
     * @param e Exception resulting from the failure.
     */
    void doAction(Exception e) {
        switch (this) {
            case IGNORE:
                return;
            
            case EXCEPTION:
                throw MiscUtil.toUnchecked(e);
                
            case LOG:
                log.error(e.getMessage(), e);
                return;
        }
    }
}
