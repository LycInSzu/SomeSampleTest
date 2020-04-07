package com.android.systemui.screenshot;

import android.os.Handler;
import android.content.IntentFilter;

public class NewLongScreenUtil {
    private static final Handler mHandler = new Handler();
    public static volatile boolean mLSRunning = false;
    private static final Runnable TIME_OUT_TASK = () -> mLSRunning = false;

    public static void postDelayStopLimit(){
        mHandler.postDelayed(() -> {
            removeCallbacks();
            postDelayed(TIME_OUT_TASK, 60000);
        }, 2000);
    }

    public static void removeCallbacks(){
        if(mHandler.hasCallbacks(TIME_OUT_TASK)){
            mHandler.removeCallbacks(TIME_OUT_TASK);
        }
    }

    static void postDelayed(Runnable r,long delay){
        mHandler.postDelayed(r, delay);
    }
}
