package com.cydroid.note.app.attachment;

import android.app.Activity;
import android.app.Dialog;
import android.app.filecrypt.zyt.filesdk.FileCryptUtil;
import android.app.filecrypt.zyt.services.CryptWork;
import android.content.Context;
//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
import android.media.AudioManager;
//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.cydroid.note.common.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cyee.app.CyeeAlertDialog;

import com.cydroid.note.R;
//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
import com.cydroid.note.app.NoteAppImpl;
//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.ZYTProgressListener;
//GIONEE wanghaiyan 2016-12-01 modify for 37025 begin
import com.cydroid.note.common.FileUtils;
//GIONEE wanghaiyan 2016-12-01 modify for 37025 end

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
//Gionee wanghaiyan 20170307 add for 77568 begin
import com.Legal.Java.*;;
//Gionee wanghaiyan 20170307 add for 77568 end

public class SoundRecorder {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final int MESSAGE_UPDATE_SOUND_TIME = 1;
    private static final int MESSAGE_ERROR_TOAST = 2;

    private Context mContext;
    private Dialog mDialog;
    private TextView mTime;
    private ImageView mStopButton;
    private MediaRecorder mSoundRecorder;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mMainHander;
    private TakeSoundRecorderListener mListener;
    private String mSoundPath;
    private int mDurationInSec;
    private boolean mIsEncrypt;
    private String mSavePath;
    //GIONEE wanghaiyan 2017-2-10 modify for 66156 begin
    public static final int KR_RECORD_TIME_MAX_SECOND = 5*60;//5min
	//Gionee wanghaiyan 2017-3-28 modify for begin 40950
	private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
	//Gionee wanghaiyan 2017-3-28 modify for end 40950
    //GIONEE wanghaiyan 2017-2-10 modify for 66156 end

    public interface TakeSoundRecorderListener {
        void onRecorderStart();
        void onRecorderComplete(String soundPath, int durationInSec);
    }

    public SoundRecorder(Context context, TakeSoundRecorderListener listener) {
        mContext = context.getApplicationContext();
        mListener = listener;
        initHandler();
    }

    public void launchRecording(Activity activity, boolean isEncrypt) {
	    //GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
        if (!hasRequestedAudioFocus()) {
            Toast.makeText(mContext, R.string.attachment_record_focus_fail, Toast.LENGTH_SHORT).show();
            return;
        }
		//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
        initDialog(activity);
        showDialog();
        startRecorder(isEncrypt);
        startTimer();
        if (null != mListener) {
            mListener.onRecorderStart();
        }
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
    public boolean hasRequestedAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) NoteAppImpl.getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                }
            };
        }
        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
	//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950

    private void initDialog(Activity activity) {
        if (mDialog == null) {
            View content = LayoutInflater.from(mContext).inflate(R.layout.sound_recorder_layout, null, false);
            mTime = (TextView) content.findViewById(R.id.sound_recorder_time);
            mStopButton = (ImageView) content.findViewById(R.id.sound_recorder_stop);
            mStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    completeRecorder();
                }
            });
            CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(activity);
            builder.setView(content);
            builder.setCancelable(false);
            Dialog dialog = builder.create();
