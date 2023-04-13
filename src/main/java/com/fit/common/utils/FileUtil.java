package com.fit.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

    public static void copyFile(String sourceDir, String targetDir) throws Exception {
        new File(targetDir).mkdirs();
        File[] fileList = new File(sourceDir).listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {
                copyFile(fileList[i], new File(new File(targetDir).getAbsolutePath() + File.separator + fileList[i].getName()));
            } else if (fileList[i].isDirectory()) {
                String dir1 = sourceDir + File.separator + fileList[i].getName();
                String dir2 = targetDir + File.separator + fileList[i].getName();
                copyFile(dir1, dir2);
            }
        }
    }

    public static void copyFile(File sourceFile, File targetFile) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fi = new FileInputStream(sourceFile);
            fo = new FileOutputStream(targetFile);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0L, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
