package com.cydroid.note.trash.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.DateUtils;

import com.cydroid.note.app.Config;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.common.UpdateHelper;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.Path;
import com.cydroid.note.provider.NoteContract;

import java.io.File;

/**
 * Created by xiaozhilong on 7/1/16.
 */
public class TrashNoteItem extends NoteItem {
    public static final int KEEP_DAYS = 7;
    public static final long KEEP_DAYS_IN_MILLIS = KEEP_DAYS * DateUtils.DAY_IN_MILLIS;
    public static final String LABEL_SEPARATOR = ",";
    private static final String[] NOTE_PROJECTION = { //NOSONAR
            NoteContract.TrashContent._ID, //NOSONAR
            NoteContract.TrashContent.COLUMN_TITLE, //NOSONAR
            NoteContract.TrashContent.COLUMN_CONTENT, //NOSONAR
            NoteContract.TrashContent.COLUMN_LABEL, //NOSONAR
            NoteContract.TrashContent.COLUMN_DATE_CREATED, //NOSONAR
            NoteContract.TrashContent.COLUMN_DATE_MODIFIED, //NOSONAR
            NoteContract.TrashContent.COLUMN_DATE_DELETED, //NOSONAR
            NoteContract.TrashContent.COLUMN_REMINDER, //NOSONAR
            NoteContract.TrashContent.COLUMN_ENCRYPT_HINT_STATE, //NOSONAR
            NoteContract.TrashContent.CLOUMN_ENCRYPT_REMIND_READ_STATE}; //NOSONAR
    public static final int INDEX_ID = 0;
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_CONTENT = 2;
    public static final int INDEX_LABEL = 3;
    public static final int INDEX_DATE_CREATED = 4;
    public static final int INDEX_DATE_MODIFIED = 5;
    public static final int INDEX_DATE_DELETED = 6;
    public static final int INDEX_REMINDER = 7;
    public static final int INDEX_ENCRYPT_HINT_STATE = 8;
    public static final int INDEX_ENCRYPT_REMIND_READ_STATE = 9;

    public static final Path ITEM_PATH = Path.fromString(TrashSource.TRASH_ITEM_PATH);
    private Context mContext;
    private final ContentResolver mResolver;

    public long dateDeletedInMs;

    public TrashNoteItem(Path path, NoteAppImpl application, Cursor cursor) {
        super(path, nextVersionNumber());
        mContext = application;
        mResolver = application.getContentResolver();
        loadFromCursor(cursor);
    }

    public TrashNoteItem(Path path, NoteAppImpl application, int id) {
        super(path, nextVersionNumber());
        mContext = application;
        mResolver = application.getContentResolver();
        Uri uri = NoteContract.TrashContent.CONTENT_URI;
        Cursor cursor = TrashNoteSet.getItemCursor(mResolver, uri, NOTE_PROJECTION, id);
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
        dateDeletedInMs = cursor.getLong(INDEX_DATE_DELETED);
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
        title = uh.update(title, cursor.getString(INDEX_TITLE));
        content = uh.update(content, cursor.getString(INDEX_CONTENT));
        String labels = uh.update(label, cursor.getString(INDEX_LABEL));
        convertLabel(labels);
        dateCreatedInMs = uh.update(dateCreatedInMs, cursor.getLong(INDEX_DATE_CREATED));
        dateModifiedInMs = uh.update(dateModifiedInMs, cursor.getLong(INDEX_DATE_MODIFIED));
        dateDeletedInMs = uh.update(dateDeletedInMs, cursor.getLong(INDEX_DATE_DELETED));
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
        Uri uri = NoteContract.TrashContent.CONTENT_URI;
        mResolver.delete(uri, "_id=?", new String[]{String.valueOf(id)});
        NoteUtils.deleteOriginMediaFile(content, false);
    }

    @Override
    public Uri getContentUri() {
        Uri baseUri = NoteContract.TrashContent.CONTENT_URI;
        return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    public long getDateTimeDeleted() {
        return dateDeletedInMs;
    }
}
