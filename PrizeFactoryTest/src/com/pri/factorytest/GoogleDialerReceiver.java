package com.pri.factorytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.pri.factorytest.util.Utils;

import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class GoogleDialerReceiver extends BroadcastReceiver {

    private static final String TAG = "GoogleDialerReceiver";
    private static final String SECRET_CODE_ACTION
            = "android.provider.Telephony.SECRET_CODE";
    private final Uri mUri8801 = Uri.parse("android_secret_code://8801");
    private final Uri mUri8802 = Uri.parse("android_secret_code://8802");
    private final Uri mUri8803 = Uri.parse("android_secret_code://8803");
    private final Uri mUri8804 = Uri.parse("android_secret_code://8804");
    private final Uri mUri8805 = Uri.parse("android_secret_code://8805");
    private final Uri mUri8818 = Uri.parse("android_secret_code://8818");
    private final Uri mUri8823 = Uri.parse("android_secret_code://8823");

    private final Uri mUri74655577 = Uri.parse("android_secret_code://74655577");
    private final Uri mUri888 = Uri.parse("android_secret_code://888");

    private final Uri mUri999 = Uri.parse("android_secret_code://999");
    private final Uri mUri74657799 = Uri.parse("android_secret_code://74657799");
    private final String GOOGLE_DIALER = "com.google.android.dialer";
    private final String SYSTEM_DIALER = "com.android.dialer";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean hasGoogleNoSysDialer = Utils.hasComponentPkg(context, GOOGLE_DIALER)
                && !Utils.hasComponentPkg(context, SYSTEM_DIALER);

        //android.util.Log.i(TAG, "----hasGoogleNoSysDialer:" + hasGoogleNoSysDialer);
        if (!Stream.of("odm","BLU","customer").anyMatch(x -> PRIZE_CUSTOMER.equals(x)) || !hasGoogleNoSysDialer) {
            return;
        }

        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Uri uri = intent.getData();
            //android.util.Log.i(TAG, "Receive secret code intent and uri is " + uri);
            if (mUri8801.equals(uri)) {
                Intent intentSnInfo = new Intent(context, PrizeSnInfo.class);
                intentSnInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentSnInfo);
            } else if (mUri8802.equals(uri) || mUri888.equals(uri)) {
                Intent intentFactory = new Intent();
                intentFactory.setClass(context, PrizeHwInfo.class);
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
            } else if (mUri8803.equals(uri) || mUri74655577.equals(uri)) {
                Intent intentFactory = new Intent();
                intentFactory.setClassName("com.mediatek.engineermode", "com.mediatek.engineermode.EngineerMode");
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
            } else if (mUri8804.equals(uri)) {
                Intent intentFactory = new Intent(context, PrizeFactoryTestActivity.class);
                intentFactory.putExtra("isAutoTest", true);
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
            } else if (mUri8805.equals(uri)) {
                Intent intentAtaInfo = new Intent(context, AgingTestActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if (mUri8818.equals(uri)) {
                Intent intentAtaInfo = new Intent(context, PrizeAtaInfo.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if (mUri8823.equals(uri)) {
                Intent intentAtaInfo = new Intent(context, ChargerProtectActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if (mUri999.equals(uri)) {
                Intent intentAtaInfo = new Intent(context, VersionDialogActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentAtaInfo.putExtra(VersionDialogActivity.BLU_VERSION, VersionDialogActivity.PCBA_VERSION);
                context.startActivity(intentAtaInfo);
            } else if (mUri74657799.equals(uri)) {
                Intent intentAtaInfo = new Intent(context, VersionDialogActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentAtaInfo.putExtra(VersionDialogActivity.BLU_VERSION, VersionDialogActivity.SW_VERSION);
                context.startActivity(intentAtaInfo);
            }
        }
    }
}
