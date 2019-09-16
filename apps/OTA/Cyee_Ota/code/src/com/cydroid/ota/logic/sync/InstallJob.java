package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.os.Message;

import com.cydroid.ota.Log;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.*;
import com.cydroid.ota.utils.Error;

import java.io.File;

/**
 * Created by borney on 4/23/15.
 */
public class InstallJob extends Job {
    private static final String TAG = "InstallJob";
    private Context mContext;
    private String mFileName;

    public InstallJob(Context context, String fileName, ISyncCallback callback) {
        super(callback);
        this.mContext = context;
        this.mFileName = fileName;
    }

    @Override
    public <T> T run() {
        File file = new File(mFileName);
        if (file.exists()) {
            String fileName = createFileNameInIntent(mContext, file);
            Log.d(TAG, "fileName = " + fileName + " Util.isMtkPlatform() = " + Util.isMtkPlatform());
            ISystemReboot reboot;
            if (Util.isMtkPlatform()) {
                reboot = new SystemReboot();
            } else {
                reboot = new QcomSystemReboot();
            }
            cacheInfoBeforeInstall(mFileName);
            reboot.reboot(mContext, fileName);
        } else {
            sendMessage(MSG.MSG_ERROR_INSTALL_FILE_NOT_EXIT);
        }
        return null;
    }

    private void cacheInfoBeforeInstall(String filename) {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
            mContext).settingStorage();
        String versionNumber = SystemPropertiesUtils.getInternalVersion();
        settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_LAST_VERSION, versionNumber);
        settingStorage.putBoolean(Key.Setting.KEY_FIRST_BOOT_COMPLEPED_AFRET_UPGRADE, true);
        settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, filename);
        Log.d(TAG, "cacheInfoBeforeInstall fileName = " + filename + "  versionNumber = " + versionNumber);
    }

    @Override
    void handleJobMessage(Message msg) {
        switch (msg.what) {
            case MSG.MSG_ERROR_INSTALL_FILE_NOT_EXIT:
                mJobCallback.onError(Error.ERROR_CODE_INSTALL_FILE_NOT_EXIT);
                break;
            default:
                break;
        }
    }

    private String createFileNameInIntent(Context context, File upgradeFile) {
        if (upgradeFile == null) {
            Log.e(TAG, "createFileNameInIntent() upgradeFile is null");
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getStorageNameForRecoverry(context, upgradeFile));
        stringBuffer.append(getFilePathWithoutStoragePath(context, upgradeFile));
        String fileName = stringBuffer.toString();
        Log.d(TAG, "getFileNameInIntent() fileName = " + fileName);
        return fileName;
    }

    public String getFilePathWithoutStoragePath(Context context, File upgradeFile) {
        String storage = StorageUtils.getStroageOfFile(context, upgradeFile);
        String filePath = upgradeFile.getPath();
        filePath = filePath.substring(storage.length());
        Log.d(TAG, "getFilePathWithoutStoragePath() filePath = " + filePath);
        return filePath;
    }

    private String getStorageNameForRecoverry(Context context, File file) {
        if (StorageUtils.isFileInInternalStoarge(context, file)) {
            boolean isEmulatedStorage = SystemPropertiesUtils.getImitateTCard();
            if (isEmulatedStorage) {
                return StorageUtils.EMULATED_INTERNAL_STORAGE_PATH;
            }
            return StorageUtils.SDCARD2;
        } else {
            return StorageUtils.SDCARD;
        }
    }

    private final class MSG extends Job.MSG {
        private static final int BASE_ERROR = BASE * 3;
        static final int MSG_ERROR_INSTALL_FILE_NOT_EXIT = BASE_ERROR + 1;
    }
}
