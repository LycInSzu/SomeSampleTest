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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.debug.profiler.IPerformanceProfile;
import com.mediatek.camera.common.debug.profiler.PerformanceTracker;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateLayout;

import android.provider.Settings;

//prize-added by tangan-20190327-adjust display size-begin
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.os.Build;
import android.content.res.Configuration;
import android.content.res.Resources;
//prize-added by tangan-20190327-adjust display size-end

/**
 * The KeyguardManager service can be queried to determine which state we are in.
 * If started from the lock screen, the activity may be quickly started,
 * resumed, paused, stopped, and then started and resumed again. This is
 * problematic for launch time from the lock screen because we typically open the
 * camera in onResume() and close it in onPause(). These camera operations take
 * a long time to complete. To workaround it, this class filters out
 * high-frequency onResume()->onPause() sequences if the KeyguardManager
 * indicates that we have started from the lock screen.
 *
 * Subclasses should override the appropriate onPermission[Create|Start...]Tasks()
 * method in place of the original.
 *
 * Sequences of onResume() followed quickly by onPause(), when the activity is
 * started from a lockscreen will result in a quick no-op.
 */
public abstract class QuickActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final Tag TAG = new Tag(QuickActivity.class.getSimpleName());
    /** onResume tasks delay from secure lockscreen. */
    private static final long ON_RESUME_DELAY_SECURE_MILLIS = 30;
    /** onResume tasks delay from non-secure lockscreen. */
    private static final long ON_RESUME_DELAY_NON_SECURE_MILLIS = 15;

    /** A reference to the main handler on which to run lifecycle methods. */
    private Handler mMainHandler;

    /**
     * True if onResume tasks have been skipped, and made false again once they
     * are executed within the onResume() method or from a delayed Runnable.
     */
    private boolean mSkippedFirstOnResume = false;

    /** Was this session started with onCreate(). */
    protected boolean mStartupOnCreate = false;

    /** Handle to Keyguard service. */
    private KeyguardManager mKeyguardManager = null;
    /**
     * A runnable for deferring tasks to be performed in onResume() if starting
     * from the lockscreen.
     */
    private final Runnable mOnResumeTasks = new Runnable() {
        @Override
        public void run() {
            if (mSkippedFirstOnResume) {
                LogHelper.d(TAG, "delayed Runnable --> onPermissionResumeTasks()");
                // Doing the tasks, can set to false.
                mSkippedFirstOnResume = false;
                onPermissionResumeTasks();
            }
        }
    };

    @Override
    protected final void onNewIntent(Intent intent) {
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onNewIntent").start();
        setIntent(intent);
        super.onNewIntent(intent);
        onNewIntentTasks(intent);
        profile.stop();
    }

    @Override
    protected final void onCreate(Bundle bundle) {
        LogHelper.i(TAG, "onCreate()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onCreate").start();
        mStartupOnCreate = true;
        //setDefaultDisplay(this);//prize-added by tangan-20190327-adjust display size-begin
        super.onCreate(bundle);
        mMainHandler = new Handler(getMainLooper());
        onPermissionCreateTasks(bundle);
        profile.stop();
    }

    @Override
    protected final void onStart() {
        LogHelper.i(TAG, "onStart()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onStart").start();
        onPermissionStartTasks();
        super.onStart();
        profile.stop();
    }

    @Override
    protected final void onResume() {
        LogHelper.i(TAG, "onResume()");
        /*prize-add for flashlight-huangpengfei-20190318-start*/
        Settings.Global.putInt(this.getContentResolver(), "pri_flash_status", 0);
        /*prize-add for flashlight-huangpengfei-20190318-end*/
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onResume").start();
        startPreWarmService();
        // For lockscreen launch, there are two possible flows:
        // 1. onPause() does not occur before mOnResumeTasks is executed:
        //      Runnable mOnResumeTasks sets mSkippedFirstOnResume to false
        // 2. onPause() occurs within ON_RESUME_DELAY_*_MILLIS:
        //     a. Runnable mOnResumeTasks is removed
        //     b. onPermissionPauseTasks() is skipped, mSkippedFirstOnResume remains true
        //     c. next onResume() will immediately execute onPermissionResumeTasks()
        //        and set mSkippedFirstOnResume to false

        mMainHandler.removeCallbacks(mOnResumeTasks);
        if (isKeyguardLocked() && mSkippedFirstOnResume == false) {
            // Skipping onPermissionResumeTasks; set to true.
            mSkippedFirstOnResume = true;
            long delay = isKeyguardSecure() ? ON_RESUME_DELAY_SECURE_MILLIS :
                    ON_RESUME_DELAY_NON_SECURE_MILLIS;
            LogHelper.d(TAG, "onResume() --> postDelayed(mOnResumeTasks," + delay + ")");
            mMainHandler.postDelayed(mOnResumeTasks, delay);
        } else {
            LogHelper.d(TAG, "onResume --> onPermissionResumeTasks()");
            // Doing the tasks, can set to false.
            mSkippedFirstOnResume = false;
            onPermissionResumeTasks();
        }
        super.onResume();
        profile.stop();
    }

    @Override
    protected final void onPause() {
        LogHelper.i(TAG, "onPause()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onPause").start();
        mMainHandler.removeCallbacks(mOnResumeTasks);
        // Only run onPermissionPauseTasks if we have not skipped onPermissionResumeTasks in a
        // first call to onResume.  If we did skip onPermissionResumeTasks (note: we
        // just killed any delayed Runnable), we also skip onPermissionPauseTasks to
        // adhere to lifecycle state machine.
        if (mSkippedFirstOnResume == false) {
            LogHelper.d(TAG, "onPause --> onPermissionPauseTasks()");
            onPermissionPauseTasks();
        }
        super.onPause();
        mStartupOnCreate = false;
        profile.stop();
    }

    @Override
    protected final void onStop() {
        LogHelper.i(TAG, "onStop()");
        if (isChangingConfigurations()) {
            LogHelper.d(TAG, "changing configurations");
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onStop").start();
        onPermissionStopTasks();
        super.onStop();
        profile.stop();
    }

    @Override
    protected final void onRestart() {
        LogHelper.i(TAG, "onRestart()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onRestart").start();
        super.onRestart();
        profile.stop();
    }

    @Override
    protected final void onDestroy() {
        LogHelper.i(TAG, "onDestroy()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onDestroy").start();
        onPermissionDestroyTasks();
        super.onDestroy();
        profile.stop();
    }

    protected boolean isKeyguardLocked() {
        boolean isLocked = false;
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (kgm != null) {
            isLocked = kgm.isKeyguardLocked();
        }
        LogHelper.d(TAG, "isKeyguardLocked = " + isLocked);
        return isLocked;
    }

    protected boolean isKeyguardSecure() {
        boolean isSecure = false;
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (kgm != null) {
            isSecure = kgm.isKeyguardSecure();
        }
        LogHelper.d(TAG, "isKeyguardSecure = " + isSecure);
        return isSecure;
    }

    /**
     * Subclasses should override this.
     */
    protected void onNewIntentTasks(Intent newIntent) {
    }

    /**
     * These are for permission check flow.
     * @param savedInstanceState create bundle to sub class.
     */
    protected void onPermissionCreateTasks(Bundle savedInstanceState) {
    }

    protected void onPermissionStartTasks() {
    }

    protected void onPermissionResumeTasks() {
    }

    protected void onPermissionPauseTasks() {
    }

    protected void onPermissionStopTasks() {
    }

    protected void onPermissionDestroyTasks() {
    }

    private void startPreWarmService() {
        if (!CameraUtil.isServiceRun(getApplicationContext(),
                "com.mediatek.camera.CameraAppService")) {
            Intent appServiceIntent = new Intent(getApplicationContext(),
                    CameraAppService.class);
            CameraUtil.startService(getApplicationContext(), appServiceIntent);
        }
    }
	//prize-added by tangan-20190327-adjust display size-begin
    public  int getDefaultDisplayDensity(int displayId) {
        try {
            final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            return wm.getInitialDisplayDensity(displayId);
        } catch (Exception exc) {
            LogHelper.e(TAG,"exc: "+exc);
            return -1;
        }
    }

    public  void setDefaultDisplay(Context context) {
        if(Build.VERSION.SDK_INT > 23) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            Configuration origConfig = context.getResources().getConfiguration();
            int initialDisplayDensity = getDefaultDisplayDensity(Display.DEFAULT_DISPLAY);
            LogHelper.i(TAG,"initialDisplayDensity="+initialDisplayDensity+",origConfig.densityDpi="+origConfig.densityDpi);
            LogHelper.i(TAG,"height="+dm.heightPixels+", dm.widthPixel ="+dm.widthPixels);
            origConfig.screenHeightDp = dm.heightPixels*160/initialDisplayDensity;
            origConfig.screenWidthDp = dm.widthPixels*160/initialDisplayDensity;
            origConfig.densityDpi = initialDisplayDensity;
            LogHelper.i(TAG,"origConfig.screenHeightDp="+origConfig.screenHeightDp+", origConfig.screenWidthDp ="+ origConfig.screenWidthDp );
            context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics());
        }
    }
	//prize-added by tangan-20190327-adjust display size-end


    /*prize-modify-bugid:75124 Keep the app display size not changing with the system settings display size-xiaoping-20190426-start*/
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.densityDpi = getDefaultDisplayDensity(Display.DEFAULT_DISPLAY);
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }
    /*prize-modify-bugid:75124 Keep the app display size not changing with the system settings display size-xiaoping-20190426-end*/
}
