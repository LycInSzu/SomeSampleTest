/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import com.goodix.fingerprint.GFBaikalConfig;
import com.goodix.fingerprint.GFShenzhenConfig;
import com.goodix.fingerprint.GFConfig;
import com.goodix.fingerprint.GFDevice;
import com.goodix.fingerprint.service.IGoodixFingerprintCallback;
import com.goodix.fingerprint.service.IGoodixFingerprintDumpCallback;
import com.goodix.fingerprint.service.IGoodixFingerprintSensorCallback;
import com.goodix.fingerprint.service.IGoodixFingerprintPreviewCallback;

interface IGoodixFingerprintInterface {
    void untrustedAuthenticate(IBinder token, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    void untrustedAuthenticate2(String userDesc,IBinder token, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    void cancelUntrustedAuthentication(IBinder token, String opPackageName);
    void untrustedEnroll(IBinder token, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    void untrustedEnroll2(String userDesc,IBinder token, int userId, IGoodixFingerprintCallback receiver, String opPackageName);

    void cancelUntrustedEnrollment(IBinder token, String opPackageName);
    void untrustedRemove(IBinder token, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    boolean hasEnrolledUntrustedFingerprint(String opPackageName);
    int getEnrolledUntrustedFingerprint(String opPackageName);
    void testCmd(IBinder token, int cmdId, in byte[] param, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    int testSync(IBinder token, int cmdId, in byte[] param, int userId, IGoodixFingerprintCallback receiver, String opPackageName);
    void cancelTestCmd(IBinder token, String opPackageName);
    void setSafeClass(int safeClass, String opPackageName);
    void navigate(int navMode, String opPackageName);
    void stopNavigation(String opPackageName);
    void enableFingerprintModule(boolean enable, String opPackageName);
    void cameraCapture(String opPackageName);
    void stopCameraCapture(String opPackageName);
    void enableFfFeature(boolean enable, String opPackageName);
    void screenOn(String opPackageName);
    void screenOff(String opPackageName);
    GFConfig getConfig(String opPackageName);
    GFDevice getDevice(String opPackageName);
    void dump(IGoodixFingerprintDumpCallback callback, String opPackageName);
    void cancelDump(String opPackageName);
    void dumpCmd(int cmdId, in byte[] param, String opPackageName);
    // for fido
    int authenticateFido(IBinder token, in byte[] aaid, in byte[] finalChallenge, IGoodixFingerprintCallback receiver, int userId, String opPackageName);
    int stopAuthenticateFido(IBinder token, String opPackageName);
    int isIdValid(int fingerId, String opPackageName);
    int[] getIdList(String opPackageName);
    int invokeFidoCommand(in byte[] inBuf, out byte[] outBuf);
    // for baikal
    void registerSensorControlCallback(IGoodixFingerprintSensorCallback callback);
    void unregisterSensorControlCallback(IGoodixFingerprintSensorCallback callback);
    void registerPreviewControlCallback(IGoodixFingerprintPreviewCallback callback);
    void unregisterPreviewControlCallback(IGoodixFingerprintPreviewCallback callback);
    GFBaikalConfig getBaikalConfig();
    void setBaikalConfig(in GFBaikalConfig config);
    // for vivo x20
    void openOrCloseHBMMode(int mode);
    // for oppo
    void setHBMMode(boolean open);
    int getScreenBrightness();
    void setScreenBrightness(int value);
    void setScreenBrightnessR15(int value);
    void setScreenManualMode();
    void goToSleep();
    void wakeUp();
    //for Shenzhen
    GFShenzhenConfig getShenzhenConfig();
    void setShenzhenConfig(in GFShenzhenConfig config);
}
