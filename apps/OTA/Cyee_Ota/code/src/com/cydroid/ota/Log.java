package com.cydroid.ota;

import android.os.SystemProperties;

/**
 * Created by borney on 14-10-20.
 */
public class Log {
    private static final String TAG = "SystemUpdateLog.";
    //private static final boolean DEBUG = EnvConfig.isDebug();
    private static boolean DEBUG = SystemProperties.get("debug.MB.running", "0").equals("1");

    public static void d(String tag, String msg) {
        if (!DEBUG){
            return;
        }
        android.util.Log.d(TAG + tag, msg);
    }

    public static void i(String tag, String msg) {
        if (!DEBUG){
            return;
        }
        android.util.Log.i(TAG + tag, msg);
    }

    public static void v(String tag, String msg) {
        if (!DEBUG){
            return ;
        }
        android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (!DEBUG){
            return;
        }
        android.util.Log.w(TAG + tag, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG + tag, msg);
    }

    public static void debug(String tag, String msg) {
        if (DEBUG) {
            d(tag, msg);
        }
    }

    public static String getFunctionName() {
        StringBuffer sb = new StringBuffer();
        sb.append("-> ");
        sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
        sb.append("()");
        sb.append("-> ");
        return sb.toString();
    }

}
