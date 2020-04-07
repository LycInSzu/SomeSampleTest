package com.cydroid.note.app.inputbackup;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cydroid.note.common.Log;

import com.cydroid.note.common.NoteUtils;

import java.io.File;

class ImportDBHelp {
    private static final String TAG = "ImportDBHelp";
    private static final String TABLE_NAME = "input";
    private static final String DB_NAME = "temp_save.db";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_CONTENT = "content";
    static final String COLUMN_LABEL = "label";

    private SQLiteDatabase mDb;

    public ImportDBHelp() {
        File dirFile = ImportBackUp.sTempSaveFile;
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                Log.i(TAG, "ImportDBHelp construct make dirs failure!!!!");
            }
        }
        final File dbFile = new File(ImportBackUp.sTempSaveFile, DB_NAME);
        mDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (_id INTEGER PRIMARY KEY," +
                "title TEXT,content TEXT,label TEXT)");
    }

    public long insert(ContentValues values) {
        return mDb.insert(TABLE_NAME, null, values);
    }

    public Cursor query(String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return mDb.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public int delete(String whereClause, String[] whereArgs) {
        return mDb.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public void close() {
        NoteUtils.closeSilently(mDb);
    }
}
