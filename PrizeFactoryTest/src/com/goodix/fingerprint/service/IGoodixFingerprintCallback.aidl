/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

interface IGoodixFingerprintCallback {
    void onTestCmd(int chipSeries, int cmdId, in byte[] result);
    void onAuthenticateFido(int fingerId, in byte[] uvt);
    void onEnrollResult(int fingerId, int remaining);
    void onImageResult(int msgType,in byte[] result,int width,int height);
    void onAcquired(int acquiredInfo);
    void onAuthenticationSucceeded(int fingerId);
    void onAuthenticationFailed();
    void onError(int error);
    void onRemoved(int fingerId);
}
