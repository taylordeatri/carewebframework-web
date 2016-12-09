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
package org.carewebframework.web.core;

import java.util.TimeZone;

import org.carewebframework.common.Localizer;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Initializes the Localizer class with the message source and locale resolver.
 */
public class InitLocalizer {
    
    public static void init(MessageSource messageSource) {
        Localizer.registerMessageSource((id, locale, args) -> {
            return messageSource.getMessage(id, args, locale);
            
        });
        
        Localizer.setLocaleResolver(() -> {
            return LocaleContextHolder.getLocale();
        });
        
        Localizer.setTimeZoneResolver(() -> {
            TimeZone tz = null;
            Page page = ExecutionContext.getPage();
            Integer offset = page == null ? null : page.getBrowserInfo("timezoneOffset", Integer.class);
            
            if (offset != null) {
                String id = "GMT" + (offset < 0 ? "-" : "+") + "%02d:%02d";
                offset = Math.abs(offset);
                id = String.format(id, offset / 60, offset % 60);
                tz = TimeZone.getTimeZone(id);
            }
            
            return tz == null ? TimeZone.getDefault() : tz;
        });
    }
    
    private InitLocalizer() {
    }
}
