package com.cydroid.note.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

public abstract class NoteItem extends NoteObject {
    public static final int INVALID_REMINDER = 0;
    public static final int THUMBNAIL_TYPE_HOME = 1;
    public static final int THUMBNAIL_TYPE_EDIT = 2;

    public static final int MEDIA_TYPE_NONE = -1;
    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;

    public static final int INVALID_ID = -1;
    public static final int DEFAULT_DATE_SOURCE = 3;
    public static final int USER_DATE_SOURCE = 4;

    // database fields
    public int id;
    public String title;
    public String content;
    public ArrayList<Integer> label = new ArrayList<>();
    public long dateCreatedInMs;
    public long dateModifiedInMs;
    public long dateReminderInMs;
    public int encyptHintState;
    public int encrytRemindReadState;
    public boolean isEncrypted;
    public int mDataSource;

    public NoteItem(Path path, long version) {
        super(path, version);
    }

    public int getId() {
        return id;
    }

    public int getSource() {
        return mDataSource;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public ArrayList<Integer> getLabel() {
        return label;
    }

    public long getDateTimeCreated() {
        return dateCreatedInMs;
    }

    public long getDateTimeModified() {
        return dateModifiedInMs;
    }

    public long getDateTimeReminder() {
        return dateReminderInMs;
    }

    public int getEncyptHintState() {
        return encyptHintState;
    }

    public int getEncrytRemindReadState() {
        return encrytRemindReadState;
    }

    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    public abstract Bitmap requestImage(int mediaType, Uri uri);

    public static String convertToStringLabel(ArrayList<Integer> arrayLabel) {
        StringBuilder updateBuilder = new StringBuilder();
        int length = arrayLabel.size();
        for (int i = 0; i < length; i++) {
            updateBuilder.append(arrayLabel.get(i));
            if (i != length - 1) {
                updateBuilder.append(LocalNoteItem.LABEL_SEPARATOR);
            }
        }
        if (updateBuilder.length() == 0) {
            return null;
        }
        return updateBuilder.toString();
    }

    abstract protected boolean updateFromCursor(Cursor cursor);

    public void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            mDataVersion = nextVersionNumber();  //NOSONAR
        }
    }
}
