/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

interface IGoodixFingerprintDumpCallback {
    void onDump(int chipSeries, int cmdId, in byte[] data);
}
