package com.cydroid.ota.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.cydroid.ota.Log;

/**
 * Created by kangjj on 15-6-18.
 */
public class NotificationUtil {
    private static final String TAG = "NotificationUtil";

    public static void clearNotification(Context context, int notificationId) {
        NotificationManager notifyManager = getNotificationManager(context);
        if (null != notifyManager) {
            notifyManager.cancel(notificationId);
        } else {
            Log.e(TAG, "clearNotification() mNotifyManager is null");
        }
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showNotication(Context context, Notification notification, int notificationId) {
        NotificationManager notifyManager = getNotificationManager(context);
        if (null != notifyManager) {
            notifyManager.notify(notificationId, notification);
        } else {
            Log.e(TAG, "clearNotification() mNotifyManager is null");
        }
    }
}
