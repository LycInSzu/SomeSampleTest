package com.pri.factorytest.Charger;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class Charger extends PrizeBaseActivity {

    private static final String TAG = "Charger";
    private static final int vbatSystemOn = 3000;//mv
    private final static boolean IS_HAS_COULOMBMETER = SystemProperties.get("ro.ftm_coulombmeter").equals("1");

    final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    final String STATUS = "/sys/class/power_supply/battery/status";
    private TextView chargesate;
    private TextView currentelectricity;
    private TextView chargermax;
    private TextView batterytemperature;
    private TextView batterytype;
    private TextView currentvoltage;
    private TextView chargercurrent;
    private TextView coulombmetervoltage;
    private BatteryManager mBatteryManager;
    private TextView mAuxiCharger;

    private Handler mhandle;
    private Timer mTimer;

    /**
     * The mChargingCurrentPath should use /sys/class/power_supply/battery/current_now
     * in androidP platform,but has the avc:denied,so use the BatteryManager to get
     * current_now and voltage_now values etc ...
     */
    private File mChargingCurrentPath = new File(
            "/sys/devices/platform/battery/charging_current_value");
    private File mCoulombmeterVoltagePath = new File(
            "/sys/class/power_supply/cw-bat/voltage_now");
    private int mVoltageNow = 0;

    private File mChargerAuxiliary = new File(
            "/sys/devices/platform/charger/chg2_exist");
    private boolean mChargeFull = false;
    private boolean mChargePlug = false;
    private boolean mChargingState = false;

    private int scanChargingCurrent() {
        try {
            Scanner scan = new Scanner(mChargingCurrentPath);
            int val = scan.nextInt();
            scan.close();
            return val;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if(mBatteryManager.isCharging()) {
            return Math.abs(mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)) / 10;
        }
        return 0;
    }

    private int scanCoulombmeterVoltage() {
        try {
            Scanner scan = new Scanner(mCoulombmeterVoltagePath);
            int val = scan.nextInt();
            scan.close();
            return val >= 50000 ? val / 1000 : val;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mVoltageNow;
    }

    private int scanChargerAuxiliary() {
        int val = -1;
        try {
            Scanner scan = new Scanner(mChargerAuxiliary);
            val = scan.nextInt();
            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context paramContext, Intent paramIntent) {
            int level;
            int scale;
            int voltage;
            int temperature;
            String technology = "";
            int batteryState = BatteryManager.BATTERY_STATUS_UNKNOWN;
            String state = "";
            mChargingState = false;
            mChargingState = false;
            mChargeFull = false;

            paramIntent.getBooleanExtra("present", false);
            paramIntent.getIntExtra("icon-small", 0);

            //state = getBatteryState(STATUS);

            level = paramIntent.getIntExtra("level", 0);
            scale = paramIntent.getIntExtra("scale", 0);
            temperature = paramIntent.getIntExtra("temperature", 0);
            technology = paramIntent.getStringExtra("technology");
            voltage = paramIntent.getIntExtra("voltage", 0);
            mVoltageNow = voltage;
            batteryState = paramIntent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
            int chargePlug = paramIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            mChargePlug = BatteryManager.isPlugWired(chargePlug);

            if (batteryState == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                state = getString(R.string.not_charging);
            } else if (batteryState == BatteryManager.BATTERY_STATUS_CHARGING) {
                mChargingState = true;
                state = getString(R.string.charging);
            } else if (batteryState == BatteryManager.BATTERY_STATUS_FULL) {
                mChargeFull = true;
                state = getString(R.string.charging_finish);
            }

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
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ChargerLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.charger, null);
        mBatteryManager = (BatteryManager)getSystemService(Context.BATTERY_SERVICE);
        setContentView(ChargerLayout);

        chargesate = ((TextView) findViewById(R.id.Charger_state));
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        currentelectricity = ((TextView) findViewById(R.id.Current_electricity));
        chargermax = ((TextView) findViewById(R.id.Charger_max));
        batterytemperature = ((TextView) findViewById(R.id.Battery_temperature));
        batterytype = ((TextView) findViewById(R.id.Battery_type));
        currentvoltage = ((TextView) findViewById(R.id.Current_voltage));
        chargercurrent = ((TextView) findViewById(R.id.Charger_current));
        coulombmetervoltage = ((TextView) findViewById(R.id.Coulombmeter_voltage));
        mAuxiCharger = findViewById(R.id.auxi_charger);

        mhandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    int val = scanChargingCurrent();
                    int coulomVol = scanCoulombmeterVoltage();
                    int auxicharger = scanChargerAuxiliary();

                    boolean chaCurP = val > 0 || ((mChargeFull || mChargingState) && mChargePlug);
                    boolean couvolP = true;
                    boolean auxChaP = true;
                    chargercurrent.setText(getString(R.string.charger_current) + " : " + (chaCurP ? val : 0) + "mA");
                    if (IS_HAS_COULOMBMETER) {
                        coulombmetervoltage.setVisibility(View.VISIBLE);
                        coulombmetervoltage.setText(getString(R.string.coulombmeter_voltage) + " : " + coulomVol + "mV");
                        couvolP = coulomVol > vbatSystemOn;
                    }
                    if (mChargerAuxiliary.exists()) {
                        mAuxiCharger.setVisibility(View.VISIBLE);
                        mAuxiCharger.setText(getString(R.string.charger_auxiliary) + " : " + auxicharger);
                        auxChaP = auxicharger > 0;
                    }
                    mButtonPass.setEnabled(chaCurP && couvolP && auxChaP);
                    doPass2NextTest();
                }
            }
        };

        confirmButtonNonEnable();
    }

    /**
     * read the file has permission denied
     * NON use
     * @param path
     * @return
     */
    private String getBatteryState(String path) {
        File file = new File(path);

        try {
            FileReader fileReader = new FileReader(file);
            char data[] = new char[128];
            int charCount = -1;
            StringBuilder sb = new StringBuilder();
            while ((charCount = fileReader.read(data)) != -1){
                sb.append(data, 0, charCount);
            }
            fileReader.close();
            Log.i(TAG, sb.toString());
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
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

    @Override
    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        unregisterReceiver(this.mBroadcastReceiver);
    }

    @Override
    public void finish() {
        super.finish();
    }
}
