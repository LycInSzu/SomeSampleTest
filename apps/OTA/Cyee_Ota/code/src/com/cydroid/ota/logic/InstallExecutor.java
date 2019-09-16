package com.cydroid.ota.logic;

import android.content.Context;
import android.text.TextUtils;
import com.cydroid.ota.logic.sync.ISyncCallback;
import com.cydroid.ota.logic.sync.InstallJob;
import com.cydroid.ota.logic.sync.Job;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Error;
import com.cydroid.ota.Log;

/**
 * Created by borney on 4/23/15.
 */
public class InstallExecutor extends AbstractExecutor implements IInstallExecutor {
    private static final String TAG = "InstallExecutor";
    private Job mJob;
    private Context mContext;
    private IExcutorCallback mExcutorCallback;

    protected InstallExecutor(Context context, IExcutorCallback callback) {
        this.mContext = context;
        this.mExcutorCallback = callback;
    }

    @Override
    public void install() {
        if (mJob == null) {
            IStorage settingStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
            String installFile = settingStorage.getString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
            if (TextUtils.isEmpty(installFile)) {
                mExcutorCallback.onError(Error.ERROR_CODE_INSTALL_FILE_NOT_EXIT);
                return;
            }
            if (mExcutorCallback != null) {
                mExcutorCallback.onResult();
            }
            mJob = new InstallJob(mContext, installFile, mCallback);
        } else if (mJob.isRunning()) {
            Log.e(TAG, "install job is running!!!");
            return;
        }
        syncexe(mJob);
    }

    @Override
    protected void handler() {

    }

    private ISyncCallback mCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {

        }

        @Override
        public void onError(int errorCode) {
            mExcutorCallback.onError(errorCode);
        }
    };
}
