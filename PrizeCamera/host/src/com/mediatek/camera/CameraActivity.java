/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.OnThumbnailClickedListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.debug.profiler.IPerformanceProfile;
import com.mediatek.camera.common.debug.profiler.PerformanceTracker;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.IModeListener;
import com.mediatek.camera.common.mode.ModeManager;
import com.mediatek.camera.common.mode.video.VideoMode;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.PriorityConcurrentSkipListMap;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.portability.pq.PictureQuality;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.ui.CameraAppUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.os.SystemProperties;


/**
 * Camera app's activity.
 * used to manager the app's life cycle, transfer system information
 * (such as key event, configuration change event ....).
 * Create app common UI and add to the activity view tree.
 */
public class CameraActivity extends PermissionActivity implements IApp {
    private static final Tag TAG = new Tag(CameraActivity.class.getSimpleName());
    private static final int MSG_CLEAR_SCREEN_ON_FLAG = 0;
    private static final int MSG_SET_SCREEN_ON_FLAG = 1;
    private static final int DELAY_MSG_SCREEN_SWITCH = 2 * 60 * 1000; // 2min
    // Orientation hysteresis amount used in rounding, in degrees
    private static final int ORIENTATION_HYSTERESIS = 5;
    private static final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
    /* intent for gts  begin */
    private static final String EXTRA_USE_FRONT_CAMERA_FOR_GOOGLE    
      = "com.google.assistant.extra.USE_FRONT_CAMERA";
    private static final String EXTRA_USE_FRONT_CAMERA_FOR_ANDROID
      = "android.intent.extra.USE_FRONT_CAMERA";
      /* intent for gts  end */
    private static final String IS_CAMERA = "isCamera";

    private CameraAppUI mCameraAppUI;
    private PriorityConcurrentSkipListMap<String, KeyEventListener> mKeyEventListeners =
            new PriorityConcurrentSkipListMap<String, KeyEventListener>(true);

    private PriorityConcurrentSkipListMap<String, BackPressedListener> mBackPressedListeners =
            new PriorityConcurrentSkipListMap<String, BackPressedListener>(true);

    private /*IModeListener*/ModeManager mModeManager;//prize-change-haungpengfei-2018-10-29
    private boolean mIsResumed;

    private final List<OnOrientationChangeListener>
            mOnOrientationListeners = new ArrayList<>();
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private OrientationEventListener mOrientationListener;
    protected Uri mUri;
    /*prize-modify-add camera mute-xiaoping-20181009-start*/
    protected String mCameramuteValue = "off";
    /*prize-modify-add camera mute-xiaoping-20181009-end*/
    protected OnThumbnailClickedListener mThumbnailClickedListener =
            new OnThumbnailClickedListener() {
                @Override
                public void onThumbnailClicked() {
                    goToGallery(mUri);
                }
            };
    /*prize-add-huangpengfei-2018-10-29-start*/
    private Handler mUiHandler;
    private boolean isBatteryLow;
    private IAppUi.HintInfo mFlashHint;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private static final int MSG_SHOW_EXIT_APP_TIPS = 0;
    private static final int MSG_HIDE_EXIT_APP_TIPS = 1;
    private static final int MSG_EXIT_APP = 2;
    private static final int TIMEOUT_SHOW_HINT_MSC = 115;
    private static final int TIMEOUT_EXIT_APP_MSC = 120;
    private int mTimeCount;
    private IAppUi.HintInfo mExitAppHint;
	 private static final boolean CAN_USE_CAMRA_WHEN_LOW_BATTERY = "1".equals(android.os.SystemProperties.get("ro.pri_use_camera_low_battery", "0"));
    private final List<OnTouchListener>
            mOnTouchListeners = new ArrayList<>();
    private final List<OnBatteryLowListener>
            mOnBatteryLowListeners = new ArrayList<>();
    /*prize-add-huangpengfei-2018-10-29-end*/

