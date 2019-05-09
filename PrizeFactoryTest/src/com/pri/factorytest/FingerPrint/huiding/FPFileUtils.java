/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.pri.factorytest.FingerPrint.huiding;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FPFileUtils {
    private static final String TAG = "FPFileUtils";
    public static final String CSVHEAD = "ChipID,Result,Error,Time,Bad Point Num,Cluster Num,Pixel of largest bad cluster," +
            "temporal noise,saptial noise,screen light leak ratio,Flesh touchdiff,nScaleRatio,fov,relative illuminance,struct ratio," +
            "pnNoise,pnSSNR,sharpness,contrast,pnP2P,Chart touchdiff\n";

    public static String PATH = "";
    public static String DATA_PATH;

    public static void init(String rootPath) {
        Log.d(TAG, "rootPath: " + rootPath);

        if (rootPath == null) {
            FPFileUtils.PATH = Environment.getExternalStorageDirectory().getPath();
        } else {
            FPFileUtils.PATH = rootPath;
        }
        //DATA_PATH = FPFileUtils.PATH + "/gf_data/factory_test/";
        //DATA_PATH = "/data/gf_data/factory_test/";
        DATA_PATH = "/data/vendor/gf_data/factory_test/";

        createDirectory(DATA_PATH);
    }

    public static boolean createDirectory(String path) {

        boolean result = true;
        File file = new File(path);
        if (!file.exists()) {
            result = file.mkdirs();
        } else if (!file.isDirectory()) {
            //remove for make sure the default dir is empty
            result = file.delete();
            if (!result) {
                Log.e(TAG, "createDirector error path: " + path);
            }
            result = file.mkdirs();
        }
        return result;
    }

    public static final String getParentDirectoryPath(String filePath) {
        if (filePath != null) {
            int index = filePath.lastIndexOf("/");
            if (index >= 0) {
                return filePath.substring(0, index);
            }
        }

        return null;
    }

    private static boolean createParentDirIfNecessary(String filePath) {
        String dir = getParentDirectoryPath(filePath);
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            return dirFile.mkdirs();
        }

        return true;
    }

    public static void removeFile(String path) {

        if (path != null) {
            File file = new File(path);
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    Log.e(TAG, "delete file fail");
                }
            }
        }
    }

    public static void saveContentAsFile(String chipID, String timeTag, String content) {
        String path = DATA_PATH + chipID + "/" +  timeTag + "/" + chipID + "_log.csv";
        Log.d(TAG, "saveContentAsFile path: " + path);

        if (path == null || content.isEmpty()) {
            return;
        }

        File file = new File(path);
        boolean ex = file.exists();

        if (!createParentDirIfNecessary(path)) {
            Log.e(TAG, "create parent directory failed");
            return;
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(path, ex);
            if( !ex){
                stream.write(CSVHEAD.getBytes("UTF-8"));
            }
            stream.write(content.getBytes("UTF-8"));

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
