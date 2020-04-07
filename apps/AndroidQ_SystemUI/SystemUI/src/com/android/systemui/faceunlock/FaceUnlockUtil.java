/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.faceunlock;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.policy.IFaceUnlockCallback;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by lirenqi on 2018/02/01.
 */
public class FaceUnlockUtil {
    private static final boolean DEBUG = true;
    public final static String TAG = "FaceUnlockUtil";
    public final static String FACE_UNLOCK_VERSION = "v3.1-20181120";

    // used for handler messages
    private static final int FACE_UNLOCK_SHOW_UNLOCK_SCREEN = 0x01;
    private static final int FACE_UNLOCK_SHOW_MESSAGE = 0x02;
    private static final int FACE_UNLOCK_RETRY = 0x03;
    private static final int FACE_UNLOCK_LAUNCH_CAMERA = 0x04;
    private static final int FACE_UNLOCK_KEYGUARD_DONE = 0x05;
    private static final int FACE_UNLOCK_UNPDATE_LOCK_ICON = 0x06;

    private static final int FACE_UNLOCK_DELAY_TIME_MS = 500;

    private static final int FACE_UNLOCK_STATUS_NONE = 0x11;
    public static final int FACE_UNLOCK_STATUS_UNLOCK = 0x12;
    public static final int FACE_UNLOCK_STATUS_FAILED = 0x13; //19
    public static final int FACE_UNLOCK_STATUS_COUNT = 0x14;

    public static final int FACE_UNLOCK_RETRY_IN_KEYGUARD = 0x01;
    public static final int FACE_UNLOCK_RETRY_IN_PASSWORD = 0x02;

    private static final int SHOW_TIPS_REASON_START = 0x01;
    private static final int SHOW_TIPS_REASON_FAILED = 0x02;
    private static final int SHOW_TIPS_REASON_COUNT = 0x03;

    private static FaceUnlockUtil mInstance;
    private static Context mContext;
    private static IFaceUnlockCallback mFaceUnlockCallback=null;
    private static String mShowString=null;

    private static StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private static KeyguardIndicationController mKeyguardIndicationController;
    private static KeyguardBottomAreaView mKeyguardBottomArea;
    private static LockIcon mLockIcon;

    private static int mFaceUnlockStatus = FACE_UNLOCK_STATUS_NONE;
    private static int mFaceUnlockRetryCount = 0;
    private static final int FACE_UNLOCK_MAX_NUM = 3;

    private static boolean mSim1Locked = false;
    private static boolean mSim2Locked = false;
    private static boolean mFingerprintLocked = false;
    private static String mLastCameraLaunchSource = KeyguardBottomAreaView.CAMERA_LAUNCH_SOURCE_AFFORDANCE;

    private static boolean mOnCenterIcon = false;
    private static boolean mKeyguardUnlocked = false;
    private static AnimationDrawable animationDrawable;

    private static long mLastTime = 0L;
    private static PowerManager.WakeLock mWakeLock = null;

    private FaceUnlockUtil(){
    }

    public static FaceUnlockUtil getInstance(){
        if(mInstance==null){
            mInstance=new FaceUnlockUtil();
        }
        return mInstance;
    }

    public void doUnlock() {
        printLog("doUnlock");
        if (DEBUG) Slog.d("LockIcon", "doUnlock");
        mHandler.sendEmptyMessage(FACE_UNLOCK_KEYGUARD_DONE);
    }

    private void keyguardDone() {
        if (null != mStatusBarKeyguardViewManager) {
            mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(false);
        }
    }

    public void registerFaceUnlockCallback(IFaceUnlockCallback callback) {
        printLog("registerFaceUnlockCallback, callback = " + callback);
        mFaceUnlockCallback = callback;
    }

