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
package org.carewebframework.web.script.renjin;

import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptException;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.script.IScriptLanguage;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

/**
 * Support for embedding Renjin (R language) scripts.
 */
public class RScript implements IScriptLanguage {

    private static volatile RenjinScriptEngineFactory factory;
    
    /**
     * Wrapper for a Renjin script. Note that Renjin does not support pre-compiling scripts.
     */
    public static class ParsedScript implements IParsedScript {

        private final String source;

        public ParsedScript(String source) {
            this.source = source.trim();
        }

        @Override
        public Object run(Map<String, Object> variables) {
            RenjinScriptEngine engine = getFactory().getScriptEngine();

            if (variables != null) {
                for (Entry<String, Object> entry : variables.entrySet()) {
                    engine.put(entry.getKey(), entry.getValue());
                }
            }

            try {
                return engine.eval(source);
            } catch (ScriptException e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }

    public static synchronized RenjinScriptEngineFactory getFactory() {
        if (factory == null) {
            factory = new RenjinScriptEngineFactory();
        }
        
        return factory;
    }

    /**
     * @see org.carewebframework.web.script.IScriptLanguage#getType()
     */
    @Override
    public String getType() {
        return "renjin";
    }

    /**
     * @see org.carewebframework.web.script.IScriptLanguage#parse(java.lang.String)
     */
    @Override
    public IParsedScript parse(String source) {
        return new ParsedScript(source);
    }

}
