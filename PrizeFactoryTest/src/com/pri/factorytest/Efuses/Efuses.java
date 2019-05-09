package com.pri.factorytest.Efuses;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.PrizeHwInfo;
import com.pri.factorytest.R;

import java.util.Optional;

public class Efuses extends PrizeBaseActivity {
    private TextView resultShow = null;
    private String result = null;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.efuses);
        resultShow = (TextView) findViewById(R.id.result_show);
        confirmButtonNonEnable();
    }

    Runnable HallTestRunnable = () -> {
        String bufferText = PrizeHwInfo.getResultHwinfo();
        //android.util.Log.i("Efuse", "------text:" + bufferText);
        boolean isBlown = Optional.ofNullable(bufferText).map(x -> x.contains("eFuse blown")).orElse(false);
        resultShow.setText(isBlown ? "1" : "0");
        mButtonPass.setEnabled(isBlown);
    };

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(HallTestRunnable);
    }

}
