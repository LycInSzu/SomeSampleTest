package com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils;

import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.MyCountdownTimeFormat;

public class MyTimeUtil {

    //将天时分秒转换为毫秒数
    public static long convertToMilliSeconds(int days,int hours,int munites,int seconds){

        return days*1000*60*60*24+hours*1000*60*60+munites*1000*60+seconds*1000;
    }

    //将毫秒数转换为时间
    public static MyCountdownTimeFormat convertToTime(long milliSeconds){
        int days=(int)(milliSeconds/(1000*60*60*24));
        long left1=milliSeconds%(1000*60*60*24);
        int hours= (int)(left1/(1000*60*60));
        long left2= left1%(1000*60*60);
        int minutes= (int)(left2/(1000*60));
        long left3= left2%(1000*60);
        int seconds = (int)(left3/1000);

        MyCountdownTimeFormat countdownTimeFormat=new MyCountdownTimeFormat();
        countdownTimeFormat.setDays(days);
        countdownTimeFormat.setHours(hours);
        countdownTimeFormat.setMinutes(minutes);
        countdownTimeFormat.setSeconds(seconds);

        return countdownTimeFormat;

    }
}
