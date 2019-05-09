package com.pri.factorytest;

import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.service.GoodixFingerprintService;
import com.pri.factorytest.BlueTooth.DeviceInfo;
import com.pri.factorytest.FingerPrint.huiding.FPFileUtils;
import com.pri.factorytest.FingerPrint.huiding.Util;
import com.pri.factorytest.util.SharedPreferencesHelper;
import com.pri.factorytest.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FactoryTestApplication extends Application {
    private static final String TAG = "FactoryTestApplication";
    private List<ScanResult> wifiScanResult = new ArrayList<ScanResult>();
    private List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
    private boolean isWifiScanning = false;
    private boolean isBluetoothScanning = false;
    private SharedPreferencesHelper shared_pref;
    private String defDuration = "00:00:00";
    private int prize_project_ver = SystemProperties.getInt("ro.pri.project.ver", 6);

    public static final String PRIZE_CUSTOMER = SystemProperties.get("ro.pri_customer");
    public static final String GSM_SERIAL = SystemProperties.get("vendor.gsm.serial");
    private static String networkStr = SystemProperties.get("ro.pri_board_network_type");
    public static final String NETWORK_TYPE = "".equals(networkStr.trim()) ? "CB/LfB/LtB/WB/TB/GB" : networkStr;
    private final static String PRIZE_FINGERPRINT_CUSOTMER = Optional.ofNullable(SystemProperties
            .get("ro.pri.fingerprint")).map(x -> x.trim()).orElse("");

    private String[] mItemsName = new String[70];
    private int[] mResultCode = new int[70];
    private String[] mReportResult = new String[70];
    private String[] mItemsValue = new String[70];

    private String[] mPrizeFactoryTotalItems = new String[70];
    private String[] mPrizeFactoryTotalNvIndexs = new String[70];

    @Override
    public void onCreate() {
        super.onCreate();

        if (Stream.of("gw9518").anyMatch(x -> PRIZE_FINGERPRINT_CUSOTMER.contains(x.trim()))) {
            if (!"1".equals(SystemProperties.get("ro.pri_goodix_finger_switch"))) {
                getService(this);
            }
            FPFileUtils.init(null);
            Util.setAccesibility(this, false);
        }

        shared_pref = new SharedPreferencesHelper(this, "com.pri.factorytest");
        loadDefaultDuration();
        loadDefaultAgingTestItem();
        loadDefaultAgingTestTime();
        loadCurrentTimeMillis();
        loadDefaultTestReport();
    }

    private void getService(Context context) {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Log.d(TAG, "success to get ServiceManager");

            Method addService = serviceManager.getMethod("addService", String.class, IBinder.class);
            Log.d(TAG, "success to get method: addService");

            GoodixFingerprintService service = new GoodixFingerprintService(context);
            addService.invoke(null,
                    new Object[]{
                            Constants.GOODIX_FINGERPRINT_SERVICE_NAME, service
                    });
            Log.d(TAG, "success to addService: " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        }
    }

    public void setTestItems(String[] items) {
        mItemsName = items;
    }

    public String[] getItems() {
        return mItemsName;
    }

    public void setItemsValue(String[] items) {
        mItemsValue = items;
    }

    public String[] getItemsValue() {
        return mItemsValue;
    }

    public String[] getPrizeFactoryTotalItems() {
        return mPrizeFactoryTotalItems;
    }

    public void setPrizeFactoryTotalItems(String[] totaltems) {
        this.mPrizeFactoryTotalItems = totaltems;
    }

    public void setPrizeFactoryTotalNvIndexs(String[] totalNvIndexs){
        this.mPrizeFactoryTotalNvIndexs = totalNvIndexs;
    }

    public String getPrizeFactoryTotalNvIndexs(int index) {
        return mPrizeFactoryTotalNvIndexs[index];
    }

    public int getResultCode(int arrayIndex) {
        if (arrayIndex >= mResultCode.length || arrayIndex < 0) {
            Log.e(TAG, "ERROR: array index is not correct!------getResultCode");
            return 0;
        }
        return mResultCode[arrayIndex];
    }

    public void setResultCode(int arrayIndex, int value) {
        if (arrayIndex >= mResultCode.length || arrayIndex < 0) {
            Log.e(TAG, "ERROR: array index is not correct!------setResultCode");
        }
        mResultCode[arrayIndex] = value;
    }

    public String getReportResult(int arrayIndex) {
        if (arrayIndex >= mReportResult.length || arrayIndex < 0) {
            Log.e(TAG, "ERROR: array index is not correct!-------getReportResult");
            return "";
        }
        return mReportResult[arrayIndex];
    }

    public void setReportResult(int arrayIndex, String value) {
        if (arrayIndex >= mReportResult.length || arrayIndex < 0) {
            Log.e(TAG, "ERROR: array index is not correct!-------setReportResult");
        }
        mReportResult[arrayIndex] = value;
    }

    private void loadDefaultTestReport() {
        if (shared_pref.getValue("reboot_result") == null) {
            shared_pref.putValue("reboot_result", "untest");
        }
        if (shared_pref.getValue("sleep_result") == null) {
            shared_pref.putValue("sleep_result", "untest");
        }
    }

    private void loadDefaultAgingTestItem() {
        if (shared_pref.getValue("reboot_selected") == null) {
            shared_pref.putValue("reboot_selected", "0");
        }
        if (shared_pref.getValue("sleep_selected") == null) {
            shared_pref.putValue("sleep_selected", "0");
        }
        if (shared_pref.getValue("video_speaker_selected") == null) {
            shared_pref.putValue("video_speaker_selected", "1");
        }
        if (shared_pref.getValue("video_receiver_selected") == null) {
            shared_pref.putValue("video_receiver_selected", "1");
        }
        if (shared_pref.getValue("vibrate_selected") == null) {
            shared_pref.putValue("vibrate_selected", "1");
        }
        if (shared_pref.getValue("mic_loop_selected") == null) {
            shared_pref.putValue("mic_loop_selected", "1");
        }
        if (shared_pref.getValue("front_camera_selected") == null) {
            shared_pref.putValue("front_camera_selected", "1");
        }
        if (shared_pref.getValue("back_camera_selected") == null) {
            shared_pref.putValue("back_camera_selected", "1");
        }
        ///ddr select init
        if (shared_pref.getValue("ddr_test_start") == null) {
            shared_pref.putValue("ddr_test_start", "1");
        }

        if (shared_pref.getValue("ddr_test") == null) {
            shared_pref.putValue("ddr_test", "0");
        }
        if (shared_pref.getValue("ddr_test_circles") == null) {
            shared_pref.putValue("ddr_test_circles", "0");
        }
        if (Utils.isFileExists("/proc/hall1/m1120_up") || Utils.isFileExists("/proc/hall2/m1120_down")) {
            shared_pref.putValue("front_motor_test_start", "1");
            shared_pref.putValue("front_motor_test_count", "350");
        }
    }

    //time is value seconds
    private void loadDefaultAgingTestTime() {
        if (shared_pref.getValue("reboot_time") == null) {
            shared_pref.putValue("reboot_time", "1800");
        }
        if (shared_pref.getValue("sleep_time") == null) {
            shared_pref.putValue("sleep_time", "1200");
        }
        if (shared_pref.getValue("parallel_time") == null) {
            shared_pref.putValue("parallel_time", "1800");
        }
    }

    private void loadCurrentTimeMillis() {
        if (shared_pref.getValue("reboot_currenttimemillis") == null) {
            shared_pref.putValue("reboot_currenttimemillis", String.valueOf(System.currentTimeMillis()));
        }
    }

    private void loadDefaultDuration() {
        if (shared_pref.getValue("video_speaker_duration") == null) {
            shared_pref.putValue("video_speaker_duration", defDuration);
        }
        if (shared_pref.getValue("video_receiver_duration") == null) {
            shared_pref.putValue("video_receiver_duration", defDuration);
        }
        if (shared_pref.getValue("vibrate_duration") == null) {
            shared_pref.putValue("vibrate_duration", defDuration);
        }
        if (shared_pref.getValue("mic_loop_duration") == null) {
            shared_pref.putValue("mic_loop_duration", defDuration);
        }
        if (shared_pref.getValue("front_camera_duration") == null) {
            shared_pref.putValue("front_camera_duration", defDuration);
        }
        if (shared_pref.getValue("back_camera_duration") == null) {
            shared_pref.putValue("back_camera_duration", defDuration);
        }
    }

    public void setWifiScanResult(List<ScanResult> wifiScanResult) {
        this.wifiScanResult = wifiScanResult;
    }

    public List<ScanResult> getWifiScanResult() {
        return wifiScanResult;
    }

    public void setBluetoothDeviceList(List<DeviceInfo> mDeviceList) {
        this.mDeviceList = mDeviceList;
    }

    public List<DeviceInfo> getBluetoothDeviceList() {
        return mDeviceList;
    }

    public void setIsWifiScanning(boolean isWifiScanning) {
        this.isWifiScanning = isWifiScanning;
    }

    public boolean getIsWifiScanning() {
        return isWifiScanning;
    }

    public void setIsBluetoothScanning(boolean isBluetoothScanning) {
        this.isBluetoothScanning = isBluetoothScanning;
    }

    public boolean getIsBluetoothScanning() {
        return isBluetoothScanning;
    }

    public SharedPreferencesHelper getSharePref() {
        return shared_pref;
    }

    //added by tangan-begin
    public boolean isOversea() {
        boolean isOversea = false;
        if (prize_project_ver % 2 == 0) {
            isOversea = true;
        }
        return isOversea;
    }
    //added by tangan-end

}
