package com.cydroid.note.common;

import com.cydroid.note.common.Log;

import com.cydroid.note.common.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
//GIONEE wanghaiyan 2016-12-01 modify for 37025 begin
import android.content.Context;
import android.os.storage.StorageVolume;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import java.util.ArrayList;
import java.util.List;

import com.cydroid.note.common.Log;
import android.os.StatFs;
//GIONEE wanghaiyan 2016-12-01 modify for 37025 end
import android.os.SystemProperties;
import android.os.storage.VolumeInfo;

public class FileUtils {
    private static String TAG = "FileUtils";
    //GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
    public static final boolean gnEncryptionSpaceSupport = SystemProperties.get("ro.encryptionspace.enabled").equals("true");
    //GIONEE wanghaiyan 2016 -12-13 modify for 45337 end
    //Gionee wanghaiyan 2017-3-31 modify for 96979 begin
    public static final boolean mODMProject = SystemProperties.get("ro.gn.oversea.odm", "no").equals("yes");
    //Gionee wanghaiyan 2017-3-31 modify for 96979 end
    public static boolean copyFile(String srcPath, String toPath) {
        Log.d(TAG, "begin to copy file");
        FileInputStream fIs = null;
        FileOutputStream fOs = null;
        FileChannel fCi = null;
        FileChannel fCo = null;
        try {
            fIs = new FileInputStream(new File(srcPath));
            fOs = new FileOutputStream(new File(toPath));
            fCi = fIs.getChannel();
            fCo = fOs.getChannel();
            fCi.transferTo(0, fCi.size(), fCo);
            return true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "image file not found:" + e);
            return false;
        } catch (IOException e) {
            Log.d(TAG, "copy image throw IOException:" + e);
            return false;
        } catch (Exception e) {
            Log.d(TAG, "copy image throw other Exception:" + e);
            return false;
        } finally {
            NoteUtils.closeSilently(fIs);
            NoteUtils.closeSilently(fOs);
            NoteUtils.closeSilently(fCi);
            NoteUtils.closeSilently(fCo);
        }
    }


    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.d(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    //unit:byte
    public static long getFileTotalSize(File file) {

        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }

        long total = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                total += getFileTotalSize(f);
            }
        }
        return total;
    }

    // copy a file from srcFile to destFile, return true if succeed, return
    // false if fail
    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                NoteUtils.closeSilently(in);
            }
        } catch (IOException e) {
            Log.d(TAG, "copyFile fail: " + e);
            result = false;
        }
        return result;
    }

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();//NOSONAR
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                NoteUtils.flushSilently(out);
                sync(out);
                NoteUtils.closeSilently(out);
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "copy to file fail: " + e);
            return false;
        }
    }

    /**
     * Perform an fsync on the given FileOutputStream.  The stream at this
     * point must be flushed but not yet closed.
     */
    public static boolean sync(FileOutputStream stream) {
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "sync fail: " + e);
        }
        return false;
    }
    //GIONEE wanghaiyan 2016-12-01 modify for 37025 begin
    public static final int ZERO_STORE = 0;
    public static final int ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE = 1005;
    public static final int MIN_AVAILABLE_STORE = 3;
    public static final int SUCCESS_SDCARD_STATE = 1007;
    public static final int ERROR_SDCARD_MIN_AVAILABLE_STORE = 1006;
    public static String PATH_PHONE = null;
    public static String PATH_SDCARD = null;
    public static int checkSDCardState(Context context ) {
        if (!isSDCardInserted(context)) {
            // sdcard not exists or unavailable
            Log.i(TAG, "sdcard not exists or unavailable!");
            return ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE;
        }
        File sdCardPath = new File(getSdcardRealPath(context));
        if ((FileUtils.getAvailableStore(sdCardPath.getPath()) / 1024 / 1024) < MIN_AVAILABLE_STORE) {
            Log.i(TAG, "FileUtils------sd card min available store < " + MIN_AVAILABLE_STORE + "M!");
            return ERROR_SDCARD_MIN_AVAILABLE_STORE;
        }
        Log.i(TAG, "Sdcard available");
        return SUCCESS_SDCARD_STATE;
    }

    public static boolean isSDCardInserted(final Context context) {
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolume = storageManager.getVolumeList();
        //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 begin
        List<VolumeInfo> vols = storageManager.getVolumes();
        //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 end
        for (int i = 0; i < storageVolume.length; i++) {
            if (Environment.MEDIA_MOUNTED.equals(storageManager.getVolumeState(storageVolume[i].getPath()))){
                //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 begin
                if(isExternalStorage(vols.get(i),storageVolume[i])){
                    //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 end
                    Log.v(TAG, "SD card is inserted");
                    if (PATH_SDCARD == null){
                        PATH_SDCARD = storageVolume[i].getPath();
                    }
                    return true;
                }
            }
        }
        Log.v(TAG, "SD card is not inserted");
        return false;
    }

    private static void initStoragepath(Context context) {
        ArrayList<StorageVolume> storageVolumes = new ArrayList<StorageVolume>();
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);

        StorageVolume[] volumes = storageManager.getVolumeList();
        //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 begin
        List<VolumeInfo> vols = storageManager.getVolumes();
        //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 end
        for (int i = 0; i < volumes.length; i++) {
            if (!storageManager.getVolumeState(volumes[i].getPath()).equals(
                    "not_present")) {
                storageVolumes.add(volumes[i]);
            }
        }
        Log.d(TAG, " storageVolumes.size() = " + storageVolumes.size());
        for (int i = 0; i < storageVolumes.size(); i++){
            if (isInternalStorage(storageVolumes.get(i))){
                PATH_PHONE = storageVolumes.get(i).getPath();
            }
            //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 begin
            //if (isExternalStorage(storageVolumes.get(i))){
            if (isExternalStorage(vols.get(i),storageVolumes.get(i))){
                //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 end
                PATH_SDCARD= storageVolumes.get(i).getPath();
            }
        }
        Log.d(TAG, " PATH_PHONE = " +  PATH_PHONE + " PATH_SDCARD = " +  PATH_SDCARD);
    }

    private static boolean isInternalStorage(StorageVolume volume) {
        if(volume.isEmulated() && !volume.isRemovable() && !volume.allowMassStorage()){
            return true;
        }
        return false;
    }

    //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 begin
    private static boolean isExternalStorage(VolumeInfo vol,StorageVolume storageVolume) {
        boolean isExternalStorage = false;
        String diskId = vol.getDiskId();
        Log.d(TAG, "External diskId: " + diskId);
        //boolean sdType = vol.getType() == VolumeInfo.TYPE_EMULATED;
        //Log.d(TAG, "sdType = " + sdType);

        if (diskId != null) {
            // for SD card, the disk id same as disk:179:x
            String[] idSplit = diskId.split(":");
            if (idSplit != null && idSplit.length == 2) {
                if (idSplit[1].startsWith("179,")) {
                    Log.d(TAG, "this is a SD card");
                    isExternalStorage = true;
                }
            }
        }
        return isExternalStorage;
    }
    //Chenyee wanghaiyan 2018-9-19 modify for CSW1705P-222 end

    private static boolean isExternalStorage(StorageVolume volume) {
        if(volume.isRemovable()){
            if(!volume.isEmulated() && !volume.allowMassStorage()){
                return false;
            }
            return true;
        }
        return false;
    }

    public static String getSdcardRealPath(Context context) {
        if (PATH_SDCARD == null){
            initStoragepath(context);
        }
        return PATH_SDCARD;
    }

    public static String getPhoneStoragetRealPath(Context context) {
        if (PATH_PHONE == null){
            initStoragepath(context);
        }
        return PATH_PHONE;
    }

    public static long getAvailableStore(String filePath) {
        File file = new File(filePath);
        if(!file.exists()){
            return 0;
        }
        // 取得sdcard文件路径
        StatFs statFs = new StatFs(filePath);

        // 获取block的SIZE
        long blocSize = statFs.getBlockSize();

        // 可使用的Block的数量
        long availaBlock = statFs.getAvailableBlocks();

        long availableSpare = availaBlock * blocSize;

        return availableSpare;
    }

    public static File CheckNoteMediaDir(Context context){
        File noteMediaDir = new File("");
        int sdCardState = checkSDCardState(context);
        if(sdCardState == ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE){
            noteMediaDir = new File(Environment.getExternalStorageDirectory(), "/Note/sound");
        }else if(sdCardState == ERROR_SDCARD_MIN_AVAILABLE_STORE){
            noteMediaDir = new File(getPhoneStoragetRealPath(context), "/Note/sound");
        }else if(sdCardState == SUCCESS_SDCARD_STATE){
            noteMediaDir = new File(getSdcardRealPath(context), "/Note/sound");
        }
        return noteMediaDir;
    }
    //GIONEE wanghaiyan 2016-12-01 modify for 37025 end
    //GIONEE wanghaiyan 2016-12-06 modify for 39890 and 40287 begin
    public static String getSaveImagePath(Context context){
        String saveImagePath =null;
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolume = storageManager.getVolumeList();
        int sdCardState = checkSDCardState(context);
        if(sdCardState == ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE){
            saveImagePath = Environment.getExternalStorageDirectory().getPath();
        }else if(sdCardState == ERROR_SDCARD_MIN_AVAILABLE_STORE){
            saveImagePath = getPhoneStoragetRealPath(context);
        }else if(sdCardState == SUCCESS_SDCARD_STATE){
            saveImagePath = getSdcardRealPath(context);
        }
        return saveImagePath;
    }
    //GIONEE wanghaiyan 2016-12-06 modify for 39890 and 40287 end
}
