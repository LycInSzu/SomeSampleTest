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

    private Context mContext;

    private static final String TABLE_COUNTDOWN = "countdown";
    private static final String TABLE_PAY = "pay";
    private static final String TABLE_INCOME = "income";

    public LifeBalanceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCountDownTable(db);

    }

    private void createCountDownTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_COUNTDOWN+" (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "isFinished INTEGER ," +
                "resttime INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
