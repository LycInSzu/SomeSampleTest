//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
package com.cydroid.note.common;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class StorageManagerHelper {
    public static final String TAG = "StorageManagerHelper";
    private static StorageManager mSm;

    public static StorageVolume getExternalStorageVolume(final Context context) {
        if (mSm == null) {
            mSm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        List<StorageVolume> storageVolume = mSm.getStorageVolumes();
        if (storageVolume != null) {
            for (int i = 0; i < storageVolume.size(); i++) {
                Log.d(TAG, "isEmulated." + storageVolume.get(i).isEmulated() + ",   isRemovable = " +storageVolume.get(i).isRemovable());
                if (storageVolume.get(i).isRemovable()) {
                    Log.d(TAG, "isSDCardInserted() SD card is inserted.");
                    return storageVolume.get(i);
                }
            }
        }
        return null;
    }

    public static String getSDPath(Context mContext) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz ;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
