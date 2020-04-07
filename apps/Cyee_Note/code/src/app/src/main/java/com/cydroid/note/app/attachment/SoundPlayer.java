package com.cydroid.note.app.attachment;

import android.app.Dialog;
import android.app.filecrypt.zyt.filesdk.FileCryptSDK;
import android.app.filecrypt.zyt.services.CryptWork;
import android.content.Context;
//GIONEE :wanghaiyan 2015-11-5 modfiy for 40950 begin 
import android.media.AudioManager;
//GIONEE :wanghaiyan 2015-11-5 modfiy for 40950  end 
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import com.cydroid.note.common.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
import android.widget.Toast;
//GIONEE :wanghaiyan 2015-11-5 modfiy for end 40950
import com.cydroid.note.R;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.ZYTGroupListener;
import com.cydroid.note.encrypt.ZYTProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

//Gionee wanghaiyan 20170307 add for 77568 begin
import com.cydroid.note.common.Log;
import android.widget.Toast;
import com.Legal.Java.*;
import java.io.File;
import com.cydroid.note.common.NoteUtils;
//Gionee wanghaiyan 20170307 add for 77568 end

public class SoundPlayer {

    private static final String TAG = "SoundPlayer";
    private static final int MESSAGE_UPDATE_SOUND_TIME = 1;

    private Context mContext;
    private Dialog mDialog;
    private TextView mTime;
    private ImageView mButton;
    private MediaPlayer mSoundPlayer;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mDurationInMs;
    private Handler mMainHandler;
    private boolean mIsEncrypt;
    private String mSoundFilePath;
    private SoundStopoListener mSoundStopoListener;
    private PowerManager.WakeLock mWakeLock;
	//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
	//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
    public SoundPlayer(Context context, boolean isEncrypt, SoundStopoListener soundStopoListener) {
        mContext = context;
        mIsEncrypt = isEncrypt;
        mSoundStopoListener = soundStopoListener;
        initDialog();
        initHandler();
    }

    public void launchPlayer(final String soundPath, final int durationInSec) {
        if (!hasRequestedAudioFocus()) {
            Toast.makeText(mContext, R.string.attachment_sound_paly_focus_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                showDialog();
                startPlayer(soundPath, durationInSec);
            }
        });
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
    private boolean hasRequestedAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        pausePlayer();
                    }
                }
            };
        }
        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950

    private void initDialog() {
        if (mDialog == null) {
            View content = LayoutInflater.from(mContext).inflate(R.layout.sound_player_layout, null, false);
            mTime = (TextView) content.findViewById(R.id.sound_player_time);
            mButton = (ImageView) content.findViewById(R.id.sound_player_button);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSoundPlayer.isPlaying()) {
                        pausePlayer();
                    } else {
					    //GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
                        restartplayer();
						//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
                    }
                }
            });
            Dialog dialog = new Dialog(mContext, R.style.DialogTheme) {
                @Override
                public void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    completePlayer();
                }
            };
            dialog.setContentView(content);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setGravity(Gravity.BOTTOM);
            mDialog = dialog;
        }
    }

    private void initHandler() {
        mMainHandler = new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_UPDATE_SOUND_TIME: {
                        if (mSoundPlayer != null) {
                            int time = (mDurationInMs - mSoundPlayer.getCurrentPosition()) / 1000;
                            time = NoteUtils.clamp(time, 0, time);
                            String timeText = NoteUtils.formatTime(time, " : ");
                            mTime.setText(timeText);
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
    }

    private void startPlayer(String soundPath, final int durationInSec) {
        if (!mIsEncrypt) {
            acquireWakeLock();
        }
	    //Gionee wanghaiyan 20170307 add for 77568 begin
	    if(NoteUtils.gnKRFlag){
	    	Log.d("kptc", "com.cydroid.note.app.attachment.SoundPlayer->startPlayer: isLegalFile()");
	    	int nFlag = pwinSign.isLegalFile(soundPath);
	    	Log.d("kptc", "retCode=" + nFlag);
    		if( nFlag != 1 && nFlag != 2){   		
    			pwinSign.showIllegalFileMessage(mContext);
			    File mDFile = new File(soundPath);
			    mDFile.delete();
			    dismissDialog();   		
		    }
	     }	
    	//Gionee wanghaiyan 20170307 add for 77568 end
        mSoundFilePath = soundPath; //NOSONAR
        mSoundPlayer = new MediaPlayer();
        try {
            mSoundPlayer.setDataSource(soundPath);
            mSoundPlayer.prepareAsync();
        } catch (IOException e) {
		    //GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
            releaseRecorder();
            Log.i(TAG, "IOException e = " + e.getMessage());
			//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
        }
        mSoundPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                completePlayer();
                return true;
            }
        });
        mSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                completePlayer();
            }
        });
        mSoundPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mDurationInMs = mp.getDuration();
                int second = mDurationInMs / 1000;
                if (durationInSec != second) {
                    mDurationInMs = durationInSec * 1000;
                }
                mp.start();
                startTimer();
            }
        });
    }
    //GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
    private void restartplayer() {
        if (!hasRequestedAudioFocus()) {
            completePlayer();
            Toast.makeText(mContext, R.string.attachment_sound_paly_focus_fail, Toast.LENGTH_SHORT).show();
        }
        mButton.setImageResource(R.drawable.media_pause);
        mSoundPlayer.start();
        startTimer();
    }
    //GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950

    private void pausePlayer() {
        mButton.setImageResource(R.drawable.media_play);
        mSoundPlayer.pause();
        cancelTimer();
    }

    private void stopPlayer() {
        releaseWakeLock();
        if (mSoundPlayer != null && mSoundPlayer.isPlaying()) {
            mSoundPlayer.stop();
        }

        if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
        mSoundStopoListener.onStop();
        checkNeedEncryptForSecurityOS();
    }

    private synchronized void checkNeedEncryptForSecurityOS() {
        if (mIsEncrypt && PlatformUtil.isSecurityOS()) {
            if (TextUtils.isEmpty(mSoundFilePath)) {
                return;
            }
            String destPath = EncryptUtil.getSecuritySpacePath(mSoundFilePath);
            CryptWork cryptWork = new CryptWork(mSoundFilePath, destPath, true);
            FileCryptSDK.setProgressCallback(cryptWork, new ZYTProgressListener(mSoundFilePath,
                    false, new ZYTProgressListener.ZYTProgressCompleteListener() {
                @Override
                public void onCompleted() {
                    if (!TextUtils.isEmpty(mSoundFilePath)) {
                        File file = new File(mSoundFilePath);
                        if (file.exists()) {
                            file.delete();//NOSONAR
                        }
                        mSoundFilePath = null;
                    }
                }
            }));
            ArrayList<CryptWork> workList = new ArrayList<>();
            workList.add(cryptWork);
            FileCryptSDK.addTasks(workList, new ZYTGroupListener());
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mMainHandler.sendEmptyMessage(MESSAGE_UPDATE_SOUND_TIME);
            }
        };
        mTimer.schedule(mTimerTask, new Date(), 1000);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public void completePlayer() {
        stopPlayer();
        cancelTimer();
		//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
        releaseRecorder();
		//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
        dismissDialog();
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
    private void releaseRecorder() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            mAudioFocusChangeListener = null;
            mAudioManager = null;
        }
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950

    private void showDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public interface SoundStopoListener {
        public void onStop();
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }
}
