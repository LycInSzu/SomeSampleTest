package com.cydroid.screenrecorder.utils;

import android.text.TextUtils;
import android.os.SystemProperties;

/**
 * @author xionghg
 * @created 18-1-25.
 */

public class Log {
    private static final String APP_TAG = "CyScreenRecord";

    //chenyee zhaocaili 20180511 modify for CSW1707A-974 begin
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG   = android.util.Log.DEBUG;
    public static final int INFO    = android.util.Log.INFO;
    public static final int WARN    = android.util.Log.WARN;
    public static final int ERROR   = android.util.Log.ERROR;
    public static boolean LOG_ENABLE = false;
    //chenyee zhaocaili 20180511 modify for CSW1707A-974 end

    public static final int LOG_LEVEL = VERBOSE;

    public static int v(String tag, String msg) {
        if (LOG_ENABLE && LOG_LEVEL <= VERBOSE && !TextUtils.isEmpty(msg)) {
            return android.util.Log.v(APP_TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (LOG_ENABLE && LOG_LEVEL <= VERBOSE && !TextUtils.isEmpty(msg)) {
            return android.util.Log.v(APP_TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    public static int d(String tag, String msg) {
        if (LOG_ENABLE && LOG_LEVEL <= DEBUG && !TextUtils.isEmpty(msg)) {
            return android.util.Log.d(APP_TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (LOG_ENABLE && LOG_LEVEL <= DEBUG && !TextUtils.isEmpty(msg)) {
            return android.util.Log.d(APP_TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    public static int i(String tag, String msg) {
        if (LOG_ENABLE && LOG_LEVEL <= INFO && !TextUtils.isEmpty(msg)) {
            return android.util.Log.i(APP_TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (LOG_ENABLE && LOG_LEVEL <= INFO && !TextUtils.isEmpty(msg)) {
            return android.util.Log.i(APP_TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    public static int w(String tag, String msg) {
        if (LOG_ENABLE && LOG_LEVEL <= WARN && !TextUtils.isEmpty(msg)) {
            return android.util.Log.w(APP_TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (LOG_ENABLE && LOG_LEVEL <= WARN && !TextUtils.isEmpty(msg)) {
            return android.util.Log.w(APP_TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    public static int e(String tag, String msg) {
        if (LOG_LEVEL <= ERROR && !TextUtils.isEmpty(msg)) {
            return android.util.Log.e(APP_TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (LOG_LEVEL <= ERROR && !TextUtils.isEmpty(msg)) {
            return android.util.Log.e(APP_TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    //chenyee zhaocaili 20180511 modify for CSW1707A-974 begin
    public static boolean isLoggable(String tag, int level){
        return android.util.Log.isLoggable(tag, level);
    }

    public static boolean setLogEnableOrNot(){
        LOG_ENABLE = SystemProperties.get("vendor.MB.running", "0").equals("1");
        return LOG_ENABLE;
    }
    //chenyee zhaocaili 20180511 modify for CSW1707A-974 end
}
