/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.shenzhen.sensor;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.GFShenzhenConfig;

import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.pri.factorytest.R;

import com.goodix.fingerprint.ShenzhenConstants;

public class SensorControlService{

    private static final String TAG = "SensorControlService";

    private static final int GF_FINGERPRINT_AREA_SHOW_INDICATOR = 1;
    private static final int GF_FINGERPRINT_AREA_HIDE = 2;
    private static final int GF_FINGERPRINT_AREA_CHANGE_TO_BACKGROUND_BGCOLOR = 3;
    private static final int GF_FINGERPRINT_AREA_CHANGE_CYAN_BGCOLOR = 4;
    private static final int GF_FINGERPRINT_CHANGE_TO_SPMT_MODE = 5;
    private static final int  GF_FINGERPRINT_CLEAR_SPMT_MODE = 6;

    private static final float GF_SENSOR_AREA_BG_ENLARGE_SCALE = 1.2f;

    private Context mContext = null;

    private WindowManager.LayoutParams mSensorAreaWindowLayoutParams = null;
    private ViewGroup.LayoutParams mSensorAreaLayoutParams;
    private FrameLayout mSensorAreaWindowLayout = null;
    private View mSensorAreaLayout;
    private ImageView mFingerprintAnimator;
    private ImageView mBitmapBackground;
    private ViewGroup.LayoutParams mFingerprintAnimatorLayoutParams;
    private AnimatedVectorDrawable mIconAnimationDrawable;
    //private View mFindSensorView;
    //private ViewGroup.LayoutParams mFindSensorViewLayoutParams;

    private WindowManager mWindowManager = null;
    private boolean mSensorAdded = false;

    private Handler mHandler = null;
    private boolean mHasPendingDownRunnable = false;

    private static boolean mIsSPMT = false;

    private int mSensorAreaBackgroundColor = Constants.DEFAULT_SENSOR_AREA_CYAN_COLOR;
    private static SensorControlService sInstance;

    private SensorControlService(Context context) {

        mContext = context;
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        if (mHandler == null) {
            mHandler = new Handler(mContext.getMainLooper());
        }

        Log.d(TAG, "SensorControlService, package name is: " + mContext.getPackageName());
        statusBarHeight = getStatusBarHeight();
        initView();
    }

