package com.cydroid.util;

import android.content.Context;

public class Tools {
    public static final String TAG = "OreoLongScreenShot";
    public static final boolean DEBUG_SAVE_TEMP_BITMAP = false;
    public static final boolean saveAllCachedBitmap = false;


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
