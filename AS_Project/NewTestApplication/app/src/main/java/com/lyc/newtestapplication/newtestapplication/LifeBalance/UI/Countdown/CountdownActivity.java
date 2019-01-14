package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.Countdown;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.lyc.newtestapplication.newtestapplication.BaseActivity;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Adapter.CountdownListAdapter;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.CountDownBean;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.MyCountDownTimer;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.DatabaseHelper.LifeBalanceDatabaseHelper;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.TimeTrickerInterface;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils.MyTimeUtil;
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.ArrayList;

public class CountdownActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimeTrickerInterface {

    @BindView(R.id.countdown_recyclerView)
    RecyclerView countdownRecyclerView;


    private RecyclerView.LayoutManager layoutManager;
    private final ArrayList<CountDownBean> countdownItemLits = new ArrayList<>();
    private static CountdownListAdapter adapter;
    //    private long endTime;
    private MyCountDownTimer myCountDownTimer;
    private static boolean getDataSuccess = false;

    private static final int QUERYDATA_FROMDATABASE_OK = 1;

    private static Handler countDownHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERYDATA_FROMDATABASE_OK:
                    Log.d("countDownHandler", "---------QUERYDATA_FROMDATABASE_OK-------");
                    getDataSuccess = true;
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public Class getCurrentActivityName() {
        return CountdownActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.CountdownTitle);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add new plan", Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                Bundle bundle=new Bundle();
//                                bundle.putParcelableArrayList();
//                                startDetermindActivity(AddNewCountdownActivity.class,null);
                                startDetermindActivityForResult(AddNewCountdownActivity.class, null, null, 1);

                            }
                        }).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        layoutManager = new LinearLayoutManager(CountdownActivity.this, RecyclerView.VERTICAL, false);
        countdownRecyclerView.setLayoutManager(layoutManager);
        adapter = new CountdownListAdapter(countdownItemLits);
        countdownRecyclerView.setAdapter(adapter);
        myCountDownTimer = new MyCountDownTimer(1000 * 60 * 60 * 34 * 10, 1000, this);

        queryAllCountdownFromDB();
        startCountdownTimer();
    }

//    private void getEndTime() {
//        SharedPreferences countdownSharedPreferences = getSharedPreferences("countdown", 0);
//        endTime = countdownSharedPreferences.getLong("endTime", 0);
//    }

    private void queryAllCountdownFromDB() {
        Log.d(TAG, "--------------queryAllCountdownFromDB-------------------");
        new Thread(new Runnable() {
            @Override
            public void run() {
                countdownItemLits.clear();
                LifeBalanceDatabaseHelper databaseHelper = LifeBalanceDatabaseHelper.getInstance(CountdownActivity.this);
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("countdown", new String[]{"name", "endTime", "isFinished"}, null, null, null, null, null);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String endTime = cursor.getString(cursor.getColumnIndex("endTime"));
//                    long durition = MyTimeUtil.convertToMilliSeconds(endTime) - startTime;
                    boolean isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) == 1;
//                    Log.d(TAG, "  cursor content name is " + name + "   startcountdownTime is " + startTime + "  and the date is " + MyTimeUtil.convertToDate(startTime) + ";   endTime is " + endTime + "   durition is " + durition);
                    CountDownBean bean = new CountDownBean(name, isFinished, endTime);
                    Log.d(TAG, "  new  CountDownBean is " + bean);
                    countdownItemLits.add(bean);
                }
                cursor.close();
                db.close();
                countDownHandler.sendEmptyMessage(QUERYDATA_FROMDATABASE_OK);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.countdown, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            // Handle the camera action
        } else if (id == R.id.nav_import) {

        } else if (id == R.id.nav_year) {

        } else if (id == R.id.nav_month) {

        } else if (id == R.id.nav_self) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCountdownTimer() {
        myCountDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCountdownTimer();
        updateDatabase();
    }

    private void updateDatabase() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                LifeBalanceDatabaseHelper databaseHelper = LifeBalanceDatabaseHelper.getInstance(CountdownActivity.this);
                SQLiteDatabase db = databaseHelper.getWritableLifeBalanceDatabase();
                String sql = "update countdown set isFinished = 1 where name = ?";
                SQLiteStatement sqLiteStatement = db.compileStatement(sql);
                for (int i = 0; i < countdownItemLits.size(); i++) {
                    CountDownBean bean = countdownItemLits.get(i);

                    if (bean.isFinished() || (bean.getDurition() < 1000)) {
                        sqLiteStatement.clearBindings();
                        sqLiteStatement.bindString(1, bean.getName());
                        sqLiteStatement.execute();
//                String sql = "update countdown set isFinished = 1 where name = '" + bean.getName() + "'";
//                db.execSQL(sql);
                    }
                }
                sqLiteStatement.close();
                databaseHelper.closeWritableLifeBalanceDatabase();
            }
        }).start();

    }

    private void cancelCountdownTimer() {
        //TODO: cancel  and  update database
//        endTime = getCurrentTimeMIllins();
//        SharedPreferences countdownSharedPreferences = getSharedPreferences("countdown", 0);
//        SharedPreferences.Editor editor = countdownSharedPreferences.edit();
//        editor.putLong("endTime", endTime);
//        editor.apply();
        myCountDownTimer.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
//                    queryAllCountdownFromDB(countdownItemLits);
                    Bundle bundle = data.getBundleExtra("data");
                    CountDownBean bean = bundle.getParcelable("newItem");
                    countdownItemLits.add(bean);
                    adapter.notifyDataSetChanged();
                } else {

                }
                break;
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Log.d(TAG, "  ---OnTrick run");
        if (!getDataSuccess) {
            return;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFinish() {

    }
}
