package com.cydroid.note.app.reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.cydroid.note.R;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NewNoteActivity;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.Log;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteParser;
import com.cydroid.note.data.Path;
import com.cydroid.note.data.SecretNoteItem;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.trash.data.TrashNoteItem;
import com.cydroid.note.trash.util.TrashUtils;

import org.json.JSONObject;
import org.json.JSONTokener;

public class ReminderManager {
	private static final String TAG = "ReminderManager";
    private static final String[] ALARM_PROJECT = new String[]{
            NoteContract.NoteContent._ID,
            NoteContract.NoteContent.COLUMN_REMINDER};
    private static final String[] TRASH_PROJECT = {
            NoteContract.TrashContent._ID,
            NoteContract.TrashContent.COLUMN_DATE_DELETED
    };
    private static final int INDEX_TRASH_ID = 0;
    private static final int INDEX_TRASH_DATE_DELETED = 1;

    private static final int WIDGET_BG_REQUEST_CODE = -1;
    private static final long ONE_DAY_IN_MILLISECOND = 24 * 60 * 60 * 1000;
    private static final long ONE_MINUTE_IN_MILLISECOND = 60 * 1000;
    private static final int INDEX_ID = 0;
    private static final int INDEX_REMINDER = 1;
    public static final String IS_SECRET_TAG = "is_secret";
    public static final int SECRET_ID_OFFSET = 1000000;
	//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
	private static final String channelId = "channel_1";
    private static final String channelName = "channel_name_1";
	//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end

