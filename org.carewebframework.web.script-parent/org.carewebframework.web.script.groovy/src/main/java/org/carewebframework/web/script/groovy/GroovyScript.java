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
package org.carewebframework.web.script.groovy;

import java.util.Map;

import org.carewebframework.web.script.IScriptLanguage;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Utility methods for compiling and executing groovy scripts.
 */
public class GroovyScript implements IScriptLanguage {
    
    private static GroovyShell shell;
    
    public static synchronized GroovyShell getGroovyShell() {
        if (shell == null) {
            shell = new GroovyShell();
        }
        
        return shell;
    }
    
    public static class ParsedScript implements IParsedScript {
        
        private final Script script;
        
        public ParsedScript(String source) {
            script = getGroovyShell().parse(source);
        }
        
        @Override
        public Object run(Map<String, Object> variables) {
            script.setBinding(variables == null ? null : new Binding(variables));
            return script.run();
        }
        
    }
    
    @Override
    public String getType() {
        return "groovy";
    }
    
    @Override
    public IParsedScript parse(String source) {
        return new ParsedScript(source);
    }
    
}
