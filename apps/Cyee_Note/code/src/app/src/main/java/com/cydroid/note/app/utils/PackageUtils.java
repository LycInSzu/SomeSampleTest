package com.cydroid.note.app.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by wuguangjie on 16-7-13.
 */
public class PackageUtils {

    public static Intent getAppLaunchIntent(Context context, String packageName) {
        Intent intent = null;
        try {
            intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
            //ignore
        }
        return intent;
    }

    /**
     * 判断应用程序是否安装
     *
     * @param context
     * @param packageName 应用程序包名
     * @return true表示已经安装
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
