package com.pri.factorytest.CameraBackSub;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.CMDExecute;
import com.pri.factorytest.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class CameraFrontYuvSub extends PrizeBaseActivity {
    private TextView alsps;
    private TextView alsps1;
    Handler mhandle;
    Timer mTimer;
    private Camera mCamera1 = null;
    private List<String> mCameraBackSubPath = new ArrayList<String>();

    private String catDevPath(String path) {
        String result = null;
        try {
            CMDExecute cmdexe = new CMDExecute();
            String[] args = {"/system/bin/cat", path};
            result = cmdexe.run(args, "system/bin/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_back_sub_als);
        List<String> backSubOpenPath = new ArrayList<String>();
        if (Utils.isFileExists("/sys/kernel/dcam/dcam_f_value")) {
            mCameraBackSubPath.add("/sys/kernel/dcam/dcam_f_value");
            backSubOpenPath.add("/sys/kernel/dcam/dcam_f_open");
        }
        if (Utils.isFileExists("/sys/kernel/spc/spc_f/value")) {
            mCameraBackSubPath.add("/sys/kernel/spc/spc_f/value");
            backSubOpenPath.add("/sys/kernel/spc/spc_f/open");
        }
        alsps1 = findViewById(R.id.als_ps1);
        alsps1.setVisibility(mCameraBackSubPath.size() > 1? View.VISIBLE:View.GONE);

        alsps = (TextView) findViewById(R.id.als_ps);
        Utils.paddingLayout(findViewById(R.id.als_ps), 0, ACTIVITY_TOP_PADDING, 0, 0);
        try {
            mCamera1 = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            backSubOpenPath.stream().forEach(x -> catDevPath(x));
        } catch (Exception exception) {
            finish();
            mCamera1 = null;
            Log.e("CameraBackYuvSub", "onCreate() exception=" + String.valueOf(exception));
        }
        HandlerThread ht = new HandlerThread("catdevpath");
        ht.start();
        mhandle = new Handler(ht.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    List<String> catResults = mCameraBackSubPath.stream().map(c->catDevPath(c)).collect(Collectors.toList());
                    runOnUiThread(() -> {
                        alsps.setText(getString(R.string.camera_back_sub_tip) + " : " + catResults.get(0));
                        if (catResults.size() > 1) {
                            alsps1.setText(getString(R.string.camera_back_sub_tip) + "1 : " + catResults.get(1));
                        }
                        if (!catResults.stream().anyMatch(x->x.contains("No") || x.contains("-1"))) {
                            mButtonPass.setEnabled(true);
                        }
                    }
                    );
                }
            }
        };

        confirmButtonNonEnable();
    }

    protected void onResume() {
        super.onResume();
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mhandle.sendEmptyMessage(0x123);
            }
        }, 0, 300);
    }

    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void finish() {
        if (mCamera1 != null) {
            mCamera1.release();
            Log.e("tangan", "2");
        }
        super.finish();
    }
}
