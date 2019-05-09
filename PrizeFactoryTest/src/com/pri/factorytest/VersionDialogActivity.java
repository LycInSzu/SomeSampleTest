package com.pri.factorytest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * AlertDialog used for PCBA VERSION.
 */

public class VersionDialogActivity extends Activity {

    private AlertDialog mAlertDialog;
    public final static String BLU_VERSION = "android_secret_code";
    public final static String PCBA_VERSION = "999";
    public final static String SW_VERSION = "74657799";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String secretcode = intent.getStringExtra(BLU_VERSION);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(null, null);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            finish();
        });
        alertDialogBuilder.setOnCancelListener((dialog) -> {
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            finish();
        });
        alertDialogBuilder.create();

        if (PCBA_VERSION.equals(secretcode)) {
            alertDialogBuilder.setTitle("PCBA VERSION");
            String pcba = android.os.SystemProperties.get("ro.pcba.version", "");
            if(TextUtils.isEmpty(pcba)){
                pcba = android.os.SystemProperties.get("ro.boot.hardware.sku", "");
            }
            String message = "\n" + pcba + "\n";
            alertDialogBuilder.setMessage(message);
        } else if (SW_VERSION.equals(secretcode)) {
            alertDialogBuilder.setTitle("SW VERSION");
            String message = "Internal version \n";
            String versionpPrize = android.os.SystemProperties.get("ro.build.custom_int_version", "");
            message = message + " " + versionpPrize + "\n";

            String versionpBlu = android.os.SystemProperties.get("ro.build.custom_version", "");
            message = message + "External version " + versionpBlu + "\n";

            alertDialogBuilder.setMessage(message);
        }

        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }
}
