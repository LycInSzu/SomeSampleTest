package com.pri.factorytest.YCD;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.IOException;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;

public class YCD extends PrizeBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ycd);
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 1 + " > /proc/led_yc_mode"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onDestroy() {
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 0 + " > /proc/led_yc_mode"};
            Runtime.getRuntime().exec(cmdMode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

}