    public static void scheduleReminder(final Context context) {
        Handler handler = new Handler(NoteAppImpl.getContext().getNoteBackgroundLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                scheduleItemReminder(context, NoteContract.NoteContent.CONTENT_URI, false);
                return;
            }
        });
    }

    public static void scheduleSecretItemReminder(final Context context) {
        Handler handler = new Handler(NoteAppImpl.getContext().getNoteBackgroundLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                scheduleItemReminder(context, NoteContract.NoteContent.SECRET_CONTENT_URI, true);
                return;
            }
        });
    }

    public static void scheduleItemReminder(Context context, Uri tableUri, boolean isSecret) {
        long current = System.currentTimeMillis();
        String selection = NoteContract.NoteContent.COLUMN_REMINDER + ">" + current;
        Cursor cursor = context.getContentResolver().query(tableUri, ALARM_PROJECT, selection, null, null);
        if (cursor == null) {
            return;
        }
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(INDEX_ID);
                long reminder = cursor.getLong(INDEX_REMINDER);
                setReminder(context, id, reminder, isSecret);
            }
        } finally {
            NoteUtils.closeSilently(cursor);
        }
    }

    public static void setWidgetBackgroundReminder(Context context) {
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent intent = new Intent(ReminderReceiver.ACTION_WIDGET_REMINDER);
		Intent intent =new Intent(context, ReminderReceiver.class);
		intent.setAction(ReminderReceiver.ACTION_WIDGET_REMINDER);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, WIDGET_BG_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        long trrigleTime = System.currentTimeMillis() + ONE_DAY_IN_MILLISECOND;
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trrigleTime, pendingIntent);
        }else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, trrigleTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, trrigleTime, pendingIntent);
        }
    }

    public static void scheduleTrashCleanReminder(final Context context) {
        Handler handler = new Handler(NoteAppImpl.getContext().getNoteBackgroundLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                long keepTime = TrashNoteItem.KEEP_DAYS_IN_MILLIS;
                long criticalTime = current - keepTime;
                TrashUtils.cleanTrashBeforeCriticalTime(context, criticalTime);


                Uri uri = NoteContract.TrashContent.CONTENT_URI;
                Cursor cursor = context.getContentResolver().query(uri, TRASH_PROJECT, null, null, null);
                if (cursor == null) {
                    return;
                }
                try {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(INDEX_TRASH_ID);
                        long dateDeleted = cursor.getLong(INDEX_TRASH_DATE_DELETED);
                        long cleanTime = dateDeleted + keepTime;
                        setTrashCleanReminder(context, id, cleanTime);
                    }
                } finally {
                    NoteUtils.closeSilently(cursor);
                }
                return;
            }
        });
    }

    public static void setTrashCleanReminder(Context context, long id, long cleanTime) {
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent intent = new Intent(ReminderReceiver.ACTION_TRASH_CLEAN_REMINDER);
		Intent intent = new Intent(context,ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_TRASH_CLEAN_REMINDER);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        intent.putExtra(NoteContract.TrashContent._ID, id);
        int notifyId = (int) id;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cleanTime, pendingIntent);
        }else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cleanTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cleanTime, pendingIntent);
        }
    }

    public static void cancelTrashCleanReminder(Context context, long id) {
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent intent = new Intent(ReminderReceiver.ACTION_TRASH_CLEAN_REMINDER);
		Intent intent = new Intent(context,ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_TRASH_CLEAN_REMINDER);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        intent.putExtra(NoteContract.TrashContent._ID, id);
        int notifyId = (int) id;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        alarmManager.cancel(pendingIntent);
    }

    public static void setReminder(Context context, long id, long reminder, boolean isSecret) {
/*        long current = System.currentTimeMillis();
        if (reminder < current) {
            return;
        }*/
  		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent intent = new Intent(ReminderReceiver.ACTION_POP_REMINDER);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_POP_REMINDER);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        intent.putExtra(NoteContract.NoteContent._ID, id);
        intent.putExtra(IS_SECRET_TAG, isSecret);
        int notifyId = INDEX_ID;
        if (isSecret) {
            notifyId = (int) id + SECRET_ID_OFFSET;
        } else {
            notifyId = (int) id;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder, pendingIntent);
        }else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminder, pendingIntent);
        }
    }

    public static void cancelAlarmAndNotification(Context context, long id, boolean isSecret) {
        //Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent intent = new Intent(ReminderReceiver.ACTION_POP_REMINDER);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_POP_REMINDER);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        intent.putExtra(NoteContract.NoteContent._ID, id);
        int notifyId = INDEX_ID;
        if (isSecret) {
            notifyId = (int) id + SECRET_ID_OFFSET;
        } else {
            notifyId = (int) id;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifyId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel((int) notifyId);
    }

    public static void cancelNotification(Context context, long id, boolean isSecret) {
        int notifyId = INDEX_ID;
        if (isSecret) {
            notifyId = (int) id + SECRET_ID_OFFSET;
        } else {
            notifyId = (int) id;
        }
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notifyId);
    }

    public static void popReminder(final Context context, Intent intent) {
        final long id = intent.getLongExtra(NoteContract.NoteContent._ID, NoteItem.INVALID_ID);
        if (id == NoteItem.INVALID_ID) {
            return;
        }
        final boolean isSecret = intent.getBooleanExtra(IS_SECRET_TAG, false);
        final NoteAppImpl app = NoteAppImpl.getContext();
        app.getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                Notification notification = null;
                int notifyId = INDEX_ID;
                if (isSecret) {
                    notifyId = (int) id + SECRET_ID_OFFSET;
                    notification = getSecretItemNotification(id, app);
                    updateReminderReadState((int) id);
                } else {
                    notifyId = (int) id;
                    notification = getLocalItemNotification(id, app);
                }
                if (null == notification) {
                    return null;
                }
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
				NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
				channel.enableVibration(true);
                nm.createNotificationChannel(channel);
				//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
                nm.notify(notifyId, notification);
                return null;
            }
        });
    }

    private static Notification getLocalItemNotification(final long id, final Context context) {
        Path path = LocalNoteItem.ITEM_PATH.getChild(id);
        final NoteAppImpl app = NoteAppImpl.getContext();
        NoteItem item = (NoteItem) app.getDataManager().getMediaObject(path);
//        if (!isValidReminder(item.getDateTimeReminder())) {
//            return null;
//        }
        String title = item.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }
        String content = getReminderContent(item.getContent());
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent innerIntent = new Intent(ReminderReceiver.ACTION_GO_NOTE_DETAIL);
		Intent innerIntent = new Intent(context, ReminderReceiver.class);
		innerIntent.setAction(ReminderReceiver.ACTION_GO_NOTE_DETAIL);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        innerIntent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
        PendingIntent wrapIntent = PendingIntent.getBroadcast(context, (int) id, innerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setAutoCancel(true);
        builder.setContentIntent(wrapIntent);
        builder.setContentTitle(title);
        builder.setContentText(content);
        //GIONEE wanghaiyan 2016-12-21 modify for 48095 begin
        //builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setSmallIcon(R.drawable.note_reminder);
	    //GIONEE wanghaiyan 2016-12-21 modify for 48095 end
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setWhen(System.currentTimeMillis());
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
		builder.setChannelId(channelId);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        return builder.build();
    }

    private static boolean isValidReminder(long reminder) {
        boolean validReminder = reminder >= System.currentTimeMillis() - ONE_MINUTE_IN_MILLISECOND;
        if (validReminder) {
            return true;
        }
        return false;
    }

    private static void updateReminderReadState(final int id) {
        Handler handler = new Handler(NoteAppImpl.getContext().getNoteBackgroundLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = NoteAppImpl.getContext().getContentResolver();
                Cursor cursor = resolver.query(NoteContract.NoteContent.SECRET_CONTENT_URI, SecretNoteItem.NOTE_PROJECTION,
                        "_id=?", new String[]{String.valueOf(id)}, null);
                if (cursor == null) {
                    return;
                }
                if (cursor.moveToFirst()) {
                    String title = cursor.getString(SecretNoteItem.INDEX_TITLE);
                    String content = cursor.getString(SecretNoteItem.INDEX_CONTENT);
                    String labels = cursor.getString(SecretNoteItem.INDEX_LABEL);
                    long dateCreatedInMs = cursor.getLong(SecretNoteItem.INDEX_DATE_CREATED);
                    long dateModifiedInMs = cursor.getLong(SecretNoteItem.INDEX_DATE_MODIFIED);
                    long dateReminderInMs = cursor.getLong(SecretNoteItem.INDEX_REMINDER);
                    int encyptHintState = cursor.getInt(SecretNoteItem.INDEX_ENCRYPT_HINT_STATE);
                    ContentValues values = new ContentValues();
                    values.put(NoteContract.NoteContent.COLUMN_TITLE, title);
                    values.put(NoteContract.NoteContent.COLUMN_CONTENT, content);
                    values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, dateCreatedInMs);
                    values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, dateModifiedInMs);
                    values.put(NoteContract.NoteContent.COLUMN_REMINDER, dateReminderInMs);
                    values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, encyptHintState);
                    values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, Constants.ENCRYPT_REMIND_NOT_READ);
                    values.put(NoteContract.NoteContent.COLUMN_LABEL, labels);
                    String selection = NoteContract.NoteContent._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(id)};
                    resolver.update(NoteContract.NoteContent.SECRET_CONTENT_URI, values, selection, selectionArgs);
                }
            }
        });
    }

    private static Notification getSecretItemNotification(final long id, final Context context) {
        Path path = SecretNoteItem.SECRET_ITEM_PATH.getChild(id);
        NoteItem item = (NoteItem) NoteAppImpl.getContext().getDataManager().getMediaObject(path);
//        if (!isValidReminder(item.getDateTimeReminder())) {
//            return null;
//        }
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
        //Intent innerIntent = new Intent(ReminderReceiver.ACTION_SECURITY_OS);
        Intent innerIntent = new Intent(context, ReminderReceiver.class);
		innerIntent.setAction(ReminderReceiver.ACTION_SECURITY_OS);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        innerIntent.putExtra(NoteContract.NoteContent._ID, (int) id);
        PendingIntent wrapIntent = PendingIntent.getBroadcast(context, (int) id + SECRET_ID_OFFSET, innerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setAutoCancel(true);
        builder.setContentIntent(wrapIntent);
	    //GIONEE wanghaiyan 2016-12-21 modify for 48095 begin
        //builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setSmallIcon(R.drawable.note_reminder);
	    //GIONEE wanghaiyan 2016-12-21 modify for 48095 end
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setWhen(System.currentTimeMillis());
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 begin
		builder.setChannelId(channelId);
		//Chenyee wanghaiyan 2018-6-8 modify for CSW1707A-1293 end
        return builder.build();
    }

    private static String getReminderContent(String json) {
        if (json == null || json.length() == 0) {
            return "";
        }

        String reminderContent = "";
        try {
            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            String text = jsonObject.getString(DataConvert.JSON_CONTENT_KEY);
            reminderContent = NoteParser.parserText(text);
        } catch (Exception e) {
        }
        return reminderContent;
    }
}
