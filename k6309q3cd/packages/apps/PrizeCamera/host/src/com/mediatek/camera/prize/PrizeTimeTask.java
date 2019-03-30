package com.mediatek.camera.prize;

import java.lang.reflect.Field;
import java.util.TimerTask;

/**
 * add for xiaoping-20190309 
 */
public  abstract class PrizeTimeTask extends TimerTask {
    /**
     * Reset period of timetask
     * @param period
     */
    public void setPeriod(long period) {
        setDeclaredField(TimerTask.class, this, "period", period);
    }

    static boolean setDeclaredField(Class<?> clazz, Object obj,
                                    String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
