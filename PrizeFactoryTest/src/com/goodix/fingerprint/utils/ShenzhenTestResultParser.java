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

import  com.goodix.fingerprint.FrrFarConstants.*;


public class ShenzhenTestResultParser extends TestResultParser {
    private static final String TAG = "ShenzhenTestResultParser";

    // add for shenzhen
    public static final int TEST_TOKEN_CALIBRATION_BRIGHT_LEVEL = 5300;
    public static final int TEST_TOKEN_CALIBRATION_NUM_PER_LEVEL    = TEST_TOKEN_CALIBRATION_BRIGHT_LEVEL + 1;
    public static final int TEST_TOKEN_CALIBRATION_CUR_SAMPLE_COUNT = TEST_TOKEN_CALIBRATION_NUM_PER_LEVEL + 1;
    public static final int TEST_TOKEN_CALIBRATION_ALGO_FINISHED_FLAG = TEST_TOKEN_CALIBRATION_CUR_SAMPLE_COUNT + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_FLAG = TEST_TOKEN_CALIBRATION_ALGO_FINISHED_FLAG + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_SEND_DOWN_UP = TEST_TOKEN_CALIBRATION_AUTO_FLAG + 1;
    public static final int TEST_TOKEN_CALIBRATION_AUTO_TEST_TYPE   = TEST_TOKEN_CALIBRATION_AUTO_SEND_DOWN_UP + 1;
    public static final int TEST_TOKEN_CALIBRATION_STEP = TEST_TOKEN_CALIBRATION_AUTO_TEST_TYPE + 1;
    public static final int TEST_TOKEN_EXPO_AUTO_CALIBRATION_FLAG = TEST_TOKEN_CALIBRATION_STEP + 1;
    public static final int TEST_TOKEN_MCU_ID = TEST_TOKEN_EXPO_AUTO_CALIBRATION_FLAG + 1;
    public static final int TEST_TOKEN_FLASH_ID = TEST_TOKEN_MCU_ID + 1;
    public static final int TEST_TOKEN_PMIC_ID = TEST_TOKEN_FLASH_ID + 1;

