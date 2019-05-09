package com.pri.factorytest.FingerPrint.huiding;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by tangjie on 09/05/18.
 */

public class Util {

    private static final String TAG = "Util";

    public static Bitmap bitMapScale(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }

    public static Bitmap convertToBitmap(byte[] result, int width, int height) {

        Bitmap bitmap = null;
        int length = width * height;
        int[] display = new int[length];
        int pixel = 0;
        for (int i = 0; i < length; i++) {
            pixel = result[i] & 0xFF;
            display[i] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
        }

        bitmap = Bitmap.createBitmap(display, width, height, Bitmap.Config.ARGB_8888);


        return bitMapScale(bitmap,2.0f);

    }

    public static void setAccesibility(Context context , boolean enable){

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        Log.d(TAG,"enabledServicesSetting = " + enabledServicesSetting);
        ComponentName selfComponentName = new ComponentName(context.getPackageName(),"com.goodix.fingerprint.setting.util.KeyCatchService");
        if(enable){
            String flattenToString = selfComponentName.flattenToString();
            if (enabledServicesSetting == null || !enabledServicesSetting.contains(flattenToString)) {
                enabledServicesSetting += flattenToString;
            }
            Settings.Secure.putString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,enabledServicesSetting);
        } else {
            Settings.Secure.putString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,"");
        }
    }

    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
