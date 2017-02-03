package org.carewebframework.web.script;

import java.util.Map;

/**
 * Every script language plugin must implement this interface.
 */
public interface IScriptLanguage {
    
    public interface IParsedScript {
        
        /**
         * Executes the compiled script with optional variables.
         * 
         * @param script The compiled script.
         * @param variables Optional variable assignments (may be null).
         * @return The result of the script evaluation, if any.
         */
        Object run(Map<String, Object> variables);
        
        /**
         * Executes the compiled script.
         * 
         * @param script The compiled script.
         * @return The result of the script evaluation, if any.
         */
        default Object run() {
            return run(null);
        }
    }
    
    /**
     * The language type of the script (e.g., "groovy"). Must be unique.
     * 
     * @return The language type.
     */
    String getType();
    
    /**
     * Compiles the script source.
     * 
     * @param source The script source.
     * @return The compiled script.
     */
    IParsedScript parse(String source);
    
    /**
     * Returns script variable the will represent the calling context.
     * 
     * @return Name of script variable for calling context.
     */
    default String getSelf() {
        return "self";
    }
    
}
