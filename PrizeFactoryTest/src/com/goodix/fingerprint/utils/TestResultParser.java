/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.utils;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.utils.BaikalTestResultParser;
import com.goodix.fingerprint.utils.ShenzhenTestResultParser;

import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

public class TestResultParser {
    private static final String TAG = "TestResultParser";

    /************ public test token define start ***************/
    public static final int TEST_TOKEN_ERROR_CODE = 100;
    public static final int TEST_TOKEN_CHIP_TYPE = TEST_TOKEN_ERROR_CODE + 1;
    public static final int TEST_TOKEN_CHIP_SERIES = TEST_TOKEN_CHIP_TYPE + 1;

    public static final int TEST_TOKEN_ALGO_VERSION = 200;
    public static final int TEST_TOKEN_PREPROCESS_VERSION = TEST_TOKEN_ALGO_VERSION + 1;
    public static final int TEST_TOKEN_PRODUCTION_ALGO_VERSION = TEST_TOKEN_PREPROCESS_VERSION + 1;
    public static final int TEST_TOKEN_FW_VERSION = TEST_TOKEN_PRODUCTION_ALGO_VERSION + 1;
    public static final int TEST_TOKEN_TEE_VERSION = TEST_TOKEN_FW_VERSION + 1;
    public static final int TEST_TOKEN_TA_VERSION = TEST_TOKEN_TEE_VERSION + 1;
    public static final int TEST_TOKEN_CHIP_ID = TEST_TOKEN_TA_VERSION + 1;
    public static final int TEST_TOKEN_VENDOR_ID = TEST_TOKEN_CHIP_ID + 1;
    public static final int TEST_TOKEN_SENSOR_ID = TEST_TOKEN_VENDOR_ID + 1;
    public static final int TEST_TOKEN_PRODUCTION_DATE = TEST_TOKEN_SENSOR_ID + 1;
    public static final int TEST_TOKEN_SENSOR_OTP_TYPE = TEST_TOKEN_PRODUCTION_DATE + 1;
    public static final int TEST_TOKEN_CODE_FW_VERSION = TEST_TOKEN_SENSOR_OTP_TYPE + 1;

    public static final int TEST_TOKEN_MCU_ID = TEST_TOKEN_CODE_FW_VERSION + 1;
    public static final int TEST_TOKEN_FLASH_ID = TEST_TOKEN_MCU_ID + 1;
    public static final int TEST_TOKEN_PMIC_ID = TEST_TOKEN_FLASH_ID + 1;

    public static final int TEST_TOKEN_AD_VERSION = 215;
    public static final int TEST_TOKEN_LENS_TYPE = TEST_TOKEN_AD_VERSION + 1;

    public static final int TEST_TOKEN_AVG_DIFF_VAL = 300;
    public static final int TEST_TOKEN_NOISE = TEST_TOKEN_AVG_DIFF_VAL + 1;
    public static final int TEST_TOKEN_BAD_PIXEL_NUM = TEST_TOKEN_NOISE + 1;
    public static final int TEST_TOKEN_FDT_BAD_AREA_NUM = TEST_TOKEN_BAD_PIXEL_NUM + 1;
    public static final int TEST_TOKEN_LOCAL_BAD_PIXEL_NUM = TEST_TOKEN_FDT_BAD_AREA_NUM + 1;
    public static final int TEST_TOKEN_FRAME_NUM = TEST_TOKEN_LOCAL_BAD_PIXEL_NUM + 1;
    public static final int TEST_TOKEN_MAX_FRAME_NUM = TEST_TOKEN_FRAME_NUM + 1;
    public static final int TEST_TOKEN_DATA_DEVIATION_DIFF = TEST_TOKEN_MAX_FRAME_NUM + 1;
    public static final int TEST_TOKEN_ALL_TILT_ANGLE = TEST_TOKEN_DATA_DEVIATION_DIFF + 1;
    public static final int TEST_TOKEN_BLOCK_TILT_ANGLE_MAX = TEST_TOKEN_ALL_TILT_ANGLE + 1;
    public static final int TEST_TOKEN_LOCAL_WORST = TEST_TOKEN_BLOCK_TILT_ANGLE_MAX + 1;
    public static final int TEST_TOKEN_SINGULAR = TEST_TOKEN_LOCAL_WORST + 1;
    public static final int TEST_TOKEN_IN_CIRCLE = TEST_TOKEN_SINGULAR + 1;
    public static final int TEST_TOKEN_BIG_BUBBLE = TEST_TOKEN_IN_CIRCLE + 1;
    public static final int TEST_TOKEN_LINE = TEST_TOKEN_BIG_BUBBLE + 1;
    public static final int TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM = TEST_TOKEN_LINE +1;
    public static final int TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM = TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM + 1;
    public static final int TEST_TOKEN_FLATNESS_BAD_PIXEL_NUM = TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM + 1;
    public static final int TEST_TOKEN_IS_BAD_LINE = TEST_TOKEN_FLATNESS_BAD_PIXEL_NUM + 1;

