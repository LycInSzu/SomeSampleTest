package com.pri.factorytest.Vibrate;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class Vibrate extends PrizeBaseActivity {
    private Handler mHandler = new Handler();
    private final long VIBRATOR_ON_TIME = 1000;
    private final long VIBRATOR_OFF_TIME = 500;
    private static final String TAG = "Vibrate";
    private Vibrator mVibrator = null;
    private PowerManager mPowerManager = null;
    private WakeLock mWakeLock = null;
    private long[] pattern = {VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME};

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vibrate);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mPowerManager = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "My Lock");
        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        mVibrator.cancel();
        mWakeLock.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mHandler.postDelayed(mRunnable, 0);
        mWakeLock.acquire();
        super.onResume();
    }

    private Runnable mRunnable = new Runnable() {

        public void run() {
            mHandler.removeCallbacks(mRunnable);
            mVibrator.vibrate(pattern, 0);
        }
    };
}
