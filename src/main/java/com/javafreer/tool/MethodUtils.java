package com.javafreer.tool;

public class MethodUtils {

    /**
     * given:com.javafreer.ProductSearchDaoImpl.join:(Ljava/util/List;Ljava/lang/String;)Ljava /lang/String;
     * return:com.javafreer.ProductSearchDaoImpl
     * 
     * @param methodFullName
     * @return
     */
    public static String getClassNameFromMethod(String methodFullName) {
        if (methodFullName == null) {
            return null;
        }
        int lastDotIndex = methodFullName.lastIndexOf(".");
        return methodFullName.substring(0, lastDotIndex);
    }

    /**
     * given:com.javafreer.ProductSearchDaoImpl.join:(Ljava/util/List;Ljava/lang/String;)Ljava /lang/String; return:
     * join:(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;
     * 
     * @param methodFullName
     * @return
     */
    public static String getMethodName(String methodFullName) {
        if (methodFullName == null) {
            return null;
        }
        int lastDotIndex = methodFullName.lastIndexOf(".");
        return methodFullName.substring(lastDotIndex + 1, methodFullName.length());
    }
}
