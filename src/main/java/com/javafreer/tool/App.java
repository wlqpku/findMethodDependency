package com.javafreer.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.NotFoundException;

/**
 * class文件解析入口
 * 
 * @author wlq Jan 22, 2014 7:51:00 PM
 */
public class App 
{

    private static final String PREFIX          = "com.javafreer.intl";
    private static final String JAR_FILE_SUFFIX = ".jar";
    private static final int    PRINT_FRE       = 100;

    public static void runMethodAndClassDependent(String[] args) {
        if (args == null || args.length <= 0) {
            System.out.println("please input at least one directory or jar file path");
            return;
        }

        ResultDto resultDto = new ResultDto();

        Map<String, List<String>> problemMethods = new HashMap<String, List<String>>();

        // get all jar files from the directories of input
        FileParseHandler fileParseHandler = new FileParseHandler();
        List<String> jarFileList = new ArrayList<String>();

        System.out.println("you have input " + args.length + " parameters");

        for (int i = 0; i < args.length; i++) {
            String filePath = args[i];
            System.out.println("=======begin to handle path: " + filePath);
            List<String> tempJarFileList = fileParseHandler.refreshFileList(filePath, JAR_FILE_SUFFIX);
            if (tempJarFileList != null && tempJarFileList.size() > 0) {

                System.out.println(tempJarFileList.size() + " jar files found");
                jarFileList.addAll(tempJarFileList);
            } else {
                System.out.println("none jar files found");
            }
        }
        if (jarFileList == null || jarFileList.size() <= 0) {
            return;
        }

        System.out.println("Totally, Found Jar Files: " + jarFileList.size());

        try {
            Set<String> haveDoneJarFiles = new HashSet<String>();
            for (int jarFileIndex = 0; jarFileIndex < jarFileList.size(); jarFileIndex++) {
                if (jarFileIndex % PRINT_FRE == 0) {
                    System.out.println("Finish scan the " + jarFileIndex + "th jar files");
                }
                String jarFilePath = jarFileList.get(jarFileIndex);
                if (haveDoneJarFiles.contains(jarFilePath)) {
                    continue;
                }
                haveDoneJarFiles.add(jarFilePath);
                ResultDto tempResultDto = JarFileUtils.getClassMethodDependentFromJar(jarFilePath);
                if (tempResultDto == null) {
                    continue;
                }
                resultDto.add(tempResultDto);
            }

            // begin to check the dependency
            if (resultDto != null) {
                Map<String, Set<String>> methodDependent = resultDto.getMethodDependentMap();
                Map<String, Set<String>> classDependent = resultDto.getClassDependentMap();
                System.out.println("There are class:" + classDependent.size());

                System.out.println("there are methods: " + methodDependent.keySet().size());

                for (String method : methodDependent.keySet()) {
                    if (method == null || !method.startsWith(PREFIX)) {
                        continue;
                    }
                    Set<String> needMethods = methodDependent.get(method);
                    if (needMethods == null || needMethods.size() <= 0) {
                        continue;
                    }
                    for (String needMethod : needMethods) {
                        if (!needMethod.startsWith(PREFIX) || ExcludeMethods.isExludeMethod(needMethod)) {
                            continue;
                        }
                        // the method we need is not exist
                        if (resultDto.seekMethod(needMethod)) {
                            continue;
                        }
                        // check whether is the parent method
                        if (!problemMethods.containsKey(method)) {
                            problemMethods.put(method, new ArrayList<String>());
                        }
                        problemMethods.get(method).add(needMethod);
                    }
                }
            }
            if (problemMethods.isEmpty()) {
                System.out.println("Congratulations!");
            } else {
                System.out.println("some methods is not available: " + problemMethods.keySet().size());

                for (String method : problemMethods.keySet()) {
                    System.out.println("Method:" + method);
                    System.out.println("Depend on but not found:");
                    for (String needMethod : problemMethods.get(method)) {
                        System.out.println("\t" + needMethod);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        runMethodAndClassDependent(args);
    }
}
