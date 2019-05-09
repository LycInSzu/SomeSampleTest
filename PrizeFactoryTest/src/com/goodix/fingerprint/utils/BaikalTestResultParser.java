/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.utils;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.utils.TestResultParser;
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

public class BaikalTestResultParser {
    private static final String TAG = "BaikalTestResultParser";

    public static final int TEST_TOKEN_CALIBRATION_BRIGHT_LEVEL = 5300;
    public static final int TEST_TOKEN_CALIBRATION_NUM_PER_LEVEL = TEST_TOKEN_CALIBRATION_BRIGHT_LEVEL + 1;
    public static final int TEST_TOKEN_CALIBRATION_CUR_SAMPLE_COUNT= TEST_TOKEN_CALIBRATION_NUM_PER_LEVEL + 1;
    public static final int TEST_TOKEN_CALIBRATION_ALGO_FINISHED_FLAG = TEST_TOKEN_CALIBRATION_CUR_SAMPLE_COUNT + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_FLAG = TEST_TOKEN_CALIBRATION_ALGO_FINISHED_FLAG + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_SEND_DOWN_UP = TEST_TOKEN_CALIBRATION_AUTO_FLAG + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_TEST_TYPE = TEST_TOKEN_CALIBRATION_AUTO_SEND_DOWN_UP + 1;

    public static final int TEST_TOKEN_GREAT_CIRCLE_X = 5400;
    public static final int TEST_TOKEN_GREAT_CIRCLE_Y = TEST_TOKEN_GREAT_CIRCLE_X + 1;
    public static final int TEST_TOKEN_CENTRAL_CIRCLE_X= TEST_TOKEN_GREAT_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_CENTRAL_CIRCLE_Y = TEST_TOKEN_CENTRAL_CIRCLE_X + 1;
    public static final int TEST_TOKEN_SMALL_CIRCLE_X = TEST_TOKEN_CENTRAL_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_SMALL_CIRCLE_Y = TEST_TOKEN_SMALL_CIRCLE_X + 1;
    public static final int TEST_TOKEN_CENTER_POINT_X = TEST_TOKEN_SMALL_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_CENTER_POINT_Y = TEST_TOKEN_CENTER_POINT_X + 1;
    public static final int TEST_TOKEN_CIRCLE_ANGLE = TEST_TOKEN_CENTER_POINT_Y + 1;
    public static final int TEST_TOKEN_CIRCLE_IMAGE_TIME_STAMP = TEST_TOKEN_CIRCLE_ANGLE + 1;

    public static final int TEST_TOKEN_BASE_BAD_POINT_NUM = TEST_TOKEN_CIRCLE_IMAGE_TIME_STAMP + 1;
    public static final int TEST_TOKEN_BASE_DARK_NOISE_T = TEST_TOKEN_BASE_BAD_POINT_NUM + 1;
    public static final int TEST_TOKEN_BASE_LIGHT_NOISE_T = TEST_TOKEN_BASE_DARK_NOISE_T + 1;
    public static final int TEST_TOKEN_BASE_DARK_NOISE_S = TEST_TOKEN_BASE_LIGHT_NOISE_T + 1;
    public static final int TEST_TOKEN_BASE_LIGHT_NOISE_S = TEST_TOKEN_BASE_DARK_NOISE_S + 1;
    public static final int TEST_TOKEN_BASE_SSNR = TEST_TOKEN_BASE_LIGHT_NOISE_S + 1;
    public static final int TEST_TOKEN_BASE_P2P = TEST_TOKEN_BASE_SSNR + 1;
    public static final int TEST_TOKEN_BASE_SHARPNESS = TEST_TOKEN_BASE_P2P + 1;

