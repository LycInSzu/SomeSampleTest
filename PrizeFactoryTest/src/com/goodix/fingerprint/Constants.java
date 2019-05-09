/*
 * Copyright (C) 2013-2018, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.graphics.Color;
import android.os.Environment;
import com.goodix.fingerprint.BaikalConstants;
import java.util.HashMap;

public class Constants {
    private static HashMap<Integer, String> mBaikalHashMap = new HashMap<Integer, String>();
    private static HashMap<Integer, String> mShenzhenHashMap = new HashMap<Integer, String>();

    public static final String GOODIX_FINGERPRINT_SERVICE_NAME = "com.goodix.FingerprintService";

    public static final int GF_CHIP_BAIKAL_SE1 = 0;
    public static final int GF_CHIP_BAIKAL_SE2 = GF_CHIP_BAIKAL_SE1 + 1;
    public static final int GF_CHIP_BAIKAL_SE3_L = GF_CHIP_BAIKAL_SE2 + 1;
    public static final int GF_CHIP_BAIKAL_SE3_M = GF_CHIP_BAIKAL_SE3_L + 1;
    public static final int GF_CHIP_BAIKAL_SE3_N = GF_CHIP_BAIKAL_SE3_M + 1;
    public static final int GF_CHIP_BAIKAL_R0 = GF_CHIP_BAIKAL_SE3_N + 1;
    public static final int GF_CHIP_BAIKAL_R1 = GF_CHIP_BAIKAL_R0 + 1;
    public static final int GF_CHIP_BAIKAL_MP1 = GF_CHIP_BAIKAL_R1 + 1;
    public static final int GF_CHIP_UNKNOWN = GF_CHIP_BAIKAL_MP1 + 1;

    public static final int ENROLLING_MIN_TEMPLATES_NUM[] = {
            8, // GF_CHIP_BAIKAL_SE1
            8, // GF_CHIP_BAIKAL_SE2
            8, // GF_CHIP_BAIKAL_SE3_L
            8, // GF_CHIP_BAIKAL_SE3_M
            8, // GF_CHIP_BAIKAL_SE3_N
            8, // GF_CHIP_BAIKAL_R0
            8, // GF_CHIP_BAIKAL_R1
            8 // GF_CHIP_BAIKAL_MP1
    };

    public static final int MAX_TEMPLATES_NUM[] = {
            20, // GF_CHIP_BAIKAL_SE1
            20, // GF_CHIP_BAIKAL_SE2
            20, // GF_CHIP_BAIKAL_SE3_L
            20, // GF_CHIP_BAIKAL_SE3_M
            20, // GF_CHIP_BAIKAL_SE3_N
            20, // GF_CHIP_BAIKAL_R0
            20, // GF_CHIP_BAIKAL_R1
            20  // GF_CHIP_BAIKAL_MP1
    };

    public static final int GF_SHENZHEN = 0;
    public static final int GF_BAIKAL = GF_SHENZHEN + 1;
    public static final int GF_UNKNOWN_SERIES = GF_BAIKAL + 1;

    public static final int GF_SAFE_CLASS_HIGHEST = 0;
    public static final int GF_SAFE_CLASS_HIGH = 1;
    public static final int GF_SAFE_CLASS_MEDIUM = 2;
    public static final int GF_SAFE_CLASS_LOW = 3;
    public static final int GF_SAFE_CLASS_LOWEST = 4;

    public static final int GF_AUTHENTICATE_BY_USE_RECENTLY = 0;
    public static final int GF_AUTHENTICATE_BY_ENROLL_ORDER = 1;
    public static final int GF_AUTHENTICATE_BY_REVERSE_ENROLL_ORDER = 2;

    /*****************daemon public message start*********************/
    public static final int GF_FINGERPRINT_ERROR = -1;
    public static final int GF_FINGERPRINT_ACQUIRED = 1;
    public static final int GF_FINGERPRINT_TEMPLATE_ENROLLING = 3;
    public static final int GF_FINGERPRINT_TEMPLATE_REMOVED = 4;
    public static final int GF_FINGERPRINT_AUTHENTICATED = 5;
    public static final int GF_FINGERPRINT_TEMPLATE_ENUMERATING = 6;
    public static final int GF_FINGERPRINT_TEST_CMD = 1001;
    public static final int GF_FINGERPRINT_DUMP_DATA = GF_FINGERPRINT_TEST_CMD + 1;
    public static final int GF_FINGERPRINT_AUTHENTICATED_FIDO = GF_FINGERPRINT_DUMP_DATA + 1;
	public static final int GF_FINGERPRINT_MSG_TYPE_MAX = GF_FINGERPRINT_AUTHENTICATED_FIDO + 1;
    /*****************daemon public message end*********************/

    /******************public test cmd start***************************/
    public static final int CMD_TEST_UNKNOWN = -1;
    public static final int CMD_TEST_ENUMERATE = 0;
    public static final int CMD_TEST_GET_VERSION = CMD_TEST_ENUMERATE + 1;
    public static final int CMD_TEST_RESET_PIN = CMD_TEST_GET_VERSION + 1;
    public static final int CMD_TEST_INTERRUPT_PIN = CMD_TEST_RESET_PIN + 1;
    public static final int CMD_TEST_CANCEL = CMD_TEST_INTERRUPT_PIN + 1;
    public static final int CMD_TEST_GET_CONFIG = CMD_TEST_CANCEL + 1;
    public static final int CMD_TEST_SET_CONFIG = CMD_TEST_GET_CONFIG + 1;

    public static final int CMD_TEST_MAX = CMD_TEST_SET_CONFIG + 1;  // 7
    /******************public test cmd end*****************************/

    public static final int CMD_DUMP_DATA = 1000;
    public static final int CMD_CANCEL_DUMP_DATA = CMD_DUMP_DATA + 1;
    public static final int CMD_DUMP_TEMPLATES = CMD_CANCEL_DUMP_DATA + 1;
    public static final int CMD_DUMP_PATH = CMD_DUMP_TEMPLATES + 1;
    public static final int CMD_DUMP_NAV_BASE = CMD_DUMP_PATH + 1;
    public static final int CMD_DUMP_FINGER_BASE = CMD_DUMP_NAV_BASE + 1;

    public static final int DUMP_PATH_SDCARD = 0;
    public static final int DUMP_PATH_DATA = 1;

    static {
        mBaikalHashMap.put(CMD_TEST_ENUMERATE, "CMD_TEST_ENUMERATE");
        mBaikalHashMap.put(CMD_TEST_GET_VERSION, "CMD_TEST_GET_VERSION");
        mBaikalHashMap.put(CMD_TEST_RESET_PIN, "CMD_TEST_RESET_PIN");
        mBaikalHashMap.put(CMD_TEST_INTERRUPT_PIN, "CMD_TEST_INTERRUPT_PIN");
        mBaikalHashMap.put(CMD_TEST_CANCEL, "CMD_TEST_CANCEL");
        mBaikalHashMap.put(CMD_TEST_GET_CONFIG, "CMD_TEST_GET_CONFIG");
        mBaikalHashMap.put(CMD_TEST_SET_CONFIG, "CMD_TEST_SET_CONFIG");

        mShenzhenHashMap.putAll(mBaikalHashMap);

        mBaikalHashMap.put(BaikalConstants.CMD_TEST_BAIKAL_FINGER_DOWN, "CMD_TEST_BAIKAL_FINGER_DOWN");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_BAIKAL_FINGER_UP, "CMD_TEST_BAIKAL_FINGER_UP");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_CONTRAST, "CMD_TEST_CONTRAST");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_OTP, "CMD_TEST_OTP");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_K_B_CALIBRATION, "CMD_TEST_K_B_CALIBRATION");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_FIND_SENSOR, "CMD_TEST_FIND_SENSOR");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_CIRCLE_LOCATION, "CMD_TEST_CIRCLE_LOCATION");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_CIRCLE_DEVIATION, "CMD_TEST_CIRCLE_DEVIATION");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_UPDATA_CIRCLE_BASE_FILE, "CMD_TEST_UPDATA_CIRCLE_BASE_FILE");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_REMOVE_CIRCLE_BASE_FILE, "CMD_TEST_REMOVE_CIRCLE_BASE_FILE");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_AUTO_FIND_SENSOR_INIT, "CMD_TEST_AUTO_FIND_SENSOR_INIT");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_AUTO_FIND_SENSOR, "CMD_TEST_AUTO_FIND_SENSOR");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_SPI, "CMD_TEST_SPI");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_RESET_PIN_AND_INTERRUPT_PIN, "CMD_TEST_RESET_PIN_AND_INTERRUPT_PIN");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_OTP_FLASH, "CMD_TEST_OTP_FLASH");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_PIXEL_OPENSHORT, "CMD_TEST_PIXEL_OPENSHORT");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_PERFORMANCE_TESTING, "CMD_TEST_PERFORMANCE_TESTING");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_FORCE_KEY, "CMD_TEST_FORCE_KEY");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_PERFORMANCE, "CMD_TEST_PERFORMANCE");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_SENSOR_LOCATION_INIT, "CMD_TEST_SENSOR_LOCATION_INIT");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_SENSOR_LOCATION, "CMD_TEST_SENSOR_LOCATION");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_PIXEL_OPEN_SHORT, "CMD_TEST_PIXEL_OPEN_SHORT");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_NOISE, "CMD_TEST_NOISE");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_MSG_PHONE_UNLOCK, "CMD_TEST_MSG_PHONE_UNLOCK");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_GET_RESET_COUNTS, "CMD_TEST_GET_RESET_COUNTS");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_CLEAR_RESET_COUNTS, "CMD_TEST_CLEAR_RESET_COUNTS");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_START_GET_RESET_COUNTS, "CMD_TEST_START_GET_RESET_COUNTS");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_STOP_GET_RESET_COUNTS, "CMD_TEST_STOP_GET_RESET_COUNTS");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_LOCATION_CIRCLE_CALIBRATION, "CMD_TEST_LOCATION_CIRCLE_CALIBRATION");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_SPI_VIVO, "CMD_TEST_SPI_VIVO");
        mBaikalHashMap.put(BaikalConstants.CMD_TEST_FLASH_RW, "CMD_TEST_FLASH_RW");

        // TODO put shenzhen cmd

        //huawei
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_AUTO_TEST, "CMD_MMI_AUTO_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_TYPE_INTERRUPT_TEST, "CMD_MMI_TYPE_INTERRUPT_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_FAKE_FINGER_TEST, "CMD_MMI_FAKE_FINGER_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_SNR_SINGAL_IMAGE_TEST, "CMD_MMI_SNR_SINGAL_IMAGE_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_SNR_WHITE_IMAGE_TEST, "CMD_MMI_SNR_WHITE_IMAGE_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_BUBBLE_TEST, "CMD_MMI_BUBBLE_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_SN_TEST, "CMD_MMI_SN_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_OPTICAL_CALIBRATION_TEST, "CMD_MMI_OPTICAL_CALIBRATION_TEST");
        mBaikalHashMap.put(BaikalConstants.CMD_MMI_SCENE_TEST, "CMD_MMI_SCENE_TEST");
    }

    public static CharSequence baikalTestCmdIdToString(int cmdId) {
        if (null == mBaikalHashMap) {
            return "mBaikalHashMap is null";
        } else if (null == mBaikalHashMap.get(cmdId)) {
            return "cmdId is unknown";
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("strCmdId = ");
        stringBuilder.append(mBaikalHashMap.get(cmdId));
        stringBuilder.append(" cmdId = ");
        stringBuilder.append(cmdId);

        return stringBuilder.toString();
    }

    public static CharSequence shenzhenTestCmdIdToString(int cmdId) {
        if (null == mShenzhenHashMap) {
            return "mBaikalHashMap is null";
        } else if (null == mShenzhenHashMap.get(cmdId)) {
            return "cmdId is unknown";
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("strCmdId = ");
        stringBuilder.append(mShenzhenHashMap.get(cmdId));
        stringBuilder.append(" cmdId = ");
        stringBuilder.append(cmdId);

        return stringBuilder.toString();
    }

    public static CharSequence testCmdIdToString(int chipSeries, int cmdId) {
        if (chipSeries == GF_BAIKAL) {
            return baikalTestCmdIdToString(cmdId);
        } else {
            return shenzhenTestCmdIdToString(cmdId);
        }
    }

    public static final int ALGO_VERSION_INFO_LEN = 64;
    public static final int FW_VERSION_INFO_LEN = 64;
    public static final int TEE_VERSION_INFO_LEN = 72;
    public static final int TA_VERSION_INFO_LEN = 64;
    public static final int VENDOR_ID_LEN = 32;
    public static final int PRODUCTION_DATE_LEN = 32;

    public static final int TEST_CAPTURE_VALID_IMAGE_QUALITY_THRESHOLD = 15;
    public static final int TEST_CAPTURE_VALID_IMAGE_AREA_THRESHOLD = 65;

    public static final String TEST_FW_VERSION_GFX18 = "GFx18M_1.04.04";
    public static final String TEST_FW_VERSION_GFX16 = "GFx16M_1.04.04";

    public static final long TEST_PERFORMANCE_TOTAL_TIME = 400;

    public static final long TEST_TIMEOUT_MS = 30 * 1000;

    public static final String TEST_HISTORY_PATH = "GFTest";

    public static final int AUTO_TEST_TIME_INTERVAL = 5000; // 5 seconds
    public static final int AUTO_TEST_BIO_PREPARE_TIME = 5000;

    public static final String PROPERTY_AUTO_TEST = "sys.goodix.starttest";
    public static final String PROPERTY_TEST_ITME_TIMEOUT = "sys.goodix.timeout";
    public static final String PROPERTY_FINGER_STATUS = "sys.goodix.fingerstatus";
    public static final String PROPERTY_TEST_ORDER = "sys.goodix.testorder";
    public static final String PROPERTY_SWITCH_FINGER_TIME = "sys.goodix.switchfingertime";

    public static final String KEY_AUTO_TEST = "auto_test";

    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_ACQUIRED_PARTIAL = 1011;
    public static final int FINGERPRINT_ERROR_ACQUIRED_IMAGER_DIRTY = 1012;

    public static final int FINGERPRINT_ERROR_TOO_MUCH_UNDER_SATURATED_PIXELS = 1068;
    public static final int FINGERPRINT_ERROR_TOO_MUCH_OVER_SATURATED_PIXELS = 1069;

    public static final int TEST_MEM_MANAGER_MIN_HEAP_SIZE = 2 * 1024 * 1024;
    public static final int TEST_MEM_MANAGER_MAX_HEAP_SIZE = 4 * 1024 * 1024;

    //for baikal, default config is for alpha3
    public static final int DEFAULT_SENSOR_X = 311;
    public static final int DEFAULT_SENSOR_Y = 934;
    public static final int DEFAULT_SENSOR_WIDTH = 110;
    public static final int DEFAULT_SENSOR_HEIGHT = 110;
    public static final boolean DEFAULT_LOCK_ASPECT_RATIO = true;
    public static final int DEFAULT_ASPECT_RATIO_WIDTH = 10;
    public static final int DEFAULT_ASPECT_RATIO_HEIGHT = 10;
    public static final int DEFAULT_PREVIEW_SCALE_RATIO = 150;

    public static final boolean DEFAULT_TOUCHED_BASE = false;
    public static final int DEFAULT_SKIP_FRAME_NUM = 2;
    public static final int DEFAULT_BASE_FRAME_NUM = 10;
    public static final int DEFAULT_IMAGE_FRAME_NUM = 1;
    public static final int DEFAULT_RATIO_BASE = 10;
    public static final int DEFAULT_RATIO_IMAGE = 10;
    public static final boolean DEFAULT_DUMP_DATA = false;
    public static final boolean DEFAULT_DEBUG_MODE = true;
    public static final boolean DEFAULT_PRESSURE_ENABLED = true;
    public static final boolean DEFAULT_ADJUST_SCREEN_BRIGHTNESS = true;
    public static final boolean DEFAULT_IMAGE_QUALITY = false;

    public static final int DEFAULT_SCREEN_BRIGHTNESS = 1000;
    public static final int DEFAULT_SAMPLE_NUM = 100;
    public static final int DEFAULT_SENSOR_AREA_CYAN_COLOR = Color.CYAN;
    public static final int DEFAULT_SENSOR_AREA_BLACK_COLOR = Color.BLACK;
    public static final String SENSOR_AREA_IMAGE_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/sensor_area.bmp";

    public static final int DEFAULT_CALIBRATE_DELAY_TIME = 10;
    public static final int DEFAULT_CALIBRATE_BRIGHTNESS_LEVELS = 3;
    public static final int DEFAULT_CALIBRATE_SAMPLE_NUM_PER_LEVEL = 10;
    public static final boolean DEFAULT_CALIBRATE_AUTO_CALIBRATE = true;
    public static final boolean DEFAULT_CUSTOM_BRIGHTNESS_LEVEL = true;
    public static final int CALIBRATE_TYPE_MANUAL = 0;
    public static final int CALIBRATE_TYPE_AUTO = 1;
    public static final int CALIBRATE_TYPE_KB_CALIBRATE = 2;
    public static final int CALIBRATE_TYPE_PREPROCESS_CHART = 3;
    public static final int CALIBRATE_TYPE_POSITION_CALIBRATE = 4;
    public static final int CALIBRATE_TYPE_CAPTURE_FINGER = 5;

    public static final String PREFERENCE_SENSOR_INFO = "sensor_info";
    public static final String PREFERENCE_NAME = "settings";
    public static final String PREFERENCE_CALIBRATE = "calibrate";

    public static final String PREFERENCE_KEY_SENSOR_X = "sensor_x";
    public static final String PREFERENCE_KEY_SENSOR_Y = "sensor_y";
    public static final String PREFERENCE_KEY_SENSOR_WIDTH = "sensor_width";
    public static final String PREFERENCE_KEY_SENSOR_HEIGHT = "sensor_height";
    public static final String PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH = "aspect_ratio_width";
    public static final String PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT = "aspect_ratio_height";
    public static final String PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO = "preview_scale_ratio";
    public static final String PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR = "sensor_area_background_color";

    public static final String PREFERENCE_KEY_TOUCHED_BASE = "touched_base";
    public static final String PREFERENCE_KEY_SKIP_FRAME_NUM = "skip_frame_num";
    public static final String PREFERENCE_KEY_BASE_FRAME_NUM = "base_frame_num";
    public static final String PREFERENCE_KEY_IMAGE_FRAME_NUM = "image_frame_num";
    public static final String PREFERENCE_KEY_RATIO_BASE = "ratio_base";
    public static final String PREFERENCE_KEY_RATIO_IMAGE = "ratio_image";
    public static final String PREFERENCE_KEY_DUMP_DATA = "dump_data";
    public static final String PREFERENCE_KEY_DEBUG_MODE = "debug_mode";
    public static final String PREFERENCE_KEY_PRESSURE_ENABLED = "pressure_enabled";
    public static final String PREFERENCE_KEY_ADJUST_SCREEN_BRIGHTNESS = "adjust_screen_brightness";
    public static final String PREFERENCE_KEY_IMAGE_QUALITY = "image_quality";

    public static final String PREFERENCE_KEY_CALIBRATE_DELAY_TIME ="calibrate_delay_time";
    public static final String PREFERENCE_KEY_CALIBRATE_BRIGHTNESS_LEVELS ="calibrate_brightness_levels";
    public static final String PREFERENCE_KEY_CALIBRATE_SAMPLE_NUM_PER_LEVEL ="calibrate_sample_num_per_level";
    public static final String PREFERENCE_KEY_CALIBRATE_AUTO_CALIBRATE = "calibrate_auto_calibrate";
    public static final String PERFERENCE_KEY_CUSTOM_BRIGHTNESS = "custom_brightness";

    public static final String PREFERENCE_KEY_SAMPLE_NUM = "sample_num";
    public static final String PREFERENCE_KEY_BRIGHTNESS_LEVEL = "brightness_level";

    public static final int CALIBRATION_BLACK_BASE = 2;
    public static final int CALIBRATION_WHITE_BASE = 1;
    public static final int CALIBRATION_IDLE = 0;

    public static final int FINGERPRINT_DATA_TYPE_RAWDATA = 0;
    public static final int FINGERPRINT_DATA_TYPE_BMPDATA = 1;

    public static final int GF_ERROR_TEST_RESET_INIT_PIN_MCU = 1106;
    public static final int GF_ERROR_TEST_RESET_INIT_PIN_SENSOR = 1107;
}
