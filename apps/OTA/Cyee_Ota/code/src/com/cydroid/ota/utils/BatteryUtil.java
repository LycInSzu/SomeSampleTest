package com.cydroid.ota.utils;

public class BatteryUtil {
    private static int sBatteryLevel = 0;
    private static boolean sIsCharging = false;

    /**
     * get battery level
     * 
     * @return current battery level
     */
    public static int getBatteryLevel() {
        return sBatteryLevel;
    }

    /**
     * is battery charging
     * 
     * @return charging
     */
    public static boolean isCharging() {
        return sIsCharging;
    }

    public static void setBatteryLevel(int level) {
        sBatteryLevel = level;
    }
    
    /**
     * is battery charging
     * 
     * @return charging
     * @hide
     */
    public static void setCharging(boolean isCharging) {
        sIsCharging = isCharging;
    }
}