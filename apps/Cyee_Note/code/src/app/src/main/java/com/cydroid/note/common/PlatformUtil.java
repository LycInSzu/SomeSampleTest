package com.cydroid.note.common;

import android.os.Build;
import com.cydroid.note.common.Log;

import java.lang.reflect.Method;

public class PlatformUtil {
    private static final String TAG = "PlatformUtil";

    public static boolean isGioneeDevice() {
        return true;
    }

    public static boolean isBusinessStyle() {
        if (!isGioneeDevice()) {
            return false;
        }
        try {
            Class type = Class.forName("cyee.widget.CyeeWidgetVersion");
            Enum o = (Enum) type.getDeclaredMethod("getVersionType").invoke(null);
            return "BUSINESS_VER".equalsIgnoreCase(o.name());
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return false;
    }

    public static boolean isSecurityOS() {
        String strEnabled = "";
        try {
            Class spclass = Class.forName("android.os.SystemProperties");
            Method getMethod = ReflectionUtils.findMethod(spclass, "get", String.class);
            strEnabled = (String) ReflectionUtils.invokeMethod(getMethod, null, "ro.encryptionspace.enabled");
        } catch (ClassNotFoundException e) {
            Log.w(TAG, e);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return "true".equals(strEnabled);
    }

    public static boolean authorizeToPermisson() {
        if (Build.VERSION.SDK_INT > 23) {
            return true;
        }
        try {
            return android.os.SystemProperties.get("ro.gn.sys_perm_alert.support", "no").equals("yes");
        } catch (Exception e) {
        }
        return false;
    }
}