    public void showTipsOrAnimation(int reason, String tips) {
        printLog("showTipsOrAnimation reason = " + reason + "; tips = " + tips);
        if (reason == SHOW_TIPS_REASON_START) {
            mFaceUnlockStatus = FACE_UNLOCK_STATUS_UNLOCK;
            mShowString = "";
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_SHOW_MESSAGE,FACE_UNLOCK_DELAY_TIME_MS);
        } else if (reason == SHOW_TIPS_REASON_FAILED) {
            mFaceUnlockStatus = FACE_UNLOCK_STATUS_FAILED;
            if (unlockingAllowed()) {
                mShowString = tips;
            }else{
                mShowString = "";
            }
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_SHOW_MESSAGE,FACE_UNLOCK_DELAY_TIME_MS);
        } else if (reason == SHOW_TIPS_REASON_COUNT) {
            mFaceUnlockStatus = FACE_UNLOCK_STATUS_COUNT;
            mHandler.sendEmptyMessage(FACE_UNLOCK_UNPDATE_LOCK_ICON);
            mShowString = tips;
        } else {
            mFaceUnlockStatus = FACE_UNLOCK_STATUS_NONE;
            mHandler.sendEmptyMessage(FACE_UNLOCK_UNPDATE_LOCK_ICON);
            mShowString = null;
        }
    }

    private void showUnLockScreenAgain() {
        boolean unlockingAllowed = unlockingAllowed();
        printLog("showUnLockScreenAgain mFaceUnlockStatus = " + mFaceUnlockStatus + " unlockingAllowed = " + unlockingAllowed);
        if(unlockingAllowed){
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_SHOW_MESSAGE, FACE_UNLOCK_DELAY_TIME_MS);
        }
    }

    private void updateMsgIcon(String message, ColorStateList color) {
        printLog("updateMsgIcon isBouncerShowing = " + mStatusBarKeyguardViewManager.isBouncerShowing() +"updateMsgIcon message = " + message);
        if(mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.isBouncerShowing() && unlockingAllowed()) {//上滑解锁

            mStatusBarKeyguardViewManager.showBouncerMessage(message, color);
            runKeyguardAnimate();
        } else {
            if (mKeyguardIndicationController != null && message!=null && !message.isEmpty() && unlockingAllowed()) { // 锁屏界面
                mKeyguardIndicationController.setVisible(true);
                mKeyguardIndicationController.showTransientIndication(message, color);
            } else {
                mKeyguardIndicationController.hideTransientIndication();
            }

            if (mLockIcon != null) {
                mLockIcon.update(true);
            }
        }
    }
    /**
     * @see #FACE_UNLOCK_SHOW_UNLOCK_SCREEN
     */
     //刷新提示语,主要解决上滑 没有显示提示语问题
    private void handleShowUnlockScreen() {
        synchronized(this) {
            printLog("handleShowUnlockScreen isBouncerShowing()=" +mStatusBarKeyguardViewManager.isBouncerShowing() + " mShowString="+mShowString);
            if (null != mStatusBarKeyguardViewManager && !mStatusBarKeyguardViewManager.isBouncerShowing()) {
                mStatusBarKeyguardViewManager.animateCollapsePanels(1.3f);
            }
            mHandler.removeMessages(FACE_UNLOCK_SHOW_UNLOCK_SCREEN);
            if (null != mShowString) {
                mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_SHOW_MESSAGE, FACE_UNLOCK_DELAY_TIME_MS);
            }
        }
    }

    /**
     * @see #FACE_UNLOCK_SHOW_MESSAGE
     */
    private void handleShowMessage() {
        synchronized(this) {
            if (mShowString != null && unlockingAllowed()) {
                printLog("handleShowMessage mShowString = :" + mShowString);
                ColorStateList mColor = ColorStateList.valueOf(Color.WHITE);
                updateMsgIcon(mShowString,mColor);
            }
            mHandler.removeMessages(FACE_UNLOCK_SHOW_MESSAGE);
        }
    }

    /**
     * @see #FACE_UNLOCK_LAUNCH_CAMERA
     */
    private void handleLaunchCamera() {
        printLog("handleLaunchCamera = "+mFaceUnlockRetryCount +",mLastCameraLaunchSource ="+mLastCameraLaunchSource);
        if (mFaceUnlockRetryCount != FACE_UNLOCK_MAX_NUM) {
            return;
        }
        synchronized(this) {
            if (mKeyguardBottomArea != null && mLastCameraLaunchSource != null) {
                mKeyguardBottomArea.launchCamera(mLastCameraLaunchSource);
            }
            mHandler.removeMessages(FACE_UNLOCK_LAUNCH_CAMERA);
        }
    }

    /**
     * @see #FACE_UNLOCK_UNPDATE_LOCK_ICON
     */
    private void handleUpdateLockIcon() {
        printLog("handleUpdateLockIcon mShowString"+mShowString);
        synchronized(this) {
            if (mLockIcon != null) {
                mLockIcon.update(true);
            }
            mHandler.removeMessages(FACE_UNLOCK_UNPDATE_LOCK_ICON);
            mHandler.sendEmptyMessage(FACE_UNLOCK_SHOW_UNLOCK_SCREEN);
        }
    }

    /**
     * This handler will be associated with the policy thread, which will also
     * be the UI thread of the keyguard.  Since the apis of the policy, and therefore
     * this class, can be called by other threads, any action that directly
     * interacts with the keyguard ui should be posted to this handler, rather
     * than called directly.
     */
    private Handler mHandler = new Handler(Looper.myLooper(), null, true /*async*/) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FACE_UNLOCK_SHOW_UNLOCK_SCREEN:
                    handleShowUnlockScreen();
                    break;
                case FACE_UNLOCK_SHOW_MESSAGE:
                    handleShowMessage();
                    break;
                case FACE_UNLOCK_RETRY:
                    startFaceUnlockBefore();
                    break;
                case FACE_UNLOCK_LAUNCH_CAMERA:
                    handleLaunchCamera();
                    break;
                case FACE_UNLOCK_KEYGUARD_DONE:
                    keyguardDone();
                    break;
                case FACE_UNLOCK_UNPDATE_LOCK_ICON:
                    handleUpdateLockIcon();
                    break;
            }
        }
    };

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        if (isFaceUnlockSupport()) {
            printLog("setStatusBarKeyguardViewManager");
            mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        }
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        if (isFaceUnlockSupport()) {
            printLog("setKeyguardIndicationController");
            mKeyguardIndicationController = keyguardIndicationController;
        }
    }

    public void setKeyguardBottomAreaView(KeyguardBottomAreaView keyguardBottomAreaView) {
        if (isFaceUnlockSupport()) {
            if (DEBUG) Slog.d(TAG, "setKeyguardBottomAreaView");
            mKeyguardBottomArea = keyguardBottomAreaView;
        }
    }

    public void setLockIcon(LockIcon lockIcon) {
        printLog("setLockIcon");
        mLockIcon = lockIcon;
    }


    private void setContext(Context context) {
        if (mContext == null && context != null) {
            printLog("setContext");
            mContext = context;
            PowerManager mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "FaceUnlock Lock");
            Settings.System.putString(mContext.getContentResolver(), "base_ver", FACE_UNLOCK_VERSION);
        }
    }

    private void startFaceUnlock() {
        boolean unlockingAllowed = unlockingAllowed();
        boolean isOwner = isOwner();
        boolean mSimLocked  = isSimLocked();
        
        //add by wangjian for EJQQQ-359 20191221 start
        if (getFaceUnlockOn() && isTopActivityCamera()) {
            String errTip = mContext.getResources().getString(R.string.faceunlock_err_for_camera_using);
            showTipsOrAnimation(SHOW_TIPS_REASON_FAILED, errTip);
            return;
        }
        //add by wangjian for EJQQQ-359 20191221 end
        printLog("startFaceUnlock mSimLocked = " + mSimLocked + "; mFaceUnlockCallback = " + mFaceUnlockCallback + "; unlockingAllowed = " + unlockingAllowed + "; isOwner = " + isOwner + "; mFingerprintLocked = " + mFingerprintLocked);
        if (mFaceUnlockCallback != null && !mSimLocked && !mFingerprintLocked && isOwner && unlockingAllowed) {
            try {
                mLastTime = System.currentTimeMillis();
                mWakeLock.acquire(30 * 1000);
                mFaceUnlockCallback.doScreenOn();
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception calling doScreenOn():", e);
            }
        }
    }

    private void startFaceUnlockBefore() {
        mHandler.removeMessages(FACE_UNLOCK_RETRY);
        long mTimeGap = System.currentTimeMillis() - mLastTime;
        
        printLog("startFaceUnlockBefore mFaceUnlockRetryCount = " + mFaceUnlockRetryCount + "; isOccluded = " + mStatusBarKeyguardViewManager.isOccluded());
        if (mTimeGap < FACE_UNLOCK_DELAY_TIME_MS*3) {
            if (DEBUG) Slog.d(TAG, "startFaceUnlockBefore mTimeGap = " + mTimeGap);
            printLog("startFaceUnlockBefore mTimeGap = " + mTimeGap);
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_RETRY,FACE_UNLOCK_DELAY_TIME_MS);
        } else if (mFaceUnlockRetryCount < FACE_UNLOCK_MAX_NUM) {
            if (mStatusBarKeyguardViewManager != null && !mStatusBarKeyguardViewManager.isOccluded()) {
                startFaceUnlock();
            } else {
                mFaceUnlockRetryCount ++;
                mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_RETRY,FACE_UNLOCK_DELAY_TIME_MS);
            }
        } else {
            
            printLog("face unlock failed!");
        }
    }

    private boolean isSimLocked() {
        printLog("mSim1Locked = " + mSim1Locked + "; mSim2Locked = " + mSim2Locked);
        return mSim1Locked || mSim2Locked;
    }

    public void setSimLockState(int phoneId, IccCardConstants.State simState) {
        printLog("setSimLockState simState = " + simState + "; phoneId = " + phoneId);
        if(simState == IccCardConstants.State.PIN_REQUIRED
                || simState == IccCardConstants.State.PUK_REQUIRED) {
            if (phoneId == 1) {
                mSim2Locked = true;
            } else {
                mSim1Locked = true;
            }
            doUnlockSuccess();
        } else if (simState == IccCardConstants.State.READY
                || simState == IccCardConstants.State.NOT_READY
                || simState == IccCardConstants.State.PERM_DISABLED
                || simState == IccCardConstants.State.ABSENT
                || simState == IccCardConstants.State.NETWORK_LOCKED
                || simState == IccCardConstants.State.CARD_IO_ERROR){
            if (phoneId == 1) {
                mSim2Locked = false;
            } else {
                mSim1Locked = false;
            }
        }
    }

    public void setFingerprintLocked(int msgId) {
        
        printLog("setFingerprintLocked msgId = " + msgId);
        if(msgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
            doUnlockSuccess();
            mFingerprintLocked = true;
        } else {
            mFingerprintLocked = false;
        }
    }

    public void resetPowerKey() {
        if (mContext != null) {
            Settings.System.putInt(mContext.getContentResolver(), "faceunlock_start", 0);
        }
    }
