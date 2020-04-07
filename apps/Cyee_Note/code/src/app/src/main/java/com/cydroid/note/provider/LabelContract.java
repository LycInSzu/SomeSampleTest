package com.cydroid.note.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;

public class LabelContract {
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = NoteProvider.AUTHORITY;
    public static final String AUTH_SCHEMA = SCHEME + AUTHORITY;

    private static void doMap(HashMap<String, String> map, String column) {
        map.put(column, column);
    }

    public static final class LabelContent implements BaseColumns {
        public static final HashMap<String, String> sProjectionMap = new HashMap<>();
        public static final String TABLE_NAME = "label_item";
        /*
         * URI definitions
         */
        public static final String PATH_NOTE_ITEMS = "/label_items";
        public static final String PATH_NOTE_ITEM = "/label_items/";
        /*
         * MIME type definitions
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.label_item";
        public static final Uri CONTENT_URI = Uri.parse(AUTH_SCHEMA + PATH_NOTE_ITEMS);
        /*
         * Column definitions
         */
        public static final String COLUMN_LABEL_CONTENT = "content";

        static {
            doMap(sProjectionMap, _ID);
            doMap(sProjectionMap, COLUMN_LABEL_CONTENT);

            CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LABEL_CONTENT + " TEXT" + ");";
        }

        public static final String CREATE_TABLE_SQL;
    }
}
