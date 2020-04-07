package com.cydroid.note.encrypt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

/**
 * Created by spc on 16-5-4.
 */
public class EncryptBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String SYSTEM_HOME_KEY_DOWN = "homekey";
    private static final String SYSTEM_HOME_KEY_LONG_PRESS = "recentapps";
    private static final String EXTRA_REASON = "reason";

    private EncryptMainActivity mActivity;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_SCREEN_OFF.equals(action)) {
            removeCurrentTask(context);
        } else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(EXTRA_REASON);
            if (TextUtils.equals(reason, SYSTEM_HOME_KEY_DOWN)
                    || TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG_PRESS)) {
                removeCurrentTask(context);
            }
        }
    }

    public void setActivity(EncryptMainActivity activity) {
        mActivity = activity;
    }

    private void removeCurrentTask(Context context) {
        if (mActivity == null) {
            EncryptUtil.removeCurrentTask(context);
            return;
        }
        mActivity.finishAndRemoveTask();
        mActivity = null;
    }


}
