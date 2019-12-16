package com.cydroid.util;

import android.os.SystemProperties;
import android.text.TextUtils;

/**
 * @author xionghg
 * @created 18-1-25.
 */

public class Log {
    private static final String APP_TAG = "LongScreenShot";

    private static final int VERBOSE = android.util.Log.VERBOSE;
    private static final int DEBUG   = android.util.Log.DEBUG;
    private static final int INFO    = android.util.Log.INFO;
    private static final int WARN    = android.util.Log.WARN;
    private static final int ERROR   = android.util.Log.ERROR;
    //chenyee zhaocaili 20180511 add for CSW1707A-975 begin
    public static boolean LOG_ENABLE = false;
    //chenyee zhaocaili 20180511 add for CSW1707A-975 end

    public static final int LOG_LEVEL = DEBUG;

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

    //chenyee zhaocaili 20180511 add for CSW1707A-975 begin
    public static boolean setLogEnableOrNot(){
        boolean enable = SystemProperties.get("debug.MB.running", "0").equals("1");
        if((LOG_ENABLE && !enable) || (!LOG_ENABLE && enable)){
            LOG_ENABLE = enable;
        }
        return LOG_ENABLE;
    }
    //chenyee zhaocaili 20180511 add for CSW1707A-975 end
}