    public static final int TEST_TOKEN_GET_DR_TIMESTAMP_TIME = 400;
    public static final int TEST_TOKEN_GET_MODE_TIME = TEST_TOKEN_GET_DR_TIMESTAMP_TIME + 1;
    public static final int TEST_TOKEN_GET_CHIP_ID_TIME = TEST_TOKEN_GET_MODE_TIME + 1;
    public static final int TEST_TOKEN_GET_VENDOR_ID_TIME = TEST_TOKEN_GET_CHIP_ID_TIME + 1;
    public static final int TEST_TOKEN_GET_SENSOR_ID_TIME = TEST_TOKEN_GET_VENDOR_ID_TIME + 1;
    public static final int TEST_TOKEN_GET_FW_VERSION_TIME = TEST_TOKEN_GET_SENSOR_ID_TIME + 1;
    public static final int TEST_TOKEN_GET_IMAGE_TIME = TEST_TOKEN_GET_FW_VERSION_TIME + 1;
    public static final int TEST_TOKEN_RAW_DATA_LEN = TEST_TOKEN_GET_IMAGE_TIME + 1;
    public static final int TEST_TOKEN_CFG_DATA = TEST_TOKEN_RAW_DATA_LEN + 1;
    public static final int TEST_TOKEN_CFG_DATA_LEN = TEST_TOKEN_CFG_DATA + 1;
    public static final int TEST_TOKEN_FW_DATA = TEST_TOKEN_CFG_DATA_LEN + 1;
    public static final int TEST_TOKEN_FW_DATA_LEN = TEST_TOKEN_FW_DATA + 1;

    public static final int TEST_TOKEN_IMAGE_QUALITY = 500;
    public static final int TEST_TOKEN_VALID_AREA = TEST_TOKEN_IMAGE_QUALITY + 1;
    public static final int TEST_TOKEN_KEY_POINT_NUM = TEST_TOKEN_VALID_AREA + 1;
    public static final int TEST_TOKEN_INCREATE_RATE = TEST_TOKEN_KEY_POINT_NUM + 1;
    public static final int TEST_TOKEN_OVERLAY = TEST_TOKEN_INCREATE_RATE + 1;
    public static final int TEST_TOKEN_GET_RAW_DATA_TIME = TEST_TOKEN_OVERLAY + 1;
    public static final int TEST_TOKEN_PREPROCESS_TIME = TEST_TOKEN_GET_RAW_DATA_TIME + 1;
    public static final int TEST_TOKEN_ALGO_START_TIME = TEST_TOKEN_PREPROCESS_TIME + 1;
    public static final int TEST_TOKEN_GET_FEATURE_TIME = TEST_TOKEN_ALGO_START_TIME + 1;
    public static final int TEST_TOKEN_ENROLL_TIME = TEST_TOKEN_GET_FEATURE_TIME + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_TIME = TEST_TOKEN_ENROLL_TIME + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_ID = TEST_TOKEN_AUTHENTICATE_TIME + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_UPDATE_FLAG = TEST_TOKEN_AUTHENTICATE_ID + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_FINGER_COUNT = TEST_TOKEN_AUTHENTICATE_UPDATE_FLAG + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_FINGER_ITME = TEST_TOKEN_AUTHENTICATE_FINGER_COUNT + 1;
    public static final int TEST_TOKEN_TOTAL_TIME = TEST_TOKEN_AUTHENTICATE_FINGER_ITME + 1;
    public static final int TEST_TOKEN_GET_GSC_DATA_TIME = TEST_TOKEN_TOTAL_TIME + 1;
    public static final int TEST_TOKEN_BIO_ASSAY_TIME = TEST_TOKEN_GET_GSC_DATA_TIME + 1;

    public static final int TEST_TOKEN_RESET_FLAG = 600;

    public static final int TEST_TOKEN_BMP_DATA_WIDTH = 690;
    public static final int TEST_TOKEN_BMP_DATA_HEIGHT = TEST_TOKEN_BMP_DATA_WIDTH + 1;