    public static final int TEST_TOKEN_POSITION_OFFSET_ANGLE = 5500;
    public static final int TEST_TOKEN_POSITION_OFFSET_X = TEST_TOKEN_POSITION_OFFSET_ANGLE + 1;
    public static final int TEST_TOKEN_POSITION_OFFSET_Y = TEST_TOKEN_POSITION_OFFSET_X + 1;
    public static final int TEST_TOKEN_BAD_POINT_NUM = TEST_TOKEN_POSITION_OFFSET_Y + 1;
    public static final int TEST_TOKEN_DARK_NOISE_T = TEST_TOKEN_BAD_POINT_NUM + 1;
    public static final int TEST_TOKEN_LIGHT_NOISE_T = TEST_TOKEN_DARK_NOISE_T + 1;
    public static final int TEST_TOKEN_DARK_NOISE_S = TEST_TOKEN_LIGHT_NOISE_T + 1;
    public static final int TEST_TOKEN_LIGHT_NOISE_S = TEST_TOKEN_DARK_NOISE_S + 1;
    public static final int TEST_TOKEN_SSNR = TEST_TOKEN_LIGHT_NOISE_S + 1;
    public static final int TEST_TOKEN_P2P = TEST_TOKEN_SSNR + 1;
    public static final int TEST_TOKEN_SHARPNESS = TEST_TOKEN_P2P + 1;

    public static final int TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_STEP = 5600;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_X = TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_STEP + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_Y = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_X + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_X = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_Y + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_Y = TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_X + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_WIDTH = TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_Y + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_HEIGHT = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_WIDTH + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_RECT_SCREEN_WIDTH = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_HEIGHT + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_WIDTH = TEST_TOKEN_AUTO_FIND_SENSOR_RECT_SCREEN_WIDTH + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_HEIGHT = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_WIDTH + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_ROTATE_RANDIAN = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_HEIGHT + 1;
    public static final int TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_WIDTH = TEST_TOKEN_AUTO_FIND_SENSOR_ROTATE_RANDIAN + 1;
    public static final int TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_HEIGHT = TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_WIDTH + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTX = TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_HEIGHT + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTY = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTX + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_INDEX_VERTICAL = TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTY + 1;
    public static final int TEST_TOKEN_AUTO_FIND_SENSOR_INDEX_HORIZONTAL = TEST_TOKEN_AUTO_FIND_SENSOR_INDEX_VERTICAL + 1;
    public static final int TEST_TOKEN_MCU_CHIP_ID = TEST_TOKEN_AUTO_FIND_SENSOR_INDEX_HORIZONTAL + 1;
    public static final int TEST_TOKEN_FINGER_CHIP_ID = TEST_TOKEN_MCU_CHIP_ID + 1;
    public static final int TEST_TOKEN_FLASH_CHIP_ID = TEST_TOKEN_FINGER_CHIP_ID + 1;
    public static final int TEST_TOKEN_RANDOM_NUM = TEST_TOKEN_FLASH_CHIP_ID + 1;
    public static final int TEST_TOKEN_PIXEL_OS_NUM = TEST_TOKEN_RANDOM_NUM + 1;
    public static final int TEST_TOKEN_DARK_NOISET = TEST_TOKEN_PIXEL_OS_NUM + 1;
    public static final int TEST_TOKEN_LIGHT_NOISET = TEST_TOKEN_DARK_NOISET + 1;
    public static final int TEST_TOKEN_DARK_NOISES = TEST_TOKEN_LIGHT_NOISET + 1;
    public static final int TEST_TOKEN_LIGHT_NOISES = TEST_TOKEN_DARK_NOISES + 1;
    public static final int TEST_TOKEN_NOISE_OPERATION_STEP = TEST_TOKEN_LIGHT_NOISES + 1;
    public static final int TEST_TOKEN_RESET_COUNTS = TEST_TOKEN_NOISE_OPERATION_STEP + 1;
    public static final int TEST_TOKEN_SOFTWARE_RESET_COUNTS = TEST_TOKEN_RESET_COUNTS + 1;
    public static final int TEST_TOKEN_HARDWARE_RESET_COUNTS = TEST_TOKEN_SOFTWARE_RESET_COUNTS + 1;
    public static final int TEST_TOKEN_OTHER_RESET_COUNTS = TEST_TOKEN_HARDWARE_RESET_COUNTS + 1;

    public static final int TEST_TOKEN_PRESSURE_THRESHOLD = 5700;