    public static final int TEST_TOKEN_PERFORMANCE_P2P = TEST_TOKEN_PMIC_ID + 1;
    public static final int TEST_TOKEN_PERFORMANCE_NOISE = TEST_TOKEN_PERFORMANCE_P2P + 1;
    public static final int TEST_TOKEN_PERFORMANCE_SSNR = TEST_TOKEN_PERFORMANCE_NOISE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_MEAN_RIDGE = TEST_TOKEN_PERFORMANCE_SSNR + 1;
    public static final int TEST_TOKEN_PERFORMANCE_MEAN_VALLEY = TEST_TOKEN_PERFORMANCE_MEAN_RIDGE + 1;
    public static final int TEST_TOKEN_PERFORMANCE_SHARPNESS = TEST_TOKEN_PERFORMANCE_MEAN_VALLEY + 1;
    public static final int TEST_TOKEN_PERFORMANCE_SHARPNESS_ALL = TEST_TOKEN_PERFORMANCE_SHARPNESS + 1;
    public static final int TEST_TOKEN_PERFORMANCE_CHART_TOUCH_DIFF = TEST_TOKEN_PERFORMANCE_SHARPNESS_ALL + 1;
    public static final int TEST_TOKEN_PERFORMANCE_CHART_CONTRAST = TEST_TOKEN_PERFORMANCE_CHART_TOUCH_DIFF + 1;
    public static final int TEST_TOKEN_FACTORY_SNOISE = TEST_TOKEN_PERFORMANCE_CHART_CONTRAST + 1;
    public static final int TEST_TOKEN_FACTORY_TNOISE = TEST_TOKEN_FACTORY_SNOISE + 1;
    public static final int TEST_TOKEN_FACTORY_FLESH_TOUCH_DIFF = TEST_TOKEN_FACTORY_TNOISE + 1;
    public static final int TEST_TOKEN_FACTORY_LIGHT_LEAK_RATIO = TEST_TOKEN_FACTORY_FLESH_TOUCH_DIFF + 1;
    public static final int TEST_TOKEN_FACTORY_FOV_LEFT = TEST_TOKEN_FACTORY_LIGHT_LEAK_RATIO + 1;
    public static final int TEST_TOKEN_FACTORY_FOV_RIGHT = TEST_TOKEN_FACTORY_FOV_LEFT + 1;
    public static final int TEST_TOKEN_FACTORY_FOV_UP = TEST_TOKEN_FACTORY_FOV_RIGHT + 1;
    public static final int TEST_TOKEN_FACTORY_FOV_DOWN = TEST_TOKEN_FACTORY_FOV_UP + 1;
    public static final int TEST_TOKEN_FACTORY_RELATIVE_ILLUMINANCE = TEST_TOKEN_FACTORY_FOV_DOWN + 1;
    public static final int TEST_TOKEN_FACTORY_ILLUM_MAX_X = TEST_TOKEN_FACTORY_RELATIVE_ILLUMINANCE + 1;
    public static final int TEST_TOKEN_FACTORY_ILLUM_MAX_Y = TEST_TOKEN_FACTORY_ILLUM_MAX_X + 1;
    public static final int TEST_TOKEN_FACTORY_SCALE = TEST_TOKEN_FACTORY_ILLUM_MAX_Y + 1;
    public static final int TEST_TOKEN_FACTORY_RMS = TEST_TOKEN_FACTORY_SCALE + 1;
    public static final int TEST_TOKEN_FACTORY_CROP_WIDTH = TEST_TOKEN_FACTORY_RMS + 1;
    public static final int TEST_TOKEN_FACTORY_CROP_HEIGHT = TEST_TOKEN_FACTORY_CROP_WIDTH + 1;
    public static final int TEST_TOKEN_FACTORY_BAD_POINT_NUM = TEST_TOKEN_FACTORY_CROP_HEIGHT + 1;
    public static final int TEST_TOKEN_FACTORY_CLUSTER_NUM = TEST_TOKEN_FACTORY_BAD_POINT_NUM + 1;
    public static final int TEST_TOKEN_FACTORY_PIXEL_OF_LARGEST_BAD_CLUSTER = TEST_TOKEN_FACTORY_CLUSTER_NUM + 1;
    public static final int TEST_TOKEN_FACTROY_SCREEN_STRUCT_RATIO = TEST_TOKEN_FACTORY_PIXEL_OF_LARGEST_BAD_CLUSTER + 1;

    public static final int TEST_TOKEN_LONG_EXPOSURE_DATA = 8000;
    public static final int TEST_TOKEN_LONG_EXPOSURE_WIDTH = 8001;
    public static final int TEST_TOKEN_LONG_EXPOSURE_HEIGHT = 8002;
    public static final int TEST_TOKEN_SHORT_EXPOSURE_DATA = 8003;
    public static final int TEST_TOKEN_SHORT_EXPOSURE_WIDTH = 8004;
    public static final int TEST_TOKEN_SHORT_EXPOSURE_HEIGHT = 8005;
    public static final int TEST_TOKEN_FUSION_DATA = 8006;
    public static final int TEST_TOKEN_FUSION_WIDTH = 8007;
    public static final int TEST_TOKEN_FUSION_HEIGHT = 8008;
    public static final int TEST_TOKEN_WEAK_DATA_WIDTH = 8009;
    public static final int TEST_TOKEN_WEAK_DATA_HEIGHT = 8010;
    public static final int TEST_TOKEN_CFG_UPDATE_MODE = 8011;
    public static final int TEST_TOKEN_FINGERPRINT_ENUMERATING_FID = 8012;
    public static final int TEST_TOKEN_CURRENT_EXPOSUE_TIME = 8013;
    public static final int TEST_TOKEN_CURRENT_EXPOSUE_RAWDATA_MAX = 8014;

    public static final int TEST_TOKEN_SCALE_RATIO_VALUE = 6000;
    public static final int TEST_TOKEN_LONG_FRAME_AVG_NUM = TEST_TOKEN_SCALE_RATIO_VALUE + 1;
    public static final int TEST_TOKEN_LONG_PAG_GAIN = TEST_TOKEN_LONG_FRAME_AVG_NUM + 1;

