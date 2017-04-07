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
package org.carewebframework.web.script.jython;

import java.util.Map;
import java.util.Map.Entry;

import org.carewebframework.web.script.IScriptLanguage;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

/**
 * Support for embedding Jython scripts.
 */
public class JythonScript implements IScriptLanguage {
    
    /**
     * Wrapper for a parsed Jython script
     */
    public static class ParsedScript implements IParsedScript {
        
        private final PyCode script;
        
        public ParsedScript(String source) {
            try (PythonInterpreter interp = new PythonInterpreter()) {
                script = interp.compile(source);
            }
        }
        
        @Override
        public Object run(Map<String, Object> variables) {
            try (PythonInterpreter interp = new PythonInterpreter()) {

                if (variables != null) {
                    for (Entry<String, Object> entry : variables.entrySet()) {
                        interp.set(entry.getKey(), entry.getValue());
                    }
                }
                return interp.eval(script);
            }
        }
    }
    
    /**
     * @see org.carewebframework.web.script.IScriptLanguage#getType()
     */
    @Override
    public String getType() {
        return "jython";
    }
    
    /**
     * @see org.carewebframework.web.script.IScriptLanguage#parse(java.lang.String)
     */
    @Override
    public IParsedScript parse(String source) {
        return new ParsedScript(source);
    }
    
}
