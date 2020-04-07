package com.android.systemui.qs.tiles;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.graphics.Color;
import android.widget.Toast;

import com.android.systemui.R;

public class ScreenRecordingActivity extends Activity {
    private final String TAG = "CaptiveAct";
    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE   = 103;
    private MediaProjectionManager projectionManager;
    private DisplayMetrics metrics;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add by wangjian for EJQQQ-628 20200302 start
        Intent intent = getIntent();
        if (intent != null) {
            if (!intent.getBooleanExtra("status", true)) {
                Intent service = new Intent(this, RecordService.class);
                service.putExtra("status", false);
                startService(service);
                finish();
            }

            if (Settings.Global.getInt(getContentResolver(),"screen_record_state",0) == 1) {
                Intent service = new Intent(this, RecordService.class);
                service.putExtra("status", false);
                startService(service);
                finish();
            }
        }
        // Add by wangjian for EJQQQ-628 20200302 start
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);      
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Log.i(TAG, "onCreate");

        //add by wangjian for screen record 20200323 start
        Log.i(TAG,"RECORD_AUDIO = " + checkSelfPermission(Manifest.permission.RECORD_AUDIO));
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting permission for audio");
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_REQUEST_CODE);
        }
        Log.i(TAG,"WRITE_EXTERNAL_STORAGE = " + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting permission for WRITE_EXTERNAL_STORAGE");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        }
        //add by wangjian screen record 20200323 end

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.screen_record_title))
            .setMessage(getString(R.string.screen_record_context))
            .setPositiveButton(getString(R.string.screen_record_start), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "setPositiveButton start createScreenCaptureIntent");
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                }
            })
            .setNegativeButton(getString(R.string.screen_record_cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {                    
                    finish();
                }
            })
            .setCancelable(false)
            .create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode =" + requestCode + " / resultCode = " + resultCode);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent service = new Intent(this, RecordService.class);
            service.putExtra("status", true);
            service.putExtra("code", resultCode);
            service.putExtra("data", data);
            service.putExtra("width", metrics.widthPixels);
            service.putExtra("height", metrics.heightPixels);
            service.putExtra("density", metrics.densityDpi);
            startService(service);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult requestCode =" + requestCode + " / permissions = " + permissions[0] + " / grantResults = " + grantResults[0]);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }
}