    public static final int TEST_TOKEN_SHORT_EXPOSURE_TIME = TEST_TOKEN_LONG_PAG_GAIN + 1;
    public static final int TEST_TOKEN_SHORT_FRAME_AVG_NUM = TEST_TOKEN_SHORT_EXPOSURE_TIME + 1;
    public static final int TEST_TOKEN_SHORT_PAG_GAIN = TEST_TOKEN_SHORT_FRAME_AVG_NUM + 1;

    public static final int TEST_TOKEN_UNLOWERTHRESH = TEST_TOKEN_SHORT_PAG_GAIN + 1;
    public static final int TEST_TOKEN_UNHIGHTHRESH = TEST_TOKEN_UNLOWERTHRESH + 1;

    public static final int TEST_TOKEN_FUSION_RATIO = TEST_TOKEN_UNHIGHTHRESH + 1;

    public static final int TEST_TOKEN_PREPROCESS = TEST_TOKEN_FUSION_RATIO + 1;

    public static final int TEST_TOKEN_RECT_X = TEST_TOKEN_PREPROCESS + 1;
    public static final int TEST_TOKEN_RECT_Y = TEST_TOKEN_RECT_X + 1;
    public static final int TEST_TOKEN_RECT_WIDTH = TEST_TOKEN_RECT_Y + 1;
    public static final int TEST_TOKEN_RECT_HEIGHT = TEST_TOKEN_RECT_WIDTH + 1;

    public static final int TEST_TOKEN_BASE_CALIBRATE_SWITCH = TEST_TOKEN_RECT_HEIGHT + 1;
    public static final int TEST_TOKEN_LDC_SWITCH = TEST_TOKEN_BASE_CALIBRATE_SWITCH + 1;

    public static final int TEST_TOKEN_TNR_RESET_THRESH = TEST_TOKEN_LDC_SWITCH + 1;  // 6016
    public static final int TEST_TOKEN_TNR_RESET_FLAG = TEST_TOKEN_TNR_RESET_THRESH + 1;  // 6017
    public static final int TEST_TOKEN_LPF_SWITCH = TEST_TOKEN_TNR_RESET_FLAG + 1;  // 6018
    public static final int TEST_TOKEN_LSC_SWITCH = TEST_TOKEN_LPF_SWITCH + 1;  // 6019
    public static final int TEST_TOKEN_ENHANCE_LEVEL = TEST_TOKEN_LSC_SWITCH + 1;  // 6020

    public static final int TEST_TOKEN_LONG_EXPOSURE_TIME_DISTANCE = TEST_TOKEN_ENHANCE_LEVEL + 1;

    public static final int TEST_TOKEN_RECT_BMP_COL = TEST_TOKEN_LONG_EXPOSURE_TIME_DISTANCE + 1;  // 6022
    public static final int TEST_TOKEN_RECT_BMP_ROW = TEST_TOKEN_RECT_BMP_COL + 1;  // 6023
    public static final int TEST_TOKEN_ENROLL_TEMPLATE_COUNT = TEST_TOKEN_RECT_BMP_ROW + 1;  // 6024
    public static final int TEST_TOKEN_AUTHENTICATE_RETRYCOUNT = TEST_TOKEN_ENROLL_TEMPLATE_COUNT + 1;  // 6025
    public static final int TEST_TOKEN_SENSOR_PREVIEW_ENHANCED = TEST_TOKEN_AUTHENTICATE_RETRYCOUNT + 1;  // 6026
    public static final int TEST_TOKEN_SENSOR_COL = TEST_TOKEN_SENSOR_PREVIEW_ENHANCED + 1;
    public static final int TEST_TOKEN_SENSOR_ROW = TEST_TOKEN_SENSOR_COL + 1;

    public static final int TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT = 1300;
    public static final int TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT = TEST_TOKEN_UNDER_SATURATED_PIXEL_COUNT + 1;
    public static final int TEST_TOKEN_SATURATED_PIXEL_THRESHOLD = TEST_TOKEN_OVER_SATURATED_PIXEL_COUNT + 1;
    public static final int TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE = TEST_TOKEN_SATURATED_PIXEL_THRESHOLD + 1;
    public static final int TEST_TOKEN_FINGERPRINT_ACQUIRED_INFO = TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE + 1;
    public static final int TEST_TOKEN_FINGERPRINT_FID = TEST_TOKEN_FINGERPRINT_ACQUIRED_INFO + 1;
    public static final int TEST_TOKEN_FINGERPRINT_GID = TEST_TOKEN_FINGERPRINT_FID + 1;
    public static final int TEST_TOKEN_FINGERPRINT_PROGRESS = TEST_TOKEN_FINGERPRINT_GID + 1;

