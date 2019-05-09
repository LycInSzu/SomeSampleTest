package com.pri.factorytest.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.internal.util.HexDump;
import com.pri.factorytest.BlueTooth.BlueTooth;
import com.pri.factorytest.Brightness.Brightness;
import com.pri.factorytest.CSensor.CSensor;
import com.pri.factorytest.CameraBack.CameraBack;
import com.pri.factorytest.CameraBack.DoubleCameraOperation;
import com.pri.factorytest.CameraBackSub.CameraBackAlsSub;
import com.pri.factorytest.CameraBackSub.CameraBackSub;
import com.pri.factorytest.CameraBackSub.CameraBackYuvSub;
import com.pri.factorytest.CameraBackSub.CameraFrontYuvSub;
import com.pri.factorytest.CameraFront.CameraFront;
import com.pri.factorytest.Charger.Charger;
import com.pri.factorytest.Efuses.Efuses;
import com.pri.factorytest.FM.FM;
import com.pri.factorytest.FingerKeyCheck.FingerKeyCheck;
import com.pri.factorytest.FingerPrint.FingerPrint;
import com.pri.factorytest.GPS.GPS;
import com.pri.factorytest.GSensor.GSensor;
import com.pri.factorytest.GySensor.GySensor;
import com.pri.factorytest.Hall.CameraHall;
import com.pri.factorytest.Hall.CameraHallCali;
import com.pri.factorytest.Hall.Hall;
import com.pri.factorytest.Headset.Headset;
import com.pri.factorytest.Infrared.Infrared;
import com.pri.factorytest.Key.Key;
import com.pri.factorytest.LCD.LCD;
import com.pri.factorytest.LED.LED;
import com.pri.factorytest.LSensor.LSensor;
import com.pri.factorytest.MIC.MIC;
import com.pri.factorytest.MICRe.MICRe;
import com.pri.factorytest.MICRe.MICSubRe;
import com.pri.factorytest.MSensor.MSensor;
import com.pri.factorytest.NFC.NFC;
import com.pri.factorytest.NxpCal.NxpCal;
import com.pri.factorytest.OTG.OTG;
import com.pri.factorytest.PSensor.PSensor;
import com.pri.factorytest.Phone.Phone;
import com.pri.factorytest.PressureSensor.PressureSensor;
import com.pri.factorytest.RAM.RAM;
import com.pri.factorytest.Receiver.Receiver;
import com.pri.factorytest.SDCard.SDCard;
import com.pri.factorytest.SIM.SIM;
import com.pri.factorytest.Speaker.Speaker;
import com.pri.factorytest.Torchled.Torchled;
import com.pri.factorytest.TorchledFront.TorchledFront;
import com.pri.factorytest.TouchPanelEdge.TouchPanelEdge;
import com.pri.factorytest.Vibrate.Vibrate;
import com.pri.factorytest.Voltmeter.Voltmeter;
import com.pri.factorytest.WiFi.WiFi;
import com.pri.factorytest.WirelessCharger.WirelessCharger;
import com.pri.factorytest.YCD.YCD;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mediatek.common.prizeoption.NvramUtils;
import vendor.mediatek.hardware.nvram.V1_0.INvram;

