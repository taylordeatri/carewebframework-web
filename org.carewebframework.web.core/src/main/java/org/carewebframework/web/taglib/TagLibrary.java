package org.carewebframework.web.taglib;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single tag library. We only provide function definitions since custom tags are not
 * supported.
 */
public class TagLibrary {
    
    private final Map<String, TagLibraryFunction> functions = new HashMap<>();
    
    private final String uri;
    
    public TagLibrary(String uri) {
        this.uri = uri;
    }
    
    public String getUri() {
        return uri;
    }
    
    /**
     * Create and add a function definition based on the input parameters.
     * 
     * @param functionName The function name.
     * @param className The name of the implementing class.
     * @param methodSignature The signature of the target method.
     */
    public void addFunction(String functionName, String className, String methodSignature) {
        TagLibraryFunction newFunction = new TagLibraryFunction(className, methodSignature);
        TagLibraryFunction oldFunction = functions.get(functionName);
        
        if (oldFunction != null) {
            if (!oldFunction.equals(newFunction)) {
                throw new RuntimeException("Duplicate tag library function name: " + functionName);
            }
        } else {
            functions.put(functionName, newFunction);
        }
    }
    
    /**
     * Returns a function definition given its name.
     * 
     * @param functionName The function name.
     * @return The function definition, or null if not found.
     */
    public TagLibraryFunction getFunction(String functionName) {
        return functions.get(functionName);
    }
}
