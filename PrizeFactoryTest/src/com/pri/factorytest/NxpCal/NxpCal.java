package com.pri.factorytest.NxpCal;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NxpCal extends PrizeBaseActivity {

    String mAudiofilePath;
    static String TAG = "NXP";
    MediaPlayer mMediaPlayer = new MediaPlayer();
    boolean isPlaying = false;
    AudioManager mAudioManager;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private StringBuilder calibrate_value = new StringBuilder();
    private String mCalibrationValue, mCalibrationCoef, mResult;

    public Runnable calibrateRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                do_exec("/vendor/bin/climax_v2 -d /dev/smartpa_i2c --slave=0x34 -l /vendor/etc/firmware/tfa9890.cnt --resetMtpEx");
                //String result0 = do_exec("/vendor/bin/climax_v2 -d /dev/smartpa_i2c --slave=0x34 -l /vendor/etc/firmware/tfa9890.cnt --dump");
                String result1 = do_exec("/vendor/bin/climax_v2 -d /dev/smartpa_i2c --slave=0x34 -l /vendor/etc/firmware/tfa9890.cnt --calibrate=once");
                Pattern p = Pattern.compile("(Calibration.*value.*ohm)");
                Matcher m = p.matcher(result1);
                if (m.find()) {
                    mCalibrationValue = m.group(1);
                }
                String result2 = do_exec("/vendor/bin/climax_v2 -d /dev/smartpa_i2c --slave=0x34 -l /vendor/etc/firmware/tfa9890.cnt --calshow");
                p = Pattern.compile("(Current calibration tCoef.*\\d)");
                m = p.matcher(result2);
                if (m.find()) {
                    mCalibrationCoef = m.group(1);
                }
                float calibrationValue = 0;
                p = Pattern.compile("(\\d.*\\d)");
                m = p.matcher(mCalibrationValue);
                if (m.find()) {
                    calibrationValue = Float.parseFloat(m.group(1));
                }
                Log.e("tangan", "calibrationValue = " + calibrationValue);
                if (calibrationValue > 6.4 && calibrationValue < 9.6) {
                    mResult = getString(R.string.cal_success);
                    findViewById(R.id.passButton).setEnabled(true);
                } else {
                    mResult = getString(R.string.cal_fail);
                }
            } catch (Exception e) {
                mResult = getString(R.string.cal_fail);
            }
            calibrate_value.append(mCalibrationValue).append("\n").append(mCalibrationCoef).append("\n").append(mResult);
            mTextView.setText(calibrate_value);
        }
    };

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
        setContentView(R.layout.nxp_cal);

        isPlaying = false;
        getService();
        mTextView = (TextView) findViewById(R.id.nxp_cal);
        mTextView.setText(getString(R.string.nxp_calibrating));
        mTextView.setTextSize(20);
        confirmButtonNonEnable();
        bindView();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mHandler.postDelayed(calibrateRunnable, 500);
    }

    String do_exec(String cmd) {
        Log.e("tangan", "do_exec cmd=" + cmd);
        String s = "";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "/n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();
            s = null;
        }
        Log.e("tangan", "do_exec result=" + s);
        return s;
    }


    @Override
    public void finish() {

        stop();
        AudioManager mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        super.finish();
    }

    void play() throws IllegalArgumentException, IllegalStateException,
            IOException {

        isPlaying = true;

        try {
            mMediaPlayer.reset();
            mMediaPlayer = MediaPlayer.create(this, R.raw.mute);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (Exception e) {
            loge(e);
        }

    }

    void stop() {

        if (isPlaying == true) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            isPlaying = false;
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void setAudio() {

        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        float ratio = 1f;

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_ALARM)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
        mAudioManager
                .setStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        (int) (ratio * mAudioManager
                                .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)),
                        0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_DTMF)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)),
                0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                (int) (ratio * mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);
    }

    void bindView() {

        try {
            play();
        } catch (Exception e) {
            loge(e);
        }
    }

    void getService() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

}
