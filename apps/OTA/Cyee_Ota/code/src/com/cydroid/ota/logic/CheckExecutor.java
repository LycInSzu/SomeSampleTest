package com.cydroid.ota.logic;

import android.content.Context;
import com.cydroid.ota.Log;
import com.cydroid.ota.logic.bean.CheckInfo;
import com.cydroid.ota.logic.bean.CheckType;
import com.cydroid.ota.logic.sync.ISyncCallback;
import com.cydroid.ota.logic.sync.Job;
import com.cydroid.ota.logic.sync.SettingUpdateCheckJob;
import com.cydroid.ota.storage.IDataInvoker;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.*;

/**
 * Created by borney on 4/14/15.
 */
public class CheckExecutor extends AbstractExecutor implements ICheckExecutor {
    private static final String TAG = "CheckExecutor";
    private IExcutorCallback mExcutorCallback;
    private Context mContext;
    private CheckInfo mCheckInfo;
    private Job mCheckJob;

    CheckExecutor(Context context, IExcutorCallback callback) {
        this.mContext = context;
        this.mExcutorCallback = callback;
    }

    @Override
    public void check(CheckType checkType) {
        //setCheckTime
        IStorage settingsStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        settingsStorage.putLong(Key.Setting.KEY_SETTING_UPDATE_LAST_CHECK_TIME, System.currentTimeMillis());

        IContextState state = SystemUpdateManager.getInstance(mContext).getContextState();
        Log.d(TAG, "state=" + state);
        if (state.state() != State.INITIAL) {
            Log.e(TAG, "error state: " + state);
            return;
        }
        mExcutorCallback.onResult();
        boolean isRoot = false;
        if (!state.isRoot() && isSystemRoot()) {
            Log.d(TAG, "the system is root!");
            isRoot = true;
            mExcutorCallback.onResult(true);
        }
        mCheckInfo = createCheckInfo(state.isRoot() || isRoot, checkType);
        if (mCheckJob == null) {
            mCheckJob = new SettingUpdateCheckJob(mContext, mCheckInfo, mCheckJobCallback);
        } else if (mCheckJob.isRunning()) {
            Log.e(TAG, "check job is running!!!");
            return;
        }
        syncexe(mCheckJob);
    }

    @Override
    protected void handler() {
        mCheckJob = null;
    }

    private CheckInfo createCheckInfo(boolean isRoot, CheckType checkTyoe) {
        CheckInfo info = new CheckInfo();
        info.setRoot(isRoot);
        info.setSupportPatch(!isRoot);
        info.setConnectionType(NetworkUtils.getConnectionType(mContext));
        IDataInvoker dataInvoker = SettingUpdateDataInvoker.getInstance(mContext);
        info.setCheckType(checkTyoe);
        int pushID = dataInvoker.pushStorage().getInt(Key.Push.KEY_PUSH_RECEIVER_NOTIFIER_ID,
                Constants.GN_SU_ERROR_PUSH_ID);
        info.setPushId(pushID);
        info.setWapNetwork(NetworkUtils.isWapConnection(mContext));
        info.setImei(SystemPropertiesUtils.getImei(mContext));
        info.setData("");
        return info;
    }

    private ISyncCallback mCheckJobCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {
            if (mExcutorCallback != null) {
                mExcutorCallback.onResult(objects);
            }
        }

        @Override
        public void onError(int errorCode) {
            Log.d(TAG, "onError:" + errorCode);
            if (mExcutorCallback != null) {
                mExcutorCallback.onError(errorCode);
            }
        }
    };
}
