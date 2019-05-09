/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.GFBaikalConfig;
import com.goodix.fingerprint.GFConfig;
import com.goodix.fingerprint.GFDevice;
import com.goodix.fingerprint.GFShenzhenConfig;
import com.goodix.fingerprint.ShenzhenConstants;
import com.goodix.fingerprint.shenzhen.sensor.SensorControlService;
import com.goodix.fingerprint.utils.ShenzhenTestResultParser;
import com.goodix.fingerprint.utils.TestResultParser;
import com.pri.factorytest.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GoodixFingerprintManager {
    private static final String TAG = "GoodixFingerprintManager";
    private Context mContext = null;
    private Handler mHandler = null;
    private static GoodixFingerprintManager sGoodixFingerprintManager;
    private static IGoodixFingerprintInterface mIGoodixFingerprintInterface = null;

    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;
    public static final int FINGERPRINT_ERROR_CANCELED = 5;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_INVALID_BASE = 1007;

    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;

    public static final int FINGERPRINT_SHOW_INDICATOR           = 1;
    public static final int FINGERPRINT_AREA_HIDE                = 2;
    public static final int FINGERPRINT_CHANGE_TO_BACKGROUND_BGCOLOR = 3;
    private static final int FINGERPRINT_AREA_CHANGE_CYAN_BGCOLOR = 4;
    public static final int FINGERPRINT_CHANGE_TO_SPMT_MODE = 5;
    public static final int FINGERPRINT_CLEAR_SPMT_MODE = 6;

    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;
    private static final int MSG_ENROLL_IMAGE_RESULT = 106;
    private static final int MSG_AUTHENTICATE_IMAGE_RESULT = 107;

    private static final int MSG_TEST = 1000;
    private static final int MSG_DUMP = 1001;
    private static final int MSG_AUTHENTICATED_FIDO = 1002;

    public static final int MSG_AUTHENTICATED_FIDO_SUCCESS = 100;
    public static final int MSG_AUTHENTICATED_FIDO_CANCELED = 102;
    public static final int MSG_AUTHENTICATED_FIDO_NO_MATCH = 103;
    public static final int MSG_AUTHENTICATED_FIDO_LOCKOUT = 107;
    public static final int MSG_AUTHENTICATED_FIDO_TIMEOUT = 113;

    private String mOpPackageName = null;
    private AuthenticateFidoCallback mAuthenticateFidoCallback = null;

    private class OnUntrustedEnrollCancelListener implements OnCancelListener {
        @Override
        public void onCancel() {
            cancelUntrustedEnrollment();
        }
    }

    private class OnUntrustedAuthenticationCancelListener implements OnCancelListener {
        @Override
        public void onCancel() {
            cancelUntrustedAuthentication();
        }
    }

    private TestCmdCallback mTestCallback = null;
    private DumpCallback mDumpCallback = null;
    private UntrustedAuthenticationCallback mUntrustedAuthenticationCallback = null;
    private UntrustedEnrollmentCallback mUntrustedEnrollmentCallback = null;
    private UntrustedRemovalCallback mUntrustedRemovalCallback = null;
    private TimeOutRunnable mTimeOutRunnable = null;

    private static SensorControlService mSensorCallBack;
    //private static PreviewControlService mPreviewCallback;

    public interface TestCmdCallback {
        public void onTestCmd(int cmdId, HashMap<Integer, Object> result);
    }

    public interface DumpCallback {
        public void onDump(int cmdId, HashMap<Integer, Object> data);
    }

    public static abstract class UntrustedEnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString){}
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString){}
        public void onEnrollmentProgress(int fingerId, int remaining){}
        public void onEnrollmentAcquired(int acquireInfo){}
        public void onEnrollImage(byte [] bmpData,int width,int height){}
    }

    public static abstract class UntrustedAuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString){}
        public void onAuthenticationHelp(int helpCode, CharSequence helpString){}
        public void onAuthenticationSucceeded(int fingerId){}
        public void onAuthenticationFailed(){}
        public void onAuthenticationAcquired(int acquireInfo){}
        public void onAuthenticateImage(byte [] bmpData,int width,int height){}
    }

    public static abstract class UntrustedRemovalCallback {
        public void onRemovalError(int errMsgId, CharSequence errString){}
        public void onRemovalSucceeded(int fingerId){}
    }

    public void showSensorViewWindow(boolean show) {
        if(mSensorCallBack == null) {
            return;
        }
        mSensorCallBack.onSensorControlCmd(show ?
                FINGERPRINT_SHOW_INDICATOR : FINGERPRINT_AREA_HIDE, null);
    }

    public void setSPMTMode(boolean isSPMTMode){
        if(mSensorCallBack == null) {
            return;
        }
        mSensorCallBack.onSensorControlCmd(isSPMTMode ?
                FINGERPRINT_CHANGE_TO_SPMT_MODE : FINGERPRINT_CLEAR_SPMT_MODE, null);
    }

    public void setSensorAreaToBackgroundColor() {
        if(mSensorCallBack == null) {
            return;
        }
        mSensorCallBack.onSensorControlCmd(FINGERPRINT_CHANGE_TO_BACKGROUND_BGCOLOR, null);
    }

    private String getAppOpPackageName() {
        String opPackageName = null;

        if (mContext == null) {
            return null;
        }

        try {
            Method getOpPackageName = mContext.getClass().getMethod("getOpPackageName");
            getOpPackageName.setAccessible(true);
            opPackageName = (String)getOpPackageName.invoke(mContext);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getAppOpPackageName NoSuchMethodException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getAppOpPackageName IllegalAccessException: ", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getAppOpPackageName InvocationTargetException: ", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getAppOpPackageName IllegalArgumentException: ", e);
        }

        return opPackageName;
    }

    public static interface AuthenticateFidoCallback {
        void onResult(int result, byte[] uvt, byte[] fingerId);
    }

    public int authenticateFido(AuthenticateFidoCallback callback, byte[] aaid, byte[] finalChallenge, long timeout) {
        Log.d(TAG, "authenticateFido");
        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return -1;
        }

        if (null == callback) {
            Log.e(TAG, "authenticate fido callback is null");
            return -1;
        }

        if (null == aaid || null == finalChallenge) {
            Log.e(TAG, "aaid or finalChallenge is null");
            return -1;
        }

        mTimeOutRunnable = new TimeOutRunnable();
        resetFidoAuthenticateTimeOut(timeout);
        mAuthenticateFidoCallback = callback;

        try {
            Log.d(TAG, "authenticateFido, register callback");
            return mIGoodixFingerprintInterface.authenticateFido(mGoodixFingerprintCallback.asBinder(), aaid, finalChallenge,
                    mGoodixFingerprintCallback, getMyUserId(), mOpPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void resetFidoAuthenticateTimeOut(long timeout){
        if (null != mHandler) {
            mHandler.postDelayed(mTimeOutRunnable, timeout);
        }
    }

    private class TimeOutRunnable implements Runnable{

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (null != mAuthenticateFidoCallback) {
                mAuthenticateFidoCallback.onResult(MSG_AUTHENTICATED_FIDO_TIMEOUT, null, null);
            }
        }
    }

    public void stopAuthenticateFido() {
        Log.d(TAG, "stopAuthenticateFido");
        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }
        mHandler.removeCallbacks(mTimeOutRunnable);
        mTimeOutRunnable = null;
        mAuthenticateFidoCallback = null;
        try {
            mIGoodixFingerprintInterface.stopAuthenticateFido(mGoodixFingerprintCallback.asBinder(), mOpPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int isIdValid(byte[] fingerId) {
        Log.d(TAG, "isIdValid");
        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return -1;
        }

        try {
            return mIGoodixFingerprintInterface.isIdValid(bytes2int(fingerId),
                    mOpPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void startFpManager(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        String packageName = "com.android.settings";
        String className = "com.android.settings.Settings$FingerPrintSettingsActivity";
        intent.setComponent(new ComponentName(packageName, className));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(":settings:show_fragment","com.android.settings.FingerPrintSettingsActivity");
        context.startActivity(intent);
    }

    public int[] getIdList() {
        Log.d(TAG, "getIdList");
        try {
            return mIGoodixFingerprintInterface.getIdList(mOpPackageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int invokeFidoCommand(byte[] inBuf, byte[] outBuf) {
        Log.d(TAG, "invokeFidoCommand");
        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return -1;
        }

        if (null == inBuf) {
            Log.e(TAG, "invalid parameter, inBuf is NULL");
            return -1;
        }

        if (null == outBuf) {
            Log.e(TAG, "invalid parameter, outBuf is NULL");
            return -1;
        }

        try {
            return mIGoodixFingerprintInterface.invokeFidoCommand(inBuf, outBuf);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void createManager(Context context){
        if(sGoodixFingerprintManager == null){
            Log.d(TAG, "createManager ");
            sGoodixFingerprintManager = new GoodixFingerprintManager(context);
        }
    }

    public static GoodixFingerprintManager getFingerprintManager(Context context){

        Log.e(TAG, "getFingerprintManager ");

        if(sGoodixFingerprintManager == null){
            sGoodixFingerprintManager = new GoodixFingerprintManager(context);
        }

        while (mIGoodixFingerprintInterface == null){
            Log.e(TAG, "IGoodixFingerprintInterface == null ");
            initService();

            if(mIGoodixFingerprintInterface != null){
                break;
            }
        }

        return sGoodixFingerprintManager;
    }

    public GoodixFingerprintManager(Context context) {
        mContext = context;
        mHandler = new MyHandler(context);
        mOpPackageName = getAppOpPackageName();

        mSensorCallBack = SensorControlService.getInstance(context);
    }

    private static void initService() {
        IBinder binder = null;
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Log.d(TAG, "success to get ServiceManager");

            Method getService = serviceManager.getMethod("getService", String.class);
            Log.d(TAG, "success to get method:getService");

            binder = (IBinder) getService.invoke(null, Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            Log.d(TAG, "success to getService: " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "initService ClassNotFoundException: ", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "initService NoSuchMethodException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "initService IllegalAccessException: ", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "initService InvocationTargetException: ", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "initService IllegalArgumentException: ", e);
        }

        if (binder == null) {
            Log.e(TAG, "failed to getService: " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        mIGoodixFingerprintInterface = IGoodixFingerprintInterface.Stub.asInterface(binder);


    }

    public void registerTestCmdCallback(TestCmdCallback callback) {
        Log.d(TAG, "registerTestCmdCallback = " + callback);
        mTestCallback = callback;
    }

    public void unregisterTestCmdCallback(TestCmdCallback callback) {
        Log.d(TAG, "unregisterTestCmdCallback = " + callback);
        mTestCallback = null;
    }

    public void testCmd(int cmdId) {
        testCmd(cmdId, null);
    }

    public void testCmd(int cmdId, byte[] param) {
        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "testCmd no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.testCmd(mGoodixFingerprintCallback.asBinder(), cmdId, param, 
                    getMyUserId(), mGoodixFingerprintCallback, mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public int testSync(int cmdId, byte[] param) {
        Log.d(TAG, "testSync");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "testSync no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return 0;
        }

        try {
            return mIGoodixFingerprintInterface.testSync(mGoodixFingerprintCallback.asBinder(), cmdId, param, 
                    getMyUserId(), mGoodixFingerprintCallback, mOpPackageName);
        } catch (RemoteException e) {
        }
        return 0;
    }

    public void setSafeClass(int safeClass) {
        Log.d(TAG, "setSafeClass");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "setSafeClass no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.setSafeClass(safeClass, mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void navigate(int navMode) {
        Log.d(TAG, "navigate");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "navigate no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.navigate(navMode, mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void stopNavigation() {
        Log.d(TAG, "stopNavigation");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "stopNavigation no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.stopNavigation(mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void enableFingerprintModule(boolean enable) {
        Log.d(TAG, "enableFingerprintModule");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "enableFingerprintModule no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.enableFingerprintModule(enable, mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void cameraCapture() {
        Log.d(TAG, "cameraCapture");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "cameraCapture no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.cameraCapture(mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void stopCameraCapture() {
        Log.d(TAG, "stopCameraCapture");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "stopCameraCapture no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.stopCameraCapture(mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void enableFfFeature(boolean enable) {
        Log.d(TAG, "enableFfFeature");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "enableFfFeature no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.enableFfFeature(enable, mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void screenOn() {
        Log.d(TAG, "screenOn");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "screenOn no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.screenOn(mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public void screenOff() {
        Log.d(TAG, "screenOff");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "screenOff no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.screenOff(mOpPackageName);
        } catch (RemoteException e) {
        }
    }

    public GFConfig getConfig() {
        Log.d(TAG, "getConfig");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "getConfig null interface " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return null;
        }

        try {
            return mIGoodixFingerprintInterface.getConfig(mOpPackageName);
        } catch (RemoteException e) {
        }

        return null;
    }

    public GFDevice getDevice() {
        Log.d(TAG, "getDevice");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "getDevice null interface " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return null;
        }

        try {
            return mIGoodixFingerprintInterface.getDevice(mOpPackageName);
        } catch (RemoteException e) {
        }

        return null;
    }

    public void dump(CancellationSignal cancel, DumpCallback callback) {
        Log.d(TAG, "dump");

        if (callback == null) {
            throw new IllegalArgumentException("Must supply a dump callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "dump already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnDumpCancelListener());
            }
        }

        if (mIGoodixFingerprintInterface != null) try {
            mDumpCallback = callback;
            mIGoodixFingerprintInterface.dump(mGoodixFingerprintDumpCallback, mOpPackageName);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in dump: ", e);
        }
    }

    private void cancelDump() {
        Log.d(TAG, "cancelDump");

        if (mIGoodixFingerprintInterface != null) try {
            mIGoodixFingerprintInterface.cancelDump(mOpPackageName);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception while canceling dump");
        }
    }

    public void dumpCmd(int cmdId, byte[] param) {
        Log.d(TAG, "dumpCmd " + cmdId);

        if (mIGoodixFingerprintInterface != null) try {
            mIGoodixFingerprintInterface.dumpCmd(cmdId, param, mOpPackageName);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in dumpCmd: ", e);
        }
    }

    private int getMyUserId() {
        try {
            Class<?> userHandle = Class.forName("android.os.UserHandle");
            Method myUserId = userHandle.getMethod("myUserId");
            return (Integer) myUserId.invoke(null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getMyUserId ClassNotFoundException: ", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getMyUserId NoSuchMethodException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getMyUserId IllegalAccessException: ", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getMyUserId InvocationTargetException: ", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getMyUserId IllegalArgumentException: ", e);
        }
        return 0;
    }

    public void untrustedAuthenticate(CancellationSignal cancel, UntrustedAuthenticationCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an untrusted authentication callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "untrusted authentication already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnUntrustedAuthenticationCancelListener());
            }
        }

        if (mIGoodixFingerprintInterface != null) {
            try {
                mUntrustedAuthenticationCallback = callback;
                mIGoodixFingerprintInterface.untrustedAuthenticate(mGoodixFingerprintCallback.asBinder(), getMyUserId(),
                        mGoodixFingerprintCallback,
                        getAppOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while untrusted authenticating: ", e);
            }
        }
    }

    public void untrustedAuthenticate2(String userDesc,CancellationSignal cancel, UntrustedAuthenticationCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an untrusted authentication callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "untrusted authentication already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnUntrustedAuthenticationCancelListener());
            }
        }

        if (mIGoodixFingerprintInterface != null) {
            try {
                mUntrustedAuthenticationCallback = callback;
                mIGoodixFingerprintInterface.untrustedAuthenticate2(userDesc,mGoodixFingerprintCallback.asBinder(), getMyUserId(),
                        mGoodixFingerprintCallback,
                        getAppOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while untrusted authenticating: ", e);
            }
        }
    }

    public void cancelUntrustedAuthentication() {
        if (mIGoodixFingerprintInterface != null)
            try {
                mIGoodixFingerprintInterface.cancelUntrustedAuthentication(mGoodixFingerprintCallback.asBinder(),
                        getAppOpPackageName());
                mUntrustedAuthenticationCallback = null;
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while canceling enrollment");
            }
    }

    public void untrustedEnroll(CancellationSignal cancel, UntrustedEnrollmentCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an untrusted enrollment callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "untrusted enrollment already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnUntrustedEnrollCancelListener());
            }
        }

        if (mIGoodixFingerprintInterface != null) {
            try {
                mUntrustedEnrollmentCallback = callback;
                mIGoodixFingerprintInterface.untrustedEnroll(mGoodixFingerprintCallback.asBinder(), getMyUserId(),
                        mGoodixFingerprintCallback,
                        getAppOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
            }
        }
    }

    public void untrustedEnroll2(String userDesc,CancellationSignal cancel, UntrustedEnrollmentCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an untrusted enrollment callback");
        }

        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "untrusted enrollment already canceled");
                return;
            } else {
                cancel.setOnCancelListener(new OnUntrustedEnrollCancelListener());
            }
        }

        if (mIGoodixFingerprintInterface != null) {
            try {
                mUntrustedEnrollmentCallback = callback;
                mIGoodixFingerprintInterface.untrustedEnroll2(userDesc,mGoodixFingerprintCallback.asBinder(), getMyUserId(),
                        mGoodixFingerprintCallback,
                        getAppOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
            }
        }
    }

    private void cancelUntrustedEnrollment() {
        if (mIGoodixFingerprintInterface != null)
            try {
                mIGoodixFingerprintInterface.cancelUntrustedEnrollment(mGoodixFingerprintCallback.asBinder(),
                        getAppOpPackageName());
                mUntrustedEnrollmentCallback = null;
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while canceling enrollment");
            }
    }



    public void untrustedRemove(int cmdId,byte[] param,UntrustedRemovalCallback callback){
        if (mIGoodixFingerprintInterface != null){
            mUntrustedRemovalCallback = callback;
            testCmd(cmdId,param);
        }
    }

    public void untrustedRemove(UntrustedRemovalCallback callback) {
        if (mIGoodixFingerprintInterface != null)
            try {
                mUntrustedRemovalCallback = callback;
                mIGoodixFingerprintInterface.untrustedRemove(mGoodixFingerprintCallback.asBinder(), getMyUserId(),
                        mGoodixFingerprintCallback,
                        getAppOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
            }
    }

    public boolean hasEnrolledUntrustedFingerprint() {
        if (mIGoodixFingerprintInterface != null) try {
            return mIGoodixFingerprintInterface.hasEnrolledUntrustedFingerprint(getAppOpPackageName());
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in hasEnrolledUntrustedFingerprint: ", e);
        }
        return false;
    }

    public int getEnrolledUntrustedFingerprint() {
        if (mIGoodixFingerprintInterface != null) try {
            return mIGoodixFingerprintInterface.getEnrolledUntrustedFingerprint(getAppOpPackageName());
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in getEnrolledUntrustedFingerprint: ", e);
        }
        return 0;
    }

    public GFBaikalConfig getBaikalConfig() {
        Log.d(TAG, "getBaikalConfig");

        if (mIGoodixFingerprintInterface != null) try {
            return mIGoodixFingerprintInterface.getBaikalConfig();
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in getBaikalConfig: ", e);
        }
        return null;
    }

    public void setBaikalConfig(GFBaikalConfig config) {
        Log.d(TAG, "setBaikalConfig");

        if (mIGoodixFingerprintInterface != null) try {
            mIGoodixFingerprintInterface.setBaikalConfig(config);
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in setBaikalConfig: ", e);
        }
    }

    public GFShenzhenConfig getShenzhenConfig() {
        Log.d(TAG, "getShenzhenConfig");

        if (mIGoodixFingerprintInterface != null) try {
            return mIGoodixFingerprintInterface.getShenzhenConfig();
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in getShenzhenConfig: ", e);
        }
        return null;
    }

    public void setShenzhenConfig(GFShenzhenConfig config) {
        Log.d(TAG, "setShenzhenConfig");

        if (mIGoodixFingerprintInterface != null) try {
            mIGoodixFingerprintInterface.setShenzhenConfig(config);
        } catch (RemoteException e) {
            Log.v(TAG, "Remote exception in setShenzhenConfig: ", e);
        }
    }

    private String getErrorString(int errMsg) {
        switch (errMsg) {
            case FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                return mContext.getString(R.string.fingerprint_error_unable_to_process);
            case FINGERPRINT_ERROR_HW_UNAVAILABLE:
                return mContext.getString(R.string.fingerprint_error_hw_not_available);
            case FINGERPRINT_ERROR_NO_SPACE:
                return mContext.getString(R.string.fingerprint_error_no_space);
            case FINGERPRINT_ERROR_TIMEOUT:
                return mContext.getString(R.string.fingerprint_error_timeout);
            case FINGERPRINT_ERROR_CANCELED:
                return mContext.getString(R.string.fingerprint_error_canceled);
            case FINGERPRINT_ERROR_LOCKOUT:
                return mContext.getString(R.string.fingerprint_error_lockout);
            default:
                if (errMsg >= FINGERPRINT_ERROR_VENDOR_BASE) {
                    int msgNumber = errMsg - FINGERPRINT_ERROR_VENDOR_BASE;
                    String[] msgArray = mContext.getResources().getStringArray(
                            R.array.fingerprint_error_vendor);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private String getAcquiredString(int acquireInfo) {
        switch (acquireInfo) {
            case FINGERPRINT_ACQUIRED_GOOD:
                return null;
            case FINGERPRINT_ACQUIRED_PARTIAL:
                return mContext.getString(R.string.fingerprint_acquired_partial);
            case FINGERPRINT_ACQUIRED_INSUFFICIENT:
                return mContext.getString(R.string.fingerprint_acquired_insufficient);
            case FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                return mContext.getString(R.string.fingerprint_acquired_imager_dirty);
            case FINGERPRINT_ACQUIRED_TOO_SLOW:
                return mContext.getString(R.string.fingerprint_acquired_too_slow);
            case FINGERPRINT_ACQUIRED_TOO_FAST:
                return mContext.getString(R.string.fingerprint_acquired_too_fast);
            default:
                if (acquireInfo >= FINGERPRINT_ACQUIRED_VENDOR_BASE) {
                    int msgNumber = acquireInfo - FINGERPRINT_ACQUIRED_VENDOR_BASE;
                    String[] msgArray = mContext.getResources().getStringArray(
                            R.array.fingerprint_acquired_vendor);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ENROLL_RESULT:
                    if (mUntrustedEnrollmentCallback != null) {
                        mUntrustedEnrollmentCallback.onEnrollmentProgress(msg.arg1, msg.arg2);
                    } else {
                        Log.d(TAG, "MSG_ENROLL_RESULT no callback");
                    }
                    break;
                case MSG_ACQUIRED:
                    if (mUntrustedEnrollmentCallback != null) {
                        mUntrustedEnrollmentCallback.onEnrollmentAcquired(msg.arg1);
                        mUntrustedEnrollmentCallback.onEnrollmentHelp(msg.arg1, getAcquiredString(msg.arg1));
                    } else if (mUntrustedAuthenticationCallback != null) {
                        mUntrustedAuthenticationCallback.onAuthenticationAcquired(msg.arg1);
                        mUntrustedAuthenticationCallback.onAuthenticationHelp(msg.arg1, getAcquiredString(msg.arg1));
                    } else {
                        Log.d(TAG, "MSG_ACQUIRED no callback");
                    }
                    break;
                case MSG_AUTHENTICATION_SUCCEEDED:
                    if (mUntrustedAuthenticationCallback != null) {
                        mUntrustedAuthenticationCallback.onAuthenticationSucceeded(msg.arg1);
                    } else {
                        Log.d(TAG, "MSG_AUTHENTICATION_SUCCEEDED no callback");
                    }
                    break;
                case MSG_AUTHENTICATION_FAILED:
                    if (mUntrustedAuthenticationCallback != null) {
                        mUntrustedAuthenticationCallback.onAuthenticationFailed();
                    } else {
                        Log.d(TAG, "MSG_AUTHENTICATION_FAILED no callback");
                    }
                    break;
                case MSG_ERROR:
                    if (mUntrustedEnrollmentCallback != null) {
                        mUntrustedEnrollmentCallback.onEnrollmentError(msg.arg1, getErrorString(msg.arg1));
                    } else if (mUntrustedAuthenticationCallback != null) {
                        mUntrustedAuthenticationCallback.onAuthenticationError(msg.arg1, getErrorString(msg.arg1));
                    } else if (mUntrustedRemovalCallback != null) {
                        mUntrustedRemovalCallback.onRemovalError(msg.arg1, getErrorString(msg.arg1));
                    } else {
                        Log.d(TAG, "MSG_ERROR no callback");
                    }
                    break;
                case MSG_REMOVED:
                    if (mUntrustedRemovalCallback != null) {
                        mUntrustedRemovalCallback.onRemovalSucceeded(msg.arg1);
                    } else {
                        Log.d(TAG, "MSG_REMOVED no callback");
                    }
                    break;
                case MSG_TEST:
                    Log.d(TAG, "MSG_TEST mTestCallback = " + mTestCallback);
                    if (mTestCallback != null) {
                        HashMap<Integer, Object> testResult = ShenzhenTestResultParser.parse((byte[]) msg.obj);
                        mTestCallback.onTestCmd(msg.arg1, testResult);
                    }
                    break;
                case MSG_DUMP:
                    if (mDumpCallback != null) {
                        HashMap<Integer, Object> dumpData = TestResultParser.parse(msg.arg2, (byte[]) msg.obj);
                        mDumpCallback.onDump(msg.arg1, dumpData);
                    }
                    break;
                case MSG_AUTHENTICATED_FIDO:
                    if (null != mAuthenticateFidoCallback) {
                        int fingerId = msg.arg1;
                        byte[] uvtData = (byte[]) msg.obj;
                        int result = 0;
                        if ((fingerId != 0) && (uvtData != null) && (uvtData.length > 0)) {
                            result = 100;// success, accord to fido
                        } else if (fingerId == 0) {
                            result = 103;// mismatch, accord to fido
                        } else if (fingerId != 0 && (uvtData == null || uvtData.length == 0)) {
                            result = 102;// error, accord to fido
                        } else {
                            result = 113;
                        }
                        mAuthenticateFidoCallback.onResult(result, uvtData, int2bytes(fingerId));
                    } else {
                        Log.e(TAG, "handleMessage, mAuthenticateFidoCallback is null");
                    }
                    break;
                case MSG_ENROLL_IMAGE_RESULT:
                    if (mUntrustedEnrollmentCallback != null) {
                        Log.e(TAG, "handleMessage, MSG_ENROLL_IMAGE_RESULT");
                        mUntrustedEnrollmentCallback.onEnrollImage((byte[]) msg.obj,(int)msg.arg1, (int)msg.arg2);
                    }

                    break;
                case MSG_AUTHENTICATE_IMAGE_RESULT:
                    if (mUntrustedAuthenticationCallback != null) {
                        Log.e(TAG, "handleMessage, MSG_AUTHENTICATE_IMAGE_RESULT");
                        mUntrustedAuthenticationCallback.onAuthenticateImage((byte[]) msg.obj,(int)msg.arg1, (int)msg.arg2);
                    }

                    break;

            }
        }
    }

    private IGoodixFingerprintCallback.Stub mGoodixFingerprintCallback = new IGoodixFingerprintCallback.Stub() {

        @Override //binder call
        public void onImageResult(int msgType,byte [] imageData,int width,int height){
            Log.d(TAG, "onImageResult, width:" + width + " height:" + height);
            Message msg = null;
            switch (msgType){
                case Constants.GF_FINGERPRINT_TEMPLATE_ENROLLING:
                    msg = mHandler.obtainMessage(MSG_ENROLL_IMAGE_RESULT);
                    break;
                case Constants.GF_FINGERPRINT_AUTHENTICATED:
                    msg = mHandler.obtainMessage(MSG_AUTHENTICATE_IMAGE_RESULT);
                    break;
            }
            if(msg != null){
                msg.arg1 = width;
                msg.arg2 = height;
                msg.obj = imageData;
                msg.sendToTarget();
            }


        }

        @Override // binder call
        public void onEnrollResult(int fingerId, int remaining) {
            Log.d(TAG, "onEnrollResult fingerId = " + fingerId + " remaining = " + remaining);
            mHandler.obtainMessage(MSG_ENROLL_RESULT, fingerId, remaining).sendToTarget();
        }

        @Override // binder call
        public void onAcquired(int acquireInfo) {
            mHandler.obtainMessage(MSG_ACQUIRED, acquireInfo, 0).sendToTarget();
        }

        @Override // binder call
        public void onAuthenticationSucceeded(int fingerId) {
            mHandler.obtainMessage(MSG_AUTHENTICATION_SUCCEEDED, fingerId, 0).sendToTarget();
        }

        @Override // binder call
        public void onAuthenticationFailed() {
            mHandler.obtainMessage(MSG_AUTHENTICATION_FAILED).sendToTarget();
        }

        @Override // binder call
        public void onError(int error) {
            mHandler.obtainMessage(MSG_ERROR, error, 0).sendToTarget();
        }

        @Override // binder call
        public void onRemoved(int fingerId) {
            mHandler.obtainMessage(MSG_REMOVED, fingerId, 0).sendToTarget();
        }

        @Override
        public void onTestCmd(final int chipSeries, final int cmdId, final byte[] result) {
            Log.d(TAG, "onTestCmd " + Constants.testCmdIdToString(chipSeries, cmdId));

            mHandler.obtainMessage(MSG_TEST, cmdId, chipSeries, result).sendToTarget();
        }

        @Override
        public void onAuthenticateFido(int fingerId, byte[] uvt) {
            Log.d(TAG, "onAuthenticateFido, fingerId:" + fingerId);
            Message msg = mHandler.obtainMessage(MSG_AUTHENTICATED_FIDO);
            msg.arg1 = fingerId;
            msg.obj = uvt;
            msg.sendToTarget();
        }
    };

    private IGoodixFingerprintDumpCallback.Stub mGoodixFingerprintDumpCallback = new IGoodixFingerprintDumpCallback.Stub() {
        @Override
        public void onDump(final int chipSeies, final int cmdId, final byte[] data) {
            Log.d(TAG, "onDump");

            mHandler.obtainMessage(MSG_DUMP, cmdId, chipSeies, data).sendToTarget();
        }
    };

    private class OnDumpCancelListener implements OnCancelListener {
        @Override
        public void onCancel() {
            cancelDump();
        }
    }

    public static byte[] int2bytes(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);
        targets[1] = (byte) ((res >> 8) & 0xff);
        targets[2] = (byte) ((res >> 16) & 0xff);
        targets[3] = (byte) (res >>> 24);
        return targets;
    }

    public static int bytes2int(byte[] res) {
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00)
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public void openOrCloseHBMMode(int mode) {
        Log.d(TAG, "openOrCloseHBMMode");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "openOrCloseHBMMode no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.openOrCloseHBMMode(mode);
        } catch (RemoteException e) {
        }
    }

	public void setHBMMode(boolean open) {
		 Log.e(TAG, "setHBMMode open = " + open);
	
		/* if (mIGoodixFingerprintInterface == null) {
			 Log.e(TAG, "setHBMMode no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
			 return;
		 }
	
		 try {
			 mIGoodixFingerprintInterface.setHBMMode(open);
		 } catch (RemoteException e) {
		 }*/
		 if(open){
			 setHBMByNode("1");
			 //testCmd(ShenzhenConstants.CMD_TEST_SZ_SET_HBM_MODE);
		 } else {
			 setHBMByNode("0");
			 //testCmd(ShenzhenConstants.CMD_TEST_SZ_CLOSE_HBM_MODE);
		 }
	 }
	
	 //private final String HBM_NODE = "/sys/class/leds/lcd-backlight/hbm_mode";
	 private final String HBM_NODE = "/proc/leds/hbm_mode";
	 public void setHBMByNode(String value){
		 BufferedWriter bufWriter = null;
		 try {
			 bufWriter = new BufferedWriter(new FileWriter(HBM_NODE));
			 bufWriter.write(value);
			 //bufWriter.close();
			 Log.e(TAG, "setHBMByNodeF value = " + value);
		 } catch (IOException e) {
			 Log.d(TAG, "setHBMByNode exception !");
			 e.printStackTrace();
		 } finally {
			 try {
				 if (bufWriter != null) {
					 bufWriter.flush();
					 bufWriter.close();
				 }
				 Log.d(TAG, "setHBMByNode bufWriter.close ");
			 } catch (IOException ex) {
				 ex.printStackTrace();
			 }
		 }
	 }

    public void setScreenManualMode() {
        Log.d(TAG, "setScreenManualMode");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "setScreenManualMode no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.setScreenManualMode();
        } catch (RemoteException e) {
        }
    }

    public void setScreenBrightness(int value) {
        Log.d(TAG, "setScreenBrightness");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "setScreenBrightness no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.setScreenBrightness(value);
        } catch (RemoteException e) {
        }
    }

    public void setScreenBrightnessR15(int value) {
        Log.d(TAG, "setScreenBrightnessR15");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "setScreenBrightnessR15 no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.setScreenBrightnessR15(value);
        } catch (RemoteException e) {
        }
    }

    public int getScreenBrightness() {
        int def = 255;
        Log.d(TAG, "getScreenBrightness");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "getScreenBrightness no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return def;
        }

        try {
            def = mIGoodixFingerprintInterface.getScreenBrightness();
        } catch (RemoteException e) {
            e.printStackTrace();
        }finally {
            return def;
        }
    }

    public void goToSleep() {
        Log.d(TAG, "goToSleep");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "goToSleep no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.goToSleep();
        } catch (RemoteException e) {
        }
    }

    public void wakeUp() {
        Log.d(TAG, "wakeUp");

        if (mIGoodixFingerprintInterface == null) {
            Log.e(TAG, "wakeUp no " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return;
        }

        try {
            mIGoodixFingerprintInterface.wakeUp();
        } catch (RemoteException e) {
        }
    }
}
