package com.cydroid.ota.utils;

import com.cydroid.ota.logic.config.EnvConfig;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by liuyanfeng on 15-3-23.
 */
public class RandomTimeUtil {
    private static final String TAG = "RandomTimeUtil";
    private static final int BEGIN_HOUR = 8;
    private static final int END_HOUR = 24;
    private static final long HOUR_MILLISECOND = 60 * 60 * 1000;
    private static final long MINUTE_MILLISECOND = 60 * 1000;
    private static final long SECOND_MILLISECOND = 1000;
    private static final long ZERO_TO_EIGHT = 8 * HOUR_MILLISECOND;
    private static final long ONE_DAY = 24 * HOUR_MILLISECOND;

    /**
     * @return delayDay days later, Random time from beginHour to endHour
     */
    public static long getRandomTime(boolean isFirstNotify) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isInNotifyTime = currentHour > BEGIN_HOUR && currentHour < END_HOUR;
        if (isFirstNotify && isInNotifyTime) {
            return System.currentTimeMillis() + SECOND_MILLISECOND;
        }

        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentSecond = calendar.get(Calendar.SECOND);
        int currentMilliSecond = calendar.get(Calendar.MILLISECOND);
        long curTimeMilliSeconds = getCurrentTimeMilliSecond(
                currentHour, currentMinute, currentSecond,
                currentMilliSecond);

        if (isFirstNotify) {
            return System.currentTimeMillis() + ZERO_TO_EIGHT - curTimeMilliSeconds + getNotifyRandomTime();
        }

        if (isInNotifyTime) {
            return System.currentTimeMillis() + ZERO_TO_EIGHT - curTimeMilliSeconds + ONE_DAY * 2
                    + getNotifyRandomTime();
        }

        return System.currentTimeMillis() + ONE_DAY - curTimeMilliSeconds + ZERO_TO_EIGHT
                + ONE_DAY * 2 + getNotifyRandomTime();

    }

    public static long getRandomTime() {
        if (EnvConfig.isInquire()) {
            return System.currentTimeMillis() + ONE_DAY * 2;
        }
        return System.currentTimeMillis() + ONE_DAY * 2 + getNotifyWholeRandomTime();
    }

    private static long getCurrentTimeMilliSecond(int hour, int minute,
            int second, int millisecond) {
        return hour * HOUR_MILLISECOND + minute * MINUTE_MILLISECOND
                + second * SECOND_MILLISECOND + millisecond;
    }

    private static long getNotifyWholeRandomTime() {
        return randomHourMillisecond(23) + randomMinuteMillisecond(59)
                + randomSecondMillisecond(59);
    }

    //random from 8 to 24
    private static long getNotifyRandomTime(){
        return randomHourMillisecond(16) + randomMinuteMillisecond(59)
                + randomSecondMillisecond(59);
    }

    private static long randomHourMillisecond(int hour) {
        return HOUR_MILLISECOND *getRandomInt(hour);
    }

    private static long randomMinuteMillisecond(int minute) {
        return MINUTE_MILLISECOND * getRandomInt(minute);
    }

    private static long randomSecondMillisecond(int second) {
        return SECOND_MILLISECOND * getRandomInt(second);
    }

    /**
     * @param num
     * @return a number from 0 to (num - 1)
     */
    private static int getRandomInt(int num) {
        Random random = new Random();
        return random.nextInt(num);
    }
}
