package com.javafreer.tool;

public class ExcludeMethods {

    private static final String[] methods = { "clone:()", "wait:()", "getName:()", "values:()", "Object:()",
            "equals:(Ljava/lang/Object;)", "getName:()Ljava/lang/String;", "hashCode:()", "notify:()", "finalize:()",
            "notifyAll:()", "toString:()", "wait:(", "name:()", "ordinal:()", "valueOf:(", "getDeclaringClass:()",
            // exception class
            "getMessage:()Ljava/lang/String;", "getCause:()Ljava/lang/Throwable;" };
    private static final String   basicClassPrefix = "java.lang.";

    public static boolean isExludeMethod(String method) {
        if (method == null) {
            return true;
        }
        for (int i = 0; i < methods.length; i++) {
            if (method.contains(methods[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBasicJavaClass(String className) {
        if (className != null && className.startsWith(basicClassPrefix)) {
            return true;
        }
        return false;
    }
}
