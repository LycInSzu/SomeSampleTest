package com.pri.factorytest.FingerPrint;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodix.fingerprint.GFShenzhenConfig;
import com.goodix.fingerprint.ShenzhenConstants;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.utils.ShenzhenTestResultParser;
import com.goodix.fingerprint.utils.TestParamEncoder;
import com.goodix.fingerprint.utils.TestResultParser;
import com.pri.factorytest.FingerPrint.huiding.Util;
import com.pri.factorytest.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;

public class HuidingFingerActivity extends BaseActivity implements View.OnClickListener {

    enum STATE {
        NULL,
        FLESH,
        DARK,
        CHECKBOX,
        CHART,
    }

    public static final int WAIT_TIME = 500;
    public static final int MESSAGE_ID_TIMEOUT = 666;
    public static String TAG = "SPMTActivity";
    private GoodixFingerprintManager mGoodixFingerprintManager = null;
    private GFShenzhenConfig mConfig;
    private Button startBtn;
    private Button nextBtn;
    private TextView resutText;
    private TextView tipsText;
    private STATE state = STATE.NULL;
    private Test currentTest;
    private StringBuffer resultBuffer;
    private StringBuffer logBuffer;
    private String chipID;
    private SparseArray<String> errorCode;
    private Handler handler;
    SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss-SSS");
    SimpleDateFormat sdfLog = new SimpleDateFormat("yyyyMMdd-HHmmSS");
    private String timeTag;
    private boolean succeed = false;
    private int testError = 0;

