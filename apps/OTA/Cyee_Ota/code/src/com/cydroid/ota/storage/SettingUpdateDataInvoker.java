package com.cydroid.ota.storage;

import android.content.Context;

/**
 * Created by borney on 4/14/15.
 */
public class SettingUpdateDataInvoker implements IDataInvoker {
    private volatile static IDataInvoker sDataInvoker;
    private Context mContext;

    private IStorage mSettingStorage;
    private IStorage mPushStorage;
    private IStorage mWlanAutoStorage;
    private IStorageUpdateInfo mSettingUpdateInfoStorage;

    private SettingUpdateDataInvoker(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized IDataInvoker getInstance(Context context) {
        if (sDataInvoker == null) {
            sDataInvoker = new SettingUpdateDataInvoker(context);
        }
        return sDataInvoker;
    }

    @Override
    public IStorage settingStorage() {
        if (mSettingStorage == null) {
            mSettingStorage = new SettingStorage(mContext);
        }
        return mSettingStorage;
    }

    @Override
    public IStorage pushStorage() {
        if (mPushStorage == null) {
            mPushStorage = new PushStorage(mContext);
        }
        return mPushStorage;
    }

    @Override
    public IStorage wlanAutoStorage() {
        if (mWlanAutoStorage == null) {
            mWlanAutoStorage = new WlanAutoStorage(mContext);
        }
        return mWlanAutoStorage;
    }

    @Override
    public IStorageUpdateInfo settingUpdateInfoStorage() {
        if (mSettingUpdateInfoStorage == null) {
            mSettingUpdateInfoStorage = new SettingUpdateInfoStorage(mContext);
        }
        return mSettingUpdateInfoStorage;
    }
}
