/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.utils;

import android.content.Context;
import android.os.Vibrator;

/**
 * Utility class for dealing with fingerprints and fingerprint settings.
 */
public class FingerprintUtils {

    private static final long[] FP_ERROR_VIBRATE_PATTERN = new long[] {
            0, 30, 100, 30
    };
    private static final long[] FP_SUCCESS_VIBRATE_PATTERN = new long[] {
            0, 30
    };

    public static void vibrateFingerprintError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator != null) {
            vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFingerprintSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator != null) {
            vibrator.vibrate(FP_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

}
