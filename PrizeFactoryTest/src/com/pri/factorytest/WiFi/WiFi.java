package com.pri.factorytest.WiFi;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.FactoryTestApplication;
import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.Service.WifiScanService;
import com.pri.factorytest.util.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WiFi extends PrizeBaseActivity {
    private List<ScanResult> wifiScanResult;
    private TextView mTextView;
    private static String TAG = "PrizeFactoryTest/WiFi";
    FactoryTestApplication mApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi);
        bindView();
        mApp = (FactoryTestApplication) getApplication();
        wifiScanResult = mApp.getWifiScanResult();
        if (!mApp.getIsWifiScanning()) {
            Log.e(TAG, "scan finish, restart wifiScanService...");
            if (wifiScanResult.size() > 0) {
                mHandler.sendEmptyMessage(0);
            }
            startService(new Intent(WiFi.this, WifiScanService.class));
        } else {
            Log.e(TAG, "scannig");
        }
        startTimer();
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }


    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!mApp.getIsWifiScanning()) {
                        stopTimer();
                        wifiScanResult = mApp.getWifiScanResult();
                        mHandler.sendEmptyMessage(0);
                    }
                    if (mApp.getWifiScanResult().size() > 0) {
                        wifiScanResult = mApp.getWifiScanResult();
                        mHandler.sendEmptyMessage(0);
                    }
                }
            };
        }
        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, 500, 1000);
    }

    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    void bindView() {
        mTextView = (TextView) findViewById(R.id.wifi_hint);
        mTextView.setText(getString(R.string.wifi_text));
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            StringBuilder s = new StringBuilder();
            StringBuilder wifiInfos = new StringBuilder();
            s.append(getString(R.string.wifi_text) + "\n\n" + "AP List:\n");
            wifiInfos.append("");
            if (wifiScanResult != null && wifiScanResult.size() > 0) {
                mButtonPass.setEnabled(true);
                for (int i = 0; i < wifiScanResult.size(); i++) {
                    if (wifiScanResult.get(i).SSID.length() < 1) {
                        continue;
                    }
                    s.append(" " + i + ": " + wifiScanResult.get(i).SSID + "   " + wifiScanResult.get(i).level + "dBm" + "\n\n");
                    wifiInfos.append(" " + i + ": " + wifiScanResult.get(i).toString() + "\n\n");
                    mTextView.setText(s);
                }
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.wifi_scan_null), Toast.LENGTH_SHORT).show();
                setResultAndFinishActivity(RESULT_CANCELED);
            }
        }

        ;
    };

    void setResultAndFinishActivity(int resultCode) {
        if (Utils.toStartAutoTest == true) {
            Utils.mItemPosition++;
        }
        setResult(resultCode);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopTimer();
        super.onDestroy();
    }
}
