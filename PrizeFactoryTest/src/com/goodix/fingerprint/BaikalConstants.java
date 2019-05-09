/*
 * Copyright (C) 2013-2018, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import com.goodix.fingerprint.Constants;

public class BaikalConstants {
    public static final int GF_FINGERPRINT_SENSOR_DISPLAY_CONTROL = Constants.GF_FINGERPRINT_MSG_TYPE_MAX + 1;
    public static final int GF_FINGERPRINT_PREVIEW_DISPLAY_CONTROL = GF_FINGERPRINT_SENSOR_DISPLAY_CONTROL + 1;

    public static final int CMD_TEST_BAIKAL_FINGER_DOWN = Constants.CMD_TEST_MAX + 1; // 8
    public static final int CMD_TEST_BAIKAL_FINGER_UP = CMD_TEST_BAIKAL_FINGER_DOWN + 1;
    public static final int CMD_TEST_CONTRAST = CMD_TEST_BAIKAL_FINGER_UP + 1;  // 10
    public static final int CMD_TEST_OTP = CMD_TEST_CONTRAST + 1;
    public static final int CMD_TEST_K_B_CALIBRATION = CMD_TEST_OTP + 1;
    public static final int CMD_TEST_FIND_SENSOR = CMD_TEST_K_B_CALIBRATION + 1;
    public static final int CMD_TEST_CIRCLE_LOCATION = CMD_TEST_FIND_SENSOR + 1;
    public static final int CMD_TEST_CIRCLE_DEVIATION = CMD_TEST_CIRCLE_LOCATION + 1;
    public static final int CMD_TEST_UPDATA_CIRCLE_BASE_FILE = CMD_TEST_CIRCLE_DEVIATION + 1;
    public static final int CMD_TEST_REMOVE_CIRCLE_BASE_FILE = CMD_TEST_UPDATA_CIRCLE_BASE_FILE + 1;
    public static final int CMD_TEST_AUTO_FIND_SENSOR_INIT = CMD_TEST_REMOVE_CIRCLE_BASE_FILE + 1;
    public static final int CMD_TEST_AUTO_FIND_SENSOR = CMD_TEST_AUTO_FIND_SENSOR_INIT + 1;
    public static final int CMD_TEST_SPI = CMD_TEST_AUTO_FIND_SENSOR + 1;  // 20
    public static final int CMD_TEST_RESET_PIN_AND_INTERRUPT_PIN = CMD_TEST_SPI + 1;
    public static final int CMD_TEST_OTP_FLASH = CMD_TEST_RESET_PIN_AND_INTERRUPT_PIN + 1;
    public static final int CMD_TEST_PIXEL_OPENSHORT = CMD_TEST_OTP_FLASH + 1;
    public static final int CMD_TEST_PERFORMANCE_TESTING = CMD_TEST_PIXEL_OPENSHORT + 1;
    public static final int CMD_TEST_FORCE_KEY = CMD_TEST_PERFORMANCE_TESTING + 1;
    public static final int CMD_TEST_PERFORMANCE = CMD_TEST_FORCE_KEY + 1;
    public static final int CMD_TEST_SENSOR_LOCATION_INIT = CMD_TEST_PERFORMANCE + 1;
    public static final int CMD_TEST_SENSOR_LOCATION = CMD_TEST_SENSOR_LOCATION_INIT + 1;
    public static final int CMD_TEST_PIXEL_OPEN_SHORT = CMD_TEST_SENSOR_LOCATION + 1;
    public static final int CMD_TEST_NOISE = CMD_TEST_PIXEL_OPEN_SHORT + 1;  // 30
    public static final int CMD_TEST_MSG_PHONE_UNLOCK = CMD_TEST_NOISE + 1;
    public static final int CMD_TEST_GET_RESET_COUNTS = CMD_TEST_MSG_PHONE_UNLOCK + 1;
    public static final int CMD_TEST_CLEAR_RESET_COUNTS = CMD_TEST_GET_RESET_COUNTS + 1;
    public static final int CMD_TEST_START_GET_RESET_COUNTS = CMD_TEST_CLEAR_RESET_COUNTS + 1;
    public static final int CMD_TEST_STOP_GET_RESET_COUNTS = CMD_TEST_START_GET_RESET_COUNTS + 1;  // 35
    public static final int CMD_TEST_LOCATION_CIRCLE_CALIBRATION = CMD_TEST_STOP_GET_RESET_COUNTS + 1;  // 36
    public static final int CMD_TEST_SPI_VIVO = CMD_TEST_LOCATION_CIRCLE_CALIBRATION + 1;  // 37
    public static final int CMD_TEST_FLASH_RW = CMD_TEST_SPI_VIVO + 1;

    //for huawei mmi test
    public static final int CMD_MMI_TEST_MAX = 10001;
    public static final int CMD_MMI_AUTO_TEST = CMD_MMI_TEST_MAX + 1;
    public static final int CMD_MMI_TYPE_INTERRUPT_TEST = CMD_MMI_AUTO_TEST + 1;
    public static final int CMD_MMI_FAKE_FINGER_TEST = CMD_MMI_TYPE_INTERRUPT_TEST + 1;
    public static final int CMD_MMI_SNR_SINGAL_IMAGE_TEST = CMD_MMI_FAKE_FINGER_TEST + 1;
    public static final int CMD_MMI_SNR_WHITE_IMAGE_TEST = CMD_MMI_SNR_SINGAL_IMAGE_TEST + 1;
    public static final int CMD_MMI_BUBBLE_TEST = CMD_MMI_SNR_WHITE_IMAGE_TEST + 1;
    public static final int CMD_MMI_SN_TEST = CMD_MMI_BUBBLE_TEST + 1;
    public static final int CMD_MMI_OPTICAL_CALIBRATION_TEST = CMD_MMI_SN_TEST + 1;
    public static final int CMD_MMI_SCENE_TEST = CMD_MMI_OPTICAL_CALIBRATION_TEST + 1;

    public static final int TEST_SPI_CHIP_ID = 0x12;
}

