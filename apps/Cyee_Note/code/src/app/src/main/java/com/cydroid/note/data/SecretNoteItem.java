package com.cydroid.note.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import com.cydroid.note.app.Config;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.common.UpdateHelper;
import com.cydroid.note.encrypt.DES;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.NoteItemAttachInfo;
import com.cydroid.note.encrypt.NoteActionProgressListener;
import com.cydroid.note.provider.NoteContract;

import java.io.File;
import java.util.List;

/**
 * Created by spc on 16-4-15.
 */
public class SecretNoteItem extends NoteItem {
    public static final String LABEL_SEPARATOR = ",";
    public static final String[] NOTE_PROJECTION = {//NOSONAR
            NoteContract.NoteContent._ID,//NOSONAR
            NoteContract.NoteContent.COLUMN_TITLE,//NOSONAR
            NoteContract.NoteContent.COLUMN_CONTENT,//NOSONAR
            NoteContract.NoteContent.COLUMN_LABEL,//NOSONAR
            NoteContract.NoteContent.COLUMN_DATE_CREATED,//NOSONAR
            NoteContract.NoteContent.COLUMN_DATE_MODIFIED,//NOSONAR
            NoteContract.NoteContent.COLUMN_REMINDER,//NOSONAR
            NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE,//NOSONAR
            NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE};//NOSONAR
    public static final int INDEX_ID = 0;
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_CONTENT = 2;
    public static final int INDEX_LABEL = 3;
    public static final int INDEX_DATE_CREATED = 4;
    public static final int INDEX_DATE_MODIFIED = 5;
    public static final int INDEX_REMINDER = 6;
    public static final int INDEX_ENCRYPT_HINT_STATE = 7;
    public static final int INDEX_ENCRYPT_REMIND_READ_STATE = 8;

    public static final Path SECRET_ITEM_PATH = Path.fromString(LocalSource.LOCAL_SECRET_ITEM_PATH);
    private Context mContext;
    private final ContentResolver mResolver;
    private final DES mDes = new DES();

    public SecretNoteItem(Path path, NoteAppImpl application, Cursor cursor) {
        super(path, nextVersionNumber());
        mContext = application;
        isEncrypted = true;
        mResolver = application.getContentResolver();
        loadFromCursor(cursor);
    }

    public SecretNoteItem(Path path, NoteAppImpl application, int id) {
        super(path, nextVersionNumber());
        mContext = application;
        isEncrypted = true;
        mResolver = application.getContentResolver();
        Uri uri = NoteContract.NoteContent.SECRET_CONTENT_URI;
        Cursor cursor = SecretNoteSet.getItemCursor(mResolver, uri, NOTE_PROJECTION, id);
        if (cursor == null) {
            throw new RuntimeException("cannot get cursor for: " + path);
        }
        try {
            if (cursor.moveToNext()) {
                loadFromCursor(cursor);
            } else {
                throw new RuntimeException("cannot find data for: " + path);
            }
        } finally {
            NoteUtils.closeSilently(cursor);
        }
    }

    private void loadFromCursor(Cursor cursor) {
        id = cursor.getInt(INDEX_ID);
        title = mDes.authcode(cursor.getString(INDEX_TITLE), DES.OPERATION_DECODE, DES.DES_KEY);
        content = mDes.authcode(cursor.getString(INDEX_CONTENT), DES.OPERATION_DECODE, DES.DES_KEY);
        String labels = cursor.getString(INDEX_LABEL);
        convertLabel(labels);
        dateCreatedInMs = cursor.getLong(INDEX_DATE_CREATED);
        dateModifiedInMs = cursor.getLong(INDEX_DATE_MODIFIED);
        dateReminderInMs = cursor.getLong(INDEX_REMINDER);
        encyptHintState = cursor.getInt(INDEX_ENCRYPT_HINT_STATE);
        encrytRemindReadState = cursor.getInt(INDEX_ENCRYPT_REMIND_READ_STATE);
    }

    private void convertLabel(String labels) {
        label.clear();
        if (labels == null) {
            return;
        }
        String[] temps = labels.split(LABEL_SEPARATOR);
        for (String temp : temps) {
            label.add(Integer.parseInt(temp));
        }
    }

