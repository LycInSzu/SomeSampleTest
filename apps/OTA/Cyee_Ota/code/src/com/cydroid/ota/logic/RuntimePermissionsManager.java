/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.ota.logic;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
//import android.util.Log;

public class RuntimePermissionsManager {
    private static final String TAG = "RuntimePermissionsManager";
    private static final List<String> REQUIRED_PERMISSIONS = new ArrayList<>();
    static {
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean isBuildSysNeedRequiredPermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNeedRequiredPermissions(Activity activity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (activity.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    //Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
	public static boolean checkRequiredPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
	//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end

    public static void requestRequiredPermissions(Activity activity, int resultCode) {
        List<String> requiredPermissions = getNoGrantedPermissions(activity);
        if (requiredPermissions.isEmpty()) {
            return;
        }
        requestPermissions(activity, requiredPermissions, resultCode);
    }

    private static List<String> getNoGrantedPermissions(Activity activity) {
        List<String> noGrantedPermissions = new ArrayList<String>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (activity.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                noGrantedPermissions.add(permission);
            }
        }
        return noGrantedPermissions;
    }

    private static void requestPermissions(Activity activity, List<String> requiredPermissions, int resultCode) {
        String[] permissions = requiredPermissions.toArray(new String[requiredPermissions.size()]);
        activity.requestPermissions(permissions, resultCode);
    }

    public static boolean hasDeniedPermissions(String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                /*Log.d(TAG, "hasDeniedPermissions permission:" + permissions[i] + ", grantResult:"
                        + grantResults[i]);*/
                return true;
            }
        }
        return false;
    }
}
