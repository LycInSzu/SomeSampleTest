package com.pri.factorytest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import com.pri.factorytest.util.Utils;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by prize on 2019/1/2.
 *
 * @author prize zengke
 */

public class PrizeHwAudioTest extends PrizeBaseActivity {

    private final static String TAG = "PrizeHwAudioTest";
    private Button mReceBtn;
    private Button mSpeakBtn;
    private Button mStopBtn;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private int mAudioMode;
    private boolean mIsPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();
        mAudioMode = mAudioManager.getMode();
        setContentView(R.layout.prize_hw_audio);
        Utils.paddingLayout(findViewById(R.id.receiver_play), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceBtn = findViewById(R.id.receiver_play);
        mSpeakBtn = findViewById(R.id.speaker_play);
        mStopBtn = findViewById(R.id.stop_play);
        mReceBtn.setOnClickListener(view -> {
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            if (mAudioManager.isSpeakerphoneOn()) {
                mAudioManager.setSpeakerphoneOn(false);
            }
            //prize add by wangwei, factory,The receiver is powerful  ,20190130 begin
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
            //prize add by wangwei, factory,The receiver is powerful  ,20190130 end
            play();
        });
        mSpeakBtn.setOnClickListener(view -> {
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            if (!mAudioManager.isSpeakerphoneOn()) {
                mAudioManager.setSpeakerphoneOn(true);
            }
            //prize add by wangwei, factory,The receiver is powerful  ,20190130 begin
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            //prize add by wangwei, factory,The receiver is powerful  ,20190130 end
            play();
        });
        mStopBtn.setOnClickListener(view -> stop());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    private void play() {
        try {
            mIsPlaying = true;
            mMediaPlayer.reset();
            mMediaPlayer = MediaPlayer.create(this, R.raw.prize_hwaudio_test);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }

    }

    private void stop() {
        if (mIsPlaying) {
            mMediaPlayer.stop();
            mIsPlaying = false;
        }
    }

    //prize add by wangwei, factory,The receiver is powerful  ,20190130 begin
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mAudioManager.setMode(mAudioMode);
    }
    //prize add by wangwei, factory,The receiver is powerful  ,20190130 end

    @Override
    public void finish() {
        super.finish();
        stop();
        Optional.ofNullable(mAudioManager).ifPresent(x -> x.setMode(mAudioMode));
        Optional.ofNullable(mMediaPlayer).ifPresent(x -> {
            x.release();
            x = null;
        });
    }
}
