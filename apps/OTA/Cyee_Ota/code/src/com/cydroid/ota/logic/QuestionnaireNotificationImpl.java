package com.cydroid.ota.logic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.cydroid.ota.Log;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.RandomTimeUtil;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.utils.Util;

/**
 * Created by liuyanfeng on 15-7-8.
 */
public class QuestionnaireNotificationImpl implements IQuestionnaireNotification {
    private static final String TAG = "QuestionnaireNotificationManager";
    public static final String NOTIFICATION_QUESTIONNAIRE =
            "com.gionee.update.QUESTIONNAIRE";
    private Context mContext;

    protected QuestionnaireNotificationImpl(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void questionnaireAlarm(boolean isFirst) {
        Log.d(TAG, Log.getFunctionName() + " :isFirst=" + isFirst);
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        if (isFirst) {
            settingStorage.putInt(Key.Setting.KEY_SETTING_QUESTIONNAIRE_COUNT,
                    1);
        } else {
            int count = settingStorage.getInt(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_COUNT, 1) + 1;
            if (count > 3) {
                Log.d(TAG, SystemPropertiesUtils.getInternalVersion() + " already notify 3 times!");
                return;
            }
            settingStorage.putInt(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_COUNT, count);
        }
        try {
            Intent intent = new Intent(mContext, Class.forName("com.cydroid.ota.receiver.QuestionnaireReceiver"));
            intent.setAction(NOTIFICATION_QUESTIONNAIRE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) mContext
                    .getSystemService(Context.ALARM_SERVICE);
            long alarmTime = RandomTimeUtil.getRandomTime();
            Log.d(TAG, "alarmtime:" + Util.utcTimeToLocal(alarmTime, "yyyy.MM.dd HH:mm:ss"));
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
