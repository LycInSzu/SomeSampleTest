package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.SystemPropertiesUtils;

/**
 * Created by liuyanfeng on 15-7-22.
 */
public class LocalVersionCheckJob extends Job {
    private static final String TAG = "LocalVersionCheckJob";
    private Context mContext;

    public LocalVersionCheckJob(Context context, ISyncCallback callback) {
        super(callback);
        mContext = context;
    }

    @Override
    public <T> T run() {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
            mContext).settingStorage();
        String cacheVersion = settingStorage.getString(
            Key.Setting.KEY_SETTING_UPDATE_CURRENT_VERSION, "");
        String curVersion = SystemPropertiesUtils.getInternalVersion();
        if (EnvConfig.isInquire()) {
            sendMessage(MSG.MSG_LOCAL_VERSION_CHANGED);
        }
        if (TextUtils.isEmpty(cacheVersion)) {
            settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_CURRENT_VERSION,
                curVersion);
            sendMessage(MSG.MSG_LOCAL_VERSION_NOT_CHANGE);
            return null;
        }
        Log.d(TAG, "cacheVersion:" + cacheVersion + "curVersion: " + curVersion);

        if (curVersion.equals(cacheVersion)) {
            sendMessage(MSG.MSG_LOCAL_VERSION_NOT_CHANGE);
            return null;
        }

        sendMessage(MSG.MSG_LOCAL_VERSION_CHANGED);
        return null;
    }

    @Override
    void handleJobMessage(Message msg) {
        switch (msg.what) {
            case MSG.MSG_LOCAL_VERSION_CHANGED:
                if (mJobCallback != null) {
                    mJobCallback.onResult();
                }
                break;
            case MSG.MSG_LOCAL_VERSION_NOT_CHANGE:
                if (mJobCallback != null) {
                    mJobCallback.onError(0);
                }
                break;
            default:
                break;
        }
    }

    private static final class MSG extends Job.MSG {
        private static final int BASE_MSG = BASE * 7;

        /*static {
            Log.d(TAG, TAG + " BASE_MSG = " + BASE_MSG);
        }*/

        static final int MSG_LOCAL_VERSION_CHANGED = BASE_MSG + 1;
        static final int MSG_LOCAL_VERSION_NOT_CHANGE = BASE_MSG + 2;
    }
}
