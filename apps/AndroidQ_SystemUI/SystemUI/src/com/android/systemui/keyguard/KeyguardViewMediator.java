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

package com.android.systemui.keyguard;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import static com.android.internal.telephony.IccCardConstants.State.ABSENT;
import static com.android.internal.telephony.IccCardConstants.State.PIN_REQUIRED;
import static com.android.internal.telephony.IccCardConstants.State.PUK_REQUIRED;
import static com.android.internal.widget.LockPatternUtils.StrongAuthTracker.SOME_AUTH_REQUIRED_AFTER_USER_REQUEST;
import static com.android.internal.widget.LockPatternUtils.StrongAuthTracker.STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW;
import static com.android.internal.widget.LockPatternUtils.StrongAuthTracker.STRONG_AUTH_REQUIRED_AFTER_LOCKOUT;
import static com.android.internal.widget.LockPatternUtils.StrongAuthTracker.STRONG_AUTH_REQUIRED_AFTER_TIMEOUT;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricSourceType;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.IWindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardConstants;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.R;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.classifier.FalsingManagerFactory;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.util.InjectionInflationController;

import com.mediatek.keyguard.AntiTheft.AntiTheftManager;
import com.mediatek.keyguard.Telephony.KeyguardDialogManager;
import com.mediatek.keyguard.VoiceWakeup.VoiceWakeupManagerProxy;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

// add BUG_ID:TBSW-52 liuzhijun 20180131 (start)
import android.net.Uri;
import android.database.ContentObserver;
// add BUG_ID:TBSW-52 liuzhijun 20180131 (end)
//FACE_UNLOCK_SUPPORT start
import com.android.systemui.faceunlock.FaceUnlockUtil;
//FACE_UNLOCK_SUPPORT end

/**
 * Mediates requests related to the keyguard.  This includes queries about the
 * state of the keyguard, power management events that effect whether the keyguard
 * should be shown or reset, callbacks to the phone window manager to notify
 * it of when the keyguard is showing, and events from the keyguard view itself
 * stating that the keyguard was succesfully unlocked.
 *
 * Note that the keyguard view is shown when the screen is off (as appropriate)
 * so that once the screen comes on, it will be ready immediately.
 *
 * Example queries about the keyguard:
 * - is {movement, key} one that should wake the keygaurd?
 * - is the keyguard showing?
 * - are input events restricted due to the state of the keyguard?
 *
 * Callbacks to the phone window manager:
 * - the keyguard is showing
 *
 * Example external events that translate to keyguard view changes:
 * - screen turned off -> reset the keyguard, and show it so it will be ready
 *   next time the screen turns on
 * - keyboard is slid open -> if the keyguard is not secure, hide it
 *
 * Events from the keyguard view:
 * - user succesfully unlocked keyguard -> hide keyguard view, and no longer
 *   restrict input events.
 *
 * Note: in addition to normal power managment events that effect the state of
 * whether the keyguard should be showing, external apps and services may request
 * that the keyguard be disabled via {@link #setKeyguardEnabled(boolean)}.  When
 * false, this will override all other conditions for turning on the keyguard.
 *
 * Threading and synchronization:
 * This class is created by the initialization routine of the {@link WindowManagerPolicyConstants},
 * and runs on its thread.  The keyguard UI is created from that thread in the
 * constructor of this class.  The apis may be called from other threads, including the
 * {@link com.android.server.input.InputManagerService}'s and {@link android.view.WindowManager}'s.
 * Therefore, methods on this class are synchronized, and any action that is pointed
 * directly to the keyguard UI is posted to a {@link android.os.Handler} to ensure it is taken on the UI
 * thread of the keyguard.
 */
public class KeyguardViewMediator extends SystemUI {
    private static final int KEYGUARD_DISPLAY_TIMEOUT_DELAY_DEFAULT = 30000;
    private static final long KEYGUARD_DONE_PENDING_TIMEOUT_MS = 3000;

    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private static final boolean DEBUG_SIM_STATES = KeyguardConstants.DEBUG_SIM_STATES;

    private final static String TAG = "KeyguardViewMediator";

    private static final String DELAYED_KEYGUARD_ACTION =
        "com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD";
    private static final String DELAYED_LOCK_PROFILE_ACTION =
            "com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK";

    private static final String SYSTEMUI_PERMISSION = "com.android.systemui.permission.SELF";

    // used for handler messages
    private static final int SHOW = 1;
    private static final int HIDE = 2;
    private static final int RESET = 3;
    private static final int VERIFY_UNLOCK = 4;
    private static final int NOTIFY_FINISHED_GOING_TO_SLEEP = 5;
    private static final int NOTIFY_SCREEN_TURNING_ON = 6;
    private static final int KEYGUARD_DONE = 7;
    private static final int KEYGUARD_DONE_DRAWING = 8;
    private static final int SET_OCCLUDED = 9;
    private static final int KEYGUARD_TIMEOUT = 10;
    private static final int DISMISS = 11;
    private static final int START_KEYGUARD_EXIT_ANIM = 12;
    private static final int KEYGUARD_DONE_PENDING_TIMEOUT = 13;
    private static final int NOTIFY_STARTED_WAKING_UP = 14;
    private static final int NOTIFY_SCREEN_TURNED_ON = 15;
    private static final int NOTIFY_SCREEN_TURNED_OFF = 16;
    private static final int NOTIFY_STARTED_GOING_TO_SLEEP = 17;
    private static final int SYSTEM_READY = 18;
    // M: for internal feature dismiss keyguard
    private static final int DISMISS_INTERNAL = 19;

    /**
     * The default amount of time we stay awake (used for all key input)
     */
    public static final int AWAKE_INTERVAL_DEFAULT_MS = 10000;

    /**
     * How long to wait after the screen turns off due to timeout before
     * turning on the keyguard (i.e, the user has this much time to turn
     * the screen back on without having to face the keyguard).
     */
    private static final int KEYGUARD_LOCK_AFTER_DELAY_DEFAULT = 5000;

    /**
     * How long we'll wait for the {@link ViewMediatorCallback#keyguardDoneDrawing()}
     * callback before unblocking a call to {@link #setKeyguardEnabled(boolean)}
     * that is reenabling the keyguard.
     */
    private static final int KEYGUARD_DONE_DRAWING_TIMEOUT_MS = 2000;

    /**
     * Boolean option for doKeyguardLocked/doKeyguardTimeout which, when set to true, forces the
     * keyguard to show even if it is disabled for the current user.
     */
    public static final String OPTION_FORCE_SHOW = "force_show";

    /** The stream type that the lock sounds are tied to. */
    private int mUiSoundsStreamType;

    private AlarmManager mAlarmManager;
    private AudioManager mAudioManager;
    private StatusBarManager mStatusBarManager;
    private final UiOffloadThread mUiOffloadThread = Dependency.get(UiOffloadThread.class);

    private boolean mSystemReady;
    private boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private boolean mShuttingDown;

    /** High level access to the power manager for WakeLocks */
    private PowerManager mPM;

    /** TrustManager for letting it know when we change visibility */
    private TrustManager mTrustManager;

    /**
     * Used to keep the device awake while to ensure the keyguard finishes opening before
     * we sleep.
     */
    private PowerManager.WakeLock mShowKeyguardWakeLock;

    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;

    // these are protected by synchronized (this)

    /**
     * External apps (like the phone app) can tell us to disable the keygaurd.
     */
    private boolean mExternallyEnabled = true;

    /**
     * Remember if an external call to {@link #setKeyguardEnabled} with value
     * false caused us to hide the keyguard, so that we need to reshow it once
     * the keygaurd is reenabled with another call with value true.
     */
    private boolean mNeedToReshowWhenReenabled = false;

    // cached value of whether we are showing (need to know this to quickly
    // answer whether the input should be restricted)
    private boolean mShowing;

    // AOD is enabled and status bar is in AOD state.
    private boolean mAodShowing;

    /** Cached value of #isInputRestricted */
    private boolean mInputRestricted;

    // true if the keyguard is hidden by another window
    private boolean mOccluded = false;

    /**
     * Helps remember whether the screen has turned on since the last time
     * it turned off due to timeout. see {@link #onScreenTurnedOff(int)}
     */
    private int mDelayedShowingSequence;

    /**
     * Simiar to {@link #mDelayedProfileShowingSequence}, but it is for profile case.
     */
    private int mDelayedProfileShowingSequence;

    /**
     * If the user has disabled the keyguard, then requests to exit, this is
     * how we'll ultimately let them know whether it was successful.  We use this
     * var being non-null as an indicator that there is an in progress request.
     */
    private IKeyguardExitCallback mExitSecureCallback;
    private final DismissCallbackRegistry mDismissCallbackRegistry = new DismissCallbackRegistry();

    // the properties of the keyguard

    private KeyguardUpdateMonitor mUpdateMonitor;

    /**
     * Last SIM state reported by the telephony system.
     * Index is the slotId - in case of multiple SIM cards.
     */
    private final SparseArray<IccCardConstants.State> mLastSimStates = new SparseArray<>();

    private boolean mDeviceInteractive;
    private boolean mGoingToSleep;

    // last known state of the cellular connection
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;

    /**
     * Whether a hide is pending an we are just waiting for #startKeyguardExitAnimation to be
     * called.
     * */
    private boolean mHiding;