//"power_unlock" 为1则为正常录入\ 打开人脸. 返回true
    public boolean getPowerUnlock() {
        if (mContext != null) {
            return (Settings.System.getInt(mContext.getContentResolver(), "power_unlock", 0) == 1);
        }
        return false;
    }
    //是否打开人脸
    public boolean getFaceUnlockOn() {
        if (mContext != null) {
            printLog("getFaceUnlockOn = "+Settings.System.getInt(mContext.getContentResolver(), "faceunlock_turn_on", 0));
            return (Settings.System.getInt(mContext.getContentResolver(), "faceunlock_turn_on", 0) == 1);
        }
        return false;
    }



    public void doScreenOn() {
        if (mFaceUnlockCallback != null) {
            boolean mStart = false;
            mFaceUnlockRetryCount = 0;
            if (mContext != null) {
                //"faceunlock_start"为 wakeUp方法里面设置的,如果是按键则为1,否则为0
                mStart = (Settings.System.getInt(mContext.getContentResolver(), "faceunlock_start", 0) == 1);
                resetPowerKey();
            }
            boolean mPowerUnlock = getPowerUnlock();
            
            printLog("doScreenOn mStart = " + mStart + "; mFaceUnlockStatus = " + mFaceUnlockStatus + "; isShowing = " + mStatusBarKeyguardViewManager.isShowing() + "; mPowerUnlock = " + mPowerUnlock);
            if (mStart && mPowerUnlock && mFaceUnlockStatus == FACE_UNLOCK_STATUS_NONE && mStatusBarKeyguardViewManager.isShowing()) {
                startFaceUnlockBefore();
            } else {
                mFaceUnlockRetryCount = FACE_UNLOCK_MAX_NUM;
                printLog("cann't start face unlock!!");
            }
        }
    }

    public void doScreenOff() {
        if (mFaceUnlockCallback != null) {
            
            printLog("doScreenOff " + FACE_UNLOCK_VERSION);
            if (!faceUnlockLockOut()) {
                mFaceUnlockStatus = FACE_UNLOCK_STATUS_NONE;
            }
            try {
                mFaceUnlockCallback.doScreenOff();
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception calling doScreenOff():", e);
            }
            mKeyguardUnlocked = false;
            mShowString = "";
            resetPowerKey();
            if (mLockIcon != null) {
                mLockIcon.update(true);
            }
        }
    }

    public void doUnlockSuccess() {
        if (mFaceUnlockCallback != null) {
            
            printLog("doUnlockSuccess");
            if (DEBUG) Slog.d("LockIcon", "doUnlockSuccess");
            try {
                mFaceUnlockCallback.doUnlockSuccess();
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception calling doUnlockSuccess():", e);
            }
        //解决5次密码输入失败后图标消失问题
            if (null != mLockIcon && unlockingAllowed() && !mFingerprintLocked) {
                mLockIcon.setVisibility(View.GONE);
            }
            //解决5次人脸识别失败后双击power键又可以使用人脸解锁问题
            printLog("!faceUnlockLockOut() = "+!faceUnlockLockOut() +": mKeyguardUnlocked="+mKeyguardUnlocked);
                if (!faceUnlockLockOut() || mKeyguardUnlocked){
                    mFaceUnlockStatus = FACE_UNLOCK_STATUS_NONE;
                }
            mFingerprintLocked = false;
            mShowString = "";
            resetPowerKey();
            if (mLockIcon != null) {
                mLockIcon.update(true);
            }

            if (null != mWakeLock && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void startFaceUnlockBySlide() {

        printLog("startFaceUnlockBySlide isFaceUnlockBySlideSupport()= "+isFaceUnlockBySlideSupport()+":mFaceUnlockStatus="+mFaceUnlockStatus +":mShowString ="+mShowString);
        if (isFaceUnlockBySlideSupport() && (mFaceUnlockStatus == FACE_UNLOCK_STATUS_NONE)) {

            startFaceUnlock();
        }
        //解决人脸识别失败后 锁屏页面双击通知的解锁页面没有提示语/5次解锁失败后解锁界面没有提示语 /5次解锁失败后上划解锁界面闪现提示?
        if (unlockingAllowed() && mShowString != null) {
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_SHOW_UNLOCK_SCREEN,FACE_UNLOCK_DELAY_TIME_MS);
        }


    }

    public boolean startFaceUnlockByRetry(int style) {
        
        printLog("startFaceUnlockByRetry getPowerUnlock()= "+getPowerUnlock() +":faceUnlockFailed()="+faceUnlockFailed()+":mOnCenterIcon="+mOnCenterIcon+ ":style ="+style);
        if (faceUnlockFailed()
                && ((mOnCenterIcon && (style == FACE_UNLOCK_RETRY_IN_KEYGUARD) && getPowerUnlock())
                || (mOnCenterIcon && (style == FACE_UNLOCK_RETRY_IN_PASSWORD)))) {
            
            printLog("startFaceUnlockByRetry" + "; style = " + style);
            if (null != mWakeLock && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            startFaceUnlock();
            return true;
        }
        return false;
    }

    public static boolean isFaceUnlockSupport() {
        String support = SystemProperties.get("ro.faceunlock.support", "0");
        return "1".equals(support);
    }

    public static boolean isFaceUnlockBySlideSupport() {
        String support = SystemProperties.get("ro.faceunlock.slide_support", "1");
        return isFaceUnlockSupport() &&"1".equals(support);
    }

    public static boolean isIsBouncerShow(){
        return mStatusBarKeyguardViewManager.isBouncerShowing();
    }
    public void startFaceUnlockService(Context context) {
        if (isFaceUnlockSupport()) {
            
            printLog("startFaceUnlockService");
            setContext(context);
            Intent intent = new Intent(context, FaceUnlockService.class);
            context.startServiceAsUser(intent, UserHandle.CURRENT);
        }
    }

    public boolean faceUnlockBeginning() {
        if (mFaceUnlockStatus == FACE_UNLOCK_STATUS_NONE) {
            return true;
        }
        return false;
    }

    private boolean faceUnlockRunning() {
        if (mFaceUnlockStatus == FACE_UNLOCK_STATUS_UNLOCK) {
            return true;
        }
        return false;
    }

    private boolean faceUnlockFailed() {
        if (mFaceUnlockStatus == FACE_UNLOCK_STATUS_FAILED) {
            return true;
        }
        return false;
    }

    public boolean faceUnlockLockOut() {
        if (mFaceUnlockStatus == FACE_UNLOCK_STATUS_COUNT) {
            return true;
        }
        return false;
    }

    public boolean unlockingAllowed() {
        printLog("unlockingAllowed() = "+KeyguardUpdateMonitor.getInstance(mContext).isUnlockingWithBiometricAllowed());
        return (mContext != null)
                && (KeyguardUpdateMonitor.getInstance(mContext).isUnlockingWithBiometricAllowed());
    }

    public boolean launchCamera(String source) {
        if (isFaceUnlockSupport() && getFaceUnlockOn()) {
            
            printLog("launchCamera source = " + source);
            mHandler.removeMessages(FACE_UNLOCK_RETRY);
            doUnlockSuccess();
            mFaceUnlockRetryCount = FACE_UNLOCK_MAX_NUM;
            mLastCameraLaunchSource = source;
            mHandler.sendEmptyMessageDelayed(FACE_UNLOCK_LAUNCH_CAMERA,FACE_UNLOCK_DELAY_TIME_MS);
            return true;
        }
        return false;
    }

    public boolean startCamera(int source) {
        if (source == StatusBarManager.CAMERA_LAUNCH_SOURCE_POWER_DOUBLE_TAP
                && launchCamera(KeyguardBottomAreaView.CAMERA_LAUNCH_SOURCE_POWER_DOUBLE_TAP)) {
            return true;
        }
        return false;
    }

    private static boolean isOwner() {
        boolean isSecondaryUser = (UserHandle.myUserId() != UserHandle.USER_OWNER) ||
                (ActivityManager.getCurrentUser() != UserHandle.USER_OWNER);
        return !isSecondaryUser;
    }



    public void setOnCenterIconPosition(float x, float y){
        
        printLog("setOnCenterIconPosition mOnCenterIcon=" + mOnCenterIcon +" :mLockIcon.getVisibility()="+mLockIcon.getVisibility());
        if (mLockIcon != null && mLockIcon.getVisibility() == View.VISIBLE) {
            int[] location = new int[2];
            mLockIcon.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + mLockIcon.getMeasuredWidth();
            int bottom = top + mLockIcon.getMeasuredHeight();
            if (y >= top && y <= bottom && x >= left
                    && x <= right) {
                mOnCenterIcon = true;
                return ;
            }else {
                mOnCenterIcon = false;
            }
        }
        mOnCenterIcon = false;
    }
    public boolean isKeyguardUnlocked(boolean locked) {
        if (mFaceUnlockCallback == null) {
            return false;
        }
        if (locked || !isFaceUnlockSupport()) {
            return false;
        } else if (mKeyguardUnlocked) {
            return true;
        } else {
            mKeyguardUnlocked = true;
            return false;
        }
    }

    private boolean animationRun(boolean isLockIcon) {
        
        printLog("animationRun faceUnlockRunning()" +faceUnlockRunning()+"isLockIcon:"+isLockIcon);
        if (faceUnlockRunning() && mContext != null) {
            animationDrawable = (AnimationDrawable) mContext
                    .getDrawable(R.drawable.face_unlock_anim);
            if (isLockIcon) {
                mLockIcon.setImageDrawable(animationDrawable);
            } else {
                if (mLockIcon != null) {
                    mLockIcon.update(true);
                }
            }

            if (animationDrawable != null
                    && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
            return true;
        }
        return false;
    }

    private boolean animationStop(boolean isLockIcon) {
        
        printLog("animationStop faceUnlockFailed()"+faceUnlockFailed());
        if (faceUnlockFailed() && mContext != null) {
            if (isLockIcon) {
                mLockIcon.setImageDrawable(mContext
                        .getDrawable(R.drawable.faceunlock_bg));
            }

            if (animationDrawable != null
                    && animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            return true;
        }
        return false;
    }

    public boolean setLockIconBg(boolean showMsg) {
        
        printLog("setLockIconBg getPowerUnlock()" + getPowerUnlock()+":mFaceUnlockStatus:"+mFaceUnlockStatus+":showMsg:"+showMsg);
        if (getPowerUnlock() && mLockIcon != null) {
            if (animationRun(true)) {
                return true;
            } else if (animationStop(true)) {
                if (showMsg) {
                    //if (DEBUG) Slog.d(TAG, "animationStop showMsg" + showMsg);
                    printLog("animationStop showMsg" + showMsg);
                    showUnLockScreenAgain();
                }
                return true;
            }
        }
        return false;
    }

    public void setLockIconBg() {
        if (mLockIcon != null) {
            mLockIcon.setImageDrawable(mContext
                    .getDrawable(R.drawable.faceunlock_bg));
        }
    }




    public void runKeyguardAnimate() {
        
        printLog("runKeyguardAnimate mShowString ="+mShowString);
        if (mLockIcon != null) {
            if (animationRun(true)) {

            } else if (animationStop(true)) {
                mLockIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnCenterIcon = true;
                        startFaceUnlockByRetry(FACE_UNLOCK_RETRY_IN_PASSWORD);
                    }
                });
            } else if (faceUnlockLockOut()) {
                showUnLockScreenAgain();
                
                printLog("runKeyguardAnimate faceUnlockLockOut:" + faceUnlockLockOut());
            }
        }
    }

    public void hideKeyguardBouncer() {
        printLog("hideKeyguardBouncer");
        if (mLockIcon != null) {
            showUnLockScreenAgain();
        }

    }

    public String getMessage() {
        if (faceUnlockFailed() && mShowString != null) {
            return mShowString;
        }
        return null;
    }

    //是否是5次解锁失败状态
    public boolean isFaceUnlockStatusCount(){
        return mFaceUnlockStatus == FACE_UNLOCK_STATUS_COUNT;
    }
	  //减少代码      StatusBarKeyguardViewManager
    public boolean isAllowSetLockIconBg(){
        return isFaceUnlockSupport() && getFaceUnlockOn() && unlockingAllowed() && !faceUnlockLockOut() && getPowerUnlock();

    }
    //减少代码      LockIcon
    public boolean isAllowSetSystemLockIconBg(){
        return isFaceUnlockStatusCount() || !getFaceUnlockOn() || !unlockingAllowed() || (!getPowerUnlock() && !isIsBouncerShow());
    }
    public static void printLog(String msg) {
        if (DEBUG) Slog.d(TAG, msg);
    }


    //add by wangjian for EJQQQ-359 20191221 start
    private boolean isTopActivityCamera() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        String topActivity = "";
        String topClass = "";
        if (runningTasks != null && !runningTasks.isEmpty()) {
            ActivityManager.RunningTaskInfo taskInfo = runningTasks.get(0);
            topActivity = taskInfo.topActivity.getPackageName();
            topClass = taskInfo.topActivity.getClassName();
            if (DEBUG) Slog.d(TAG, "isTopActivityCamera topActivity = " + topActivity);
            if ("com.android.camera".equals(topActivity) || "com.wheatek.qrscanner".equals(topActivity) && "com.wheatek.qrscanner.activity.ScanActivity".equals(topClass)
                || "com.mediatek.camera".equals(topActivity)) {
                return true;
            }
        }
        return false;
    }
    //add by wangjian for EJQQQ-359 20191221 end
}
