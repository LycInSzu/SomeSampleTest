package com.cydroid.ota.storage;

import android.content.Context;

/**
 * Created by borney on 4/14/15.
 */
public class SettingStorage extends StorageAdapter {

    protected SettingStorage(Context context) {
        super(context.getSharedPreferences(getName(), Context.MODE_PRIVATE));
    }

    private static String getName() {
        return "com_gionee_settingupdate_setting";
    }
}