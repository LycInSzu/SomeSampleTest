package com.android.launcher3.theme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.cydroid.launcher3.theme.CondorThemeManager;
//import com.android.launcher3.Launcher;

import com.android.launcher3.LauncherAppState;


public class ThemeChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ThemeChangeReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "----------ThemeChangeReceiver   -----------themeReceiver------------onReceive=" + action + ", " + intent.getCategories());
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(context.getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(homeIntent);
        CondorThemeManager.getInstance().clearShadowImage();
        LauncherAppState.getInstance(context.getApplicationContext()).getModel().forceReload();
        LauncherAppState.getInstance(context.getApplicationContext()).clearIcon();


    }


}
