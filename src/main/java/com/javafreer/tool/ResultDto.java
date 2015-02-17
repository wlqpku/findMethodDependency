package com.javafreer.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResultDto {

    private Map<String, Set<String>> classDependentMap; // child class->supper classes
    private Map<String, Set<String>> classMethodsMap;   // class->declared methods
    private Map<String, Set<String>> methodDependentMap; // method dependent on other methods

    public Set<String> getParentClass(String currentClassName) {
        if(classDependentMap==null || classDependentMap.isEmpty()){
            return null;
        }
        return classDependentMap.get(currentClassName);
    }

    private boolean recurionSeekMethodFromParentClass(String methodFullName, String currentClassName) {
        if (methodFullName == null || currentClassName == null) {
            return false;
        }

        // 如果是java基本存在的类型，说明已经OK，默认存在
        if (ExcludeMethods.isBasicJavaClass(currentClassName) && ExcludeMethods.isExludeMethod(methodFullName)) {
            return true;
        }
        if (classMethodsMap == null) {
            return false;
        }

        String pureMethodName = MethodUtils.getMethodName(methodFullName);
        Set<String> methods = classMethodsMap.get(currentClassName);
        String currentMethodFullName = currentClassName + "." + pureMethodName;
        if (methods != null && methods.contains(currentMethodFullName)) {
            return true;
        }
        if (classDependentMap != null) {
            Set<String> parentClassNames = classDependentMap.get(currentClassName);
            if (parentClassNames == null || parentClassNames.isEmpty()) {
                return false;
            }
            for (String parentClass : parentClassNames) {
                boolean check = recurionSeekMethodFromParentClass(methodFullName, parentClass);
                if (check) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * fullname: com.javafreer.ProductSearchDaoImpl.join:(Ljava/util/List;Ljava/lang/String;)Ljava
     * 
     * @param methodFullName
     * @return
     */
    public boolean seekMethod(String methodFullName) {
        if (methodFullName == null || methodFullName.length() <= 0) {
            return false;
        }

        if (methodDependentMap != null && methodDependentMap.containsKey(methodFullName)) {
            return true;
        }

        String className = MethodUtils.getClassNameFromMethod(methodFullName);

        return recurionSeekMethodFromParentClass(methodFullName, className);

    }

    public void add(ResultDto resultDto) {
        if (resultDto == null) {
            return;
        }

        Map<String, Set<String>> classdp = resultDto.getClassDependentMap();
        Map<String, Set<String>> classmt = resultDto.getClassMethodsMap();
        Map<String, Set<String>> methodDp = resultDto.getMethodDependentMap();

        if (classdp != null && !classdp.isEmpty()) {
            for (String key : classdp.keySet()) {
                Set<String> classset = classdp.get(key);
                for (String cl : classset) {
                    this.inputClassDependent(key, cl);
                }
            }
        }

        if (classmt != null && !classmt.isEmpty()) {
            for (String key : classmt.keySet()) {
                Set<String> classset = classmt.get(key);
                for (String cl : classset) {
                    this.inputClassMathod(key, cl);
                }
            }
        }

        if (methodDp != null && !methodDp.isEmpty()) {
            for (String key : methodDp.keySet()) {
                Set<String> classset = methodDp.get(key);
                for (String cl : classset) {
                    this.inputMethodDependent(key, cl);
                }
            }
        }
    }

    public void printClassDependent() {
        if (classDependentMap != null && !classDependentMap.isEmpty()) {
            for (String classname : classDependentMap.keySet()) {
                System.out.println("====" + classname + ":");
                for (String supperclass : classDependentMap.get(classname)) {
                    System.out.println(supperclass);
                }
            }
        }
    }

    public void printClassMethods() {
        if (classMethodsMap != null && !classMethodsMap.isEmpty()) {
            for (String classname : classMethodsMap.keySet()) {
                System.out.println("====" + classname + " has methods:");
                for (String method : classMethodsMap.get(classname)) {
                    System.out.println(method);
                }
            }
        }
    }

    public void printMethodDependent() {
        if (methodDependentMap != null && !methodDependentMap.isEmpty()) {
            for (String method : methodDependentMap.keySet()) {
                System.out.println("====" + method + " method depend on:");
                for (String dmethod : methodDependentMap.get(method)) {
                    System.out.println(dmethod);
                }
            }
        }
    }

    public void clear() {
        if (classDependentMap != null) {
            classDependentMap.clear();
        }
        if (classMethodsMap != null) {
            classMethodsMap.clear();
        }
        if (methodDependentMap != null) {
            methodDependentMap.clear();
        }
    }

    public void inputClassDependent(String currentClass, String supperClass) {
        if (classDependentMap == null) {
            classDependentMap = new HashMap<String, Set<String>>();
        }
        if (!classDependentMap.containsKey(currentClass)) {
            classDependentMap.put(currentClass, new HashSet<String>());
        }

        if (classDependentMap.get(currentClass).contains(supperClass)) {
            return;
        }
        classDependentMap.get(currentClass).add(supperClass);
    }

    public void inputClassMathod(String className, String method) {
        if (classMethodsMap == null) {
            classMethodsMap = new HashMap<String, Set<String>>();
        }
        if (!classMethodsMap.containsKey(className)) {
            classMethodsMap.put(className, new HashSet<String>());
        }
        if (classMethodsMap.get(className).contains(method)) {
            return;
        }
        classMethodsMap.get(className).add(method);
    }

    public void inputMethodDependent(String method, String dependentMethod) {
        if (methodDependentMap == null) {
            methodDependentMap = new HashMap<String, Set<String>>();
        }
        if (!methodDependentMap.containsKey(method)) {
            methodDependentMap.put(method, new HashSet<String>());
        }
        if (methodDependentMap.get(method).contains(dependentMethod)) {
            return;
        }
        methodDependentMap.get(method).add(dependentMethod);
    }

    public Map<String, Set<String>> getClassDependentMap() {
        return classDependentMap;
    }

    public void setClassDependentMap(Map<String, Set<String>> classDependentMap) {
        this.classDependentMap = classDependentMap;
    }

    public Map<String, Set<String>> getClassMethodsMap() {
        return classMethodsMap;
    }

    public void setClassMethodsMap(Map<String, Set<String>> classMethodsMap) {
        this.classMethodsMap = classMethodsMap;
    }

    public Map<String, Set<String>> getMethodDependentMap() {
        return methodDependentMap;
    }

    public void setMethodDependentMap(Map<String, Set<String>> methodDependentMap) {
        this.methodDependentMap = methodDependentMap;
    }

}
