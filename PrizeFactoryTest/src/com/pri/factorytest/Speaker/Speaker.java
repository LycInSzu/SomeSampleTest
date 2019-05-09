package com.pri.factorytest.Speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.IOException;

public class Speaker extends PrizeBaseActivity {

    String mAudiofilePath;
    static String TAG = "Speaker";
    MediaPlayer mMediaPlayer = new MediaPlayer();
    boolean isPlaying = false;
    AudioManager mAudioManager;
    private TextView mTextView;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaker);
        isPlaying = false;
        getService();
        mTextView = (TextView) findViewById(R.id.speaker_hint);
        mTextView.setTextSize(20);
        if (Utils.isWiredHeadsetPluggedIn(this)) {
            mTextView.setText(getString(R.string.remove_headset));
            confirmButtonNonEnable();
        } else {
            mTextView.setText(getString(R.string.speaker_playing));
            confirmButton();
            mButtonPass.setEnabled(true);
        }
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        bindView();
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
            mMediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (Exception e) {
        }

    }

    void stop() {
        if (isPlaying == true) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            isPlaying = false;
        }
    }

    private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        mTextView.setText(getString(R.string.speaker_playing));
                        confirmButton();
                        mButtonPass.setEnabled(true);
                        break;
                    case 1:
                        mTextView.setText(getString(R.string.remove_headset));
                        confirmButtonNonEnable();
                        break;
                    default:
                        break;
                }

            }
        }

    };

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mHeadSetReceiver);
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
        }
    }

    void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }
}
