package com.cydroid.note.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

import com.cydroid.note.common.PlatformUtil;

public class NoteProvider extends ContentProvider {
    public static final String TAG = "NoteProvider";
    public static final String AUTHORITY;
    public static final String DATABASE_NAME = "note.db";
    private static final int DATABASE_VERSION = 5;
    /*
     * Constants used by the Uri matcher to choose an action based on the pattern of the
     * incoming URI
     */
    private static final int NOTE_ITEMS = 1;

    private static final int LABEL_ITEMS = 2;

    private static final int SECRET_NOTE_ITEMS = 3;

    private static final int TRASH_ITEMS = 4;


    private static final UriMatcher URI_MATCHER;
    private DatabaseHelper mOpenHelper;

    /**
     * A block that instantiates and sets static objects
     */
    static {
        if (PlatformUtil.isGioneeDevice()) {
            AUTHORITY = "com.cydroid.note.provider.NoteProvider";
        } else {
            AUTHORITY = "com.gionee.aminote.provider.NoteProvider";
        }
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        /*
         * Uri matchers for story_content
         */
        URI_MATCHER.addURI(AUTHORITY, "note_items", NOTE_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, "label_items", LABEL_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, "secret_note_items", SECRET_NOTE_ITEMS);
        URI_MATCHER.addURI(AUTHORITY, "trash_items", TRASH_ITEMS);
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch (URI_MATCHER.match(uri)) {
            case NOTE_ITEMS: {
                qb.setTables(NoteContract.NoteContent.TABLE_NAME);
                qb.setProjectionMap(NoteContract.NoteContent.sProjectionMap);
                break;
            }
            case LABEL_ITEMS: {
                qb.setTables(LabelContract.LabelContent.TABLE_NAME);
                qb.setProjectionMap(LabelContract.LabelContent.sProjectionMap);
                break;
            }
            case SECRET_NOTE_ITEMS: {
                qb.setTables(NoteContract.NoteContent.SECRET_TABLE_NAME);
                qb.setProjectionMap(NoteContract.NoteContent.sProjectionMap);
                break;
            }
            case TRASH_ITEMS: {
                qb.setTables(NoteContract.TrashContent.TABLE_NAME);
                qb.setProjectionMap(NoteContract.TrashContent.sProjectionMap);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (!TextUtils.isEmpty(sortOrder)) {
            orderBy = sortOrder;
        }
        String limit = uri.getQueryParameter("limit");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, limit);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTE_ITEMS:
                return NoteContract.NoteContent.CONTENT_TYPE;
            case LABEL_ITEMS:
                return LabelContract.LabelContent.CONTENT_TYPE;
            case SECRET_NOTE_ITEMS:
                return NoteContract.NoteContent.SECRET_CONTENT_TYPE;
            case TRASH_ITEMS:
                return NoteContract.TrashContent.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int match = URI_MATCHER.match(uri);
        String tableName = null;
        Uri noteUri = null;
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        switch (match) {
            case NOTE_ITEMS:
                tableName = NoteContract.NoteContent.TABLE_NAME;
                noteUri = NoteContract.NoteContent.CONTENT_URI;
                break;
            case LABEL_ITEMS:
                tableName = LabelContract.LabelContent.TABLE_NAME;
                noteUri = LabelContract.LabelContent.CONTENT_URI;
                break;
            case SECRET_NOTE_ITEMS:
                tableName = NoteContract.NoteContent.SECRET_TABLE_NAME;
                noteUri = NoteContract.NoteContent.SECRET_CONTENT_URI;
                break;
            case TRASH_ITEMS:
                tableName = NoteContract.TrashContent.TABLE_NAME;
                noteUri = NoteContract.TrashContent.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(tableName, null, values);
        if (rowId > 0) {
            noteUri = ContentUris.withAppendedId(noteUri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        switch (URI_MATCHER.match(uri)) {
            case NOTE_ITEMS:
                count = db.delete(NoteContract.NoteContent.TABLE_NAME, selection, selectionArgs);
                break;
            case LABEL_ITEMS:
                count = db.delete(LabelContract.LabelContent.TABLE_NAME, selection, selectionArgs);
                break;
            case SECRET_NOTE_ITEMS:
                count = db.delete(NoteContract.NoteContent.SECRET_TABLE_NAME, selection, selectionArgs);
                break;
            case TRASH_ITEMS:
                count = db.delete(NoteContract.TrashContent.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case NOTE_ITEMS:
                count = db.update(NoteContract.NoteContent.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LABEL_ITEMS:
                count = db.update(LabelContract.LabelContent.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SECRET_NOTE_ITEMS:
                count = db.update(NoteContract.NoteContent.SECRET_TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRASH_ITEMS:
                count = db.update(NoteContract.TrashContent.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    /**
     * This class helps open, create, and upgrade the database file. Set to package visibility for testing
     * purposes.
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(NoteContract.NoteContent.CREATE_TABLE_SQL);
            db.execSQL(LabelContract.LabelContent.CREATE_TABLE_SQL);
            db.execSQL(NoteContract.NoteContent.CREATE_SECRET_TABLE_SQL);
            db.execSQL(NoteContract.TrashContent.CREATE_TABLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           //GIONEE wanghaiyan 2017-2-9 modify for 68369 begin
           /*
            if (oldVersion <= 3) {
                addIntegerColumn(db, NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE);
                addIntegerColumn(db, NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE);
                db.execSQL(NoteContract.NoteContent.CREATE_SECRET_TABLE_SQL);
                db.execSQL(NoteContract.TrashContent.CREATE_TABLE_SQL);
            }

            if (oldVersion <= 4) {
                addIntegerColumn(db, NoteContract.NoteContent.CLOUMN_ITEM_SOURCE);
            }
            */
			if (newVersion==5){
				addIntegerColumn(db, NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE);
				addIntegerColumn(db, NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE);
				db.execSQL(NoteContract.NoteContent.CREATE_SECRET_TABLE_SQL);
				db.execSQL(NoteContract.TrashContent.CREATE_TABLE_SQL);
			    addIntegerColumn(db, NoteContract.NoteContent.CLOUMN_ITEM_SOURCE);
			}
			//GIONEE wanghaiyan 2017-2-9 modify for 68369 end
        }

        private void addIntegerColumn(SQLiteDatabase db, String columnName) {
            try {
                db.execSQL("ALTER TABLE " + NoteContract.NoteContent.TABLE_NAME + " ADD COLUMN "
                        + columnName + " INTEGER;");
            } catch (Exception e) {
                Log.d(TAG, "onUpgrade addColumn e : " + e);
            }
        }
    }
}