import static com.pri.factorytest.FactoryTestApplication.NETWORK_TYPE;
import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class Utils {

    private static boolean DEBUG = true;
    private static String TAG = "PrizeFactoryTestUtils";
    public static final String PRODUCT_INFO_FILENAME = "/vendor/nvdata/APCFG/APRDEB/PRODUCT_INFO";

    private static long lastClickTime;

    public volatile static int mItemPosition = 0;
    public volatile static boolean toStartAutoTest = false;

    public static final String LCD = "LCD";
    public static final String TOUCH_SCREEN = "touch_screen";
    public static final String RAM = "RAM";
    public static final String REAR_CAM = "rear_camera";
    public static final String FRONT_CAM = "front_camera";
    public static final String REAR_CAM_SUB = "rear_camera_sub";
    public static final String REAR_CAM_SUB2 = "rear_camera_sub2";
    public static final String REAR_CAM_SUB3 = "rear_camera_sub3";
    public static final String FRONT_CAM_SUB = "front_camera_sub";
    public static final String FRONT_CAM_SUB2 = "front_camera_sub2";
    public static final String LED = "LED";
    public static final String YCD = "YCD";
    public static final String BATTERY = "battery";
    public static final String KEYS = "keys";
    public static final String NXPCAL = "NxpCal";
    public static final String SPEAKER = "speaker";
    public static final String MICRO_PHONE_LOOP = "microphone(loop)";
    public static final String MICRO_PHONE = "microphone";
    public static final String SUB_MICRO_PHONE = "sub_microphone";
    public static final String RECEIVER = "receiver";
    public static final String HEADSET = "headset";
    public static final String RADIO = "radio";
    public static final String MOTOR = "motor";
    public static final String LIGHT_SENSOR = "light_sensor";
    public static final String RANG_SENSOR = "rang_sensor";
    public static final String PRESS_SENSOR = "pressure_sensor";
    public static final String PHONE = "phone";
    public static final String BACKLIGHT = "backlight";
    public static final String TF_CARD = "TF_card";
    public static final String SIM = "SIM";
    public static final String FLASH_LAMP = "flash_lamp";
    public static final String GRAVITY_SENSOR = "gravity_sensor";
    public static final String MAGNETIC_SENSOR = "magnetic_sensor";
    public static final String STEP_COUNTER_SENSOR = "step_counter_sensor";
    public static final String FINGERPRINT = "fingerprint";
    public static final String FLASH_LAMP_FRONT = "flash_lamp_front";
    public static final String HALL_SENSOR = "hall_sensor";
    public static final String INFRARED = "infrared";
    public static final String WIFI = "WiFi";
    public static final String BLUETOOTH = "bluetooth";
    public static final String GPS = "GPS";
    public static final String GYROSCOPE_SENSOR = "Gyroscope_Sensor";
    public static final String OTG = "OTG";
    public static final String NFC = "NFC";
    public static final String VOLTMETER = "Voltmeter";
    public static final String WIRELESS_CHARGER = "Wireless_Charger";
    public static final String CAMERA_HALL = "camera_hall";
    public static final String CAMERA_HALL_CALI = "camera_hall_cali";

    public static final String DOUBLE_CAMERA_STANDAR = "double_camera_standar";

    public static final String EFUSE_STATE = "efust_state";
    public static final String FINGER_KEY_CHECK = "finger_key_check";
    public static final int PRIZE_FACTORY_FACTORY_INFO_OFFSET = NvramUtils.PRIZE_FACTORY_FACTORY_INFO_OFFSET;

    /** The prize camera hall cali include 2 items(up/down),each item
     * use 3 bits,start from 244,end 249.
     */
    public static final int PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET = PRIZE_FACTORY_FACTORY_INFO_OFFSET + 94;
    public static final int PRODUCTINFO_CAMERA_HALL_CALI_OFFSET = 341;
    public static final int PRIZE_HUIDING_FINGERPRINT_CALI = 250;
    public static final int PRIZE_DOUBLE_CAMERA_STANDAR = 251;
    /** The prize aging test include 6 items,each item use 3 bits
     *  start from the 265,end 282.
     */
    public static final int PRIZE_AGING_TEST_DURATION = 265;
    public static String CURRENT_LAN = "1".equals(SystemProperties.get("ro.pri_factory_default_lang_en")) ? "en_US" : "zh_CN";

    //prize-added by tangan-add emmc test-begin
	public final static int EMCC_TEST_RESULT_SN_INDEX = Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET+102;
	public final static int EMCC_TEST_START_SCAN_INDEX = Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET+103;
	public final static int EMCC_TEST_START_SCAN_INDEX_LENGTH = 5;
	public final static int EMCC_TEST_CLEAR_COUNT_INDEX =  Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET+108;
	public final static int EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH = 5;
	//prize-added by tangan-add emmc test-end
    private static volatile byte[] nvBuffArrCache = null;

    public synchronized static boolean isNoNFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            if (DEBUG) {
                Log.i("Utils", "------fast click-----");
            }
            return false;
        }
        lastClickTime = time;
        if (DEBUG) {
            Log.i("Utils", "-----NON-fast click-----");
        }
        return true;
    }

    public static String convertNvChars(String conStr) {
        if (conStr == null) {
            return "";
        }
        if (conStr.equals("")) {
            return "";
        }
        String str = conStr;
        int strLength = str.length();
        for (int i = 0; i < strLength; i++) {
            char ch = str.charAt(i);
            if ((ch >= 0x00 && ch <= 0x08)
                    || (ch >= 0x0b && ch <= 0x0c)
                    || (ch >= 0x0e && ch <= 0x1f)) {
                str = str.replace(ch, ' ');
            }
        }

        return str;
    }

    /**
     * @param sn
     * @param index
     */
    public static void writeProInfo(String sn, int index) {
        if (null == sn || sn.length() < 1) {
            Log.e("Utils", "---writeProInfo result length is less 1,not collect ");
            return;
        }
        NvramUtils.writeFactoryNvramInfo(index, sn.length(), sn);
        if (!Stream.of(36, 37, 45, 49).anyMatch(x -> x == index)) {
            return;
        }
        NvramUtils.writeNvramInfo(PRODUCT_INFO_FILENAME, index, sn.length(), sn);
        /*int offsetIndex = index + sn.length();
        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "-----writeProInfo----NvRAMAgent is null");
                return;
            }

            String buff = "";
            try {
                buff = agent.readFileByName(PRODUCT_INFO_FILENAME, offsetIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (DEBUG) {
                Log.d(TAG, "-----writeProInfo-------RAW buff=" + buff);
                Log.d(TAG, "-----writeProInfo-------buff.length()=" + buff.length());
            }

            if (buff.length() < 2 * offsetIndex) {
                if (DEBUG) {
                    Log.d(TAG, "The foramt of NVRAM is not correct");
                }
                return;
            }

            byte[] buffArr = HexDump.hexStringToByteArray(buff.substring(0, buff.length() - 1));
            nvBuffArrCache = buffArr;

            if (DEBUG) {
                Log.d(TAG, "-----writeProInfo-------buffArr.length=" + buffArr.length);
                Log.d(TAG, "-----writeProInfo-------index=" + offsetIndex);
                for (int j = offsetIndex - 5; j < buffArr.length; j++)
                    Log.d(TAG, "-----writeProInfo--buffArr[" + j + "]=" + buffArr[j]);
            }

            ArrayList<Byte> dataArray = new ArrayList<Byte>(offsetIndex);
            byte[] by = sn.toString().getBytes();
            for (int i = 0; i < offsetIndex; i++) {
                if (buffArr[i] == 0x00) {
                    buffArr[i] = " ".toString().getBytes()[0];
                }
                if (i == (offsetIndex - 1)) {
                    Log.d(TAG, "-----writeProInfo------add----by[0]=" + by[0]);
                    dataArray.add(i, by[0]);
                } else {
                    dataArray.add(i, new Byte(buffArr[i]));
                }
            }
            int flag = 0;
            try {
                flag = agent.writeFileByNamevec(PRODUCT_INFO_FILENAME, offsetIndex, dataArray);
                Log.d(TAG, "-----writeProInfo-------flag=" + flag);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "-----writeProInfo----successfully-------end");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * @param offset
     * @param length
     * @return
     */
    public static String readProInfo(int offset,int length) {
        return convertNvChars(NvramUtils.readFactoryNvramInfo(offset, length));
        /*if (DEBUG) {
            Log.d(TAG, "--------readProInfo------begin---");
        }

        if (nvBuffArrCache != null && nvBuffArrCache.length > index) {
            char c = (char) nvBuffArrCache[index - 1];
            String st = String.valueOf(c);
            Log.d(TAG, "-----the " + index + " nv is:" + st + "---read from nvBuffArrCache!!!!!");
            return st;
        }

        String st = "";
        try {
            INvram agent = INvram.getService();
            if (agent == null) {
                Log.e(TAG, "NvRAMAgent is null");
                return "";
            }

            String buff = "";
            try {
                buff = agent.readFileByName(PRODUCT_INFO_FILENAME, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] buffArr = HexDump.hexStringToByteArray(buff.substring(0, buff.length() - 1));
            nvBuffArrCache = buffArr;

            if (DEBUG) {
                Log.d(TAG, "------buffArr.length=" + buffArr.length);
            }

            char c = (char) buffArr[index - 1];
            st = String.valueOf(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (DEBUG) {
            Log.d(TAG, "readProInfo()-----------st=" + st);
            Log.d(TAG, "--------readProInfo------end---");
        }
        return st;*/
    }

    public static void clearNvBuffCache() {
        nvBuffArrCache = null;
    }

    public static boolean isPhoneCalibration() {
        if (PRIZE_CUSTOMER.contains("pcba")) {
            return true;
        }
        String antResult = readProInfo(41, 8);
        boolean wbg = getAntPass(antResult, 41);
        boolean tb = true;
        boolean wb = true;
        boolean cb = true;
        boolean lte = true;
        boolean gb = true;
        if (!wbg) {
            return false;
        }
        if (NETWORK_TYPE.contains("TB")) {
            tb = getAntPass(antResult, 43);
        }
        if (NETWORK_TYPE.contains("WB")) {
            wb = getAntPass(antResult, 44);
        }
        if (NETWORK_TYPE.contains("CB")) {
            cb = getAntPass(antResult, 46);
        }
        if (NETWORK_TYPE.contains("LtB") || NETWORK_TYPE.contains("LfB")) {
            lte = getAntPass(antResult, 47);
        }
        if (NETWORK_TYPE.contains("GB")) {
            gb = getAntPass(antResult, 48);
        }
        return wb && cb && lte && gb;
    }

    public static boolean getAntPass(String antResult, int index){
        String result = null;
        if (antResult.length() > index) {
            result = String.valueOf(antResult.charAt(index));
            result = result.toUpperCase();
        }
        return "P".equals(result);
    }

    public static void startTestItemActivity(Activity activity, String itemName) {
        if (null == itemName || "".equals(itemName)) {
            Log.e(TAG, "----itemName is null or empty----not start activity");
            Toast.makeText(activity, "ERROR:factoryTest itemName is null or empty!!!", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent();
        if (itemName.equals(TOUCH_SCREEN)) {
            intent.setClass(activity, TouchPanelEdge.class);
        } else if (itemName.equals(SIM)) {
            intent.setClass(activity, SIM.class);
        } else if (itemName.equals(LCD)) {
            intent.setClass(activity, LCD.class);
        } else if (itemName.equals(LED)) {
            intent.setClass(activity, LED.class);
        } else if (itemName.equals(YCD)) {
            intent.setClass(activity, YCD.class);
        } else if (itemName.equals(FLASH_LAMP)) {
            intent.setClass(activity, Torchled.class);
        } else if (itemName.equals(FLASH_LAMP_FRONT)) {
            intent.setClass(activity, TorchledFront.class);
        } else if (itemName.equals(MOTOR)) {
            intent.setClass(activity, Vibrate.class);
        } else if (itemName.equals(KEYS)) {
            intent.setClass(activity, Key.class);
        } else if (itemName.equals(HEADSET)) {
            intent.setClass(activity, Headset.class);
        } else if (itemName.equals(RECEIVER)) {
            intent.setClass(activity, Receiver.class);
        } else if (itemName.equals(NXPCAL)) {
            intent.setClass(activity, NxpCal.class);
        } else if (itemName.equals(SPEAKER)) {
            intent.setClass(activity, Speaker.class);
        } else if (itemName.equals(RADIO)) {
            intent.setClass(activity, FM.class);
        } else if (itemName.equals(MICRO_PHONE_LOOP)) {
            intent.setClass(activity, MIC.class);
        } else if (itemName.equals(MICRO_PHONE)) {
            intent.setClass(activity, MICRe.class);
        } else if (itemName.equals(SUB_MICRO_PHONE)) {
            intent.setClass(activity, MICSubRe.class);
        } else if (itemName.equals(TF_CARD)) {
            intent.setClass(activity, SDCard.class);
        } else if (itemName.equals(RAM)) {
            intent.setClass(activity, RAM.class);
        } else if (itemName.equals(REAR_CAM)) {
            intent.setClass(activity, CameraBack.class);
        } else if (itemName.equals(REAR_CAM_SUB)) {
            if (SystemProperties.get("ro.pri_rear_camera_sub").equals("1")) {
                intent.putExtra("rear_camer_id", 2);
                intent.setClass(activity, CameraBackSub.class);
            } else if (SystemProperties.get("ro.pri_rear_camera_sub_als").equals("1")) {
                intent.setClass(activity, CameraBackAlsSub.class);
            } else if (isFileExists("/sys/kernel/dcam/dcam_r_value") ||
                    isFileExists("/sys/kernel/spc/spc_r/value") ||
                    isFileExists("/sys/kernel/spc/spc_r_1/value")) {
                intent.setClass(activity, CameraBackYuvSub.class);
            }
        } else if (itemName.equals(REAR_CAM_SUB2)) {
            intent.putExtra("rear_camer_id", 3);
            intent.setClass(activity, CameraBackSub.class);
        } else if (itemName.equals(REAR_CAM_SUB3)) {
            intent.putExtra("rear_camer_id", 4);
            intent.setClass(activity, CameraBackSub.class);
        } else if (itemName.equals(FRONT_CAM_SUB)) {
            intent.setClass(activity, CameraFrontYuvSub.class);
        } else if (itemName.equals(FRONT_CAM_SUB2)) {

        } else if (itemName.equals(FRONT_CAM)) {
            intent.setClass(activity, CameraFront.class);
        } else if (itemName.equals(BATTERY)) {
            intent.setClass(activity, Charger.class);
        } else if (itemName.equals(LIGHT_SENSOR)) {
            intent.setClass(activity, LSensor.class);
        } else if (itemName.equals(PHONE)) {
            intent.setClass(activity, Phone.class);
        } else if (itemName.equals(BACKLIGHT)) {
            intent.setClass(activity, Brightness.class);
        } else if (itemName.equals(GRAVITY_SENSOR)) {
            intent.setClass(activity, GSensor.class);
        } else if (itemName.equals(FINGERPRINT)) {
            intent.setClass(activity, FingerPrint.class);
        } else if (itemName.equals(RANG_SENSOR)) {
            intent.setClass(activity, PSensor.class);
        } else if (itemName.equals(MAGNETIC_SENSOR)) {
            intent.setClass(activity, MSensor.class);
        } else if (itemName.equals(STEP_COUNTER_SENSOR)) {
            intent.setClass(activity, CSensor.class);
        } else if (itemName.equals(GYROSCOPE_SENSOR)) {
            intent.setClass(activity, GySensor.class);
        } else if (itemName.equals(OTG)) {
            intent.setClass(activity, OTG.class);
        } else if (itemName.equals(WIFI)) {
            intent.setClass(activity, WiFi.class);
        } else if (itemName.equals(BLUETOOTH)) {
            intent.setClass(activity, BlueTooth.class);
        } else if (itemName.equals(GPS)) {
            intent.setClass(activity, GPS.class);
        } else if (itemName.equals(HALL_SENSOR)) {
            intent.setClass(activity, Hall.class);
        } else if (itemName.equals(INFRARED)) {
            intent.setClass(activity, Infrared.class);
        } else if (itemName.equals(NFC)) {
            intent.setClass(activity, NFC.class);
        } else if (itemName.equals(VOLTMETER)) {
            intent.setClass(activity, Voltmeter.class);
        } else if (itemName.equals(PRESS_SENSOR)) {
            intent.setClass(activity, PressureSensor.class);
        } else if (itemName.equals(WIRELESS_CHARGER)) {
            intent.setClass(activity, WirelessCharger.class);
        } else if (itemName.equals(CAMERA_HALL)) {
            intent.setClass(activity, CameraHall.class);
        } else if (itemName.equals(CAMERA_HALL_CALI)) {
            intent.setClass(activity, CameraHallCali.class);
        } else if (itemName.equals(DOUBLE_CAMERA_STANDAR)) {
            intent.setClass(activity, DoubleCameraOperation.class);
        } else if (itemName.equals(EFUSE_STATE)) {
            intent.setClass(activity, Efuses.class);
        } else if (itemName.equals(FINGER_KEY_CHECK)) {
            intent.setClass(activity, FingerKeyCheck.class);
        }
        String intentClassName = null;
        try {
            intentClassName = intent.getComponent().getClassName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != intentClassName) {
            intent.putExtra("mTestValue", itemName);
            activity.startActivityForResult(intent, 0);
        } else {
            Log.e(TAG, "----itemName is:" + itemName + ",But not start,since the Name is notEquals!!!");
            Toast.makeText(activity, "ERROR:itemName is notEquals!!!", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isFileExists(String path) {
        if (path != null) {
            File f = new File(path);
            return f.exists();
        }
        Log.e(TAG, "file path is null!");
        return false;
    }

    public static String readContextFromStream(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = bis.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, length));
            }
            bis.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static boolean isNotchScreen(View view) {
        Context context = view.getContext();
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_fillMainBuiltInDisplayCutout);
    }

    public static void paddingLayout(View firstView, int left, int top, int right, int bottom) {
        /*LinearLayout ll =  (LinearLayout) firstView.getParent();
        ll.setPadding(left, top, right, bottom);*/
        ViewGroup vg = (ViewGroup) firstView.getParent();
        boolean isNotchScreen = isNotchScreen(firstView);
        Log.i(TAG, "---isNotchScreen---" + isNotchScreen);
        if (isNotchScreen) {
            vg.setPadding(left + 2, 5, right + 2, bottom);
        } else {
            vg.setPadding(left + 10, top, right, bottom);
        }
    }

    public static Context createConfigurationResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        switch (language) {
            case "en_US":
                Log.i(TAG, "---createConfigurationResources-en_us");
                configuration.setLocale(Locale.ENGLISH);
                break;
            case "zh_CN":
                Log.i(TAG, "---createConfigurationResources-zh_cn");
                configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
                break;
            default:
                configuration.setLocale(Locale.ENGLISH);
                break;
        }
        return context.createConfigurationContext(configuration);
    }

    public static boolean hasComponentPkg(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSupportDoubuleCameraStand(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("com.kb.action.FACTORY_CAMERA");
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return Optional.ofNullable(list).map(x -> x.size() > 0).orElse(false);
    }

    public static boolean isWiredHeadsetPluggedIn(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        Collection<Integer> deviceColl = Arrays.stream(devices).map(AudioDeviceInfo::getType).collect(Collectors.toList());
        return Stream.of(AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE).anyMatch(x -> deviceColl.contains(x));
    }
}
