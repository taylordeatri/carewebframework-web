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
package org.carewebframework.web.script.clojure;

import java.util.Map;

import org.carewebframework.web.script.IScriptLanguage;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

/**
 * Support for embedding Clojure scripts.
 */
public class ClojureScript implements IScriptLanguage {
    
    private static volatile Var COMPILE;
    
    /**
     * Wrapper for a parsed Clojure script
     */
    public static class ParsedScript implements IParsedScript {
        
        private final IFn script;
        
        public ParsedScript(String source) {
            init();
            this.script = (IFn) COMPILE.invoke("(fn [args] " + source + ")");
        }
        
        @Override
        public Object run(Map<String, Object> variables) {
            return script.invoke(variables);
        }
    }
    
    private static synchronized void init() {
        if (COMPILE == null) {
            COMPILE = RT.var("clojure.core", "load-string");
        }
    }
    
    /**
     * @see org.carewebframework.web.script.IScriptLanguage#getType()
     */
    @Override
    public String getType() {
        return "clojure";
    }
    
    /**
     * @see org.carewebframework.web.script.IScriptLanguage#parse(java.lang.String)
     */
    @Override
    public IParsedScript parse(String source) {
        return new ParsedScript(source);
    }
    
}
