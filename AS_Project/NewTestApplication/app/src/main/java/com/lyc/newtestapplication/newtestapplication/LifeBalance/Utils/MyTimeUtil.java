package com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils;


import android.icu.text.SimpleDateFormat;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.MyCountdownTimeFormat;


import java.text.ParseException;
import java.util.Date;

public class MyTimeUtil {

    //将用户输入的天时分秒转换为毫秒数
    public static long convertUserDateToMilliSeconds(int days, int hours, int munites, int seconds) {

        return (long)days * 1000 * 60 * 60 * 24 + (long)hours * 1000 * 60 * 60 + (long)munites * 1000 * 60 + (long)seconds * 1000;
    }


    public static String convertToDate(long millisecondes) {
        Date date = new Date();
        date.setTime(millisecondes);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return simpleDateFormat.format(date);
    }

    //将日期转换为毫秒数
    public static long convertToMilliSeconds(String date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        long time = 0;
        try {
            time = simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;

    }

    //将毫秒数转换为时间
    public static MyCountdownTimeFormat convertToTime(long milliSeconds) {
        int days = (int) (milliSeconds / (1000 * 60 * 60 * 24));
        long left1 = milliSeconds % (1000 * 60 * 60 * 24);
        int hours = (int) (left1 / (1000 * 60 * 60));
        long left2 = left1 % (1000 * 60 * 60);
        int minutes = (int) (left2 / (1000 * 60));
        long left3 = left2 % (1000 * 60);
        int seconds = (int) (left3 / 1000);

        MyCountdownTimeFormat countdownTimeFormat = new MyCountdownTimeFormat();
        countdownTimeFormat.setDays(days);
        countdownTimeFormat.setHours(hours);
        countdownTimeFormat.setMinutes(minutes);
        countdownTimeFormat.setSeconds(seconds);

        return countdownTimeFormat;

    }
}
