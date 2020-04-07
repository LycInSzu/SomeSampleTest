package com.cydroid.note.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.cydroid.note.app.Config;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.common.UpdateHelper;
import com.cydroid.note.encrypt.DES;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.NoteActionProgressListener;
import com.cydroid.note.encrypt.NoteItemAttachInfo;
import com.cydroid.note.provider.NoteContract;

import java.io.File;
import java.util.List;

public class LocalNoteItem extends NoteItem {
    public static final String LABEL_SEPARATOR = ",";
    private static final String[] NOTE_PROJECTION = {//NOSONAR
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

    public static final Path ITEM_PATH = Path.fromString(LocalSource.LOCAL_ITEM_PATH);
    private Context mContext;
    private final ContentResolver mResolver;
    private final DES mDES = new DES();

    public LocalNoteItem(Path path, NoteAppImpl application, Cursor cursor) {
        super(path, nextVersionNumber());
        mContext = application;
        mResolver = application.getContentResolver();
        loadFromCursor(cursor);
//        checkDeleteEmpty();
    }

    public LocalNoteItem(Path path, NoteAppImpl application, int id) {
        super(path, nextVersionNumber());
        mContext = application;
        mResolver = application.getContentResolver();
        Uri uri = NoteContract.NoteContent.CONTENT_URI;
        Cursor cursor = LocalNoteSet.getItemCursor(mResolver, uri, NOTE_PROJECTION, id);
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
        title = cursor.getString(INDEX_TITLE);
        content = cursor.getString(INDEX_CONTENT);
        String labels = cursor.getString(INDEX_LABEL);
        convertLabel(labels);
        dateCreatedInMs = cursor.getLong(INDEX_DATE_CREATED);
        dateModifiedInMs = cursor.getLong(INDEX_DATE_MODIFIED);
        dateReminderInMs = cursor.getLong(INDEX_REMINDER);
        encyptHintState = cursor.getInt(INDEX_ENCRYPT_HINT_STATE);
        encrytRemindReadState = cursor.getInt(INDEX_ENCRYPT_REMIND_READ_STATE);
    }

    private void checkDeleteEmpty() {
        if (isEmpty()) {
            try {
                delete();
            } catch (Exception e) {
            }
        }
    }

    private boolean isEmpty() {
        return TextUtils.isEmpty(title) && TextUtils.isEmpty(content)
                && label.isEmpty() && (dateReminderInMs == 0);
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
        title = uh.update(title, cursor.getString(INDEX_TITLE));
        content = uh.update(content, cursor.getString(INDEX_CONTENT));
        String labels = uh.update(label, cursor.getString(INDEX_LABEL));
        convertLabel(labels);
        dateCreatedInMs = uh.update(dateCreatedInMs, cursor.getLong(INDEX_DATE_CREATED));
        dateModifiedInMs = uh.update(dateModifiedInMs, cursor.getLong(INDEX_DATE_MODIFIED));
        dateReminderInMs = uh.update(dateReminderInMs, cursor.getLong(INDEX_REMINDER));
        encyptHintState = uh.update(encyptHintState, cursor.getInt(INDEX_ENCRYPT_HINT_STATE));
        encrytRemindReadState = uh.update(encrytRemindReadState, cursor.getInt(INDEX_ENCRYPT_REMIND_READ_STATE));
        return uh.isUpdated();
    }

    @Override
    public Bitmap requestImage(int mediaType, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String filePath = uri.getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
        }
        Config.NoteCard config = Config.NoteCard.get(mContext);
        ThumbnailDecodeProcess decodeProcess = new ThumbnailDecodeProcess(mContext, uri, config.mImageWidth,
                config.mImageHeight, ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT,
                false);
        return decodeProcess.getThumbnail();
    }

    @Override
    public void delete() throws Exception {
        Uri uri = NoteContract.NoteContent.CONTENT_URI;
        mResolver.delete(uri, "_id=?", new String[]{String.valueOf(id)});
        cancelReminder();
        NoteUtils.deleteOriginMediaFile(content, false);
    }

    @Override
    public void encrypt(NoteActionProgressListener listener) throws Exception {
        EncryptUtil.checkPhotoSpanHasSize(content);
        int secretId = encryptTextContent();
        List<NoteItemAttachInfo> attachInfos = EncryptUtil.getAttachs(content);
        if (PlatformUtil.isSecurityOS()) {
            EncryptUtil.encryptAttachFileForSecurityOS(attachInfos, listener);
        } else {
            EncryptUtil.encryptAttachFile(attachInfos, listener);
        }
        handleRemind(secretId);
    }

    private int encryptTextContent() {
        String encryptTitle = mDES.authcode(title, DES.OPERATION_ENCODE, DES.DES_KEY);
        String encryptContent = mDES.authcode(content, DES.OPERATION_ENCODE, DES.DES_KEY);
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteContent.COLUMN_TITLE, encryptTitle);
        values.put(NoteContract.NoteContent.COLUMN_CONTENT, encryptContent);
        values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, dateModifiedInMs);
        values.put(NoteContract.NoteContent.COLUMN_REMINDER, dateReminderInMs);
        String labels = NoteItem.convertToStringLabel(label);
        values.put(NoteContract.NoteContent.COLUMN_LABEL, labels);
        values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, dateCreatedInMs);
        values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, encryptContent);
        int encrptRemindRead = Constants.ENCRYPT_REMIND_NOT_READ;
        if (dateReminderInMs != NoteItem.INVALID_REMINDER) {
            encrptRemindRead = Constants.ENCRYPT_REMIND_READED;
        }
        values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, encrptRemindRead);

        Uri uri = mResolver.insert(NoteContract.NoteContent.SECRET_CONTENT_URI, values);
        int secretId = (int) ContentUris.parseId(uri);
        mResolver.delete(NoteContract.NoteContent.CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
        return secretId;
    }

    private void handleRemind(int secretId) {
        if (dateReminderInMs != NoteItem.INVALID_REMINDER
                && dateReminderInMs > System.currentTimeMillis()) {
            ReminderManager.cancelAlarmAndNotification(NoteAppImpl.getContext(),
                    id, false);
            ReminderManager.setReminder(NoteAppImpl.getContext(), secretId, dateReminderInMs, true);
        }
    }

    private void cancelReminder() {
        if (dateReminderInMs != INVALID_REMINDER) {
            ReminderManager.cancelAlarmAndNotification(mContext, id, false);
        }
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = NoteContract.NoteContent.CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

}
