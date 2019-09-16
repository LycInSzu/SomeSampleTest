package com.cydroid.ota.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
//Chenyee <CY_Bug> <xuyongji> <20180110> modify for CSW1702A-2433 begin
import android.os.Build;
import android.os.UserHandle;
//Chenyee <CY_Bug> <xuyongji> <20180110> modify for CSW1702A-2433 end

import com.cydroid.ota.Log;

import com.cydroid.ota.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 4/22/15.
 */
public class StorageUtils {
    private static final String TAG = "StorageUtils";
    public static final String SDCARD = "/sdcard";
    public static final String SDCARD2 = "/data/media/0";
    public static final String EMULATED_INTERNAL_STORAGE_PATH = "/data/media/0";
    private static String sSDCARDPATH = null;
    private static String sInternalStorage = null;
    private static String sOTGStorage = null;

    public static String getSDCARDPATH() {
    	String path = sSDCARDPATH;
    	if (sSDCARDPATH == null) {
    	    return null;
    	}
    	if (!path.endsWith("/")) {
    		path = path + "/";
    	}
        return path;
    }
    
    public static String getOTGStorage() {
        String path = sOTGStorage;
        if (sOTGStorage == null) {
            return null;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    public static boolean checkExternalStorageMounted() {
        boolean flag = Environment.MEDIA_MOUNTED
                .equals(getExternalStorageState());
        Log.d(TAG, "checkExternalStorageMounted()  " + flag);

        return flag;
    }

    public static boolean checkInternalStorageMounted(Context context) {
        boolean flag = Environment.MEDIA_MOUNTED
                .equals(getInternalStorageState(context));
        Log.d(TAG, "checkInternalStorageMounted()  " + flag);

        return flag;
    }

    public static String getExternalStorageState() {
        return Environment.getExternalStorageState();
    }

    public static String getInternalStorageState(Context context) {

        return getStorageVolumeState(context, getInternalStoragePath(context));
    }

    public static String getInternalStoragePath(Context context) {
    	String path = Environment.getExternalStorageDirectory().getPath();
    	if (!path.endsWith("/")) {
    		path = path + "/";
    	}
        return path;
    }

    public static String getStorageVolumeState(Context context,
                                               String mountPoint) {
        String state = Environment.MEDIA_REMOVED;
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager != null) {
            try {
                state = storageManager.getVolumeState(mountPoint);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }

        Log.d(TAG, "getStorageVolumeState() mountPoint = " + mountPoint
                + " , state = " + state);
        return state;
    }

    public static List<String> getStorageVolumesPath(Context context) {
        List<String> storages = new ArrayList<String>();

        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return storages;
        }

        String[] paths = storageManager.getVolumePaths();

        if (paths == null) {
            return storages;
        }
        int length = paths.length;

        for (int i = 0; i < length; i++) {
            if (!storageManager.getVolumeState(paths[i]).equals("not_present")) {
                storages.add(paths[i]);
            }
        }

        Log.d(TAG,
                "getStorageVolumesPath() storages = " + storages.toString());
        return storages;
    }

    public static List<String> getAllMountedStorageVolumesPath(Context context) {
        List<String> mountedPaths = new ArrayList<String>();
        List<String> paths = getStorageVolumesPath(context);

        for (String path : paths) {
            if (Environment.MEDIA_MOUNTED.equals(getStorageVolumeState(context,
                    path))) {
                mountedPaths.add(path);
            }
        }
        return mountedPaths;
    }

    public static String getStroageOfFile(Context context, File file) {
        String filePath = file.getPath();

        List<String> storagePaths = getAllMountedStorageVolumesPath(context);
        for (String storage : storagePaths) {
            if (filePath.startsWith(storage)) {
                return storage;
            }
        }
        String storage = filePath.substring(0,
                filePath.lastIndexOf(File.separator));
        Log.d(TAG, "getStroageOfFile() storage = " + storage);
        return storage;
    }

    public static boolean isFileInInternalStoarge(Context context, File file) {
        Log.d(TAG, "isFileInInternalStoarge() file path = " + file.getPath());

        return isInternalStorage(context, file.getPath().toString());
    }
    
    public static boolean isOTGStorage(Context context, String storagePath) {
        initStoragepath(context);
        if (sOTGStorage == null) {
            return false;
        }
        Log.d(TAG, "isOTGStorage() sOTGStorage = " + sOTGStorage );
        return storagePath.startsWith(sOTGStorage) ? true : false;
    }

    public static boolean isInternalStorage(Context context, String storagePath) {
        initStoragepath(context);
        Log.d(TAG, "isInternalStorage() storagePath = " + storagePath + "   sSDCARDPATH = " + sSDCARDPATH + "sInternalStorage  = " + sInternalStorage);
        if (sInternalStorage == null) {
            return false;
        }
        Log.d(TAG, "isInternalStorage() sInternalStorage = " + sInternalStorage );
        return storagePath.startsWith(sInternalStorage) ? true : false;
    }

    private static void initStoragepath(Context context) {
        ArrayList<StorageVolume> storageVolumes = new ArrayList<StorageVolume>();
        List<String> storages = new ArrayList<String>();
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);

		//Chenyee <CY_Bug> <xuyongji> <20180110> modify for CSW1702A-2433 begin
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            int userId = UserHandle.myUserId();
            List<VolumeInfo> volumeInfos = storageManager.getVolumes();

            for (VolumeInfo volumeInfo:volumeInfos) {
                if (!volumeInfo.isMountedWritable()) {
                    continue;
                }
                if(volumeInfo.getType() == VolumeInfo.TYPE_EMULATED) {
                    sInternalStorage = volumeInfo.getPathForUser(userId).toString();
                } else if(volumeInfo.getType() == VolumeInfo.TYPE_PUBLIC) {
                    if (volumeInfo.getDisk().isSd()) {
                        sSDCARDPATH = volumeInfo.getPath().toString();
                    } else if (volumeInfo.getDisk().isUsb()){
                        sOTGStorage = volumeInfo.getPath().toString();
                    }
                }
            }
            Log.d(TAG, "externalStorageId=" + sSDCARDPATH
                    + " internalStoraedID=" + sInternalStorage
                    + " otgStrorgeID=" + sOTGStorage);
            return;
        }
		//Chenyee <CY_Bug> <xuyongji> <20180110> modify for CSW1702A-2433 end

