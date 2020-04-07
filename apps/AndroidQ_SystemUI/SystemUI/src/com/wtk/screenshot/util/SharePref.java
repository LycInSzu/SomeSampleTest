package com.wtk.screenshot.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePref {
    /* Common */
    // Default
    public static final String TAG = ShotUtil.TAG;

    // Util
    private Context mContext;
    private SharedPreferences sharedPreferences;

    // Flag

    public synchronized static SharePref getInstance(Context context,
                                                     String shareTitle) {
        if (context == null || shareTitle == null) {
            return null;
        }
        return new SharePref(context, shareTitle);
    }

    private SharePref(Context context, String title) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(title, 0);
    }

    public void setString(String tag, String value) {
        sharedPreferences.edit().putString(tag, value).commit();
    }

    public String getString(String tag) {
        return sharedPreferences.getString(tag, null);
    }

    public String getString(String tag, String defaultValue) {
        return sharedPreferences.getString(tag, defaultValue);
    }

    public void setBoolean(String tag, boolean value) {
        sharedPreferences.edit().putBoolean(tag, value).commit();
    }

    public boolean getBoolean(String tag) {
        return sharedPreferences.getBoolean(tag, false);
    }

    public boolean getBoolean(String tag, boolean defaultValue) {
        return sharedPreferences.getBoolean(tag, defaultValue);
    }

    public void setInt(String tag, int value) {
        sharedPreferences.edit().putInt(tag, value).commit();
    }

    public int getInt(String tag) {
        return sharedPreferences.getInt(tag, -1);
    }

    public int getInt(String tag, int defaultValue) {
        return sharedPreferences.getInt(tag, defaultValue);
    }
}
