package com.pri.factorytest.Voltmeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.util.Scanner;

public class Voltmeter extends PrizeBaseActivity {
    private TextView voltmeterTextView;
    private TextView voltmeterErrorTextView;
    private int voltmeterError;
    private int currentVoltage;
    private int voltmeterVoltage;
    private static final int MIN_CURRENT_VOLTAGE = 3000;//mv

    private File mVoltmeterNowPath = new File(
            "sys/class/power_supply/cw-bat/voltage_now");

    private int scanChargingCurrent() {
        try {
            Scanner scan = new Scanner(mVoltmeterNowPath);
            int val = scan.nextInt();
            scan.close();
            return val;
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voltmeter);
        voltmeterTextView = (TextView) findViewById(R.id.voltage);
        voltmeterErrorTextView = (TextView) findViewById(R.id.voltmeter_error);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent paramIntent) {
            paramIntent.getBooleanExtra("present", false);
            paramIntent.getIntExtra("icon-small", 0);
            currentVoltage = paramIntent.getIntExtra("voltage", 0);
            voltmeterTextView.setText(getString(R.string.voltage) + " : " + currentVoltage + "mV");
            if (currentVoltage > MIN_CURRENT_VOLTAGE) {
                mButtonPass.setEnabled(true);
            }
            doPass2NextTest();
            /*voltmeterVoltage = scanChargingCurrent() / 1000;
            voltmeterError = voltmeterVoltage - currentVoltage;
            if (voltmeterError >= -100 && voltmeterError <= 100) {
                voltmeterErrorTextView.setText(getString(R.string.voltmeter_error) + " : " + "PASS");
            } else {
                voltmeterErrorTextView.setText(getString(R.string.voltmeter_error) + " : " + "FAIL");
                mButtonPass.setEnabled(false);
            }*/
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mBroadcastReceiver, localIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}