        String[] pathList = storageManager.getVolumePaths();
        StorageVolume[] volumes = storageManager.getVolumeList();
        int len = pathList.length;
        for (int i = 0; i < len; i++) {
            if (!storageManager.getVolumeState(pathList[i]).equals(
                    "not_present")) {
                storages.add(pathList[i]);
                storageVolumes.add(volumes[i]);
            }
        }
        Log.d(TAG, "storages=" + storages.toString()
                + " storageVolumes=" + storageVolumes.toString());
        int externalStorageId = -1;
        int internalStoraedID = -1;
        int otgStrorgeID  = -1;
        Log.d(TAG, " storageVolumes.size()=" +  storageVolumes.size());
        for (int i = 0; i < storageVolumes.size(); i++) {
//            if (isExternalStorage(context, storageVolumes.get(i))) {
//                externalStorageId = i;
//            }
//            if (isInternalStorage(context, storageVolumes.get(i))) {
//                internalStoraedID = i;
//            }
            if (!storageVolumes.get(i).isEmulated() && storageVolumes.get(i).allowMassStorage()) {
            	externalStorageId = i;
           }
            if (storageVolumes.get(i).isEmulated()) {
            	 internalStoraedID = i;
            }
            if (storageVolumes.get(i).isEmulated()==false && storageVolumes.get(i).allowMassStorage()==false) {
                otgStrorgeID = i;
            }

        }
        Log.d(TAG, "externalStorageId=" + externalStorageId
                + " internalStoraedID=" + internalStoraedID
                + " otgStrorgeID=" + otgStrorgeID);
        if (externalStorageId >= 0) {
            sSDCARDPATH = storages.get(externalStorageId);
        }
        if (internalStoraedID >= 0) {
            sInternalStorage = storages.get(internalStoraedID);
        }
        if (otgStrorgeID >= 0) {
            sOTGStorage = storages.get(otgStrorgeID);
        }        
    }

    public static boolean isExSdcardInserted(Context context) {
        initStoragepath(context);
        if (null == sSDCARDPATH) {
            return false;
        }

        String state = getStorageVolumeState(context, sSDCARDPATH);
        if (state.equals("mounted")) {
            Log.d(TAG, "sd card inserted");
            return true;
        } else {
            Log.d(TAG, "sd card uninserted");
            return false;
        }
    }

    private static boolean isExternalStorage(Context context, StorageVolume mVolume) {
        Log.d(TAG, "isExternalStorage description:" + mVolume.getDescription(context));
        String descrip = mVolume.getDescription(context).replaceAll(" ", "");
        String sdcard = "";
        try {
            Field field = com.android.internal.R.string.class.getField("storage_sd_card");
            int resId = field.getInt("storage_sd_card");
            sdcard = context.getResources().getString(resId);
            sdcard = sdcard.replaceAll(" ", "");
            Log.v(TAG, "isExternalStorage sdcard =" + sdcard);
        } catch (Exception ex) {
            sdcard = context.getString(R.string.gn_su_sd_card_title);
            Log.i(TAG, " --- ex=" + ex);
        }
        if (descrip.contains(sdcard)) {
            Log.d(TAG, "isExternalStorage true");
            return true;
        } else {
            return false;
        }
    }

    private static boolean isInternalStorage(Context context, StorageVolume mVolume) {
        String descrip = mVolume.getDescription(context);
        Log.d(TAG, "isInternalStorage descrip:" + descrip);
        String interstorage = "";
        try {
            Field field = com.android.internal.R.string.class.getField("storage_internal");
            int resId = field.getInt("storage_internal");
            interstorage = context.getResources().getString(resId);
            interstorage = interstorage.replaceAll(" ", "");
            Log.v(TAG, "isInternalStorage  interstorage =" + interstorage);
        } catch (Exception ex) {
        	interstorage = context.getString(R.string.gn_su_internal_storage);
            Log.e(TAG, " --- ex=" + ex);
        }
        if (descrip.equals(interstorage)) {
            Log.d(TAG, "isInternalStorage  true");
            return true;
        } else {
            return false;
        }
    }
}
