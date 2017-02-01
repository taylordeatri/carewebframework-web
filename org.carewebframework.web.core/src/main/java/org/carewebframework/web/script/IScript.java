package org.carewebframework.web.script;

import java.util.Map;

public interface IScript {
    
    String getType();
    
    Object execute(String source, Map<String, Object> variables);
    
    default Object execute(String source) {
        return execute(source, null);
    }
}
