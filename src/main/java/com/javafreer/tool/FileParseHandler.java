package com.javafreer.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileParseHandler {

    private List<String> filelist = new ArrayList<String>();

    private void initialList() {
        filelist.clear();
        filelist = new ArrayList<String>();
    }

    public List<String> refreshFileList(String filePath, String suffix) {
        initialList();
        List<String> resultList = new ArrayList<String>();
        File file = new File(filePath);
        if (file.isFile()) {
            resultList.add(file.getAbsoluteFile().toString());
            return resultList;
        }

        List<String> tempFileList = refreshFileList(filePath);
        if (suffix == null || tempFileList == null) {
            return tempFileList;
        }
        for (int i = 0; i < tempFileList.size(); i++) {
            if (tempFileList.get(i).endsWith(suffix)) {
                resultList.add(tempFileList.get(i));
            }
        }
        return resultList;
    }

    private List<String> refreshFileList(String filePath) {
        File dir = new File(filePath);
        File[] files = dir.listFiles();

        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                refreshFileList(files[i].getAbsolutePath());
            } else {
                filelist.add(files[i].getAbsolutePath().toString());
            }
        }
        return filelist;
    }

}
