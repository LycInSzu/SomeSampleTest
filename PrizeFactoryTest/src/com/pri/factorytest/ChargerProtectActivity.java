package com.pri.factorytest;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pri.factorytest.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChargerProtectActivity extends PrizeBaseActivity {

    private static final String TAG = "ChargerProtectActivity";
    private static final String LOW_CHAGRE_PROTECT_PATH = "/proc/mtk_battery_cmd/lt_charger_protect";
    private static final String HIGH_CHAGRE_PROTECT_PATH = "/proc/mtk_battery_cmd/ht_charger_protect";
    private Button mHighChargerProtect;
    private Button mLowChargerProtect;
    private String mHighChargerProtectTxt;
    private String mLowChargerProtectTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charger_protect);
        mHighChargerProtect = findViewById(R.id.high_charger_protect);
        mLowChargerProtect = findViewById(R.id.low_charger_protect);
        mHighChargerProtectTxt = getResources().getString(R.string.high_charger_protect_txt);
        mLowChargerProtectTxt = getResources().getString(R.string.low_charger_protect_txt);
        Utils.paddingLayout(findViewById(R.id.high_charger_protect), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mHighChargerProtect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sufaceHighView();
            }
        });

        mLowChargerProtect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sufaceLowCView();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    public void initView() {
        setChargerProtectState(HIGH_CHAGRE_PROTECT_PATH);
        String curHCP = getChargerProtectState(HIGH_CHAGRE_PROTECT_PATH);
        Log.e(TAG, "---------chargerProtect(HCP)= " + curHCP);
        int curHcp = Integer.parseInt(curHCP);
        if (/*curHCP.contains("50")*/curHcp < 60) {
            mHighChargerProtect.setText(mHighChargerProtectTxt + ": ON");
            mHighChargerProtect.setBackgroundColor(Color.GREEN);
        } else {
            mHighChargerProtect.setText(mHighChargerProtectTxt + ": OFF");
            mHighChargerProtect.setBackgroundColor(Color.RED);
        }

        setChargerProtectState(LOW_CHAGRE_PROTECT_PATH);
        String curLCP = getChargerProtectState(LOW_CHAGRE_PROTECT_PATH);
        int curLcp = Integer.parseInt(curLCP);
        Log.e(TAG, "------chargerProtect(LCP)= " + curLCP);
        if (/*curLCP.contains("1")*/curLcp > -10) {
            mLowChargerProtect.setText(mLowChargerProtectTxt + ": ON");
            mLowChargerProtect.setBackgroundColor(Color.GREEN);
        } else {
            mLowChargerProtect.setText(mLowChargerProtectTxt + ": OFF");
            mLowChargerProtect.setBackgroundColor(Color.RED);
        }
    }

    public void sufaceLowCView() {
        String curLCP = getChargerProtectState(LOW_CHAGRE_PROTECT_PATH);
        int curLcp = Integer.parseInt(curLCP);
        Log.e(TAG, "---------chargerProtect(LCP)= " + curLCP);
        if (/*curLCP.contains("1")*/curLcp > -10) {
            setChargerProtect(0, LOW_CHAGRE_PROTECT_PATH);
            mLowChargerProtect.setText(mLowChargerProtectTxt + ": OFF");
            mLowChargerProtect.setBackgroundColor(Color.RED);
        } else {
            setChargerProtect(1, LOW_CHAGRE_PROTECT_PATH);
            mLowChargerProtect.setText(mLowChargerProtectTxt + ": ON");
            mLowChargerProtect.setBackgroundColor(Color.GREEN);
        }
    }

    public void sufaceHighView() {
        String curHCP = getChargerProtectState(HIGH_CHAGRE_PROTECT_PATH);
        int curHcp = Integer.parseInt(curHCP);
        Log.e(TAG, "---------chargerProtect(HCP)= " + curHCP);
        if (/*curHCP.contains("50")*/curHcp < 60) {
            setChargerProtect(0, HIGH_CHAGRE_PROTECT_PATH);
            mHighChargerProtect.setText(mHighChargerProtectTxt + ": OFF");
            mHighChargerProtect.setBackgroundColor(Color.RED);
        } else {
            setChargerProtect(1, HIGH_CHAGRE_PROTECT_PATH);
            mHighChargerProtect.setText(mHighChargerProtectTxt + ": ON");
            mHighChargerProtect.setBackgroundColor(Color.GREEN);
        }
    }

    public static void setChargerProtect(int value, String path) {
        setChargerProtectState(value, path);
        /*try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + value + " > " + path};
            for (String str :cmdMode) {
                Log.e(TAG, "---------chargerProtect(cmdMode)= " + str + "\n");
            }
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private static String getChargerProtectState(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            charCount = mFileReader.read(data);
            status = new String(data, 0, charCount).trim().split(" ");
            return status[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setChargerProtectState(String path) {
        try {
            String cmdMode = "/system/bin/sh" + " chmod 646 " + path;
            Log.e(TAG, "---------chargerProtect(cmdMode)= " + cmdMode + "\n");
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setChargerProtectState(int value, String path) {
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(path);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(String.valueOf(value).getBytes(), 0, String.valueOf(value).getBytes().length);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
