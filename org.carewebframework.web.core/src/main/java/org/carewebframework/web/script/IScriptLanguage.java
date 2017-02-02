package org.carewebframework.web.script;

import java.util.Map;

/**
 * Every script language plugin must implement this interface.
 */
public interface IScriptLanguage {
    
    /**
     * The language type of the script (e.g., "groovy"). Must be unique.
     * 
     * @return The language type.
     */
    String getType();
    
    /**
     * Executes the script source with optional variables.
     * 
     * @param source The script source.
     * @param variables Optional variable assignments (may be null).
     * @return The result of the script evaluation, if any.
     */
    Object execute(String source, Map<String, Object> variables);
    
    /**
     * Executes the script source.
     * 
     * @param source The script source.
     * @return The result of the script evaluation, if any.
     */
    default Object execute(String source) {
        return execute(source, null);
    }
}
