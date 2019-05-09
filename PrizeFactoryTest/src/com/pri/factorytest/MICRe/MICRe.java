package com.pri.factorytest.MICRe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MICRe extends PrizeBaseActivity {
    private MediaRecorder mMediaRecorder;
    private AudioManager mAudioManager;
    private int mStatus = 0;
    private Button mBtnTest;
    private File mSoundFile;
    private MediaPlayer mMediaPlayer;
    private TextView mDBView;
    private int BASE = 1;
    private int SPACE = 300;
    private int MIN_DB = 70;
    private ArrayList<Double> mDBList = new ArrayList<Double>();
    private double mSunDB;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.micre);
        getService();
        mBtnTest = (Button) findViewById(R.id.micre_button);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mBtnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mStatus) {
                    case 0: {
                        startRecording();
                        mBtnTest.setText(R.string.mic_stop_recording);
                        mStatus = 1;
                        break;
                    }
                    case 1: {
                        stopRecording();
                        startPlayback();
                        mBtnTest.setText(R.string.mic_stop_playing);
                        mStatus = 2;
                        break;
                    }
                    case 2: {
                        stopPlayback();
                        mBtnTest.setText(R.string.mic_recording);
                        //if(mDBList.size() > 0 && mSunDB / mDBList.size() > MIN_DB){
                        mButtonPass.setEnabled(true);
                        //}
                        mStatus = 0;
                    }
                    default:
                        return;
                }
            }
        });

        mDBView = (TextView) findViewById(R.id.db_text);

        confirmButtonNonEnable();
    }

    private void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
    }

    void startRecording() {
        mSoundFile = new File(getCacheDir(), "MicRecorder.amr");
        try {
            if (mSoundFile.exists()) {
                mSoundFile.delete();
            }
            mSoundFile.createNewFile();
        } catch (IOException e) {
            return;
        }
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mSoundFile.getAbsolutePath());
        mMediaRecorder.setAudioEncodingBitRate(128000);
        mMediaRecorder.setAudioSamplingRate(48000);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mSoundFile != null) {
                mSoundFile.delete();
            }
        }
        try {
            mMediaRecorder.start();
            mDBList.clear();
            mSunDB = 0;
            mHandler.postDelayed(mUpdateMicStatusTimer, 500);
        } catch (IllegalStateException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mSoundFile != null) {
                mSoundFile.delete();
            }
        }

    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    private void updateMicStatus() {
        if (mMediaRecorder != null) {
            int maxAmplitude = mMediaRecorder.getMaxAmplitude();
            double ratio = (double) maxAmplitude / BASE;
            Log.d("MICRe", "getMaxAmplitude()" + maxAmplitude);
            double db = 0;
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                Log.d("MICRe", "db = " + db);
                mDBList.add(db);
                mSunDB += db;
                mDBView.setText(String.format("%.2f,%d,%.2f", db, mDBList.size(), mSunDB / mDBList.size()));
                if (mSunDB / mDBList.size() > MIN_DB && mDBList.size() > 10) {
                    //mButtonPass.setEnabled(true);
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }


    private void stopRecording() {
        mHandler.removeCallbacks(mUpdateMicStatusTimer);
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e){
                Log.e("MICRe", "stopRecording" + e.toString());
            } finally {
                mMediaRecorder = null;
            }
        }
    }

    private void startPlayback() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mSoundFile.getAbsolutePath());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("MICRe", "startPlayback");
        }
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e) {
                Log.e("MICRe", e.toString());
            } finally {
                mMediaPlayer = null;
            }
        }
    }

    private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        mBtnTest.setText(R.string.mic_recording);
                        mBtnTest.setEnabled(true);
                        break;
                    case 1:
                        mBtnTest.setText(R.string.remove_headset);
                        mBtnTest.setEnabled(false);
                        mButtonPass.setEnabled(false);
                        break;
                    default:
                        break;
                }

            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        if (!SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) {
            AudioSystem.setParameters("SET_MIC_CHOOSE=1");//close main mic
            Log.e("wuliang", "wuliang SET_MIC_CHOOSE=1");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetReceiver, filter);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(mHeadSetReceiver);

        if (!SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) {
            AudioSystem.setParameters("SET_MIC_CHOOSE=0");
            Log.e("wuliang", "wuliang SET_MIC_CHOOSE=0");
        }
    }


    @Override
    public void finish() {
        AudioManager mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        stopRecording();
        stopPlayback();
        super.finish();
    }
}
