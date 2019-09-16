package com.cydroid.ota.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.cydroid.ota.utils.BatteryUtil;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.Constants;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra("status", 0);
        int level = intent.getIntExtra("level", 0);
        int scale = intent.getIntExtra("scale", 100);
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);


        Log.debug(TAG, "status = " + status + " level = " + level + " scale = " + scale + " chargePlug = " + chargePlug);

        int currentLevel = level * 100 / scale;
        BatteryUtil.setBatteryLevel(currentLevel);

        boolean isCharging = false;
        if ((chargePlug & BatteryManager.BATTERY_PLUGGED_ANY) != 0) {
            isCharging = true;
        }
        BatteryUtil.setCharging(isCharging);

        Log.debug(TAG, "isCharging = " + isCharging);
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
       // Gionee zhouhuiquan 2017-04-01 add for 101983 begin
        /*if (BatteryUtil.getBatteryLevel() < Constants.MINI_CHARGE) {   // 电量小于20%
            AutoUpgradeManager.getInstance(context).stopAutoUpgradeSystem();
        } else if (BatteryUtil.getBatteryLevel() < Constants.LOWER_CHARGE) { //电量大于20%,小于40%
            if (isCharging){                                                  //电量大于20%,小于40%，充电
                SystemUpdateFactory.autoUpgrade(context).autoUpgradeSystem();
            }else{                                                            //电量大于20%,小于40%，不充电
                AutoUpgradeManager.getInstance(context).stopAutoUpgradeSystem();
            }
        } else {                                                            // 电量大于40%
           SystemUpdateFactory.autoUpgrade(context).autoUpgradeSystem();
        }*/
        // Gionee zhouhuiquan 2017-03-20 add for 101983 end
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end

    }
}
