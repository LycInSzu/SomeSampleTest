package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.Countdown;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.ArrayList;

public class CountdownActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimeTrickerInterface {

    @BindView(R.id.countdown_recyclerView)
    RecyclerView countdownRecyclerView;


    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<CountDownBean> countdownItemLits = new ArrayList<>();
    private CountdownListAdapter adapter;
    private long startTime;
    private long endTime;
    private MyCountDownTimer myCountDownTimer;
    private boolean getDataSuccess=false;

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

//        countdownItemLits.add(new CountDownBean(120,2));
//        countdownItemLits.add(new CountDownBean(120,2));
//        countdownItemLits.add(new CountDownBean(120,2));


        adapter = new CountdownListAdapter(countdownItemLits);
        countdownRecyclerView.setAdapter(adapter);

        myCountDownTimer = new MyCountDownTimer(1000 * 60 * 60 * 34 * 10, 1000, this);

        queryAllCountdownFromDB(countdownItemLits);
        myCountDownTimer.start();
    }

    private void queryAllCountdownFromDB(ArrayList<CountDownBean> countdownItemLits) {
        countdownItemLits.clear();
        LifeBalanceDatabaseHelper databaseHelper = new LifeBalanceDatabaseHelper(CountdownActivity.this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query("countdown", new String[]{"name", "isFinished", "resttime"}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            boolean isFinished = cursor.getInt(cursor.getColumnIndex("isFinished")) == 1;
            long durition = cursor.getLong(cursor.getColumnIndex("resttime"));
            Log.d(TAG, "  cursor content name is " + name + "   durition is " + durition);
            CountDownBean bean = new CountDownBean(name, isFinished, durition);
            Log.d(TAG, "  new  CountDownBean is " + bean);
            countdownItemLits.add(bean);
        }
        db.close();
        getDataSuccess=true;
        adapter.notifyDataSetChanged();
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


    private long getCurrentTimeMIllins() {
        return System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCountdownTimer();
    }

    private void startCountdownTimer() {
        //TODO: getdata from database
        startTime = getCurrentTimeMIllins();

        //TODO: calculate period between and update data and start countdowntimer

        //TODO: show in recycle view
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelCountdownTimer();
        updateDatabase(countdownItemLits);

    }

    private void updateDatabase(ArrayList<CountDownBean> countdownItemLits) {
        LifeBalanceDatabaseHelper databaseHelper = new LifeBalanceDatabaseHelper(CountdownActivity.this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (int i = 0; i < countdownItemLits.size(); i++) {
            CountDownBean bean = countdownItemLits.get(i);
            if (bean.isFinished()||(bean.getDurition() < 1000)){
                String sql = "update countdown set isFinished = 1 where name = '"+bean.getName()+"'";
                db.execSQL(sql);
                continue;
            } else {
                String sql = "update countdown set resttime ="+bean.getDurition()+" where name = '"+bean.getName()+"'";
                db.execSQL(sql);
            }
        }
        db.close();
    }

    private void cancelCountdownTimer() {
        //TODO: cancel  and  update database
        endTime = getCurrentTimeMIllins();
        myCountDownTimer.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    queryAllCountdownFromDB(countdownItemLits);
                } else {

                }
                break;
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (!getDataSuccess){
            return;
        }
        for (int i = 0; i < countdownItemLits.size(); i++) {
            CountDownBean bean = countdownItemLits.get(i);
            if (bean.isFinished()){
                continue;
            }
            if (bean.getDurition() - 1000 > 1000) {
                bean.setDurition(bean.getDurition() - 1000);
            } else {
                bean.setFinished(true);
            }

        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFinish() {

    }
}