    public static final int TEST_TOKEN_SUPPORT_IMAGE_SEGMENT = 5800;

    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_COLLECT_PHASE = 5900;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_TEST_PHASE = TEST_PARAM_TOKEN_PERFORMANCE_TEST_COLLECT_PHASE + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_TEST_ITEMS = TEST_PARAM_TOKEN_PERFORMANCE_TEST_TEST_PHASE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_COLLECT_VAILD_NUM = TEST_PARAM_TOKEN_PERFORMANCE_TEST_TEST_ITEMS + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISET = TEST_TOKEN_PERFORMANCE_TEST_COLLECT_VAILD_NUM + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISET = TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISET + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISES = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISET + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISES = TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISES + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_SSNR = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISES + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_SIGNAL = TEST_TOKEN_PERFORMANCE_TEST_RESULT_SSNR + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_NOISE = TEST_TOKEN_PERFORMANCE_TEST_RESULT_SIGNAL + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_SHARPNESS = TEST_TOKEN_PERFORMANCE_TEST_RESULT_NOISE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SSNR = TEST_TOKEN_PERFORMANCE_TEST_RESULT_SHARPNESS + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SIGNAL = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SSNR + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_NOISE = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SIGNAL + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SHARPNESS = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_NOISE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_BAD_POINT_NUM = TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SHARPNESS + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_CLUSTER_NUM = TEST_TOKEN_PERFORMANCE_TEST_RESULT_BAD_POINT_NUM + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_HOT_PIXEL_NUM = TEST_TOKEN_PERFORMANCE_TEST_RESULT_CLUSTER_NUM + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_NUM = TEST_TOKEN_PERFORMANCE_TEST_RESULT_HOT_PIXEL_NUM + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_X = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_NUM + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_Y = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_X + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ANGLE = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_Y + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_X = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ANGLE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_Y = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_X + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_ANGLE = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_Y + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_WIDTH = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_ANGLE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_HEIGHT = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_WIDTH + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_X = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_HEIGHT + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_Y = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_X + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_Y + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_WIDTH = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_HEIGHT = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_WIDTH + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_DATA = TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_HEIGHT + 1;
    public static final int TEST_PARAM_TOKEN_PERFORMANCE_TEST_CMD_ID = TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_DATA + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_EDGEPIXELS = TEST_PARAM_TOKEN_PERFORMANCE_TEST_CMD_ID + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_MAXLOCALBADPIXEL = TEST_TOKEN_PERFORMANCE_TEST_RESULT_EDGEPIXELS + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_MAXTOTALBADPIXEL = TEST_TOKEN_PERFORMANCE_TEST_RESULT_MAXLOCALBADPIXEL + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_DATANOISE = TEST_TOKEN_PERFORMANCE_TEST_RESULT_MAXTOTALBADPIXEL + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_DATANOISEMT = TEST_TOKEN_PERFORMANCE_TEST_RESULT_DATANOISE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_TEST_RESULT_DIRECTION = TEST_TOKEN_PERFORMANCE_TEST_RESULT_DATANOISEMT + 1;

    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_CALIBRATION_STEP = 6000;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_GREATE_CIRCLE_X = TEST_PARAM_TOKEN_LOCATION_CIRCLE_CALIBRATION_STEP + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_GREATE_CIRCLE_Y = TEST_TOKEN_LOCATION_CALIBRATION_GREATE_CIRCLE_X + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_MIDDLE_CIRCLE_X = TEST_TOKEN_LOCATION_CALIBRATION_GREATE_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_MIDDLE_CIRCLE_Y = TEST_TOKEN_LOCATION_CALIBRATION_MIDDLE_CIRCLE_X + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_SMALL_CIRCLE_X = TEST_TOKEN_LOCATION_CALIBRATION_MIDDLE_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_SMALL_CIRCLE_Y = TEST_TOKEN_LOCATION_CALIBRATION_SMALL_CIRCLE_X + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_CENTER_X = TEST_TOKEN_LOCATION_CALIBRATION_SMALL_CIRCLE_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_CENTER_Y = TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_CENTER_X + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_ANGLE = TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_CENTER_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_CALCULATE_CIRCLE_SUCCESS = TEST_TOKEN_LOCATION_CALIBRATION_CIRCLE_ANGLE + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_ANGLE = TEST_TOKEN_LOCATION_CALIBRATION_CALCULATE_CIRCLE_SUCCESS + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_X = TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_ANGLE + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_Y = TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_X + 1;
    public static final int TEST_TOKEN_LOCATION_CALIBRATION_CALCULATE_DEVIATION_SUCCESS = TEST_TOKEN_LOCATION_CALIBRATION_DEVIATION_Y + 1;

