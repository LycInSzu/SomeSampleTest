package com.pri.factorytest.LED;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LED extends PrizeBaseActivity {

    private static final String RED_LED_DEV = "/sys/class/leds/red/brightness";
    private static final String GREEN_LED_DEV = "/sys/class/leds/green/brightness";
    private static final String BLUE_LED_DEV = "/sys/class/leds/blue/brightness";
    private final byte[] OFF = {'0'};
    private final byte[] BLINK = {'1', '2', '5'};

    private Handler mHandler = new Handler();
    private int mLedState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led);
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 15 + " > /proc/led_mode"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setPrizeLed(1);
        mHandler.postDelayed(mRunnable, 0);
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 1 + " > /proc/aw9106b_test"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            setPrizeLed(mLedState);
            mLedState = (mLedState + 1) % 6;
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 0 + " > /proc/led_mode"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 0 + " > /proc/aw9106b_test"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacks(mRunnable);
        setPrizeLed(6);
        super.onDestroy();
    }

    public void setPrizeLed(int mode) {
        switch (mode) {
            case 0:
                write(GREEN_LED_DEV, BLINK);
                break;
            case 1:
                write(GREEN_LED_DEV, OFF);
                break;
            case 2:
                write(RED_LED_DEV, BLINK);
                break;
            case 3:
                write(RED_LED_DEV, OFF);
                break;
            case 4:
                write(BLUE_LED_DEV, BLINK);
                break;
            case 5:
                write(BLUE_LED_DEV, OFF);
                break;
            case 6:
                write(BLUE_LED_DEV, OFF);
                write(RED_LED_DEV, OFF);
                write(GREEN_LED_DEV, OFF);
                break;
            default:
        }
    }

    private void write(String devPath, byte[] b) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(devPath);
            fileOutputStream.write(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
