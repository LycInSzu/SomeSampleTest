package com.cydroid.note.app.reminder;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

import com.cydroid.note.common.Log;
import com.cydroid.note.app.NewNoteActivity;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.trash.util.TrashUtils;
import com.cydroid.note.widget.WidgetUtil;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";
    public static final String ACTION_POP_REMINDER = "com.cydroid.note.action.pop_reminder";
    public static final String ACTION_WIDGET_REMINDER = "com.cydroid.note.action.widget_reminder";
    public static final String ACTION_SECURITY_OS = "com.cydroid.note.action.security_os";
    public static final String ACTION_GO_NOTE_DETAIL = "com.cydroid.note.action.go_note_detail";
    public static final String ACTION_TRASH_CLEAN_REMINDER = "com.cydroid.note.action.TRASH_CLEAN";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (TextUtils.equals(Intent.ACTION_BOOT_COMPLETED, action)) {
            Log.d(TAG, "ACTION_BOOT_COMPLETED");
            ReminderManager.scheduleReminder(context);
            ReminderManager.scheduleSecretItemReminder(context);
            ReminderManager.setWidgetBackgroundReminder(context);
            ReminderManager.scheduleTrashCleanReminder(context);
            WidgetUtil.updateAllWidgets();
            return;
        }

        if (TextUtils.equals(Intent.ACTION_TIME_CHANGED, action)) {
            ReminderManager.scheduleReminder(context);
            ReminderManager.scheduleSecretItemReminder(context);
            ReminderManager.setWidgetBackgroundReminder(context);
            WidgetUtil.updateAllWidgets();
            return;
        }

        if (ACTION_POP_REMINDER.equals(action)) {
            ReminderManager.popReminder(context, intent);
            return;
        }

        if (ACTION_WIDGET_REMINDER.equals(action)) {
            ReminderManager.setWidgetBackgroundReminder(context);
            WidgetUtil.updateAllWidgets();
            return;
        }

        if (ACTION_SECURITY_OS.equals(action)) {
            int id = intent.getIntExtra(NoteContract.NoteContent._ID, 0);
            ReminderManager.cancelNotification(context, id, true);
            return;
        }

        if (ACTION_GO_NOTE_DETAIL.equals(action)) {
            EncryptUtil.removeCurrentTask(context);
            try {
                String path = intent.getStringExtra(NewNoteActivity.NOTE_ITEM_PATH);
                Intent jumpIntent = new Intent(context, NewNoteActivity.class);
                jumpIntent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path);
                jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(jumpIntent);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "e = " + e);
            }
            return;
        }

        if (ACTION_TRASH_CLEAN_REMINDER.equals(action)) {
            long id = intent.getLongExtra(NoteContract.TrashContent._ID, NoteItem.INVALID_ID);
            Log.d(TAG, "clean trash id = " + id);
            TrashUtils.cleanTrash(id);
            return;
        }

    }
}
