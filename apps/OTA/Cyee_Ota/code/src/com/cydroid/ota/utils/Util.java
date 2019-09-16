package com.cydroid.ota.utils;

import android.content.Context;
import android.provider.Settings;
import android.os.Build;
import android.os.SystemProperties;
import com.cydroid.ota.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liuyanfeng on 15-4-16.
 */
public class Util {
    private static final String TAG = "Util";
    public static final int ANDROID_M_INT = 23;

    public static String getUaString(String imei) {
        try {
            Class productConfigurationClass = Class.forName("com.cyee.utils.ProductConfiguration");
            Method method = productConfigurationClass.getMethod("getUAString", String.class);
            return (String) method.invoke(productConfigurationClass, imei);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        String brand = SystemProperties.get("ro.product.brand", "soda");
        String model = SystemProperties.get("ro.product.model", "Phone");
        String extModel = SystemProperties.get("ro.cy.extmodel", "Phone");
        String romVer = SystemProperties.get("ro.cy.rom.vernumber", Build.VERSION.RELEASE);
        String Ver = romVer.substring(romVer.indexOf("M") == -1 ? 0 : (romVer.indexOf("M") + 1));
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry().toLowerCase();
        String decodeImei = GNDecodeUtils.get(imei);
        String uaString = "Mozilla/5.0 (Linux; U; Android " + Build.VERSION.RELEASE + "; " + language + "-"
                + country + ";" + brand + "-" + model + "/" + extModel
                + " Build/IMM76D) AppleWebKit534.30(KHTML,like Gecko)Version/4.0 Mobile Safari/534.30 Id/"
                + decodeImei + " RV/" + Ver;
        Log.d("uaString", "uaString=" + uaString);
        return uaString;
    }

    public static String utcTimeToLocal(long utcTime, String timeFormat) throws NumberFormatException {
        Date date = new Date(utcTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
        String localTime = simpleDateFormat.format(date);
        return localTime;
    }

    public static String getProduct() {
        return android.os.Build.MODEL;
    }

    public static boolean isMtkPlatform() {
        String mtkRelease = SystemPropertiesUtils.getMtkRelease();
        return mtkRelease != null && !mtkRelease.equals("");
    }

    public static void notifyLaucherShowBadge(Context context, int flag) {
        try {
            if (Build.VERSION.SDK_INT < ANDROID_M_INT) {
                Settings.System.putInt(context.getContentResolver(), Constants.SYSTEM_UPDATE_LAUNCHER_UNREAD_KEY, flag);
            } else {
                cyee.provider.CyeeSettings.putInt(context.getContentResolver(), Constants.SYSTEM_UPDATE_LAUNCHER_UNREAD_KEY, flag);
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "can't put SYSTEM_UPDATE_LAUNCHER_UNREAD_KEY to setting:" + e.getMessage());
        }
    }
}
