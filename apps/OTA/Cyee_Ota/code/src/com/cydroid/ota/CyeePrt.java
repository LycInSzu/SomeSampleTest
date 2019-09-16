package com.cydroid.ota;

import com.cydroid.ota.logic.config.EnvConfig;

/**
 * Created by kangjj on 15-9-15.
 */
public class CyeePrt {
    private static final String TAG = "CyeePrt";

	//Chenyee <CY_Bug> <20171213> modify for SW17W16A-2562 change gn to cy begin
    static {
        Log.d("CyeePrt", "System.loadLibrary(\"cy_prt_jni\")");
        try {
            System.loadLibrary("cy_prt_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e("CyeePrt", "libcy_prt_jni does not found.");
        }
    }
    //Chenyee <CY_Bug> <20171213> modify for SW17W16A-2562 change gn to cy end

    public static native int nativeCheckIfRoot();

    public final static boolean isSystemRoot() {
        Log.d(TAG, "isTestRoot = " + EnvConfig.isTestRoot());
        if (EnvConfig.isTestRoot()) {
            if (EnvConfig.isTestRootTrue()) {
                Log.d(TAG, "isTestRootTrue = " + EnvConfig.isTestRootTrue());
                return true;
            }
            if (EnvConfig.isTestRootFalse()) {
                Log.d(TAG, "isTestRootFalse = " + EnvConfig.isTestRootFalse());
                return false;
            }
        }

        /*try {
            int tmp = CyeePrt.nativeCheckIfRoot();
            Log.d(TAG, "tmp = " + tmp);
            return tmp == 0 ? false : true;
        } catch (Throwable e) {
            e.printStackTrace();
        }*/
        return false;
    }

}
