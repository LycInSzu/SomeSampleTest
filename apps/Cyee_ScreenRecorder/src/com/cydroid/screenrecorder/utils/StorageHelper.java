package com.cydroid.screenrecorder.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

public class StorageHelper {
    private static final String TAG = "StorageHelper";

    private static final String DEFAULT_VIDEO_DIR = "/storage/emulated/0";
    // private static final Logger MLOGGER = Logger.getLogger(StorageHelper.class.getSimpleName());
    private static StorageManager storageManager = null;

    public static boolean isInternalPath(final String path) {
        return path.equals(DEFAULT_VIDEO_DIR);
    }

    public static String getSDcardRecordVideoDir(final Context context) {
        String videoDirPath = null;
        if (storageManager == null) {
            storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        String[] paths;
        try {
            paths = storageManager.getVolumePaths();

        } catch (Exception e) {
            Log.e(TAG, "getSDcardRecordVideoDir()--> Fail to access external storage");
            return null;
        }

        if (isSDCardInserted(context)) {
            if (paths.length == 1) {
                videoDirPath = paths[0];
            } else {
                if (android.os.SystemProperties.get("ro.gn.gn2sdcardswap", "no").equals("yes")) {
                    videoDirPath = paths[0];
                } else {
                    videoDirPath = paths[1];
                }
                return videoDirPath;
            }
        }
        return null;
    }

    public static long getVolumeSpace(Context context, String volumePath) {
        // xionghg 20180314 add for potential bug begin
        if (TextUtils.isEmpty(volumePath)) {
            Log.e(TAG, "getVolumeSpace: e=", new Exception());
            return -1L;
        }
        // xionghg 20180314 add for potential bug end
        if (storageManager == null) {
            storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        String state = storageManager.getVolumeState(volumePath);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                StatFs stat = new StatFs(volumePath);
                return stat.getAvailableBlocks() * (long) stat.getBlockSize();
            } catch (Exception e) {
                Log.e(TAG, "getVolumeSpace() --> Fail to access external storage");
            }
        }
        return -1L;
    }

    public static boolean isSDCardInserted(final Context context) {
        //chenyee zhaocaili 20180426 add for CSW1703A-1992 begin
        if (storageManager == null) {
            storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        //chenyee zhaocaili 20180426 add for CSW1703A-1992 end
        StorageVolume[] storageVolume = storageManager.getVolumeList();
        if (storageVolume != null) {
            for (int i = 0; i < storageVolume.length; i++) {
                if (Environment.MEDIA_MOUNTED.equals(storageManager.getVolumeState(storageVolume[i].getPath()))
                        && storageVolume[i].isRemovable()
                        && storageVolume[i].getPath().contains("/storage/")) {
                    Log.d(TAG, "isSDCardInserted() SD card is inserted.");
                    return true;
                }
            }
        }
        Log.d(TAG, "isSDCardInserted() --> SD card is not inserted!");
        return false;
    }

    //chenyee zhaocaili 20180514 add for CSW1703CX-551 begin
    public static String getInternalRootDir(final Context context) {
        if (storageManager == null) {
            storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        String[] paths;
        try {
            paths = storageManager.getVolumePaths();

        } catch (Exception e) {
            return null;
        }

        for (String path : paths){
            if(path.contains(DEFAULT_VIDEO_DIR)){
                return path;
            }
        }
        return null;
    }
    //chenyee zhaocaili 20180514 add for CSW1703CX-551 end
}
