package com.cydroid.note.common;

import android.os.SystemProperties;

public class Log {

    public static final String TAG = "Log";
    public static final String PREFIX = "CyeeNote";
    public static final boolean DEBUG = SystemProperties.get("vendor.MB.running", "0").equals("1");

    public static void i(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.i(PREFIX, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.i(PREFIX, tag + ":" + msg, tr);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.d(PREFIX, tag + ":" + msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.d(PREFIX, tag + ":" + msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            android.util.Log.d(PREFIX, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.e(PREFIX, tag + ":" + msg, tr);
        }
    }

    public static void e(String tag, String error) {
        android.util.Log.e(PREFIX, tag + ":" + error);
    }

    public static void e(String error) {
        android.util.Log.e(PREFIX, error);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.w(PREFIX, tag + ":" + msg, tr);
        }
    }

    public static void w(String tag, String error) {
        android.util.Log.w(PREFIX, tag + ":" + error);
    }

    public static void w(String error) {
        android.util.Log.w(PREFIX, error);
    }

     public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            android.util.Log.v(PREFIX, tag + ":" + msg, tr);
        }
    }

    public static void v(String tag, String error) {
        android.util.Log.v(PREFIX, tag + ":" + error);
    }

    public static void v(String error) {
        android.util.Log.v(PREFIX, error);
    }
}
