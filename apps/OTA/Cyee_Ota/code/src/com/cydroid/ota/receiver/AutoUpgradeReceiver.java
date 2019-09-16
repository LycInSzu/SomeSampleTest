package com.cydroid.ota.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.logic.AutoUpgradeDispatcher;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.SystemUpdateAnimActivity;
import com.cydroid.ota.utils.FileUtils;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
import com.cydroid.ota.utils.SystemPropertiesUtils;
import android.app.NotificationChannel;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end

/**
 * Created by liuyanfeng on 15-7-16.
 */
public class AutoUpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoUpgradeReceiver";
    private static final int MAX_COUNT = 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.e(TAG, "Invalid broadcast! No action!");
            return;
        }

        String action = intent.getAction();
        //Log.d(TAG, "action = " + action);
        IStorage wlanAuto = SettingUpdateDataInvoker.getInstance(
                context).wlanAutoStorage();
        if (AutoUpgradeDispatcher.NOTIFICATION_AUTO_CHECK_NEW_VERSION_ACTION
                .equals(action)) {
            boolean isDownloadNotified = wlanAuto.getInt(
                    Key.WlanAuto.KEY_AUTO_UPGRADE_DOWNLOAD_COMPLETE_NOTIFY_COUNT,
                    0) > 0;
            if (!isDownloadNotified) {
                sendAutoNotification(context, false);
                SystemUpdateFactory.autoUpgrade(context).getAutoUpgradeDispatcher().notifyAutoOperation(
                        false);
            }
        } else if (AutoUpgradeDispatcher.NOTIFICATION_AUTO_DOWNLOAD_COMPLETE_ACTION
                .equals(action)) {
            String notifyVersion = intent.getStringExtra(
                    AutoUpgradeDispatcher.SYSTEM_VERSION_NUM);
            Log.d(TAG, "notifyVersion = " + notifyVersion);
            boolean isNeedNotify = notifyVersion.equals(wlanAuto.getString(
                    Key.WlanAuto.KEY_AUTO_UPGRADE_LAST_NOTIFY_SYSTEM_VERSION, ""));
            if (isNeedNotify && isDownloadComplete(context)) {
                sendAutoNotification(context, true);
                SystemUpdateFactory.autoUpgrade(context).getAutoUpgradeDispatcher().notifyAutoOperation(
                        true);
            }
        }

    }


    private boolean isDownloadComplete(Context context) {
        IUpdateInfo updateInfo = SystemUpdateFactory.systemUpdate(context).getSettingUpdateInfo();
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
        return FileUtils.checkFinished(updateInfo.getFileSize(),
                settingStorage
                        .getString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME,
                                ""));
    }

    private void sendAutoNotification(Context context, boolean isDownloadComplete) {
        IStorage wlanAuto = SettingUpdateDataInvoker.getInstance(context).wlanAutoStorage();
        String key;
        if (isDownloadComplete) {
            key = Key.WlanAuto.KEY_AUTO_UPGRADE_DOWNLOAD_COMPLETE_NOTIFY_COUNT;
        } else {
            key = Key.WlanAuto.KEY_AUTO_UPGRADE_NEW_VERSION_NOTIFY_COUNT;
        }
        int notifyCount = wlanAuto.getInt(key, 0);
        if (notifyCount > MAX_COUNT) {
            Log.d(TAG, "already notify more than max!");          
            return;
        }
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
        Notification.Builder builder = new Notification.Builder(context, SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA);
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
        builder.setSmallIcon(R.drawable.gn_su_notification_small_icon);
        builder.setColor(context.getResources().getColor(R.color.gn_su_notification_back_color));
        builder.setAutoCancel(true);
        String ticker;
        String title;
        String content;
        if (isDownloadComplete) {
            ticker = context.getString(R.string.gn_su_notify_auto_download_complete_title);
            title = context.getString(R.string.gn_su_notify_auto_download_complete_title);
            content = context.getString(R.string.gn_su_notify_auto_download_complete_content);
        } else {
            ticker = context.getString(R.string.gn_su_notify_auto_update_title);
            title = context.getString(R.string.gn_su_notify_auto_update_title);
            content = context.getString(R.string.gn_su_notify_auto_check_notify);
        }
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        builder.setContentText(content);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, new Intent(context,
                                SystemUpdateAnimActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin		
        notificationManager.createNotificationChannel(new NotificationChannel(
        		SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA,
                SystemPropertiesUtils.NOTIFICATION_CHANNEL_NAME_OTA,
                NotificationManager.IMPORTANCE_LOW));
        notificationManager.notify(SystemPropertiesUtils.NOTIFICATION_ID, builder.build());
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
        //record notify count
        wlanAuto.putInt(key, notifyCount + 1);
    }
}
