
package com.wtk.screenshot;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.screenshot.GlobalScreenshot;

public class WtkFreeScreenShot {
    // Default
    private static final String TAG = ShotUtil.TAG;
    // Util
    private Context mContext;
    private Handler mHandler = new Handler();
    private ShotUtil mShotUtil;
    private GlobalScreenshot mScreenshot;
    private static WtkFreeScreenShot instance;

    // TWJY-93 WTK_FUNNY_SCREEN_SHOT add by qiaojinzhao
    public synchronized static WtkFreeScreenShot setInstance(Context context) {
        // context can't be null
        Log.i(TAG, "WtkFreeScreenShot;setInstance,context != null?="
                + (context != null));
        if (context == null) {
            return null;
        }

        if (instance == null) {
            instance = new WtkFreeScreenShot(context);
        }
        return instance;
    }

    public synchronized static WtkFreeScreenShot getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }
    //TWJY-93 WTK_FUNNY_SCREEN_SHOT add by qiaojinzhao


    public WtkFreeScreenShot(Context context) {
        mContext = context;
        mScreenshot = new GlobalScreenshot(mContext);
        mShotUtil = ShotUtil.getInstance(mContext);
    }

    public void start(Runnable finishRunnable) {
        if (null == mContext) {
            return;
        }
        //mShotUtil = ShotUtil.getInstance(mContext);

        PowerManager mPowerManager = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = mPowerManager.isScreenOn();
        Log.i(TAG, "WtkFreeScreenShot;start;getLockState=" + mShotUtil.getLockState());
        if (isScreenOn && mShotUtil.getLockState() == ShotUtil.LOCK_STATE_NONE) {
            mShotUtil.setFinishRunnable(finishRunnable);
            mShotUtil.setGlobalScreenshot(mScreenshot);

            Vibrator vibrator = (Vibrator) mContext
                    .getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {100, 200};
            vibrator.vibrate(pattern, -1);

            mShotUtil.takeScreenshot();
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 300);
        }
    }

    public void saveFinish() {
        if (mShotUtil != null) {
            mShotUtil.clear();
        }
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if (mShotUtil.getFullScreenBitmap() == null) {
                return;
            }
            if (mShotUtil.getScreenShotEntrance() == null) {
                mShotUtil
                        .setScreenShotEntrance(new ScreenShotEntrance(mContext));
                mShotUtil.getScreenShotEntrance().show();
            } else if (mShotUtil.getScreenShotShowState()) {
                mShotUtil.getScreenShotEntrance().cancel();
                mHandler.postDelayed(mRunnable, 300);
            } else {
                mShotUtil.getScreenShotEntrance().show();
            }
        }
    };
}
