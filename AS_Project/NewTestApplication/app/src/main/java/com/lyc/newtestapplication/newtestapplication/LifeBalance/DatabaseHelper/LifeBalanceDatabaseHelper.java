package com.lyc.newtestapplication.newtestapplication.LifeBalance.DatabaseHelper;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LifeBalanceDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "LifeBalanceDatabaseHelper";
    private static final String DATABASE_NAME = "LifeBalanceDatabase.db";

    private static final int DATABASE_VERSION = 1;
    private int writabeConnectionCount = 0;

    private Context mContext;

    private static final String TABLE_COUNTDOWN = "countdown";
    private static final String TABLE_PAY = "pay";
    private static final String TABLE_INCOME = "income";
    private static LifeBalanceDatabaseHelper dbHelper;
    private static SQLiteDatabase writableDatabase;

    public LifeBalanceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static LifeBalanceDatabaseHelper getInstance(Context context) {
        if (dbHelper == null) {
            synchronized (LifeBalanceDatabaseHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new LifeBalanceDatabaseHelper(context);
                }
            }
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCountDownTable(db);

    }

    //-------------------------------------------------------对此数据库的写入操作 打开和关闭，必须使用如下两个方法-----------------------------------------------------------
    //因为SQLite要求，同时只能有一个写入连接，但对读取没有限制。由于我们这里使用了LifeBalanceDatabaseHelper单例,且SQLite要求wirtableDatabase也是单例，所以，这里对写入做限制：防止：
    //java.lang.IllegalStateException: attempt to re-open an already-closed object异常
    public SQLiteDatabase getWritableLifeBalanceDatabase() {
        if (writableDatabase == null) {
            synchronized(LifeBalanceDatabaseHelper.class){
                if (writableDatabase == null){
                    writableDatabase = dbHelper.getWritableDatabase();//当磁盘已经满了时，getWritableDatabase会抛异常
                }
            }
        }
        writabeConnectionCount++;
        return writableDatabase;
    }

    public synchronized void closeWritableLifeBalanceDatabase() {
        writabeConnectionCount--;
        if (writabeConnectionCount < 1) {
            if (writableDatabase.isOpen()) {
                writableDatabase.close();
                writableDatabase=null;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void createCountDownTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_COUNTDOWN + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "endTime TEXT ," +
                "isFinished INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
