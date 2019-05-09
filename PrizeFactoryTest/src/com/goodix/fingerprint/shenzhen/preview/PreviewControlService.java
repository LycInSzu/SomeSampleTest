/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.shenzhen.preview;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.GFShenzhenConfig;

import com.goodix.fingerprint.ShenzhenConstants;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.service.IGoodixFingerprintInterface;
import com.goodix.fingerprint.service.IGoodixFingerprintPreviewCallback;

import com.pri.factorytest.R;
import com.goodix.fingerprint.utils.TestResultParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;


public class PreviewControlService extends Service implements IBinder.DeathRecipient {
    private static final String TAG = "PreviewControlService";

    private Context mContext = null;
    private IGoodixFingerprintInterface mIGoodixFingerprintInterface = null;
    private WindowManager.LayoutParams mPopWindowLayoutParams = null;
    private ViewGroup.LayoutParams mPreViewAreaLayoutParams = null;
    private CameraPreview mPreViewArea = null;
    private RelativeLayout mPreViewAreaLayout = null;
    private WindowManager mWindowManager = null;
    private boolean mPreViewAdded = false;
    private float mLocationX;
    private float mLocationY;
    private float mTouchStartX;
    private float mTouchStartY;
    private int mWidth;
    private int mHeight;
    private static final int GF_FINGERPRINT_SHOW_PREVIEW = 1;
    private static final int GF_FINGERPRINT_HIDE_PREVIEW = 2;
    private Handler mHandler = null;
    Point screenSize = new Point();

    private GoodixFingerprintManager mGoodixFingerprintManager;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mContext = getApplicationContext();

