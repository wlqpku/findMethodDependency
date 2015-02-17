package com.javafreer.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.CannotCompileException;
import javassist.NotFoundException;


public class JarFileUtils {

    public static List<String> getClassFileListFromJar(String filePath) throws IOException {
        List<String> classList = new ArrayList<String>();
        File file = new File(filePath);
        JarFile jarFile = new JarFile(file, false, JarFile.OPEN_READ);
        ZipFile zipFile = jarFile;
        final Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            if (!zipEntry.getName().endsWith(".class")) {
                continue;
            }
            // InputStream is = jarFile.getInputStream(zipEntry);
            classList.add(zipEntry.getName());
        }
        return classList;
    }

    /**
     * methods include: classpath.methodname.methodsignature
     * eg:intl/biz/killer/client/dao/KillerClientCategoryKeywordDAO.class.methodName:signameture
     * 
     * @param jarFilePath
     * @return
     * @throws IOException
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws RuntimeException
     */
    public static List<String> getDeclareMethodsFromJar(String jarFilePath) throws IOException, RuntimeException,
                                                                         CannotCompileException, NotFoundException {
        List<String> resultList = new ArrayList<String>();
        File file = new File(jarFilePath);
        JarFile jarFile = new JarFile(file, false, JarFile.OPEN_READ);
        ZipFile zipFile = jarFile;
        final Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            if (!zipEntry.getName().endsWith(".class")) {
                continue;
            }
            InputStream is = jarFile.getInputStream(zipEntry);
            String className = zipEntry.getName();
            MethodFinder methodFinder = new MethodFinder();

            List<String> decalreMethods = methodFinder.getDeclareMethodsFromClassInStream(is);
            is.close();
            if (decalreMethods != null && decalreMethods.size() > 0) {
                for (String methodName : decalreMethods) {
                    resultList.add(className.substring(0, className.indexOf("class")).replace('/', '.') + methodName);
                }
            }

        }
        zipFile.close();
        return resultList;
    }

    public static ResultDto getClassMethodDependentFromJar(String jarFilePath)
                                                                                                     throws IOException,
                                                                                                     RuntimeException,
                                                                                                     CannotCompileException,
                                                                                                     NotFoundException {
        ResultDto result = new ResultDto();
        File file = new File(jarFilePath);
        JarFile jarFile = new JarFile(file, false, JarFile.OPEN_READ);
        ZipFile zipFile = jarFile;
        final Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            String className = zipEntry.getName();
            if (!zipEntry.getName().endsWith(".class")) {
                continue;
            }
            InputStream is = jarFile.getInputStream(zipEntry);
            MethodFinder methodFinder = new MethodFinder();
            ResultDto resultDto = methodFinder.getDependentDataFromClassInputStream(is, jarFilePath, className);
            is.close();
            if (resultDto == null) {
                continue;
            }
            result.add(resultDto);

        }
        return result;
    }

    public static Map<String, List<String>> getDeclareMethodsRelationShipsFromJar(String jarFilePath)
                                                                                                     throws IOException,
                                                                                        RuntimeException,
                                                                            CannotCompileException, NotFoundException {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        File file = new File(jarFilePath);
        JarFile jarFile = new JarFile(file, false, JarFile.OPEN_READ);
        ZipFile zipFile = jarFile;
        final Enumeration zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
            String className = zipEntry.getName();
            if (!zipEntry.getName().endsWith(".class")) {
                continue;
            }
            InputStream is = jarFile.getInputStream(zipEntry);

            MethodFinder methodFinder = new MethodFinder();

            Map<String, List<String>> methodsRelationShip = methodFinder.getMethodsRelationsFromClassInputStream(is,
                                                                                                                 className,
                                                                                                                 jarFile.getName());
            is.close();
            if (methodsRelationShip == null) {
                continue;
            }
            for (String key : methodsRelationShip.keySet()) {
                // System.out.println("methods:" + key);

                String classNameWithoutSuffix = className.substring(0, className.indexOf("class")).replace('/', '.');
                String methodFullName = classNameWithoutSuffix + key;
                resultMap.put(methodFullName, new ArrayList<String>());

                Set<String> denpendentMethodsSet = new HashSet<String>();
                List<String> dependentMethods = methodsRelationShip.get(key);
                if (dependentMethods == null) {
                    continue;
                }
                for (String method : dependentMethods) {
                    // have handled, or depend on the method itself
                    if (denpendentMethodsSet.contains(method) || method.startsWith(classNameWithoutSuffix)) {
                        continue;
                    }
                    denpendentMethodsSet.add(method);
                    resultMap.get(methodFullName).add(method);
                }
            }

        }
        return resultMap;
    }

    public static void main(String[] args) throws RuntimeException, CannotCompileException, NotFoundException {
        String testJarPath = "";
        try {
            // List<String> result = getClassFileListFromJar(testJarPath);
            Map<String, List<String>> resultMap = getDeclareMethodsRelationShipsFromJar(testJarPath);
            for (String key : resultMap.keySet()) {
                if (key == null) {
                    continue;
                }
                System.out.println("=========methods here: " + key);
                List<String> dependentMethods = resultMap.get(key);
                if (dependentMethods == null || dependentMethods.size() <= 0) {
                    continue;
                }
                for (String method : dependentMethods) {
                    System.out.println(method);
                }
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