    public static final int TEST_TOKEN_RAW_DATA = 700;
    public static final int TEST_TOKEN_BMP_DATA = TEST_TOKEN_RAW_DATA + 1;
    public static final int TEST_TOKEN_ALGO_INDEX = TEST_TOKEN_BMP_DATA + 1;
    public static final int TEST_TOKEN_SAFE_CLASS = TEST_TOKEN_ALGO_INDEX + 1;
    public static final int TEST_TOKEN_TEMPLATE_COUNT = TEST_TOKEN_SAFE_CLASS + 1;
    public static final int TEST_TOKEN_GSC_DATA = TEST_TOKEN_TEMPLATE_COUNT + 1;
    public static final int TEST_TOKEN_HBD_BASE = TEST_TOKEN_GSC_DATA + 1;
    public static final int TEST_TOKEN_HBD_AVG = TEST_TOKEN_HBD_BASE + 1;
    public static final int TEST_TOKEN_HBD_RAW_DATA = TEST_TOKEN_HBD_AVG + 1;
    public static final int TEST_TOKEN_ELECTRICITY_VALUE = TEST_TOKEN_HBD_RAW_DATA + 1;
    public static final int TEST_TOKEN_FINGER_EVENT = TEST_TOKEN_ELECTRICITY_VALUE + 1;
    public static final int TEST_TOKEN_GSC_FLAG = TEST_TOKEN_FINGER_EVENT + 1;
    public static final int TEST_TOKEN_BASE_DATA = TEST_TOKEN_GSC_FLAG + 1;
    public static final int TEST_TOKEN_KR_DATA = TEST_TOKEN_BASE_DATA + 1;
    public static final int TEST_TOKEN_B_DATA = TEST_TOKEN_KR_DATA + 1;
    public static final int TEST_TOKEN_FPC_KEY_DATA = TEST_TOKEN_B_DATA + 1;
    public static final int TEST_TOKEN_FW_EXPOSE_TIME = 736;

    public static final int TEST_TOKEN_MAX_FINGERS = 800;
    public static final int TEST_TOKEN_MAX_FINGERS_PER_USER = TEST_TOKEN_MAX_FINGERS + 1;
    public static final int TEST_TOKEN_SUPPORT_KEY_MODE = TEST_TOKEN_MAX_FINGERS_PER_USER + 1;
    public static final int TEST_TOKEN_SUPPORT_FF_MODE = TEST_TOKEN_SUPPORT_KEY_MODE + 1;
    public static final int TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE = TEST_TOKEN_SUPPORT_FF_MODE + 1;
    public static final int TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL = TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE + 1;
    public static final int TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS = TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL + 1;
    public static final int TEST_TOKEN_SUPPORT_BIO_ASSAY = TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS + 1;
    public static final int TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP = TEST_TOKEN_SUPPORT_BIO_ASSAY + 1;
    public static final int TEST_TOKEN_SUPPORT_NAV_MODE = TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP + 1;
    public static final int TEST_TOKEN_NAV_DOUBLE_CLICK_TIME = TEST_TOKEN_SUPPORT_NAV_MODE + 1;
    public static final int TEST_TOKEN_NAV_LONG_PRESS_TIME = TEST_TOKEN_NAV_DOUBLE_CLICK_TIME + 1;
    public static final int TEST_TOKEN_ENROLLING_MIN_TEMPLATES = TEST_TOKEN_NAV_LONG_PRESS_TIME + 1;
    public static final int TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD = TEST_TOKEN_ENROLLING_MIN_TEMPLATES + 1;
    public static final int TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD = TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD + 1;
    public static final int TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE = TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD + 1;
    public static final int TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO = TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE + 1;
    public static final int TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT = TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO + 1;
    public static final int TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT = TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT + 1;
    public static final int TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD = TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT + 1;
    public static final int TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD = TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD + 1;
    public static final int TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH = TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD + 1;
    public static final int TEST_TOKEN_AUTHENTICATE_ORDER = TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH + 1;
    public static final int TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE = TEST_TOKEN_AUTHENTICATE_ORDER + 1;
    public static final int TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE = TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE + 1;
    public static final int TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK = TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE + 1;
    public static final int TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR = TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK + 1;
    public static final int TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY = TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR + 1;
    public static final int TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER = TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY + 1;
    public static final int TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE = TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER + 1;
    public static final int TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE = TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE + 1;
    public static final int TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE = TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE + 1;
    public static final int TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE = TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE + 1;
    public static final int TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE = TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE + 1;
    public static final int TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE = TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE + 1;
    public static final int TEST_TOKEN_SUPPORT_FRR_ANALYSIS = TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE + 1;