    private int TS_min_expo_time = 0;
    private int TS_max_expo_time = 0;
    private int TS_badpointNum = 200;
    private int TS_clusterNum = 12;
    private int TS_pixelOfLargestBad = 30;
    private float TS_tnoise = 22.0f;
    private float TS_snoise = 150.0f;
    private float TS_lightLeakRatio = 0.8f;
    private float TS_fleshTouchDiff = 380.0f;
    private float TS_scale_min = 5.0f;
    private float TS_scale_max = 8.0f;
    private int TS_fov = 12800;
    private float TS_illuminace = 0.4f;
    private float TS_structratio = 0.13f;
    private float TS_ssnr = 3.0f;
    private float TS_sharpness = 0.3f;
    private float TS_contrast = 0.015f;
    private float TS_p2p = 30.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spmt1);
        displayBackButton();
        setTitleHeaderText(getResources().getString(R.string.k_b_calibration));
        startBtn = (Button) findViewById(R.id.spmt1_test_btn);
        nextBtn = (Button) findViewById(R.id.spmt1_next_btn);
        startBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        nextBtn.setEnabled(false);
        resutText = (TextView) findViewById(R.id.result_txt);
        resutText.setMovementMethod(ScrollingMovementMethod.getInstance());
        tipsText = (TextView) findViewById(R.id.tips_txt);
        mGoodixFingerprintManager = GoodixFingerprintManager.getFingerprintManager(this);
        mConfig = new GFShenzhenConfig(mGoodixFingerprintManager.getShenzhenConfig());
        mGoodixFingerprintManager.registerTestCmdCallback(mTestCmdCallback);
        mConfig.mSensorAreaBackgroundColor = 0xFF00FF00;
        mConfig.mSensorX = 446;
        mConfig.mSensorY = 1839;
        mConfig.mSensorWidth = 190;
        mConfig.mSensorHeight = 190;
        mConfig.mSensorLockAspectRatio = true;
        mConfig.mSensorAspectRatioWidth = 10;
        mConfig.mSensorAspectRatioHeight = 10;
        mGoodixFingerprintManager.setShenzhenConfig(mConfig);

        handler = new Handler(getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MESSAGE_ID_TIMEOUT && tipsText != null){
                    testError = 0x60;
                    if((currentTest != null)&& (currentTest.getDesc() != null)) {
                        tipsText.setText(currentTest.getDesc() + "  " + getString(R.string.errorcode_0x60));
                    }

                    startBtn.setEnabled(true);
                    return true;
                }
                return false;
            }
        });
        initTest();
        initErrorCode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoodixFingerprintManager.showSensorViewWindow(true);
        mGoodixFingerprintManager.setSensorAreaToBackgroundColor();
        mGoodixFingerprintManager.setSPMTMode(true);

        Util.setAccesibility(this,true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoodixFingerprintManager.showSensorViewWindow(false);
        mGoodixFingerprintManager.setSPMTMode(false);
        Util.setAccesibility(this,false);
        exitFT();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mGoodixFingerprintManager.setSPMTMode(false);
        mGoodixFingerprintManager.setHBMMode(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoodixFingerprintManager.setSPMTMode(false);
        //mGoodixFingerprintManager.setHBMMode(false);
    }

    /*
     * !!! important !!!
     * sleep sensor after SPMT test success/fail/pause
     * app should make sure this will be called!
     */
    private void exitFT(){
        FT_EXIT.go();
    }

    Test SPI;
    Test MT_CHECK;
    Test SPI_RST_INT;
    Test FT_INIT;
    Test AUTO_EXPOSURE;
    Test L1_FRESH;
    Test L2_FRESH;
    Test L1_DARK;
    Test L2_DARK;
    Test DARK_BASE;
    Test CHECKBOX;
    Test CHART;
    Test FT_EXIT;

    private void initTest() {
        SPI = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_SPI,
                getString(R.string.spmt_desc_SPI), true);
        MT_CHECK = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_MT_CHECK,
                getString(R.string.spmt_desc_MT), true);
        SPI_RST_INT = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_SPI_RST_INT,
                getString(R.string.spmt_desc_SPI_RST_INT), true);
        FT_INIT = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_INIT,
                null, false);
        AUTO_EXPOSURE = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION,
                getString(R.string.spmt_desc_AUTO_EXPOSURE), true);
        L1_FRESH = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_FLESH,
                getString(R.string.spmt_desc_L1_FRESH), true);
        L2_FRESH = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_FLESH,
                getString(R.string.spmt_desc_L2_FRESH), true);
        L1_DARK = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_DARK,
                getString(R.string.spmt_desc_L1_DARK), true);
        L2_DARK = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_DARK,
                getString(R.string.spmt_desc_L2_DARK), true);
        DARK_BASE = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_DARK_BASE,
                getString(R.string.spmt_desc_DARK_BASE), true);
        CHECKBOX = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHECKBOX,
                getString(R.string.spmt_desc_CHECKBOX), true);
        CHART = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHART,
                getString(R.string.spmt_desc_CHART), true);
        FT_EXIT = new CMDTest(ShenzhenConstants.CMD_TEST_SZ_FT_EXIT,
                null, false);
    }

    private void initErrorCode() {
        errorCode = new SparseArray<>();
        errorCode.append(0x00, getString(R.string.errorcode_0x00));
        errorCode.append(0x10, getString(R.string.errorcode_0x10));
        errorCode.append(0x11, getString(R.string.errorcode_0x11));
        errorCode.append(0x12, getString(R.string.errorcode_0x12));
        errorCode.append(0x13, getString(R.string.errorcode_0x13));
        errorCode.append(0x14, getString(R.string.errorcode_0x14));
        errorCode.append(0x15, getString(R.string.errorcode_0x15));
        errorCode.append(0x16, getString(R.string.errorcode_0x16));
        errorCode.append(0x17, getString(R.string.errorcode_0x17));
        errorCode.append(0x18, getString(R.string.errorcode_0x18));
        errorCode.append(0x19, getString(R.string.errorcode_0x19));
        errorCode.append(0x1A, getString(R.string.errorcode_0x1A));
        errorCode.append(0x1B, getString(R.string.errorcode_0x1B));
        errorCode.append(0x1C, getString(R.string.errorcode_0x1C));
        errorCode.append(0x1D, getString(R.string.errorcode_0x1D));
        errorCode.append(0x1E, getString(R.string.errorcode_0x1E));
        errorCode.append(0x20, getString(R.string.errorcode_0x20));
        errorCode.append(0x21, getString(R.string.errorcode_0x21));
        errorCode.append(0x22, getString(R.string.errorcode_0x22));
        errorCode.append(0x23, getString(R.string.errorcode_0x23));
        errorCode.append(0x24, getString(R.string.errorcode_0x24));
        errorCode.append(0x25, getString(R.string.errorcode_0x25));
        errorCode.append(0x26, getString(R.string.errorcode_0x26));
        errorCode.append(0x27, getString(R.string.errorcode_0x27));
        errorCode.append(0x28, getString(R.string.errorcode_0x28));
        errorCode.append(0x29, getString(R.string.errorcode_0x29));
        errorCode.append(0x2C, getString(R.string.errorcode_0x2C));
        errorCode.append(0x2D, getString(R.string.errorcode_0x2D));
        errorCode.append(0x2E, getString(R.string.errorcode_0x2E));
        errorCode.append(0x2F, getString(R.string.errorcode_0x2F));
        errorCode.append(0x30, getString(R.string.errorcode_0x30));
        errorCode.append(0x31, getString(R.string.errorcode_0x31));
        errorCode.append(0x32, getString(R.string.errorcode_0x32));
        errorCode.append(0x33, getString(R.string.errorcode_0x33));
        errorCode.append(0x34, getString(R.string.errorcode_0x34));
        errorCode.append(0x35, getString(R.string.errorcode_0x35));
        errorCode.append(0x36, getString(R.string.errorcode_0x36));
        errorCode.append(0x37, getString(R.string.errorcode_0x37));
        errorCode.append(0x38, getString(R.string.errorcode_0x38));
        errorCode.append(0x39, getString(R.string.errorcode_0x39));
        errorCode.append(0x3A, getString(R.string.errorcode_0x3A));
        errorCode.append(0x3B, getString(R.string.errorcode_0x3B));
        errorCode.append(0x3C, getString(R.string.errorcode_0x3C));
        errorCode.append(0x3D, getString(R.string.errorcode_0x3D));
        errorCode.append(0x5D, getString(R.string.errorcode_0x5D));
        errorCode.append(0x5E, getString(R.string.errorcode_0x5E));
        errorCode.append(0x60, getString(R.string.errorcode_0x60));
        errorCode.append(0x70, getString(R.string.errorcode_0x70));
        errorCode.append(0x71, getString(R.string.errorcode_0x71));
        errorCode.append(0x72, getString(R.string.errorcode_0x72));
        errorCode.append(0x73, getString(R.string.errorcode_0x73));
        errorCode.append(0x74, getString(R.string.errorcode_0x74));
        errorCode.append(0x75, getString(R.string.errorcode_0x75));
        errorCode.append(0x76, getString(R.string.errorcode_0x76));
        errorCode.append(0x77, getString(R.string.errorcode_0x77));
        errorCode.append(0x78, getString(R.string.errorcode_0x78));
        errorCode.append(0x79, getString(R.string.errorcode_0x79));
        errorCode.append(0x7A, getString(R.string.errorcode_0x7A));
        errorCode.append(0x7B, getString(R.string.errorcode_0x7B));
        errorCode.append(0x7C, getString(R.string.errorcode_0x7C));
        errorCode.append(0x7D, getString(R.string.errorcode_0x7D));
        errorCode.append(0x7E, getString(R.string.errorcode_0x7E));
        errorCode.append(0x7F, getString(R.string.errorcode_0x7F));
        errorCode.append(0x80, getString(R.string.errorcode_0x80));
        errorCode.append(0x81, getString(R.string.errorcode_0x81));
        errorCode.append(0x82, getString(R.string.errorcode_0x82));
        errorCode.append(0x83, getString(R.string.errorcode_0x83));
        errorCode.append(0x84, getString(R.string.errorcode_0x84));
        errorCode.append(0x85, getString(R.string.errorcode_0x85));
        errorCode.append(0x86, getString(R.string.errorcode_0x86));
        errorCode.append(0x87, getString(R.string.errorcode_0x87));
        errorCode.append(0x88, getString(R.string.errorcode_0x88));
        errorCode.append(0x89, getString(R.string.errorcode_0x89));
        errorCode.append(0x8A, getString(R.string.errorcode_0x8A));
        errorCode.append(0xF8, getString(R.string.errorcode_0xF8));
        errorCode.append(0xFB, getString(R.string.errorcode_0xFB));
        errorCode.append(0xFC, getString(R.string.errorcode_0xFC));
        errorCode.append(0xFD, getString(R.string.errorcode_0xFD));
        errorCode.append(0xFE, getString(R.string.errorcode_0xFE));
        errorCode.append(0xFF, getString(R.string.errorcode_0xFF));
    }

    private String getErrorDes(int code) {
        return errorCode.get(code, getString(R.string.errorcode_null));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.spmt1_test_btn: {
                startTest();
                break;
            }
            case R.id.spmt1_next_btn: {
                nextBtn.setEnabled(false);
                gotoTest();
                break;
            }
            default:
                break;
        }
    }

    private void startTest() {
        SPI.go();
        state = STATE.NULL;
        testError = 0;
        tipsText.setText("");
        resutText.setText("");
        resutText.invalidate();
        resultBuffer = new StringBuffer();
        logBuffer = new StringBuffer();
        startBtn.setEnabled(false);
        succeed = false;
        chipID = "";
        timeTag = sdfLog.format(new Date());
    }

    private void gotoTest() {
        tipsText.setText("");
        switch (state) {
            case FLESH: {
                testFresh();
                break;
            }
            case DARK: {
                testDark();
                break;
            }
            case CHECKBOX: {
                testCheckBox();
                break;
            }
            case CHART: {
                testChart();
                break;
            }
            default:
                break;
        }
    }

    interface Test {
        void go();
        String getDesc();
    }

    class CMDTest implements Test {

        private int cmdId;
        private String desc;
        private boolean timeout;

        public CMDTest(int cmdId, String desc, boolean timeout) {
            this.cmdId = cmdId;
            this.desc = desc;
            this.timeout = timeout;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public void go() {
            currentTest = this;
            Log.e(TAG, "sending cmdId: " + cmdId);
            if (cmdId == ShenzhenConstants.CMD_TEST_SZ_FT_INIT){
                String path = chipID + "/" + timeTag;
                int offset = 0;
                byte[] dataBytes = new byte[TestParamEncoder.testEncodeSizeOfArray(path.getBytes().length)];
                TestParamEncoder.encodeArray(dataBytes, offset, TestResultParser.TEST_TOKEN_METADATA, path.getBytes(),
                        path.getBytes().length);
                mGoodixFingerprintManager.testCmd(cmdId, dataBytes);
                Log.e(TAG, "sending CMD_TEST_SZ_FT_INIT: " + path);
            } else {
                mGoodixFingerprintManager.testCmd(cmdId);
            }
            if (timeout){
                Message intMess = Message.obtain();
                intMess.what = MESSAGE_ID_TIMEOUT;
                handler.sendMessageDelayed(intMess, 20000);
            }
        }
    }

    private void runWithDelay(final Test test){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                test.go();
            }
        }, WAIT_TIME);
    }

    //------------------FRESH--------------------
    private void preFlesh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATE.FLESH;
                FT_INIT.go();
                tipsText.setText(getString(R.string.spmt_fresh));
                nextBtn.setEnabled(true);
            }
        });
    }

    private void testFresh() {
        mGoodixFingerprintManager.setHBMMode(true);
        runWithDelay(AUTO_EXPOSURE);
    }

    private void postFresh() {
        mGoodixFingerprintManager.setHBMMode(false);
        mGoodixFingerprintManager.setScreenBrightnessR15(100);
        runWithDelay(L2_FRESH);
    }

    //------------------DARK-----------------
    private void preDark() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATE.DARK;
                tipsText.setText(getString(R.string.spmt_dark));
                nextBtn.setEnabled(true);
            }
        });
    }

    private void testDark() {
		
		Log.e(TAG, "testDark ");
        mGoodixFingerprintManager.setHBMMode(true);
        runWithDelay(L1_DARK);
    }

    private void testDark2() {
		Log.e(TAG, "testDark2 ");
		
        mGoodixFingerprintManager.setHBMMode(false);
        mGoodixFingerprintManager.setScreenBrightnessR15(100);
        runWithDelay(L2_DARK);
    }

    private void postDark() {
        //mGoodixFingerprintManager.goToSleep();
		Log.e(TAG, "postDark ");
        mGoodixFingerprintManager.setScreenBrightnessR15(0);
        //mGoodixFingerprintManager.showSensorViewWindow(false);
        runWithDelay(DARK_BASE);
    }

    //------------------CB-----------------
    private void preCheckBox() {
        //mGoodixFingerprintManager.wakeUp();
        
		Log.e(TAG, "preCheckBox ");
        mGoodixFingerprintManager.setScreenBrightnessR15(100);
        //mGoodixFingerprintManager.showSensorViewWindow(true);
        //mGoodixFingerprintManager.setSensorAreaToBackgroundColor();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATE.CHECKBOX;
                tipsText.setText(getString(R.string.spmt_checkbox));
                nextBtn.setEnabled(true);
            }
        });

    }

    private void testCheckBox() {
        mGoodixFingerprintManager.setHBMMode(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CHECKBOX.go();
            }
        }, WAIT_TIME);
    }

    //------------------CH-----------------
    private void preChart() {
        state = STATE.CHART;
        testChart();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                state = STATE.CHART;
//                tipsText.setText(getString(R.string.spmt_chart));
//                nextBtn.setEnabled(true);
//            }
//        });
    }

    private void testChart() {
        mGoodixFingerprintManager.setHBMMode(true);
        runWithDelay(CHART);
    }

    private void printResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultBuffer.append(sdf.format(new Date()) + " " + result + "\n");
                resutText.setText(resultBuffer.toString());

                int offset = resutText.getLineCount() * resutText.getLineHeight();
                Log.e(TAG, "printResult offset: " + offset);

                if (offset > resutText.getHeight()) {
                    resutText.scrollTo(0, offset - resutText.getHeight());
                } else {
                    resutText.scrollTo(0, 0);
                }
                resutText.invalidate();
            }
        });

    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) {
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString().toUpperCase();
    }

    private void filterThreshold(int cmdId, HashMap<Integer, Object> result) {

        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_EXPO_TIME)) {
            TS_min_expo_time = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_EXPO_TIME);
            Log.e(TAG, "filterThreshold TS_min_expo_time: " + TS_min_expo_time);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_EXPO_TIME)) {
            TS_max_expo_time = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_EXPO_TIME);
            Log.e(TAG, "filterThreshold TS_max_expo_time: " + TS_max_expo_time);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_BAD_POINT_NUM)) {
            TS_badpointNum = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_BAD_POINT_NUM);
            Log.e(TAG, "filterThreshold TS_badpointNum: " + TS_badpointNum);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_CLUSTER_NUM)) {
            TS_clusterNum = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_CLUSTER_NUM);
            Log.e(TAG, "filterThreshold TS_clusterNum: " + TS_clusterNum);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_PIXEL_OF_LARGEST_BAD_CLUSTER)) {
            TS_pixelOfLargestBad = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_PIXEL_OF_LARGEST_BAD_CLUSTER);
            Log.e(TAG, "filterThreshold TS_pixelOfLargestBad: " + TS_pixelOfLargestBad);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_NOISET)) {
            TS_tnoise = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_NOISET);
            Log.e(TAG, "filterThreshold TS_tnoise: " + TS_tnoise);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_NOISES)) {
            TS_snoise = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_NOISES);
            Log.e(TAG, "filterThreshold TS_snoise: " + TS_snoise);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_LEAK_RATIO)) {
            TS_lightLeakRatio = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_LIGHT_LEAK_RATIO);
            Log.e(TAG, "filterThreshold TS_lightLeakRatio: " + TS_lightLeakRatio);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_FLESH_TOUCH_DIFF)) {
            TS_fleshTouchDiff = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_FLESH_TOUCH_DIFF);
            Log.e(TAG, "filterThreshold TS_fleshTouchDiff: " + TS_fleshTouchDiff);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_SCALE_RATIO)) {
            TS_scale_min = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_SCALE_RATIO);
            Log.e(TAG, "filterThreshold TS_scale_min: " + TS_scale_min);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_SCALE_RATIO)) {
            TS_scale_max = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_SCALE_RATIO);
            Log.e(TAG, "filterThreshold TS_scale_max: " + TS_scale_max);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_FOV_AREA)) {
            TS_fov = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_FOV_AREA);
            Log.e(TAG, "filterThreshold TS_fov: " + TS_fov);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_RELATIVE_ILLUMINANCE)) {
            TS_illuminace = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_RELATIVE_ILLUMINANCE);
            Log.e(TAG, "filterThreshold TS_illuminace: " + TS_illuminace);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MAX_SCREEN_STRUCT_RATIO)) {
            TS_structratio = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MAX_SCREEN_STRUCT_RATIO);
            Log.e(TAG, "filterThreshold TS_structratio: " + TS_structratio);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_SSNR)) {
            TS_ssnr = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_SSNR);
            Log.e(TAG, "filterThreshold TS_ssnr: " + TS_ssnr);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_SHAPENESS)) {
            TS_sharpness = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_SHAPENESS);
            Log.e(TAG, "filterThreshold TS_sharpness: " + TS_sharpness);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_CHART_CONSTRAST)) {
            TS_contrast = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_CHART_CONSTRAST);
            Log.e(TAG, "filterThreshold TS_contrast: " + TS_contrast);
        }
        if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_MIN_P2P)) {
            TS_p2p = (Float) result.get(ShenzhenTestResultParser.TEST_TOKEN_MIN_P2P);
            Log.e(TAG, "filterThreshold TS_p2p: " + TS_p2p);
        }
    }

    private void filterMessage(int cmdId, HashMap<Integer, Object> result) {
        int error = 0;

        if (result.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
            error = (Integer) result.get(TestResultParser.TEST_TOKEN_ERROR_CODE);
        }

        switch (cmdId) {
            case ShenzhenConstants.CMD_TEST_SZ_FT_SPI: {
                Log.e(TAG, "CMD_TEST_SZ_FT_SPI");
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_CHIP_ID)) {
                    byte[] id = (byte[]) result.get(ShenzhenTestResultParser.TEST_TOKEN_CHIP_ID);
                    chipID = bytesToHex(id);
                    Log.e(TAG, "CMD_TEST_SZ_FT_SPI chipID: " + chipID);
                }
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_MT_CHECK: {
                Log.e(TAG, "CMD_TEST_SZ_FT_MT_CHECK");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_SPI_RST_INT: {
                Log.e(TAG, "CMD_TEST_SZ_FT_SPI_RST_INT");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_INIT: {
                Log.e(TAG, "CMD_TEST_SZ_FT_INIT");
                filterThreshold(cmdId, result);
                break;
            }

            case ShenzhenConstants.CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION: {
                Log.e(TAG, "CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION");
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_SHORT_EXPOSURE_TIME)) {
                    int expo = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_SHORT_EXPOSURE_TIME);
                    printResult("exposure time:" + expo + ", threshold: " + TS_min_expo_time + " - " + TS_max_expo_time);
                    //printResult("exposure time: " + expo);
                }
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_FLESH: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_H_FLESH");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_FLESH: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_L_FLESH");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_DARK: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_H_DARK");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_DARK: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_L_DARK");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_DARK_BASE: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_DARK_BASE");
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHECKBOX: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_CHECKBOX");
                int badpointNum = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_BAD_POINT_NUM)) {
                    badpointNum = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_BAD_POINT_NUM);
                    printResult("bad point:" + badpointNum + ", threshold: " + TS_badpointNum);
                    logBuffer.append("," + badpointNum);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX badpointNum :" + badpointNum);
                } else {
                    logBuffer.append(",");
                }
                int clusterNum = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_CLUSTER_NUM)) {
                    clusterNum = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_CLUSTER_NUM);
                    printResult("cluster number:" + clusterNum + ", threshold: " + TS_clusterNum);
                    logBuffer.append("," + clusterNum);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX clusterNum :" + clusterNum);
                } else {
                    logBuffer.append(",");
                }
                int pixelOfLargestBad = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_PIXEL_OF_LARGEST_BAD_CLUSTER)) {
                    pixelOfLargestBad = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_PIXEL_OF_LARGEST_BAD_CLUSTER);
                    printResult("pixel of largest bad:"+ pixelOfLargestBad + ",threshold: "+ TS_pixelOfLargestBad);
                    logBuffer.append("," + pixelOfLargestBad);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX pixelOfLargestBad :" + pixelOfLargestBad);
                } else {
                    logBuffer.append(",");
                }
                int tnoise = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_TNOISE)) {
                    tnoise = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_TNOISE);
                    printResult("tnoise:"+ (double)tnoise/1024 + ",threshold:"+ TS_tnoise);
                    logBuffer.append("," + (double)tnoise/1024);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX tnoise :" + tnoise);
                } else {
                    logBuffer.append(",");
                }
                int snoise = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_SNOISE)) {
                    snoise = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_SNOISE);
                    printResult("snoise:"+ (double)snoise/1024 + ",threshold: "+ TS_snoise);
                    logBuffer.append("," + (double)snoise/1024);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX SNoise :" + snoise);
                } else {
                    logBuffer.append(",");
                }
                int lightLeakRatio = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_LIGHT_LEAK_RATIO)) {
                    lightLeakRatio = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_LIGHT_LEAK_RATIO);
                    logBuffer.append("," + (double)lightLeakRatio/256);
                    printResult("leak ratio:"+ (double)lightLeakRatio/256 + ",threshold: " + TS_lightLeakRatio);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX lightLeakRatio :" + lightLeakRatio);
                } else {
                    logBuffer.append(",");
                }
                int fleshTouchDiff = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FLESH_TOUCH_DIFF)) {
                    fleshTouchDiff = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FLESH_TOUCH_DIFF);
                    logBuffer.append("," + (double)fleshTouchDiff/256);
                    printResult("flesh touch diff:"+(double)fleshTouchDiff/256 + ",threshold: "+ TS_fleshTouchDiff);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX fleshTouchDiff :" + fleshTouchDiff);
                } else {
                    logBuffer.append(",");
                }
                int scale = 0;
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_SCALE)) {
                    scale = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_SCALE);
                    printResult("scale:"+ (double)scale/1024 + ",threshold: "+ TS_scale_min + " - " + TS_scale_max);
                    logBuffer.append("," + (double)scale/1024);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX scale :" + scale);
                } else {
                    logBuffer.append(",");
                }

                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_LEFT) &&
                        result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_RIGHT) &&
                        result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_DOWN) &&
                        result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_UP)) {
                    int fovLeft = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_LEFT);
                    int fovRight = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_RIGHT);
                    int fovDown = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_DOWN);
                    int fovUp = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_FOV_UP);

                    int transfer = (fovRight - fovLeft)*(fovDown - fovUp);
                    logBuffer.append("," + Math.abs(transfer));
                    printResult("fov are:"+Math.abs(transfer) + ",threshold: "+ TS_fov);

                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX fovLeft :" + fovLeft);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX fovRight :" + fovRight);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX fovDown :" + fovDown);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX fovUp :" + fovUp);
                } else {
                    logBuffer.append(",");
                }
                byte [] ri = {0};
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_RELATIVE_ILLUMINANCE)) {
                    ri = (byte[]) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTORY_RELATIVE_ILLUMINANCE);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX relativeIlluminace length :" + ri.length);

                    int rii1 = ((ri[0] & 0xff) | ((ri[1] & 0xff) << 8) | ((ri[2] & 0xff) << 16) | ((ri[3] & 0xff) << 24));
                    int rii2 = ((ri[4] & 0xff) | ((ri[5] & 0xff) << 8) | ((ri[6] & 0xff) << 16) | ((ri[7] & 0xff) << 24));
                    int rii3 = ((ri[8] & 0xff) | ((ri[9] & 0xff) << 8) | ((ri[10] & 0xff) << 16) | ((ri[11] & 0xff) << 24));
                    int rii4 = ((ri[12] & 0xff) | ((ri[13] & 0xff) << 8) | ((ri[14] & 0xff) << 16) | ((ri[15] & 0xff) << 24));

                    float ri1 = (float)rii1/256;
                    float ri2 = (float)rii2/256;
                    float ri3 = (float)rii3/256;
                    float ri4 = (float)rii4/256;

                    String content = ri1 + ", " + ri2 + ", " + ri3 + ", " + ri4;
                    logBuffer.append(",\"" + content + "\"");
                    printResult("Relative illuminace:"+ content + ",threshold: "+ TS_illuminace);

                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX relativeIlluminace :" + ri1);
                } else {
                    logBuffer.append(",");
                }

                if (result.containsKey(ShenzhenTestResultParser. TEST_TOKEN_FACTROY_SCREEN_STRUCT_RATIO)) {
                    int ratio = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_FACTROY_SCREEN_STRUCT_RATIO);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHECKBOX ratio :" + ratio);
                    printResult("struct ratio: " + (float)ratio/1024 + " threshold: " + TS_structratio);
                    logBuffer.append("," + (float)ratio/1024);
                } else {
                    logBuffer.append(",");
                }
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHART: {
                Log.e(TAG, "CMD_TEST_SZ_FT_CAPTURE_CHART");
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_NOISE)) {
                    int noise = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_NOISE);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART noise :" + noise);
                    logBuffer.append("," + (float)noise);
                } else {
                    logBuffer.append(",");
                }
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_SSNR)) {
                    int ssnr = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_SSNR);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART ssnr :" + ssnr);
                    printResult("ssnr: " + (float)ssnr/256 + " threshold: " + TS_ssnr);
                    logBuffer.append("," + (float)ssnr/256);
                } else {
                    logBuffer.append(",");
                }
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_SHARPNESS)) {
                    int sharpness = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_SHARPNESS);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART sharpness :" + sharpness);
                    printResult("sharpness: " + (float)sharpness/1048576 + " threshold: " + TS_sharpness);
                    logBuffer.append("," + (float)sharpness/1048576);
                } else {
                    logBuffer.append(",");
                }
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_CHART_CONTRAST)) {
                    int contrast = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_CHART_CONTRAST);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART chartContrast :" + contrast);
                    printResult("contrast: " + (float)contrast/4096 + " threshold: " + TS_contrast);
                    logBuffer.append("," + (float)contrast/4096);
                } else {
                    logBuffer.append(",");
                }
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_P2P)) {
                    int p2p = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_P2P);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART p2p :" + p2p);
                    printResult("p2p: " + (float)p2p/4096 + " threshold: " + TS_p2p);
                    logBuffer.append("," + (float)p2p/4096);
                } else {
                    logBuffer.append(",");
                }
                if (result.containsKey(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_CHART_TOUCH_DIFF)) {
                    int chartTouchDiff = (Integer) result.get(ShenzhenTestResultParser.TEST_TOKEN_PERFORMANCE_CHART_TOUCH_DIFF);
                    Log.d(TAG, "onTestCmd cmdId = CMD_TEST_SZ_FT_CAPTURE_CHART chartTouchDiff :" + chartTouchDiff);
                    logBuffer.append("," + chartTouchDiff);
                } else {
                    logBuffer.append(",");
                }

                // add for kill fingerprint service
                closeLinuxProcess(getPID("biometrics.fingerprint@2.1-service"));

                break;
            }
            default:
                break;
        }

        final int finalError = error;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (finalError != 0) {
                    tipsText.setText(getErrorDes(finalError));
                    if((currentTest != null)&& (currentTest.getDesc() != null)) {
                        printResult(currentTest.getDesc() + "  " + getString(R.string.spmt_desc_failed));
                    }

                    startBtn.setEnabled(true);
                    nextBtn.setEnabled(false);
                } else {
                    if((currentTest != null)&& (currentTest.getDesc() != null)) {
                        printResult(currentTest.getDesc() + "  " + getString(R.string.spmt_desc_success));
                    }
                }
            }
        });
    }

    private void continueTest(int cmdId, HashMap<Integer, Object> result) {
        switch (cmdId) {
            case ShenzhenConstants.CMD_TEST_SZ_FT_SPI: {
                MT_CHECK.go();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_MT_CHECK: {
                SPI_RST_INT.go();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_SPI_RST_INT: {
                preFlesh();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_EXPO_AUTO_CALIBRATION: {
                L1_FRESH.go();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_FLESH: {
                postFresh();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_FLESH: {
                preDark();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_H_DARK: {
                testDark2();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_L_DARK: {
                postDark();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_DARK_BASE: {
                preCheckBox();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHECKBOX: {
                preChart();
                break;
            }
            case ShenzhenConstants.CMD_TEST_SZ_FT_CAPTURE_CHART: {
                if(testError == 0){
                    succeed = true;
                }
                HuidingFingerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tipsText.setText(getString(R.string.spmt_finish));
                        startBtn.setEnabled(true);
                        //prize zengke add
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(20);
                                HuidingFingerActivity.this.finish();
                            }
                        }, 1500);
                    }
                });
                break;
            }
            default:
                break;
        }
    }

    private GoodixFingerprintManager.TestCmdCallback mTestCmdCallback = new GoodixFingerprintManager.TestCmdCallback() {

        @Override
        public void onTestCmd(int cmdId, HashMap<Integer, Object> result) {
            handler.removeMessages(MESSAGE_ID_TIMEOUT);

            int error = 0;

            if (result.containsKey(TestResultParser.TEST_TOKEN_ERROR_CODE)) {
                error = (Integer) result.get(TestResultParser.TEST_TOKEN_ERROR_CODE);

                testError = error;
            }

            Log.e(TAG, "errorCode: " + error);
            Log.e(TAG, "currentTest: " + currentTest.getDesc());

            filterMessage(cmdId, result);
            if (error == 0) {
                continueTest(cmdId, result);
            }
        }
    };

    public static String getPID(String command) {
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec("ps -ef");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains(command)) {
                    System.out.println("info -----> " + command);
                    String[] strs = line.split("\\s+");
                    return strs[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void closeLinuxProcess(String Pid) {
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec("kill -9 " + Pid);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println("kill PID return info -----> " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

} 