    public static SensorControlService getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SensorControlService(context);
        }
        return sInstance;
    }

    public void setSPMTMode (boolean isSPMT){
        mIsSPMT = isSPMT;
    }

    private void initView() {
        Log.d(TAG, "initial layout");

        mSensorAreaWindowLayout = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.fingerprint_sensor_area, null);
        mSensorAreaLayout = mSensorAreaWindowLayout.findViewById(R.id.sensor_area);
        mSensorAreaLayoutParams = mSensorAreaLayout.getLayoutParams();
        mSensorAreaWindowLayoutParams = new WindowManager.LayoutParams();
        //mSensorAreaWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mSensorAreaWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mSensorAreaWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mSensorAreaWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mSensorAreaWindowLayoutParams.format = PixelFormat.RGBA_8888;
        mSensorAreaWindowLayoutParams.x = Constants.DEFAULT_SENSOR_X;
        mSensorAreaWindowLayoutParams.y = Constants.DEFAULT_SENSOR_Y;
        mSensorAreaWindowLayoutParams.width = Constants.DEFAULT_SENSOR_WIDTH;
        mSensorAreaWindowLayoutParams.height = Constants.DEFAULT_SENSOR_HEIGHT;

        mFingerprintAnimator = (ImageView) mSensorAreaWindowLayout.findViewById(R.id.fingerprint_image_hint);
        mBitmapBackground = (ImageView) mSensorAreaWindowLayout
                .findViewById(R.id.bitmap_background);
        mFingerprintAnimatorLayoutParams = mFingerprintAnimator.getLayoutParams();
        mIconAnimationDrawable = (AnimatedVectorDrawable) mFingerprintAnimator.getDrawable();
        //mFindSensorView = mSensorAreaWindowLayout.findViewById(R.id.find_sensor_view);
        //mFindSensorViewLayoutParams = mFindSensorView.getLayoutParams();

        setSensorAreaOnTouchListener(mSensorAreaLayout);
        //setSensorAreaOnKeyListener(mSensorAreaLayout);
    }

    public  void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    private void updateFingerprintArea() {
        GFShenzhenConfig shenZhenConfig = GoodixFingerprintManager.getFingerprintManager(mContext).getShenzhenConfig();
        if (shenZhenConfig != null) {
            mSensorAreaWindowLayoutParams.x = shenZhenConfig.mSensorX;
            mSensorAreaWindowLayoutParams.y = shenZhenConfig.mSensorY + statusBarHeight;//statusbarHeight:72
            mSensorAreaWindowLayoutParams.width = (int) (shenZhenConfig.mSensorWidth/** GF_SENSOR_AREA_BG_ENLARGE_SCALE*/);
            mSensorAreaWindowLayoutParams.height = (int) (shenZhenConfig.mSensorHeight/** GF_SENSOR_AREA_BG_ENLARGE_SCALE*/);
        }
        mSensorAreaLayoutParams.width = mSensorAreaWindowLayoutParams.width;
        mSensorAreaLayoutParams.height = mSensorAreaWindowLayoutParams.height;
        // enlarge sensor bg size to 2*mSensorWidth, sensor icon size not changed
        mFingerprintAnimatorLayoutParams.width = shenZhenConfig.mSensorWidth;
        mFingerprintAnimatorLayoutParams.height = shenZhenConfig.mSensorHeight;
        mFingerprintAnimator.setLayoutParams(mFingerprintAnimatorLayoutParams);
        mSensorAreaLayout.setLayoutParams(mSensorAreaLayoutParams);
        mSensorAreaBackgroundColor = shenZhenConfig.mSensorAreaBackgroundColor;
        Log.d(TAG, "updateFingerprintArea mSensorAdded="+mSensorAdded);

        expandViewTouchDelegate(mFingerprintAnimator,10,10,10,10);
        expandViewTouchDelegate(mSensorAreaLayout,10,10,10,10);

        if (mSensorAdded) {
            mWindowManager.updateViewLayout(mSensorAreaWindowLayout, mSensorAreaWindowLayoutParams);
        }

    }

    private int statusBarHeight = 0;
    private int getStatusBarHeight() {
        int statusBarHeight2 = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight2 = mContext.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight2;
    }

    public void setSensorAreaBackgroundColor(int color) {
        Log.d(TAG, "setSensorAreaBackgroundColor: color = 0x" + Integer.toHexString(color).toUpperCase());
        mSensorAreaBackgroundColor = color;
    }

    private Runnable mDownRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "mDownRunnable");
            GoodixFingerprintManager.getFingerprintManager(mContext).testCmd(ShenzhenConstants.CMD_TEST_SZ_FINGER_DOWN, null);
            mHasPendingDownRunnable = false;
        }
    };

    private void setSensorAreaOnKeyListener(View view){
        view.setFocusable(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    Log.d("onkey", "onkey KEYCODE_ENTER ACTION_DOWN");
                    mFingerprintAnimator.setVisibility(View.GONE);
                    ShapeDrawable downShape =  new ShapeDrawable(new OvalShape());
                    downShape.getPaint().setColor(mSensorAreaBackgroundColor | 0xFF000000);
                    mSensorAreaLayout.setBackground(downShape);
                    Log.d(TAG, "sensor_area_shape ");
                    mHandler.removeCallbacks(mDownRunnable);
                    mHasPendingDownRunnable = true;
                    mHandler.postDelayed(mDownRunnable, 150);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    Log.d("onkey", "onkey KEYCODE_ENTER ACTION_UP");
                    if(mIsSPMT){
                        return  true;
                    }

                    mFingerprintAnimator.setVisibility(View.VISIBLE);
                    ShapeDrawable upShape =  new ShapeDrawable(new OvalShape());
                    upShape.getPaint().setColor(0x00000000);
                    mSensorAreaLayout.setBackground(upShape);
                    Log.d(TAG, "sensor_area_shape_hide ");

                    if (mHasPendingDownRunnable) {
                        mHasPendingDownRunnable = false;
                        mHandler.removeCallbacks(mDownRunnable);
                    } else {
                        GoodixFingerprintManager.getFingerprintManager(mContext).testCmd(ShenzhenConstants.CMD_TEST_SZ_FINGER_UP, null);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void setSensorAreaOnTouchListener(View view) {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN ");
                        mFingerprintAnimator.setVisibility(View.GONE);
                        //mFindSensorView.setVisibility(View.VISIBLE);
                        //mSensorAreaLayout.setBackgroundColor(mSensorAreaBackgroundColor | 0xFF000000);

                        //mSensorAreaLayout.setBackground(mContext.getDrawable(R.drawable.sensor_area_shape));

                        ShapeDrawable downShape =  new ShapeDrawable(new OvalShape());
                        downShape.getPaint().setColor(mSensorAreaBackgroundColor | 0xFF000000);
                        mSensorAreaLayout.setBackground(downShape);

                        Log.d(TAG, "sensor_area_shape ");
                        //updateFingerprintArea();
                        mHandler.removeCallbacks(mDownRunnable);
                        mHasPendingDownRunnable = true;
                        mHandler.postDelayed(mDownRunnable, 150);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        Log.d(TAG, "ACTION_UP ");

                        if(mIsSPMT){
                            break;
                        }
                        mFingerprintAnimator.setVisibility(View.VISIBLE);
                        //mFindSensorView.setVisibility(View.GONE);
                        //mSensorAreaLayout.setBackgroundColor(0x00000000);
                        //LayerDrawable layerDrawable = (LayerDrawable) mContext.getDrawable(R.drawable.sensor_area_shape_hide);

                        ShapeDrawable upShape =  new ShapeDrawable(new OvalShape());
                        upShape.getPaint().setColor(0x00000000);
                        mSensorAreaLayout.setBackground(upShape);
                        Log.d(TAG, "sensor_area_shape_hide ");

                        //updateFingerprintArea();
                        if (mHasPendingDownRunnable) {
                            mHasPendingDownRunnable = false;
                            mHandler.removeCallbacks(mDownRunnable);
                        } else {
                            GoodixFingerprintManager.getFingerprintManager(mContext).testCmd(ShenzhenConstants.CMD_TEST_SZ_FINGER_UP, null);
                        }
                        break;

                    default:
                        break;
                }

                return true;
            }
        });
    }

    public void onSensorControlCmd(final int cmdId, final byte[] data) {
        Log.d(TAG, "onSensorControlCmd cmdId=" + cmdId);
        switch(cmdId) {
            case GoodixFingerprintManager.FINGERPRINT_SHOW_INDICATOR:
                Log.d(TAG, "onSensorControlCmd GF_FINGERPRINT_AREA_SHOW_INDICATOR");
                //mSensorAreaLayout.setBackgroundColor(0x00000000);
                if(mIsSPMT){
                    break;
                }

                ShapeDrawable downShape =  new ShapeDrawable(new OvalShape());
                downShape.getPaint().setColor(0x00000000);
                mSensorAreaLayout.setBackground(downShape);
                mFingerprintAnimator.setVisibility(View.VISIBLE);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateFingerprintArea();
                        if (!mSensorAdded) {
                            mWindowManager.addView(mSensorAreaWindowLayout, mSensorAreaWindowLayoutParams);
                            mSensorAdded = true;
                        }
                    }
                });
                break;
            case GoodixFingerprintManager.FINGERPRINT_AREA_HIDE:
                Log.d(TAG, "onSensorControlCmd GF_FINGERPRINT_AREA_HIDE");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSensorAdded == true && mWindowManager != null) {
                            mWindowManager.removeView(mSensorAreaWindowLayout);
                            mSensorAdded = false;
                        }
                    }
                });
                break;
            case GoodixFingerprintManager.FINGERPRINT_CHANGE_TO_BACKGROUND_BGCOLOR:
                Log.d(TAG, "onSensorControlCmd GF_FINGERPRINT_CHANGE_TO_BACKGROUND_BGCOLOR");
                mFingerprintAnimator.setVisibility(View.GONE);
                GFShenzhenConfig baikalConfig = GoodixFingerprintManager.getFingerprintManager(mContext).getShenzhenConfig();
                if (baikalConfig != null) {
                    mSensorAreaBackgroundColor = baikalConfig.mSensorAreaBackgroundColor;
                }
                //mSensorAreaLayout.setBackgroundColor(mSensorAreaBackgroundColor | 0xFF000000);
                Log.d(TAG, "onSensorControlCmd shape color");
                ShapeDrawable colorShape =  new ShapeDrawable(new OvalShape());
                colorShape.getPaint().setColor(mSensorAreaBackgroundColor | 0xFF000000);
                mSensorAreaLayout.setBackground(colorShape);

                    /*mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSensorAreaLayout.setBackgroundColor(mSensorAreaBackgroundColor | 0xFF000000);
                            updateFingerprintArea();
                            if (!mSensorAdded) {
                                mWindowManager.addView(mSensorAreaWindowLayout, mSensorAreaWindowLayoutParams);
                                mSensorAdded = true;
                            }
                        }
                    });*/
                break;
            case GF_FINGERPRINT_AREA_CHANGE_CYAN_BGCOLOR:
                Log.d(TAG, "onSensorControlCmd GF_FINGERPRINT_AREA_CHANGE_CYAN_BGCOLOR");
                mSensorAreaBackgroundColor = Constants.DEFAULT_SENSOR_AREA_CYAN_COLOR;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //mSensorAreaLayout.setBackgroundColor(mSensorAreaBackgroundColor | 0xFF000000);
                        ShapeDrawable downShape =  new ShapeDrawable(new OvalShape());
                        downShape.getPaint().setColor(mSensorAreaBackgroundColor | 0xFF000000);
                        mSensorAreaLayout.setBackground(downShape);
                        updateFingerprintArea();
                        if (!mSensorAdded) {
                            mWindowManager.addView(mSensorAreaWindowLayout, mSensorAreaWindowLayoutParams);
                            mSensorAdded = true;
                        }
                    }
                });
                break;
            case GF_FINGERPRINT_CHANGE_TO_SPMT_MODE:
                setSPMTMode(true);
                break;
            case GF_FINGERPRINT_CLEAR_SPMT_MODE:
                setSPMTMode(false);
                break;
            default:
                Log.d(TAG, "unsupported cmdId : " + cmdId);
                break;
        }
    }

}
