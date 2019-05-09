/*
 * Copyright (C) 2013-2018, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.graphics.Color;

import com.goodix.fingerprint.Constants;

public class ShenzhenConstants extends Constants {
    public static final int CMD_TEST_SZ_TEST_BASE = 0x600;
    public static final int CMD_TEST_SZ_FINGER_DOWN = CMD_TEST_SZ_TEST_BASE;
    public static final int CMD_TEST_SZ_FINGER_UP   = CMD_TEST_SZ_FINGER_DOWN + 1;
    public static final int CMD_TEST_SZ_ENROLL      = CMD_TEST_SZ_FINGER_UP + 1;
    public static final int CMD_TEST_SZ_FIND_SENSOR = CMD_TEST_SZ_ENROLL + 1;
    public static final int CMD_TEST_SZ_FUSION_PREVIEW  = CMD_TEST_SZ_FIND_SENSOR + 1;
    public static final int CMD_TEST_SZ_UNTRUSTED_ENROLL    =   CMD_TEST_SZ_FUSION_PREVIEW + 1;
    public static final int CMD_TEST_SZ_UNTRUSTED_AUTHENTICATE  = CMD_TEST_SZ_UNTRUSTED_ENROLL + 1;
    public static final int CMD_TEST_SZ_DELETE_UNTRUSTED_ENROLLED_FINGER = CMD_TEST_SZ_UNTRUSTED_AUTHENTICATE + 1;
    public static final int CMD_TEST_SZ_RAWDATA_PREVIEW     = CMD_TEST_SZ_DELETE_UNTRUSTED_ENROLLED_FINGER + 1;
    public static final int CMD_TEST_SZ_LDC_CALIBRATE       = CMD_TEST_SZ_RAWDATA_PREVIEW + 1;
    public static final int CMD_TEST_SZ_ENROLL_TEMPLATE_COUNT = CMD_TEST_SZ_LDC_CALIBRATE + 1;
    public static final int CMD_TEST_SZ_UPDATE_CAPTURE_PARM     = CMD_TEST_SZ_ENROLL_TEMPLATE_COUNT + 1;
    public static final int CMD_TEST_SZ_CANCEL                  =   CMD_TEST_SZ_UPDATE_CAPTURE_PARM + 1;
    public static final int CMD_TEST_SZ_GET_CONFIG              =   CMD_TEST_SZ_CANCEL + 1;
    public static final int CMD_TEST_SZ_GET_VERSION             =   CMD_TEST_SZ_GET_CONFIG + 1;
    public static final int CMD_TEST_SZ_K_B_CALIBRATION         =   CMD_TEST_SZ_GET_VERSION + 1;
    public static final int CMD_TEST_SZ_SET_GROUP_ID            =   CMD_TEST_SZ_K_B_CALIBRATION + 1;
    public static final int CMD_TEST_SZ_UPDATE_CFG              =   CMD_TEST_SZ_SET_GROUP_ID + 1;
    public static final int CMD_TEST_SZ_UPDATE_FW               =   CMD_TEST_SZ_UPDATE_CFG + 1;
    public static final int CMD_TEST_SZ_UNTRUSTED_ENUMERATE     =   CMD_TEST_SZ_UPDATE_FW + 1;

    public static final int CMD_TEST_SZ_FT_CAPTURE_DARK_BASE = CMD_TEST_SZ_TEST_BASE + 20;
    public static final int CMD_TEST_SZ_FT_CAPTURE_H_DARK = CMD_TEST_SZ_FT_CAPTURE_DARK_BASE + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_L_DARK = CMD_TEST_SZ_FT_CAPTURE_H_DARK + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_H_FLESH = CMD_TEST_SZ_FT_CAPTURE_L_DARK + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_L_FLESH = CMD_TEST_SZ_FT_CAPTURE_H_FLESH + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_CHART   = CMD_TEST_SZ_FT_CAPTURE_L_FLESH + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_CHECKBOX = CMD_TEST_SZ_FT_CAPTURE_CHART + 1;
    public static final int CMD_TEST_SZ_FT_CAPTURE_LOCATION_IMAGE = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX + 1;
    public static final int CMD_TEST_SZ_FT_FACTORY_PERFORMANCE = CMD_TEST_SZ_FT_CAPTURE_LOCATION_IMAGE + 1;
    public static final int CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION = CMD_TEST_SZ_FT_FACTORY_PERFORMANCE + 1;
    public static final int CMD_TEST_SZ_FT_STOP_EXPO_AUTO_CALIBRATION = CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION + 1;
    public static final int CMD_TEST_SZ_FT_RESET                 = CMD_TEST_SZ_FT_STOP_EXPO_AUTO_CALIBRATION + 1;
    public static final int CMD_TEST_SZ_FT_SPI_RST_INT           = CMD_TEST_SZ_FT_RESET + 1;
    public static final int CMD_TEST_SZ_FT_SPI                   = CMD_TEST_SZ_FT_SPI_RST_INT + 1;
    public static final int CMD_TEST_SZ_FT_INIT                  = CMD_TEST_SZ_FT_SPI + 1;
    public static final int CMD_TEST_SZ_FT_EXIT                  = CMD_TEST_SZ_FT_INIT + 1;
    public static final int CMD_TEST_SZ_FT_CALIBRATE             = CMD_TEST_SZ_FT_EXIT + 1;
    public static final int CMD_TEST_SZ_FT_MT_CHECK              = CMD_TEST_SZ_FT_CALIBRATE + 1;
    public static final int CMD_SZ_DUMP_TEST = CMD_TEST_SZ_FT_MT_CHECK +1;

    public static final int CMD_TEST_SZ_DUMP_TEMPLATE = CMD_TEST_SZ_TEST_BASE + 60;
    public static final int CMD_TEST_SZ_SET_HBM_MODE = CMD_TEST_SZ_DUMP_TEMPLATE + 1;
    public static final int CMD_TEST_SZ_CLOSE_HBM_MODE = CMD_TEST_SZ_SET_HBM_MODE + 1;
    public static final int CMD_TEST_SZ_SET_HIGH_BRIGHTNESS = CMD_TEST_SZ_CLOSE_HBM_MODE + 1;
    public static final int CMD_TEST_SZ_SET_LOW_BRIGHTNESS = CMD_TEST_SZ_SET_HIGH_BRIGHTNESS + 1;
    public static final int CMD_TEST_SZ_SET_DUMP_ENABLE_FLAG = CMD_TEST_SZ_SET_LOW_BRIGHTNESS + 1;
    public static final int CMD_TEST_SZ_FACTORY_TEST_GET_MT_INFO = CMD_TEST_SZ_SET_DUMP_ENABLE_FLAG + 1;
    public static final int CMD_TEST_SZ_SET_MAX_BRIGHTNESS = CMD_TEST_SZ_FACTORY_TEST_GET_MT_INFO + 1;
    public static final int CMD_TEST_SZ_LOCAL_AREA_SAMPLE = CMD_TEST_SZ_SET_MAX_BRIGHTNESS + 1;
    public static final int CMD_TEST_SZ_ENABLE_POWER = CMD_TEST_SZ_LOCAL_AREA_SAMPLE + 1;
    public static final int CMD_TEST_SZ_DISABLE_POWER = CMD_TEST_SZ_ENABLE_POWER + 1;

    public static final int DEFAULT_SENSOR_X = 472;
    public static final int DEFAULT_SENSOR_Y = 1596;
    public static final int DEFAULT_SENSOR_WIDTH = 150;
    public static final int DEFAULT_SENSOR_HEIGHT = 150;
    public static final boolean DEFAULT_LOCK_ASPECT_RATIO = true;
    public static final int DEFAULT_ASPECT_RATIO_WIDTH = 10;
    public static final int DEFAULT_ASPECT_RATIO_HEIGHT = 10;
    public static final int DEFAULT_PREVIEW_SCALE_RATIO = 150;

    public static final int DEFAULT_SENSOR_AREA_CYAN_COLOR = Color.CYAN;
    public static final int DEFAULT_SENSOR_AREA_BLACK_COLOR = Color.WHITE;

    public static final int MAX_TEMPLATE_COUNT = 20;
    public static final int MAX_SAMPLE_COUNT = 50;

    public static final int MAX_REAL_SAMPLE_COUNT = 50;
    public static final int MAX_FAKE_SAMPLE_COUNT = 50;

    public static final int GF_SAFE_CLASS_MEDIUM = 2;

    public static final String PREFERENCE_SENSOR_CONFIG_INFO = "sensor_config_info";
    public static final String PREFERENCE_SHORT_EXPOSURE_TIME = "short_exposure_time";
    public static final String PREFERENCE_RECT_BMP_COL = "rect_bmp_col";
    public static final String PREFERENCE_RECT_BMP_ROW = "rect_bmp_row";
    public static final String PREFERENCE_KEY_SENSOR_X = "sensor_x";
    public static final String PREFERENCE_KEY_SENSOR_Y = "sensor_y";
    public static final String PREFERENCE_KEY_SENSOR_ROW = "sensor_row";
    public static final String PREFERENCE_KEY_SENSOR_COL = "sensor_col";
    public static final String PREFERENCE_KEY_SENSOR_WIDTH = "sensor_width";
    public static final String PREFERENCE_KEY_SENSOR_HEIGHT = "sensor_height";
    public static final String PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH = "aspect_ratio_width";
    public static final String PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT = "aspect_ratio_height";
    public static final String PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO = "preview_scale_ratio";
    public static final String PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR = "sensor_area_background_color";
}
