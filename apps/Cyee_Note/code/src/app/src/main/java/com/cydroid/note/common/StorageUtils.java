package com.cydroid.note.common;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.cydroid.note.common.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {

    private static final String TAG = "StorageUtils";
    private static final int NO_SPACE_ERROR = -1;

    public static List<String> getLocalRootPath(Context context) {
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
            String[] volumePaths = (String[]) method.invoke(storageManager);
            List<String> storagePaths = new ArrayList<>();
            for (String path : volumePaths) {
                storagePaths.add(path);
            }
            return storagePaths;
        } catch (Exception ex) {
            Log.d(TAG, "error:" + ex);
        }

        return null;
    }

    public static File createOtherSdCardFile(List<String> rootPaths, String absolutePath) {
        int length = absolutePath.length();

        String curPrefix = null;
        String suffix = null;
        for (String root : rootPaths) {
            if (absolutePath.startsWith(root)) {
                curPrefix = root;
                int l = root.length();
                suffix = absolutePath.substring(l, length);
                break;
            }
        }

        if (curPrefix != null && suffix != null) {
            for (String prefix : rootPaths) {
                if (!prefix.equals(curPrefix)) {
                    absolutePath = prefix + suffix;
                    File file = new File(absolutePath);
                    return file;
                }
            }
        }
        return null;
    }

    public static long getFileAvailableBytes(List<String> rootPaths, File file) {
        String curFileSD = null;
        for (String root : rootPaths) {
            if (file.getAbsolutePath().startsWith(root)) {
                curFileSD = root;
                break;
            }
        }
        return getAvailableBytes(curFileSD);
    }

    public static long getAvailableBytes(String path) {
        if (TextUtils.isEmpty(path)) {
            return NO_SPACE_ERROR;
        }
        try {
            File pathFile = new File(path);
            StatFs stat = new StatFs(pathFile.getAbsolutePath());
            long availableBlocks = 0;
            long blockSize = 0;
            if (Build.VERSION.SDK_INT >= 18) {
                availableBlocks = stat.getAvailableBlocksLong();
                blockSize = stat.getBlockSizeLong();
            } else {
                availableBlocks = stat.getAvailableBlocks();
                blockSize = stat.getBlockSize();
            }
            return availableBlocks * blockSize;
        } catch (Exception e) {
            Log.d(TAG, "Fail to access external storage:" + e);
        }
        return NO_SPACE_ERROR;
    }

    public static File getAvailableFileDirectory(Context context, long size, File defaultFile) {
        List<String> rootPaths = StorageUtils.getLocalRootPath(context);
        File destPathRoot = defaultFile;
        long availableSize = StorageUtils.getFileAvailableBytes(rootPaths, destPathRoot);
        if (availableSize > size) {
            return destPathRoot;
        }
        destPathRoot = StorageUtils.createOtherSdCardFile(rootPaths, destPathRoot.getAbsolutePath());

        if (destPathRoot != null) {
            availableSize = StorageUtils.getFileAvailableBytes(rootPaths, destPathRoot);
            if (availableSize > size) {
                return destPathRoot;
            }
        }
        return null;
    }
}
