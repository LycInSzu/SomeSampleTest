package com.cydroid.note.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.cydroid.note.common.Log;

/**
 * Created by wuguangjie on 16-3-30.
 */
public abstract class AbsCompetitiveAppCollector {

    private static final boolean DEBUG = false;
    private static final String TAG = "cpt_app_colletor";

    public static final class CompetitiveAppInfo {
        private String name;
        private String packageName;

        private CompetitiveAppInfo() {}

        public static CompetitiveAppInfo create(String name, String packageName) {
            CompetitiveAppInfo info = new CompetitiveAppInfo();
            info.name = name;
            info.packageName = packageName;
            return info;
        }

        @Override
        public String toString() {
            return "CompetitiveAppInfo [name=" + name + ", packageName=" + packageName + "]";
        }

    }

    private static volatile boolean sChecking = false;

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(packageName, PackageManager.GET_META_DATA) != null;
        } catch (Exception e) {
            Log.d(TAG, "isPackageInstalled error " + e.getMessage());
            return false;
        }
    }

    private static SharedPreferences getSaveSharedPreferences(Context context) {
        return context.getSharedPreferences("cpt_app_collect", Context.MODE_PRIVATE);
    }

    private static boolean isCollected(Context context, String packageName) {
        try {
            SharedPreferences sp = getSaveSharedPreferences(context);
            return sp.getBoolean(packageName, false);
        } catch (Exception e) {
            Log.d(TAG, "io error " + e.getMessage());
            return false;
        }
    }

    private void collect(Context context, CompetitiveAppInfo info) {
        try {
            sendEvent2Cloud(info.name);
            SharedPreferences sp = getSaveSharedPreferences(context);
            sp.edit().putBoolean(info.packageName, true).commit();
        } catch (Exception e) {
            Log.d(TAG, "collect error " + e.getMessage());
        }
    }


    private void checkHelper(Context context) {

        CompetitiveAppInfo[] infos = createCompetitiveAppInfos();

        if (infos == null) {
            throw new NullPointerException("CompetitiveAppInfo is null");
        }

        if (DEBUG) {
            Log.d(TAG, "infos size " + infos.length);
        }

        for (CompetitiveAppInfo info : infos) {
            if (DEBUG) {
                Log.d(TAG, "info " + info);
            }
            if (isPackageInstalled(context, info.packageName)) {
                if (DEBUG) {
                    Log.d(TAG, "package " + info.packageName + " installed!");
                }
                if (isCollected(context, info.packageName)) {
                    if (DEBUG) {
                        Log.d(TAG, "package " + info.packageName + " collected!");
                    }
                } else {
                    collect(context, info);
                }
            }
        }
    }

    public final void checkAsync(Context context) {

        if (context == null) {
            throw new NullPointerException("context is null");
        }

        if (sChecking) {
            if (DEBUG) {
                Log.d(TAG, "checking...");
            }
            return;
        }

        sChecking = true;//NOSONAR

        final Context temp = context.getApplicationContext() != null ? context.getApplicationContext() : context;

        new Thread() {
            public void run() {
                checkHelper(temp);
                sChecking = false;//NOSONAR
            };
        }.start();
    }

    protected abstract CompetitiveAppInfo[] createCompetitiveAppInfos();

    protected abstract void sendEvent2Cloud(String event);
}
