package com.cydroid.note.trash.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.cydroid.note.common.Log;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.Path;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.trash.data.TrashNoteItem;

/**
 * Created by xiaozhilong on 7/6/16.
 */
public class TrashUtils {
    private static final String TAG = "TrashUtils";

    public static void throwIntoTrash(Context context, NoteItem item) {
        ContentResolver resolver = context.getContentResolver();
        Uri noteUri = NoteContract.NoteContent.CONTENT_URI;
        resolver.delete(noteUri, "_id=?", new String[]{String.valueOf(item.getId())});
        if (item.getDateTimeReminder() != NoteItem.INVALID_REMINDER) {
            ReminderManager.cancelAlarmAndNotification(context, item.getId(), false);
        }

        long dateDeleted = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(NoteContract.TrashContent.COLUMN_TITLE, item.getTitle());
        values.put(NoteContract.TrashContent.COLUMN_CONTENT, item.getContent());
        values.put(NoteContract.TrashContent.COLUMN_LABEL, NoteItem.convertToStringLabel(item.getLabel()));
        values.put(NoteContract.TrashContent.COLUMN_DATE_CREATED, item.getDateTimeCreated());
        values.put(NoteContract.TrashContent.COLUMN_DATE_MODIFIED, item.getDateTimeModified());
        values.put(NoteContract.TrashContent.COLUMN_DATE_DELETED, dateDeleted);
        values.put(NoteContract.TrashContent.COLUMN_REMINDER, item.getDateTimeReminder());
        values.put(NoteContract.TrashContent.COLUMN_ENCRYPT_HINT_STATE, item.getEncyptHintState());
        values.put(NoteContract.TrashContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, item.getEncrytRemindReadState());
        Uri trashUri = NoteContract.TrashContent.CONTENT_URI;
        Uri uri = resolver.insert(trashUri, values);
        if (uri == null) {
            return;
        }
        int id = Integer.parseInt(uri.getPathSegments().get(1));
        long cleanTime = dateDeleted + TrashNoteItem.KEEP_DAYS_IN_MILLIS;
        ReminderManager.setTrashCleanReminder(context, id, cleanTime);
    }

    public static void recoverFromTrash(Context context, NoteItem item) {
        ContentResolver resolver = context.getContentResolver();
        Uri trashUri = NoteContract.TrashContent.CONTENT_URI;
        resolver.delete(trashUri, "_id=?", new String[]{String.valueOf(item.getId())});
        ReminderManager.cancelTrashCleanReminder(context, item.getId());

        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteContent.COLUMN_TITLE, item.getTitle());
        values.put(NoteContract.NoteContent.COLUMN_CONTENT, item.getContent());
        values.put(NoteContract.NoteContent.COLUMN_LABEL, NoteItem.convertToStringLabel(item.getLabel()));
        values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, item.getDateTimeCreated());
        values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, item.getDateTimeModified());
        values.put(NoteContract.NoteContent.COLUMN_REMINDER, item.getDateTimeReminder());
        values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, item.getEncyptHintState());
        values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, item.getEncrytRemindReadState());
        Uri noteUri = NoteContract.NoteContent.CONTENT_URI;
        Uri itemUri = resolver.insert(noteUri, values);
        if (item.getDateTimeReminder() == NoteItem.INVALID_REMINDER || itemUri == null) {
            return;
        }
        long id = Long.parseLong(itemUri.getPathSegments().get(1));
        ReminderManager.setReminder(context, id, item.getDateTimeReminder(), false);
    }

    public static void cleanTrashBeforeCriticalTime(Context context, long criticalTime) {
        Uri uri = NoteContract.TrashContent.CONTENT_URI;
        String selection = NoteContract.TrashContent.COLUMN_DATE_DELETED + "<=" + criticalTime;
        context.getContentResolver().delete(uri, selection, null);
    }


    public static void cleanTrash(long id) {
        if (id == NoteItem.INVALID_ID) {
            return;
        }

        try {
            Path path = TrashNoteItem.ITEM_PATH.getChild(id);
            NoteAppImpl.getContext().getDataManager().delete(path);
        } catch (Exception e) {
            Log.d(TAG, "cleanTrash fail : " + e);
        }
    }

}
