package com.android.systemui.statusbar.notification.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import com.android.systemui.R;

public class DKNotificationPackageDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DKNotificationPackageDatabaseHelper";
    private static final String DATABASE_NAME = "DKNotificationPackageDatabase.db";

    private static final int DATABASE_VERSION = 1;
    private int writabeConnectionCount = 0;

    private Context mContext;

    public static final String TABLE_NOTIFICATION_PACKAGE_IM = "notificationpackageimportance";
    private static DKNotificationPackageDatabaseHelper dbHelper;
    private static SQLiteDatabase writableDatabase;

    public DKNotificationPackageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DKNotificationPackageDatabaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            synchronized (DKNotificationPackageDatabaseHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new DKNotificationPackageDatabaseHelper(context);
                }
            }
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCountDownTable(db);
        initDatabase();
    }

    public SQLiteDatabase getWritableLifeBalanceDatabase() {
        if (writableDatabase == null) {
            synchronized (DKNotificationPackageDatabaseHelper.class) {
                if (writableDatabase == null) {
                    writableDatabase = dbHelper.getWritableDatabase();
                }
            }
        }
        synchronized (DKNotificationPackageDatabaseHelper.class) {
            writabeConnectionCount++;
        }
        return writableDatabase;
    }

    public synchronized void closeWritableLifeBalanceDatabase() {
        writabeConnectionCount--;
        if (writabeConnectionCount < 1) {
            if (writableDatabase.isOpen()) {
                writableDatabase.close();
                writableDatabase = null;
            }
        }
    }

    private void createCountDownTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION_PACKAGE_IM + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "packagename TEXT," +
                "important INTEGER" +//0:high  1:low
                ");");
    }

    private void initDatabase() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] dkImportantPackages = mContext.getResources().getStringArray(R.array.DK_important_notification_packages);
                List<String> dkImportantPackagesList = Arrays.asList(dkImportantPackages);
                DKNotificationPackageDatabaseHelper writabledatabaseHelper = DKNotificationPackageDatabaseHelper.getInstance(mContext);
                SQLiteDatabase wdb = writabledatabaseHelper.getWritableLifeBalanceDatabase();
                for (int i = 0; i < dkImportantPackagesList.size(); i++) {
                    ContentValues cValue = new ContentValues();
                    cValue.put("packagename", dkImportantPackagesList.get(i));
                    cValue.put("important", 0);
                    wdb.insert(DKNotificationPackageDatabaseHelper.TABLE_NOTIFICATION_PACKAGE_IM, null, cValue);
                }
                writabledatabaseHelper.closeWritableLifeBalanceDatabase();
            }
        }).start();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
