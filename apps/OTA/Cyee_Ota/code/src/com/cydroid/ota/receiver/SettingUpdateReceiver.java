package com.cydroid.ota.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.net.Uri;

import com.cydroid.ota.Log;
import com.cydroid.ota.execption.SettingUpdateNetException;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.logic.net.HttpHelper;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.logic.utils.ClltStatisticsUtil;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.SystemUpdateAnimActivity;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.StorageUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import org.apache.http.NameValuePair;

import java.io.File;
import java.util.List;

import com.cydroid.ota.R;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
import android.app.NotificationChannel;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.utils.StatisticsHelper;
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

/**
 * Created by liuyanfeng on 15-3-23.
 */
public class SettingUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "SettingUpdateReceiver";
    private boolean isRecordingClltData = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.d(TAG, "Invalid broadcast! No action!");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "action: " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "boot completed");
            //add for CR01772552
            //deleteOldVersionFile(context);
            IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                    context).settingStorage();
            String oldVersion = settingStorage.getString(
                    Key.Setting.KEY_SETTING_UPDATE_LAST_VERSION, "");
            Log.d(TAG, "boot completed oldVersion = " + oldVersion);
            if (TextUtils.isEmpty(oldVersion)) {
                return;
            }

            String curVersion = SystemPropertiesUtils.getInternalVersion();
            Log.d(TAG, "oldVersion:" + oldVersion + "curVersion: " + curVersion);
            boolean isFirstBootCompleted = settingStorage.getBoolean(Key.Setting.KEY_FIRST_BOOT_COMPLEPED_AFRET_UPGRADE, true);
            Log.d(TAG, "boot completed isFirstBootCompleted = " + isFirstBootCompleted);
            if (!curVersion.equals(oldVersion) && isFirstBootCompleted) {
                sendUpgradeNotification(context);
                settingStorage.putBoolean(Key.Setting.KEY_FIRST_BOOT_COMPLEPED_AFRET_UPGRADE, false);
                settingStorage.putLong(Key.Setting.KEY_SETTING_UPDATE_SUCCESS_DATE, System.currentTimeMillis());
				//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
                if (SettingUpdateApplication.sStatisticsHelper != null) {
                    SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_FINISH_UPGRADE,
                            StatisticsHelper.KEY_UPGRADED_VERSION_INFO, oldVersion + "->" + curVersion);
                }
				//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
            }
            deleteUpgradeFile(context);

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

            if (NetworkUtils.isNetworkAvailable(context) && NetworkUtils.isNetCanUse(context) && !isRecordingClltData) {
                //sendClltUpgradedataOrNot(context);
            }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin	
        } else if("com.cydroid.ota.CHECK_NEW_VERSION".equals(action)) {
            if (NetworkUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "auto check new version begin");
                SystemUpdateFactory.autoUpgrade(context).autoUpgradeSystem();
            } else {
                Log.d(TAG, "Network is not available!");
            }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end	
        } else {
            Log.d(TAG, "Other action, no operation!");
        }
    }

     // Gionee zhouhuiquan 2017-03-06 modify for 70709 begin
     private void deleteUpgradeFile(Context context) {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
        String installFile = settingStorage.getString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
        Log.d(TAG, "deleteUpgradeFile installFile = " + installFile);
       //String name =  "system_update_ota_new.zip";
       if (!TextUtils.isEmpty(installFile)) {
          // if (installFile.contains(name) || installFile.endsWith(name))  {
               File file = new File(installFile);
               if (file.exists()) {
                   Log.d(TAG,"file.exists()");
                   file.delete();
                   updateMediaDate(context,installFile);
                   settingStorage.putString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
                   settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
               }
          // }
       }

    }