    public static final int TEST_TOKEN_SENSOR_VALIDITY = 900;
    public static final int TEST_TOKEN_SPI_TRANSFER_RESULT = TEST_TOKEN_SENSOR_VALIDITY + 1;
    public static final int TEST_TOKEN_SPI_TRANSFER_REMAININGS = TEST_TOKEN_SPI_TRANSFER_RESULT + 1;
    public static final int TEST_TOKEN_AVERAGE_PIXEL_DIFF = TEST_TOKEN_SPI_TRANSFER_REMAININGS + 1;

    public static final int TEST_TOKEN_SPI_RW_CMD = 1100;
    public static final int TEST_TOKEN_SPI_RW_START_ADDR = TEST_TOKEN_SPI_RW_CMD + 1;
    public static final int TEST_TOKEN_SPI_RW_LENGTH =  TEST_TOKEN_SPI_RW_START_ADDR + 1;
    public static final int TEST_TOKEN_SPI_RW_CONTENT = TEST_TOKEN_SPI_RW_LENGTH + 1;

    public static final int TEST_TOKEN_PACKAGE_VERSION = 1200;
    public static final int TEST_TOKEN_PROTOCOL_VERSION = TEST_TOKEN_PACKAGE_VERSION + 1;
    public static final int TEST_TOKEN_CHIP_SUPPORT_BIO = TEST_TOKEN_PROTOCOL_VERSION + 1;
    public static final int TEST_TOKEN_IS_BIO_ENABLE = TEST_TOKEN_CHIP_SUPPORT_BIO + 1;
    public static final int TEST_TOKEN_AUTHENTICATED_WITH_BIO_SUCCESS_COUNT = TEST_TOKEN_IS_BIO_ENABLE + 1;
    public static final int TEST_TOKEN_AUTHENTICATED_WITH_BIO_FAILED_COUNT = TEST_TOKEN_AUTHENTICATED_WITH_BIO_SUCCESS_COUNT + 1;
    public static final int TEST_TOKEN_AUTHENTICATED_SUCCESS_COUNT = TEST_TOKEN_AUTHENTICATED_WITH_BIO_FAILED_COUNT + 1;
    public static final int TEST_TOKEN_AUTHENTICATED_FAILED_COUNT = TEST_TOKEN_AUTHENTICATED_SUCCESS_COUNT + 1;
    public static final int TEST_TOKEN_BUF_FULL = TEST_TOKEN_AUTHENTICATED_FAILED_COUNT + 1;
    public static final int TEST_TOKEN_UPDATE_POS = TEST_TOKEN_BUF_FULL + 1;
    public static final int TEST_TOKEN_METADATA = TEST_TOKEN_UPDATE_POS + 1;
    public static final int TEST_TOKEN_DISPLAY_TYPE = TEST_TOKEN_METADATA + 1;

    public static final int TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT = 1300;
    public static final int TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT = TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT + 1;
    public static final int TEST_TOKEN_SATURATED_PIXEL_THRESHOLD = TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT + 1;

    public static final int TEST_PARAM_TOKEN_FW_DATA = 5000;
    public static final int TEST_PARAM_TOKEN_CFG_DATA = TEST_PARAM_TOKEN_FW_DATA + 1;

    public static final int TEST_PARAM_TOKEN_DUMP_PATH = 5100;

    public static final int TEST_TOKEN_TEMPLATE_UPDATE_SAVE_THRESHOLD = 5200;
    /************ public test token define end ***************/





