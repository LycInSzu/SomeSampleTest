package com.cydroid.longscreenshot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.cydroid.util.Log;

public class MyReceiver {

    private static String TAG = "MyReceiver";
    private static ScreenShotService mScreenService = null;

    public MyReceiver() {
        super();
    }

    public static void init(ScreenShotService mScreenShotService) {
        mScreenService = mScreenShotService;
    }

    public static BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {  //监听home键，如果按了home键则退出长截屏模式
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.v(TAG, "mHomeKeyEventReceiver   onReceive ");
            if (mScreenService.isSavingPicture) {
                Log.d(TAG, "mHomeKeyEventReceiver onReceive mScreenService.isSavingPicture==true, now return");
                mScreenService.setAnimationFlag(false);
                return;
            }
            String action = intent.getAction();
            Log.d(TAG, "mHomeKeyEventReceiver onReceive action = " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                Log.d(TAG, "mHomeKeyEventReceiver onReceive reason = " + reason);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    //表示按了home键,程序到了后台
                    Log.v(TAG, "mHomeKeyEventReceiver onReceive Home clicked  stopSelfBySelf");
                    mScreenService.stopSelfBySelf();
                } else if (TextUtils.equals(reason, RECENT_APPS)) {
                    Log.v(TAG, "mHomeKeyEventReceiver onReceive recent apps  clicked   stopSelfBySelf");
                    mScreenService.stopSelfBySelf();
                }
            }
        }
    };
    //CHENYEE 20170523 lixiaohong modify for #123637 begin
    public static final String CHENYEE_ACTION_PHONESTATE = "android.intent.action.PHONE_STATE";
    public static final String CHENYEE_ACTION_SINGLEHAND = "cy.oversea.SINGLE_HAND";
    public static final String CHENYEE_ACTION_ALARM = "com.android.deskclock.ALARM_ALERT";
    public static final String CHENYEE_ACTION_SCREENOFF = "android.intent.action.SCREEN_OFF";
    public static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver-onReceive() action = " + action);
            if (mScreenService.isSavingPicture) {
                mScreenService.setAnimationFlag(false);
                return;
            }
            if (action.equals(CHENYEE_ACTION_ALARM) || action.equals(CHENYEE_ACTION_SCREENOFF)) {
                Log.v(TAG, "receive a alarm broadcast,so longscreenshot will exit.");
                mScreenService.stopSelfBySelf();
            } else if (action.equals(CHENYEE_ACTION_SINGLEHAND)) {
                if (intent.getBooleanExtra("isSingleHand", false)) {
                    Log.v(TAG, "From normal mode to single hand mode,so longscreenshot will exit.");
                    mScreenService.stopSelfBySelf();
                }
            } else if (action.equals(CHENYEE_ACTION_PHONESTATE)) {
                // 如果是来电
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.v(TAG, "phoneStatReceiver call is comming stopSelfBySelf");
                        mScreenService.stopSelfBySelf();
                }
            }
        }
    };
    //CHENYEE 20170523 lixiaohong modify for #123637 end
}
