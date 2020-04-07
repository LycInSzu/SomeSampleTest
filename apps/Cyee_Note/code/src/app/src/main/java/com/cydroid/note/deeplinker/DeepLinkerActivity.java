package com.cydroid.note.deeplinker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.cydroid.note.common.Log;
import com.cydroid.note.app.NoteAppImpl;
import java.util.List;

/**
 * in android:
 * adb shell am start -a "android.intent.action.VIEW" -d "scheme://host/decode_intent_data"
 * <p/>
 * in html:
 * <a href="scheme://host/decode_intent_data">start</a>
 */
public class DeepLinkerActivity extends Activity {
    public static final String TAG = "Deeplinker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processIntent(getIntent(), this);
        finish();
    }

    private static void sendStatistic(String goAppPackageName, Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(goAppPackageName, PackageManager.GET_META_DATA);
                String appName = info.loadLabel(packageManager).toString();
                Log.d(TAG, "appName = " + appName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "get package error" + e);
            }
        }

    }

    private static void processIntent(Intent intent, Context context) {
        if (intent == null) {
            Log.d(TAG, "intent is null");
            return;
        }

        final Uri uri = intent.getData();
        if (uri == null) {
            Log.d(TAG, "uri is null");
            return;
        }

        List<String> path = uri.getPathSegments();

        if (path == null || path.size() == 0) {
            Log.d(TAG, "path size is 0");
            return;
        }

        if (path.size() >= 1) {
            final String intentStr = path.get(0);
            Log.d(TAG, "intentStr " + intentStr);
            launchActivity(intentStr, context);
        }
    }

    private static void launchActivity(String intentStr, Context context) {

        intentStr = DeepLinkerHelper.decodeIntentString(intentStr);
        final String packageName = DeepLinkerHelper.getPackageNameFromIntentString(intentStr);
        Log.d(TAG, "packageName " + packageName);
        Log.d(TAG, "restore after: " + intentStr);
        try {
            Intent intent = Intent.parseUri(intentStr, Intent.URI_INTENT_SCHEME);
            Log.d(TAG, "intent " + intent);
            if (intent != null) {
                context.startActivity(intent);
            }
            sendStatistic(packageName, context);
        } catch (Exception e) {
            Log.d(TAG, "launchActivity error " + e);
            if (packageName != null) {
                launchAppEnterActivity(packageName, context);
            }
        }
    }

    private static void launchAppEnterActivity(String packageName, Context context) {
        Intent goIntent = getAppEnterActivityIntent(packageName, context);
        if (goIntent != null) {
            try {
                Log.d(TAG, "goIntent " + goIntent.toUri(Intent.URI_INTENT_SCHEME));
                context.startActivity(goIntent);
                sendStatistic(packageName, context);
            } catch (Exception e) {
                Log.d(TAG, "start error " + e);
            }
        } else {
            Log.d(TAG, "package name " + packageName + " not installed!");
        }
    }

    private static Intent getAppEnterActivityIntent(String packageName, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
            Log.d(TAG, "error: " + e);
            return null;
        }
    }
}
