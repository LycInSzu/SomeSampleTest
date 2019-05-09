package com.pri.factorytest.Hall;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.CMDExecute;
import com.pri.factorytest.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CameraHall extends PrizeBaseActivity {
    private static final int MSG_CAMERA_LID = 0x1236;
    private static final String TAG = "CameraHall";
    private TextView resultShow;
    private TextView resultShow2;
    private String result = "";
    private String result2 = "";
    private String result_begin = "";
    private String result_begin2 = "";
    private List<String> cameraHallList = new ArrayList<>();
    private boolean isResultChange = false;
    private boolean isResultChange2 = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAMERA_LID:
                    List<String> catResult = (ArrayList<String>) msg.obj;
                    resultShow.setText(getString(R.string.camera_hall_one) + ":" + (catResult.size() > 0 ? catResult.get(0) : ""));
                    resultShow2.setText(getString(R.string.camera_hall_Two) + ":" + (catResult.size() > 1 ? catResult.get(1) : ""));
                    break;
            }
            mHandler.postDelayed(mCameraHallRunnable, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_hall);
        resultShow = findViewById(R.id.result_show);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        if (Utils.isFileExists("/proc/hall1/m1120_up")) {
            cameraHallList.add("/proc/hall1/m1120_up");
        }
        if (Utils.isFileExists("/proc/hall2/m1120_down")) {
            cameraHallList.add("/proc/hall2/m1120_down");
        }
        resultShow2 = findViewById(R.id.result_show2);
        resultShow2.setVisibility(cameraHallList.size() > 1 ? View.VISIBLE : View.GONE);

        List<String> strList = cameraHallList.stream().map(x -> catDevPath(x)).collect(Collectors.toList());
        if (strList.size() < 1) {
            return;
        }
        Optional.ofNullable(strList).ifPresent(x -> {
            result_begin = x.get(0).trim();
            if (x.size() > 1) {
                result_begin2 = x.get(1).trim();
            }
        });
    }

    Runnable mCameraHallRunnable = () -> {
        List<String> catResult = cameraHallList.stream().map(x -> catDevPath(x)).collect(Collectors.toList());
        int size = catResult.size();
        if (size < 1) {
            return;
        }
        Optional.ofNullable(catResult).ifPresent(x -> {
            result = x.get(0);
            if (!result.trim().equals(result_begin)) {
                isResultChange = true;
            }
            if (x.size() > 1) {
                result2 = x.get(1);
                if (!result2.trim().equals(result_begin2)) {
                    isResultChange2 = true;
                }
            }
        });
        Message message = new Message();
        message.obj = catResult;
        message.what = MSG_CAMERA_LID;
        mHandler.sendMessage(message);

        if (size == 1 && (isResultChange || isResultChange2)) {
            mButtonPass.setEnabled(true);
        }
        if (size > 1 && isResultChange && isResultChange2) {
            mButtonPass.setEnabled(true);
        }
    };

    public void onResume() {
        super.onResume();
        mHandler.post(mCameraHallRunnable);
    }

    public void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_CAMERA_LID);
        mHandler.removeCallbacks(mCameraHallRunnable);
    }

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
}
