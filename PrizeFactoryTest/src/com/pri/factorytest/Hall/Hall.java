package com.pri.factorytest.Hall;

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

public class Hall extends PrizeBaseActivity {
    private static final int MSG_CLOSE_LID = 0x1234;
    private static final int MSG_OPEN_LID = 0x1235;
    protected static final String TAG = "HallActivity";
    private TextView resultShow = null;
    protected String result = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLOSE_LID:
                    resultShow.setText("0");
                    break;
                case MSG_OPEN_LID:
                    resultShow.setText("1");
                    break;
            }
            mHandler.postDelayed(HallTestRunnable, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hall);
        resultShow = (TextView) findViewById(R.id.result_show);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

    }

    Runnable HallTestRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                CMDExecute cmdexe = new CMDExecute();
                String[] args = {"/system/bin/cat",
                        "/sys/hall_state/hall_status"};
                result = cmdexe.run(args, "system/bin/");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result.trim().equals("0")) {
                mButtonPass.setEnabled(true);
                Message message = new Message();
                message.what = MSG_CLOSE_LID;
                mHandler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = MSG_OPEN_LID;
                mHandler.sendMessage(message);
            }
        }

    };

    public void onResume() {
        super.onResume();
        mHandler.post(HallTestRunnable);
    }

    public void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_CLOSE_LID);
        mHandler.removeMessages(MSG_OPEN_LID);
        mHandler.removeCallbacks(HallTestRunnable);
    }
}