// Gionee zhouhuiquan 2017-03-06 modify for 70709 end
    
    //delete old version upgrade file
    private void deleteOldVersionFile(Context context) {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
      String interpath =  StorageUtils.getInternalStoragePath(context);
     String  path = interpath + "system_update_ota.zip";
      Log.d(TAG, "deleteOldVersionFile system_update_ota path = " + path);
        File file = new File(path);
        if (file.exists())  {
         file.delete();
         updateMediaDate(context,path);
         settingStorage.putString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
         settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
       } 
        path = interpath + "gn_system_update_ota.zip";
        Log.d(TAG, "deleteOldVersionFile gn_system_update_ota path = " + path);
          File file1 = new File(path);
          if (file1.exists())  {
           file1.delete();
           updateMediaDate(context,path);
           settingStorage.putString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
           settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
         } 
    }
    
    private void updateMediaDate(Context context,String fileName) {
        Uri data = Uri.parse("file://" + fileName);     
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    private void sendClltUpgradedataOrNot(Context context) {
        Log.d(TAG, Log.getFunctionName());
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                context).settingStorage();

        String oldVersion = settingStorage.getString(
                Key.Setting.KEY_SETTING_UPDATE_LAST_VERSION, "");
        Log.d(TAG, Log.getFunctionName() + "oldVersion :" + oldVersion);
        if (TextUtils.isEmpty(oldVersion)) {
            return;
        }

        String curVersion = SystemPropertiesUtils.getInternalVersion();
        boolean isRecover = settingStorage.getBoolean(Key.Setting.KEY_UPGRADE_RECOVER_UPGRADE_FLAG, false);
        boolean isInstallSuccess = false;
        if (!oldVersion.equals(curVersion)) {
            isInstallSuccess = true;
        } else {
            if (isRecover) {
                isInstallSuccess = true;
            }
        }
        new ClltAsynctask(context).execute(isInstallSuccess);
    }

    private void sendUpgradeNotification(Context context) {
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
        Notification.Builder builder = new Notification.Builder(context, SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA);
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
        builder.setSmallIcon(R.drawable.gn_su_notification_small_icon).setColor(context.getResources().getColor(R.color.gn_su_notification_back_color));
        builder.setTicker(context.getString(R.string.gn_su_string_upgrade_success));
        builder.setContentTitle(context.getString(R.string.gn_su_string_upgrade_success));
        builder.setAutoCancel(true);
        String hint = String.format(context.getString(R.string.gn_su_string_upgrade_success_hint),
                SystemPropertiesUtils.getCurrentVersion());
        builder.setContentText(hint);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,
                SystemUpdateAnimActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
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
    }

    class ClltAsynctask extends AsyncTask<Boolean, String, Boolean> {
        private Context context;

        public ClltAsynctask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            isRecordingClltData = true;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            boolean isInstallSuccess = params[0].booleanValue();
            boolean isUploadSucess = false;
            Log.d(TAG, " isInstallSuccess :" + isInstallSuccess);
            try {
                IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                        context).settingStorage();
                String packagename;

                if (settingStorage.getBoolean(Key.Setting.KEY_UPGRADE_UPGRADE_ONLINE_FLAG, false)) {
                    packagename = SystemUpdateFactory.systemUpdate(context).getSettingUpdateInfo().getDownloadUrl();
                    Log.d(TAG, "packageName xx = " + packagename);
                    if (packagename != null) {
                        int indexOf = packagename.lastIndexOf("/");
                        if (indexOf > 0) {
                            packagename = packagename.substring(indexOf);
                        }
                    }

                } else {
                    packagename = settingStorage.getString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
                    Log.d(TAG, "packageName yy = " + packagename);
                }

                if (!TextUtils.isEmpty(packagename)) {
                    String url = ClltStatisticsUtil.createRequestURl(false, false, isInstallSuccess, packagename);
                    boolean isAutoDownlod = settingStorage.getBoolean(Key.Setting.KEY_SETTING_UPDATE_LAST_DOWNLOAD_FLAG, false);
                    Log.d(TAG, " isAutoDownlod :" + isAutoDownlod);
                    boolean isRoot = SystemUpdateFactory.systemUpdate(context).getContextState().isRoot();
                    List<NameValuePair> pairs = ClltStatisticsUtil.createVersionPairs(context, isAutoDownlod, isRoot, false);
                    HttpHelper.executeHttpGet(url, pairs, true, null, HttpUtils.getUAHeader(context));
                    isUploadSucess = true;
                }
            } catch (SettingUpdateNetException e) {
                e.printStackTrace();
            }
            return isUploadSucess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, " result:" + (result == null));
            isRecordingClltData = false;
            if (result) {
                IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                        context).settingStorage();
                settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_LAST_VERSION, "");
                settingStorage.putString(Key.Setting.KEY_SETTING_UPDATE_PACKAGE_NAME, "");
                settingStorage.putBoolean(Key.Setting.KEY_UPGRADE_UPGRADE_ONLINE_FLAG, false);
                settingStorage.putBoolean(Key.Setting.KEY_UPGRADE_RECOVER_UPGRADE_FLAG, false);
            }
        }
    }


}