    @Override
    protected boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        id = uh.update(id, cursor.getInt(INDEX_ID));
        title = uh.update(title,
                mDes.authcode(cursor.getString(INDEX_TITLE), DES.OPERATION_DECODE, DES.DES_KEY));
        content = uh.update(content,
                mDes.authcode(cursor.getString(INDEX_CONTENT), DES.OPERATION_DECODE, DES.DES_KEY));
        String labels = uh.update(label, cursor.getString(INDEX_LABEL));
        convertLabel(labels);
        dateCreatedInMs = uh.update(dateCreatedInMs, cursor.getLong(INDEX_DATE_CREATED));
        dateModifiedInMs = uh.update(dateModifiedInMs, cursor.getLong(INDEX_DATE_MODIFIED));
        dateReminderInMs = uh.update(dateReminderInMs, cursor.getLong(INDEX_REMINDER));
        encyptHintState = uh.update(encyptHintState, cursor.getInt(INDEX_ENCRYPT_HINT_STATE));
        encrytRemindReadState = uh.update(encrytRemindReadState, cursor.getInt(INDEX_ENCRYPT_REMIND_READ_STATE));
        isEncrypted = true;
        return uh.isUpdated();
    }

    @Override
    public Bitmap requestImage(int mediaType, Uri uri) {
        if (!PlatformUtil.isSecurityOS()
                && ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String filePath = uri.getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
        }
        Config.NoteCard config = Config.NoteCard.get(mContext);
        ThumbnailDecodeProcess decodeProcess = new ThumbnailDecodeProcess(mContext, uri, config.mImageWidth,
                config.mImageHeight, ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT,
                true);
        return decodeProcess.getThumbnail();
    }

    @Override
    public void delete() throws Exception {
        Uri uri = NoteContract.NoteContent.SECRET_CONTENT_URI;
        mResolver.delete(uri, "_id=?", new String[]{String.valueOf(id)});

        cancelReminder();
        NoteUtils.deleteOriginMediaFile(content, true);
    }

    @Override
    public void decrypt(NoteActionProgressListener listener) throws Exception {
        int noteId = decryptTextContent();
        List<NoteItemAttachInfo> attachInfos = EncryptUtil.getAttachs(content);
        if (PlatformUtil.isSecurityOS()) {
            EncryptUtil.decrpyAttachFileForSecurityOS(attachInfos, listener);
        } else {
            EncryptUtil.decryptAttachFile(attachInfos, listener);
        }
        handleRemind(noteId);
    }

    private int decryptTextContent() {
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteContent.COLUMN_TITLE, title);
        values.put(NoteContract.NoteContent.COLUMN_CONTENT, content);
        values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, dateModifiedInMs);
        values.put(NoteContract.NoteContent.COLUMN_REMINDER, dateReminderInMs);
        String labels = NoteItem.convertToStringLabel(label);
        values.put(NoteContract.NoteContent.COLUMN_LABEL, labels);
        values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, dateCreatedInMs);
        values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, encyptHintState);
        values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, encrytRemindReadState);
        Uri uri = mResolver.insert(NoteContract.NoteContent.CONTENT_URI, values);
        mResolver.delete(NoteContract.NoteContent.SECRET_CONTENT_URI, "_id=?",
                new String[]{String.valueOf(id)});
        return (int) ContentUris.parseId(uri);
    }

    private void handleRemind(int noteId) {
        if (dateReminderInMs != NoteItem.INVALID_REMINDER
                && dateReminderInMs > System.currentTimeMillis()) {
            ReminderManager.cancelAlarmAndNotification(NoteAppImpl.getContext(), id, true);
            ReminderManager.setReminder(NoteAppImpl.getContext(), noteId, dateReminderInMs, false);
        }
    }

    private void cancelReminder() {
        if (dateReminderInMs != INVALID_REMINDER) {
            ReminderManager.cancelAlarmAndNotification(mContext, id, true);
        }
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = NoteContract.NoteContent.SECRET_CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

}
