package com.cydroid.ota.logic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.cydroid.ota.Log;
import com.cydroid.ota.logic.bean.CheckType;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.*;
import com.cydroid.ota.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.util.List;
/**
 * Created by liuyanfeng on 15-6-11.
 */
public class AutoUpgradeManager implements IAutoUpgradeSystem {
    private static final String TAG = "AutoUpgradeManager";
    private Context mContext;
    public static final int UPTIME_MILLIS = 1000 * 30;
    private static IAutoUpgradeSystem sIAutoUpgradeSystem = null;
    private static final int MSG_START_AUTO_UPGRADE_SYSTEM = 0x10001;
    private boolean isAlreadyStartAutoUpgrade = false;
    private IAutoUpgradeDispatcher mAutoUpgradeDispatcher;
    private boolean isRegisterBattery = false;
    private BatteryBroadcastReceiver mBatteryBroadcastReceiver;

    private AutoUpgradeManager(Context context) {
        mContext = context.getApplicationContext();
        mAutoUpgradeDispatcher = new AutoUpgradeDispatcher(mContext);
        mBatteryBroadcastReceiver = new BatteryBroadcastReceiver();
    }

    synchronized static IAutoUpgradeSystem getInstance(Context context) {
        if (sIAutoUpgradeSystem == null) {
            sIAutoUpgradeSystem = new AutoUpgradeManager(context);
        }
        return sIAutoUpgradeSystem;
    }

    @Override
    public void autoUpgradeSystem() {
        Log.debug(TAG, "isAlreadyStartAutoUpgrade = " + isAlreadyStartAutoUpgrade);
        if (isAlreadyStartAutoUpgrade || !isBackgroundProcess(mContext)) {
            return;
        }
        Log.d(TAG, "start auto upgrade system!");
        isAlreadyStartAutoUpgrade = true;
        mHandler.removeMessages(MSG_START_AUTO_UPGRADE_SYSTEM);
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
        mHandler.sendMessage(Message.obtain(mHandler,
                MSG_START_AUTO_UPGRADE_SYSTEM));
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
    }

    @Override
    public void stopAutoUpgradeSystem() {
        Log.d(TAG, "stopAutoUpgradeSystem()");
        if (!isAlreadyStartAutoUpgrade || !isBackgroundProcess(mContext)) {
            return;
        }
        isAlreadyStartAutoUpgrade = false;
        SystemUpdateManager.getInstance(mContext).unregisterObserver(mStateObserver);
        mHandler.removeMessages(MSG_START_AUTO_UPGRADE_SYSTEM);
        ISystemUpdate systemUpdate = SystemUpdateFactory.systemUpdate(mContext);
        IContextState state = systemUpdate.getContextState();
        if (state.state().value() < State.READY_TO_DOWNLOAD.value()) {
            systemUpdate.checkUpdate().cancel();
        } else if (state.state().value() < State.DOWNLOAD_VERIFY.value()) {
            systemUpdate.downUpdate(null).cancel();
        }
    }

    @Override
    public IAutoUpgradeDispatcher getAutoUpgradeDispatcher() {
        return mAutoUpgradeDispatcher;
    }

    @Override
    public void registerBatteryReceiver() {
        if (!isRegisterBattery) {
            mContext.registerReceiver(mBatteryBroadcastReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            isRegisterBattery = true;
        }
    }

    @Override
    public boolean isAutoUpgrade() {
        return isBackgroundProcess(mContext);
    }

    private boolean canStartAutoDownload() {
        if (!isBackgroundProcess(mContext)) {
            Log.e(TAG, "startAutoDownload() process is not background!");
            return false;
        }

        IStorage storage = SettingUpdateDataInvoker.getInstance(mContext)
                .wlanAutoStorage();
        if (!storage.getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH,
                mContext.getResources().getBoolean(
                        R.bool.auto_download_only_wlan))) {
            return false;
        }

        if (NetworkUtils.isMobileNetwork(mContext)) {
            return false;
        }

        if (BatteryUtil.getBatteryLevel() < Constants.MINI_CHARGE){  // 电量小于20%
            return false;
        } else if(BatteryUtil.getBatteryLevel() < Constants.LOWER_CHARGE) {  // 电量小于40%
            if (!BatteryUtil.isCharging()){
                Log.d(TAG, " isCharging() = " + BatteryUtil.isCharging());
                return false;
            }
        }


        IContextState contextState = SystemUpdateManager.getInstance(mContext).getContextState();
        if (contextState.state() != State.READY_TO_DOWNLOAD) {
            return false;
        }

        return true;
    }

