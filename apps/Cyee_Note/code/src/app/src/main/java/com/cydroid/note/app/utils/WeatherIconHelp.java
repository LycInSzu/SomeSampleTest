package com.cydroid.note.app.utils;

import android.text.format.Time;

import com.cydroid.note.R;


/**
 * Created by spc on 16-8-8.
 */
public class WeatherIconHelp {

    private static class StateInt {
        public static final int BAOXUE = 1;
        public static final int BINGBAO = 2;
        public static final int DABAOYU = 3;
        public static final int DAXUE = 4;
        public static final int DAYU = 5;
        public static final int DONGYU = 6;
        public static final int DUOYUN = 7;
        public static final int FUCHEN = 8;
        public static final int LEIZHENYU = 9;
        public static final int MAI = 10;
        public static final int NONGWU = 11;
        public static final int QIANGSHACHENBAO = 12;
        public static final int QINGWU = 13;
        public static final int QIN = 14;
        public static final int SHACHENBAO = 15;
        public static final int SHUANGDONG = 16;
        public static final int TEDABAOYU = 17;
        public static final int WU = 18;
        public static final int XIAOXUE = 19;
        public static final int XIAOYU = 20;
        public static final int YAN = 21;
        public static final int YANGSHA = 22;
        public static final int YIN = 23;
        public static final int YUJIAXUE = 24;
        public static final int ZHENYU = 25;
        public static final int ZHONGXUE = 26;
        public static final int ZHONGYU = 27;
        public static final int BAOYU = 28;
        public static final int ZHENXUE = 29;
        public static final int OTHER = 0;
    }

    private enum TimeSection {
        DAY, EVENING
    }

    public static TimeSection getTimeSection() {
        Time time = new Time();
        time.setToNow();
        if (time.hour >= 6 && time.hour < 19) {
            return TimeSection.DAY;
        } else {
            return TimeSection.EVENING;
        }
    }

    public static int getWeatherHelper(int sort, TimeSection slot) {
        switch (sort) {
            case StateInt.OTHER:
                break;
            case StateInt.BAOXUE:
                return R.drawable.widget41_icon_baoxue_day;
            case StateInt.BINGBAO:
                return R.drawable.widget41_icon_bingbao_day;
            case StateInt.BAOYU:
                return R.drawable.widget41_icon_dabaoyu_day;
            case StateInt.DABAOYU:
                return R.drawable.widget41_icon_dabaoyu_day;
            case StateInt.DAXUE:
                return R.drawable.widget41_icon_daxue_day;
            case StateInt.DAYU:
                return R.drawable.widget41_icon_dayu_day;
            case StateInt.DONGYU:
                return R.drawable.widget41_icon_dongyu_day;
            case StateInt.DUOYUN:
                if (slot == TimeSection.DAY) {
                    return R.drawable.widget41_icon_cloud_day;
                } else if (slot == TimeSection.EVENING) {
                    return R.drawable.widget41_icon_cloud_evening;
                } else {
                    return R.drawable.widget41_icon_cloud_night;
                }
            case StateInt.FUCHEN:
                return R.drawable.widget41_icon_sandstorm_day;
            case StateInt.LEIZHENYU:
                return R.drawable.widget41_icon_leizhenyu_day;
            case StateInt.MAI:
            case StateInt.NONGWU:
                return R.drawable.widget41_icon_fog_day;
            case StateInt.QIANGSHACHENBAO:
                return R.drawable.widget41_icon_sandstorm_day;
            case StateInt.QINGWU:
                return R.drawable.widget41_icon_fog_day;
            case StateInt.QIN:
                if (slot == TimeSection.DAY) {
                    return R.drawable.widget41_icon_sun_day;
                } else if (slot == TimeSection.EVENING) {
                    return R.drawable.widget41_icon_sun_evening;
                } else {
                    return R.drawable.widget41_icon_sun_night;
                }
            case StateInt.SHACHENBAO:
                return R.drawable.widget41_icon_sandstorm_day;
            case StateInt.SHUANGDONG:
                return R.drawable.widget41_icon_shuangdong_day;
            case StateInt.TEDABAOYU:
                return R.drawable.widget41_icon_tedabaoyu_day;
            case StateInt.WU:
                return R.drawable.widget41_icon_fog_day;
            case StateInt.XIAOXUE:
                return R.drawable.widget41_icon_xiaoxue_day;
            case StateInt.XIAOYU:
                return R.drawable.widget41_icon_xiaoyu_day;
            case StateInt.YAN:
                return R.drawable.widget41_icon_sandstorm_day;
            case StateInt.YANGSHA:
                return R.drawable.widget41_icon_sandstorm_day;
            case StateInt.YIN:
                return R.drawable.widget41_icon_cloudy_day;
            case StateInt.YUJIAXUE:
                return R.drawable.widget41_icon_yujiaxue_day;
            case StateInt.ZHENYU:
                return R.drawable.widget41_icon_zhenyu_day;
            case StateInt.ZHONGXUE:
                return R.drawable.widget41_icon_zhongxue_day;
            case StateInt.ZHONGYU:
                return R.drawable.widget41_icon_zhongyu_day;
            case StateInt.ZHENXUE:
                return R.drawable.widget41_icon_zhongxue_day;
        }
        return R.drawable.widget41_icon_nodata;
    }
}
