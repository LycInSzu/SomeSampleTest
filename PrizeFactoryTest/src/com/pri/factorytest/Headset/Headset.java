package com.pri.factorytest.Headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.FileOutputStream;

public class Headset extends PrizeBaseActivity {
    private AudioRecord m_record;
    private AudioTrack m_track;
    private int bufferSize;
    private byte[] buffer;
    private AudioManager mAudioManager;
    private Thread mThread;
    private boolean flag = false;
    private TextView mTextView;
    private static final String TAG = "PrizeFactoryTestHeadset";
    private boolean keyeventflag = false;
    private TextView mKeyView;
    private volatile boolean hookKeyPass = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "keycode:" + keyCode + " keyeventflag:" + keyeventflag);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            /*if (!keyeventflag) {
                micSwitch();
            }*/
            updateKeyView(true);
            hookKeyPass = true;
            updateButton();
            return true;
        }
        return false;
    }

    private void updateKeyView(boolean testPass) {
        if (testPass) {
            mKeyView.setBackgroundResource(R.color.green);
        } else {
            mKeyView.setBackgroundResource(R.color.black);
        }
    }

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headset);
        mTextView = (TextView) findViewById(R.id.headset_hint);
        mTextView.setTextSize(20);
        mKeyView = findViewById(R.id.headset_hook);
        confirmButtonNonEnable();
        updateKeyView(false);
        getService();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

    }

    int writeFile(String value) {
        if (!Utils.isWiredHeadsetPluggedIn(this))
            return -1;
        try {
            FileOutputStream fos = new FileOutputStream("/sys/devices/mediatek_typec/mic_switch");
            fos.write(value.getBytes());
            fos.close();
            Log.e(TAG, "value :" + value);
            return Integer.parseInt(value);
        } catch (Exception e) {
            Log.e(TAG, "mic_switch write read error: " + e);
            return -1;
        }
    }

    void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /* lifenfen-0412 start */
        if (Utils.isWiredHeadsetPluggedIn(this))
            mAudioManager.setParameter("SET_LOOPBACK_TYPE", "22,2");
		/*
		mAudioManager.setSpeakerphoneOn(false);
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		*/
		/* lifenfen-0412 end */
    }

    private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        flag = false;
                        keyeventflag = false;
                        mTextView.setText(getString(R.string.headset_insert));
					/* Begin, prize-lifenfen-0612, Mobile test for HIFI powerup*/
                        mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");
					/* End, prize-lifenfen-0612, Mobile test for HIFI powerup*/
                        break;
                    case 1:
                        flag = true;
                        mTextView.setText(getString(R.string.headset_tip));
                                        /* Begin, prize-lifenfen-0612, Mobile test for HIFI powerup*/
                        mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");
                        mAudioManager.setParameter("SET_LOOPBACK_TYPE", "22,2");
                                        /* End, prize-lifenfen-0612, Mobile test for HIFI powerup*/
                        int streamType = mAudioManager.getUiSoundsStreamType();
                        mAudioManager.setStreamVolume(streamType, (int)(mAudioManager.getStreamMaxVolume(streamType) * 0.65), AudioManager.FLAG_PLAY_SOUND);
                        break;
                    default:
                        break;
                }
                updateButton();
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetReceiver, filter);
        if (!Utils.isWiredHeadsetPluggedIn(this)) {
            mTextView.setText(getString(R.string.headset_insert));
        } else {
            mTextView.setText(getString(R.string.headset_tip));
        }
        updateButton();
    }

    private void updateButton() {
        mButtonPass.setEnabled(flag && hookKeyPass);
    }

    void micSwitch() {
        Thread mKeyEventThread = new Thread(new Runnable() {
            public void run() {
                keyeventflag = true;
                mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");

                writeFile("0");
                try {
                    final int hundredMillisecond = 500;
                    Thread.sleep(hundredMillisecond);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                writeFile("1");
                try {
                    final int hundredMillisecond = 100;
                    Thread.sleep(hundredMillisecond);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (Utils.isWiredHeadsetPluggedIn(Headset.this))
                    mAudioManager.setParameter("SET_LOOPBACK_TYPE", "22,2");
                int streamType = mAudioManager.getUiSoundsStreamType();
                mAudioManager.setStreamVolume(streamType, (int)(mAudioManager.getStreamMaxVolume(streamType) * 0.65), AudioManager.FLAG_PLAY_SOUND);
                keyeventflag = false;
            }
        });
        mKeyEventThread.start();
    }

    void startServer() {
        int SAMPLE_RATE = 8000;
        int BUF_SIZE = 1024;

        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        bufferSize = Math.max(bufferSize, AudioTrack.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT));
        bufferSize = Math.max(bufferSize, BUF_SIZE);

        buffer = new byte[bufferSize];

        m_record = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        m_track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STREAM);

        m_track.setPlaybackRate(SAMPLE_RATE);
        m_record.startRecording();
        m_track.play();

        while (flag) {
            int readSize = m_record.read(buffer, 0, bufferSize);
            if (readSize > 0) {
                m_track.write(buffer, 0, readSize);
            }
        }
        try {
            if (m_record != null) {
                m_record.stop();
                m_record.release();
                m_record = null;
            }
            if (m_track != null) {
                m_track.stop();
                m_track.release();
                m_track = null;
            }
        } catch (IllegalStateException e) {
            Log.e("liup", "m_record,m_track error");
        }
    }

    @Override
    public void finish() {
        flag = false;
        keyeventflag = false;
        AudioManager mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
		/* lifenfen-0412 start */
		/*
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		*/
        mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");
		/* lifenfen-0412 end */
        super.finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    protected void onPause() {
        this.unregisterReceiver(mHeadSetReceiver);
        super.onPause();
    }
}