    /*prize-modify-increase external storage-xiaoping-20190111-start*/
    private Intent mData;
    private int MY_REQUEST_CODE;
    private static final String FILE_PATH_INDEX = "/storage/";
    private static final String FOLDER_PATH = "/" + Environment.DIRECTORY_DCIM + "/Camera";
    private static final String DOCUMENTFILE_TYPE = "image/jpg";
    /*prize-modify-increase external storage-xiaoping-20190111-end*/
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
    private long clickTime = 0;
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/
    /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-start*/
    private boolean isBatteryLow10 = false;
    private IAppUi.HintInfo mLowPowerInfo;
    /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-end*/
    private static final int REQUEST_CODE_SCAN_GALLERY = 100;
    /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-start*/
    private int messageMarginTop;
    private IAppUi.HintInfo mExitAppToast;
    private static final int MSG_EXIT_APP_TOAST = 3;
    /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-end*/

    @Override
    protected void onNewIntentTasks(Intent newIntent) {
        super.onNewIntentTasks(newIntent);
    }

    @Override
    protected void onCreateTasks(Bundle savedInstanceState) {
        if (!isThirdPartyIntent(this) && !isOpenFront(this)) {  /* intent for gts  begin */
            CameraUtil.launchCamera(this);
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onCreate").start();
        super.onCreateTasks(savedInstanceState);
        //if (CameraUtil.isTablet() || WifiDisplayStatusEx.isWfdEnabled(this)) {
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //  setRequestedOrientation(CameraUtil.calculateCurrentScreenOrientation(this));
        //}
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_main);
        mOrientationListener = new OrientationEventListenerImpl(this);
        //create common ui module.
        mCameraAppUI = new CameraAppUI(this);
        profile.mark("CameraAppUI initialized.");
        mCameraAppUI.onCreate();
        profile.mark("CameraAppUI.onCreate done.");
        /*prize-modify-The backlight is turned to the brightest when the camera is turned on-xiaoping-20190306-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 3) {
            changeAppBrightness(this,255);
        }
        /*prize-modify-The backlight is turned to the brightest when the camera is turned on-xiaoping-20190306-end*/
        mModeManager = new ModeManager();
        mModeManager.create(this);
        profile.mark("ModeManager.create done.");
        profile.stop();
        /*prize-add-huangpengfei-2018-10-29-start*/
        mUiHandler = new UiHandler();
        mFlashHint = new IAppUi.HintInfo();
        mFlashHint.mDelayTime = 3000;
        int id = getResources().getIdentifier("hint_text_background",
                "drawable", getPackageName());
        mFlashHint.mBackground = getDrawable(id);
        mFlashHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mFlashHint.mHintText = getString(R.string.flash_failed_when_battery_is_low);
        mExitAppHint = new IAppUi.HintInfo();
        mExitAppHint.mDelayTime = 5000;
        mExitAppHint.mBackground = getDrawable(id);
        mExitAppHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mExitAppHint.mHintText = getString(R.string.camera_timeout_warning_message);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryStatusBR, intentFilter);
        startExitAppTimer();
        /*prize-add-huangpengfei-2018-10-29-end*/
        /*prize-modify-increase external storage-xiaoping-20190111-start*/
        requestPermissionForSD();
        /*prize-modify-increase external storage-xiaoping-20190111-end*/

