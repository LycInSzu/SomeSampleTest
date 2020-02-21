
package com.wtk.screenshot;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.wtk.screenshot.util.ShotUtil;
import com.wtk.screenshot.view.ScreenShotView;

import android.provider.Settings;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.util.Log;
import android.view.View;

public class ScreenShotEntrance {
    /* Common */
    private static final String TAG = ShotUtil.TAG;
    // Util
    private Context mContext;
    private ShotUtil mShotUtil;
    private WindowManager wm;
    private static WindowManager.LayoutParams params;

    private TelephonyManager telephonyManager;
    private ExPhoneCallListener myPhoneCallListener;

    private IntentFilter filterScreen;

    // Flag
    private boolean cancelInRingEnable = false;
    private boolean isRegister = false;

    /* View */
    private ScreenShotView mShotView;

    public ScreenShotEntrance(Context context) {
        /* Common */
        this.mContext = context;

        mShotUtil = ShotUtil.getInstance(mContext);
        telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneCallListener = new ExPhoneCallListener();

        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0,
                0,
                WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT);
        params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;

        filterScreen = new IntentFilter();
        filterScreen.addAction(Intent.ACTION_SCREEN_OFF);

        /* View */
        mShotView = new ScreenShotView(mContext);
    }

    public synchronized void show() {
        Log.i(TAG, "ScreenShotEntrance;show;isRegister=" + isRegister);
        if (!isRegister) {
            isRegister = true;
            telephonyManager.listen(myPhoneCallListener,
                    PhoneStateListener.LISTEN_CALL_STATE);

            //mContext.registerReceiver(mReceiverScreen, filterScreen);

            mContext.getApplicationContext()
                    .getContentResolver()
                    .registerContentObserver(
                            Settings.System
                                    .getUriFor("hall_state"),
                            true, hallObserver);
        }

        mShotView.reset();
        wm.addView(mShotView, params);
        mShotUtil.setScreenShotShowState(true);
    }

    public synchronized void cancel() {
        Log.i(TAG, "ScreenShotEntrance;cancel;isRegister=" + isRegister);
        wm.removeView(mShotView);
        mShotView.cancel();
        mShotUtil.setScreenShotShowState(false);
        if (isRegister) {
            isRegister = false;
            telephonyManager.listen(myPhoneCallListener,
                    PhoneStateListener.LISTEN_NONE);

            //mContext.unregisterReceiver(mReceiverScreen);

            mContext.getApplicationContext().getContentResolver()
                    .unregisterContentObserver(hallObserver);
        }
    }

    private BroadcastReceiver mReceiverScreen = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mShotUtil.cancel();
            }

        }
    };

    public void clear() {
        mShotView.clear();
    }

    public ScreenShotView getShotView() {
        return mShotView;
    }

    /* Phone ring listener */
    class ExPhoneCallListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    cancelInRingEnable = true;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    cancelInRingEnable = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (cancelInRingEnable) {
                        cancelInRingEnable = false;
                        mShotUtil.cancel();
                    }
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /* Hall */
    private ContentObserver hallObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            int HallState = Settings.System.getInt(mContext.getContentResolver(), "hall_state", 0);
            //int enable = Settings.System.getInt(mContext.getContentResolver(),
            //                      Settings.System.INTELLIGENTHOLSTER_SETTINGS, 0);
            if (HallState == 1) {
                mShotUtil.cancel();
            }
        }
    };

}
