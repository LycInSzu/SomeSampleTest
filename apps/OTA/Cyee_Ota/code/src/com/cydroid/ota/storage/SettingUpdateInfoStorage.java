package com.cydroid.ota.storage;

import android.content.Context;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.bean.SettingUpdateInfo;

/**
 * Created by liuyanfeng on 15-4-20.
 */
public class SettingUpdateInfoStorage extends StorageAdapter implements IStorageUpdateInfo{
    protected SettingUpdateInfoStorage(Context context) {
        super(context.getSharedPreferences(getName(), Context.MODE_PRIVATE));
    }

    private static String getName() {
        return "com_gionee_settingupdateinfo_setting";
    }

    @Override
    public void storage(IUpdateInfo info) {
        if (info == null) {
            return;
        }
        putString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_RELEASE_NOTE,
                info.getReleaseNote());
        putString(Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_MD5,
                info.getMd5());
        putString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_DOWNLOAD_URL,
                info.getDownloadUrl());
        putString(Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_VERSION,
                info.getVersion());
        putLong(Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_FILE_SIZE,
                info.getFileSize());
        putBoolean(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_IS_PRE_RELEASE,
                info.isPreRelease());
        putString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_VERSION_RELEASE_DATE,
                info.getVersionReleaseDate());
        putString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_RELEASE_NOTE_URL,
                info.getReleaseNoteUrl());
        putString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_INTERNAL_VER,
                info.getInternalVer());
        putInt(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_DOWNLOAD_PEOPLE_NUM,
                info.getDownloadedPeopleNum());
        putBoolean(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_EXT_PKG,
                info.isExtPkg());
    }

    @Override
    public IUpdateInfo getUpdateInfo() {
        String releaseNote = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_RELEASE_NOTE, "");
        String md5 = getString(Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_MD5, "");
        String downloadUrl = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_DOWNLOAD_URL, "");
        String version = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_VERSION, "");
        long fileSize = getLong(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_FILE_SIZE, 0);
        boolean isPreRelease = getBoolean(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_IS_PRE_RELEASE,
                false);
        String versionReleaseDate = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_VERSION_RELEASE_DATE,
                "");
        String releaseNoteUrl = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_RELEASE_NOTE_URL,
                "");
        String internalVer = getString(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_INTERNAL_VER, "");
        int downloadPeopleNum = getInt(Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_DOWNLOAD_PEOPLE_NUM, 0);
        boolean extPkg = getBoolean(
                Key.SettingUpdateInfo.KEY_SETTING_UPDATE_INFO_EXT_PKG, false);
        SettingUpdateInfo updateInfo = new SettingUpdateInfo();
        updateInfo.setReleaseNote(releaseNote);
        updateInfo.setMd5(md5);
        updateInfo.setDownloadUrl(downloadUrl);
        updateInfo.setVersion(version);
        updateInfo.setFileSize(fileSize);
        updateInfo.setPreRelease(isPreRelease);
        updateInfo.setVersionReleaseDate(versionReleaseDate);
        updateInfo.setReleaseNoteUrl(releaseNoteUrl);
        updateInfo.setInternalVer(internalVer);
        updateInfo.setDownloadedPeopleNum(downloadPeopleNum);
        updateInfo.setExtPkg(extPkg);
        return updateInfo;
    }
}
