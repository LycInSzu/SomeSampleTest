package com.pri.factorytest.CameraBackSub;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.CMDExecute;
import com.pri.factorytest.util.Utils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CameraBackAlsSub extends PrizeBaseActivity {
    private TextView alsps;
    Handler mhandle;
    Timer mTimer;

    private String catAlsPs() {
        String result = null;
        try {
            CMDExecute cmdexe = new CMDExecute();
            String[] args = {"/system/bin/cat", "/sys/bus/platform/drivers/als_ps1/als"};
            result = cmdexe.run(args, "system/bin/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_back_sub_als);

        alsps = (TextView) findViewById(R.id.als_ps);
        Utils.paddingLayout(findViewById(R.id.als_ps), 0, ACTIVITY_TOP_PADDING, 0, 0);
        //alsps.setText(getString(R.string.camera_back_sub_tip));
        mhandle = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    alsps.setText(getString(R.string.camera_back_sub_tip) + " : " + catAlsPs());

                    if (!catAlsPs().contains("No") && !catAlsPs().contains("0x0000")) {
                        mButtonPass.setEnabled(true);
                    }
                }
            }
        };

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
    }

    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void finish() {
        super.finish();
    }
}
