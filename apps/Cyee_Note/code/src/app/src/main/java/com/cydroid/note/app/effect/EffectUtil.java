package com.cydroid.note.app.effect;

import com.cydroid.note.common.Log;

import java.util.Calendar;

public class EffectUtil {
    private static final String TAG = "EffectUtil";

    public static final int EFFECT_NORMAL = 0;
    public static final int EFFECT_OLD = 1;
    public static final int EFFECT_VERY_OLD = 2;
    public static final int EFFECT_VERY_VERY_OLD = 3;

    private static final long ONE_DAY_TIME_MILLIS = 24 * 60 * 60 * 1000;
    private static final long SERVEN_DAY_TIME_MILLIS = 7 * ONE_DAY_TIME_MILLIS;
    private static final long ONE_MONTH_TIME_MILLIS = 30 * ONE_DAY_TIME_MILLIS;

    private TimeSpan mNormalTimeSpan;
    private TimeSpan mOldTimeSpan;
    private TimeSpan mVeryOldTimeSpan;

    private static class TimeSpan {
        private long mStartTime;
        private long mEndTime;

        public TimeSpan(long startT, long endT) {
            mStartTime = startT;
            mEndTime = endT;
        }

        public boolean isContain(long time) {
            return time >= mStartTime && time <= mEndTime;
        }

        public boolean isLessThanStart(long time) {
            return time < mStartTime;
        }

        public boolean isMoreThanEnd(long time) {
            return time > mEndTime;
        }
    }

    public EffectUtil(long timeInMillis) {
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTimeInMillis(timeInMillis);
        printCalendar(curCalendar);
        int year = curCalendar.get(Calendar.YEAR);
        int month = curCalendar.get(Calendar.MONTH);
        int day = curCalendar.get(Calendar.DAY_OF_MONTH);
        Calendar curStartCalendar = Calendar.getInstance();
        curStartCalendar.set(year, month, day, 0, 0, 0);
        long curStartTimeMillis = curStartCalendar.getTimeInMillis();
        initTimeSpans(curStartTimeMillis);
    }

    private void initTimeSpans(long curStartTimeMillis) {
        long normalEndTimeMillis = curStartTimeMillis + ONE_DAY_TIME_MILLIS;
        long normalStartTimeMillis = normalEndTimeMillis - SERVEN_DAY_TIME_MILLIS;
        mNormalTimeSpan = new TimeSpan(normalStartTimeMillis, normalEndTimeMillis);

        long oldEndTimeMillis = normalStartTimeMillis;
        long oldStartTimeMillis = oldEndTimeMillis - (ONE_MONTH_TIME_MILLIS -
                SERVEN_DAY_TIME_MILLIS);
        mOldTimeSpan = new TimeSpan(oldStartTimeMillis, oldEndTimeMillis);

        long veryOldEndTimeMillis = oldStartTimeMillis;
        long veryOldStartTimeMillis = veryOldEndTimeMillis - ONE_MONTH_TIME_MILLIS;
        mVeryOldTimeSpan = new TimeSpan(veryOldStartTimeMillis, veryOldEndTimeMillis);
    }

    public int getEffect(long time) {

        if (mNormalTimeSpan.isMoreThanEnd(time) || mNormalTimeSpan.isContain(time)) {
            return EFFECT_NORMAL;
        }
        if (mOldTimeSpan.isContain(time)) {
            return EFFECT_OLD;
        }

        if (mVeryOldTimeSpan.isContain(time)) {
            return EFFECT_VERY_OLD;
        }
        return EFFECT_VERY_VERY_OLD;
    }

    private static void printCalendar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int mimute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        Log.d(TAG, "year = " + year + ",month = " + month + ",day = "
                + day + ",hour = " + hour + ",mimute = "
                + mimute + ",second = " + second
                + ",timeInMillis = " + calendar.getTimeInMillis());
    }

}
