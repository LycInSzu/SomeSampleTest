package com.cydroid.ota.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cydroid.ota.logic.QuestionnaireNotificationImpl;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.QuestionnaireActivity;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.cydroid.ota.R;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
import android.app.NotificationChannel;
//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
/**
 * Created by liuyanfeng on 15-7-16.
 */
public class QuestionnaireReceiver extends BroadcastReceiver {
    private static final String TAG = "QuestionnaireReceiver";
    private static final int QUESTIONNAIRE_NOTIFY_MAX_COUNT = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.d(TAG, "Invalid broadcast! No action!");
            return;
        }
        String action = intent.getAction();
        if (QuestionnaireNotificationImpl.NOTIFICATION_QUESTIONNAIRE
            .equals(action)) {
            Log.d(TAG, "action = NOTIFICATION_QUESTIONNAIRE");
            IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                .questionnaire(context).getQuestionnaireInfo();
            Log.d(TAG,"questionnaireInfois null " + (questionnaireInfo != null));
            if (questionnaireInfo != null && questionnaireInfo.getStatus() == 0) {
                sendQuestionnaireNotification(context);
                SystemUpdateFactory.questionnaire(context)
                    .questionnaireNotification().questionnaireAlarm(
                    false);
            }

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (NetworkUtils.isNetCanUse(context) && isNeedCheckQuestionnaire(context)) {
                IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                    context).settingStorage();
                settingStorage.putLong(Key.Setting.KEY_SETTING_QUESTIONNAIRE_LAST_CHECK_TIME, System.currentTimeMillis());
                SystemUpdateFactory.questionnaire(context).registerDataChange(null);
            }
        }
    }

    private boolean isNeedCheckQuestionnaire(Context context) {
        Log.d(TAG, "isNeedCheckQuestionnaire");
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
            context).settingStorage();
        long upgradeSuccessTime = settingStorage.getLong(Key.Setting.KEY_SETTING_UPDATE_SUCCESS_DATE, 0);
        upgradeSuccessTime = System.currentTimeMillis() - Constants.ONE_DAY_MILLISECOND * 2;
        if (upgradeSuccessTime == 0) {
            return false;
        }

        long now = System.currentTimeMillis();
        long gap = (now - upgradeSuccessTime) / Constants.ONE_DAY_MILLISECOND;
        if (gap > 15)
            return false;

        long lastCheckTime = settingStorage.getLong(Key.Setting.KEY_SETTING_QUESTIONNAIRE_LAST_CHECK_TIME, 0);
        if (lastCheckTime == 0) {
            Log.d(TAG, "return true 000");
            return true;
        }
        gap = now - lastCheckTime;
        if (gap < Constants.ONE_DAY_MILLISECOND) {
            Log.d(TAG, "return false");
            return false;
        }
        Log.d(TAG, "return true");
        return true;
    }

    private void sendQuestionnaireNotification(Context context) {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
        int count = settingStorage.getInt(
            Key.Setting.KEY_SETTING_QUESTIONNAIRE_COUNT, 1);
        if (count > QUESTIONNAIRE_NOTIFY_MAX_COUNT) {
            Log.d(TAG, SystemPropertiesUtils.getInternalVersion() + " already notify 3 times!");
            return;
        }
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
        Notification.Builder builder = new Notification.Builder(context, SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA);
		//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end
        builder.setSmallIcon(R.drawable.gn_su_notification_small_icon);
        builder.setColor(context.getResources()
            .getColor(R.color.gn_su_notification_back_color));
        builder.setTicker(context.getString(R.string.gn_su_notify_auto_download_complete_title));
        builder.setContentTitle(context.getString(R.string.gn_su_notify_auto_download_complete_title));
        builder.setAutoCancel(true);
        builder.setContentText(context.getString(R.string.gn_su_string_questionnaire_hint));
        Intent intent = new Intent(context,
            QuestionnaireActivity.class);
        intent.putExtra("fromNotify", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