    public static final int TEST_TOKEN_MAX_BAD_POINT_NUM = 1400;
    public static final int TEST_TOKEN_MAX_CLUSTER_NUM = TEST_TOKEN_MAX_BAD_POINT_NUM + 1;
    public static final int TEST_TOKEN_MAX_PIXEL_OF_LARGEST_BAD_CLUSTER = TEST_TOKEN_MAX_CLUSTER_NUM + 1;
    public static final int TEST_TOKEN_MAX_LIGHT_NOISET = TEST_TOKEN_MAX_PIXEL_OF_LARGEST_BAD_CLUSTER + 1;
    public static final int TEST_TOKEN_MAX_LIGHT_NOISES = TEST_TOKEN_MAX_LIGHT_NOISET + 1;
    public static final int TEST_TOKEN_MIN_FLESH_TOUCH_DIFF = TEST_TOKEN_MAX_LIGHT_NOISES + 1;
    public static final int TEST_TOKEN_MIN_FOV_AREA = TEST_TOKEN_MIN_FLESH_TOUCH_DIFF + 1;
    public static final int TEST_TOKEN_MAX_LIGHT_LEAK_RATIO = TEST_TOKEN_MIN_FOV_AREA + 1;
    public static final int TEST_TOKEN_MIN_RELATIVE_ILLUMINANCE = TEST_TOKEN_MAX_LIGHT_LEAK_RATIO + 1;
    public static final int TEST_TOKEN_MAX_SCALE_RATIO = TEST_TOKEN_MIN_RELATIVE_ILLUMINANCE + 1;
    public static final int TEST_TOKEN_MIN_SCALE_RATIO = TEST_TOKEN_MAX_SCALE_RATIO + 1;
    public static final int TEST_TOKEN_MIN_MASK_CROP_AREA = TEST_TOKEN_MIN_SCALE_RATIO + 1;
    public static final int TEST_TOKEN_MIN_SSNR = TEST_TOKEN_MIN_MASK_CROP_AREA + 1;
    public static final int TEST_TOKEN_MIN_SHAPENESS = TEST_TOKEN_MIN_SSNR + 1;
    public static final int TEST_TOKEN_MIN_P2P = TEST_TOKEN_MIN_SHAPENESS + 1;
    public static final int TEST_TOKEN_MIN_CHART_CONSTRAST = TEST_TOKEN_MIN_P2P + 1;
    public static final int TEST_TOKEN_EFF_REG_RAD = TEST_TOKEN_MIN_CHART_CONSTRAST + 1;
    public static final int TEST_TOKEN_EFF_REG_RAD2 = TEST_TOKEN_EFF_REG_RAD + 1;
    public static final int TEST_TOKEN_SPMT_BAD_PIX_THD = TEST_TOKEN_EFF_REG_RAD2 + 1;
    public static final int TEST_TOKEN_SPMT_BAD_PIX_THD2 = TEST_TOKEN_SPMT_BAD_PIX_THD + 1;
    public static final int TEST_TOKEN_SATURAT_PIX_HIGH_THD = TEST_TOKEN_SPMT_BAD_PIX_THD2 + 1;
    public static final int TEST_TOKEN_CHART_DIRECTION_THD = TEST_TOKEN_SATURAT_PIX_HIGH_THD + 1;
    public static final int TEST_TOKEN_CHART_DIRECTION_TARGET = TEST_TOKEN_CHART_DIRECTION_THD + 1;
    public static final int TEST_TOKEN_MAX_SCREEN_STRUCT_RATIO = TEST_TOKEN_CHART_DIRECTION_TARGET + 1;
    public static final int TEST_TOKEN_MAX_EXPO_TIME = TEST_TOKEN_MAX_SCREEN_STRUCT_RATIO + 1;
    public static final int TEST_TOKEN_MIN_EXPO_TIME = TEST_TOKEN_MAX_EXPO_TIME + 1;
    public static final int TEST_TOKEN_EXPOSURE_VALUE = TEST_TOKEN_MIN_EXPO_TIME + 1;


