package com.javafreer.tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class MethodFinder {

    private final String              methodNameSignatureSeperator = ":";

    private Map<String, List<String>> methodsRelations             = new HashMap<String, List<String>>();
    private String                    key;

    private ResultDto                 resultDto;

    /**
     * @param classFilePath refer to the class file path
     * @return Map,key is the methods declare, value is the methods called in current method specified by this key
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RuntimeException
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public Map<String, List<String>> getMethodsRelations(String classFilePath) throws FileNotFoundException,
                                                                              IOException, RuntimeException,
                                                                              CannotCompileException, NotFoundException {
        methodsRelations = new HashMap<String, List<String>>();
        key = null;
        CtClass ctClass = null;
        ClassPool pool = ClassPool.getDefault();
        ctClass = pool.makeClass(new FileInputStream(classFilePath));
        CtMethod[] methods = null;
        methods = ctClass.getDeclaredMethods();
        if (methods == null || methods.length <= 0) {
            return null;
        }

        Object[] objs = ctClass.getAvailableAnnotations();
        for (Object obj : objs) {
            System.out.println("--" + obj.toString());
        }
        try {
            CtClass[] interfaces = ctClass.getInterfaces();
            for (CtClass ctclass : interfaces) {
                ctclass.getClassFile();
            }
        } catch (NotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            key = method.getName() + methodNameSignatureSeperator + method.getSignature();
            if (!methodsRelations.containsKey(key)) {
                methodsRelations.put(key, new ArrayList<String>());
            }
            method.instrument(new ExprEditor() {

                public void edit(MethodCall m) throws CannotCompileException {
                    String calledMethodsString = m.getClassName() + "." + m.getMethodName()
                                                 + methodNameSignatureSeperator + m.getSignature();
                    methodsRelations.get(key).add(calledMethodsString);
                }
            });
        }
        return methodsRelations;
    }

    private CtMethod[] getInheritMethods(CtClass ctClass) {
        if (ctClass == null) {
            return null;
        }
        if (ctClass.isInterface()) {
            System.out.print("");
        }
        CtMethod[] methods = ctClass.getMethods();
        int length1 = methods != null ? methods.length : 0;
        try {
            if (ctClass.getSuperclass() != null) {
                CtMethod[] superClassMethod = getInheritMethods(ctClass.getSuperclass());
                if (superClassMethod == null || superClassMethod.length <= 0) {
                    return methods;
                }
                CtMethod[] allMethods = new CtMethod[length1 + superClassMethod.length];
                for (int i = 0; i < length1; i++) {
                    allMethods[i] = methods[i];
                }
                for (int i = 0; i < superClassMethod.length; i++) {
                    allMethods[length1 + i] = superClassMethod[i];
                }
                return allMethods;
            }
        } catch (NotFoundException e) {
            System.out.println(ctClass.getName() + " have methods:" + methods.length);
            e.printStackTrace();
            return methods;
        }
        return methods;
    }

    public ResultDto getDependentDataFromClassInputStream(InputStream is, String jarName, String className)
                                                                                                           throws IOException {
        if (resultDto != null) {
            resultDto.clear();
        } else {
            resultDto = new ResultDto();
        }
        methodsRelations.clear();
        methodsRelations = new HashMap<String, List<String>>();
        key = null;
        CtClass ctClass = null;
        ClassPool pool = ClassPool.getDefault();
        try {
            ctClass = pool.makeClass(is);
        } catch (IOException e) {
            System.out.println("1:" + jarName + "\t" + className);
            e.printStackTrace();
            return null;

        } catch (RuntimeException e) {
            System.out.println("2:" + jarName + "\t" + className);
            e.printStackTrace();
            return null;
        }

        if (ctClass == null) {
            System.out.println("3:" + jarName + "\t" + className);
            return null;
        }
        ClassFile classFile = ctClass.getClassFile();
        String thisclassName = classFile.getName();

        String testClassName = "com.javafreer.AbstractUrlFactory";
        if (thisclassName.equals(testClassName)) {
            System.out.println();
        }
        boolean isinterface = ctClass.isInterface();// 当前类是接口类
        if (isinterface) {
            String[] interfaces = classFile.getInterfaces();// the supper interface name;
            if (interfaces != null && interfaces.length > 0) {
                for (int i = 0; i < interfaces.length; i++) {
                    resultDto.inputClassDependent(thisclassName, interfaces[i]);
                }
            }
        } else {
            // get the supper class name[class or based
            // class:java.lang.Object/java.lang.Enum], but if the current
            // class
            // is interface,
            // it fails
            String temp = classFile.getSuperclass();
            resultDto.inputClassDependent(thisclassName, temp);
        }

        CtMethod[] methodsIncludeInherit = null;

        methodsIncludeInherit = ctClass.getMethods();// all methods
        if (methodsIncludeInherit != null && methodsIncludeInherit.length > 0) {
            for (int i = 0; i < methodsIncludeInherit.length; i++) {
                CtMethod method = methodsIncludeInherit[i];
                key = method.getName() + methodNameSignatureSeperator + method.getSignature();
                if (methodsRelations.containsKey(key)) {
                    continue;
                }
                methodsRelations.put(key, new ArrayList<String>());
            }
        }
        CtMethod[] methodsInCurrent = ctClass.getDeclaredMethods();
        if (methodsInCurrent == null || methodsInCurrent.length <= 0) {
            ctClass.detach();
            return resultDto;
        }

        for (int i = 0; i < methodsInCurrent.length; i++) {
            CtMethod method = methodsInCurrent[i];
            key = method.getName() + methodNameSignatureSeperator + method.getSignature();
            if (!methodsRelations.containsKey(key)) {
                methodsRelations.put(key, new ArrayList<String>());
            }
            try {
                method.instrument(new ExprEditor() {

                    public void edit(MethodCall m) throws CannotCompileException {
                        String calledMethodsString = m.getClassName() + "." + m.getMethodName()
                                                     + methodNameSignatureSeperator + m.getSignature();
                        methodsRelations.get(key).add(calledMethodsString);
                    }
                });
            } catch (CannotCompileException e) {
                e.printStackTrace();
                ctClass.detach();
                return resultDto;
            }
        }
        if (methodsRelations != null) {
            for (String method : methodsRelations.keySet()) {
                if (methodsRelations.containsKey(method)) {
                    List<String> calledMethods = methodsRelations.get(method);
                    for (int i = 0; i < calledMethods.size(); i++) {
                        String calledMethod = calledMethods.get(i);
                        // if the method invoke a class in the same class, ignore
                        if (thisclassName.equals(MethodUtils.getClassNameFromMethod(calledMethod))) {
                            continue;
                        }
                        resultDto.inputMethodDependent(thisclassName + "." + method, calledMethod);
                    }
                }
                resultDto.inputClassMathod(thisclassName, thisclassName + "." + method);
            }
        }
        ctClass.detach();
        return resultDto;
    }

    public Map<String, List<String>> getMethodsRelationsFromClassInputStream(InputStream is, String classPath,
                                                                             String jarpath) {
        methodsRelations = new HashMap<String, List<String>>();
        key = null;
        CtClass ctClass = null;
        ClassPool pool = ClassPool.getDefault();
        try {
            ctClass = pool.makeClass(is);
        } catch (IOException e) {
            System.out.println(classPath + " in " + jarpath);
            e.printStackTrace();

        } catch (RuntimeException e) {
            System.out.println(classPath + " in " + jarpath);
            e.printStackTrace();
            return null;
        }

        if (ctClass == null) {
            System.out.println(classPath + " in " + jarpath);
            return null;
        }

        CtMethod[] methodsIncludeInherit = null;
        Object[] objs = ctClass.getAvailableAnnotations();
        for (Object obj : objs) {
            System.out.println("--" + obj.toString());
        }
        try {
            CtClass[] interfaces = ctClass.getInterfaces();
            for (CtClass ctclass : interfaces) {
                ctclass.getClassFile();
            }
        } catch (NotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        methodsIncludeInherit = getInheritMethods(ctClass);
        if (methodsIncludeInherit != null && methodsIncludeInherit.length > 0) {
            for (int i = 0; i < methodsIncludeInherit.length; i++) {
                CtMethod method = methodsIncludeInherit[i];
                key = method.getName() + methodNameSignatureSeperator + method.getSignature();
                if (methodsRelations.containsKey(key)) {
                    continue;
                }
                methodsRelations.put(key, new ArrayList<String>());
            }
        }
        CtMethod[] methodsInCurrent = ctClass.getDeclaredMethods();
        if (methodsInCurrent == null || methodsInCurrent.length <= 0) {
            return null;
        }

        for (int i = 0; i < methodsInCurrent.length; i++) {
            CtMethod method = methodsInCurrent[i];
            key = method.getName() + methodNameSignatureSeperator + method.getSignature();
            if (!methodsRelations.containsKey(key)) {
                methodsRelations.put(key, new ArrayList<String>());
            }
            try {
                method.instrument(new ExprEditor() {

                    public void edit(MethodCall m) throws CannotCompileException {
                        String calledMethodsString = m.getClassName() + "." + m.getMethodName()
                                                     + methodNameSignatureSeperator + m.getSignature();
                        // System.out.println(calledMethodsString);
                        methodsRelations.get(key).add(calledMethodsString);
                    }
                });
            } catch (CannotCompileException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println(classPath + " in " + jarpath);
                return null;
            }
        }
        return methodsRelations;
    }

    /**
     * @param is
     * @return List of <methodname:signature>
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RuntimeException
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public List<String> getDeclareMethodsFromClassInStream(InputStream is) throws FileNotFoundException, IOException,
                                                                          RuntimeException, CannotCompileException,
                                                                          NotFoundException {
        List<String> declareMethods = new ArrayList<String>();
        CtClass ctClass = null;
        ClassPool pool = ClassPool.getDefault();
        ctClass = pool.makeClass(is);
        CtMethod[] methods = null;
        methods = ctClass.getDeclaredMethods();
        if (methods == null || methods.length <= 0) {
            return null;
        }
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            String key = method.getName() + methodNameSignatureSeperator + method.getSignature();
            declareMethods.add(key);
        }
        return declareMethods;
    }

    public static void main(String[] args) throws NotFoundException {
        String filePath = "/home/wlq/work/biz.searchproduct.exception/classfiles/intl/biz/product/dao/search/ProductSearchDaoImpl.class";
        // String filePath =
        // "/home/wlq/work/biz.searchproduct.exception/classfiles/intl/sourcing/modules/rfq/datasource/common/GroupSequenceWrapper.class";

        try {
            MethodFinder methodFinder = new MethodFinder();
            Map<String, List<String>> relationships = methodFinder.getMethodsRelations(filePath);
            for (String key : relationships.keySet()) {
                System.out.println("==========key:" + key);
                List<String> value = relationships.get(key);
                for (String v : value) {
                    System.out.println(v);
                }
            }

            ResultDto resultDto = methodFinder.getDependentDataFromClassInputStream(new FileInputStream(filePath), "",
                                                                                    "");
            if (resultDto != null) {
                resultDto.printClassDependent();
                resultDto.printClassMethods();
                resultDto.printMethodDependent();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CannotCompileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