    /**
     * we send this intent when the keyguard is dismissed.
     */
    private static final Intent USER_PRESENT_INTENT = new Intent(Intent.ACTION_USER_PRESENT)
            .addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING
                    | Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                    | Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS);

    /**
     * {@link #setKeyguardEnabled} waits on this condition when it reenables
     * the keyguard.
     */
    private boolean mWaitingUntilKeyguardVisible = false;
    private LockPatternUtils mLockPatternUtils;
    private boolean mKeyguardDonePending = false;
    private boolean mHideAnimationRun = false;
    private boolean mHideAnimationRunning = false;

    private SoundPool mLockSounds;
    private int mLockSoundId;
    private int mUnlockSoundId;
    private int mTrustedSoundId;
    private int mLockSoundStreamId;

    /**
     * The animation used for hiding keyguard. This is used to fetch the animation timings if
     * WindowManager is not providing us with them.
     */
    private Animation mHideAnimation;

    /**
     * The volume applied to the lock/unlock sounds.
     */
    private float mLockSoundVolume;

    /**
     * For managing external displays
     */
    private KeyguardDisplayManager mKeyguardDisplayManager;

    private final ArrayList<IKeyguardStateCallback> mKeyguardStateCallbacks = new ArrayList<>();

    /**
     * When starting going to sleep, we figured out that we need to reset Keyguard state and this
     * should be committed when finished going to sleep.
     */
    private boolean mPendingReset;

    /**
     * When starting going to sleep, we figured out that we need to lock Keyguard and this should be
     * committed when finished going to sleep.
     */
    private boolean mPendingLock;

    /**
     * Controller for showing individual "work challenge" lock screen windows inside managed profile
     * tasks when the current user has been unlocked but the profile is still locked.
     */
    private WorkLockActivityController mWorkLockController;

    /**
     * @see #setPulsing(boolean)
     */
    private boolean mPulsing;

    private boolean mLockLater;

    private boolean mWakeAndUnlocking;
    private IKeyguardDrawnCallback mDrawnCallback;
    private CharSequence mCustomMessage;
    //add by wangjian for EJSLYQ-94 20200107 start
    private LockIcon mLockIcon;
    //add by wangjian for EJSLYQ-94 20200107 end

    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onUserSwitching(int userId) {
            // Note that the mLockPatternUtils user has already been updated from setCurrentUser.
            // We need to force a reset of the views, since lockNow (called by
            // ActivityManagerService) will not reconstruct the keyguard if it is already showing.
            synchronized (KeyguardViewMediator.this) {
                resetKeyguardDonePendingLocked();
                if (mLockPatternUtils.isLockScreenDisabled(userId)) {
                    // If we switching to a user that has keyguard disabled, dismiss keyguard.
                    dismiss(null /* callback */, null /* message */);
                } else {
                    resetStateLocked();
                }
                adjustStatusBarLocked();
            }
        }

        @Override
        public void onUserSwitchComplete(int userId) {
            if (userId != UserHandle.USER_SYSTEM) {
                UserInfo info = UserManager.get(mContext).getUserInfo(userId);
                // Don't try to dismiss if the user has Pin/Patter/Password set
                if (info == null || mLockPatternUtils.isSecure(userId)) {
                    return;
                } else if (info.isGuest() || info.isDemo()) {
                    // If we just switched to a guest, try to dismiss keyguard.
                    dismiss(null /* callback */, null /* message */);
                }
            }
        }

        @Override
        public void onUserInfoChanged(int userId) {
        }

        @Override
        public void onClockVisibilityChanged() {
            adjustStatusBarLocked();
        }

        @Override
        public void onDeviceProvisioned() {
            sendUserPresentBroadcast();
            synchronized (KeyguardViewMediator.this) {
                // If system user is provisioned, we might want to lock now to avoid showing launcher
                if (mustNotUnlockCurrentUser()) {
                    doKeyguardLocked(null);
                }
            }
        }

        @Override
        /*public void onSimStateChanged(int subId, int slotId, IccCardConstants.State simState) {

            if (DEBUG_SIM_STATES) {
                Log.d(TAG, "onSimStateChanged(subId=" + subId + ", slotId=" + slotId
                        + ",state=" + simState + ")");
            }

            int size = mKeyguardStateCallbacks.size();
            boolean simPinSecure = mUpdateMonitor.isSimPinSecure();
            for (int i = size - 1; i >= 0; i--) {
                try {
                    mKeyguardStateCallbacks.get(i).onSimSecureStateChanged(simPinSecure);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onSimSecureStateChanged", e);
                    if (e instanceof DeadObjectException) {
                        mKeyguardStateCallbacks.remove(i);
                    }
                }
            }*/
        public void onSimStateChangedUsingPhoneId(int phoneId, IccCardConstants.State simState) {
            if (DEBUG) {
                Log.d(TAG, "onSimStateChangedUsingSubId: " + simState + ", phoneId=" + phoneId);
            }
			//FACE_UNLOCK_SUPPORT start
            if (FaceUnlockUtil.isFaceUnlockSupport()) {
                FaceUnlockUtil.getInstance().setSimLockState(phoneId, simState);
            }
            //FACE_UNLOCK_SUPPORT end
            boolean simWasLocked;
            synchronized (KeyguardViewMediator.this) {
                IccCardConstants.State lastState = mLastSimStates.get(phoneId);
                simWasLocked = (lastState == PIN_REQUIRED || lastState == PUK_REQUIRED);
                mLastSimStates.append(phoneId, simState);
            }

            switch (simState) {
                case NOT_READY:
                case ABSENT:
                    // only force lock screen in case of missing sim if user hasn't
                    // gone through setup wizard
                    synchronized (KeyguardViewMediator.this) {
                        if (shouldWaitForProvisioning()) {
                            if (!mShowing) {
                                if (DEBUG_SIM_STATES) Log.d(TAG, "ICC_ABSENT isn't showing,"
                                        + " we need to show the keyguard since the "
                                        + "device isn't provisioned yet.");
                                doKeyguardLocked(null);
                            } else {
                                resetStateLocked();
                            }
                        }
                        if (simState == ABSENT) {
                            // MVNO SIMs can become transiently NOT_READY when switching networks,
                            // so we should only lock when they are ABSENT.
                            if (simWasLocked) {
                                if (DEBUG_SIM_STATES) Log.d(TAG, "SIM moved to ABSENT when the "
                                        + "previous state was locked. Reset the state.");
                                resetStateLocked();
                            }
                        }
                    }
                    break;
                case PIN_REQUIRED:
                case PUK_REQUIRED:
                ///M: add network lock
                case NETWORK_LOCKED:
                    /*synchronized (KeyguardViewMediator.this) {
                        if (!mShowing) {
                            if (DEBUG_SIM_STATES) Log.d(TAG,
                                    "INTENT_VALUE_ICC_LOCKED and keygaurd isn't "
                                    + "showing; need to show keyguard so user can enter sim pin");
                            doKeyguardLocked(null);
                        } else {
                            resetStateLocked();
                        }
                    }*/
                    synchronized (this) {
                        if ((simState == IccCardConstants.State.NETWORK_LOCKED) &&
                             !KeyguardUtils.isMediatekSimMeLockSupport()) {
                            Log.d(TAG, "Get NETWORK_LOCKED but not support ME lock. Not show.");
                            break;
                        }

                        if ((simState == IccCardConstants.State.NETWORK_LOCKED) &&
                            !KeyguardUtils.isSimMeLockValid(phoneId)) {
                            Log.d(TAG, "Get NETWORK_LOCKED but not to show with specific policy!");
                            break;
                        }

                        if ((simState == IccCardConstants.State.NETWORK_LOCKED) &&
                                KeyguardUtils.isUnlockSimMeWithDeviceSupport() &&
                                !KeyguardUtils.isShowSimMeLock(false)) {
                                Log.d(TAG, "Get NETWORK_LOCKED but more than SimMe time. Not show.");
                                break;
                        }

                        ///M: [ALPS02009053] avoid to show SIM pin view during decryption phase
                        if (KeyguardUtils.isSystemEncrypted()) {
                            if (DEBUG) {
                                Log.d(TAG, "Currently system needs to be decrypted. Not show.");
                            }
                            break;
                        }

                        mUpdateMonitor.setDismissFlagWhenWfcOn(simState);

                        /// M: if the puk retry count is zero, show the invalid dialog.
                        if (mUpdateMonitor.getRetryPukCountOfPhoneId(phoneId) == 0) {
                            mDialogManager.requestShowDialog(new InvalidDialogCallback());
                            break;
                        }

                        /// M: detected whether the SimME permanently locked,
                        ///    show the permanently locked dialog.
                        if (IccCardConstants.State.NETWORK_LOCKED == simState
                                    && 0 == mUpdateMonitor.getSimMeLeftRetryCountOfPhoneId(phoneId)
                            ) {
                            Log.d(TAG, "SIM ME lock retrycount is 0, only to show dialog");
                            mDialogManager.requestShowDialog(new MeLockedDialogCallback());
                            break;
                        }

                        if (!isShowing()) {
                            if (DEBUG) {
                                Log.d(TAG, "INTENT_VALUE_ICC_LOCKED and keygaurd isn't "
                                    + "showing; need to show keyguard so user can enter sim pin");
                            }
                            doKeyguardLocked(null);
                        } else if (mKeyguardDoneOnGoing) {
                            Log.d(TAG, "mKeyguardDoneOnGoing is true") ;
                            doKeyguardLaterLocked() ;
                        } else {
                            /// M: [ALPS01472600] to remove KEYGUARD_DONE in message queue,
                            removeKeyguardDoneMsg();
                            resetStateLocked();
                        }
                    }
                    break;
                case PERM_DISABLED:
                    synchronized (KeyguardViewMediator.this) {
                        if (!mShowing) {
                            if (DEBUG_SIM_STATES) Log.d(TAG, "PERM_DISABLED and "
                                  + "keygaurd isn't showing.");
                            doKeyguardLocked(null);
                        } else {
                            if (DEBUG_SIM_STATES) Log.d(TAG, "PERM_DISABLED, resetStateLocked to"
                                  + "show permanently disabled message in lockscreen.");
                            resetStateLocked();
                        }
                    }
                    break;
                case READY:
                    synchronized (KeyguardViewMediator.this) {
                        if (DEBUG_SIM_STATES) Log.d(TAG, "READY, reset state? " + mShowing);
                        ///M : [ALPS01770528]
                        ///    Avoid flash notification keyguard after dismiss SIM Pin lock.
                        /*if (mShowing && simWasLocked) {
                            if (DEBUG_SIM_STATES) Log.d(TAG, "SIM moved to READY when the "
                                    + "previous state was locked. Reset the state.");
                            resetStateLocked();
                        }*/
                    }
                    break;
                default:
                    if (DEBUG_SIM_STATES) Log.v(TAG, "Unspecific state: " + simState);
                    break;
            }

            /// M: [ALPS01995091] update isSecure() state, after SIM dismissFlag is updated.
            try {
                int size = mKeyguardStateCallbacks.size();
                boolean simPinSecure = mUpdateMonitor.isSimPinSecure();
                for (int i = 0; i < size; i++) {
                    mKeyguardStateCallbacks.get(i).onSimSecureStateChanged(simPinSecure);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call onSimSecureStateChanged", e);
            }
        }

        @Override
        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (mLockPatternUtils.isSecure(currentUser)) {
                mLockPatternUtils.getDevicePolicyManager().reportFailedBiometricAttempt(
                        currentUser);
            }
        }

        @Override
        public void onBiometricAuthenticated(int userId, BiometricSourceType biometricSourceType) {
            if (mLockPatternUtils.isSecure(userId)) {
                mLockPatternUtils.getDevicePolicyManager().reportSuccessfulBiometricAttempt(
                        userId);
            }
        }

        @Override
        public void onTrustChanged(int userId) {
            if (userId == KeyguardUpdateMonitor.getCurrentUser()) {
                synchronized (KeyguardViewMediator.this) {
                    notifyTrustedChangedLocked(mUpdateMonitor.getUserHasTrust(userId));
                }
            }
        }

        @Override
        public void onHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
            synchronized (KeyguardViewMediator.this) {
                notifyHasLockscreenWallpaperChanged(hasLockscreenWallpaper);
            }
        }
    };

    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback() {

        @Override
        public void userActivity() {
            KeyguardViewMediator.this.userActivity();
        }

        @Override
        public void keyguardDone(boolean strongAuth, int targetUserId) {
            if (targetUserId != ActivityManager.getCurrentUser()) {
                return;
            }

            tryKeyguardDone();
        }

        @Override
        public void keyguardDoneDrawing() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDoneDrawing");
            mHandler.sendEmptyMessage(KEYGUARD_DONE_DRAWING);
            Trace.endSection();
        }

        @Override
        public void setNeedsInput(boolean needsInput) {
            mStatusBarKeyguardViewManager.setNeedsInput(needsInput);
        }

        @Override
        public void keyguardDonePending(boolean strongAuth, int targetUserId) {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDonePending");
            if (targetUserId != ActivityManager.getCurrentUser()) {
                Trace.endSection();
                return;
            }

            mKeyguardDonePending = true;
            mHideAnimationRun = true;
            mHideAnimationRunning = true;
            mStatusBarKeyguardViewManager.startPreHideAnimation(mHideAnimationFinishedRunnable);
            mHandler.sendEmptyMessageDelayed(KEYGUARD_DONE_PENDING_TIMEOUT,
                    KEYGUARD_DONE_PENDING_TIMEOUT_MS);
            Trace.endSection();
        }

        @Override
        public void keyguardGone() {
            //FACE_UNLOCK_SUPPORT start
            if (FaceUnlockUtil.isFaceUnlockSupport()) {
                FaceUnlockUtil.getInstance().doUnlockSuccess();
            }
            //FACE_UNLOCK_SUPPORT end
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardGone");
            mKeyguardDisplayManager.hide();
            mVoiceWakeupManager.notifyKeyguardIsGone();
            Trace.endSection();
        }

        @Override
        public void readyForKeyguardDone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#readyForKeyguardDone");
            if (mKeyguardDonePending) {
                mKeyguardDonePending = false;
                tryKeyguardDone();
            }
            Trace.endSection();
        }

        @Override
        public void resetKeyguard() {
            resetStateLocked();
        }

        @Override
        public void onCancelClicked() {
            mStatusBarKeyguardViewManager.onCancelClicked();
        }

        @Override
        public void onBouncerVisiblityChanged(boolean shown) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.adjustStatusBarLocked(shown);
            }
        }

        @Override
        public void playTrustedSound() {
            KeyguardViewMediator.this.playTrustedSound();
        }

        @Override
        public boolean isScreenOn() {
            return mDeviceInteractive;
        }

        @Override
        public int getBouncerPromptReason() {
            int currentUser = ActivityManager.getCurrentUser();
            boolean trust = mTrustManager.isTrustUsuallyManaged(currentUser);
            boolean biometrics = mUpdateMonitor.isUnlockingWithBiometricsPossible(currentUser);
            boolean any = trust || biometrics;
            KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker =
                    mUpdateMonitor.getStrongAuthTracker();
            int strongAuth = strongAuthTracker.getStrongAuthForUser(currentUser);

            if (any && !strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
                return KeyguardSecurityView.PROMPT_REASON_RESTART;
            } else if (any && (strongAuth & STRONG_AUTH_REQUIRED_AFTER_TIMEOUT) != 0) {
                return KeyguardSecurityView.PROMPT_REASON_TIMEOUT;
            } else if (any && (strongAuth & STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW) != 0) {
                return KeyguardSecurityView.PROMPT_REASON_DEVICE_ADMIN;
            } else if (trust && (strongAuth & SOME_AUTH_REQUIRED_AFTER_USER_REQUEST) != 0) {
                return KeyguardSecurityView.PROMPT_REASON_USER_REQUEST;
            } else if (any && (strongAuth & STRONG_AUTH_REQUIRED_AFTER_LOCKOUT) != 0) {
                return KeyguardSecurityView.PROMPT_REASON_AFTER_LOCKOUT;
            }
            return KeyguardSecurityView.PROMPT_REASON_NONE;
        }

        @Override
        public CharSequence consumeCustomMessage() {
            final CharSequence message = mCustomMessage;
            mCustomMessage = null;
            return message;
        }

        /// M: Mediatek Added
        /// M : added for AntiTheft callback @{
        @Override
        public boolean isShowing() {
            return KeyguardViewMediator.this.isShowing();
        }

        @Override
        public void showLocked(Bundle options) {
            KeyguardViewMediator.this.showLocked(options);
        }

        @Override
        public void resetStateLocked() {
            KeyguardViewMediator.this.resetStateLocked();
        }

        @Override
        public void adjustStatusBarLocked() {
            KeyguardViewMediator.this.adjustStatusBarLocked();
        }

        @Override
        public boolean isKeyguardDoneOnGoing() {
            return KeyguardViewMediator.this.isKeyguardDoneOnGoing();
        }
        /// @}

        /// M : added for VoiceWakeup @{
        @Override
        public void dismiss() {
            KeyguardViewMediator.this.dismiss();
        }

        @Override
        public void dismiss(boolean authenticated) {
            KeyguardViewMediator.this.dismiss(authenticated);
        }

        @Override
        public boolean isKeyguardExternallyEnabled() {
            return KeyguardViewMediator.this.isKeyguardExternallyEnabled();
        }
        /// @}

        /// M : add for Nav @{
        @Override
        public void updateNavbarStatus() {
            KeyguardViewMediator.this.updateNavbarStatus() ;
        }
        /// @}
    };

    public void userActivity() {
        mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    boolean mustNotUnlockCurrentUser() {
        return UserManager.isSplitSystemUser()
                && KeyguardUpdateMonitor.getCurrentUser() == UserHandle.USER_SYSTEM;
    }

    private void setupLocked() {
        mPM = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mTrustManager = (TrustManager) mContext.getSystemService(Context.TRUST_SERVICE);

        mShowKeyguardWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "show keyguard");
        mShowKeyguardWakeLock.setReferenceCounted(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        mContext.registerReceiver(mBroadcastReceiver, filter);

        final IntentFilter delayedActionFilter = new IntentFilter();
        delayedActionFilter.addAction(DELAYED_KEYGUARD_ACTION);
        delayedActionFilter.addAction(DELAYED_LOCK_PROFILE_ACTION);
        mContext.registerReceiver(mDelayedLockBroadcastReceiver, delayedActionFilter,
                SYSTEMUI_PERMISSION, null /* scheduler */);

        InjectionInflationController injectionInflationController =
                new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent());
        mKeyguardDisplayManager = new KeyguardDisplayManager(mContext,
                injectionInflationController);

        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(mContext);

        mLockPatternUtils = new LockPatternUtils(mContext);
        KeyguardUpdateMonitor.setCurrentUser(ActivityManager.getCurrentUser());

        // Assume keyguard is showing (unless it's disabled) until we know for sure, unless Keyguard
        // is disabled.
        if (mContext.getResources().getBoolean(
                com.android.keyguard.R.bool.config_enableKeyguardService)) {
            setShowingLocked(!shouldWaitForProvisioning()
                    && !mLockPatternUtils.isLockScreenDisabled(
                            KeyguardUpdateMonitor.getCurrentUser()),
                    mAodShowing, true /* forceCallbacks */);
        } else {
            // The system's keyguard is disabled or missing.
            setShowingLocked(false, mAodShowing, true);
        }

        mStatusBarKeyguardViewManager =
                SystemUIFactory.getInstance().createStatusBarKeyguardViewManager(mContext,
                        mViewMediatorCallback, mLockPatternUtils);
        final ContentResolver cr = mContext.getContentResolver();

        mDeviceInteractive = mPM.isInteractive();

        mLockSounds = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                .build();
        String soundPath = Settings.Global.getString(cr, Settings.Global.LOCK_SOUND);
        if (soundPath != null) {
            mLockSoundId = mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || mLockSoundId == 0) {
            Log.w(TAG, "failed to load lock sound from " + soundPath);
        }
        soundPath = Settings.Global.getString(cr, Settings.Global.UNLOCK_SOUND);
        if (soundPath != null) {
            mUnlockSoundId = mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || mUnlockSoundId == 0) {
            Log.w(TAG, "failed to load unlock sound from " + soundPath);
        }
        soundPath = Settings.Global.getString(cr, Settings.Global.TRUSTED_SOUND);
        if (soundPath != null) {
            mTrustedSoundId = mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || mTrustedSoundId == 0) {
            Log.w(TAG, "failed to load trusted sound from " + soundPath);
        }

        int lockSoundDefaultAttenuation = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lockSoundVolumeDb);
        mLockSoundVolume = (float)Math.pow(10, (float)lockSoundDefaultAttenuation/20);

        mHideAnimation = AnimationUtils.loadAnimation(mContext,
                com.android.internal.R.anim.lock_screen_behind_enter);

        mWorkLockController = new WorkLockActivityController(mContext);

        /// M: add keyguard dialog manager to show dialog
        mDialogManager = KeyguardDialogManager.getInstance(mContext);

        /// M: add AntiTheftManager
        // M: ALPS02409993 fix PPL show dependency broadcast when phone boot
        AntiTheftManager.checkPplStatus();
        mAntiTheftManager = AntiTheftManager.getInstance(mContext,
                    mViewMediatorCallback, mLockPatternUtils);
        mAntiTheftManager.doAntiTheftLockCheck();

        /// M: add VoiceWakeupManager
        mVoiceWakeupManager = VoiceWakeupManagerProxy.getInstance();
        mVoiceWakeupManager.init(mContext, mViewMediatorCallback);
    }

    @Override
    public void start() {
        KeyguardUtils.getDefault().initSimmePolicy(mContext);
        synchronized (this) {
            setupLocked();
        }
        putComponent(KeyguardViewMediator.class, this);
        
        //add BUG_ID:TBSW-52 liuzhijun 20180131 (start)
        if (SystemProperties.get("ro.wtk_app_wake_gesture").equals("1")) {
            Uri uri = Settings.System.getUriFor("wake_gesture_unlock");
            mContext.getContentResolver().registerContentObserver(uri, true, mObserver);
        }
        //add BUG_ID:TBSW-52 liuzhijun 20180131 (end)
        
        //debuid:AQWL-44 add chenxuanyu start mKeyObserver
        if (SystemProperties.get("ro.odm_app_side_key").equals("1")) {
            Uri urikey = Settings.System.getUriFor("key_unlock");
            mContext.getContentResolver().registerContentObserver(urikey, true, mKeyObserver);
        }
        //debuid:AQWL-44 add chenxuanyu end
    }

    /**
     * Let us know that the system is ready after startup.
     */
    public void onSystemReady() {
        mHandler.obtainMessage(SYSTEM_READY).sendToTarget();
    }

    //add mtk start
    public void KvmSystemReady(){
        handleSystemReady();
    }
    //add mtk end

    private void handleSystemReady() {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "onSystemReady");
            mSystemReady = true;
            doKeyguardLocked(null);
            mUpdateMonitor.registerCallback(mUpdateCallback);
        }
        // Most services aren't available until the system reaches the ready state, so we
        // send it here when the device first boots.
        maybeSendUserPresentBroadcast();
    }

    /**
     * Called to let us know the screen was turned off.
     * @param why either {@link WindowManagerPolicyConstants#OFF_BECAUSE_OF_USER} or
     *   {@link WindowManagerPolicyConstants#OFF_BECAUSE_OF_TIMEOUT}.
     */
    public void onStartedGoingToSleep(int why) {
        if (DEBUG) Log.d(TAG, "onStartedGoingToSleep(" + why + ")");
        synchronized (this) {
            mDeviceInteractive = false;
            mGoingToSleep = true;

            // Lock immediately based on setting if secure (user has a pin/pattern/password).
            // This also "locks" the device when not secure to provide easy access to the
            // camera while preventing unwanted input.
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            final boolean lockImmediately =
                    mLockPatternUtils.getPowerButtonInstantlyLocks(currentUser)
                            || !mLockPatternUtils.isSecure(currentUser);
            long timeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
            mLockLater = false;
            if (mExitSecureCallback != null) {
                if (DEBUG) Log.d(TAG, "pending exit secure callback cancelled");
                try {
                    mExitSecureCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
                mExitSecureCallback = null;
                if (!mExternallyEnabled) {
                    hideLocked();
                }
            } else if (mShowing) {
                mPendingReset = true;
            } else if ((why == WindowManagerPolicyConstants.OFF_BECAUSE_OF_TIMEOUT && timeout > 0)
                    || (why == WindowManagerPolicyConstants.OFF_BECAUSE_OF_USER && !lockImmediately)) {
                doKeyguardLaterLocked(timeout);
                mLockLater = true;
            } else if (!mLockPatternUtils.isLockScreenDisabled(currentUser)) {
                mPendingLock = true;
            }

            if (mPendingLock) {
                playSounds(true);
            }
        }
        KeyguardUpdateMonitor.getInstance(mContext).dispatchStartedGoingToSleep(why);
        notifyStartedGoingToSleep();
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        if (DEBUG) Log.d(TAG, "onFinishedGoingToSleep(" + why + ")");
        synchronized (this) {
            mDeviceInteractive = false;
            mGoingToSleep = false;
            mWakeAndUnlocking = false;

            resetKeyguardDonePendingLocked();
            mHideAnimationRun = false;

            notifyFinishedGoingToSleep();

            if (cameraGestureTriggered) {
                Log.i(TAG, "Camera gesture was triggered, preventing Keyguard locking.");

                // Just to make sure, make sure the device is awake.
                mContext.getSystemService(PowerManager.class).wakeUp(SystemClock.uptimeMillis(),
                        PowerManager.WAKE_REASON_CAMERA_LAUNCH,
                        "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
                mPendingLock = false;
                mPendingReset = false;
            }

            if (mPendingReset) {
                resetStateLocked();
                mPendingReset = false;
            }

            if (mPendingLock) {
                doKeyguardLocked(null);
                mPendingLock = false;
            }

            // We do not have timeout and power button instant lock setting for profile lock.
            // So we use the personal setting if there is any. But if there is no device
            // we need to make sure we lock it immediately when the screen is off.
            if (!mLockLater && !cameraGestureTriggered) {
                doKeyguardForChildProfilesLocked();
            }

        }
        KeyguardUpdateMonitor.getInstance(mContext).dispatchFinishedGoingToSleep(why);
    }

    private long getLockTimeout(int userId) {
        // if the screen turned off because of timeout or the user hit the power button
        // and we don't need to lock immediately, set an alarm
        // to enable it a little bit later (i.e, give the user a chance
        // to turn the screen back on within a certain window without
        // having to unlock the screen)
        final ContentResolver cr = mContext.getContentResolver();

        // From SecuritySettings
        final long lockAfterTimeout = Settings.Secure.getInt(cr,
                Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT,
                KEYGUARD_LOCK_AFTER_DELAY_DEFAULT);

        // From DevicePolicyAdmin
        final long policyTimeout = mLockPatternUtils.getDevicePolicyManager()
                .getMaximumTimeToLock(null, userId);

        long timeout;

        if (policyTimeout <= 0) {
            timeout = lockAfterTimeout;
        } else {
            // From DisplaySettings
            long displayTimeout = Settings.System.getInt(cr, SCREEN_OFF_TIMEOUT,
                    KEYGUARD_DISPLAY_TIMEOUT_DELAY_DEFAULT);

            // policy in effect. Make sure we don't go beyond policy limit.
            displayTimeout = Math.max(displayTimeout, 0); // ignore negative values
            timeout = Math.min(policyTimeout - displayTimeout, lockAfterTimeout);
            timeout = Math.max(timeout, 0);
        }
        return timeout;
    }

    private void doKeyguardLaterLocked() {
        long timeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
        if (timeout == 0) {
            doKeyguardLocked(null);
        } else {
            doKeyguardLaterLocked(timeout);
        }
    }

    private void doKeyguardLaterLocked(long timeout) {
        // Lock in the future
        long when = SystemClock.elapsedRealtime() + timeout;
        Intent intent = new Intent(DELAYED_KEYGUARD_ACTION);
        intent.putExtra("seq", mDelayedShowingSequence);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent sender = PendingIntent.getBroadcast(mContext,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, when, sender);
        if (DEBUG) Log.d(TAG, "setting alarm to turn off keyguard, seq = "
                         + mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        UserManager um = UserManager.get(mContext);
        for (int profileId : um.getEnabledProfileIds(UserHandle.myUserId())) {
            if (mLockPatternUtils.isSeparateProfileChallengeEnabled(profileId)) {
                long userTimeout = getLockTimeout(profileId);
                if (userTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long userWhen = SystemClock.elapsedRealtime() + userTimeout;
                    Intent lockIntent = new Intent(DELAYED_LOCK_PROFILE_ACTION);
                    lockIntent.putExtra("seq", mDelayedProfileShowingSequence);
                    lockIntent.putExtra(Intent.EXTRA_USER_ID, profileId);
                    lockIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    PendingIntent lockSender = PendingIntent.getBroadcast(
                            mContext, 0, lockIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            userWhen, lockSender);
                }
            }
        }
    }

    private void doKeyguardForChildProfilesLocked() {
        UserManager um = UserManager.get(mContext);
        for (int profileId : um.getEnabledProfileIds(UserHandle.myUserId())) {
            if (mLockPatternUtils.isSeparateProfileChallengeEnabled(profileId)) {
                lockProfile(profileId);
            }
        }
    }

    private void cancelDoKeyguardLaterLocked() {
        mDelayedShowingSequence++;
    }

    private void cancelDoKeyguardForChildProfilesLocked() {
        mDelayedProfileShowingSequence++;
    }

    /**
     * Let's us know when the device is waking up.
     */
    public void onStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#onStartedWakingUp");

        // TODO: Rename all screen off/on references to interactive/sleeping
        synchronized (this) {
            mDeviceInteractive = true;
            cancelDoKeyguardLaterLocked();
            cancelDoKeyguardForChildProfilesLocked();
            if (DEBUG) Log.d(TAG, "onStartedWakingUp, seq = " + mDelayedShowingSequence);
            notifyStartedWakingUp();
        }
        KeyguardUpdateMonitor.getInstance(mContext).dispatchStartedWakingUp();
        maybeSendUserPresentBroadcast();
        Trace.endSection();
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback callback) {
        Trace.beginSection("KeyguardViewMediator#onScreenTurningOn");
        notifyScreenOn(callback);
        Trace.endSection();
    }

    public void onScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#onScreenTurnedOn");
        notifyScreenTurnedOn();
        mUpdateMonitor.dispatchScreenTurnedOn();
        Trace.endSection();
    }

    public void onScreenTurnedOff() {
        notifyScreenTurnedOff();
        mUpdateMonitor.dispatchScreenTurnedOff();
    }

    private void maybeSendUserPresentBroadcast() {
        if (mSystemReady && mLockPatternUtils.isLockScreenDisabled(
                KeyguardUpdateMonitor.getCurrentUser())) {
            // Lock screen is disabled because the user has set the preference to "None".
            // In this case, send out ACTION_USER_PRESENT here instead of in
            // handleKeyguardDone()
            sendUserPresentBroadcast();
        } else if (mSystemReady && shouldWaitForProvisioning()) {
            // Skipping the lockscreen because we're not yet provisioned, but we still need to
            // notify the StrongAuthTracker that it's now safe to run trust agents, in case the
            // user sets a credential later.
            getLockPatternUtils().userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    /**
     * A dream started.  We should lock after the usual screen-off lock timeout but only
     * if there is a secure lock pattern.
     */
    public void onDreamingStarted() {
        KeyguardUpdateMonitor.getInstance(mContext).dispatchDreamingStarted();
        synchronized (this) {
            if (mDeviceInteractive
                    && mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                doKeyguardLaterLocked();
            }
        }
    }

    /**
     * A dream stopped.
     */
    public void onDreamingStopped() {
        KeyguardUpdateMonitor.getInstance(mContext).dispatchDreamingStopped();
        synchronized (this) {
            if (mDeviceInteractive) {
                cancelDoKeyguardLaterLocked();
            }
        }
    }

    /**
     * Same semantics as {@link WindowManagerPolicyConstants#enableKeyguard}; provide
     * a way for external stuff to override normal keyguard behavior.  For instance
     * the phone app disables the keyguard when it receives incoming calls.
     */
    public void setKeyguardEnabled(boolean enabled) {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "setKeyguardEnabled(" + enabled + ")");

            mExternallyEnabled = enabled;

            if (!enabled && mShowing) {
                if (mExitSecureCallback != null) {
                    if (DEBUG) Log.d(TAG, "in process of verifyUnlock request, ignoring");
                    // we're in the process of handling a request to verify the user
                    // can get past the keyguard. ignore extraneous requests to disable / reenable
                    return;
                }

                // hiding keyguard that is showing, remember to reshow later
                if (DEBUG) Log.d(TAG, "remembering to reshow, hiding keyguard, "
                        + "disabling status bar expansion");
                mNeedToReshowWhenReenabled = true;
                updateInputRestrictedLocked();
                hideLocked();
            } else if (enabled && mNeedToReshowWhenReenabled) {
                // reenabled after previously hidden, reshow
                if (DEBUG) Log.d(TAG, "previously hidden, reshowing, reenabling "
                        + "status bar expansion");
                mNeedToReshowWhenReenabled = false;
                updateInputRestrictedLocked();

                if (mExitSecureCallback != null) {
                    if (DEBUG) Log.d(TAG, "onKeyguardExitResult(false), resetting");
                    try {
                        mExitSecureCallback.onKeyguardExitResult(false);
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                    }
                    mExitSecureCallback = null;
                    resetStateLocked();
                } else {
                    showLocked(null);

                    // block until we know the keygaurd is done drawing (and post a message
                    // to unblock us after a timeout so we don't risk blocking too long
                    // and causing an ANR).
                    mWaitingUntilKeyguardVisible = true;
                    mHandler.sendEmptyMessageDelayed(KEYGUARD_DONE_DRAWING, KEYGUARD_DONE_DRAWING_TIMEOUT_MS);
                    if (DEBUG) Log.d(TAG, "waiting until mWaitingUntilKeyguardVisible is false");
                    while (mWaitingUntilKeyguardVisible) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (DEBUG) Log.d(TAG, "done waiting for mWaitingUntilKeyguardVisible");
                }
            }
        }
    }

    /**
     * @see android.app.KeyguardManager#exitKeyguardSecurely
     */
    public void verifyUnlock(IKeyguardExitCallback callback) {
        Trace.beginSection("KeyguardViewMediator#verifyUnlock");
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "verifyUnlock");
            if (shouldWaitForProvisioning()) {
                // don't allow this api when the device isn't provisioned
                if (DEBUG) Log.d(TAG, "ignoring because device isn't provisioned");
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (mExternallyEnabled) {
                // this only applies when the user has externally disabled the
                // keyguard.  this is unexpected and means the user is not
                // using the api properly.
                Log.w(TAG, "verifyUnlock called when not externally disabled");
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (mExitSecureCallback != null) {
                // already in progress with someone else
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (!isSecure()) {

                // Keyguard is not secure, no need to do anything, and we don't need to reshow
                // the Keyguard after the client releases the Keyguard lock.
                mExternallyEnabled = true;
                mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
                try {
                    callback.onKeyguardExitResult(true);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
            } else {

                // Since we prevent apps from hiding the Keyguard if we are secure, this should be
                // a no-op as well.
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onKeyguardExitResult(false)", e);
                }
            }
        }
        Trace.endSection();
    }

    /**
     * Is the keyguard currently showing and not being force hidden?
     */
    public boolean isShowingAndNotOccluded() {
        return mShowing && !mOccluded;
    }

    /**
     * Notify us when the keyguard is occluded by another window
     */
    public void setOccluded(boolean isOccluded, boolean animate) {
        Trace.beginSection("KeyguardViewMediator#setOccluded");
        if (DEBUG) Log.d(TAG, "setOccluded " + isOccluded);
        mHandler.removeMessages(SET_OCCLUDED);
        Message msg = mHandler.obtainMessage(SET_OCCLUDED, isOccluded ? 1 : 0, animate ? 1 : 0);
        mHandler.sendMessage(msg);
        Trace.endSection();
    }

    public boolean isHiding() {
        return mHiding;
    }

    /**
     * Handles SET_OCCLUDED message sent by setOccluded()
     */
    private void handleSetOccluded(boolean isOccluded, boolean animate) {
        Trace.beginSection("KeyguardViewMediator#handleSetOccluded");
        synchronized (KeyguardViewMediator.this) {
            if (mHiding && isOccluded) {
                // We're in the process of going away but WindowManager wants to show a
                // SHOW_WHEN_LOCKED activity instead.
                startKeyguardExitAnimation(0, 0);
            }

            if (mOccluded != isOccluded) {
                mOccluded = isOccluded;
                mUpdateMonitor.setKeyguardOccluded(isOccluded);
                mStatusBarKeyguardViewManager.setOccluded(isOccluded, animate
                        && mDeviceInteractive);
                adjustStatusBarLocked();
            }
        }
        Trace.endSection();
    }

    /**
     * Used by PhoneWindowManager to enable the keyguard due to a user activity timeout.
     * This must be safe to call from any thread and with any window manager locks held.
     */
    public void doKeyguardTimeout(Bundle options) {
        mHandler.removeMessages(KEYGUARD_TIMEOUT);
        Message msg = mHandler.obtainMessage(KEYGUARD_TIMEOUT, options);
        mHandler.sendMessage(msg);
    }

    /**
     * Given the state of the keyguard, is the input restricted?
     * Input is restricted when the keyguard is showing, or when the keyguard
     * was suppressed by an app that disabled the keyguard or we haven't been provisioned yet.
     */
    public boolean isInputRestricted() {
        return mShowing || mNeedToReshowWhenReenabled;
    }

    private void updateInputRestricted() {
        synchronized (this) {
            updateInputRestrictedLocked();
        }
    }

    private void updateInputRestrictedLocked() {
        boolean inputRestricted = isInputRestricted();
        if (mInputRestricted != inputRestricted) {
            mInputRestricted = inputRestricted;
            int size = mKeyguardStateCallbacks.size();
            for (int i = size - 1; i >= 0; i--) {
                final IKeyguardStateCallback callback = mKeyguardStateCallbacks.get(i);
                try {
                    callback.onInputRestrictedStateChanged(inputRestricted);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        mKeyguardStateCallbacks.remove(callback);
                    }
                }
            }
        }
    }

    /**
     * Enable the keyguard if the settings are appropriate.
     */
    private void doKeyguardLocked(Bundle options) {
        if (KeyguardUpdateMonitor.CORE_APPS_ONLY) {
            // Don't show keyguard during half-booted cryptkeeper stage.
            if (DEBUG) Log.d(TAG, "doKeyguard: not showing because booting to cryptkeeper");
            return;
        }

        // if another app is disabling us, don't show
        if (!mExternallyEnabled) {
            if (DEBUG) Log.d(TAG, "doKeyguard: not showing because externally disabled");

            mNeedToReshowWhenReenabled = true;
            return;
        }

        // if the keyguard is already showing, don't bother
        if (mStatusBarKeyguardViewManager.isShowing()) {
            if (DEBUG) Log.d(TAG, "doKeyguard: not showing because it is already showing");
            resetStateLocked();
            return;
        }

        // In split system user mode, we never unlock system user.
        if (!mustNotUnlockCurrentUser()
                || !mUpdateMonitor.isDeviceProvisioned()) {

            // if the setup wizard hasn't run yet, don't show
            final boolean requireSim = !SystemProperties.getBoolean(
                    "keyguard.no_require_sim", true);
            boolean lockedOrMissing = false;
            for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                if (isSimLockedOrMissing(i, requireSim)) {
                    lockedOrMissing = true;
                    break;
                }
            }

            /// M: Add new condition DM lock is not true
            boolean antiTheftLocked = AntiTheftManager.isAntiTheftLocked();

            Log.d(TAG, "lockedOrMissing is " + lockedOrMissing + ", requireSim=" + requireSim
                    + ", antiTheftLocked=" + antiTheftLocked);

            if (!lockedOrMissing && shouldWaitForProvisioning() && !antiTheftLocked) {
                if (DEBUG) Log.d(TAG, "doKeyguard: not showing because device isn't provisioned"
                        + " and the sim is not locked or missing");
                return;
            }

            boolean forceShow = options != null && options.getBoolean(OPTION_FORCE_SHOW, false);
            if (mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())
                    && !lockedOrMissing && !forceShow && !antiTheftLocked) {
                if (DEBUG) Log.d(TAG, "doKeyguard: not showing because lockscreen is off");
                return;
            }

            if (mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser())) {
                if (DEBUG) Log.d(TAG, "Not showing lock screen since just decrypted");
                // Without this, settings is not enabled until the lock screen first appears
                setShowingLocked(false, mAodShowing);
                hideLocked();
                return;
            }
        }

        if (DEBUG) Log.d(TAG, "doKeyguard: showing the lock screen");
        showLocked(options);
    }

    private void lockProfile(int userId) {
        mTrustManager.setDeviceLockedForUser(userId, true);
    }

    private boolean shouldWaitForProvisioning() {
        return !mUpdateMonitor.isDeviceProvisioned() && !isSecure();
    }

    /**
     * Dismiss the keyguard through the security layers.
     * @param callback Callback to be informed about the result
     * @param message Message that should be displayed on the bouncer.
     */
    private void handleDismiss(IKeyguardDismissCallback callback, CharSequence message) {
        if (mShowing) {
            if (callback != null) {
                mDismissCallbackRegistry.addCallback(callback);
            }
            mCustomMessage = message;
            mStatusBarKeyguardViewManager.dismissAndCollapse();
        } else if (callback != null) {
            new DismissCallbackWrapper(callback).notifyDismissError();
        }
    }

    public void dismiss(IKeyguardDismissCallback callback, CharSequence message) {
        mHandler.obtainMessage(DISMISS, new DismissMessage(callback, message)).sendToTarget();
    }

    /**
     * Send message to keyguard telling it to reset its state.
     * @see #handleReset
     */
    private void resetStateLocked() {
        if (DEBUG) Log.e(TAG, "resetStateLocked");
        Message msg = mHandler.obtainMessage(RESET);
        mHandler.sendMessage(msg);
    }

    /**
     * Send message to keyguard telling it to verify unlock
     * @see #handleVerifyUnlock()
     */
    private void verifyUnlockLocked() {
        if (DEBUG) Log.d(TAG, "verifyUnlockLocked");
        mHandler.sendEmptyMessage(VERIFY_UNLOCK);
    }

    private void notifyStartedGoingToSleep() {
        if (DEBUG) Log.d(TAG, "notifyStartedGoingToSleep");
        mHandler.sendEmptyMessage(NOTIFY_STARTED_GOING_TO_SLEEP);
    }

    private void notifyFinishedGoingToSleep() {
        if (DEBUG) Log.d(TAG, "notifyFinishedGoingToSleep");
        mHandler.sendEmptyMessage(NOTIFY_FINISHED_GOING_TO_SLEEP);
    }

    private void notifyStartedWakingUp() {
        if (DEBUG) Log.d(TAG, "notifyStartedWakingUp");
        mHandler.sendEmptyMessage(NOTIFY_STARTED_WAKING_UP);
    }

    private void notifyScreenOn(IKeyguardDrawnCallback callback) {
        if (DEBUG) Log.d(TAG, "notifyScreenOn");
        Message msg = mHandler.obtainMessage(NOTIFY_SCREEN_TURNING_ON, callback);
        mHandler.sendMessage(msg);
    }

    private void notifyScreenTurnedOn() {
        if (DEBUG) Log.d(TAG, "notifyScreenTurnedOn");
        Message msg = mHandler.obtainMessage(NOTIFY_SCREEN_TURNED_ON);
        mHandler.sendMessage(msg);
    }

    private void notifyScreenTurnedOff() {
        if (DEBUG) Log.d(TAG, "notifyScreenTurnedOff");
        Message msg = mHandler.obtainMessage(NOTIFY_SCREEN_TURNED_OFF);
        mHandler.sendMessage(msg);
    }

    /**
     * Send message to keyguard telling it to show itself
     * @see #handleShow
     */
    private void showLocked(Bundle options) {
        Trace.beginSection("KeyguardViewMediator#showLocked aqcuiring mShowKeyguardWakeLock");
        if (DEBUG) Log.d(TAG, "showLocked");
        // ensure we stay awake until we are finished displaying the keyguard
        //add by wangjian for EJSLYQ-94 20200107 start
        if (mLockIcon != null) {
            mLockIcon.setKeyguardGone(false);
        }
        //add by wangjian for EJSLYQ-94 20200107 end
        mShowKeyguardWakeLock.acquire();
        Message msg = mHandler.obtainMessage(SHOW, options);
        mHandler.sendMessage(msg);
        Trace.endSection();
    }

    /**
     * Send message to keyguard telling it to hide itself
     * @see #handleHide()
     */
    private void hideLocked() {
        Trace.beginSection("KeyguardViewMediator#hideLocked");
        if (DEBUG) Log.d(TAG, "hideLocked");
        Message msg = mHandler.obtainMessage(HIDE);
        mHandler.sendMessage(msg);
        Trace.endSection();
    }

    public boolean isSecure() {
        return isSecure(KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isSecure(int userId) {
        return mLockPatternUtils.isSecure(userId)
                || KeyguardUpdateMonitor.getInstance(mContext).isSimPinSecure()
                || AntiTheftManager.isAntiTheftLocked();
    }

    public void setSwitchingUser(boolean switching) {
        KeyguardUpdateMonitor.getInstance(mContext).setSwitchingUser(switching);
    }

    /**
     * Update the newUserId. Call while holding WindowManagerService lock.
     * NOTE: Should only be called by KeyguardViewMediator in response to the user id changing.
     *
     * @param newUserId The id of the incoming user.
     */
    public void setCurrentUser(int newUserId) {
        KeyguardUpdateMonitor.setCurrentUser(newUserId);
        synchronized (this) {
            notifyTrustedChangedLocked(mUpdateMonitor.getUserHasTrust(newUserId));
        }
    }

    /**
     * This broadcast receiver should be registered with the SystemUI permission.
     */
    private final BroadcastReceiver mDelayedLockBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DELAYED_KEYGUARD_ACTION.equals(intent.getAction())) {
                final int sequence = intent.getIntExtra("seq", 0);
                if (DEBUG) Log.d(TAG, "received DELAYED_KEYGUARD_ACTION with seq = "
                        + sequence + ", mDelayedShowingSequence = " + mDelayedShowingSequence);
                synchronized (KeyguardViewMediator.this) {
                    if (mDelayedShowingSequence == sequence) {
                        doKeyguardLocked(null);
                    }
                }
            } else if (DELAYED_LOCK_PROFILE_ACTION.equals(intent.getAction())) {
                final int sequence = intent.getIntExtra("seq", 0);
                int userId = intent.getIntExtra(Intent.EXTRA_USER_ID, 0);
                if (userId != 0) {
                    synchronized (KeyguardViewMediator.this) {
                        if (mDelayedProfileShowingSequence == sequence) {
                            lockProfile(userId);
                        }
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                synchronized (KeyguardViewMediator.this){
                    mShuttingDown = true;
                }
            }
        }
    };

    public void keyguardDone() {
        Trace.beginSection("KeyguardViewMediator#keyguardDone");
        if (DEBUG) Log.d(TAG, "keyguardDone()");
        userActivity();
        EventLog.writeEvent(70000, 2);
        Message msg = mHandler.obtainMessage(KEYGUARD_DONE);
        mHandler.sendMessage(msg);
        Trace.endSection();
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
                case SHOW:
                    handleShow((Bundle) msg.obj);
                    break;
                case HIDE:
                    handleHide();
                    break;
                case RESET:
                    handleReset();
                    break;
                case VERIFY_UNLOCK:
                    Trace.beginSection("KeyguardViewMediator#handleMessage VERIFY_UNLOCK");
                    handleVerifyUnlock();
                    Trace.endSection();
                    break;
                case NOTIFY_STARTED_GOING_TO_SLEEP:
                    handleNotifyStartedGoingToSleep();
                    break;
                case NOTIFY_FINISHED_GOING_TO_SLEEP:
                    handleNotifyFinishedGoingToSleep();
                    break;
                case NOTIFY_SCREEN_TURNING_ON:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNING_ON");
                    handleNotifyScreenTurningOn((IKeyguardDrawnCallback) msg.obj);
                    Trace.endSection();
                    break;
                case NOTIFY_SCREEN_TURNED_ON:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNED_ON");
                    handleNotifyScreenTurnedOn();
                    Trace.endSection();
                    break;
                case NOTIFY_SCREEN_TURNED_OFF:
                    handleNotifyScreenTurnedOff();
                    break;
                case NOTIFY_STARTED_WAKING_UP:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_STARTED_WAKING_UP");
                    handleNotifyStartedWakingUp();
                    Trace.endSection();
                    break;
                case KEYGUARD_DONE:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE");
                    handleKeyguardDone();
                    Trace.endSection();
                    break;
                case KEYGUARD_DONE_DRAWING:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_DRAWING");
                    handleKeyguardDoneDrawing();
                    Trace.endSection();
                    break;
                case SET_OCCLUDED:
                    Trace.beginSection("KeyguardViewMediator#handleMessage SET_OCCLUDED");
                    handleSetOccluded(msg.arg1 != 0, msg.arg2 != 0);
                    Trace.endSection();
                    break;
                case KEYGUARD_TIMEOUT:
                    synchronized (KeyguardViewMediator.this) {
                        doKeyguardLocked((Bundle) msg.obj);
                    }
                    break;
                case DISMISS:
                    final DismissMessage message = (DismissMessage) msg.obj;
                    handleDismiss(message.getCallback(), message.getMessage());
                    break;
                case START_KEYGUARD_EXIT_ANIM:
                    Trace.beginSection("KeyguardViewMediator#handleMessage START_KEYGUARD_EXIT_ANIM");
                    StartKeyguardExitAnimParams params = (StartKeyguardExitAnimParams) msg.obj;
                    handleStartKeyguardExitAnimation(params.startTime, params.fadeoutDuration);
                    FalsingManagerFactory.getInstance(mContext).onSucccessfulUnlock();
                    Trace.endSection();
                    break;
                case KEYGUARD_DONE_PENDING_TIMEOUT:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_PENDING_TIMEOUT");
                    Log.w(TAG, "Timeout while waiting for activity drawn!");
                    Trace.endSection();
                    break;
                case SYSTEM_READY:
                    handleSystemReady();
                    break;
                case DISMISS_INTERNAL:
                    handleDismissInternal(msg.arg1 == 1 ? true : false /* allowWhileOccluded */);
                    break;
            }
        }
    };

    private void tryKeyguardDone() {
        if (!mKeyguardDonePending && mHideAnimationRun && !mHideAnimationRunning) {
            handleKeyguardDone();
        } else if (!mHideAnimationRun) {
            mHideAnimationRun = true;
            mHideAnimationRunning = true;
            mStatusBarKeyguardViewManager.startPreHideAnimation(mHideAnimationFinishedRunnable);
        }
    }

    /**
     * @see #keyguardDone
     * @see #KEYGUARD_DONE
     */
    private void handleKeyguardDone() {
        //add by wangjian for EJSLYQ-94 20200107 start
        if (mLockIcon != null) {
            mLockIcon.setKeyguardGone(true);
            mLockIcon.setVisibility(View.INVISIBLE);
        }
        //add by wangjian for EJSLYQ-94 20200107 end
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDone");
        final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        mUiOffloadThread.submit(() -> {
            if (mLockPatternUtils.isSecure(currentUser)) {
                mLockPatternUtils.getDevicePolicyManager().reportKeyguardDismissed(currentUser);
            }
        });
        if (DEBUG) Log.d(TAG, "handleKeyguardDone");
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }

        ///M: [ALPS01567248] Timing issue.
        ///   Voice Unlock View dismiss -> AntiTheft View shows
        ///   -> previous Voice Unlock dismiss flow calls handleKeyguardDone
        ///   -> remove AntiTheft View
        ///   So we avoid handleKeyguardDone if AntiTheft is the current view,
        ///   and not yet unlock correctly.
        if (AntiTheftManager.isAntiTheftLocked()) {
            Log.d(TAG, "handleKeyguardDone() - Skip keyguard done! antitheft = true" +
                       " or sim = " + mUpdateMonitor.isSimPinSecure());
            return;
        }

        mKeyguardDoneOnGoing = true;

        mUpdateMonitor.clearBiometricRecognized();

        if (mGoingToSleep) {
            Log.i(TAG, "Device is going to sleep, aborting keyguardDone");
            return;
        }
        if (mExitSecureCallback != null) {
            try {
                mExitSecureCallback.onKeyguardExitResult(true /* authenciated */);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call onKeyguardExitResult()", e);
            }

            mExitSecureCallback = null;

            // after succesfully exiting securely, no need to reshow
            // the keyguard when they've released the lock
            mExternallyEnabled = true;
            mNeedToReshowWhenReenabled = false;
            updateInputRestricted();
        }

        handleHide();
        Trace.endSection();
    }

    private void sendUserPresentBroadcast() {
        synchronized (this) {
            if (mBootCompleted) {
                int currentUserId = KeyguardUpdateMonitor.getCurrentUser();
                final UserHandle currentUser = new UserHandle(currentUserId);
                final UserManager um = (UserManager) mContext.getSystemService(
                        Context.USER_SERVICE);
                mUiOffloadThread.submit(() -> {
                    for (int profileId : um.getProfileIdsWithDisabled(currentUser.getIdentifier())) {
                        mContext.sendBroadcastAsUser(USER_PRESENT_INTENT, UserHandle.of(profileId));
                    }
                    getLockPatternUtils().userPresent(currentUserId);
                });
            } else {
                mBootSendUserPresent = true;
            }
        }
    }

    /**
     * @see #keyguardDone
     * @see #KEYGUARD_DONE_DRAWING
     */
    private void handleKeyguardDoneDrawing() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDoneDrawing");
        synchronized(this) {
            if (DEBUG) Log.d(TAG, "handleKeyguardDoneDrawing");
            if (mWaitingUntilKeyguardVisible) {
                if (DEBUG) Log.d(TAG, "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                mWaitingUntilKeyguardVisible = false;
                notifyAll();

                // there will usually be two of these sent, one as a timeout, and one
                // as a result of the callback, so remove any remaining messages from
                // the queue
                mHandler.removeMessages(KEYGUARD_DONE_DRAWING);
            }
        }
        Trace.endSection();
    }

    private void playSounds(boolean locked) {
	 	//FACE_UNLOCK_SUPPORT start
        if (FaceUnlockUtil.isFaceUnlockSupport() && FaceUnlockUtil.getInstance().isKeyguardUnlocked(locked)) {
            Log.d(FaceUnlockUtil.TAG, "isKeyguardUnlocked return");
            return;
        }
        //FACE_UNLOCK_SUPPORT end
        playSound(locked ? mLockSoundId : mUnlockSoundId);
    }

    private void playSound(int soundId) {
        if (soundId == 0) return;
        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.System.getInt(cr, Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) == 1) {

            mLockSounds.stop(mLockSoundStreamId);
            // Init mAudioManager
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                if (mAudioManager == null) return;
                mUiSoundsStreamType = mAudioManager.getUiSoundsStreamType();
            }

            mUiOffloadThread.submit(() -> {
                // If the stream is muted, don't play the sound
                if (mAudioManager.isStreamMute(mUiSoundsStreamType)) return;

                int id = mLockSounds.play(soundId,
                        mLockSoundVolume, mLockSoundVolume, 1/*priortiy*/, 0/*loop*/, 1.0f/*rate*/);
                synchronized (this) {
                    mLockSoundStreamId = id;
                }
            });

        }
    }

    private void playTrustedSound() {
        playSound(mTrustedSoundId);
    }

    private void updateActivityLockScreenState(boolean showing, boolean aodShowing) {
        mUiOffloadThread.submit(() -> {
            try {
                ActivityTaskManager.getService().setLockScreenShown(showing, aodShowing);
            } catch (RemoteException e) {
            }
        });
    }

    /**
     * Handle message sent by {@link #showLocked}.
     * @see #SHOW
     */
    private void handleShow(Bundle options) {
        Trace.beginSection("KeyguardViewMediator#handleShow");
        final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (mLockPatternUtils.isSecure(currentUser)) {
            mLockPatternUtils.getDevicePolicyManager().reportKeyguardSecured(currentUser);
        }
        synchronized (KeyguardViewMediator.this) {
            if (!mSystemReady) {
                if (DEBUG) Log.d(TAG, "ignoring handleShow because system is not ready.");
                return;
            } else {
                if (DEBUG) Log.d(TAG, "handleShow");
            }

            setShowingLocked(true, mAodShowing);
            mStatusBarKeyguardViewManager.show(options);
            mHiding = false;
            mWakeAndUnlocking = false;
            resetKeyguardDonePendingLocked();
            mHideAnimationRun = false;
            adjustStatusBarLocked();
            userActivity();
            mUpdateMonitor.setKeyguardGoingAway(false /* away */);
            mShowKeyguardWakeLock.release();
        }
        mKeyguardDisplayManager.show();
        Trace.endSection();
    }

    private final Runnable mKeyguardGoingAwayRunnable = new Runnable() {
        @Override
        public void run() {
            Trace.beginSection("KeyguardViewMediator.mKeyGuardGoingAwayRunnable");
            if (DEBUG) Log.d(TAG, "keyguardGoingAway");
            mStatusBarKeyguardViewManager.keyguardGoingAway();

            int flags = 0;
            if (mStatusBarKeyguardViewManager.shouldDisableWindowAnimationsForUnlock()
                    || (mWakeAndUnlocking && !mPulsing)) {
                flags |= WindowManagerPolicyConstants
                        .KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS;
            }
            if (mStatusBarKeyguardViewManager.isGoingToNotificationShade()
                    || (mWakeAndUnlocking && mPulsing)) {
                flags |= WindowManagerPolicyConstants.KEYGUARD_GOING_AWAY_FLAG_TO_SHADE;
            }
            if (mStatusBarKeyguardViewManager.isUnlockWithWallpaper()) {
                flags |= WindowManagerPolicyConstants.KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER;
            }

            mUpdateMonitor.setKeyguardGoingAway(true /* goingAway */);

            // Don't actually hide the Keyguard at the moment, wait for window
            // manager until it tells us it's safe to do so with
            // startKeyguardExitAnimation.
            // Posting to mUiOffloadThread to ensure that calls to ActivityTaskManager will be in
            // order.
            final int keyguardFlag = flags;
            mUiOffloadThread.submit(() -> {
                try {
                    ActivityTaskManager.getService().keyguardGoingAway(keyguardFlag);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while calling WindowManager", e);
                }
            });
            Trace.endSection();
        }
    };

    private final Runnable mHideAnimationFinishedRunnable = () -> {
        mHideAnimationRunning = false;
        tryKeyguardDone();
    };

    /**
     * Handle message sent by {@link #hideLocked()}
     * @see #HIDE
     */
    private void handleHide() {
        Trace.beginSection("KeyguardViewMediator#handleHide");

        // It's possible that the device was unlocked in a dream state. It's time to wake up.
        if (mAodShowing) {
            PowerManager pm = mContext.getSystemService(PowerManager.class);
            pm.wakeUp(SystemClock.uptimeMillis(), PowerManager.WAKE_REASON_GESTURE,
                    "com.android.systemui:BOUNCER_DOZING");
        }

        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleHide");

            if (mustNotUnlockCurrentUser()) {
                // In split system user mode, we never unlock system user. The end user has to
                // switch to another user.
                // TODO: We should stop it early by disabling the swipe up flow. Right now swipe up
                // still completes and makes the screen blank.
                if (DEBUG) Log.d(TAG, "Split system user, quit unlocking.");
                return;
            }
            mHiding = true;

            if (mShowing && !mOccluded) {
                mKeyguardGoingAwayRunnable.run();
            } else {
                handleStartKeyguardExitAnimation(
                        SystemClock.uptimeMillis() + mHideAnimation.getStartOffset(),
                        mHideAnimation.getDuration());
            }
        }
        Trace.endSection();
    }

    private void handleStartKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        Trace.beginSection("KeyguardViewMediator#handleStartKeyguardExitAnimation");
        if (DEBUG) Log.d(TAG, "handleStartKeyguardExitAnimation startTime=" + startTime
                + " fadeoutDuration=" + fadeoutDuration);
        synchronized (KeyguardViewMediator.this) {

            if (!mHiding) {
                // Tell ActivityManager that we canceled the keyguardExitAnimation.
                setShowingLocked(mShowing, mAodShowing, true /* force */);
                return;
            }
            mHiding = false;

            if (mWakeAndUnlocking && mDrawnCallback != null) {

                // Hack level over 9000: To speed up wake-and-unlock sequence, force it to report
                // the next draw from here so we don't have to wait for window manager to signal
                // this to our ViewRootImpl.
                mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                notifyDrawn(mDrawnCallback);
                mDrawnCallback = null;
            }

            // only play "unlock" noises if not on a call (since the incall UI
            // disables the keyguard)
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(mPhoneState)) {
                playSounds(false);
            }

            mWakeAndUnlocking = false;
            setShowingLocked(false, mAodShowing);
            mDismissCallbackRegistry.notifyDismissSucceeded();
            mStatusBarKeyguardViewManager.hide(startTime, fadeoutDuration);
            resetKeyguardDonePendingLocked();
            mHideAnimationRun = false;
            adjustStatusBarLocked();
            sendUserPresentBroadcast();
        }

        Log.d(TAG, "set mKeyguardDoneOnGoing = false");
        mKeyguardDoneOnGoing = false;
        Trace.endSection();
    }

    private void adjustStatusBarLocked() {
        adjustStatusBarLocked(false /* forceHideHomeRecentsButtons */);
    }

    private void adjustStatusBarLocked(boolean forceHideHomeRecentsButtons) {
        if (mStatusBarManager == null) {
            mStatusBarManager = (StatusBarManager)
                    mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        }

        if (mStatusBarManager == null) {
            Log.w(TAG, "Could not get status bar manager");
        } else {
            // Disable aspects of the system/status/navigation bars that must not be re-enabled by
            // windows that appear on top, ever
            int flags = StatusBarManager.DISABLE_NONE;
            if (forceHideHomeRecentsButtons || isShowingAndNotOccluded()) {
                flags |= StatusBarManager.DISABLE_HOME | StatusBarManager.DISABLE_RECENT;
            }

            if (DEBUG) {
                Log.d(TAG, "adjustStatusBarLocked: mShowing=" + mShowing + " mOccluded=" + mOccluded
                        + " isSecure=" + isSecure() + " force=" + forceHideHomeRecentsButtons
                        +  " --> flags=0x" + Integer.toHexString(flags));
            }

            mStatusBarManager.disable(flags);
        }
    }

    /**
     * Handle message sent by {@link #resetStateLocked}
     * @see #RESET
     */
    private void handleReset() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleReset");
            mStatusBarKeyguardViewManager.reset(true /* hideBouncerWhenShowing */);
        }
    }

    /**
     * Handle message sent by {@link #verifyUnlock}
     * @see #VERIFY_UNLOCK
     */
    private void handleVerifyUnlock() {
        Trace.beginSection("KeyguardViewMediator#handleVerifyUnlock");
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleVerifyUnlock");
            setShowingLocked(true, mAodShowing);
            mStatusBarKeyguardViewManager.dismissAndCollapse();
        }
        Trace.endSection();
    }

    private void handleNotifyStartedGoingToSleep() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyStartedGoingToSleep");
            mStatusBarKeyguardViewManager.onStartedGoingToSleep();
            //FACE_UNLOCK_SUPPORT start
            if (FaceUnlockUtil.isFaceUnlockSupport()) {
                FaceUnlockUtil.getInstance().doScreenOff();
            }
            //FACE_UNLOCK_SUPPORT end
        }
    }

    /**
     * Handle message sent by {@link #notifyFinishedGoingToSleep()}
     * @see #NOTIFY_FINISHED_GOING_TO_SLEEP
     */
    private void handleNotifyFinishedGoingToSleep() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyFinishedGoingToSleep");
            mStatusBarKeyguardViewManager.onFinishedGoingToSleep();
        }
    }

    private void handleNotifyStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#handleMotifyStartedWakingUp");
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyWakingUp");
            //FACE_UNLOCK_SUPPORT start
            if (FaceUnlockUtil.isFaceUnlockSupport()) {
                FaceUnlockUtil.getInstance().doScreenOn();
            }
            //FACE_UNLOCK_SUPPORT end
            mStatusBarKeyguardViewManager.onStartedWakingUp();
        }
        Trace.endSection();
    }

    private void handleNotifyScreenTurningOn(IKeyguardDrawnCallback callback) {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurningOn");
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyScreenTurningOn");
            mStatusBarKeyguardViewManager.onScreenTurningOn();
            if (callback != null) {
                if (mWakeAndUnlocking) {
                    mDrawnCallback = callback;
                } else {
                    ///M: optimization patch of boot time on Q
                    try {
                        mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                    } catch (Exception e) {
                        Log.e(TAG, "status bar window doesn't create when booting phone");
                    }
                    notifyDrawn(callback);
                }
            }
        }
        Trace.endSection();
    }

    private void handleNotifyScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurnedOn");
        if (LatencyTracker.isEnabled(mContext)) {
            LatencyTracker.getInstance(mContext).onActionEnd(LatencyTracker.ACTION_TURN_ON_SCREEN);
        }
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "handleNotifyScreenTurnedOn");
            mStatusBarKeyguardViewManager.onScreenTurnedOn();
        }
        Trace.endSection();
    }

    private void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "handleNotifyScreenTurnedOff");
            mDrawnCallback = null;
        }
    }

    private void notifyDrawn(final IKeyguardDrawnCallback callback) {
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        try {
            callback.onDrawn();
        } catch (RemoteException e) {
            Slog.w(TAG, "Exception calling onDrawn():", e);
        }
        Trace.endSection();
    }

    private void resetKeyguardDonePendingLocked() {
        mKeyguardDonePending = false;
        mHandler.removeMessages(KEYGUARD_DONE_PENDING_TIMEOUT);
    }

    @Override
    public void onBootCompleted() {
        mUpdateMonitor.dispatchBootCompleted();
        synchronized (this) {
            mBootCompleted = true;
            if (mBootSendUserPresent) {
                sendUserPresentBroadcast();
            }
        }
    }

    public void onWakeAndUnlocking() {
        Trace.beginSection("KeyguardViewMediator#onWakeAndUnlocking");
        mWakeAndUnlocking = true;
        keyguardDone();
        Trace.endSection();
    }

    public StatusBarKeyguardViewManager registerStatusBar(StatusBar statusBar,
            ViewGroup container, NotificationPanelView panelView,
            BiometricUnlockController biometricUnlockController, ViewGroup lockIconContainer) {
        //add by wangjian for EJSLYQ-94 20200107 start
        mLockIcon = statusBar.getStatusBarWindow().findViewById(R.id.lock_icon);
        //add by wangjian for EJSLYQ-94 20200107 end
        mStatusBarKeyguardViewManager.registerStatusBar(statusBar, container, panelView,
                biometricUnlockController, mDismissCallbackRegistry, lockIconContainer);
        return mStatusBarKeyguardViewManager;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        Trace.beginSection("KeyguardViewMediator#startKeyguardExitAnimation");
        Message msg = mHandler.obtainMessage(START_KEYGUARD_EXIT_ANIM,
                new StartKeyguardExitAnimParams(startTime, fadeoutDuration));
        mHandler.sendMessage(msg);
        Trace.endSection();
    }

    public void onShortPowerPressedGoHome() {
        // do nothing
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return mViewMediatorCallback;
    }

    public LockPatternUtils getLockPatternUtils() {
        return mLockPatternUtils;
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("  mSystemReady: "); pw.println(mSystemReady);
        pw.print("  mBootCompleted: "); pw.println(mBootCompleted);
        pw.print("  mBootSendUserPresent: "); pw.println(mBootSendUserPresent);
        pw.print("  mExternallyEnabled: "); pw.println(mExternallyEnabled);
        pw.print("  mShuttingDown: "); pw.println(mShuttingDown);
        pw.print("  mNeedToReshowWhenReenabled: "); pw.println(mNeedToReshowWhenReenabled);
        pw.print("  mShowing: "); pw.println(mShowing);
        pw.print("  mInputRestricted: "); pw.println(mInputRestricted);
        pw.print("  mOccluded: "); pw.println(mOccluded);
        pw.print("  mDelayedShowingSequence: "); pw.println(mDelayedShowingSequence);
        pw.print("  mExitSecureCallback: "); pw.println(mExitSecureCallback);
        pw.print("  mDeviceInteractive: "); pw.println(mDeviceInteractive);
        pw.print("  mGoingToSleep: "); pw.println(mGoingToSleep);
        pw.print("  mHiding: "); pw.println(mHiding);
        pw.print("  mWaitingUntilKeyguardVisible: "); pw.println(mWaitingUntilKeyguardVisible);
        pw.print("  mKeyguardDonePending: "); pw.println(mKeyguardDonePending);
        pw.print("  mHideAnimationRun: "); pw.println(mHideAnimationRun);
        pw.print("  mPendingReset: "); pw.println(mPendingReset);
        pw.print("  mPendingLock: "); pw.println(mPendingLock);
        pw.print("  mWakeAndUnlocking: "); pw.println(mWakeAndUnlocking);
        pw.print("  mDrawnCallback: "); pw.println(mDrawnCallback);
    }

    /**
     * @param aodShowing true when AOD - or ambient mode - is showing.
     */
    public void setAodShowing(boolean aodShowing) {
        setShowingLocked(mShowing, aodShowing);
    }

    /**
     * @param pulsing true when device temporarily wakes up to display an incoming notification.
     */
    public void setPulsing(boolean pulsing) {
        mPulsing = pulsing;
    }

    private static class StartKeyguardExitAnimParams {

        long startTime;
        long fadeoutDuration;

        private StartKeyguardExitAnimParams(long startTime, long fadeoutDuration) {
            this.startTime = startTime;
            this.fadeoutDuration = fadeoutDuration;
        }
    }

    private void setShowingLocked(boolean showing, boolean aodShowing) {
        setShowingLocked(showing, aodShowing, false /* forceCallbacks */);
    }

    private void setShowingLocked(boolean showing, boolean aodShowing, boolean forceCallbacks) {
        final boolean notifyDefaultDisplayCallbacks = showing != mShowing
                || aodShowing != mAodShowing || forceCallbacks;
        if (notifyDefaultDisplayCallbacks) {
            mShowing = showing;
            mAodShowing = aodShowing;
            if (notifyDefaultDisplayCallbacks) {
                notifyDefaultDisplayCallbacks(showing);
            }
            updateActivityLockScreenState(showing, aodShowing);
        }
    }

    private void notifyDefaultDisplayCallbacks(boolean showing) {
        int size = mKeyguardStateCallbacks.size();
        for (int i = size - 1; i >= 0; i--) {
            IKeyguardStateCallback callback = mKeyguardStateCallbacks.get(i);
            try {
                callback.onShowingStateChanged(showing);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call onShowingStateChanged", e);
                if (e instanceof DeadObjectException) {
                    mKeyguardStateCallbacks.remove(callback);
                }
            }
        }
        updateInputRestrictedLocked();
        mUiOffloadThread.submit(() -> {
            mTrustManager.reportKeyguardShowingChanged();
        });
    }

    private void notifyTrustedChangedLocked(boolean trusted) {
        int size = mKeyguardStateCallbacks.size();
        for (int i = size - 1; i >= 0; i--) {
            try {
                mKeyguardStateCallbacks.get(i).onTrustedChanged(trusted);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call notifyTrustedChangedLocked", e);
                if (e instanceof DeadObjectException) {
                    mKeyguardStateCallbacks.remove(i);
                }
            }
        }
    }

    private void notifyHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
        int size = mKeyguardStateCallbacks.size();
        for (int i = size - 1; i >= 0; i--) {
            try {
                mKeyguardStateCallbacks.get(i).onHasLockscreenWallpaperChanged(
                        hasLockscreenWallpaper);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call onHasLockscreenWallpaperChanged", e);
                if (e instanceof DeadObjectException) {
                    mKeyguardStateCallbacks.remove(i);
                }
            }
        }
    }

    public void addStateMonitorCallback(IKeyguardStateCallback callback) {
        synchronized (this) {
            mKeyguardStateCallbacks.add(callback);
            try {
                callback.onSimSecureStateChanged(mUpdateMonitor.isSimPinSecure());
                callback.onShowingStateChanged(mShowing);
                callback.onInputRestrictedStateChanged(mInputRestricted);
                callback.onTrustedChanged(mUpdateMonitor.getUserHasTrust(
                        KeyguardUpdateMonitor.getCurrentUser()));
                callback.onHasLockscreenWallpaperChanged(mUpdateMonitor.hasLockscreenWallpaper());
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call to IKeyguardStateCallback", e);
            }
        }
    }

    private static class DismissMessage {
        private final CharSequence mMessage;
        private final IKeyguardDismissCallback mCallback;

        DismissMessage(IKeyguardDismissCallback callback, CharSequence message) {
            mCallback = callback;
            mMessage = message;
        }

        public IKeyguardDismissCallback getCallback() {
            return mCallback;
        }

        public CharSequence getMessage() {
            return mMessage;
        }
    }

    /********************************************************
     ** Mediatek add begin
     ********************************************************/

    /// M: Fix the issue that SIM PIN/PUK/ME View will show
    ///    and disappear instantly because the last KeyguardDone flow is still on going.
    private static boolean mKeyguardDoneOnGoing = false;
    ///M: dialog manager for SIM detect dialog
    private KeyguardDialogManager mDialogManager;

    /// M: AntiTheft
    private AntiTheftManager mAntiTheftManager;

    /// M: VoiceWakeup
    private VoiceWakeupManagerProxy mVoiceWakeupManager;

    // Whether the next call to playSounds() should be skipped.  Defaults to
    // true because the first lock (on boot) should be silent.
    private boolean mSuppressNextLockSound = true;

    /**
     * M: If the keyguard currently showing and going to be hidden,
     *    the SIM Pin view won't correctly show.
     *    We will need to use this API to guarantee the PIN/PUK/ME lock
     *    will be shown anyway (ALPS01472600).
     */
    private void removeKeyguardDoneMsg() {
        mHandler.removeMessages(KEYGUARD_DONE);
    }

    /**
     * M: The real show dialog callback when invalid SIM card inserted.
     */
    private class InvalidDialogCallback implements
            KeyguardDialogManager.DialogShowCallBack {
        public void show() {
            String title = mContext.getString(R.string.invalid_sim_title);
            String message = mContext.getString(R.string.invalid_sim_message);
            AlertDialog dialog = createDialog(title, message);
            dialog.show();
        }
    }

    /**
      * M: The real show dialog callback when phone is ME locked.
      */
    private class MeLockedDialogCallback implements
            KeyguardDialogManager.DialogShowCallBack {
        public void show() {
            String title = null;
            String message = mContext.getString(R.string.simlock_slot_locked_message);
            AlertDialog dialog = createDialog(title, message);
            dialog.show();
        }
    }

    /**
     *  M: For showing invalid sim card dilaog if user insert a perm_disabled sim card.
     * @param title The invalid sim card alert dialog title.
     * @param message THe invalid sim catd alert dialog content.
     */
    private AlertDialog createDialog(String title, String message) {
        final AlertDialog dialog  =  new AlertDialog.Builder(mContext)
            .setTitle(title)
            .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .setMessage(message)
            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mDialogManager.reportDialogClose();
                    Log.d(TAG, "invalid sim card ,reportCloseDialog");
                }
            }).create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return dialog;
    }

    public boolean isKeyguardDoneOnGoing() {
        return mKeyguardDoneOnGoing;
    }

    /**
     * Is the keyguard currently showing?
     * @return mShowing
     */
    public boolean isShowing() {
        return mShowing;
    }

    public boolean isOccluded() {
        return mOccluded;
    }

    private boolean isSimLockedOrMissing(int phoneId, boolean requireSim) {
        IccCardConstants.State state = mUpdateMonitor.getSimStateOfPhoneId(phoneId);
        boolean simLockedOrMissing = (mUpdateMonitor.isSimPinSecure(phoneId))
                || ((state == IccCardConstants.State.ABSENT
                || state == IccCardConstants.State.PERM_DISABLED)
                && requireSim);
        return simLockedOrMissing;
    }

    /** M: add for vow @{
     * dismiss keyguard.
     */
    public void dismiss() {
        dismiss(false) ;
    }

    public void dismiss(boolean allowWhileOccluded) {
        mHandler.obtainMessage(DISMISS_INTERNAL, allowWhileOccluded ? 1 : 0, 0).sendToTarget();
    }

    public void handleDismissInternal(boolean allowWhileOccluded) {
        if (mShowing && (allowWhileOccluded || !mOccluded)) {
            mStatusBarKeyguardViewManager.dismiss(allowWhileOccluded);
        }
    }

    public boolean isKeyguardExternallyEnabled() {
        return mExternallyEnabled;
    }
    /// @}

    ///M: add for Nav @{
    void updateNavbarStatus() {
        Log.d(TAG, "updateNavbarStatus() is called.") ;
        mStatusBarKeyguardViewManager.updateStates() ;
    }
    /// @}
    
    //add BUG_ID:TBSW-52 liuzhijun 20180131 (start)
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isInteractive()) {
                keyguardDone();
            }
        }
    };
    //add BUG_ID:TBSW-52 liuzhijun 20180131 (end)

    //debuid:AQWL-44 add chenxuanyu start mKeyObserver
    private ContentObserver mKeyObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
			android.util.Log.i("xuanyukey","powerManager.isInteractive()="+powerManager.isInteractive());
            if (powerManager.isInteractive()) {
                keyguardDone();
            }
        }
    };
//debuid:AQWL-44 add chenxuanyu end mKeyObserver 

}