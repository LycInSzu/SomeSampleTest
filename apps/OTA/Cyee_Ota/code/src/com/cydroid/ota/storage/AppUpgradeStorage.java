package com.cydroid.ota.storage;

import android.content.Context;

/**
 * Created by borney on 4/27/15.
 */
public class AppUpgradeStorage extends StorageAdapter {
    protected AppUpgradeStorage(Context context) {
        super(context.getSharedPreferences(getName(), Context.MODE_PRIVATE));
    }

    private static String getName() {
        return "com_gionee_appupgrade_setting";
    }
}
