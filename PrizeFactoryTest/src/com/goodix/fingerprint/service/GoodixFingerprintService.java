/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.BaikalConstants;
import com.goodix.fingerprint.ShenzhenConstants;
import com.goodix.fingerprint.CmdResult;
import com.goodix.fingerprint.GFBaikalConfig;
import com.goodix.fingerprint.GFShenzhenConfig;
import com.goodix.fingerprint.GFConfig;
import com.goodix.fingerprint.GFDevice;
import com.goodix.fingerprint.proxy.DaemonManager;
import com.goodix.fingerprint.proxy.IGFDaemon;
import com.goodix.fingerprint.proxy.IGFDaemonCallback;
import com.goodix.fingerprint.proxy.IGFDaemonFido;
import com.goodix.fingerprint.utils.TestResultParser;
import com.goodix.fingerprint.utils.BaikalTestResultParser;
import com.goodix.fingerprint.utils.ShenzhenTestResultParser;
import com.goodix.fingerprint.utils.TestParamEncoder;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class GoodixFingerprintService extends IGoodixFingerprintInterface.Stub {
    private static final String TAG = "GoodixFingerprintService";
    public static final boolean DEBUG = true;

    public static final String MANAGE_FINGERPRINT = "android.permission.MANAGE_FINGERPRINT";
    public static final String USE_FINGERPRINT = "android.permission.USE_FINGERPRINT";

    private static final int TEST_GET_CONFIG_MAX_RETRY_TIMES = 20;
    // max wait for onCancel from HAL,in ms
    // current there is no onCancel notify from HAL, so just wait 300ms to make sure cancel operation done
    private static final long CANCEL_TIMEOUT_LIMIT = 300;

    private ClientMonitor mCurrentClient = null;
    ContentResolver mContentResolver = null;
    PowerManager pm;
    private Context mContext = null;
    private Handler mHandler = null;
    private DaemonManager mDaemonMgr = null;
    private DaemonMessageHandlerBase mDaemonMessageHandler = null;
    private IGoodixFingerprintDumpCallback mDumpCallback = null;
    private IGoodixFingerprintSensorCallback mSensorCallback = null;
    private IGoodixFingerprintPreviewCallback mPreviewCallback = null;
    private GFConfig mConfig = new GFConfig();
    private GFDevice mDevice = new GFDevice();
    private GFBaikalConfig mBaikalConfig = new GFBaikalConfig();
    private GFShenzhenConfig mShenzhenConfig = new GFShenzhenConfig();

    private AppOpsManager mAppOps = null;
    private String mOpPackageName = null;
    private int mOpUseFingerprint = -1;
    private int mUntrustedFingerprintId = 0;
    int mGetConfigRetryCount = 0;
    int mUserId = 0;

    private HashMap<String, IGoodixFingerprintCallback> mGoodixFingerprintCallbackForUntrustedEnrollOrAuth
                                                           = new HashMap<String, IGoodixFingerprintCallback>();

    public GoodixFingerprintService(Context context) {
        mContext = context;
        mHandler = new Handler(context.getMainLooper());

        mAppOps = (AppOpsManager) (context.getSystemService(Context.APP_OPS_SERVICE));
        mUserId = getMyUserId();
        mOpPackageName = getAppOpPackageName();
        mOpUseFingerprint = getOpUseFingerprint();
        mDaemonMgr = DaemonManager.getInstance();

        mDaemonMessageHandler = new ShenzhenDaemonMessageHandler();

        mContentResolver = mContext.getContentResolver();
        pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        startGetConfigTask();
        startGetShenzhenConfigTask();
    }

    private void startGetConfigTask() {
        Thread t = new Thread(new GetConfigCmdTask());
        t.start();
    }

    private void startGetBaikalConfigTask() {
        Thread t = new Thread(new GetBaikalConfigTask());
        t.start();
    }

    private void startGetShenzhenConfigTask() {
        Thread t = new Thread(new GetShenzhenConfigTask());
        t.start();
    }

    private void addUntrustedCallback(final String opPackageName, IGoodixFingerprintCallback callback){
        int i = 0;
        boolean isExist = false;
        IGoodixFingerprintCallback temp;
        int size = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.size();

        if(callback == null || opPackageName == null){
            return;
        }

        if(mGoodixFingerprintCallbackForUntrustedEnrollOrAuth != null
                && mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.size() == 0){
            mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.put(opPackageName, callback);
            return;
        }

        Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
        while (iterator != null && iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String)entry.getKey();
            if(key.equals(opPackageName)){
                isExist = true;
                break;
            }
        }

        if(!isExist){
            mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.put(opPackageName, callback);
        } else {
            mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.remove(opPackageName);
            mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.put(opPackageName, callback);
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

    private String getAppOpPackageName() {
        String opPackageName = null;

        if (mContext == null) {
            return null;
        }

        try {
            Method getOpPackageName = mContext.getClass().getMethod("getOpPackageName");
            getOpPackageName.setAccessible(true);
            opPackageName = (String) getOpPackageName.invoke(mContext);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getAppOpPackageName NoSuchMethodException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getAppOpPackageName IllegalAccessException: ", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getAppOpPackageName IllegalArgumentException: ", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getAppOpPackageName InvocationTargetException: ", e);
        }

        return opPackageName;
    }

    private int getOpUseFingerprint() {
        try {
            Class c = AppOpsManager.class;
            Field f = c.getDeclaredField("OP_USE_FINGERPRINT");
            return (f.getInt(null));
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getOpUseFingerprint NoSuchFieldException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getOpUseFingerprint IllegalAccessException: ", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getOpUseFingerprint IllegalArgumentException: ", e);
        }
        return -1;
    }

    private boolean isRestricted() {
        // Only give privileged apps (like Settings) access to fingerprint info
        //final boolean restricted = !hasPermission(MANAGE_FINGERPRINT);
        //don't check MANAGE_FINGERPRINT permission for gf_test
        return true;
    }

    void checkPermission(String permission) {
        mContext.enforceCallingOrSelfPermission(permission,
                "Must have " + permission + " permission.");
    }

    private boolean canUseFingerprint(String opPackageName) {
        checkPermission(USE_FINGERPRINT);

        try {
            Method noteOp = mAppOps.getClass().getMethod("noteOp", new Class[] {
                    int.class, int.class, String.class
            });

            noteOp.setAccessible(true);
            Integer mode = (Integer) (noteOp.invoke(mAppOps, mOpUseFingerprint,
                    Binder.getCallingUid(), opPackageName));
            return mode.intValue() == AppOpsManager.MODE_ALLOWED;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "canUseFingerprint NoSuchMethodException: ", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "canUseFingerprint InvocationTargetException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "canUseFingerprint IllegalAccessException: ", e);
        }
        return false;
    }

    /**
     * Calls fingerprintd to switch states to the new task. If there's already a current task,
     * it calls cancel() and sets mPendingClient to begin when the current task finishes
     * ({@link FingerprintManager#FINGERPRINT_ERROR_CANCELED}).
     * @param newClient the new client that wants to connect
     * @param initiatedByClient true for authenticate, remove and enroll
     */
    private int startClient(ClientMonitor newClient) {
        ClientMonitor currentClient = mCurrentClient;
        if (currentClient != null) {
            if (DEBUG) Log.d(TAG, "request stop current client " + currentClient.getOwner());
            //this stopClient is interaction stop, not cancel by User
            stopClient(currentClient, false);
        }
        if (newClient != null) {
            mCurrentClient = newClient;
            if (DEBUG) Log.d(TAG, "starting client "
                    + newClient.getClass().getSuperclass().getSimpleName()
                    + "(" + newClient.getOwner() + ")");
            return newClient.start();
        }
        return 0; // no client to handle it, finish
    }

    private void stopClient(ClientMonitor client, boolean cancelByUser) {
        if (client != null) {
            // no onCancel callback from HAL for cancel operation of testcmd, hbd, authenticateFido, dump
            // remove client directly
            if (cancelByUser) {
                if (DEBUG) Log.d(TAG, "request remove client with stop" + client.getOwner());
            } else {
                if (DEBUG) Log.d(TAG, "request remove client without stop " + client.getOwner());
            }

            client.stop(cancelByUser);
            removeClient(client);
        }
    }

    private void removeClient(ClientMonitor client) {
        if (client != null) {
            client.destroy();
            if (client != mCurrentClient && mCurrentClient != null) {
                Log.w(TAG, "Unexpected client: " + client.getOwner() + "expected: "
                        + mCurrentClient != null ? mCurrentClient.getOwner() : "null");
            }
        }
        if (mCurrentClient != null) {
            if (DEBUG) Log.d(TAG, "Done with client: " + client.getOwner());
            mCurrentClient = null;
        }
    }

    protected void handleError(int error) {
        ClientMonitor client = mCurrentClient;
        try {
            int i = 0;
            IGoodixFingerprintCallback callback;
            Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                callback = (IGoodixFingerprintCallback)entry.getValue();
                callback.onError(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null && client.onError(error)) {
            removeClient(client);
        }
        if (DEBUG) Log.d(TAG, "handleError(client="
                + (client != null ? client.getOwner() : "null") + ", error = " + error + ")");
    }

    protected void handleRemoved(int fingerId) {
        ClientMonitor client = mCurrentClient;
        if (client != null) {
            mUntrustedFingerprintId = 0;
            if (client.onRemoved(fingerId)) {
                removeClient(client);
            }
        }
    }

    protected void handleTestCmd(int cmdId, byte[] result) {
        Log.d(TAG,"handleTestCmd cmdId = " + cmdId);
        ClientMonitor client = mCurrentClient;
        if (client != null) {
            if (client.onTestCmdFinish(mConfig.mChipSeries, cmdId, result)) {
                removeClient(client);
            }
        }
    }

    protected void handleAuthenticated(int fingerId) {
        ClientMonitor client = mCurrentClient;
        try {
            int i = 0;
            IGoodixFingerprintCallback callback;
            Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                callback = (IGoodixFingerprintCallback)entry.getValue();
                if(fingerId != 0){
                    callback.onAuthenticationSucceeded(fingerId);
                } else {
                    callback.onAuthenticationFailed();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (client != null && client.onAuthenticated(fingerId)) {
            removeClient(client);
        }*/
    }

    protected void handleAcquired(int acquiredInfo) {
        ClientMonitor client = mCurrentClient;
        Log.d(TAG, "handleAcquired client = " + client);

        try {
            int i = 0;
            IGoodixFingerprintCallback callback;
            Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                callback = (IGoodixFingerprintCallback)entry.getValue();
                Log.d(TAG, "handleAcquired callback = " + callback);

                callback.onAcquired(acquiredInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client != null && client.onAcquired(acquiredInfo)) {
            Log.d(TAG, "handleAcquired removeClient = " + client);
            removeClient(client);
        }
    }

    protected void handleImageResult(int msgType ,byte [] bmpData,int width,int height){
        ClientMonitor client = mCurrentClient;
        Log.d(TAG, "handleImageResult client = " + client);

        try {
            int i = 0;
            IGoodixFingerprintCallback callback;
            Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                callback = (IGoodixFingerprintCallback)entry.getValue();
                callback.onImageResult(msgType,bmpData,width,height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void handleEnrollResult(int fingerId, int remaining) {
        ClientMonitor client = mCurrentClient;

        if (client != null) {
            mUntrustedFingerprintId = fingerId;
            try {
                int i = 0;
                IGoodixFingerprintCallback callback;
                Iterator iterator = mGoodixFingerprintCallbackForUntrustedEnrollOrAuth.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    callback = (IGoodixFingerprintCallback)entry.getValue();
                    callback.onEnrollResult(fingerId, remaining);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*if (client.onEnrollResult(fingerId, remaining)) {
                if (remaining == 0) {
                    removeClient(client);
                }
            }*/
        }
    }

    protected void handleAuthenticatedFido(int fingerId, byte[] uvtData) {
        ClientMonitor client = mCurrentClient;
        if (client != null && client.onAuthenticateFido(fingerId, uvtData)) {
            removeClient(client);
        }
    }

    protected void handleSensorDisplayControl(int cmdId, byte[] sensorData) {
        Log.d(TAG, "handleSensorDisplayControl");
        try {
            if(mSensorCallback != null) {
                mSensorCallback.onSensorControlCmd(cmdId, sensorData);
            } else {
                Log.d(TAG, "handleSensorDisplayControl, mSensorCallback is null");
            }
        } catch (RemoteException e) {
        }
    }

    protected void handlePreviewDisplayControl(int cmdId, byte[] previewData) {
        Log.d(TAG, "handlePreviewDisplayControl");
        try {
            if(mPreviewCallback != null) {
                mPreviewCallback.onPreviewControlCmd(cmdId, previewData);
            } else {
                Log.d(TAG, "handlePreviewDisplayControl, mPreviewCallback is null");
            }
        } catch (RemoteException e) {
        }
    }

    private IGFDaemon getFingerprintDaemon() {
        Log.d(TAG, "getFingerprintDaemon()");
        return mDaemonMgr.getGoodixFingerprintDaemon(mDaemonCallback);
    }

    private IGFDaemonFido getFingerprintDaemonFido() {
        Log.d(TAG, "getFingerprintDaemonFido()");
        return mDaemonMgr.getGoodixFingerprintDaemonFido(null);
    }

    private class GetConfigCmdTask implements Runnable {
        @Override
        public void run() {
            int result = -1;
            while (true) {
                result = startTestCmd(mContext, null, null, mUserId, isRestricted(), mOpPackageName, Constants.CMD_TEST_GET_CONFIG, null, false);
                mGetConfigRetryCount++;
                Log.i(TAG, "CMD_TEST_GET_CONFIG retry count : " + mGetConfigRetryCount);
                if (result != 0 && mGetConfigRetryCount < TEST_GET_CONFIG_MAX_RETRY_TIMES) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    int startTestCmd(final Context context, final IBinder token, final IGoodixFingerprintCallback receiver,
            final int userId, final boolean restricted, final String opPackageName,
            final int cmdId, final byte[] param, final boolean initiatedByClient) {

        TestCmdClient client = new TestCmdClient(mContext, token, receiver,
                userId, isRestricted(), opPackageName, cmdId, param, initiatedByClient) {
                    @Override
                    public IGFDaemon getGoodixFingerprintDaemon() {
                        return GoodixFingerprintService.this.getFingerprintDaemon();
                    }
        };
        return startClient(client);
    }

    private class GetBaikalConfigTask implements Runnable {
        @Override
        public void run() {
            loadBaikalConfig();
        }
    }

    private void loadBaikalConfig() {
        SharedPreferences pref = mContext.getSharedPreferences(Constants.PREFERENCE_SENSOR_INFO, Context.MODE_PRIVATE);
        mBaikalConfig.mSensorX = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_X,
                Constants.DEFAULT_SENSOR_X);
        mBaikalConfig.mSensorY = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_Y,
                Constants.DEFAULT_SENSOR_Y);
        mBaikalConfig.mSensorWidth = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_WIDTH,
                Constants.DEFAULT_SENSOR_WIDTH);
        mBaikalConfig.mSensorHeight = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_HEIGHT,
                Constants.DEFAULT_SENSOR_HEIGHT);
        mBaikalConfig.mSensorLockAspectRatio = pref.getBoolean(
                Constants.PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO,
                Constants.DEFAULT_LOCK_ASPECT_RATIO);
        mBaikalConfig.mSensorAspectRatioWidth = pref.getInt(
                Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH,
                Constants.DEFAULT_ASPECT_RATIO_WIDTH);
        mBaikalConfig.mSensorAspectRatioHeight = pref.getInt(
                Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT,
                Constants.DEFAULT_ASPECT_RATIO_HEIGHT);
        mBaikalConfig.mSensorPreviewScaleRatio = pref.getInt(
                Constants.PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO,
                Constants.DEFAULT_PREVIEW_SCALE_RATIO);
        mBaikalConfig.mSensorAreaBackgroundColor = pref.getInt(
                Constants.PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR,
                Constants.DEFAULT_SENSOR_AREA_CYAN_COLOR);
    }

    private void saveBaikalConfig() {
        SharedPreferences pref = mContext.getSharedPreferences(Constants.PREFERENCE_SENSOR_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_X, mBaikalConfig.mSensorX);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_Y, mBaikalConfig.mSensorY);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_WIDTH, mBaikalConfig.mSensorWidth);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_HEIGHT, mBaikalConfig.mSensorHeight);
        editor.putBoolean(Constants.PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO, mBaikalConfig.mSensorLockAspectRatio);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH, mBaikalConfig.mSensorAspectRatioWidth);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT, mBaikalConfig.mSensorAspectRatioHeight);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO, mBaikalConfig.mSensorPreviewScaleRatio);
        editor.putInt(Constants.PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR, mBaikalConfig.mSensorAreaBackgroundColor);
        editor.commit();
    }

    private void loadShenzhenConfig(){
        SharedPreferences pref = mContext.getSharedPreferences(ShenzhenConstants.PREFERENCE_SENSOR_CONFIG_INFO, Context.MODE_PRIVATE);
        mShenzhenConfig.mShortExposureTime = pref.getInt(ShenzhenConstants.PREFERENCE_SHORT_EXPOSURE_TIME, 0);
        mShenzhenConfig.mRect_bmp_col = pref.getInt(ShenzhenConstants.PREFERENCE_RECT_BMP_COL, 0);
        mShenzhenConfig.mRect_bmp_row = pref.getInt(ShenzhenConstants.PREFERENCE_RECT_BMP_ROW, 0);
        mShenzhenConfig.mSensorRow = pref.getInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_ROW, 0);
        mShenzhenConfig.mSensorCol = pref.getInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_COL, 0);
        mShenzhenConfig.mSensorX = pref.getInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_X, ShenzhenConstants.DEFAULT_SENSOR_X);
        mShenzhenConfig.mSensorY = pref.getInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_Y, ShenzhenConstants.DEFAULT_SENSOR_Y);
        mShenzhenConfig.mSensorWidth = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_WIDTH,
                ShenzhenConstants.DEFAULT_SENSOR_WIDTH);
        mShenzhenConfig.mSensorHeight = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_HEIGHT,
                ShenzhenConstants.DEFAULT_SENSOR_HEIGHT);
        mShenzhenConfig.mSensorLockAspectRatio = pref.getBoolean(Constants.PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO,
                ShenzhenConstants.DEFAULT_LOCK_ASPECT_RATIO);
        mShenzhenConfig.mSensorAspectRatioWidth = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH,
                ShenzhenConstants.DEFAULT_ASPECT_RATIO_WIDTH);
        mShenzhenConfig.mSensorAspectRatioHeight = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT,
                ShenzhenConstants.DEFAULT_ASPECT_RATIO_HEIGHT);
        mShenzhenConfig.mSensorPreviewScaleRatio = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO,
                ShenzhenConstants.DEFAULT_PREVIEW_SCALE_RATIO);
        mShenzhenConfig.mSensorAreaBackgroundColor = pref.getInt(Constants.PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR,
                ShenzhenConstants.DEFAULT_SENSOR_AREA_BLACK_COLOR);
        Log.e(TAG, "loadShenzhenConfig mSensorX : " + mShenzhenConfig.mSensorY);
    }
    
    private void saveShenzhenConfig() {
        Log.d(TAG, "saveShenzhenConfig");
        Log.e(TAG, "saveShenzhenConfig mSensorX : " + mShenzhenConfig.mSensorY);

        SharedPreferences pref = mContext.getSharedPreferences(ShenzhenConstants.PREFERENCE_SENSOR_CONFIG_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(ShenzhenConstants.PREFERENCE_SHORT_EXPOSURE_TIME, mShenzhenConfig.mShortExposureTime);
        editor.putInt(ShenzhenConstants.PREFERENCE_RECT_BMP_COL, mShenzhenConfig.mRect_bmp_col);
        editor.putInt(ShenzhenConstants.PREFERENCE_RECT_BMP_ROW, mShenzhenConfig.mRect_bmp_row);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_X, mShenzhenConfig.mSensorX);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_Y, mShenzhenConfig.mSensorY);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_ROW, mShenzhenConfig.mSensorRow);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_COL, mShenzhenConfig.mSensorCol);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_WIDTH, mShenzhenConfig.mSensorWidth);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_HEIGHT, mShenzhenConfig.mSensorHeight);
        editor.putBoolean(ShenzhenConstants.PREFERENCE_KEY_SENSOR_LOCK_ASPECT_RATIO, mShenzhenConfig.mSensorLockAspectRatio);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_WIDTH, mShenzhenConfig.mSensorAspectRatioWidth);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_ASPECT_RATIO_HEIGHT, mShenzhenConfig.mSensorAspectRatioHeight);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_PREVIEW_SCALE_RATIO, mShenzhenConfig.mSensorPreviewScaleRatio);
        editor.putInt(ShenzhenConstants.PREFERENCE_KEY_SENSOR_AREA_BACKGROUND_COLOR, mShenzhenConfig.mSensorAreaBackgroundColor);
        editor.commit();
    }

    private class GetShenzhenConfigTask implements Runnable {
        @Override
        public void run() {
            loadShenzhenConfig();
            int result = -1;
            while (true) {
                result = startTestCmd(mContext, null, null, mUserId, isRestricted(), mOpPackageName, ShenzhenConstants.CMD_TEST_SZ_GET_CONFIG, null, false);
                mGetConfigRetryCount++;
                Log.i(TAG, "CMD_TEST_SZ_GET_CONFIG retry count : " + mGetConfigRetryCount);
                if (result != 0 && mGetConfigRetryCount < TEST_GET_CONFIG_MAX_RETRY_TIMES) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void testCmd(final IBinder token, final int cmdId, final byte[] param, final int userId,
            final IGoodixFingerprintCallback receiver, final String opPackageName) {
        Log.d(TAG, "testCmd " + cmdId + ", chipSeries="+mConfig.mChipSeries+", "+ Constants.testCmdIdToString(mConfig.mChipSeries, cmdId));

        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return;
        }

        if (Constants.CMD_TEST_CANCEL == cmdId) {
            cancelTestCmd(token, opPackageName);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startTestCmd(mContext, token, receiver, userId, isRestricted(),
                            opPackageName, cmdId, param, true);
                }
            });
        }
    }

    @Override
    public void cancelTestCmd(final IBinder token, final String opPackageName) {
        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ClientMonitor client = mCurrentClient;
                if (client == null) {
                    if (DEBUG) Log.d(TAG, "current client is null, can't cancel");
                } else {
                    if (client instanceof TestCmdClient) {
                        if (client.getToken() == token) {
                            stopClient(client, true);
                        } else {
                            if (DEBUG) Log.d(TAG, "can't stop client "
                                    + client.getOwner() + " since tokens don't match");
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "can't cancel non-testcmd client "
                                + client.getOwner());
                    }
                }
            }
        });
    }

    @Override
    public int testSync(final IBinder token, final int cmdId, final byte[] param, final int userId,
            final IGoodixFingerprintCallback receiver, final String opPackageName) {
        Log.d(TAG, "testSync");
        if (!canUseFingerprint(opPackageName)) {
            return 0;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startTestCmd(mContext, token, receiver, userId, isRestricted(),
                        opPackageName, cmdId, param, true);
            }
        });

        return 0;
    }

    @Override
    public void setSafeClass(final int safeClass, String opPackageName) {
        Log.d(TAG, "setSafeClass");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "setSafeClass: service not found!");
                    return;
                }

                try {
                    daemon.setSafeClass(safeClass);
                    mDevice.mSafeClass = safeClass;
                } catch (RemoteException e) {
                    Log.e(TAG, "setSafeClass RemoteException");
                }*/
            }

        });
    }

    @Override
    public void navigate(final int navMode, String opPackageName) {
        Log.d(TAG, "navigate");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "navigate: service not found!");
                    return;
                }

                try {
                    daemon.navigate(navMode);
                } catch (RemoteException e) {
                    Log.e(TAG, "navigate RemoteException");
                }*/
            }
        });
    }

    @Override
    public void stopNavigation(String opPackageName) {
        Log.d(TAG, "stopNavigation");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "stopNavigation: service not found!");
                    return;
                }

                try {
                    daemon.stopNavigation();
                } catch (RemoteException e) {
                    Log.e(TAG, "stopNavigation RemoteException");
                }*/
            }

        });
    }

    @Override
    public void enableFingerprintModule(final boolean enable, String opPackageName) {
        Log.d(TAG, "enableFingerprintModule");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "enableFingerprintModule: service not found!");
                    return;
                }

                try {
                    daemon.enableFingerprintModule(enable ? (byte) 1 : (byte) 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "enableFingerprintModule RemoteException");
                }*/
            }

        });
    }

    @Override
    public void cameraCapture(String opPackageName) {
        Log.d(TAG, "cameraCapture");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "cameraCapture: service not found!");
                    return;
                }

                try {
                    daemon.cameraCapture();
                } catch (RemoteException e) {
                    Log.e(TAG, "cameraCapture RemoteException");
                }*/
            }
        });
    }

    @Override
    public void stopCameraCapture(String opPackageName) {
        Log.d(TAG, "stopCameraCapture");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "stopCameraCapture: service not found!");
                    return;
                }

                try {
                    daemon.stopCameraCapture();
                } catch (RemoteException e) {
                    Log.e(TAG, "stopCameraCapture RemoteException");
                }*/
            }

        });
    }

    @Override
    public void enableFfFeature(final boolean enable, String opPackageName) {
        Log.d(TAG, "enableFfFeature");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "enableFfFeature: service not found!");
                    return;
                }

                try {
                    daemon.enableFfFeature(enable ? (byte) 1 : (byte) 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "enableFfFeature RemoteException");
                }*/
            }

        });
    }

    @Override
    public void screenOn(String opPackageName) {
        Log.d(TAG, "screenOn");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "screenOn: service not found!");
                    return;
                }

                try {
                    daemon.screenOn();
                } catch (RemoteException e) {
                    Log.e(TAG, "screenOn RemoteException");
                }*/
            }

        });
    }

    @Override
    public void screenOff(String opPackageName) {
        Log.d(TAG, "screenOff");
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
            /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "screenOff: service not found!");
                    return;
                }

                try {
                    daemon.screenOff();
                } catch (RemoteException e) {
                    Log.e(TAG, "screenOff RemoteException");
                }*/
            }

        });
    }

    @Override
    public GFConfig getConfig(String opPackageName) {
        if (!canUseFingerprint(opPackageName)) {
            return null;
        }

        return new GFConfig(mConfig);
    }

    @Override
    public GFDevice getDevice(String opPackageName) {
        if (!canUseFingerprint(opPackageName)) {
            return null;
        }

        return new GFDevice(mDevice);
    }

    @Override
    public void dump(IGoodixFingerprintDumpCallback callback, String opPackageName)
            throws RemoteException {
        Log.d(TAG, "dump");
        mDumpCallback = callback;
        dumpCmd(Constants.CMD_DUMP_DATA, null, opPackageName);
    }

    @Override
    public void cancelDump(String opPackageName) throws RemoteException {
        Log.d(TAG, "cancelDump");
        mDumpCallback = null;
        dumpCmd(Constants.CMD_CANCEL_DUMP_DATA, null, opPackageName);
    }

    @Override
    public void dumpCmd(final int cmdId, final byte[] param, String opPackageName) throws RemoteException {
        Log.d(TAG, "dumpCmd " + cmdId);
        if (!canUseFingerprint(opPackageName)) {
            return;
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                /*
                IGFDaemonExt daemon = getFingerprintDaemonExt();
                if (daemon == null) {
                    Log.e(TAG, "dumpCmd: service not found!");
                    return;
                }

                try {
                    daemon.dumpCmd(cmdId, param);
                } catch (RemoteException e) {
                    Log.e(TAG, "dumpCmd RemoteException");
                }*/
            }

        });
    }

    @Override
    public int authenticateFido(final IBinder token, final byte[] aaid, final byte[] finalChallenge,
            final IGoodixFingerprintCallback receiver, final int userId, final String opPackageName) {
        Log.d(TAG, "authenticateFido");
        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return -1;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AuthenticateFidoClient client = new AuthenticateFidoClient(mContext, token, receiver,
                        userId, isRestricted(), opPackageName, aaid, finalChallenge) {
                    @Override
                    public IGFDaemonFido getGoodixFingerprintDaemonFido() {
                        return GoodixFingerprintService.this.getFingerprintDaemonFido();
                    }
                };
                startClient(client);
            }
        });
        return 0; // success scheduled by goodixfingerprintservice
    }

    @Override
    public int stopAuthenticateFido(final IBinder token, final String opPackageName) {
        Log.d(TAG, "cancelAuthenticateFido");
        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return -1;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ClientMonitor client = mCurrentClient;
                if (client == null) {
                    if (DEBUG) Log.d(TAG, "current client is null, can't cancel");
                } else {
                    if (client instanceof AuthenticateFidoClient) {
                        if (client.getToken() == token) {
                            stopClient(client, true);
                        } else {
                            if (DEBUG) Log.d(TAG, "can't stop client "
                                    + client.getOwner() + " since tokens don't match");
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "can't cancel non-AuthenticateFido client "
                                + client.getOwner());
                    }
                }
            }
        });
        return 0; // success scheduled by goodixfingerprintservice
    }

    @Override
    public int isIdValid(int fingerId, String opPackageName) {
        Log.d(TAG, "isIdInvalid");
        if (!canUseFingerprint(opPackageName)) {
            return -1;
        }

        IGFDaemonFido daemon = getFingerprintDaemonFido();
        if (null == daemon) {
            Log.e(TAG, "isIdInvalid:no goodixfingerprintd");
            return -1;
        }

        try {
            return daemon.isIdValid(0, fingerId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int[] getIdList(String opPackageName) {
        Log.d(TAG, "getIdList");
        if (!canUseFingerprint(opPackageName)) {
            return null;
        }

        IGFDaemonFido daemon = getFingerprintDaemonFido();
        if (null == daemon) {
            Log.e(TAG, "getIdList:no goodixfingerprintd");
            return null;
        }

        try {
            return daemon.getIdList(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int invokeFidoCommand(byte[] inBuf, byte[] outBuf) {
        Log.d(TAG, "invokeFidoCommand");

        IGFDaemonFido daemon = getFingerprintDaemonFido();
        if (null == daemon) {
            Log.e(TAG, "invokeFidoCommand:no goodixfingerprintd");
            return -1;
        }

        try {
            return daemon.invokeFidoCommand(inBuf, outBuf);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override // Binder call
    public void untrustedEnroll(final IBinder token, final int userId, final IGoodixFingerprintCallback receiver,
            final String opPackageName) {
        Log.e(TAG, "untrustedEnroll:"+opPackageName);
        addUntrustedCallback(opPackageName, receiver);
        testCmd(token, ShenzhenConstants.CMD_TEST_SZ_UNTRUSTED_ENROLL, null, userId, receiver, opPackageName);
    }

    @Override // Binder call
    public void untrustedEnroll2(final String userDesc,final IBinder token, final int userId, final IGoodixFingerprintCallback receiver,
                                final String opPackageName) {

        Log.e(TAG, "untrustedEnroll2:"+opPackageName);
        addUntrustedCallback(opPackageName, receiver);

        int offset = 0;
        byte[] dataBytes = new byte[TestParamEncoder.testEncodeSizeOfArray(userDesc.getBytes().length)];
        TestParamEncoder.encodeArray(dataBytes, offset, TestResultParser.TEST_TOKEN_METADATA, userDesc.getBytes(),
                userDesc.getBytes().length);
        testCmd(token, ShenzhenConstants.CMD_TEST_SZ_UNTRUSTED_ENROLL, dataBytes, userId, receiver, opPackageName);
    }

    @Override // Binder call
    public void cancelUntrustedEnrollment(final IBinder token, final String opPackageName) {
        cancelTestCmd(token, opPackageName);
    }

    @Override // Binder call
    public void untrustedAuthenticate(final IBinder token, final int userId,
            final IGoodixFingerprintCallback receiver,
            final String opPackageName) {
        Log.e(TAG, "untrustedAuthenticate:"+opPackageName);
        addUntrustedCallback(opPackageName, receiver);
        testCmd(token, ShenzhenConstants.CMD_TEST_SZ_UNTRUSTED_AUTHENTICATE, null, userId, receiver, opPackageName);
    }

    @Override // Binder call
    public void untrustedAuthenticate2(String userDesc,final IBinder token, final int userId,
                                      final IGoodixFingerprintCallback receiver,
                                      final String opPackageName) {
        Log.e(TAG, "untrustedAuthenticate2:"+opPackageName);
        addUntrustedCallback(opPackageName, receiver);

        int offset = 0;
        byte[] dataBytes = new byte[TestParamEncoder.testEncodeSizeOfArray(userDesc.getBytes().length)];
        TestParamEncoder.encodeArray(dataBytes, offset, TestResultParser.TEST_TOKEN_METADATA, userDesc.getBytes(),
                userDesc.getBytes().length);

        testCmd(token, ShenzhenConstants.CMD_TEST_SZ_UNTRUSTED_AUTHENTICATE, dataBytes, userId, receiver, opPackageName);
    }

    @Override // Binder call
    public void cancelUntrustedAuthentication(final IBinder token, final String opPackageName) {
        cancelTestCmd(token, opPackageName);
    }

    @Override // Binder call
    public void untrustedRemove(final IBinder token, final int userId, final IGoodixFingerprintCallback receiver,
            final String opPackageName) {
        stopClient(mCurrentClient, true);
        mUntrustedFingerprintId = 0;
        // testCmd(token, Constants.CMD_TEST_DELETE_UNTRUSTED_ENROLLED_FINGER, null, userId, receiver, opPackageName);
    }

    @Override // Binder call
    public boolean hasEnrolledUntrustedFingerprint(final String opPackageName) {
        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return false;
        }

        return mUntrustedFingerprintId != 0;
    }

    @Override // Binder call
    public int getEnrolledUntrustedFingerprint(String opPackageName) {
        if (!canUseFingerprint(opPackageName)) {
            Log.w(TAG, "Calling not granted permission to use fingerprint");
            return 0;
        }

        return mUntrustedFingerprintId;
    }

    @Override // Binder call
    public void registerSensorControlCallback(IGoodixFingerprintSensorCallback callback) {
        Log.d(TAG, "registerSensorControlCallback");
        mSensorCallback = callback;
    }

    @Override // Binder call
    public void unregisterSensorControlCallback(IGoodixFingerprintSensorCallback callback) {
        Log.d(TAG, "unregisterSensorControlCallback");
        mSensorCallback = null;
    }

    @Override // Binder call
    public void registerPreviewControlCallback(IGoodixFingerprintPreviewCallback callback) {
        Log.d(TAG, "registerPreviewControlCallback");
        mPreviewCallback = callback;
    }

    @Override // Binder call
    public void unregisterPreviewControlCallback(IGoodixFingerprintPreviewCallback callback) {
        Log.d(TAG, "unregisterPreviewControlCallback");
        mPreviewCallback = null;
    }

    @Override
    public GFBaikalConfig getBaikalConfig() {
        Log.d(TAG, "getBaikalConfig");
        return new GFBaikalConfig(mBaikalConfig);
    }

    @Override
    public void setBaikalConfig(GFBaikalConfig config) {
        Log.d(TAG, "setBaikalConfig");
        mBaikalConfig = new GFBaikalConfig(config);
        saveBaikalConfig();
    }

    @Override
    public GFShenzhenConfig getShenzhenConfig(){
        Log.d(TAG, "getShenzhenConfig");
        loadShenzhenConfig();
        return new GFShenzhenConfig(mShenzhenConfig);
    }

    @Override
    public void setShenzhenConfig(GFShenzhenConfig config) {
        Log.d(TAG, "setShenzhenConfig");
        mShenzhenConfig = new GFShenzhenConfig(config);
        saveShenzhenConfig();
    }

    public int getIntValue(final byte[] data) {
        return data[0] | data[1] << 8 | data[2] << 16 | data[3] << 24;
    }

    public int onShenzhenConfigCmd(final byte[] result){
        HashMap<Integer, Object> testResult = TestResultParser.parseConfig(result);
        if (testResult == null) {
            return -1;
        }

        if (testResult.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
            int errCode = (Integer) testResult
                    .get(TestResultParser.TEST_TOKEN_ERROR_CODE);
            if (errCode > 0) {
                return errCode;
            }
        }

        if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_SHORT_EXPOSURE_TIME)) {
            mShenzhenConfig.mShortExposureTime = (Integer) testResult
                    .get(ShenzhenTestResultParser.TEST_TOKEN_SHORT_EXPOSURE_TIME);
        }

        if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_COL)) {
            mShenzhenConfig.mRect_bmp_col = (Integer) testResult
                    .get(ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_COL);
        }

        if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_ROW)) {
            mShenzhenConfig.mRect_bmp_row = (Integer) testResult
                    .get(ShenzhenTestResultParser.TEST_TOKEN_RECT_BMP_ROW);
        }

        if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_SENSOR_ROW)) {
            mShenzhenConfig.mSensorRow = (Integer) testResult
                    .get(ShenzhenTestResultParser.TEST_TOKEN_SENSOR_ROW);
        }

        if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_SENSOR_COL)) {
            mShenzhenConfig.mSensorCol = (Integer) testResult
                    .get(ShenzhenTestResultParser.TEST_TOKEN_SENSOR_COL);
        }
        saveShenzhenConfig();
        return  0;

    }

    public int onConfigCmd(final byte[] result) {
            HashMap<Integer, Object> testResult = TestResultParser.parseConfig(result);
            if (testResult == null) {
                return -1;
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
                int errCode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_ERROR_CODE);
                if (errCode > 0) {
                    return errCode;
                }
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_CHIP_TYPE)) {
                mConfig.mChipType = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_CHIP_TYPE);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_CHIP_SERIES)) {
                mConfig.mChipSeries = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_CHIP_SERIES);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_MAX_FINGERS)) {
                mConfig.mMaxFingers = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_MAX_FINGERS);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_MAX_FINGERS_PER_USER)) {
                mConfig.mMaxFingersPerUser = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_MAX_FINGERS_PER_USER);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_KEY_MODE)) {
                mConfig.mSupportKeyMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_KEY_MODE);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_FF_MODE)) {
                mConfig.mSupportFFMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_FF_MODE);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE)) {
                mConfig.mSupportPowerKeyFeature = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_POWER_KEY_FEATURE);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL)) {
                mConfig.mForbiddenUntrustedEnroll = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_FORBIDDEN_UNTRUSTED_ENROLL);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS)) {
                mConfig.mForbiddenEnrollDuplicateFingers = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_FORBIDDEN_ENROLL_DUPLICATE_FINGERS);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_BIO_ASSAY)) {
                mConfig.mSupportBioAssay = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_BIO_ASSAY);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP)) {
                mConfig.mSupportPerformanceDump = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_PERFORMANCE_DUMP);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_SUPPORT_NAV_MODE)) {
                mConfig.mSupportNavMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_NAV_MODE);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_NAV_DOUBLE_CLICK_TIME)) {
                mConfig.mNavDoubleClickTime = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_NAV_DOUBLE_CLICK_TIME);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_NAV_LONG_PRESS_TIME)) {
                mConfig.mNavLongPressTime = (Integer) testResult
                       .get(TestResultParser.TEST_TOKEN_NAV_LONG_PRESS_TIME);
            }

            if (testResult.containsKey(TestResultParser.TEST_TOKEN_ENROLLING_MIN_TEMPLATES)) {
                mConfig.mEnrollingMinTemplates = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_ENROLLING_MIN_TEMPLATES);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD)) {
                mConfig.mValidImageQualityThreshold = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_VALID_IMAGE_QUALITY_THRESHOLD);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD)) {
                mConfig.mValidImageAreaThreshold = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_VALID_IMAGE_AREA_THRESHOLD);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE)) {
                mConfig.mDuplicateFingerOverlayScore = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_DUPLICATE_FINGER_OVERLAY_SCORE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO)) {
                mConfig.mIncreaseRateBetweenStitchInfo = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_INCREASE_RATE_BETWEEN_STITCH_INFO);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT)) {
                mConfig.mScreenOnAuthenticateFailRetryCount = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SCREEN_ON_AUTHENTICATE_FAIL_RETRY_COUNT);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT)) {
                mConfig.mScreenOffAuthenticateFailRetryCount = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SCREEN_OFF_AUTHENTICATE_FAIL_RETRY_COUNT);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD)) {
                mConfig.mScreenOnValidTouchFrameThreshold = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SCREEN_ON_VALID_TOUCH_FRAME_THRESHOLD);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD)) {
                mConfig.mScreenOffValidTouchFrameThreshold = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SCREEN_OFF_VALID_TOUCH_FRAME_THRESHOLD);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH)) {
                mConfig.mImageQualityThresholdForMistakeTouch = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_IMAGE_QUALITY_THRESHOLD_FOR_MISTAKE_TOUCH);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_AUTHENTICATE_ORDER)) {
                mConfig.mAuthenticateOrder = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_AUTHENTICATE_ORDER);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE)) {
                mConfig.mReissueKeyDownWhenEntryFfMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_FF_MODE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE)) {
                mConfig.mReissueKeyDownWhenEntryImageMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REISSUE_KEY_DOWN_WHEN_ENTRY_IMAGE_MODE);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK)) {
                mConfig.mSupportSensorBrokenCheck = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_SENSOR_BROKEN_CHECK);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR)) {
                mConfig.mBrokenPixelThresholdForDisableSensor = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_SENSOR);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY)) {
                mConfig.mBrokenPixelThresholdForDisableStudy = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_BROKEN_PIXEL_THRESHOLD_FOR_DISABLE_STUDY);
            }

            if (testResult
                    .containsKey(TestResultParser.TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER)) {
                mConfig.mBadPointTestMaxFrameNumber = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_BAD_POINT_TEST_MAX_FRAME_NUMBER);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE)) {
                mConfig.mReportKeyEventOnlyEnrollAuthenticate = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REPORT_KEY_EVENT_ONLY_ENROLL_AUTHENTICATE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE)) {
                mConfig.mRequireDownAndUpInPairsForImageMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_IMAGE_MODE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE)) {
                mConfig.mRequireDownAndUpInPairsForFFMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_FF_MODE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE)) {
                mConfig.mRequireDownAndUpInPairsForKeyMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_KEY_MODE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE)) {
                mConfig.mRequireDownAndUpInPairsForNavMode = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_REQUIRE_DOWN_AND_UP_IN_PAIRS_FOR_NAV_MODE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE)) {
                mConfig.mSupportSetSpiSpeedInTEE = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_SET_SPI_SPEED_IN_TEE);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_SUPPORT_FRR_ANALYSIS)) {
                mConfig.mSupportFrrAnalysis = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_SUPPORT_FRR_ANALYSIS);
            }

            if (testResult.containsKey(
                    TestResultParser.TEST_TOKEN_TEMPLATE_UPDATE_SAVE_THRESHOLD)) {
                mConfig.mTemplateUpateSaveThreshold = (Integer) testResult
                        .get(TestResultParser.TEST_TOKEN_TEMPLATE_UPDATE_SAVE_THRESHOLD);
            }

            if (testResult.containsKey(
                    BaikalTestResultParser.TEST_TOKEN_SUPPORT_IMAGE_SEGMENT)) {
                mConfig.mSupportImageSegment = (Integer) testResult
                        .get(BaikalTestResultParser.TEST_TOKEN_SUPPORT_IMAGE_SEGMENT);
            }

            if (testResult.containsKey(
                    BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING)) {
                mConfig.mSupportBaikalContinuousSampling = (Integer) testResult
                        .get(BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING);
            }

            if (testResult.containsKey(
                    BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING_NUM)) {
                mConfig.mContinuousSamplingNumber = (Integer) testResult
                        .get(BaikalTestResultParser.TEST_TOKEN_SUPPORT_BAIKAL_CONTINUOUS_SAMPLING_NUM);
            }

            if (mConfig.mMaxFingers < mConfig.mMaxFingersPerUser) {
                mConfig.mMaxFingers = mConfig.mMaxFingersPerUser;
            }

            /*if (mDaemonMessageHandler == null) {
                if (mConfig.mChipSeries == Constants.GF_BAIKAL) {
                    mDaemonMessageHandler = new BaikalDaemonMessageHandler();
                    startGetBaikalConfigTask();
                } else {
                    mDaemonMessageHandler = new ShenzhenDaemonMessageHandler();
                    startGetShenzhenConfigTask();
                }
            }*/



            return 0;
    }

    private class DaemonMessageHandlerBase {
        public void handleMessage(long devId, int msgId, int cmdId, byte[] data) {
            Log.d(TAG, "DaemonMessageHandlerBase, handleMessage msgId :" + msgId + " cmdId:" + cmdId);
            switch(msgId) {
                case Constants.GF_FINGERPRINT_ERROR: {
                    onError(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_ACQUIRED: {
                    onAcquired(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_TEMPLATE_ENROLLING: {
                    onEnrollResult(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_TEMPLATE_REMOVED: {
                    onRemoved(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_AUTHENTICATED: {
                    onAuthenticated(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_TEST_CMD: {
                    onTestCmd(devId, cmdId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_DUMP_DATA: {
                    onDump(devId, data);
                    break;
                }
                case Constants.GF_FINGERPRINT_AUTHENTICATED_FIDO:
                default:{
                    Log.d(TAG, "onDaemonMessage, msg<" + msgId + "> ignored!");
                    break;
                }
            }
        }

        public void onTestCmd(long deviceId, final int cmdId, final byte[] data) {
            Log.d(TAG, "onTestCmd " + Constants.testCmdIdToString(mConfig.mChipSeries, cmdId));
            if (Constants.CMD_TEST_GET_CONFIG == cmdId || Constants.CMD_TEST_SET_CONFIG == cmdId) {
                Log.d(TAG, "onTestCmd CMD_TEST_GET_CONFIG" );
                if (onConfigCmd(data) != 0) {
                    return;
                }
            }

            if(ShenzhenConstants.CMD_TEST_SZ_GET_CONFIG == cmdId ){
                Log.d(TAG, "onTestCmd CMD_TEST_SZ_GET_CONFIG" );
                if(onShenzhenConfigCmd(data) != 0){
                    return;
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleTestCmd(cmdId, data);
                }
            });
        }

        public void onDump(long deviceId, byte[] data) {
            Log.d(TAG, "onDump");
            if (mDumpCallback != null && data != null && data.length > 4) {
                int cmdId = getIntValue(data);
                byte[] dumpData = new byte[data.length - 4];
                System.arraycopy(data, 4, dumpData, 0, data.length - 4);
                try {
                    mDumpCallback.onDump(mConfig.mChipSeries, cmdId, dumpData);
                } catch (RemoteException e) {
                }
            }
        }

        public void onAuthenticatedFido(long devId, final int fingerId, final byte[] uvtData) {
            Log.d(TAG, "onAuthenticatedFido, fingerId:" + fingerId);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleAuthenticatedFido(fingerId, uvtData);
                }
            });
        }

        public void onEnrollResult(long devId, final byte[] data) {
            Log.d(TAG, "onEnrollResult, devId :" + devId + " data=" + data);
            HashMap<Integer, Object> testResult = ShenzhenTestResultParser.parse(data);
            if (testResult == null) {
                return;

            }

            int msgType = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE)){
                msgType = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE);
            }
            int fid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID)){
                fid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID);
            }
            int gid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID)){
                gid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID);
            }
            int remaining = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_PROGRESS)){
                remaining = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_PROGRESS);
                Log.d(TAG, "onEnrollResult token remaining = " +remaining);
            }

            byte [] bmpData = {};
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA)){
                bmpData = (byte [] )testResult.get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA);
            }

            int bmpWidth = 0;
            if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_WIDTH)) {
                bmpWidth = (Integer) testResult .get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_WIDTH);
            }
            int bmpHight = 0;
            if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT)) {
                bmpHight = (Integer) testResult .get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT);
            }


            /*final int fpId = getIntValue(data);
            byte[] tmp = new byte[4];
            System.arraycopy(data, 4, tmp, 0, 4);
            final int gpId = getIntValue(tmp);
            System.arraycopy(data, 8, tmp, 0, 4);
            final int samplesRemaining = getIntValue(tmp);*/

            final int fFid = fid;
            final int sampleRemain = remaining;
            final byte[] imageData = bmpData;
            final int width = bmpWidth;
            final int hight = bmpHight;
            final int type = msgType;
            Log.d(TAG, "onEnrollResult width = " + width + " hight =" + hight + " type =" + type + " imageData =" + imageData);
            mHandler.post(new Runnable(){
                @Override
                public void run() {

                    if(width > 0 && hight > 0){
                        Log.d(TAG, "handleImageResult");
                        handleImageResult(type,imageData,width,hight);
                    } else {
                        Log.d(TAG, "onEnrollResult sampleRemain = " +sampleRemain);
                        handleEnrollResult(fFid, sampleRemain);
                    }
                }
            });
        }

        public void onAcquired(long devId, final byte[] data) {
            if (data == null || data.length < 4) {
                return;
            }

            HashMap<Integer, Object> testResult = ShenzhenTestResultParser.parse(data);
            if (testResult == null) {
                return;
            }

            int info = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_ACQUIRED_INFO)){
                info = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_ACQUIRED_INFO);
            } else {
                return;
            }

            final int acquiredInfo = info;
            Log.d(TAG, "onAcquired, acquiredInfo :" + acquiredInfo);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleAcquired(acquiredInfo);
                }
            });
        }

        public void onError(long devId, final byte[] data) {
            if (data == null || data.length < 4) {
                return;
            }
            final int error = getIntValue(data);
            Log.d(TAG, "onError, error :" + error);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleError(error);
                }
            });
        }

        public void onAuthenticated(long devId, final byte[] data) {

            HashMap<Integer, Object> testResult = ShenzhenTestResultParser.parse(data);
            if (testResult == null) {
                return;
            }

            int msgType = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE)){
                msgType = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE);
            }
            int fid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID)){
                fid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID);
            }
            int gid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID)){
                gid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID);
            }


            byte [] bmpData = {};
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA)){
                bmpData = (byte [] )testResult.get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA);
            }

            int bmpWidth = 0;
            if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_WIDTH)) {
                bmpWidth = (Integer) testResult .get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_WIDTH);
            }
            int bmpHight = 0;
            if (testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT)) {
                bmpHight = (Integer) testResult .get(ShenzhenTestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT);
            }

            final int fingerId = fid;
            final byte[] imageData = bmpData;
            final int width = bmpWidth;
            final int hight = bmpHight;
            final int type = msgType;

            Log.d(TAG, "onAuthenticated, fingerId :" + fid + ",groupId : " + gid);
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if(width > 0 && hight > 0){
                        Log.d(TAG, "handleImageResult");
                        handleImageResult(type,imageData,width,hight);
                    } else {
                        handleAuthenticated(fingerId);
                    }


                }
            });
        }

        public void onRemoved(long deviceId, final byte[] data) {
            /*if (data == null || data.length < 8) {
                return;
            }
            final int fingerId = getIntValue(data);
            final byte[] tmp = new byte[4];
            System.arraycopy(data, 4, tmp, 0, 4);
            final int groupId = getIntValue(tmp);
            Log.d(TAG, "onRemoved fingerId = " + fingerId);*/
            HashMap<Integer, Object> testResult = ShenzhenTestResultParser.parse(data);
            if (testResult == null) {
                return;

            }
            int msgType = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE)){
                msgType = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_MESSAGE_TYPE);
            }
            int fid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID)){
                fid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_FID);
            }
            int gid = -1;
            if(testResult.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID)){
                gid = (Integer)testResult.get(ShenzhenTestResultParser.TEST_TOKEN_FINGERPRINT_GID);
            }

            final int fingerId = fid;

            mHandler.post(new Runnable()
            {
                @Override
                public void run() {
                    handleRemoved(fingerId);
                }
            });
        }
    };

    private class BaikalDaemonMessageHandler extends DaemonMessageHandlerBase {
        public void handleMessage(long devId, int msgId, int cmdId, byte[] data) {
            switch(msgId) {
                case BaikalConstants.GF_FINGERPRINT_SENSOR_DISPLAY_CONTROL: {
                    onSensorDisplayControl(devId, cmdId, data);
                    break;
                }
                case BaikalConstants.GF_FINGERPRINT_PREVIEW_DISPLAY_CONTROL: {
                    onPreviewDisplayControl(devId, cmdId, data);
                    break;
                }
                default:{
                    super.handleMessage(devId, msgId, cmdId, data);
                    break;
                }
            }
        }

        public void onSensorDisplayControl(long devId, final int cmdId, final byte[] data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleSensorDisplayControl(cmdId, data);
                }
            });
        }

        public void onPreviewDisplayControl(long devId, final int cmdId, final byte[] data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handlePreviewDisplayControl(cmdId, data);
                }
            });
        }
    };

    private class ShenzhenDaemonMessageHandler extends DaemonMessageHandlerBase {
        public void handleMessage(long devId, int msgId, int cmdId, byte[] data) {
            super.handleMessage(devId, msgId, cmdId, data);
            Log.d(TAG, "ShenzhenDaemonMessageHandler devId = " + devId + " msgId="+ msgId + " cmdId=" + cmdId + " data=" + data );
        }

        public void onSensorDisplayControl(long devId, final int cmdId, final byte[] data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleSensorDisplayControl(cmdId, data);
                }
            });
        }

        public void onPreviewDisplayControl(long devId, final int cmdId, final byte[] data) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    handlePreviewDisplayControl(cmdId, data);
                }
            });
        }
    };

    private IGFDaemonCallback mDaemonCallback = new IGFDaemonCallback() {
        @Override
        public void onDaemonMessage(long devId, int msgId, int cmdId, byte[] data) {
            Log.d(TAG, "onDaemonMessage devId = " + devId + " msgId="+ msgId + " cmdId=" + cmdId + " data=" + data );
            if (mDaemonMessageHandler == null && cmdId == Constants.CMD_TEST_GET_CONFIG) {
                Log.d(TAG, "onTestCmd " + Constants.testCmdIdToString(mConfig.mChipSeries, cmdId));
                onConfigCmd(data);
            } else if (mDaemonMessageHandler != null) {
                Log.d(TAG, "DaemonMessageHandler.handleMessage devId = " + devId + " msgId="+ msgId + " cmdId=" + cmdId + " data=" + data);
                mDaemonMessageHandler.handleMessage(devId, msgId, cmdId, data);
            }
        }

        @Override
        public void onDaemonDied() {
            handleError(GoodixFingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
        }
    };

    @Override
    public void openOrCloseHBMMode(int mode0){
        if (mode0 == 1){
            setHBMMode(true);
            return;
        }
        if (mode0 == 0){
            setHBMMode(false);
            return;
        }
    }

    @Override
    public void setHBMMode(boolean open){
       /* String content = "0x20000";
        if (!open)
            content = "0xF0000";
        try {
            Log.d(TAG, "setHBMMode , mode = " + open);
            FileOutputStream out = null;
            out = new FileOutputStream(new File("/sys/class/drm/card0-DSI-1/disp_param"));
            out.write(content.getBytes("ASCII"));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "write file failed!" + e);
        }*/


    }

    @Override
    public void setScreenBrightnessR15(int value) {
        try {
            Log.d(TAG, "setScreenBrightnessR15F , value = " + value);
            if (value > 255) {
                value = 255;
            }
            FileOutputStream out = null;
            out = new FileOutputStream(new File("/proc/leds/brightness"));
            out.write(String.valueOf(value).getBytes("ASCII"));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "write file failed!" + e);
        }
    }

    @Override
    public void setScreenBrightness(int value) {
        setScreenManualMode();

        boolean ret = Settings.System.putInt(mContentResolver,
                Settings.System.SCREEN_BRIGHTNESS, value);
        Log.d(TAG, "setScreenBrightness result = " + ret);
    }

    @Override
    public void setScreenManualMode() {
        try {
            int mode = Settings.System.getInt(mContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            Log.d(TAG,"getCurrent mode = " + mode);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getScreenBrightness() {
        int defVal = 255;
        int value = Settings.System.getInt(mContentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
        Log.d(TAG,"getScreenBrightness ==" + value);
        return value;
    }

    @Override
    public void goToSleep() {
        try {
            Class<?> powerManager = Class.forName("android.os.PowerManager");
            Log.d(TAG, "success to get android.os.PowerManager");

            Method goToSleep = powerManager.getMethod("goToSleep", long.class);
            Log.d(TAG, "success to get method: goToSleep");

            goToSleep.invoke(pm,
                    new Object[] {
                            SystemClock.uptimeMillis()
                    });
            Log.d(TAG, "success to goToSleep: ");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        }
    }

    @Override
    public void wakeUp() {
        try {
            Class<?> powerManager = Class.forName("android.os.PowerManager");
            Log.d(TAG, "success to get android.os.PowerManager");

            Method wakeUp = powerManager.getMethod("wakeUp", long.class);
            Log.d(TAG, "success to get method: wakeUp");

            wakeUp.invoke(pm,
                    new Object[] {
                            SystemClock.uptimeMillis()
                    });
            Log.d(TAG, "success to wakeUp: ");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        }
    }
}