    public static final int TEST_TOKEN_SPMT_CALIBRATION_PERFORMANCE = 1500;

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
                case TEST_TOKEN_ERROR_CODE:
                case TEST_TOKEN_BMP_DATA_WIDTH:
                case TEST_TOKEN_BMP_DATA_HEIGHT:
                case TEST_TOKEN_LONG_EXPOSURE_WIDTH:
                case TEST_TOKEN_LONG_EXPOSURE_HEIGHT:
                case TEST_TOKEN_SHORT_EXPOSURE_WIDTH:
                case TEST_TOKEN_SHORT_EXPOSURE_HEIGHT:
                case TEST_TOKEN_FUSION_WIDTH:
                case TEST_TOKEN_FUSION_HEIGHT:
                case TEST_TOKEN_WEAK_DATA_WIDTH:
                case TEST_TOKEN_WEAK_DATA_HEIGHT:
                case TEST_TOKEN_SENSOR_ID:
                case TEST_TOKEN_MCU_ID:
                case TEST_TOKEN_FLASH_ID:
                case TEST_TOKEN_PMIC_ID:
                case TEST_TOKEN_EXPO_AUTO_CALIBRATION_FLAG:
                case TEST_TOKEN_LONG_PAG_GAIN:
                case TEST_TOKEN_SCALE_RATIO_VALUE:
                case TEST_TOKEN_SHORT_EXPOSURE_TIME:
                case TEST_TOKEN_SHORT_PAG_GAIN:
                case TEST_TOKEN_PERFORMANCE_P2P:
                case TEST_TOKEN_PERFORMANCE_NOISE:
                case TEST_TOKEN_PERFORMANCE_SSNR:
                case TEST_TOKEN_PERFORMANCE_MEAN_RIDGE:
                case TEST_TOKEN_PERFORMANCE_MEAN_VALLEY:
                case TEST_TOKEN_PERFORMANCE_SHARPNESS:
                case TEST_TOKEN_PERFORMANCE_SHARPNESS_ALL:
                case TEST_TOKEN_PERFORMANCE_CHART_TOUCH_DIFF:
                case TEST_TOKEN_PERFORMANCE_CHART_CONTRAST:
                case TEST_TOKEN_FACTORY_SNOISE:
                case TEST_TOKEN_FACTORY_TNOISE:
                case TEST_TOKEN_FACTORY_FLESH_TOUCH_DIFF:
                case TEST_TOKEN_FACTORY_LIGHT_LEAK_RATIO:
                case TEST_TOKEN_FACTORY_FOV_LEFT:
                case TEST_TOKEN_FACTORY_FOV_RIGHT:
                case TEST_TOKEN_FACTORY_FOV_UP:
                case TEST_TOKEN_FACTORY_FOV_DOWN:
                case TEST_TOKEN_FACTORY_ILLUM_MAX_X:
                case TEST_TOKEN_FACTORY_ILLUM_MAX_Y:
                case TEST_TOKEN_FACTORY_SCALE:
                case TEST_TOKEN_FACTORY_CROP_WIDTH:
                case TEST_TOKEN_FACTORY_CROP_HEIGHT:
                case TEST_TOKEN_FACTORY_BAD_POINT_NUM:
                case TEST_TOKEN_FACTORY_CLUSTER_NUM:
                case TEST_TOKEN_FACTORY_PIXEL_OF_LARGEST_BAD_CLUSTER:
                case TEST_TOKEN_FACTROY_SCREEN_STRUCT_RATIO:
                case TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE:
                case TEST_TOKEN_FINGERPRINT_FID:
                case TEST_TOKEN_FINGERPRINT_GID:
                case TEST_TOKEN_FINGERPRINT_PROGRESS:
                case TEST_TOKEN_FINGERPRINT_ACQUIRED_INFO:
                case TEST_TOKEN_ENROLL_TEMPLATE_COUNT:
                case TEST_TOKEN_IMAGE_QUALITY:
                case TEST_TOKEN_VALID_AREA:
                case TEST_TOKEN_PREPROCESS_TIME:
                case TEST_TOKEN_GET_FEATURE_TIME:
                case TEST_TOKEN_AUTHENTICATE_TIME:
                case TEST_TOKEN_FRAME_NUM:
                case TEST_TOKEN_FW_EXPOSE_TIME:
                case TEST_TOKEN_SENSOR_COL:
                case TEST_TOKEN_SENSOR_ROW:
                case TEST_TOKEN_MAX_BAD_POINT_NUM:
                case TEST_TOKEN_MAX_CLUSTER_NUM:
                case TEST_TOKEN_MAX_PIXEL_OF_LARGEST_BAD_CLUSTER:
                case TEST_TOKEN_MIN_FOV_AREA:
                case TEST_TOKEN_MIN_MASK_CROP_AREA:
                case TEST_TOKEN_EFF_REG_RAD:
                case TEST_TOKEN_EFF_REG_RAD2:
                case TEST_TOKEN_SPMT_BAD_PIX_THD:
                case TEST_TOKEN_SPMT_BAD_PIX_THD2:
                case TEST_TOKEN_SATURAT_PIX_HIGH_THD:
                case TEST_TOKEN_CHART_DIRECTION_THD:
                case TEST_TOKEN_CHART_DIRECTION_TARGET:
                {
                    int value = decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, value);
                    Log.d(TAG, "value = " + value);
                    break;
                }
                case TEST_TOKEN_AD_VERSION:
                case TEST_TOKEN_LENS_TYPE:
                case TEST_TOKEN_FUSION_DATA:
                case TEST_TOKEN_LONG_EXPOSURE_DATA:
                case TEST_TOKEN_SHORT_EXPOSURE_DATA:
                case TEST_TOKEN_CHIP_ID:
                case TEST_TOKEN_VENDOR_ID:
                case TEST_TOKEN_PRODUCTION_DATE:
                case TEST_TOKEN_FACTORY_RELATIVE_ILLUMINANCE:
                case TEST_TOKEN_BMP_DATA:
                case TEST_TOKEN_FINGERPRINT_ENUMERATING_FID:
                {

                    int size = decodeInt32(result, offset);
                    offset += 4;
                    if (size > 0) {
                        testResult.put(token, Arrays.copyOfRange(result, offset, offset + size));
                        offset += size;
                    }
                    break;
                }

                case TEST_TOKEN_ALGO_VERSION:
                case TEST_TOKEN_PREPROCESS_VERSION:
                case TEST_TOKEN_FW_VERSION:
                case TEST_TOKEN_TEE_VERSION:
                case TEST_TOKEN_TA_VERSION:
                case TEST_TOKEN_PRODUCTION_ALGO_VERSION: {
                    int size = decodeInt32(result, offset);
                    offset += 4;
                    testResult.put(token, new String(result, offset, size));
                    offset += size;
                    break;
                }
                case TEST_TOKEN_FACTORY_RMS:{
                    int size = decodeInt32(result, offset);
                    offset += 4;
                    double value = decodeDouble(result, offset, size);
                    testResult.put(token, value);
                    offset += size;
                    Log.d(TAG, "value = " + value);
                    break;
                }
                case TEST_TOKEN_MAX_LIGHT_NOISET:
                case TEST_TOKEN_MAX_LIGHT_NOISES:
                case TEST_TOKEN_MIN_FLESH_TOUCH_DIFF:
                case TEST_TOKEN_MAX_LIGHT_LEAK_RATIO:
                case TEST_TOKEN_MIN_RELATIVE_ILLUMINANCE:
                case TEST_TOKEN_MAX_SCALE_RATIO:
                case TEST_TOKEN_MIN_SCALE_RATIO:
                case TEST_TOKEN_MIN_SSNR:
                case TEST_TOKEN_MIN_SHAPENESS:
                case TEST_TOKEN_MIN_P2P:
                case TEST_TOKEN_MIN_CHART_CONSTRAST:
                case TEST_TOKEN_MAX_SCREEN_STRUCT_RATIO:
                {
                    int size = decodeInt32(result, offset);
                    offset += 4;
                    float value = decodeFloat(result, offset, size);
                    testResult.put(token, value);
                    offset += size;
                    Log.d(TAG, "value = " + value);
                    break;
                }
                default:
                    break;
            }
        }

        return testResult;
    }
}

