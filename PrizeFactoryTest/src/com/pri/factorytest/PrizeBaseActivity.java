package com.pri.factorytest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;

import com.pri.factorytest.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

/**
 * Created by prize on 2018/11/22.
 * @author prize zengke
 */

public abstract class PrizeBaseActivity extends Activity {
    private final static String TAG = "PrizeBaseActivity";
    protected int ACTIVITY_TOP_PADDING;
    protected Button mButtonFail;
    protected Button mButtonPass;
    private String TEST_VALUE;
    private int mTestValueNvIndex = -1;
    private final static int PRIZE_FACTORY_FACTORY_INFO_OFFSET = Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET;
    private static List<String> mDownKeyFilter = new ArrayList<String>();
    private FactoryTestApplication mApp;

    static {
        mDownKeyFilter.add("FactoryTestReport");
        mDownKeyFilter.add("FactoryTestReportQr");
        mDownKeyFilter.add("AgingTestActivity");
        mDownKeyFilter.add("ChargerProtectActivity");
        mDownKeyFilter.add("PrizeAtaInfo");
        mDownKeyFilter.add("PrizeFactoryTestActivity");
        mDownKeyFilter.add("PrizeFactoryTestListActivity");
        mDownKeyFilter.add("PrizeHwInfo");
        mDownKeyFilter.add("PrizeLogoInfo");
        mDownKeyFilter.add("PrizeSnInfo");
        mDownKeyFilter.add("Version");
        mDownKeyFilter.add("PrizeHwAudioTest");
        mDownKeyFilter.add("HuidingFingerActivity");
        mDownKeyFilter.add("EnrollActivity");
        mDownKeyFilter.add("DdrSingleActivity");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = Utils.CURRENT_LAN;
        //boolean isEn = "1".equals(SystemProperties.get("ro.pri_factory_default_lang_en"));
        Context context = Utils.createConfigurationResources(newBase, language);
        super.attachBaseContext(context);
    }
    
    /*prize-*#*#888#*#*and*#*#8812#*#*HwInfo Change with system language-yaoshu-20190124-start*/
    protected void superAttachBaseContext(Context context) {
        super.attachBaseContext(context);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (FactoryTestApplication) getApplication();
        ACTIVITY_TOP_PADDING = getResources().getInteger(R.integer.activity_top_padding);
        TEST_VALUE = getIntent().getStringExtra("mTestValue");
    }

    @Override
    protected void onResume() {
        super.onResume();
        int testValueIndex = Optional.ofNullable(mApp.getPrizeFactoryTotalItems()).map(x-> Arrays.asList(x).indexOf(TEST_VALUE)).orElse(-1);
        if (testValueIndex < 0) {
            return ;
        }
        mTestValueNvIndex = Optional.ofNullable(mApp.getPrizeFactoryTotalNvIndexs(testValueIndex)).map(x-> Integer.parseInt(x)).orElse(-1);
    }

    public void confirmButtonNonEnable() {
        confirmButton();
        mButtonPass.setEnabled(false);
    }

    protected String getClassName() {
        return getClass().getName();
    }

    public void doPass2NextTest(){
        if (Utils.toStartAutoTest && mButtonPass.isEnabled()) {
            Log.i(TAG, "----doPass2NextTest--");
            Utils.mItemPosition++;
            writeItemTestResult2Nv(mTestValueNvIndex, "P");
            setResult(RESULT_OK);
            finish();
        }
    }

    public void confirmButton() {
        mButtonFail = findViewById(R.id.failButton);
        mButtonPass = findViewById(R.id.passButton);
        if (mButtonFail == null || mButtonPass == null) {
            return;
        }
        mButtonPass.setOnClickListener(view -> {
            if (Utils.isNoNFastClick()) {
                if (Utils.toStartAutoTest == true) {
                    Utils.mItemPosition++;
                }
                writeItemTestResult2Nv(mTestValueNvIndex, "P");
                setResult(RESULT_OK);
                finish();
            }
        });
        mButtonFail.setOnClickListener(view -> {
            if (Utils.isNoNFastClick()) {
                if (Utils.toStartAutoTest == true) {
                    Utils.mItemPosition++;
                }
                writeItemTestResult2Nv(mTestValueNvIndex, "F");
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void writeItemTestResult2Nv(int index, String result) {
        if (Stream.of("pcba-sea").anyMatch(x -> x.equals(PRIZE_CUSTOMER))) {
            if (index >= 0) {
                Log.i(TAG, "-----PrizeBaseActivity---write the nv index for pcba-sea customer--");
                Utils.writeProInfo(result, PRIZE_FACTORY_FACTORY_INFO_OFFSET + index);
            } else {
                Log.e(TAG, "-----PrizeBaseActivity---not find the nv index in totalItems--");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean contain = mDownKeyFilter.stream().anyMatch(x -> getClassName().contains(x));
        if (keyCode == KeyEvent.KEYCODE_BACK && !contain) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
