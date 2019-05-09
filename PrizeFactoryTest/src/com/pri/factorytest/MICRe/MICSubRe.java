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
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.io.IOException;

public class MICSubRe extends PrizeBaseActivity {
    private MediaRecorder mMediaRecorder;
    private AudioManager mAudioManager;
    private int mStatus = 0;
    private Button mBtnTest;
    private File mSoundFile;
    private MediaPlayer mMediaPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.micre);
        getService();
        mBtnTest = (Button) findViewById(R.id.micre_button);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mBtnTest.setText(R.string.sub_mic_recording);
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
                        mBtnTest.setText(R.string.sub_mic_recording);
                        mButtonPass.setEnabled(true);
                        mStatus = 0;
                    }
                    default:
                        return;
                }
            }
        });

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
            e.printStackTrace();
        }
        mMediaRecorder = new MediaRecorder();
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
        } catch (IllegalStateException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            if (mSoundFile != null) {
                mSoundFile.delete();
            }
        }

    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mMediaRecorder = null;
            }
        }
    }

    private void startPlayback() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mSoundFile.getAbsolutePath());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("liup", "startPlayback");
        }
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
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
                        mBtnTest.setText(R.string.sub_mic_recording);
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
            AudioSystem.setParameters("SET_MIC_CHOOSE=2");//close main mic
            Log.e("wuliang", "wuliang SET_MIC_CHOOSE=2");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetReceiver, filter);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(mHeadSetReceiver);

        if (!SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) {
            AudioSystem.setParameters("SET_MIC_CHOOSE=0");//close main mic
            Log.e("wuliang", "wuliang SET_MIC_CHOOSE=0");
        }
    }


    @Override
    public void finish() {
        AudioManager mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mMediaRecorder = null;
            }
        }
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mMediaPlayer = null;
            }
        }
        super.finish();
    }
}
