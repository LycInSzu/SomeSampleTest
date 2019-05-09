/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

interface IGoodixFingerprintSensorCallback {
    void onSensorControlCmd(int cmdId, in byte[] data);
}
