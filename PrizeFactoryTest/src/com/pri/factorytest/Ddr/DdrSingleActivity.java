package com.pri.factorytest.Ddr;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.pri.factorytest.FactoryTestApplication;
import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// prize-add-houjian-20180720-begin

// prize-add-houjian-20180720-end
public class DdrSingleActivity extends PrizeBaseActivity {
    private long ddr_circles;

    private static final String TAG = "DDRTest";
    private static final int REFRESH_VIEW = 1;
    private static final int FINISH_ACTIVITY = 2;
    private static final int STRAT_DDR_TEST = 3;
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
    private TextView test_result = null;
    private TextView test_title = null;

    static String test_setText = "";
    static int Testing_times = 0;
    String complexMemtester_1 = "memtester 3G 1";
    String mDdrTestResult = "";

    PowerManager mPowerManager = null;
    PowerManager.WakeLock mWakeLock = null;
    KeyguardManager mKeyguardManager = null;
    KeyguardManager.KeyguardLock mKeyguardLock = null;

    private FactoryTestApplication app;

    private MyHandler mHandler = new MyHandler();

    /**
     * prize-DDR Test write nvram-by-zhongweilin-20171201-end
     */
    // prize-add-houjian-20180720-begin
    private StatusBarManager mStatusBarManager;

    // prize-add-houjian-20180720-end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddr_single);

        Intent intent = getIntent();
        app = (FactoryTestApplication) getApplication();
        ddr_circles = intent.getIntExtra("extra_message", 0);
        if (ddr_circles == 0) {
            ddr_circles = 1;
            app.getSharePref().putValue("ddr_test", "0");
        } else {
            ddr_circles = 1;
            app.getSharePref().putValue("ddr_test_circles", String.valueOf(ddr_circles));
            app.getSharePref().putValue("ddr_test", "1");
            startReboot();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        test_title = (TextView) findViewById(R.id.title);
        test_result = (TextView) findViewById(R.id.tvShow);
        test_result.setText(String.valueOf(ddr_circles));
        mDdrTestResult = "Start Date : " + getSystemDate() + "\n";
        // prize-add-houjian-20180720-begin
        mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
        mStatusBarManager.disable(StatusBarManager.DISABLE_NAVIGATION | StatusBarManager.DISABLE_BACK);
        // prize-add-houjian-20180720-end
        mHandler.sendEmptyMessageDelayed(STRAT_DDR_TEST, 2000);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }

    }

    private String getSystemDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    public Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            String comand = "memtester " + formatFileSize(getAvailableMemory(DdrSingleActivity.this)) + " 1";
            Memtester(comand);
        }
    };

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_VIEW:
                    test_result.setText(mDdrTestResult);
                    break;
                case FINISH_ACTIVITY:
                    if (mDdrTestResult.contains("fail")) {
                        test_title.setText("Fail");
                        test_title.setTextColor(Color.parseColor("#FF0000"));
                        test_title.setTextSize(100);
                        Utils.writeProInfo("F", 36);
                    } else {
                        test_title.setText("PASS");
                        test_title.setTextColor(Color.parseColor("#00FF00"));
                        test_title.setTextSize(100);
                        Utils.writeProInfo("P", 36);
                    }
                    // prize-add-houjian-20180720-begin
                    mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
                    // prize-add-houjian-20180720-end
                    break;
                case STRAT_DDR_TEST:
                    new Thread(mRunnable).start();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static String formatFileSize(long size) {
        DecimalFormat df = fileDecimalFormat;
        // prize-delete-houjian-20180720-begin
        //size = size - 200 * 1024 * 1024;
        // prize-delete-houjian-20180720-end
        // prize-add-houjian-20180720-begin
        size = size - 400 * 1024 * 1024;
        // prize-add-houjian-20180720-end
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format(size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format(size / 1024) + "K";
        } else {
            fileSizeString = df.format(size / (1024 * 1024)) + "M";
        }
        return fileSizeString;
    }

    public void Memtester(String command) {
        Runtime r = Runtime.getRuntime();
        Process mProcess;
        BufferedReader mBufferedReader;
        Testing_times++;
        try {
            mProcess = r.exec(command);
            mBufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            String inline;
            while ((inline = mBufferedReader.readLine()) != null) {
                if (inline.length() > 1000) {
                    inline = inline.substring(0, 1000);
                }
                mDdrTestResult += inline + "\n";
                mHandler.sendEmptyMessage(REFRESH_VIEW);
                System.out.println(inline);
            }
            mBufferedReader.close();
            mProcess.waitFor();
            mDdrTestResult += "Finish Date : " + getSystemDate() + "\n";
            mHandler.sendEmptyMessage(REFRESH_VIEW);
            mHandler.sendEmptyMessage(FINISH_ACTIVITY);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String ReadTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = "";
        File file = new File(path);
        if (file.isDirectory()) {
            Log.e("oldtest", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                        // if(inline.equals("SUCCESS")){
                        if (line.indexOf("SUCCESS") != -1) {
                            test_setText += Testing_times + " : " + "SUCCESS \n";
                            test_result.setText(test_setText);
                        } else {
                            test_setText += Testing_times + " : " + "FAIL \n";
                            test_result.setText(test_setText);
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.e("oldtest", "Exception The File doesn't not exist.");
            } catch (IOException e) {
                Log.e("oldtest", e.getMessage());
            }
        }
        return content;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void startReboot() {
        Intent rebootIntent = new Intent(Intent.ACTION_REBOOT);
        rebootIntent.putExtra("nowait", 1);
        rebootIntent.putExtra("interval", 1);
        rebootIntent.putExtra("window", 0);
        sendBroadcast(rebootIntent);
        Log.e("oldtest", "DDR Test reboot...");
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

}
