package com.pri.factorytest.WirelessCharger;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class WirelessCharger extends PrizeBaseActivity {

    String TAG = "WirelessCharger";

    final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    final String STATUS = "/sys/class/power_supply/battery/status";
    final String CHARGER_TYPE = "/sys/bus/platform/devices/battery/charger_type";
    private TextView chargesate;
    private TextView currentelectricity;
    private TextView chargermax;
    private TextView batterytemperature;
    private TextView batterytype;
    private TextView currentvoltage;
    private TextView chargercurrent;

    Handler mhandle;
    Timer mTimer;

    private File mChargingCurrentPath = new File(
            "/sys/devices/platform/battery/charging_current_value");

    private int scanChargingCurrent() {
        try {
            Scanner scan = new Scanner(mChargingCurrentPath);
            int val = scan.nextInt();
            scan.close();
            return val;
        } catch (Exception e) {
        }
        return 0;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent paramIntent) {
            int level;
            int scale;
            int voltage;
            int temperature;
            String technology;
            String state;

            paramIntent.getBooleanExtra("present", false);
            paramIntent.getIntExtra("icon-small", 0);

            state = getBatteryState(STATUS);
            if ("Not charging".equals(state)) {
                state = getString(R.string.not_charging);
            } else if ("Charging".equals(state)) {
                state = getString(R.string.charging);
            } else if ("Full".equals(state)) {
                state = getString(R.string.charging_finish);
            }
            level = paramIntent.getIntExtra("level", 0);
            scale = paramIntent.getIntExtra("scale", 0);
            temperature = paramIntent.getIntExtra("temperature", 0);
            technology = paramIntent.getStringExtra("technology");
            voltage = paramIntent.getIntExtra("voltage", 0);

            chargesate.setText(getString(R.string.charger_state) + " : "
                    + state);
            currentelectricity.setText(getString(R.string.current_electricity)
                    + " : " + String.valueOf(level) + "%");
            chargermax.setText(getString(R.string.charger_max) + " : "
                    + String.valueOf(scale) + "%");
            batterytemperature.setText(getString(R.string.battery_temperature)
                    + " : " + String.valueOf(temperature / 10.0D) + " "
                    + getResources().getString(R.string.degrees_centigrade));
            batterytype.setText(getString(R.string.battery_type) + " : "
                    + technology);
            currentvoltage.setText(getString(R.string.current_voltage) + " : "
                    + String.valueOf(voltage) + "mV");

            return;
        }
    };
    private String result = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        LinearLayout ChargerLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.wireless_charger, null);
        setContentView(ChargerLayout);

        chargesate = ((TextView) findViewById(R.id.Charger_state));
        currentelectricity = ((TextView) findViewById(R.id.Current_electricity));
        chargermax = ((TextView) findViewById(R.id.Charger_max));
        batterytemperature = ((TextView) findViewById(R.id.Battery_temperature));
        batterytype = ((TextView) findViewById(R.id.Battery_type));
        currentvoltage = ((TextView) findViewById(R.id.Current_voltage));
        chargercurrent = ((TextView) findViewById(R.id.Charger_current));
        chargercurrent.setText(getString(R.string.charger_current) + " : "
                + Integer.toString(scanChargingCurrent()) + "mA");
        mhandle = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    chargercurrent.setText(getString(R.string.charger_current)
                            + " : " + Integer.toString(scanChargingCurrent())
                            + "mA");
                    if (getBatteryState(CHARGER_TYPE).equals("NONSTANDARD_CHARGER")) {
                        mButtonPass.setEnabled(true);
                        doPass2NextTest();
                    }
                }
            }
        };

        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @SuppressWarnings("resource")
    private String getBatteryState(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                return status[0];
            } catch (IOException e) {

            }
        } catch (FileNotFoundException e) {

        }
        return null;
    }

    protected void onResume() {
        super.onResume();
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mBroadcastReceiver, localIntentFilter);
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mhandle.sendEmptyMessage(0x123);
            }
        }, 0, 300);
    }

    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        unregisterReceiver(this.mBroadcastReceiver);
    }

    public void finish() {
        super.finish();
    }
}
