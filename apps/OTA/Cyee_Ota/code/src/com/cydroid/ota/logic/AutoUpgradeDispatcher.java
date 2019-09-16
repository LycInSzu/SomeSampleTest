package com.cydroid.ota.logic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.RandomTimeUtil;
import com.cydroid.ota.Log;

/**
 * Created by liuyanfeng on 15-3-20.
 */
public class AutoUpgradeDispatcher implements IAutoUpgradeDispatcher {
    private static final String TAG = "AutoUpgradeDispatcher";
    public static final String NOTIFICATION_AUTO_DOWNLOAD_COMPLETE_ACTION
            = "com.gionee.update.AUTO_DOWNLOAD_COMPLETE";
    public static final String NOTIFICATION_AUTO_CHECK_NEW_VERSION_ACTION
            = "com.gionee.update.AUTO_CHECK_NEW_VERSION";
    public static final String SYSTEM_VERSION_NUM = "SYSTEM_VERSION_NUM";
    public static final String ACTION_ALARM_START_SERVICE = "com.gionee.update.ACTION_ALARM_START_SERVICE";
    private static final int ALARM_TIME_DURATION = 1000 * 60 * 10;
    private Context mContext;

    protected AutoUpgradeDispatcher(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void notifyAutoOperation(boolean isDownloadComplete) {
        try {
            Intent intent = new Intent(mContext, Class.forName("com.cydroid.ota.receiver.AutoUpgradeReceiver"));
            if (isDownloadComplete) {
                intent.setAction(NOTIFICATION_AUTO_DOWNLOAD_COMPLETE_ACTION);
            } else {
                intent.setAction(NOTIFICATION_AUTO_CHECK_NEW_VERSION_ACTION);
            }
            IStorage wlanAuto = SettingUpdateDataInvoker.getInstance(mContext).wlanAutoStorage();
            intent.putExtra(SYSTEM_VERSION_NUM,
                    wlanAuto.getString(
                            Key.WlanAuto.KEY_AUTO_UPGRADE_LAST_NOTIFY_SYSTEM_VERSION,
                            ""));
            boolean isFirstNotify = isFirstNotify(isDownloadComplete);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) mContext
                    .getSystemService(Context.ALARM_SERVICE);
            long alarmTime = RandomTimeUtil.getRandomTime(isFirstNotify);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyAutoCheck() {
        IUpdateInfo updateInfo = SystemUpdateManager
                .getInstance(mContext).getSettingUpdateInfo();
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin		
        String updateVersion = updateInfo.getInternalVer();
        IStorage wlanAuto = SettingUpdateDataInvoker
                .getInstance(mContext).wlanAutoStorage();
        String lastNotifyVersion = wlanAuto.getString(
                Key.WlanAuto.KEY_AUTO_UPGRADE_LAST_NOTIFY_SYSTEM_VERSION,"");
        Log.d(TAG, "updateVersion: " + updateVersion + " lastNotifyVersion: " + lastNotifyVersion);

        if (!updateVersion.equals(lastNotifyVersion)) {
            resetNotifyCount();
            wlanAuto.putString(
                    Key.WlanAuto.KEY_AUTO_UPGRADE_LAST_NOTIFY_SYSTEM_VERSION, updateVersion);
            notifyAutoOperation(false);
        }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
    }

    @Override
    public void notifyAutoDownloadComplete() {
        if (isFirstNotify(true)) {
            notifyAutoOperation(true);
        }
    }

    private boolean isFirstNotify(boolean isDownloadComplete) {
        IStorage wlanAuto = SettingUpdateDataInvoker.getInstance(mContext).wlanAutoStorage();
        String key;
        if (isDownloadComplete) {
            key = Key.WlanAuto.KEY_AUTO_UPGRADE_DOWNLOAD_COMPLETE_NOTIFY_COUNT;
        } else {
            key = Key.WlanAuto.KEY_AUTO_UPGRADE_NEW_VERSION_NOTIFY_COUNT;
        }
        return wlanAuto.getInt(key, 0) == 0? true: false;
    }

    private void resetNotifyCount(){
        IStorage wlanAuto = SettingUpdateDataInvoker.getInstance(mContext).wlanAutoStorage();
        wlanAuto.putInt(Key.WlanAuto.KEY_AUTO_UPGRADE_NEW_VERSION_NOTIFY_COUNT,
                0);
        wlanAuto.putInt(
                Key.WlanAuto.KEY_AUTO_UPGRADE_DOWNLOAD_COMPLETE_NOTIFY_COUNT, 0);
    }
}
