package com.cydroid.ota.logic;

import android.content.Context;
import android.provider.Settings;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.logic.sync.Job;
import com.cydroid.ota.logic.sync.LocalVersionCheckJob;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.utils.Util;

/**
 * Created by liuyanfeng on 15-7-22.
 */
public class LocalVersionCheckImpl extends AbstractExecutor
        implements ILocalVersionCheck {
    private static final String TAG = "LocalVersionCheckImpl";
    private Context mContext;
    private Job mLocalVersionCheckJob;

    protected LocalVersionCheckImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void checkLocalVersion() {
        if (mLocalVersionCheckJob == null) {
            mLocalVersionCheckJob = new LocalVersionCheckJob(mContext,
                    mLocalVersionCheckCallback);
        } else if (mLocalVersionCheckJob.isRunning()) {
            Log.d(TAG, "local version check job is running!");
            return;
        }
        syncexe(mLocalVersionCheckJob);
    }

    private IExcutorCallback mLocalVersionCheckCallback = new IExcutorCallback() {
        @Override
        public void onResult(Object... objects) {
            Util.notifyLaucherShowBadge(mContext,0);

            IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                    mContext).settingStorage();
            IUpdateInfo updateInfo = SettingUpdateDataInvoker
                    .getInstance(mContext).
                            settingUpdateInfoStorage().getUpdateInfo();
            settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_CURRENT_VERSION,
                    SystemPropertiesUtils.getInternalVersion());
            settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASENOTE,
                    updateInfo.getReleaseNote());
            settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASEURL,
                    updateInfo.getReleaseNoteUrl());
            QuestionnaireManager.getInstance(mContext).questionnaireNotification().questionnaireAlarm(
                    true);
        }

        @Override
        public void onError(int errorCode) {

        }
    };

    @Override
    protected void handler() {

    }
}