        /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-start*/
        mLowPowerInfo = new IAppUi.HintInfo();
        mLowPowerInfo.mDelayTime = 3000;
        int id_low = getResources().getIdentifier("hint_text_background",
                "drawable", getPackageName());
        mLowPowerInfo.mBackground = getDrawable(id_low);
        mLowPowerInfo.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mLowPowerInfo.mHintText = getString(R.string.can_not_open_front_camera_on_lowpower);
        /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-end*/
        /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-start*/
        mExitAppToast = new IAppUi.HintInfo();
        mExitAppToast.mDelayTime = 1000;
        mExitAppToast.mBackground = getDrawable(id);
        mExitAppToast.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mExitAppToast.mHintText = getString(R.string.exit_camera_toast);
        /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-end*/
    }

    @Override
    protected void onStartTasks() {
        super.onStartTasks();
    }

    @Override
    protected void onResumeTasks() {
        LogHelper.i(TAG, "onResumeTasks+");
    	CameraDeviceManagerFactory.setCurrentActivity(this);
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onResume").start();
        mIsResumed = true;
        mOrientationListener.enable();
        super.onResumeTasks();
        PictureQuality.enterCameraMode();
        mModeManager.resume();
        profile.mark("ModeManager resume done.");
        mCameraAppUI.onResume();
        profile.mark("CameraAppUI resume done.");
        mCameraAppUI.setThumbnailClickedListener(mThumbnailClickedListener);
        keepScreenOnForAWhile();
        profile.stop();
        /*prize-add-huangpengfei-2018-10-29-start*/
        startExitAppTimer();
        /*prize-add-huangpengfei-2018-10-29-end*/
        LogHelper.i(TAG, "onResumeTasks-");

    }

    @Override
    protected void onPauseTasks() {
        LogHelper.i(TAG, "onPauseTasks+");
        mIsResumed = false;
        super.onPauseTasks();
        PictureQuality.exitCameraMode();
        mModeManager.pause();
        mCameraAppUI.onPause();
        mOrientationListener.disable();
        resetScreenOn();
        /*prize-add-huangpengfei-2018-10-29-start*/
        stopExitAppTimer();
        /*prize-add-huangpengfei-2018-10-29-end*/
        LogHelper.i(TAG, "onPauseTasks-");
    }

    @Override
    protected void onStopTasks() {
        super.onStopTasks();
        LogHelper.i(TAG, "onStopTasks+");
        /*prize-add-huangpengfei-2018-10-29-start*/
        mTimeCount = 0;
        if (mCameraAppUI != null){
            mCameraAppUI.onStop();
        }
        /*prize-add-huangpengfei-2018-10-29-end*/
        LogHelper.i(TAG, "onStopTasks-");
    }

    @Override
    protected void onDestroyTasks() {
        super.onDestroyTasks();
        LogHelper.i(TAG, "onDestroyTasks+");
        mModeManager.destroy();
        mCameraAppUI.onDestroy();
        CameraDeviceManagerFactory.release(this);
        /*prize-add-huangpengfei-2018-10-29-start*/
        unregisterReceiver(mBatteryStatusBR);
        stopExitAppTimer();
        /*prize-add-huangpengfei-2018-10-29-end*/
        LogHelper.i(TAG, "onDestroyTasks-");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RotateLayout root = (RotateLayout) findViewById(R.id.app_ui);
        LogHelper.d(TAG, "onConfigurationChanged orientation = " + newConfig.orientation);
        if (root != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                root.setOrientation(0, false);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                root.setOrientation(90, false);
            }
            mCameraAppUI.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*prize add for bug[75253]-huangpengfei-2019-04-28-start*/
        for (OnTouchListener l : mOnTouchListeners) {
            l.onTouch();
        }
        /*prize add for bug[75253]-huangpengfei-2019-04-28-end*/
        Iterator iterator = mKeyEventListeners.entrySet().iterator();
        KeyEventListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (KeyEventListener) map.getValue();
            if (listener != null && listener.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 2) {
            if (keyCode == KeyEvent.KEYCODE_BACK && !"recording".equals(mCameraAppUI.getCaptureType()) && !mCameraAppUI.isSettingViewShow()
                    && !mCameraAppUI.getSelfTimerState() && !mCameraAppUI.isPluginPageShow()) {
                mUiHandler.sendEmptyMessage(MSG_EXIT_APP_TOAST);
                return true;
                /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Iterator iterator = mKeyEventListeners.entrySet().iterator();
        KeyEventListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (KeyEventListener) map.getValue();
            if (listener != null && listener.onKeyUp(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Iterator iterator = mBackPressedListeners.entrySet().iterator();
        BackPressedListener listener = null;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (BackPressedListener) map.getValue();
            if (listener != null && listener.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onUserInteraction() {
        if (mModeManager == null || !mModeManager.onUserInteraction()) {
            super.onUserInteraction();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public IAppUi getAppUi() {
        return mCameraAppUI;
    }

    @Override
    public void enableKeepScreenOn(boolean enabled) {
        LogHelper.d(TAG, "enableKeepScreenOn enabled " + enabled);
        if (mIsResumed) {
            mMainHandler.removeMessages(MSG_SET_SCREEN_ON_FLAG);
            Message msg = Message.obtain();
            msg.arg1 = enabled ? 1 : 0;
            msg.what = MSG_SET_SCREEN_ON_FLAG;
            mMainHandler.sendMessage(msg);
        }

    }

    @Override
    public void notifyNewMedia(Uri uri, boolean needNotify) {
        /*prize-modify-bugid:70624 Probably unable to enter the album after continuous shooting-xiaoping-20190115-start*/
        if (uri == null) {
            LogHelper.e(TAG,"current uri is null ,do not update new uri and return");
//            return;
        }
        /*prize-modify-bugid:70624 Probably unable to enter the album after continuous shooting-xiaoping-20190115-end*/
        mUri = uri;
    }

    @Override
    public boolean notifyCameraSelected(String newCameraId) {
        /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 3) {
            if (isBatteryLow10 && "1".equals(newCameraId)) {
                int messageMarginTop = (int) CameraActivity.this.getResources().getDimension(R.dimen.exit_app_hint_margintop);
                mCameraAppUI.showScreenHint(mLowPowerInfo,messageMarginTop);
                return false;
            } else {
                return mModeManager.onCameraSelected(newCameraId);
            }
            /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-end*/
        } else {
            return mModeManager.onCameraSelected(newCameraId);
        }

    }

    @Override
    public void registerKeyEventListener(KeyEventListener keyEventListener, int priority) {
        if (keyEventListener == null) {
            LogHelper.e(TAG, "registerKeyEventListener error [why null]");
        }
        mKeyEventListeners.put(mKeyEventListeners.getPriorityKey(priority, keyEventListener),
                keyEventListener);
    }

    @Override
    public void registerBackPressedListener(BackPressedListener backPressedListener,
            int priority) {
        if (backPressedListener == null) {
            LogHelper.e(TAG, "registerKeyEventListener error [why null]");
        }
        mBackPressedListeners.put(mBackPressedListeners.getPriorityKey(priority,
                backPressedListener), backPressedListener);
    }

    @Override
    public void unRegisterKeyEventListener(KeyEventListener keyEventListener) {
        if (keyEventListener == null) {
            LogHelper.e(TAG, "unRegisterKeyEventListener error [why null]");
        }
        if (mKeyEventListeners.containsValue(keyEventListener)) {
            mKeyEventListeners.remove(mKeyEventListeners.findKey(keyEventListener));
        }
    }

    @Override
    public void unRegisterBackPressedListener(BackPressedListener backPressedListener) {
        if (backPressedListener == null) {
            LogHelper.e(TAG, "unRegisterBackPressedListener error [why null]");
        }
        if (mBackPressedListeners.containsValue(backPressedListener)) {
            mBackPressedListeners.remove(mBackPressedListeners.findKey(backPressedListener));
        }
    }

    @Override
    public void registerOnOrientationChangeListener(OnOrientationChangeListener listener) {
        synchronized (mOnOrientationListeners) {
            if (!mOnOrientationListeners.contains(listener)) {
                if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    listener.onOrientationChanged(mOrientation);
                }
                mOnOrientationListeners.add(listener);
            }
        }
    }

    @Override
    public void unregisterOnOrientationChangeListener(OnOrientationChangeListener listener) {
        synchronized (mOnOrientationListeners) {
            if (mOnOrientationListeners.contains(listener)) {
                mOnOrientationListeners.remove(listener);
            }
        }
    }

    @Override
    public int getGSensorOrientation() {
        synchronized (mOnOrientationListeners) {
            return mOrientation;
        }
    }



    @Override
    public void enableGSensorOrientation() {
        if (mIsResumed) {
            // can not enable, after activity paused
            mOrientationListener.enable();
        }
    }

    @Override
    public void disableGSensorOrientation() {
        //always run disable, since settings request
        mOrientationListener.disable();
    }

    /**
     * start gallery activity to browse the file withe specified uri.
     *
     * @param uri The specified uri of file to browse.
     */
    protected void goToGallery(Uri uri) {
        if (uri == null) {
            LogHelper.d(TAG, "uri is null, can not go to gallery");
            return;
        }
        String mimeType = getContentResolver().getType(uri);
        LogHelper.d(TAG, "[goToGallery] uri: " + uri + ", mimeType = " + mimeType);
        Intent intent = new Intent(REVIEW_ACTION);
        intent.setDataAndType(uri, mimeType);
        intent.putExtra(IS_CAMERA, true);
        // add this for screen pinning
        ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activityManager.LOCK_TASK_MODE_PINNED == activityManager
                    .getLockTaskModeState()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            LogHelper.e(TAG, "[startGalleryActivity] Couldn't view ", ex);
        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "handleMessage what = " + msg.what + " arg1 = " + msg.arg1);
            switch (msg.what) {
                case MSG_CLEAR_SCREEN_ON_FLAG:
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                case MSG_SET_SCREEN_ON_FLAG:
                    boolean enabled = msg.arg1 == 1;
                    if (enabled) {
                        keepScreenOn();
                    } else {
                        keepScreenOnForAWhile();
                    }
                    break;
                default:
                    break;
            }
        };
    };

    private void resetScreenOn() {
        mMainHandler.removeMessages(MSG_SET_SCREEN_ON_FLAG);
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnForAWhile() {
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mMainHandler.sendEmptyMessageDelayed(MSG_CLEAR_SCREEN_ON_FLAG,
                DELAY_MSG_SCREEN_SWITCH);
    }

    private void keepScreenOn() {
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * The implementer of OrientationEventListener.
     */
    private class OrientationEventListenerImpl extends OrientationEventListener {
        public OrientationEventListenerImpl(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }
            synchronized (mOnOrientationListeners) {
                final int roundedOrientation = roundOrientation(orientation, mOrientation);
                if (mOrientation != roundedOrientation) {
                    mOrientation = roundedOrientation;
                    LogHelper.i(TAG, "mOrientation = " + mOrientation);
                    for (OnOrientationChangeListener l : mOnOrientationListeners) {
                        l.onOrientationChanged(mOrientation);
                    }
                }
            }
        }
    }

    private static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    /**
     * Judge current is launch by intent.
     * @param activity the launch activity.
     * @return true means is launch by intent; otherwise is false.
     */
    private boolean isThirdPartyIntent(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action) ||
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }
    

    /**
     * add this for GTS test,because GTS will open front
     * @param activity current activity
     * @return whether is front
     */
    private boolean isOpenFront(Activity activity) {   
        Intent intent = activity.getIntent();
        boolean isOpenFront =
                intent.getBooleanExtra(EXTRA_USE_FRONT_CAMERA_FOR_ANDROID,false) ||
                        intent.getBooleanExtra(EXTRA_USE_FRONT_CAMERA_FOR_GOOGLE,false);
        return isOpenFront;
    }
     /* intent for gts  end */   
    

    /*prize-modify-add camera mute-xiaoping-20181009-start*/
    @Override
    public void setCameraMuteValue(String value) {
        mCameramuteValue = value;
    }

    @Override
    public String getCameraMuteValue() {
        return mCameramuteValue;
    }

    private BroadcastReceiver mBatteryStatusBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) (level * 100 / (float)scale);
                if(!CAN_USE_CAMRA_WHEN_LOW_BATTERY){
                    if (batteryPct <= 5 && SystemProperties.getInt("ro.pri.current.project",0) != 3) { //prize-modify-Do not use the flash when the battery is low-xiaoping-20190306
                        Toast.makeText(CameraActivity.this, CameraActivity.this
                                .getString(R.string.close_camera_when_battery_is_low), Toast.LENGTH_SHORT).show();
                        CameraActivity.this.finish();
                        return;
                    }
                }
                if (batteryPct <= 15) {
                    if (FeatureSwitcher.getCurrentProjectValue() != 2 &&  !isBatteryLow) {
                        mCameraAppUI.showScreenHint(mFlashHint);
                        /*prize-add-huangpengfei-2019-03-15-start*/
                        for (OnBatteryLowListener l : mOnBatteryLowListeners) {
                            l.onBatteryLow();
                        }
                        /*prize-add-huangpengfei-2019-03-15-end*/
                    }
                    isBatteryLow = true;
                }else {
                    isBatteryLow = false;
                }
                /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-start*/
                if (batteryPct <= 10) {
                    isBatteryLow10 = true;
                } else {
                    isBatteryLow10 = false;
                }
                /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-end*/
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mUiHandler == null)return false;//prize add fix bug[68703]-huangpengfei-2018-11-26
        synchronized (mUiHandler) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mTimeCount >= TIMEOUT_SHOW_HINT_MSC){
                        mUiHandler.sendEmptyMessage(MSG_HIDE_EXIT_APP_TIPS);
                    }
                    mTimeCount = 0;
                    for (OnTouchListener l : mOnTouchListeners) {
                        l.onTouch();
                    }
                    LogHelper.d(TAG, "MotionEvent.ACTION_DOWN"+"Click coordinates,getX: "+event.getX()+",getY: "+event.getY()+",getRawX: "+event.getRawX()+",getRawY: "+event.getRawY());
                    break;
                case MotionEvent.ACTION_UP:
                    mTimeCount = 0;
                    LogHelper.d(TAG, "MotionEvent.ACTION_UP");
                    break;
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //PRIZE-add-disable KeyEvent.KEYCODE_ENTER event - 20170314-pengcancan-start
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            return true;
        }
        //PRIZE-add-disable KeyEvent.KEYCODE_ENTER event - 20170314-pengcancan-end
        if (mUiHandler == null)return false;//prize add fix bug[70670]-huangpengfei-2019-01-11
        synchronized (mUiHandler) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (mTimeCount >= TIMEOUT_SHOW_HINT_MSC){
                        mUiHandler.sendEmptyMessage(MSG_HIDE_EXIT_APP_TIPS);
                    }
                    mTimeCount = 0;
                    LogHelper.d(TAG, "KeyEvent.ACTION_DOWN");
                    break;
                case KeyEvent.ACTION_UP:
                    mTimeCount = 0;
                    LogHelper.d(TAG, "KeyEvent.ACTION_UP");
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void startExitAppTimer() {
        LogHelper.i(TAG, "startExitAppTimer");
        if (mTimer == null) {
            mTimer = new Timer();
            mTimeCount = 0;
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mUiHandler != null) {
                        mTimeCount += 1;
                        if (mTimeCount == TIMEOUT_SHOW_HINT_MSC) {
                            mUiHandler.sendEmptyMessage(MSG_SHOW_EXIT_APP_TIPS);
                        }else if (mTimeCount == TIMEOUT_EXIT_APP_MSC){
                            mUiHandler.sendEmptyMessage(MSG_EXIT_APP);
                        }
                    }
                }};
            mTimer.schedule(mTimerTask,0,1000);
        }
        if (mTimeCount >= TIMEOUT_SHOW_HINT_MSC){
            mUiHandler.sendEmptyMessage(MSG_HIDE_EXIT_APP_TIPS);
        }
    }

    private void stopExitAppTimer() {
        LogHelper.i(TAG,"stopExitAppTimer");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimeCount >= TIMEOUT_SHOW_HINT_MSC){
            mUiHandler.sendEmptyMessage(MSG_HIDE_EXIT_APP_TIPS);
        }
    }

    private final class UiHandler extends Handler {
        public UiHandler() {
            super();
            messageMarginTop = (int) CameraActivity.this.getResources().getDimension(R.dimen.exit_app_hint_margintop);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogHelper.i(TAG, "[handleMessage]msg id=" + msg.what);
            switch (msg.what) {
                case MSG_SHOW_EXIT_APP_TIPS:
                    if (isRecording()) return;
                    mCameraAppUI.showScreenHint(mExitAppHint,messageMarginTop);
                    break;
                case MSG_HIDE_EXIT_APP_TIPS:
                    mCameraAppUI.hideScreenHint(mExitAppHint);
                    break;
                case MSG_EXIT_APP:
                    if (isRecording()) return;
                    CameraActivity.this.finish();
                    break;
                /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-start*/
                case MSG_EXIT_APP_TOAST:
                    exitCameraApp();
                    break;
                /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20190427-end*/
                default:
            }
        }
    }

    private boolean isRecording(){
        ICameraMode currentMode = mModeManager.getCurrentMode();
        if (currentMode instanceof VideoMode) {
            VideoMode videoMode = (VideoMode) currentMode;
            if (videoMode.getVideoState() == VideoMode.VideoState.STATE_RECORDING){
                return true;
            }
        }
        return false;
    }
    /*prize-modify-add camera mute-xiaoping-20181009-end*/


    public void reset(){
        /*prize-modify-add professional mode function-xiaoping-20190216-start*/
        mCameraAppUI.reset();
        /*prize-modify-add professional mode function-xiaoping-20190216-end*/
        /*prize-add camera restore settings-haungpengfei-20181112-start*/
        mModeManager.reset();
        /*prize-add camera restore settings-haungpengfei-20181112-end*/


    }


    /**
     * add for xiaoping 20181116 get the value in datastore
     * @param key feature setting key
     * @param defaultvalue set the dafaultvalue when get the value is null
     * @param cameraid
     * @return return the value in datastore
     */
    @Override
    public String getSettingValue(String key, String defaultvalue, int cameraid) {
        DataStore dataStore = mModeManager.getCameraContext().getDataStore();
        String value = dataStore.getValue(key,defaultvalue,dataStore.getCameraScope(cameraid));
        LogHelper.i(TAG,"key: "+key+",cameraid: "+cameraid+",value: "+value);
        return value;

    }

    /*prize-modify-bugid:68677 Judging the current storage time can be used-xiaoping-20181123-start*/
    @Override
    public ModeManager getModeManger() {
        return mModeManager;
    }



    public boolean isStrorageReady() {
        return mModeManager.getCameraContext().getStorageService().getCaptureStorageSpace() > 0;
    }
    /*prize-modify-bugid:68677 Judging the current storage time can be used-xiaoping-20181123-end*/


    /*prize-modify-increase external storage-xiaoping-20190111-start*/
    public StorageVolume getExtranelStorageVolume() {
        StorageManager storageManager = (StorageManager) getApplicationContext()
                .getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumes = storageManager.getStorageVolumes();
        for (StorageVolume volume : volumes) {
            String volumePathStr = volume.getPath();
            if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(volume.getState())) {
                /*prize-add try catch fixbug[72588]-huangpengfei-20190313-start*/
                try{
                    if (Environment.isExternalStorageRemovable(new File(
                            volumePathStr))) {
                        return volume;
                    }
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                /*prize-add try catch fixbug[72588]-huangpengfei-20190313-end*/
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            this.getApplicationContext()
                    .getContentResolver()
                    .takePersistableUriPermission(
                            data.getData(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            mData = data;
        }else if(requestCode == REQUEST_CODE_SCAN_GALLERY){
            if (mModeManager != null){
                mModeManager.onActivityResult(requestCode,resultCode,data);
            }
        }
    }

    public String getDocumentId(String  abs) {
        String filePath = abs;
        int externalStorageIndex = filePath.indexOf(FILE_PATH_INDEX);
        if (externalStorageIndex == -1) {
            return null;
        }
        String documentId = filePath.substring(externalStorageIndex
                + FILE_PATH_INDEX.length());
        documentId = documentId.replaceFirst("/", ":");
        return documentId;
    }


    public Uri doCreate(String dirPath,String displayName,String mimeType) {
        Uri uri = DocumentsContract.buildChildDocumentsUriUsingTree(
                mData.getData(), getDocumentId(dirPath));
        try {
         Uri documenturi = DocumentsContract.createDocument(this.getApplicationContext().getContentResolver(), uri, mimeType, displayName);
         return documenturi;
        } catch (Exception e) {
            requestPermissionForSD();
        }
        return null;
    }

    @Override
    public Uri creteDocumenFile(String displayname,String mintype) {
        Uri uri = null;
        if (getExtranelStorageVolume() != null) {
            String camerapath = FILE_PATH_INDEX+getExtranelStorageVolume().toString()+FOLDER_PATH;
            uri = doCreate(getCameraDirectorySD(),displayname,mintype);
        }
        return uri;
    }

    @Override
    public String getCameraDirectorySD() {
        String sdmount = mModeManager.getCameraContext().getStorageService().getExternalStoragePath();
        StorageVolume volume = getExtranelStorageVolume();
        String fileDirectory = null;
        if (volume != null) {
            fileDirectory = sdmount+FOLDER_PATH;
        }
        /*prize-modify-bugid:70733 Determine if the target folder exists and create it if it does not exist-xiaoping-20190114-start*/
        File filepicture = new File(fileDirectory);
        File filedcim = new File(sdmount+"/" + Environment.DIRECTORY_DCIM);
        if (volume != null && !filepicture.exists()) {
            if (!filedcim.exists()) {
                doCreate(sdmount+"/",Environment.DIRECTORY_DCIM,DocumentsContract.Document.MIME_TYPE_DIR);
            }
            doCreate(sdmount+"/"+Environment.DIRECTORY_DCIM+"/","Camera",DocumentsContract.Document.MIME_TYPE_DIR);
        }
        /*prize-modify-bugid:70733 Determine if the target folder exists and create it if it does not exist-xiaoping-20190114-end*/
        return fileDirectory;
    }

    @Override
    public boolean isExtranelStorageMount() {
        return getExtranelStorageVolume() != null;
    }

    @Override
    public boolean isHasPermissionForSD() {
        return mData != null;
    }

    private void requestPermissionForSD() {
        /*prize-modify-bugid:71247 Write files to an external SD card using File-xiaoping-20190220-start*/
/*        StorageVolume volume = getExtranelStorageVolume();
        if (volume != null) {
            Intent intent = volume.createAccessIntent(null);
            startActivityForResult(intent, MY_REQUEST_CODE);
        }*/
        /*prize-modify-bugid:71247 Write files to an external SD card using File-xiaoping-20190220-end*/
    }
    /*prize-modify-increase external storage-xiaoping-20190111-end*/

    /*prize-add-huangpengfei-2019-02-28-start*/
    @Override
    public void unregisterOnTouchListener(OnTouchListener listener) {
        synchronized (mOnTouchListeners) {
            if (mOnTouchListeners.contains(listener)) {
                mOnTouchListeners.remove(listener);
            }
        }
    }

    @Override
    public void registerOnTouchListener(OnTouchListener listener) {
        synchronized (mOnTouchListeners) {
            if (!mOnTouchListeners.contains(listener)) {
                mOnTouchListeners.add(listener);
            }
        }
    }
    /*prize-add-huangpengfei-2019-02-28-end*/

    /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20180813-start*/
    private void exitCameraApp() {
        if ((System.currentTimeMillis() - clickTime) > 1500) {
/*            Toast toast = new Toast(CameraActivity.this);
            View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.exit_camera_toast, null);
            toast.setView(view);
            toast.setDuration(1000);
            toast.setGravity(Gravity.BOTTOM, 0, (int) getResources().getDimension(R.dimen.shutter_group_height));
            toast.show();*/
            mCameraAppUI.showScreenHint(mExitAppToast,messageMarginTop);
            clickTime = System.currentTimeMillis();
        } else {
            LogHelper.e(TAG, "exit application");
            this.finish();
        }
    }
    /*prize-add-K6206Q2TV: Camera application should be closed as pressing 2 times back button-xiaoping-20180813-end*/

    /*prize-modify-The backlight is turned to the brightest when the camera is turned on-xiaoping-20190306-start*/
    public void changeAppBrightness(Context context, int brightness) {
        Window window = ((Activity) context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }
    /*prize-modify-The backlight is turned to the brightest when the camera is turned on-xiaoping-20190306-end*/

    /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-start*/
    @Override
    public boolean isBatteryLow() {
        return isBatteryLow;
    }
    /*prize-modify-Do not use the flash when the battery is low-xiaoping-20190306-end*/

    /*prize-add-huangpengfei-2019-03-15-start*/
    @Override
    public void unregisterBatteryLowListener(OnBatteryLowListener listener) {
        synchronized (mOnBatteryLowListeners) {
            if (mOnBatteryLowListeners.contains(listener)) {
                mOnBatteryLowListeners.remove(listener);
            }
        }
    }

    @Override
    public void registerOnBatteryLowListener(OnBatteryLowListener listener) {
        synchronized (mOnBatteryLowListeners) {
            if (!mOnBatteryLowListeners.contains(listener)) {
                mOnBatteryLowListeners.add(listener);
            }
        }
    }
    /*prize-add-huangpengfei-2019-03-15-end*/
}
