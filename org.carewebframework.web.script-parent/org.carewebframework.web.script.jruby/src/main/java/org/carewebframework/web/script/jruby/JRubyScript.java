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
package org.carewebframework.web.script.jruby;

import java.util.Map;
import java.util.Map.Entry;

import org.carewebframework.web.script.IScriptLanguage;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils.EvalUnit;

/**
 * Support for embedding Ruby scripts.
 */
public class JRubyScript implements IScriptLanguage {

    /**
     * Wrapper for a parsed Ruby script
     */
    public static class ParsedScript implements IParsedScript {

        private final ScriptingContainer container;

        private final EvalUnit unit;

        public ParsedScript(String source) {
            container = new ScriptingContainer();
            unit = container.parse(source);
        }

        @Override
        public Object run(Map<String, Object> variables) {
            container.clear();

            if (variables != null) {
                for (Entry<String, Object> entry : variables.entrySet()) {
                    container.put(entry.getKey(), entry.getValue());
                }
            }

            return unit.run();
        }
    }

    /**
     * @see org.carewebframework.web.script.IScriptLanguage#getType()
     */
    @Override
    public String getType() {
        return "jruby";
    }

    /**
     * Global variables in JRuby must start with a '$' character.
     *
     * @see org.carewebframework.web.script.IScriptLanguage#getSelf()
     */
    @Override
    public String getSelf() {
        return "$self";
    }

    /**
     * @see org.carewebframework.web.script.IScriptLanguage#parse(java.lang.String)
     */
    @Override
    public IParsedScript parse(String source) {
        return new ParsedScript(source);
    }

}