        mGoodixFingerprintManager = GoodixFingerprintManager.getFingerprintManager(mContext);

        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }
        if (mHandler == null) {
            mHandler = new Handler(mContext.getMainLooper());
        }

        boolean serviceInited = false;
        int retryCount = 5;
        while(!serviceInited && retryCount > 0) {
            serviceInited = initService();
            retryCount --;
        }

        initView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "bind");
        return mGoodixFingerprintPreviewCallback;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mPreViewAdded == true && mWindowManager != null) {
            mWindowManager.removeView(mPreViewAreaLayout);
            mPreViewAdded = false;
        }
    }

    private void initView() {
        Log.d(TAG, "initial layout");
        mWindowManager.getDefaultDisplay().getSize(screenSize);
        mWidth = (screenSize.x < screenSize.y) ? (screenSize.x / 4) : (screenSize.y / 4);
        mHeight = mWidth;

        mPopWindowLayoutParams = new WindowManager.LayoutParams();
        //mPopWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mPopWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mPopWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mPopWindowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        mPopWindowLayoutParams.format = PixelFormat.RGBA_8888;
        mPopWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mPopWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mPreViewAreaLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.fingerprint_preview_area,
                null);
        mPreViewAreaLayout.setBackgroundColor(Color.RED);
        setPreviewOnTouchListener(mPreViewAreaLayout);
        mPreViewArea = (CameraPreview) mPreViewAreaLayout.findViewById(R.id.showpreview);

        mPreViewAreaLayoutParams = mPreViewArea.getLayoutParams();
        mPreViewAreaLayoutParams.width = mWidth;
        mPreViewAreaLayoutParams.height = mHeight;
        mPreViewArea.setLayoutParams(mPreViewAreaLayoutParams);
    }

    private void setPreviewOnTouchListener(View view) {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLocationX = event.getRawX();
                mLocationY = event.getRawY();

                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN " + event.getActionIndex());
                    mTouchStartX = event.getX() /* + mWidth / 2 */;
                    mTouchStartY = event.getY() /* + mHeight / 2 */;
                    Log.d(TAG, "ACTION_DOWN " + "mTouchStartX = " + mTouchStartX + ", mTouchStartY = " + mTouchStartY);
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_Move " + "mLocationX = " + mLocationX + ", mLocationY = " + mLocationY);
                    updatePreViewPosition();
                    break;

                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP " + event.getActionIndex());
                    updatePreViewPosition();
                    mTouchStartX = mTouchStartY = 0;
                    break;

                default:
                    break;
                }

                return true;
            }
        });
    }

    private void updatePreViewPosition() {
        mPopWindowLayoutParams.x = (int) (mLocationX);
        mPopWindowLayoutParams.y = (int) (screenSize.y - mLocationY);//if start from bottom
        if (mPreViewAdded) {
            mWindowManager.updateViewLayout(mPreViewAreaLayout, mPopWindowLayoutParams);
        }
    }

    private int getStatusBarHeight() {
        int statusBarHeight = -1;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        Log.d(TAG, "statusBarHeight = " + statusBarHeight);
        return statusBarHeight;
    }

    private boolean isShowInAuthenticateView() {
        boolean isShow = false;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.d(TAG, "isShowInAuthenticateView : pkg : " + cn.getPackageName());
        Log.d(TAG, "isShowInAuthenticateView : cls : " + cn.getClassName());
        if (cn.getClassName().equals("com.vivo.fingerprint.FingerprintActivity")) {
            Log.d(TAG, "isShowInAuthenticateView");
            isShow = true;
        }
        return isShow;
    }

    @Override
    public void binderDied() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mPreViewAreaLayout);
        }
    }

    private boolean initService() {
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
            return false;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "initService NoSuchMethodException: ", e);
            return false;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "initService IllegalAccessException: ", e);
            return false;
        } catch (InvocationTargetException e) {
            Log.e(TAG, "initService InvocationTargetException: ", e);
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "initService IllegalArgumentException: ", e);
            return false;
        }

        if (binder == null) {
            Log.e(TAG, "failed to getService: " + Constants.GOODIX_FINGERPRINT_SERVICE_NAME);
            return true;
        }

        mIGoodixFingerprintInterface = IGoodixFingerprintInterface.Stub.asInterface(binder);
        Log.d(TAG, "initService mIGoodixFingerprintInterface=" + mIGoodixFingerprintInterface);
        try {
            if(mIGoodixFingerprintInterface != null) {
                mIGoodixFingerprintInterface.registerPreviewControlCallback(mGoodixFingerprintPreviewCallback);
            } else {
                return false;
            }
        } catch (RemoteException e) {
            return false;
        }
        return true;
    }

    private IGoodixFingerprintPreviewCallback.Stub mGoodixFingerprintPreviewCallback = new IGoodixFingerprintPreviewCallback.Stub() {
        @Override
        public void onPreviewControlCmd(final int cmdId, final byte[] data) {
            GFShenzhenConfig mConfig = mGoodixFingerprintManager.getShenzhenConfig();
            int cmdIdpreview = cmdId;
            if ((mConfig.misSensorPreviewBmp == 0)&&(cmdIdpreview != ShenzhenConstants.CMD_TEST_SZ_FIND_SENSOR)) {
                cmdIdpreview = GoodixFingerprintManager.FINGERPRINT_AREA_HIDE;
            }
            Log.d(TAG, "onPreviewControlCmd cmdIdpreview=" + cmdIdpreview);
            switch(cmdIdpreview) {
                case GoodixFingerprintManager.FINGERPRINT_SHOW_INDICATOR:
                    Log.d(TAG, "onPreviewControlCmd GF_FINGERPRINT_SHOW_PREVIEW");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mPreViewAdded) {
                                mWindowManager.addView(mPreViewAreaLayout, mPopWindowLayoutParams);
                                mPreViewAdded = true;
                            }

                            HashMap<Integer, Object> map = TestResultParser.parse(Constants.GF_SHENZHEN,data);
                            if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA)) {
                                byte[] bmp = (byte[]) map.get(TestResultParser.TEST_TOKEN_BMP_DATA);
                                int width = 0;
                                int height = 0;
                                if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA_WIDTH)) {
                                    width = (Integer) map.get(TestResultParser.TEST_TOKEN_BMP_DATA_WIDTH);
                                }
                                if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT)) {
                                    height = (Integer) map.get(TestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT);
                                }
                                Log.d(TAG, "onPreviewControlCmd bmp length=" + bmp.length + "; width=" + width + ", height=" + height);

                                if (mPreViewArea != null) {
                                    if ((bmp != null) && (bmp.length == width * height)) {
                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreViewArea.getLayoutParams();
                                        params.width = width*3;
                                        params.height = height*3;
                                        mPreViewArea.setLayoutParams(params);
                                        mPreViewArea.updateBitmap(bmp, width, height);
                                    }
                                }
                            }
                        }
                    });
                    break;
                case ShenzhenConstants.CMD_TEST_SZ_FIND_SENSOR:
                    Log.d(TAG, "onPreviewControlCmd GF_FINGERPRINT_SHOW_PREVIEW");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mPreViewAdded) {
                                mWindowManager.addView(mPreViewAreaLayout, mPopWindowLayoutParams);
                                mPreViewAdded = true;
                            }

                            HashMap<Integer, Object> map = TestResultParser.parse(Constants.GF_SHENZHEN,data);
                            if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA)) {
                                byte[] bmp = (byte[]) map.get(TestResultParser.TEST_TOKEN_BMP_DATA);
                                int width = 0;
                                int height = 0;
                                if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA_WIDTH)) {
                                    width = (Integer) map.get(TestResultParser.TEST_TOKEN_BMP_DATA_WIDTH);
                                }
                                if (map.containsKey(TestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT)) {
                                    height = (Integer) map.get(TestResultParser.TEST_TOKEN_BMP_DATA_HEIGHT);
                                }
                                Log.d(TAG, "onPreviewControlCmd bmp length=" + bmp.length + "; width=" + width + ", height=" + height);

                                if (mPreViewArea != null) {
                                    if ((bmp != null) && (bmp.length == width * height)) {
                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreViewArea.getLayoutParams();
                                        params.width = width*2;
                                        params.height = height*2;
                                        mPreViewArea.setLayoutParams(params);
                                        mPreViewArea.updateBitmap(bmp, width, height);
                                    }
                                }
                            }
                        }
                    });
                    break;
                case GoodixFingerprintManager.FINGERPRINT_AREA_HIDE:
                    Log.d(TAG, "onPreviewControlCmd GF_FINGERPRINT_HIDE_PREVIEW");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mPreViewAdded == true && mWindowManager != null) {
                                mWindowManager.removeView(mPreViewAreaLayout);
                                mPreViewAdded = false;
                            }
                        }
                    });
                    break;
                default:
                    Log.d(TAG, "unsupported cmdIdpreview : " + cmdIdpreview);
                    break;
            }
        }
    };

}