/*            Dialog dialog = new Dialog(activity, R.style.DialogTheme);
            dialog.setContentView(content);
            dialog.setCancelable(false);*/
            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setGravity(Gravity.BOTTOM);
            mDialog = dialog;
        }
    }

    private void initHandler() {
        mMainHander = new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_UPDATE_SOUND_TIME: {
                        int elapse = msg.arg1;
                        mDurationInSec = elapse;
                        String time = NoteUtils.formatTime(elapse, " : ");
                        mTime.setText(time);
						//GIONEE wanghaiyan 2017-2-10 modify for 66156 begin
						if(NoteUtils.gnKRFlag){
						if (elapse == KR_RECORD_TIME_MAX_SECOND){
							completeRecorder();
							Toast.makeText(mContext,mContext.getResources().getString(R.string.message_media_record_time_max),               Toast.LENGTH_LONG).show();
						}	
						}
						//GIONEE wanghaiyan 2017-2-10 modify for 66156 end
                        break;
                    }
                    case MESSAGE_ERROR_TOAST:
                        checkSoundRecordSuccess();
                        break;
                    default: {
                        break;
                    }
                }
            }
        };
    }

    private void startRecorder(boolean isEncrypt) {
        mIsEncrypt = isEncrypt;
        String soundName = getSoundName();
		//GIONEE wanghaiyan 2016-12-01 modify for 37025 begin
		//if(!Constants.NOTE_MEDIA_SOUND_PATH.exists()) {
		File noteMediaDir = new File(""); 
		noteMediaDir=FileUtils.CheckNoteMediaDir(mContext);
	   
		if (!noteMediaDir.exists()) {
		//boolean success = Constants.NOTE_MEDIA_SOUND_PATH.mkdirs();
			boolean success = noteMediaDir.mkdirs();
			if (!success) {
			    return;
			}
		}
		// String path = Constants.NOTE_MEDIA_SOUND_PATH + "/" + soundName;
		String path =noteMediaDir + "/" + soundName;
	    //GIONEE wanghaiyan 2016-12-01 modify for 37025 end
        //Gionee wanghaiyan 20170307 add for 77568 begin
	    if(NoteUtils.gnKRFlag){		
			//Chenyee 2018-5-10 modify for CSW1703KR-68 begin
			// path = noteMediaDir + "/" + soundName + ".mp3";
			path = noteMediaDir + "/" + soundName + ".3gpp";
			//Chenyee 2018-5-10 modify for CSW1703KR-68 end
	    }	
	    //Gionee wanghaiyan 20170307 add for 77568 end
        mSavePath = path;
        if (!PlatformUtil.isSecurityOS() && isEncrypt) {
            path = Constants.SOUND_ENCRYPT_PATH + File.separator + soundName;
        }
        mSoundPath = path;
        try {//NOSONAR
            mSoundRecorder = new MediaRecorder();
            mSoundRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//NOSONAR
			
			//Chenyee 2018-5-10 modify for CSW1703KR-68 begin
			if(NoteUtils.gnKRFlag){
				mSoundRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	            mSoundRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			} else {
	            mSoundRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//NOSONAR
	            mSoundRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//NOSONAR
			}
			//Chenyee 2018-5-10 modify for CSW1703KR-68 end
			
            mSoundRecorder.setOutputFile(path);//NOSONAR
            mSoundRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {//NOSONAR
                @Override//NOSONAR
                public void onError(MediaRecorder mr, int what, int extra) {
                    completeRecorder();//NOSONAR
                }//NOSONAR
            });//NOSONAR
            mSoundRecorder.prepare();//NOSONAR
            mSoundRecorder.start();//NOSONAR
        } catch (Exception e) {//NOSONAR
            Log.e("SoundRecorder","startRecorder e = " + e.getMessage());
            cancelTimer();
            //Chenyee wanghaiyan 2017-9-1 modify for SW17W16A-235 begin
            Toast.makeText(mContext, R.string.attachment_record_focus_fail, Toast.LENGTH_SHORT).show();//NOSONAR
            //Chenyee wanghaiyan 2017-9-1 modify for SW17W16A-235 end
			//GIONEE :wanghaiyan 2015-11-5 modfiy begin for 40950
            releaseRecorder();
			//GIONEE :wanghaiyan 2015-11-5 modfiy end for 40950
            dismissDialog();//NOSONAR
            return;//NOSONAR
        }
    }

    private void stopRecorder() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mSoundRecorder != null) {
                    try {
                        mSoundRecorder.stop();
                        mSoundRecorder.release();
                        checkNeedEncryptForSecurityOS();
                        mMainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyListener();
                            }
                        });
                    } catch (Exception e) {
                    } finally {
                        mSoundRecorder = null;
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private void checkNeedEncryptForSecurityOS() {
        if (PlatformUtil.isSecurityOS() && mIsEncrypt) {
            String destPath = EncryptUtil.getSecuritySpacePath(mSoundPath);
            CryptWork cryptWork = new CryptWork(mSoundPath, destPath, true);
            FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(mSoundPath, false, null));
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            private int elapse = 0;

            @Override
            public void run() {
                Message message = mMainHander.obtainMessage(MESSAGE_UPDATE_SOUND_TIME);
                message.arg1 = elapse;
                mMainHander.sendMessage(message);
                elapse++;
/*                if(elapse > 1){
                    checkSoundRecordSuccess();
                }*/
            }
        };
        mTimer.schedule(mTimerTask, new Date(), 1000);
        Message message = mMainHander.obtainMessage(MESSAGE_ERROR_TOAST);
        mMainHander.sendMessageDelayed(message, 1500);
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

    private void completeRecorder() {
        stopRecorder();
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

    private void checkSoundRecordSuccess() {
        if (TextUtils.isEmpty(mSoundPath)) {
            return;
        }
        File soundFile = new File(mSoundPath);
        if (soundFile.exists()) {
            long length = soundFile.length();
            if (length <= 0) {
                if (soundFile.delete()) {
                    cancelTimer();
                    dismissDialog();
                    Toast.makeText(mContext, R.string.attachment_record_permission_hint, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void notifyListener() {
        if (mListener != null) {
        //Gionee wanghaiyan 20170307 add for 77568 begin
	    if(NoteUtils.gnKRFlag){
    		Log.d("kptc", "com.cydroid.note.app.attachment.SoundRecorder->notifyListener(): saveSelfSignFile()");
    		int errCode = pwinSign.saveSelfSignFile(mSoundPath);
    		Log.d("kptc", "errCode=" + errCode);
			//Chenyee 2018-5-10 modify for CSW1703KR-68 begin
			pwinSign.sendBroadcastToRedService(mContext, mSoundPath);
			//Chenyee 2018-5-10 modify for CSW1703KR-68 end
	    }
        //Gionee wanghaiyan 20170307 add for 77568 end
            mListener.onRecorderComplete(mSavePath, mDurationInSec);
        }
        mSoundPath = null;
        mSavePath = null;
        mDurationInSec = 0;
    }

    private void showDialog() {
        mTime.setText(NoteUtils.formatTime(0, " : "));
        mDialog.show();
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void cancel() {
        completeRecorder();
    }

    public static synchronized String getSoundName() {
        Date date = new Date(System.currentTimeMillis());
        return DATE_FORMATTER.format(date);
    }
}
