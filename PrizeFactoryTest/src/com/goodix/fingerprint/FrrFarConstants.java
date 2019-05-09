/*
 * Copyright (C) 2013-2018, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import com.goodix.fingerprint.Constants;


public class FrrFarConstants extends Constants {
    public static final int CMD_TEST_FRR_FAR_BASE = ShenzhenConstants.CMD_TEST_SZ_TEST_BASE + 1000;

    public static final int CMD_TEST_SZ_FRR_FAR_GET_CHIP_TYPE = CMD_TEST_FRR_FAR_BASE;
    public static final int CMD_TEST_SZ_FRR_FAR_INIT    = CMD_TEST_SZ_FRR_FAR_GET_CHIP_TYPE + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_RECORD_CALIBRATION = CMD_TEST_SZ_FRR_FAR_INIT + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_RECORD_ENROLL = CMD_TEST_SZ_FRR_FAR_RECORD_CALIBRATION + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_RECORD_AUTHENTICATE = CMD_TEST_SZ_FRR_FAR_RECORD_ENROLL + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_RECORD_AUTHENTICATE_FINISH = CMD_TEST_SZ_FRR_FAR_RECORD_AUTHENTICATE + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_PLAY_CALIBRATION = CMD_TEST_SZ_FRR_FAR_RECORD_AUTHENTICATE_FINISH + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_PLAY_ENROLL = CMD_TEST_SZ_FRR_FAR_PLAY_CALIBRATION + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_PLAY_AUTHENTICATE = CMD_TEST_SZ_FRR_FAR_PLAY_ENROLL + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_ENROLL_FINISH = CMD_TEST_SZ_FRR_FAR_PLAY_AUTHENTICATE + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_SAVE_FINGER = CMD_TEST_SZ_FRR_FAR_ENROLL_FINISH + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_DEL_FINGER = CMD_TEST_SZ_FRR_FAR_SAVE_FINGER + 1;
    public static final int CMD_TEST_SZ_CANCEL_FRR_FAR = CMD_TEST_SZ_FRR_FAR_DEL_FINGER + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_PLAY_ENROLL_RAWDATA = CMD_TEST_SZ_CANCEL_FRR_FAR + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_PLAY_AUTH_RAWDATA = CMD_TEST_SZ_FRR_FAR_PLAY_ENROLL_RAWDATA + 1;
    public static final int CMD_TEST_SZ_CANCEL_FRR_FAR_PREPROCESS = CMD_TEST_SZ_FRR_FAR_PLAY_AUTH_RAWDATA + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P0 = CMD_TEST_SZ_CANCEL_FRR_FAR_PREPROCESS + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P1 = CMD_TEST_SZ_FRR_FAR_CALI_P0 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P2 = CMD_TEST_SZ_FRR_FAR_CALI_P1 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P3 = CMD_TEST_SZ_FRR_FAR_CALI_P2 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P4 = CMD_TEST_SZ_FRR_FAR_CALI_P3 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P5 = CMD_TEST_SZ_FRR_FAR_CALI_P4 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P6 = CMD_TEST_SZ_FRR_FAR_CALI_P5 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P7 = CMD_TEST_SZ_FRR_FAR_CALI_P6 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P8 = CMD_TEST_SZ_FRR_FAR_CALI_P7 + 1;
    public static final int CMD_TEST_SZ_FRR_FAR_CALI_P9 = CMD_TEST_SZ_FRR_FAR_CALI_P8 + 1;

    public static final int TEST_TOKEN_FRR_FAR_GROUP_ID = 10000;
    public static final int TEST_TOKEN_FRR_FAR_FINGER_ID = TEST_TOKEN_FRR_FAR_GROUP_ID + 1;
    public static final int TEST_TOKEN_FRR_FAR_SAVE_FINGER_PATH = TEST_TOKEN_FRR_FAR_FINGER_ID + 1;
    public static final int TEST_TOKEN_FRR_FAR_FUSION_DATA = TEST_TOKEN_FRR_FAR_SAVE_FINGER_PATH + 1;
    public static final int TEST_TOKEN_FRR_FAR_LONG_EXPO_DATA = TEST_TOKEN_FRR_FAR_FUSION_DATA + 1;
    public static final int TEST_TOKEN_FRR_FAR_SHORT_EXPO_DATA = TEST_TOKEN_FRR_FAR_LONG_EXPO_DATA + 1;
    public static final int TEST_TOKEN_FRR_FAR_PUS_BASE = TEST_TOKEN_FRR_FAR_SHORT_EXPO_DATA + 1;
    public static final int TEST_TOKEN_FRR_FAR_PUS_BASE_UNIT = TEST_TOKEN_FRR_FAR_PUS_BASE + 1;
    public static final int TEST_TOKEN_FRR_FAR_PUS_KR = TEST_TOKEN_FRR_FAR_PUS_BASE_UNIT + 1;
    public static final int TEST_TOKEN_FRR_FAR_HOLE_MASK = TEST_TOKEN_FRR_FAR_PUS_KR + 1;
    public static final int TEST_TOKEN_FRR_FAR_FUSION_DATA_WIDTH = TEST_TOKEN_FRR_FAR_HOLE_MASK + 1;;
    public static final int TEST_TOKEN_FRR_FAR_FUSION_DATA_HEIGHT = TEST_TOKEN_FRR_FAR_FUSION_DATA_WIDTH + 1;
    public static final int TEST_TOKEN_FRR_FAR_RAWFEATUREST_LEN = TEST_TOKEN_FRR_FAR_FUSION_DATA_HEIGHT + 1;
    public static final int TEST_TOKEN_FRR_FAR_RAWFEATUREST_DATA = TEST_TOKEN_FRR_FAR_RAWFEATUREST_LEN + 1;
}
