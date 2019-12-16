package com.cydroid.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import cyee.provider.CyeeSettings;

/**
 * 进入长截屏，隐藏悬浮触点，退出恢复，解决长截屏和悬浮触点的各种冲突问题
 */
public class FloatingTouchHelper {
    private static final String TAG = "FloatingTouchHelper";
    private static final String ACTION_START_FLOATINGTOUCH = "com.cydroid.floatingtouch.action.START_SERVICE";
    private static final String ACTION_STOP_FLOATINGTOUCH = "com.cydroid.floatingtouch.action.STOP_SERVICE";
    private static final ComponentName FLOATINGTOUCH_RECEIVER_COMPONENT = ComponentName
            .unflattenFromString("com.cydroid.floatingtouch/.service.TouchReceiver");
    private Context mContext;
    private boolean floatingTouchPreOpened = false;

    public FloatingTouchHelper(Context appContext) {
        mContext = appContext;
    }

    public void saveFloatingTouchState() {
        if (isFloatingTouchOpen()) {
            Log.d(TAG, "floatingTouch is opened, close it");
            floatingTouchPreOpened = true;
            setFloatingTouch(false);
        }
    }

    public void restoreFloatingTouchIfNeeded() {
        if (floatingTouchPreOpened) {
            Log.d(TAG, "restore floatingTouch");
            setFloatingTouch(true);
            floatingTouchPreOpened = false;
        }
    }

    private void setFloatingTouch(boolean configVal) {
        if (isFloatingTouchOpen() == configVal) {
            return;
        }
        if (configVal) {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 1);
            Intent intent = new Intent(ACTION_START_FLOATINGTOUCH);
            intent.setComponent(FLOATINGTOUCH_RECEIVER_COMPONENT);
            mContext.sendBroadcast(intent);
            Log.d(TAG, "send intent START FloatTouch SERVICE");
        } else {
            Intent intent = new Intent(ACTION_STOP_FLOATINGTOUCH);
            intent.setComponent(FLOATINGTOUCH_RECEIVER_COMPONENT);
            mContext.sendBroadcast(intent);
            Log.d(TAG, "send intent STOP_SERVICE");
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0);
        }
    }

    private boolean isFloatingTouchOpen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0) == 1;
    }
}
