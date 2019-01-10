package com.lyc.newtestapplication.newtestapplication.Battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

/**
 * 监听获取手机系统剩余电量
 * Created by Lx on 2016/9/17.
 */
public class BatteryReceiver extends BroadcastReceiver {
//    private TextView pow;
//
//    public BatteryReceiver(TextView pow) {
//        this.pow = pow;
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int current = intent.getExtras().getInt("level");// 获得当前电量百分百
        int total = intent.getExtras().getInt("scale");// 获得总电量（100）

        Log.i("BatteryReceiver","Battery info  total battery is "+total+"   and current is  "+current);
//        int percent = current * 100 / total;
//        pow.setText(percent + "%");
    }


    //java反射方式获取电量mAh
    public String getCapaCity(Context context) {
        Object mPowerProfile;
        double mBatteryCapacity = 0;
        String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            mBatteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(mBatteryCapacity + " mAh");
    }
}