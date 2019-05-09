package com.pri.factorytest.Hall;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.CMDExecute;
import com.mediatek.common.prizeoption.NvramUtils;
import com.pri.factorytest.util.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CameraHallCali extends PrizeBaseActivity {
    private static final String TAG = "CameraHallCali";
    private final static String HALL_UP_PATH = "/proc/hall1/m1120_up";
    private final static String HALL_DOWN_PATH = "/proc/hall2/m1120_down";
    private final static String CALI_FLAG = "/proc/hall1/m1120_calibrate_flag";
    private final static int TESTCOUNT = 5;
    private TextView mLastTopResult;
    private TextView mLastBottomResult;
    private TextView mTopResult;
    private TextView mBottomResult;
    private Camera mCamera = null;
    private Handler mHandler = new Handler();
    private boolean upSuccess = false;
    private boolean downSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_hall_cali);
        confirmButtonNonEnable();
        initView();
    }

    private void initView() {
        mLastTopResult = findViewById(R.id.last_cali_result_top);
        mLastBottomResult = findViewById(R.id.last_cali_result_bottom);
        mTopResult = findViewById(R.id.cali_result_top);
        mBottomResult = findViewById(R.id.cali_result_bottom);
    }

    private void getLastResultAndShow() {
        String nvResult = Utils.readProInfo(Utils.PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET, 6);
        // up/top read 244  245  246 index, down/bottom read 247  248  249 index
        String cameraHallCaliNv = nvResult.substring(nvResult.length() - 6);
        String lastTop = cameraHallCaliNv.substring(0, 3);
        String lastBottom = cameraHallCaliNv.substring(3, cameraHallCaliNv.length());
        mLastTopResult.setText(getString(R.string.camera_hall_cali_up_top) + lastTop);
        mLastBottomResult.setText(getString(R.string.camera_hall_cali_down_bottom) + lastBottom);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastResultAndShow();
        mHandler.post(openCameraRunnable);
        mHandler.postDelayed(upRunnable, 2000);
        mHandler.postDelayed(closeCameraRunnable, 3000);
        mHandler.postDelayed(downRunnable, 5000);
    }

    private Runnable upRunnable = () -> {
        int average = -1;
        try {
            average = getTestResult(HALL_UP_PATH);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mTopResult.setText(getString(R.string.camera_hall_cali_up_top) + average);
        if (average < 250) {
            return;
        }
        //write prize factory info 244  245  246 index,use the first 3 bits
        String sn = formatResult(average);
        upSuccess = true;
        Utils.writeProInfo(sn, Utils.PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET);
        //write pro info 341  342  343 index,use the last 3 bits
        NvramUtils.writeNvramInfo(Utils.PRODUCT_INFO_FILENAME, Utils.PRODUCTINFO_CAMERA_HALL_CALI_OFFSET, sn.length(), sn);
    };

    private Runnable downRunnable = () -> {
        int average = -1;
        try {
            average = getTestResult(HALL_DOWN_PATH);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mBottomResult.setText(getString(R.string.camera_hall_cali_down_bottom) + average);
        if (average < 300) {
            return;
        }
        //write prize factory info 247  248  249 index,use the last 3 bits
        String sn = formatResult(average);
        downSuccess = true;
        Utils.writeProInfo(sn, Utils.PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET + 3);
        //write pro info 344  345  346 index,use the last 3 bits
        NvramUtils.writeNvramInfo(Utils.PRODUCT_INFO_FILENAME, Utils.PRODUCTINFO_CAMERA_HALL_CALI_OFFSET + 3, sn.length(), sn);
        if (upSuccess && downSuccess) {
            mButtonPass.setEnabled(true);
        }
    };

    private int getTestResult(String path) throws NumberFormatException {
        List<String> results = new ArrayList<>(TESTCOUNT);
        results.clear();
        for (int i = 0; i < TESTCOUNT; i++) {
            results.add(catDevPath(path));
        }
        int total = results.stream().map(x -> Integer.valueOf(x)).reduce(0, Integer::sum);
        int average = total / results.size();
        Log.i(TAG, "---up string average:" + average);
        return average;
    }

    private String formatResult(int value) {
        String nvValue;
        if (value >= 100) {
            nvValue = String.valueOf(value);
        } else if (value < 100 && value >= 10) {
            nvValue = "0" + String.valueOf(value);
        } else if (value < 10 && value >= 0) {
            nvValue = "00" + String.valueOf(value);
        } else {
            nvValue = "000";
        }
        return nvValue;
    }

    private Runnable openCameraRunnable = () -> {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.startPreview();
            writeFile("1", CALI_FLAG);
        } catch (Exception e) {
            writeFile("0", CALI_FLAG);
            e.printStackTrace();
        }
    };

    private Runnable closeCameraRunnable = () -> {
        Optional.ofNullable(mCamera).ifPresent(x -> {
            x.stopPreview();
            x.release();
            writeFile("0", CALI_FLAG);
        });
    };

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

    private static void writeFile(String data, String filePath){
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            byte[] bytes = data.getBytes();
            fout.write(bytes);
            fout.flush();
            fout.close();
            Log.e(TAG, "writeFile succcess");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
