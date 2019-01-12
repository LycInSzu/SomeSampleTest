package com.lyc.newtestapplication.newtestapplication.LifeBalance.UI.Countdown;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.lyc.newtestapplication.newtestapplication.BaseActivity;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans.CountDownBean;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.DatabaseHelper.LifeBalanceDatabaseHelper;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils.MyTimeUtil;
import com.lyc.newtestapplication.newtestapplication.R;
import com.lyc.newtestapplication.newtestapplication.R2;

public class AddNewCountdownActivity extends BaseActivity {


    @BindView(R2.id.countdownname)
    EditText countdownname;
    @BindView(R2.id.countdown_days)
    EditText countdownDays;
    @BindView(R2.id.countdown_hours)
    EditText countdownHours;
    @BindView(R2.id.countdown_minutes)
    EditText countdownMinutes;
    @BindView(R2.id.countdown_seconds)
    EditText countdownSeconds;
    @BindView(R2.id.checkBox_importantcountdown)
    CheckBox checkBoxImportantcountdown;
    @BindView(R2.id.countdown_submit)
    Button countdownSubmit;

    private long submitTime;

    @Override
    public Class getCurrentActivityName() {
        return AddNewCountdownActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_countdown);
        ButterKnife.bind(this);
        countdownSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTime = System.currentTimeMillis();
                String name = countdownname.getText().toString().trim();
                int days = Integer.parseInt(countdownDays.getText().toString());
                int hours = Integer.parseInt(countdownHours.getText().toString());
                int minutes = Integer.parseInt(countdownMinutes.getText().toString());
                int seconds = Integer.parseInt(countdownSeconds.getText().toString());

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(AddNewCountdownActivity.this, "name can not be null!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0) {
                    Toast.makeText(AddNewCountdownActivity.this, "please ensure your time plan!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (days < 0) {
                    Toast.makeText(AddNewCountdownActivity.this, "days can not small than zero!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hours < 0) {
                    Toast.makeText(AddNewCountdownActivity.this, "days can not small than zero!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (minutes < 0) {
                    Toast.makeText(AddNewCountdownActivity.this, "days can not small than zero!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (seconds < 0) {
                    Toast.makeText(AddNewCountdownActivity.this, "days can not small than zero!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                addNewCountDownBean(name, days, hours, minutes, seconds);
                AddNewCountdownActivity.this.finish();
            }
        });
    }

    private void addNewCountDownBean(String name, int days, int hours, int minutes, int seconds) {

        long durition = MyTimeUtil.convertUserDateToMilliSeconds(days, hours, minutes, seconds);
        long allTime=durition+submitTime;
        Log.d(TAG," the durition is  "+durition+"  and the submitTime is "+submitTime+"   and plus is "+allTime);
        String endtime = MyTimeUtil.convertToDate(allTime);
        Log.d(TAG," the end date is set as "+endtime+"  and now is "+MyTimeUtil.convertToDate(submitTime));

        CountDownBean countDownBean = new CountDownBean(name, false, endtime, durition);
        addToDatabase(countDownBean);
        Intent intent=new Intent();
        Bundle bundle=new Bundle();
        bundle.putParcelable("newItem",countDownBean);
        intent.putExtra("data",bundle);

        setResult(RESULT_OK, intent);

    }

    private void addToDatabase(CountDownBean countDownBean) {
        LifeBalanceDatabaseHelper databaseHelper = new LifeBalanceDatabaseHelper(AddNewCountdownActivity.this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues cValue = new ContentValues();
        cValue.put("name", countDownBean.getName());
        cValue.put("endTime", countDownBean.getEntTime());
//        cValue.put("resttime", countDownBean.getDurition());
        cValue.put("isFinished", countDownBean.isFinished() ? 1 : 0);
        db.insert("countdown", null, cValue);
        db.close();
    }

}
