package com.wtk.charge;

import android.app.Service;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.util.Log;

import com.android.keyguard.KeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.CommandQueue;

public class ChargeAnimationService extends Service implements CommandQueue.Callbacks{
    private final String TAG = "ChargeAnimationService";
    private final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    private final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
    private final String FROM_NOTIFICATION_EXTRA = "intent.extra.from.notification";
    private Context mContext;
    private int mBatteryLevel = 100;
    private int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    private int mPlugType = 0;
    private int mPhoneState;

    private WindowManager mWindowManager;
    private ChargeAnimationView mChargeAnimationView;
    private LinearLayout mLinearLayout;
    private IRemoveView iRemoveView;
    private WakefulnessLifecycle mWakefulnessLifecycle;
    private boolean isAnimationShowing = false;
    private boolean isAlarmShowing = false;
    private CommandQueue mCommandQueue;
    private IntentFilter filter;
    //add by wangjian for YJSQ-4078 20190606 start
    private boolean mEnableChargeAnimation = false;
    //add by wangjian for YJSQ-4078 20190606 end

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(ALARM_ALERT_ACTION);
        filter.addAction(ALARM_DONE_ACTION);
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        filter.addAction("onStartedGoingToSleep");

        mContext = getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(WindowManager.class);
        iRemoveView = new IRemoveView() {
            @Override
            public void remove() {
                cancelChargeAnimation();
            }
        };