    public static final int TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING = 6200;
    public static final int TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING_NUM = TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING + 1;

    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_TEST_COLLECT_PHASE = 6300;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_X = TEST_PARAM_TOKEN_LOCATION_CIRCLE_TEST_COLLECT_PHASE + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_Y = TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_X + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH = TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_Y + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT = TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET = TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_WIDTH = TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET + 1;
    public static final int TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_HEIGHT = TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_WIDTH + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_IMAGE_DATA = TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_HEIGHT + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_US_NUM = TEST_TOKEN_LOCATION_CIRCLE_IMAGE_DATA + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_X = TEST_TOKEN_LOCATION_CIRCLE_US_NUM + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_Y = TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_X + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_ANGLE = TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_X = TEST_TOKEN_LOCATION_CIRCLE_ANGLE + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_Y = TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_X + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_ANGLE = TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_Y + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_WIDTH = TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_ANGLE + 1;
    public static final int TEST_TOKEN_LOCATION_CIRCLE_HEIGHT = TEST_TOKEN_LOCATION_CIRCLE_WIDTH + 1;

    public static HashMap<Integer, Object> parse(byte[] result) {
        HashMap<Integer, Object> testResult = new HashMap<Integer, Object>();

        int len = 0;
        if (result != null) {
            len = result.length;
        }
        for (int offset = 0; offset < len;) {
            Log.d(TAG, "offset = " + offset);
            int token = TestResultParser.decodeInt32(result, offset);
            offset += 4;
            Log.d(TAG, "token = " + token);

            switch (token) {
                case TestResultParser.TEST_TOKEN_ERROR_CODE:
                case TestResultParser.TEST_TOKEN_BAD_PIXEL_NUM:
                case TestResultParser.TEST_TOKEN_FDT_BAD_AREA_NUM:
                case TestResultParser.TEST_TOKEN_LOCAL_BAD_PIXEL_NUM:
                case TestResultParser.TEST_TOKEN_GET_DR_TIMESTAMP_TIME:
                case TestResultParser.TEST_TOKEN_GET_MODE_TIME:
                case TestResultParser.TEST_TOKEN_GET_CHIP_ID_TIME:
                case TestResultParser.TEST_TOKEN_GET_VENDOR_ID_TIME:
                case TestResultParser.TEST_TOKEN_GET_SENSOR_ID_TIME:
                case TestResultParser.TEST_TOKEN_GET_FW_VERSION_TIME:
                case TestResultParser.TEST_TOKEN_GET_IMAGE_TIME:
                case TestResultParser.TEST_TOKEN_RAW_DATA_LEN:
                case TestResultParser.TEST_TOKEN_IMAGE_QUALITY:
                case TestResultParser.TEST_TOKEN_VALID_AREA:
                case TestResultParser.TEST_TOKEN_GSC_FLAG:
                case TestResultParser.TEST_TOKEN_KEY_POINT_NUM:
                case TestResultParser.TEST_TOKEN_INCREATE_RATE:
                case TestResultParser.TEST_TOKEN_OVERLAY:
                case TestResultParser.TEST_TOKEN_GET_RAW_DATA_TIME:
                case TestResultParser.TEST_TOKEN_PREPROCESS_TIME:
                case TestResultParser.TEST_TOKEN_ALGO_START_TIME:
                case TestResultParser.TEST_TOKEN_GET_FEATURE_TIME:
                case TestResultParser.TEST_TOKEN_ENROLL_TIME:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_TIME:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_ID:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_UPDATE_FLAG:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_FINGER_COUNT:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_FINGER_ITME:
                case TestResultParser.TEST_TOKEN_TOTAL_TIME:
                case TestResultParser.TEST_TOKEN_RESET_FLAG:
                case TestResultParser.TEST_TOKEN_SINGULAR:
                case TestResultParser.TEST_TOKEN_CHIP_TYPE:
                case TestResultParser.TEST_TOKEN_CHIP_SERIES:
                case TestResultParser.TEST_TOKEN_MAX_FINGERS:
                case TestResultParser.TEST_TOKEN_MAX_FINGERS_PER_USER:
                case TestResultParser.TEST_TOKEN_SUPPORT_KEY_MODE:
                case TestResultParser.TEST_TOKEN_SUPPORT_FF_MODE:
                case TestResultParser.TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE:
                case TestResultParser.TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL:
                case TestResultParser.TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS:
                case TestResultParser.TEST_TOKEN_SUPPORT_BIO_ASSAY:
                case TestResultParser.TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP:
                case TestResultParser.TEST_TOKEN_SUPPORT_NAV_MODE:
                case TestResultParser.TEST_TOKEN_NAV_DOUBLE_CLICK_TIME:
                case TestResultParser.TEST_TOKEN_NAV_LONG_PRESS_TIME:
                case TestResultParser.TEST_TOKEN_ENROLLING_MIN_TEMPLATES:
                case TestResultParser.TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD:
                case TestResultParser.TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD:
                case TestResultParser.TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE:
                case TestResultParser.TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO:
                case TestResultParser.TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT:
                case TestResultParser.TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT:
                case TestResultParser.TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD:
                case TestResultParser.TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD:
                case TestResultParser.TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH:
                case TestResultParser.TEST_TOKEN_AUTHENTICATE_ORDER:
                case TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE:
                case TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE:
                case TestResultParser.TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK:
                case TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR:
                case TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY:
                case TestResultParser.TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER:
                case TestResultParser.TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE:
                case TestResultParser.TEST_TOKEN_SUPPORT_FRR_ANALYSIS:
                case TestResultParser.TEST_TOKEN_GET_GSC_DATA_TIME:
                case TestResultParser.TEST_TOKEN_BIO_ASSAY_TIME:
                case TestResultParser.TEST_TOKEN_SENSOR_VALIDITY:
                case TestResultParser.TEST_TOKEN_ALGO_INDEX:
                case TestResultParser.TEST_TOKEN_SAFE_CLASS:
                case TestResultParser.TEST_TOKEN_TEMPLATE_COUNT:
                case TestResultParser.TEST_TOKEN_ELECTRICITY_VALUE:
                case TestResultParser.TEST_TOKEN_FINGER_EVENT:
                case TestResultParser.TEST_TOKEN_LOCAL_SMALL_BAD_PIXEL_NUM:
                case TestResultParser.TEST_TOKEN_LOCAL_BIG_BAD_PIXEL_NUM:
                case TestResultParser.TEST_TOKEN_FLATNESS_BAD_PIXEL_NUM:
                case TestResultParser.TEST_TOKEN_AVERAGE_PIXEL_DIFF:
                case TestResultParser.TEST_TOKEN_IS_BAD_LINE:
                case TestResultParser.TEST_TOKEN_SPI_RW_CMD:
                case TestResultParser.TEST_TOKEN_SPI_RW_START_ADDR:
                case TestResultParser.TEST_TOKEN_SPI_RW_LENGTH:
                case TestResultParser.TEST_TOKEN_FW_DATA_LEN:
                case TestResultParser.TEST_TOKEN_CFG_DATA_LEN:
                case TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE:
                case TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE:
                case TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE:
                case TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE:
                case TestResultParser.TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE:
                case TestResultParser.TEST_TOKEN_FRAME_NUM:
                case TestResultParser.TEST_TOKEN_MAX_FRAME_NUM:
                case TestResultParser.TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT:
                case TestResultParser.TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT:
                case TestResultParser.TEST_TOKEN_SATURATED_PIXEL_THRESHOLD:
                case TestResultParser.TEST_TOKEN_TEMPLATE_UPDATE_SAVE_THRESHOLD:
                case TEST_TOKEN_CALIBRATION_CUR_SAMPLE_COUNT:
                case TEST_TOKEN_CALIBRATION_ALGO_FINISHED_FLAG:
                case TEST_TOKEN_CALIBRATION_AUTO_TEST_TYPE:
                case TestResultParser.TEST_TOKEN_BMP_DATA_WIDTH:
                case TestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT:
                case TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_STEP:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_X:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_Y:
                case TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_X:
                case TEST_TOKEN_AUTO_FIND_SENSOR_LEFT_TOP_Y:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_WIDTH:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_IMAGE_HEIGHT:
                case TEST_TOKEN_AUTO_FIND_SENSOR_RECT_SCREEN_WIDTH:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_WIDTH:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_SCREEN_HEIGHT:
                case TEST_TOKEN_AUTO_FIND_SENSOR_ROTATE_RANDIAN:
                case TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_WIDTH:
                case TEST_PARAM_TOKEN_AUTO_FIND_SENSOR_SCREEN_HEIGHT:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTX:
                case TEST_TOKEN_AUTO_FIND_SENSOR_SENSOR_GUIDE_LTY:
                case TEST_TOKEN_PRESSURE_THRESHOLD:
                case TEST_TOKEN_SUPPORT_IMAGE_SEGMENT:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_COLLECT_PHASE:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISET:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISET:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_DARK_NOISES:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LIGHT_NOISES:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_SSNR:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_SIGNAL:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_NOISE:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_SHARPNESS:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SSNR:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SIGNAL:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_NOISE:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_LFP_SHARPNESS:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_BAD_POINT_NUM:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_CLUSTER_NUM:
                case TEST_TOKEN_PERFORMANCE_TEST_RESULT_HOT_PIXEL_NUM:
                case TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING:
                case TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING_NUM:
                case TEST_TOKEN_MCU_CHIP_ID:
                case TEST_TOKEN_FLASH_CHIP_ID:
                case TEST_TOKEN_FINGER_CHIP_ID:
                case TEST_TOKEN_RANDOM_NUM:
                case TEST_TOKEN_NOISE_OPERATION_STEP:
                case TEST_TOKEN_PIXEL_OS_NUM:
                case TEST_TOKEN_RESET_COUNTS:
                case TEST_TOKEN_SOFTWARE_RESET_COUNTS:
                case TEST_TOKEN_HARDWARE_RESET_COUNTS:
                case TEST_TOKEN_OTHER_RESET_COUNTS:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_TEST_COLLECT_PHASE:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_X:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_Y:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_WIDTH:
                case TEST_PARAM_TOKEN_LOCATION_CIRCLE_SCREEN_HEIGHT:
                case TEST_TOKEN_LOCATION_CIRCLE_US_NUM:
                case TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_X:
                case TEST_TOKEN_LOCATION_CIRCLE_US_CENTER_Y:
                case TEST_TOKEN_LOCATION_CIRCLE_ANGLE:
                case TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_X:
                case TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_Y:
                case TEST_TOKEN_LOCATION_CIRCLE_ABS_CENTER_ANGLE:
                case TEST_TOKEN_LOCATION_CIRCLE_WIDTH:
                case TEST_TOKEN_LOCATION_CIRCLE_HEIGHT:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_NUM:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_X:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_US_CENTER_Y:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ANGLE:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_X:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_Y:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_ABS_CENTER_ANGLE:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_WIDTH:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_HEIGHT:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_X:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_Y:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_WIDTH:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_HEIGHT:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_SENSOR_OFFSET:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_WIDTH:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_SCREEN_HEIGHT:
                case TEST_PARAM_TOKEN_PERFORMANCE_TEST_CMD_ID: {
                    int value = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_ALGO_VERSION:
                case TestResultParser.TEST_TOKEN_PREPROCESS_VERSION:
                case TestResultParser.TEST_TOKEN_PRODUCTION_ALGO_VERSION:
                case TestResultParser.TEST_TOKEN_FW_VERSION:
                case TestResultParser.TEST_TOKEN_TEE_VERSION:
                case TestResultParser.TEST_TOKEN_TA_VERSION:
                case TestResultParser.TEST_TOKEN_CODE_FW_VERSION: {
                    int size = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, new String(result, offset, size));
                    offset += size;
                    break;
                }

                case TestResultParser.TEST_TOKEN_CHIP_ID:
                case TestResultParser.TEST_TOKEN_VENDOR_ID:
                case TestResultParser.TEST_TOKEN_SENSOR_ID:
                case TestResultParser.TEST_TOKEN_PRODUCTION_DATE:
                case TestResultParser.TEST_TOKEN_RAW_DATA:
                case TestResultParser.TEST_TOKEN_BMP_DATA:
                case TestResultParser.TEST_TOKEN_BASE_DATA:
                case TestResultParser.TEST_TOKEN_KR_DATA:
                case TestResultParser.TEST_TOKEN_B_DATA:
                case TestResultParser.TEST_TOKEN_HBD_RAW_DATA:
                case TestResultParser.TEST_TOKEN_FPC_KEY_DATA:
                case TestResultParser.TEST_TOKEN_SPI_RW_CONTENT:
                case TestResultParser.TEST_TOKEN_CFG_DATA:
                case TestResultParser.TEST_TOKEN_FW_DATA:
                case TestResultParser.TEST_TOKEN_GSC_DATA:
                case TestResultParser.TEST_TOKEN_DATA_DEVIATION_DIFF:
                case TEST_TOKEN_LOCATION_CIRCLE_IMAGE_DATA:
                case TEST_TOKEN_PERFORMANCE_TEST_LOCATION_CIRCLE_IMAGE_DATA:{
                    int size = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    if (size > 0) {
                        testResult.put(token, Arrays.copyOfRange(result, offset, offset + size));
                        offset += size;
                    }
                    break;
                }

                case TestResultParser.TEST_TOKEN_ALL_TILT_ANGLE:
                case TestResultParser.TEST_TOKEN_BLOCK_TILT_ANGLE_MAX: {
                    int size = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    float value = TestResultParser.decodeFloat(result, offset, size);
                    testResult.put(token, value);
                    offset += size;
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_NOISE: {
                    int size = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    double value = TestResultParser.decodeDouble(result, offset, size);
                    testResult.put(token, value);
                    offset += size;
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_AVG_DIFF_VAL:
                case TestResultParser.TEST_TOKEN_LOCAL_WORST:
                case TestResultParser.TEST_TOKEN_IN_CIRCLE:
                case TestResultParser.TEST_TOKEN_BIG_BUBBLE:
                case TestResultParser.TEST_TOKEN_LINE:
                case TestResultParser.TEST_TOKEN_HBD_BASE:
                case TestResultParser.TEST_TOKEN_HBD_AVG: {
                    short value = TestResultParser.decodeInt16(result, offset);
                    offset += 2;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_SENSOR_OTP_TYPE: {
                    byte value = TestResultParser.decodeInt8(result, offset);
                    offset += 1;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_PACKAGE_VERSION:
                case TestResultParser.TEST_TOKEN_PROTOCOL_VERSION:
                case TestResultParser.TEST_TOKEN_CHIP_SUPPORT_BIO:
                case TestResultParser.TEST_TOKEN_IS_BIO_ENABLE:
                case TestResultParser.TEST_TOKEN_AUTHENTICATED_WITH_BIO_SUCCESS_COUNT:
                case TestResultParser.TEST_TOKEN_AUTHENTICATED_WITH_BIO_FAILED_COUNT:
                case TestResultParser.TEST_TOKEN_AUTHENTICATED_SUCCESS_COUNT:
                case TestResultParser.TEST_TOKEN_AUTHENTICATED_FAILED_COUNT:
                case TestResultParser.TEST_TOKEN_BUF_FULL:
                case TestResultParser.TEST_TOKEN_UPDATE_POS: {
                    int value = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }

                case TestResultParser.TEST_TOKEN_METADATA: {
                    int size = TestResultParser.decodeInt32(result, offset);
                    offset += 4;
                    String value = new String(result, offset, size);
                    testResult.put(token, value);
                    offset += size;
                    break;
                }

                default:
                    break;
            }
        }

        return testResult;
    }
}