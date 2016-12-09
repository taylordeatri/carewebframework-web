package org.carewebframework.web.taglib;

/**
 * Represents a function definition block from a tag library. Currently ignore the method signature
 * except for extracting the method name. Instead, we rely on the EL parser to find the method
 * signature that matches the parameter list.
 */
public class TagLibraryFunction {
    
    private final String className;
    
    private final String methodSignature;
    
    private final String methodName;
    
    TagLibraryFunction(String className, String methodSignature) {
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
        if (object instanceof TagLibraryFunction) {
            TagLibraryFunction fcn = (TagLibraryFunction) object;
            return fcn.className.equals(className) && fcn.methodSignature.equals(methodSignature);
        }
        
        return false;
    }
    
}
