package com.pri.factorytest.CameraBackSub;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
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

public class CameraBackYuvSub extends PrizeBaseActivity {
    private TextView alsps;
    private TextView alsps1;
    private Handler mhandle;
    private Timer mTimer;
    private volatile Camera mCamera = null;
    private List<String> mCameraBackSubPath = new ArrayList<String>();
    private List<String> mBackSubClosePath = new ArrayList<String>();
    private List<String> mBackSubOpenPath = new ArrayList<String>();

    private String catDevPath(String path) {
        String result = null;
        try {
            CMDExecute cmdexe = new CMDExecute();
            String[] args = {"/system/bin/cat", path};
            result = cmdexe.run(args, "system/bin/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //android.util.Log.i("ke.zeng", "----result:" + result);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_back_sub_als);
        if (Utils.isFileExists("/sys/kernel/dcam/dcam_r_value")) {
            mCameraBackSubPath.add("/sys/kernel/dcam/dcam_r_value");
            mBackSubOpenPath.add("/sys/kernel/dcam/dcam_r_open");
            mBackSubClosePath.add("/sys/kernel/dcam/dcam_r_close");
        }
        if (Utils.isFileExists("/sys/kernel/spc/spc_r/value")) {
            mCameraBackSubPath.add("/sys/kernel/spc/spc_r/value");
            mBackSubOpenPath.add("/sys/kernel/spc/spc_r/open");
            mBackSubClosePath.add("/sys/kernel/spc/spc_r/close");
        }
        if (Utils.isFileExists("/sys/kernel/spc/spc_r_1/value")) {
            mCameraBackSubPath.add("/sys/kernel/spc/spc_r_1/value");
            mBackSubOpenPath.add("/sys/kernel/spc/spc_r_1/open");
            mBackSubClosePath.add("/sys/kernel/spc/spc_r_1/close");
        }
        alsps1 = findViewById(R.id.als_ps1);
        alsps1.setVisibility(mCameraBackSubPath.size() > 1? View.VISIBLE:View.GONE);

        alsps = (TextView) findViewById(R.id.als_ps);
        Utils.paddingLayout(findViewById(R.id.als_ps), 0, ACTIVITY_TOP_PADDING, 0, 0);
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            finish();
            mCamera = null;
            Log.e("CameraBackYuvSub", "onCreate() exception=" + String.valueOf(e));
        }
        HandlerThread ht = new HandlerThread("catdevpath");
        ht.start();
        mhandle = new Handler(ht.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    List<String> catResults = mCameraBackSubPath.stream().map(c->catDevPath(c)).collect(Collectors.toList());
                    if (catResults.stream().anyMatch(x -> x.contains("No") || x.contains("-1"))) {
                        mBackSubOpenPath.stream().forEach(x -> catDevPath(x));
                        mhandle.sendEmptyMessage(0x123);
                    }
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
        mBackSubOpenPath.stream().forEach(x -> catDevPath(x));

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
        mBackSubOpenPath.stream().forEach(x -> catDevPath(x));
    }

    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //android.util.Log.e("ke.zeng", "-----onDestroy-----");
        mBackSubClosePath.stream().forEach(x -> catDevPath(x));
        if (mCamera != null) {
            mCamera.release();
            Log.e("tangan", "3");
        }
    }
}
