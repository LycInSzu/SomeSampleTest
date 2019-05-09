package com.pri.factorytest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pri.factorytest.CameraBack.CameraBack;
import com.pri.factorytest.LCD.LCD;
import com.pri.factorytest.Service.BluetoothScanService;
import com.pri.factorytest.Service.WifiScanService;
import com.pri.factorytest.Version.Version;
import com.pri.factorytest.util.Utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.GSM_SERIAL;
import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;
import static com.pri.factorytest.util.Utils.BATTERY;
import static com.pri.factorytest.util.Utils.BLUETOOTH;
import static com.pri.factorytest.util.Utils.CAMERA_HALL;
import static com.pri.factorytest.util.Utils.CAMERA_HALL_CALI;

import static com.pri.factorytest.util.Utils.DOUBLE_CAMERA_STANDAR;

import static com.pri.factorytest.util.Utils.EFUSE_STATE;
import static com.pri.factorytest.util.Utils.FINGERPRINT;
import static com.pri.factorytest.util.Utils.FINGER_KEY_CHECK;
import static com.pri.factorytest.util.Utils.FLASH_LAMP_FRONT;
import static com.pri.factorytest.util.Utils.FRONT_CAM;
import static com.pri.factorytest.util.Utils.FRONT_CAM_SUB;
import static com.pri.factorytest.util.Utils.FRONT_CAM_SUB2;
import static com.pri.factorytest.util.Utils.GPS;
import static com.pri.factorytest.util.Utils.GRAVITY_SENSOR;
import static com.pri.factorytest.util.Utils.GYROSCOPE_SENSOR;
import static com.pri.factorytest.util.Utils.HALL_SENSOR;
import static com.pri.factorytest.util.Utils.HEADSET;
import static com.pri.factorytest.util.Utils.INFRARED;
import static com.pri.factorytest.util.Utils.KEYS;
import static com.pri.factorytest.util.Utils.LED;
import static com.pri.factorytest.util.Utils.LIGHT_SENSOR;
import static com.pri.factorytest.util.Utils.MAGNETIC_SENSOR;
import static com.pri.factorytest.util.Utils.MICRO_PHONE;
import static com.pri.factorytest.util.Utils.MICRO_PHONE_LOOP;
import static com.pri.factorytest.util.Utils.NFC;
import static com.pri.factorytest.util.Utils.NXPCAL;
import static com.pri.factorytest.util.Utils.OTG;
import static com.pri.factorytest.util.Utils.PHONE;
import static com.pri.factorytest.util.Utils.PRESS_SENSOR;
import static com.pri.factorytest.util.Utils.RADIO;
import static com.pri.factorytest.util.Utils.RAM;
import static com.pri.factorytest.util.Utils.RANG_SENSOR;
import static com.pri.factorytest.util.Utils.REAR_CAM;
import static com.pri.factorytest.util.Utils.REAR_CAM_SUB;
import static com.pri.factorytest.util.Utils.REAR_CAM_SUB2;
import static com.pri.factorytest.util.Utils.REAR_CAM_SUB3;
import static com.pri.factorytest.util.Utils.RECEIVER;
import static com.pri.factorytest.util.Utils.SIM;
import static com.pri.factorytest.util.Utils.SPEAKER;
import static com.pri.factorytest.util.Utils.STEP_COUNTER_SENSOR;
import static com.pri.factorytest.util.Utils.SUB_MICRO_PHONE;
import static com.pri.factorytest.util.Utils.TF_CARD;
import static com.pri.factorytest.util.Utils.TOUCH_SCREEN;
import static com.pri.factorytest.util.Utils.VOLTMETER;
import static com.pri.factorytest.util.Utils.WIFI;
import static com.pri.factorytest.util.Utils.WIRELESS_CHARGER;
import static com.pri.factorytest.util.Utils.YCD;

public class PrizeFactoryTestActivity extends PrizeBaseActivity {
    private static final String TAG = "PrizeFactoryTestMain";

