
package com.cydroid.screenrecorder.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MTool provid all tool method 
 * 
 * @author fuwenzhi
 *
 */
public class MTool {

    /**
     * convert dip value to px value
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    /**
     * convert px value to dip value
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    /**
     * set touches options value
     * @param context
     * @param value  
     * @return
     */
    public static boolean setTouchesOptionsValue(Context context,boolean value) {
        return Settings.System.putInt(context.getContentResolver(),Settings.System.SHOW_TOUCHES, value?1:0);
    }
    
    
    /**
     * get TouchesOpetions value
     * @param context
     * @return true , user already open touches option.
     */
    public static boolean getTouchesOptionsValue(Context context) {
        return Settings.System.getInt(context.getContentResolver(),Settings.System.SHOW_TOUCHES, 0)==1?true:false;
    }
    
    public static String formatTime(long timeMillis){
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd_HHmmss"); 
        return formatter.format(new Date(timeMillis)); 
    }
}
