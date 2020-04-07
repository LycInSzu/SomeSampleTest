package com.cydroid.note.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;

public class NoteContract {
    private static final String SCHEME = "content://";
    public static final String AUTHORITY = NoteProvider.AUTHORITY;
    private static final String AUTH_SCHEMA = SCHEME + AUTHORITY;

    private static void doMap(HashMap<String, String> map, String column) {
        map.put(column, column);
    }

    public static final class NoteContent implements BaseColumns {
        public static final HashMap<String, String> sProjectionMap = new HashMap<>();
        public static final String TABLE_NAME = "note_item";
        public static final String SECRET_TABLE_NAME = "secret_note_item";
        /*
         * URI definitions
         */
        public static final String PATH_NOTE_ITEMS = "/note_items";
        public static final String PATH_SECRET_NOTE_ITEMS = "/secret_note_items";
        /*
         * MIME type definitions
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.note_item";
        public static final String SECRET_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.secret_note_item";
        public static final Uri CONTENT_URI = Uri.parse(AUTH_SCHEMA + PATH_NOTE_ITEMS);
        public static final Uri SECRET_CONTENT_URI = Uri.parse(AUTH_SCHEMA + PATH_SECRET_NOTE_ITEMS);
        /*
         * Column definitions
         */
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_DATE_CREATED = "date_created";
        public static final String COLUMN_DATE_MODIFIED = "date_modified";
        public static final String COLUMN_REMINDER = "reminder";
        public static final String COLUMN_ENCRYPT_HINT_STATE = "encypt_hint_state";
        public static final String CLOUMN_ENCRYPT_REMIND_READ_STATE = "encrypt_remind_read_state";
        public static final String CLOUMN_ITEM_SOURCE = "source";
	    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
        public static final String COLUMN_NOTE_SAVEDAS_IMAGE_PATH ="noteSavedAsImagePath";
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

        static {
            doMap(sProjectionMap, _ID);
            doMap(sProjectionMap, COLUMN_TITLE);
            doMap(sProjectionMap, COLUMN_CONTENT);
            doMap(sProjectionMap, COLUMN_LABEL);
            doMap(sProjectionMap, COLUMN_DATE_CREATED);
            doMap(sProjectionMap, COLUMN_DATE_MODIFIED);
            doMap(sProjectionMap, COLUMN_REMINDER);
            doMap(sProjectionMap, COLUMN_ENCRYPT_HINT_STATE);
            doMap(sProjectionMap, CLOUMN_ENCRYPT_REMIND_READ_STATE);
            doMap(sProjectionMap, CLOUMN_ITEM_SOURCE);
	        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
            doMap(sProjectionMap, COLUMN_NOTE_SAVEDAS_IMAGE_PATH);
            //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

            CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_TITLE + " TEXT, "
                    + COLUMN_CONTENT + " TEXT, "
                    + COLUMN_LABEL + " TEXT, "
                    + COLUMN_DATE_CREATED + " INTEGER, "
                    + COLUMN_DATE_MODIFIED + " INTEGER, "
                    + COLUMN_REMINDER + " INTEGER, "
                    + COLUMN_ENCRYPT_HINT_STATE + " INTEGER, "
                    + CLOUMN_ENCRYPT_REMIND_READ_STATE + " INTEGER, "
                    + CLOUMN_ITEM_SOURCE + " INTEGER, "
                    + COLUMN_NOTE_SAVEDAS_IMAGE_PATH + " TEXT"
                    + ");";

            CREATE_SECRET_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + SECRET_TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_TITLE + " TEXT, "
                    + COLUMN_CONTENT + " TEXT, "
                    + COLUMN_LABEL + " TEXT, "
                    + COLUMN_DATE_CREATED + " INTEGER, "
                    + COLUMN_DATE_MODIFIED + " INTEGER, "
                    + COLUMN_REMINDER + " INTEGER, "
                    + COLUMN_ENCRYPT_HINT_STATE + " INTEGER, "
                    + CLOUMN_ENCRYPT_REMIND_READ_STATE + " INTEGER, "
                    + CLOUMN_ITEM_SOURCE + " INTEGER, "
                    + COLUMN_NOTE_SAVEDAS_IMAGE_PATH + " TEXT"
                    + ");";
        }

        public static final String CREATE_TABLE_SQL;

        public static final String CREATE_SECRET_TABLE_SQL;
    }


    public static final class TrashContent implements BaseColumns {
        public static final HashMap<String, String> sProjectionMap = new HashMap<>();
        public static final String TABLE_NAME = "trash_item";
        /*
         * URI definitions
         */
        public static final String PATH_TRASH_ITEMS = "/trash_items";
        /*
         * MIME type definitions
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.trash_item";
        public static final Uri CONTENT_URI = Uri.parse(AUTH_SCHEMA + PATH_TRASH_ITEMS);
        /*
         * Column definitions
         */
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_DATE_CREATED = "date_created";
        public static final String COLUMN_DATE_MODIFIED = "date_modified";
        public static final String COLUMN_DATE_DELETED = "date_deleted";
        public static final String COLUMN_REMINDER = "reminder";
        public static final String COLUMN_ENCRYPT_HINT_STATE = "encypt_hint_state";
        public static final String CLOUMN_ENCRYPT_REMIND_READ_STATE = "encrypt_remind_read_state";
        public static final String CLOUMN_ITEM_SOURCE = "source";
	    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
        public static final String COLUMN_NOTE_SAVEDAS_IMAGE_PATH ="noteSavedAsImagePath";
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883)end


        static {
            doMap(sProjectionMap, _ID);
            doMap(sProjectionMap, COLUMN_TITLE);
            doMap(sProjectionMap, COLUMN_CONTENT);
            doMap(sProjectionMap, COLUMN_LABEL);
            doMap(sProjectionMap, COLUMN_DATE_CREATED);
            doMap(sProjectionMap, COLUMN_DATE_MODIFIED);
            doMap(sProjectionMap, COLUMN_DATE_DELETED);
            doMap(sProjectionMap, COLUMN_REMINDER);
            doMap(sProjectionMap, COLUMN_ENCRYPT_HINT_STATE);
            doMap(sProjectionMap, CLOUMN_ENCRYPT_REMIND_READ_STATE);
	        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
            doMap(sProjectionMap, COLUMN_NOTE_SAVEDAS_IMAGE_PATH);
            //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

            CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_TITLE + " TEXT, "
                    + COLUMN_CONTENT + " TEXT, "
                    + COLUMN_LABEL + " TEXT, "
                    + COLUMN_DATE_CREATED + " INTEGER, "
                    + COLUMN_DATE_MODIFIED + " INTEGER, "
                    + COLUMN_DATE_DELETED + " INTEGER, "
                    + COLUMN_REMINDER + " INTEGER, "
                    + COLUMN_ENCRYPT_HINT_STATE + " INTEGER, "
                    + CLOUMN_ENCRYPT_REMIND_READ_STATE + " INTEGER, "
                    + CLOUMN_ITEM_SOURCE + " INTEGER, "
                    + COLUMN_NOTE_SAVEDAS_IMAGE_PATH + " TEXT"
                    + ");";
        }

        public static final String CREATE_TABLE_SQL;
    }

}
