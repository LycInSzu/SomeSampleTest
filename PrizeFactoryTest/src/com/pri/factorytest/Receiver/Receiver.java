package com.pri.factorytest.Receiver;

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

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.IOException;

public class Receiver extends PrizeBaseActivity {
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    private AudioManager mAudioManager;
    private TextView mTextView;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver);
        isPlaying = false;
        getService();
        mTextView = (TextView) findViewById(R.id.receiver_hint);
        mTextView.setTextSize(20);
        if (Utils.isWiredHeadsetPluggedIn(this)) {
            mTextView.setText(getString(R.string.remove_headset));
            confirmButtonNonEnable();
        } else {
            mTextView.setText(getString(R.string.receiver_playing));
            confirmButton();
            mButtonPass.setEnabled(true);
        }
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        bindView();
    }

    @Override
    public void finish() {
        stop();
        mAudioManager = (AudioManager) this
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

        if (isPlaying) {
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
                        mTextView.setText(getString(R.string.receiver_playing));
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
        /* prize-lifenfen-0824 start */
        getService();
        /* prize-lifenfen-0824 end */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mHeadSetReceiver, filter);
    }

    /* prize-lifenfen-0824 start */
    public void onPause() {
        unregisterReceiver(mHeadSetReceiver);
        super.onPause();
        finish();
    }
    /* prize-lifenfen-0824 end */

    public void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /**MODE_IN_CALL设置为听筒模式没有效果，android5.0以上google推荐使用AudioManager.MODE_IN_COMMUNICATION*/
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
    }

    void bindView() {
        try {
            play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
