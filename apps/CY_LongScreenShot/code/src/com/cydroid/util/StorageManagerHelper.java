package com.cydroid.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

public class StorageManagerHelper {
    public static final String TAG = Tools.TAG;
    private static StorageManager mSm;
    static final long STORAGE_SPACE_LIMIT = 10 * 1024 * 1024;//10M

    private static StorageManager getStorageManager(Context context) {
        if (mSm == null) {
            mSm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        return mSm;
    }

    public static String getSaveImagePath(Context mContext) {
        return getAvailableStoragePath(mContext, STORAGE_SPACE_LIMIT);
    }

    public static String getAvailableStoragePath(final Context context, long fileSize) {
        String mSaveImageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (getVolumeSpace(getStorageManager(context), mSaveImageDir) > fileSize) {
            return mSaveImageDir;
        }
        return null;
    }

    private static long getVolumeSpace(StorageManager stManager, String volumePath) {
        String state = stManager.getVolumeState(volumePath);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                StatFs stat = new StatFs(volumePath);
                return stat.getAvailableBlocks() * (long) stat.getBlockSize();
            } catch (Exception e) {
                Log.e(TAG, "Fail to access external storage", e);
            }
        }
        return -1L;
    }

    public static boolean isAvailableForSpecifyVolumePath(Context context, String volumePath) {
        if (!TextUtils.isEmpty(volumePath)) {
            return getVolumeSpace(getStorageManager(context), volumePath) > STORAGE_SPACE_LIMIT;
        }
        return false;
    }
}