    private Button pcbaTestButton = null;
    private Button autoTestButton = null;
    private Button manualTestButton = null;
    private Button listtestButton = null;
    private Button testReportButton = null;
    private Button factorySetButton = null;
    private Button softInfoButton = null;
    private Button agingtestButton = null;
    private Button languageSwitchButton = null;
    private SensorManager mSensorManager = null;
    private boolean isPcbaTest = false;
    private boolean isMobileTest = false;
    private boolean isManualTest = false;
    private Context mContext;
    private long startMili;
    private long endMili;
    private long mTestTime;
    private FactoryTestApplication mApp;
    private volatile int mExceptItem;
    private String[] mTestItems = new String[70];
    private String[] mItemsValue = new String[70];
    private volatile String[] mTestValue = new String[70];
    private List<String> mItemsValueList = null;
    private String mLastOperation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_test);
        //Log.i(TAG, "----system.serial" + Build.getSerial());
        mContext = this;
        mTestItems = mContext.getResources().getStringArray(R.array.single_test_items);
        String[] totalItems = mContext.getResources().getStringArray(R.array.test_item_values);
        mItemsValue = Arrays.stream(totalItems).map(x -> {return x.split(",")[0];}).toArray(String[]::new);
        String[] totalItemsNvIndexs = Arrays.stream(totalItems).map(x -> {return x.split(",")[1];}).toArray(String[]::new);
        mApp = (FactoryTestApplication) getApplication();

        mApp.setPrizeFactoryTotalNvIndexs(totalItemsNvIndexs);
        mApp.setPrizeFactoryTotalItems(mItemsValue);

        mTestItems = initTestItems();
        mApp.setTestItems(mTestItems);
        mApp.setItemsValue(mItemsValue);
        mItemsValueList = Arrays.asList(mItemsValue);
        mTestValue = mItemsValue;
        initViews();
        Utils.paddingLayout(findViewById(R.id.prize_title), 0, ACTIVITY_TOP_PADDING, 0, 0);
        enableGPS(true);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                startService(new Intent(PrizeFactoryTestActivity.this, WifiScanService.class));
                //if (SystemProperties.get("ro.mtk_bt_support").equals("1")) {
                startService(new Intent(PrizeFactoryTestActivity.this, BluetoothScanService.class));
                //}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        if ("koobee".equals(PRIZE_CUSTOMER)) {
            agingtestButton.setVisibility(View.GONE);
        } else {
            agingtestButton.setVisibility(View.GONE);
        }
        if ((null == getIntent().getExtras()) || !getIntent().getExtras().getBoolean("isAutoTest")) {
            String componentName = getIntent().getComponent().getClassName();
            Log.i(TAG, "---componentName:" + componentName);
            if (!preStartAutoCit()) {
                return;
            }
            switch (componentName) {
                case "com.pri.factorytest.ManualTestLauncher":
                    startManualTest();
                    break;

                case "com.pri.factorytest.PrizeFactoryTestLauncher":
                default:
                    startAutoTest();
                    break;
            }
        }
    }

    private boolean isAutomanualTest() {
        boolean isAutomanualTest = false;
        if (null != GSM_SERIAL && GSM_SERIAL.length() >= 39) {
            if (GSM_SERIAL.substring(38, 39).equals("P")) {
                isAutomanualTest = true;
            } else {
                isAutomanualTest = false;
            }
        }
        return isAutomanualTest;
    }

    private void startAutoTest() {
        startMili = System.currentTimeMillis();
        Utils.toStartAutoTest = true;
        //String[] testItems = mTestItems;
        String[] itemsValue = mItemsValue;
        if ("0".equals(SystemProperties.get("ro.pri_factory_test_wbg"))) {
            itemsValue = deleteItemByValue(itemsValue, WIFI, GPS, BLUETOOTH);
            mTestValue = itemsValue;
        }
        itemsValue = deleteItemByValue(itemsValue, STEP_COUNTER_SENSOR, OTG, PHONE,
                DOUBLE_CAMERA_STANDAR, EFUSE_STATE, FINGER_KEY_CHECK, NFC, WIRELESS_CHARGER);
        mTestValue = itemsValue;
        Log.d(TAG, "--------INIT------items.length:" + mTestValue.length);
        for (int pos = 0; pos <= mItemsValue.length; pos++) {
            mApp.setResultCode(pos, 0);
            mApp.setReportResult(pos, null);
        }
        Utils.mItemPosition = 0;
        mExceptItem = 0;

        isMobileTest = true;
        Intent intent = new Intent().setClass(this, LCD.class);
        startActivityForResult(intent, 0);
    }

    private void startManualTest() {
        startMili = System.currentTimeMillis();
        Utils.toStartAutoTest = true;
        //String[] testItems = mTestItems;
        String[] itemsValue = mItemsValue;
        itemsValue = deleteItemByValue(itemsValue, TOUCH_SCREEN, SIM, LED, YCD, KEYS,
                HEADSET, RECEIVER, SPEAKER, MICRO_PHONE_LOOP, MICRO_PHONE, SUB_MICRO_PHONE,
                TF_CARD, RAM, REAR_CAM, FRONT_CAM_SUB, FRONT_CAM, BATTERY, LIGHT_SENSOR,
                GRAVITY_SENSOR, RANG_SENSOR, MAGNETIC_SENSOR, STEP_COUNTER_SENSOR,
                OTG, GYROSCOPE_SENSOR, WIFI, BLUETOOTH, GPS, HALL_SENSOR, INFRARED, NFC,
                CAMERA_HALL, CAMERA_HALL_CALI, PHONE, DOUBLE_CAMERA_STANDAR,
                EFUSE_STATE, FINGER_KEY_CHECK, NFC, WIRELESS_CHARGER);
        mTestValue = itemsValue;
        Log.d(TAG, "--------INIT--Manual----items.length:" + mTestValue.length);
        for (int pos = 0; pos <= mItemsValue.length; pos++) {
            mApp.setResultCode(pos, 0);
            mApp.setReportResult(pos, null);
        }
        Utils.mItemPosition = 0;
        mExceptItem = 0;

        isManualTest = true;
        Intent intent = new Intent().setClass(this, LCD.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME) {
            //if (SystemProperties.get("ro.mtk_bt_support").equals("1")) {
            if (mApp.getIsBluetoothScanning()) {
                stopService(new Intent(PrizeFactoryTestActivity.this, BluetoothScanService.class));
            }
            //}
            if (mApp.getIsWifiScanning()) {
                stopService(new Intent(PrizeFactoryTestActivity.this, WifiScanService.class));
            }
            enableWifi(false);
            enableBluetooth(false);
            enableGPS(false);
            finish();
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void enableBluetooth(boolean enable) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (enable)
                mBluetoothAdapter.enable();
            else
                mBluetoothAdapter.disable();
        }
    }

    private void enableWifi(boolean enable) {

        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            mWifiManager.setWifiEnabled(enable);
        }
    }

    private void enableGPS(boolean enable) {
        if (false == enable) {
            Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.LOCATION_MODE, 0, UserHandle.USER_CURRENT);
        } else {
            Settings.Secure.putIntForUser(getContentResolver(), Settings.Secure.LOCATION_MODE, 1, UserHandle.USER_CURRENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Utils.toStartAutoTest == true) {
            if (Utils.mItemPosition > 0) {
                mExceptItem++;
            } else {
                return;
            }
            android.util.Log.i(TAG, "--------onActivityResult----Index:" + Utils.mItemPosition + "---mExceptItem:" + mExceptItem);
            if (Utils.mItemPosition != mExceptItem) {
                Utils.mItemPosition = mExceptItem;
            }
            String testItemName = mTestValue[Utils.mItemPosition - 1];
            android.util.Log.i(TAG, "--------onActivityResult----Index:" + Utils.mItemPosition + "---testItemName:" + testItemName);
            if (Utils.mItemPosition == 0) {
                String autoappname = mTestValue[Utils.mItemPosition];
                Utils.mItemPosition = 1;

                setResultList(resultCode, testItemName, autoappname);
                Utils.startTestItemActivity(this, autoappname);
                return;
            }
            if (Utils.mItemPosition < mTestValue.length) {
                String autoappname = mTestValue[Utils.mItemPosition];

                setResultList(resultCode, testItemName, autoappname);
                Utils.startTestItemActivity(this, autoappname);
            } else {
                Utils.toStartAutoTest = false;
                mExceptItem = 0;

                setResultList(resultCode, testItemName, "END!!!!!!!");
            }

            if (Utils.mItemPosition == mTestValue.length) {
                android.util.Log.i(TAG, "--------onActivityResult----end,return the preserveTestResult.");
                preserveTestResult();
                Utils.mItemPosition = 0;
            }
        }
    }

    private void preserveTestResult() {
        if (isPcbaTest) {
            preserveTestResult("pcbaResult");
        }
        if (isMobileTest) {
            preserveTestResult("mobileResult");
        }
        if (isManualTest) {
            preserveTestResult("manualResult");
        }

    }

    private void preserveTestResult(String type) {
        android.util.Log.i(TAG, "--------onActivityResult----write the nv and show the testReport.");
        String testResult = "P";
        for (int i = 0; i < mItemsValue.length; i++) {
            if (mApp.getResultCode(i) == 2) {
                testResult = "F";
                break;
            }
        }
        if ("mobileResult".equals(type)) {
            isMobileTest = false;
            Utils.writeProInfo(testResult, 45);
        }
        if ("pcbaResult".equals(type)) {
            isPcbaTest = false;
            Utils.writeProInfo(testResult, 49);
        }
        if ("manualResult".equals(type)) {
            isManualTest = false;
            Utils.writeProInfo(testResult, 37);
        }

        Intent intent = new Intent();
        mLastOperation = type;
        intent.putExtra("last_operation", mLastOperation);
        if ("mobileResult".equals(type)) {
            endMili = System.currentTimeMillis();
            mTestTime = (endMili - startMili) / 1000;
            intent.putExtra("testTimeReportQr", mTestTime);
            intent.setClass(PrizeFactoryTestActivity.this, FactoryTestReportQr.class);
        } else {
            intent.setClass(PrizeFactoryTestActivity.this, FactoryTestReport.class);
        }
        startActivity(intent);
    }

    private void setResultList(int resultCode, String testItemName, String autoappname) {
        int positionInItemsValue = mItemsValueList.indexOf(testItemName);
        if (resultCode == RESULT_OK) {
            mApp.setResultCode(positionInItemsValue, 1);
            mApp.setReportResult(positionInItemsValue, getResources().getString(R.string.result_normal));
        } else if (resultCode == RESULT_CANCELED) {
            mApp.setResultCode(positionInItemsValue, 2);
            mApp.setReportResult(positionInItemsValue, getResources().getString(R.string.result_error));
        }
        android.util.Log.i(TAG, "--------onActivityResult----Index:" + Utils.mItemPosition
                + "||lastItemName:" + testItemName + "||result:" +
                mApp.getReportResult(positionInItemsValue)
                + "||nextItemName:" + autoappname);
    }

    private String[] deleteItemByValue(String[] array, String... values) {
        /*List<String> list = new ArrayList();
        for (String i : array) {
            list.add(i);
        }
        for (String str : values) {
            if (list.contains(str)) {
                list.remove(str);
            }
        }
        String[] newArray = new String[list.size()];
        list.toArray(newArray);
        return newArray;*/
        Collection<String> collect = Arrays.stream(array).collect(Collectors.toList());
        Collection<String> c = collect;
        Arrays.stream(values).forEach(x -> c.removeIf(s->s.equals(x)));
        String[] aaa = c.stream().toArray(String[]::new);
        return aaa;
    }

    private String[] initTestItems() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_LIGHT)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.light_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, LIGHT_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_BAROMETER)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.pressure_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, PRESS_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.gravity_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, GRAVITY_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_PROXIMITY)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.rang_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, RANG_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_COMPASS)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.magnetic_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, MAGNETIC_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.step_counter_sensor));
                mItemsValue = deleteItemByValue(mItemsValue, STEP_COUNTER_SENSOR);
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null && !hasHardwareDevice(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
                mTestItems = deleteItemByValue(mTestItems, getResources().getString(R.string.gysensor_name));
                mItemsValue = deleteItemByValue(mItemsValue, GYROSCOPE_SENSOR);
            }
        }
        if (!hasComponentPkg(this, "com.android.fmradio")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.radio));
            mItemsValue = deleteItemByValue(mItemsValue, RADIO);
        }
        if (!hasHardwareDevice(PackageManager.FEATURE_FINGERPRINT)) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.fingerprint));
            mItemsValue = deleteItemByValue(mItemsValue, FINGERPRINT);
        }
        if (!Utils.isFileExists("/sys/hall_state/hall_status")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.hall_sensor));
            mItemsValue = deleteItemByValue(mItemsValue, HALL_SENSOR);
        }
        if (!Utils.isFileExists("/proc/hall1/m1120_up") && !Utils.isFileExists("/proc/hall2/m1120_down")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.camera_hall_one));
            mItemsValue = deleteItemByValue(mItemsValue, CAMERA_HALL);
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.camera_hall_cali));
            mItemsValue = deleteItemByValue(mItemsValue, CAMERA_HALL_CALI);
        }
        if (!SystemProperties.get("ro.mtk_dual_mic_support").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.sub_microphone));
            mItemsValue = deleteItemByValue(mItemsValue, SUB_MICRO_PHONE);
        }
        if (!Utils.isSupportDoubuleCameraStand(this)) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.double_camera_standar));
            mItemsValue = deleteItemByValue(mItemsValue, DOUBLE_CAMERA_STANDAR);
        }
        if (!SystemProperties.get("ro.pri_infrared").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.infrared));
            mItemsValue = deleteItemByValue(mItemsValue, INFRARED);
        }
        if (!Utils.isFileExists("/sys/class/leds/green/brightness")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.prize_led));
            mItemsValue = deleteItemByValue(mItemsValue, LED);
        }
        if (!Utils.isFileExists("/proc/yc_mode")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.prize_ycd));
            mItemsValue = deleteItemByValue(mItemsValue, YCD);
        }
        /*if (!SystemProperties.get("ro.mtk_bt_support").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.bluetooth));
            mItemsValue = deleteItemByValue(mItemsValue, BLUETOOTH);
        }*/
        /*if (Stream.of("koobee", "odm","BLU","customer").anyMatch(x -> PRIZE_CUSTOMER.equals(x))) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.phone));
            mItemsValue = deleteItemByValue(mItemsValue, PHONE);
        }*/
        if (!"1".equals(SystemProperties.get("ro.pri_rear_camera_sub")) &&
                !"1".equals(SystemProperties.get("ro.pri_rear_camera_sub_als")) &&
                !Utils.isFileExists("/sys/kernel/dcam/dcam_r_value") &&
                !Utils.isFileExists("/sys/kernel/spc/spc_r/value") &&
                !Utils.isFileExists("/sys/kernel/spc/spc_r_1/value")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.rear_camera_sub));
            mItemsValue = deleteItemByValue(mItemsValue, REAR_CAM_SUB);
        }
        if (!"1".equals(SystemProperties.get("ro.pri_rear_camera_sub2"))) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.rear_camera_sub2));
            mItemsValue = deleteItemByValue(mItemsValue, REAR_CAM_SUB2);
        }
        if (!"1".equals(SystemProperties.get("ro.pri_rear_camera_sub3"))) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.rear_camera_sub3));
            mItemsValue = deleteItemByValue(mItemsValue, REAR_CAM_SUB3);
        }
        if (!Utils.isFileExists("/sys/kernel/dcam/dcam_f_value") && !Utils.isFileExists("/sys/kernel/spc/spc_f/value")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.front_camera_sub));
            mItemsValue = deleteItemByValue(mItemsValue, FRONT_CAM_SUB);
        }
        if (!"1".equals(SystemProperties.get("ro.pri_front_camera_sub2"))) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.front_camera_sub2));
            mItemsValue = deleteItemByValue(mItemsValue, FRONT_CAM_SUB2);
        }
        if (!isFrontFlashSupport()) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.flash_lamp_front));
            mItemsValue = deleteItemByValue(mItemsValue, FLASH_LAMP_FRONT);
        }
        if (!SystemProperties.get("ro.pri_otg").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.otg));
            mItemsValue = deleteItemByValue(mItemsValue, OTG);
        }
        if (!SystemProperties.get("ro.pri_nxp_cal").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.nxp_cal));
            mItemsValue = deleteItemByValue(mItemsValue, NXPCAL);
        }
        if (NfcAdapter.getDefaultAdapter(this) == null) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.prize_nfc));
            mItemsValue = deleteItemByValue(mItemsValue, NFC);
        }
        if (!Utils.isFileExists("/sys/class/power_supply/cw-bat/voltage_now")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.voltmeter));
            mItemsValue = deleteItemByValue(mItemsValue, VOLTMETER);
        }
        if (!SystemProperties.get("ro.pri_wireless_charger").equals("1")) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.wirelesscharger));
            mItemsValue = deleteItemByValue(mItemsValue, WIRELESS_CHARGER);
        }
        if (Stream.of("koobee").noneMatch(x -> PRIZE_CUSTOMER.contains(x))) {
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.efuse_state));
            mItemsValue = deleteItemByValue(mItemsValue, EFUSE_STATE);
            mTestItems = deleteItemByValue(mTestItems, getString(R.string.finger_key_check));
            mItemsValue = deleteItemByValue(mItemsValue, FINGER_KEY_CHECK);
        }

        mTestItems = deleteItemByValue(mTestItems, getString(R.string.microphone_loop));
        mItemsValue = deleteItemByValue(mItemsValue, MICRO_PHONE_LOOP);
        return mTestItems;
    }

    void initViews() {
        pcbaTestButton = (Button) findViewById(R.id.pcbatest);
        autoTestButton = (Button) findViewById(R.id.autotest);
        manualTestButton = (Button) findViewById(R.id.manualtest);
        listtestButton = (Button) findViewById(R.id.listtest);
        testReportButton = (Button) findViewById(R.id.testreport);
        factorySetButton = (Button) findViewById(R.id.factoryset);
        softInfoButton = (Button) findViewById(R.id.softinfo);
        languageSwitchButton = (Button) findViewById(R.id.languageswitch);
        agingtestButton = (Button) findViewById(R.id.agingtest);
        if (PRIZE_CUSTOMER.contains("pcba")) {
            manualTestButton.setVisibility(View.GONE);
        } else {
            manualTestButton.setVisibility("1".equals(SystemProperties.get("ro.pri_auto_test")) ? View.VISIBLE : View.GONE);
        }
        pcbaTestButton.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    PrizeFactoryTestActivity.this);
            dialog.setCancelable(false)
                    .setTitle(R.string.pcbatest)
                    .setMessage(R.string.pcbatest_confirm)
                    .setPositiveButton(R.string.confirm, (dialoginterface, i) -> {
                        Utils.toStartAutoTest = true;
                        String[] itemsValue = mItemsValue;
                        mTestValue = itemsValue;
                        for (int pos = 0; pos <= mItemsValue.length; pos++) {
                            mApp.setResultCode(pos, 0);
                            mApp.setReportResult(pos, null);
                        }
                        Utils.mItemPosition = 3;
                        mExceptItem = 3;

                        isPcbaTest = true;
                        Intent intent = new Intent().setClass(
                                PrizeFactoryTestActivity.this,
                                CameraBack.class);
                        startActivityForResult(intent, 0);
                    })
                    .setNegativeButton(R.string.cancel, (dialoginterface, i) -> {
                    }).show();
        });


        autoTestButton.setOnClickListener(view -> {
            if (!preStartAutoCit()) {
                return;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    PrizeFactoryTestActivity.this);
            dialog.setCancelable(false)
                    .setTitle(R.string.phonetest)
                    .setMessage(R.string.phonetest_confirm)
                    .setPositiveButton(R.string.confirm, (dialoginterface, i) -> {
                        startAutoTest();
                    })
                    .setNegativeButton(R.string.cancel, (dialoginterface, i) -> {
                    }).show();
        });

        manualTestButton.setOnClickListener(view -> {
            if (!preStartAutoCit()) {
                return;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    PrizeFactoryTestActivity.this);
            dialog.setCancelable(false)
                    .setTitle(R.string.manualtest)
                    .setMessage(R.string.manualtest_confirm)
                    .setPositiveButton(R.string.confirm, (dialoginterface,i) -> {
                        startManualTest();
                    })
                    .setNegativeButton(R.string.cancel, (dialoginterface,i) -> {
                    }).show();
        });

        listtestButton.setOnClickListener(view -> {
            mLastOperation = null;
            for (int pos = 0; pos <= mItemsValue.length; pos++) {
                mApp.setResultCode(pos, 0);
                mApp.setReportResult(pos, null);
            }
            Intent intent = new Intent().setClass(
                    PrizeFactoryTestActivity.this, PrizeFactoryTestListActivity.class);
            startActivity(intent);
        });

        testReportButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("last_operation", mLastOperation);
            intent.setClass(
                    PrizeFactoryTestActivity.this, FactoryTestReport.class);
            startActivity(intent);
        });

        factorySetButton.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    PrizeFactoryTestActivity.this);
            dialog.setCancelable(false)
                    .setTitle(R.string.factoryset)
                    .setMessage(R.string.factoryset_confirm)
                    .setPositiveButton(R.string.confirm, (dialoginterface, i) -> {
                                Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                                intent.setPackage("android");
                                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                                intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, /*mEraseSdCard*/false);
                                intent.putExtra(Intent.EXTRA_WIPE_ESIMS, /*mEraseEsims*/true);
                                sendBroadcast(intent);
                            }
                    )
                    .setNegativeButton(R.string.cancel, (dialoginterface, i) -> {
                    }).show();
        });

        softInfoButton.setOnClickListener(view -> {
            Intent intent = new Intent().setClass(
                    PrizeFactoryTestActivity.this, Version.class);
            intent.putExtra("softinfo", true);
            startActivity(intent);
        });

        if (!"1".equals(SystemProperties.get("ro.pri_del_lan_switch_btn"))) {
            languageSwitchButton.setVisibility(View.GONE);
        }
        languageSwitchButton.setOnClickListener(view -> {
            switchLanguage();
        });

        agingtestButton.setOnClickListener(view -> {
            Intent intent = new Intent().setClass(
                    PrizeFactoryTestActivity.this, AgingTestActivity.class);
            startActivity(intent);
        });

    }

    /**
     * rebuild the process of the product,MMI test not limit the MT(BT/FT)
     * @return TRUE:the MT nv test are all pass,false or else
     */
    private boolean preStartAutoCit() {
        if (false/*!Utils.isPhoneCalibration()*/) {
            Toast.makeText(mContext, getString(R.string.no_calibration), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Stream.of("gw9518").anyMatch(x -> SystemProperties
                .get("ro.pri.fingerprint").contains(x.trim()))) {
            String huidingCali = Utils.readProInfo(Utils.PRIZE_HUIDING_FINGERPRINT_CALI, 1);
            huidingCali = String.valueOf(huidingCali.charAt(huidingCali.length() - 1));
            if (!"P".equals(huidingCali)) {
                Toast.makeText(mContext, getString(R.string.huiding_fingerprint_cali_fail), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void switchLanguage() {
        //String currentlanguage = getCountry();
        if (Utils.CURRENT_LAN.contains("zh")) {
            Configuration config = getResources().getConfiguration();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            config.locale = Locale.ENGLISH;
            Utils.CURRENT_LAN = "en_US";
            getResources().updateConfiguration(config, metrics);
        } else {
            Configuration config = getResources().getConfiguration();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            config.locale = Locale.SIMPLIFIED_CHINESE;
            Utils.CURRENT_LAN = "zh_CN";
            getResources().updateConfiguration(config, metrics);
        }
        Intent intent = new Intent();
        intent.setClass(this, PrizeFactoryTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("isAutoTest", true);
        startActivity(intent);
    }

    private String getCountry() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getCountry();
        return language;
    }

    private boolean hasComponentPkg(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        android.content.pm.ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isFrontFlashSupport() {
        /*boolean result = false;
        Camera camera = null;
        try {
            //modified by tangan-begin
            //camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT, 0x100);
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            //modified by tangan-end
            Log.e("yanglvxiong", "try:: camera = " + camera);
            camera.startPreview();
            if (camera.getParameters().getSupportedFlashModes() != null && camera.getParameters().getSupportedFlashModes().contains("on")) {
                Log.e("tangan", "front SupportedFlashModes:" + camera.getParameters().getSupportedFlashModes());
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e("yanglvxiong", "finally:: camera = " + camera);
            if (camera != null) {
                camera.stopPreview();
                camera.release();
            }
        }
        return result;*/
        CameraManager cameraManager = (CameraManager) getApplication().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                //Log.i(TAG, "----id:" + id + "||flashAvailable:" + flashAvailable.booleanValue() + "||lensFacing:" + lensFacing.intValue());
                if ("1".equals(id) && flashAvailable != null && flashAvailable
                        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean hasHardwareDevice(String deviceName) {
        PackageManager pm = mContext.getPackageManager();
        return pm.hasSystemFeature(deviceName);
    }
}