    public static long decodeInt64(byte[] result, int offset) {
        long value = 0;
        try {
            value = (result[offset] & 0xff) | ((long)(result[offset + 1] & 0xff) << 8)
                    | ((long)(result[offset + 2] & 0xff) << 16)
                    | ((long)(result[offset + 3] & 0xff) << 24)
                    | ((long)(result[offset + 4] & 0xff) << 32)
                    | ((long)(result[offset + 5] & 0xff) << 40)
                    | ((long)(result[offset + 6] & 0xff) << 48)
                    | ((long)(result[offset + 7] & 0xff) << 56);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + (offset + 7) + " array length =" + result.length);
        }
        return value;
    }

    public static int decodeInt32(byte[] result, int offset) {
        int value = 0;
        try {
            value = (result[offset] & 0xff) | ((result[offset + 1] & 0xff) << 8)
                    | ((result[offset + 2] & 0xff) << 16)
                    | ((result[offset + 3] & 0xff) << 24);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + (offset + 3) + " array length =" + result.length);
        }
        return value;
    }

    public static short decodeInt16(byte[] result, int offset) {
        short value = 0;
        try {
            value = (short) ((result[offset] & 0xff) | ((result[offset + 1] & 0xff) << 8));
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + (offset + 1) + " array length =" + result.length);
        }
        return value;
    }

    public static byte decodeInt8(byte[] result, int offset) {
        byte value = 0;
        try {
            value = result[offset];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + offset + " array length =" + result.length);
        }

        return value;
    }

    public static float decodeFloat(byte[] result, int offset, int size) {
        int value = 0;
        try {
            value = (result[offset] & 0xff) | ((result[offset + 1] & 0xff) << 8)
                    | ((result[offset + 2] & 0xff) << 16)
                    | ((result[offset + 3] & 0xff) << 24);;
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + (offset + 3) + " array length =" + result.length);
        }
        return Float.intBitsToFloat(value);
    }

    public static double decodeDouble(byte[] result, int offset, int size) {
        long value = 0;
        try{
            value = (result[offset] & 0xff)
                    | (((long) result[offset + 1] & 0xff) << 8)
                    | (((long) result[offset + 2] & 0xff) << 16)
                    | (((long) result[offset + 3] & 0xff) << 24)
                    | (((long) result[offset + 4] & 0xff) << 32)
                    | (((long) result[offset + 5] & 0xff) << 40)
                    | (((long) result[offset + 6] & 0xff) << 48)
                    | (((long) result[offset + 7] & 0xff) << 56);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException offset = " + (offset + 7) + " array length =" + result.length);
        }
        return Double.longBitsToDouble(value);
    }

    public static HashMap<Integer, Object> parseConfig(byte[] result) {
        HashMap<Integer, Object> testResult = new HashMap<Integer, Object>();

        int len = 0;
        if (result != null) {
            len = result.length;
        }
        for (int offset = 0; offset < len;) {
            Log.d(TAG, "offset = " + offset);
            int token = decodeInt32(result, offset);
            offset += 4;
            Log.d(TAG, "token = " + token);

            switch (token) {
                case TEST_TOKEN_ERROR_CODE:
                case TEST_TOKEN_CHIP_TYPE:
                case TEST_TOKEN_CHIP_SERIES:
                case TEST_TOKEN_MAX_FINGERS:
                case TEST_TOKEN_MAX_FINGERS_PER_USER:
                case TEST_TOKEN_SUPPORT_KEY_MODE:
                case TEST_TOKEN_SUPPORT_FF_MODE:
                case TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE:
                case TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL:
                case TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS:
                case TEST_TOKEN_SUPPORT_BIO_ASSAY:
                case TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP:
                case TEST_TOKEN_SUPPORT_NAV_MODE:
                case TEST_TOKEN_NAV_DOUBLE_CLICK_TIME:
                case TEST_TOKEN_NAV_LONG_PRESS_TIME:
                case TEST_TOKEN_ENROLLING_MIN_TEMPLATES:
                case TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD:
                case TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD:
                case TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE:
                case TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO:
                case TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT:
                case TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT:
                case TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD:
                case TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD:
                case TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH:
                case TEST_TOKEN_AUTHENTICATE_ORDER:
                case TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE:
                case TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE:
                case TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK:
                case TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR:
                case TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY:
                case TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER:
                case TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE:
                case TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE:
                case TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE:
                case TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE:
                case TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE:
                case TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE:
                case TEST_TOKEN_SUPPORT_FRR_ANALYSIS:
                case TEST_TOKEN_TEMPLATE_UPDATE_SAVE_THRESHOLD:
                case BaikalTestResultParser.TEST_TOKEN_SUPPORT_IMAGE_SEGMENT:
                case BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING:
                case BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING_NUM:

                case ShenzhenTestResultParser.TEST_TOKEN_SHORT_EXPOSURE_TIME:
                case ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_COL:
                case ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_ROW:
                case ShenzhenTestResultParser.TEST_TOKEN_SENSOR_COL:
                case ShenzhenTestResultParser.TEST_TOKEN_SENSOR_ROW:

                {
                    int value = decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }
                default:
                    break;
            }
        }

        return testResult;
    }

    public static HashMap<Integer, Object> parse(int chipSeries, byte[] result) {
        if (chipSeries == Constants.GF_BAIKAL) {
            return BaikalTestResultParser.parse(result);
        } else {
            return ShenzhenTestResultParser.parse(result);
        }
    }
}
