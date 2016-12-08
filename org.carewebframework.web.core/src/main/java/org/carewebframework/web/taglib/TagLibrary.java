package org.carewebframework.web.taglib;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single tag library. We only provide function definitions since custom tags are not
 * supported.
 */
public class TagLibrary {
    
    /**
     * Represents a function definition block from a tag library.
     */
    public static class Function {
        
        private final String className;
        
        private final String methodSignature;
        
        private final String methodName;
        
        private Function(String className, String methodSignature) {
            this.className = className.trim();
            this.methodSignature = methodSignature;
            this.methodName = extractMethodName(methodSignature);
        }
        
        private String extractMethodName(String methodSignature) {
            int i = methodSignature.indexOf(" ");
            int j = methodSignature.indexOf("(", i);
            return methodSignature.substring(i + 1, j < 0 ? methodSignature.length() : j).trim();
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        @Override
        public String toString() {
            return className + "." + methodName;
        }
        
        @Override
        public boolean equals(Object object) {
            if (object instanceof Function) {
                Function fcn = (Function) object;
                return fcn.className.equals(className) && fcn.methodSignature.equals(methodSignature);
            }
            
            return false;
        }
        
    }
    
    private final Map<String, Function> functions = new HashMap<>();
    
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
        Function newFunction = new Function(className, methodSignature);
        Function oldFunction = functions.get(functionName);
        
        if (oldFunction != null) {
            if (!oldFunction.equals(newFunction)) {
                throw new RuntimeException("Duplicate tag function name: " + functionName);
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
    public Function getFunction(String functionName) {
        return functions.get(functionName);
    }
}