    private boolean canStartAutoCheck() {
        Log.d(TAG, "canStartAutoCheck");
        if (!isBackgroundProcess(mContext)) {
            Log.d(TAG, "canStartAutoCheck() process is not background!");
            return false;
        }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
        /*IStorage wlanAutoStorage = SettingUpdateDataInvoker
                .getInstance(mContext).wlanAutoStorage();
        if (!wlanAutoStorage
                .getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_CHECK_SWITCH,
                        mContext.getResources().getBoolean(R.bool.auto_check_only_wlan))) {
            Log.d(TAG, "canStartAutoCheck wlanAutoStorage KEY_WLAN_AUTO_CHECK_SWITCH is false");
            return false;
        }*/

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            Log.d(TAG, "canStartAutoCheck wifi connection is false");
            return false;
        }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end

        if (BatteryUtil.getBatteryLevel() < Constants.MINI_CHARGE){  // 电量小于20%
            return false;
        } else if(BatteryUtil.getBatteryLevel() < Constants.LOWER_CHARGE) {  // 电量小于40%
            if (!BatteryUtil.isCharging()){
                Log.d(TAG, " isCharging() = " + BatteryUtil.isCharging());
                return false;
            }
        }

        IContextState contextState = SystemUpdateManager.getInstance(mContext)
                .getContextState();
        if (contextState.state() != State.INITIAL) {
            Log.d(TAG, "canStartAutoCheck contextState is not INITIAL");
            return false;
        }
        Log.d(TAG, "canStartAutoCheck return true");
        return true;
    }

     //    private boolean isBackgroundProcess(Context context) {
//        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
//        int lastPidFromUser = settingStorage.getInt(Key.Setting.KEY_SETTING_UPDATE_CURRENT_PID_FROM_USER, -1);
//        Log.d(TAG, "isBackgroundProcess  lastPidFromUser = " + lastPidFromUser);
//        Log.d(TAG, "isBackgroundProcess android.os.Process.myPid() = " + android.os.Process.myPid());
//        if (lastPidFromUser != android.os.Process.myPid()) {
//            return true;
//        }
//        return false;
//    }
	//Gionee zhouhuiquan 2017-03-14 add for 73281 begin
    /**
     * 程序是否在前台运行
     * @return
     */
    private boolean isBackgroundProcess(Context context) {
        // Returns a list of application processes that are running on the device
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();

        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null){
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Log.d(TAG,"程序处于前台");
                return false;
            }
        }
        //Log.d(TAG,"程序处于后台");
        return true;
    }
    //Gionee zhouhuiquan 2017-03-14 add for 73281 end

    private void startAutoUpgradeSystem() {
        Log.d(TAG, "startAutoUpgradeSystem");
        SystemUpdateManager.getInstance(mContext).registerObserver(mStateObserver);
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
        if (canStartAutoCheck()) {
            SystemUpdateManager.getInstance(mContext).checkUpdate().check(CheckType.CHECK_TYPE_AUTO);
        }
        isAlreadyStartAutoUpgrade = false;
        /*if (canStartAutoDownload()) {
            SystemUpdateManager.getInstance(mContext).downUpdate(null)
                    .start();
        }*/
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_START_AUTO_UPGRADE_SYSTEM:
                startAutoUpgradeSystem();
                break;
            default:
                break;
            }
        }
    };

    private IObserver mStateObserver = new IObserver() {
        @Override
        public void onStateChange(IContextState state) {
			//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
            Log.d(TAG, "onStateChange() state.state() = " + state.state() + " canStartAutoDownload() = " + canStartAutoDownload()
                    + " state.error() = " + state.error() + " isBackgroundProcess = " + isBackgroundProcess(mContext));
            if (state.state() == State.READY_TO_DOWNLOAD && isBackgroundProcess(mContext)) {
                boolean hasNeedpermissions = RuntimePermissionsManager.checkRequiredPermissions(mContext);
                Log.d(TAG, "hasNeedpermissions: " + hasNeedpermissions);
                if (canStartAutoDownload() && hasNeedpermissions) {
                    SystemUpdateManager.getInstance(mContext).
                            downUpdate(null).start();
                } else {
                	Log.d(TAG, "mAutoUpgradeDispatcher.notifyAutoCheck()" );
                	mAutoUpgradeDispatcher.notifyAutoCheck();
				}
                return;
				//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
            }

            if (state.state() == State.DOWNLOAD_VERIFY && isBackgroundProcess(mContext)) {
                Log.d(TAG, "mAutoUpgradeDispatcher.notifyAutoDownloadComplete()" );
                mAutoUpgradeDispatcher.notifyAutoDownloadComplete();
            }
        }

        @Override
        public void onError(IContextState state, int error) {

        }
    };
}
