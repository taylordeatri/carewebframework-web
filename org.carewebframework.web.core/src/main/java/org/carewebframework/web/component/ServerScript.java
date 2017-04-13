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
package org.carewebframework.web.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.core.WebUtil;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.script.IScriptLanguage;
import org.carewebframework.web.script.ScriptRegistry;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * A component wrapping script source code for server-side invocation.
 */
@Component(value = "sscript", widgetClass = "MetaWidget", content = ContentHandling.AS_ATTRIBUTE, parentTag = "*")
public class ServerScript extends BaseScriptComponent {

    private static final String EVENT_DEFERRED = "deferredExecution";

    public static final String EVENT_EXECUTED = "scriptExecution";

    private IScriptLanguage script;

    public ServerScript() {
        super(false);
    }
    
    public ServerScript(String script) {
        super(script, false);
    }

    @Override
    protected void onAttach(Page page) {
        super.onAttach(page);

        if (isDeferred()) {
            EventUtil.post(EVENT_DEFERRED, this, null);
        } else {
            doExecute();
        }
    }

    private Object execute() {
        Assert.notNull(script, "A script type must be specified");
        return script.parse(getScript()).run(Collections.singletonMap(script.getSelf(), this));
    }

    private String getScript() {
        if (getSrc() != null) {
            return getExternalScript();
        } else {
            return getContent();
        }
    }

    private String getExternalScript() {
        try {
            Resource resource = WebUtil.getResource(getSrc());
            return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    @Override
    @PropertySetter("type")
    public void setType(String type) {
        type = nullify(type);
        script = type == null ? null : ScriptRegistry.getInstance().get(type);

        if (script == null && type != null) {
            throw new IllegalArgumentException("Unknown script type: " + type);
        }
        
        super.setType(type);
    }

    @EventHandler(value = EVENT_DEFERRED, syncToClient = false)
    private void onDeferredExecution() {
        doExecute();
    }

    private void doExecute() {
        EventUtil.post(new Event(EVENT_EXECUTED, this, execute()));
    }
}
