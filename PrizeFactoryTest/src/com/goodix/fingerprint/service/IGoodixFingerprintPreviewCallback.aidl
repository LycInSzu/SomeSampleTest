/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

interface IGoodixFingerprintPreviewCallback {
    void onPreviewControlCmd(int cmdId, in byte[] data);
}