        //mWakefulnessLifecycle = Dependency.get(WakefulnessLifecycle.class);
        //SysUiServiceProvider.getComponent(mContext, CommandQueue.class).addCallbacks(this);
        addCallbacks();
        //modify by wangjian for YJSQ-4078 20190606 start
        mEnableChargeAnimation  = Settings.System.getInt(mContext.getContentResolver(),"show_charge_animation", 0) == 1;
        if (mEnableChargeAnimation) {
            registerReceiver(mReceiver, filter);
            //mWakefulnessLifecycle.addObserver(mWakefulnessObserver);
        }
        //modify by wangjian for YJSQ-4078 20190606 start
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_charge_animation"), false, mShowChargeAnimationOberver, UserHandle.USER_ALL);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG,"onStart");
        addCallbacks();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy ");
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"action = " + action + " / userid = " + getUserId());
            addCallbacks();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);

                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;
                Log.i(TAG,"plugged = " + plugged + " / oldPlugged = " + oldPlugged);
                if (plugged && !oldPlugged) {
                    showChargeAnimation();
                } else if (!plugged && oldPlugged) {
                    cancelChargeAnimation();
                }
                updateChargeView();
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                mPhoneState = TelephonyManager.CALL_STATE_IDLE;
                for (int i = 0; i < KeyguardUtils.getNumOfPhone(); i++) {
                    int subId = KeyguardUtils.getSubIdUsingPhoneId(i);
                    int callState = TelephonyManager.getDefault().getCallState(subId);
                    if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        mPhoneState = callState;
                    } else if (callState == TelephonyManager.CALL_STATE_RINGING
                            && mPhoneState == TelephonyManager.CALL_STATE_IDLE) {
                        mPhoneState = callState;
                    }
                }
                Log.i(TAG,"mPhoneState = " + mPhoneState + " / state = " + state);
                if (state != null && !TelephonyManager.EXTRA_STATE_IDLE.equals(state) || mPhoneState != TelephonyManager.CALL_STATE_IDLE) {
                    cancelChargeAnimation();
                }
            } else if (action.equals(ALARM_ALERT_ACTION)) {
                isAlarmShowing = true;
                if (isAnimationShowing) {
                    cancelChargeAnimation();
                }
            } else if (action.equals(ALARM_DONE_ACTION)) {
                isAlarmShowing = false;
            } else if (action.equals("onStartedGoingToSleep")) {
                //modify by wangjian for YJSQ-4078 20190606 start
                if (mEnableChargeAnimation && mPlugType != 0) {
                    showChargeAnimation();
                }
                //modify by wangjian for YJSQ-4078 20190606 end
            } else if (action.equals(Intent.ACTION_USER_SWITCHED)) {
                cancelChargeAnimation();
            }
        }
    };

    private void showChargeAnimation() {
        Log.i(TAG,"showChargeAnimation isAnimationShowing = " + isAnimationShowing + " / isincts = " + SystemProperties.get("persist.sys.cts_deviceprofile","0"));
        //add by wangjian for YJSQ-4171 cts test 20190614 start
        if (SystemProperties.get("persist.sys.cts_deviceprofile","0").equals("1")) {
            return;
        }
        //add by wangjian for YJSQ-4171 cts test 20190614 end
        addCallbacks();
        if (isAnimationShowing || mPhoneState != TelephonyManager.CALL_STATE_IDLE || isAlarmShowing) {
            return;
        }
        addChargeAnimationView();
        updateChargeView();
    }

    private void cancelChargeAnimation() {
        if (mLinearLayout != null) {
            try {
                mLinearLayout.removeAllViews();
                mWindowManager.removeViewImmediate(mLinearLayout);
                Log.i(TAG,"remove charge view success" );
            } catch (Exception e){
                Log.e(TAG,"remove charge view error = " + e.getMessage());
            }
            isAnimationShowing = false;
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.inputFeatures |= WindowManager.LayoutParams.INPUT_FEATURE_DISABLE_HOME_KEY;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        mLayoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.FILL_PARENT;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.setTitle("ChargeAnimation" + getUserId());
        mLayoutParams.gravity = Gravity.CENTER;
        mLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        return mLayoutParams;
    }

    public interface IRemoveView{
        void remove();
    }

    private void addChargeAnimationView() {
        isAnimationShowing = true;
        mChargeAnimationView = new ChargeAnimationView(mContext);
        mChargeAnimationView.setIRemoveView(iRemoveView);
        mLinearLayout = new LinearLayout(mContext);
        mLinearLayout.addView(mChargeAnimationView, WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        mLinearLayout.setGravity(Gravity.CENTER);
        mWindowManager.addView(mLinearLayout, getWindowLayoutParams());
    }

    private void updateChargeView() {
        if (mChargeAnimationView != null) {
            mChargeAnimationView.setBattertPercent(mBatteryLevel);
            if (mBatteryStatus == BatteryManager.BATTERY_STATUS_FULL || mBatteryLevel == 100) {
                mChargeAnimationView.setChargeStatus(mContext.getResources().getString(R.string.expanded_header_battery_charged));
            } else {
                mChargeAnimationView.setChargeStatus(mContext.getResources().getString(R.string.expanded_header_battery_charging));
            }
            if (mBatteryStatus == BatteryManager.BATTERY_STATUS_FULL || mBatteryLevel == 100) {
                mChargeAnimationView.setChargeStatusImageVisibility(View.GONE);
            } else {
                mChargeAnimationView.setChargeStatusImageVisibility(View.VISIBLE);
            }
            if (mBatteryLevel <= 10) {
                mChargeAnimationView.setmChargeStatusImage(R.drawable.charge_flash_less);
            } else {
                mChargeAnimationView.setmChargeStatusImage(R.drawable.charge_flash_normal);
            }
        }
    }

    /*final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        @Override
        public void onFinishedGoingToSleep() {
        }

        @Override
        public void onStartedGoingToSleep() {
            if (mPlugType != 0) {
                Log.i(TAG,"onStartedGoingToSleep = " + getUserId());
                //mContext.sendBroadcast(new Intent("onStartedGoingToSleep"));
                mContext.sendBroadcastAsUser(new Intent("onStartedGoingToSleep"), UserHandle.ALL);
                showChargeAnimation();
            }
        }

        @Override
        public void onStartedWakingUp() {
        }
    };*/

    @Override
    public void onCameraLaunchGestureDetected(int source) {
        Log.i(TAG, "onCameraLaunchGestureDetected source = " + source);
        if (source == StatusBarManager.CAMERA_LAUNCH_SOURCE_POWER_DOUBLE_TAP) {
            cancelChargeAnimation();
        }
    }

    private void addCallbacks() {
        if (mCommandQueue == null) {
            mCommandQueue = SysUiServiceProvider.getComponent(mContext, CommandQueue.class);
            if (mCommandQueue != null) {
                mCommandQueue.addCallback(this);
            }
        }
    }

    //add by wangjian for charge animation start
    private ContentObserver mShowChargeAnimationOberver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mEnableChargeAnimation = Settings.System.getInt(mContext.getContentResolver(),"show_charge_animation", 0) == 1;
            Log.e("wangjian","mEnableChargeAnimation 222= " + mEnableChargeAnimation);
            if (mEnableChargeAnimation) {
                registerReceiver(mReceiver, filter);
                //mWakefulnessLifecycle.addObserver(mWakefulnessObserver);
            } else {
                cancelChargeAnimation();
                try {
                    unregisterReceiver(mReceiver);
                    //mWakefulnessLifecycle.removeObserver(mWakefulnessObserver);
                } catch (Exception e) {
                    Log.e(TAG,"unregisterReceiver error " + e.getMessage());
                }
            }
        }
    };
    //add by wangjian for charge animation end
}
