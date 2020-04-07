package com.android.systemui.qs.tiles;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.widget.Toast;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;  
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.MediaScannerConnection;
import java.io.File;
import java.text.SimpleDateFormat;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.qs.tiles.ScreenRecordingActivity;

//add BUG_ID:YJSQ-933 sunshiwei 20181221 start
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
//add BUG_ID:YJSQ-933 sunshiwei 20181221 end
import android.provider.Settings;

public class RecordService extends Service {
    private final String TAG = "CaptiveImpl";
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;
    
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private boolean mRecording = false;
    private File mRecordFile;
    private String mFilePath;
    private String mVideoName;
    
    // add BUG_ID:CQYL-185 donghao 20181025 start
    private FloatingBallManager manager;
    // add BUG_ID:CQYL-185 donghao 20181025 end

    //add BUG_ID:YJSQ-933 sunshiwei 20181221 start
    private AudioManager mAudioManager = null;
    private OnAudioFocusChangeListener mFocusChangeListener = null;
    //add BUG_ID:YJSQ-933 sunshiwei 20181221 end
    // Add by wangjian for EJQQQ-628 20200302 start
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = 6666;
    private static final String AUTHORITY_MEDIA = "com.android.providers.media.documents";
    // Add by wangjian for EJQQQ-628 20200302 end
    
    // A: Bug_id:YJSQ-933 chenchunyong 20181228 {
    private static final String SCREEN_RECORD_STATE = "screen_record_state";
    // A: }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        // add BUG_ID:CQYL-185 donghao 20181025 start
        if (getResources().getBoolean(R.bool.config_show_record_floating_ball)) {
             manager = FloatingBallManager.getInstance(this);
        }
        // add BUG_ID:CQYL-185 donghao 20181025 end
        //add BUG_ID:YJSQ-933 sunshiwei 20181221 start
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                         stopRecord();
                         broadcastStatus(false);
                }
            }
        };
        //add BUG_ID:YJSQ-933 sunshiwei 20181221 end
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null!=intent){
            if(!mRecording){ // start record
                // M: Bug_id:TQWQO-248 chenchunyong 20180712 {
                //mScreenWidth = intent.getIntExtra("width", 720);
                //mScreenHeight = intent.getIntExtra("height", 1280);
                int width = intent.getIntExtra("width", 720);
                int height = intent.getIntExtra("height", 1280);
                mScreenWidth = width > 720 ? 720 : width;
                mScreenHeight = height> 1280 ? 1280 : height;
                // M: }
                mScreenDensity = intent.getIntExtra("density", 1);
                mResultCode = intent.getIntExtra("code", -1);
                mResultData = intent.getParcelableExtra("data");
                mProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
                mediaRecorder = new MediaRecorder();
                boolean result = startRecord();
                // Add by wangjian for EJQQQ-628 20200302 start
                if (result) {
                    showNotification(0);
                }
                // Add by wangjian for EJQQQ-628 20200302 end
                broadcastStatus(result);
            }else{  // stop record
                // Add by wangjian for EJQQQ-628 20200302 start
                showNotification(1);
                // Add by wangjian for EJQQQ-628 20200302 end
                stopRecord();
                broadcastStatus(false);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.abandonAudioFocus(mFocusChangeListener);//add BUG_ID:YJSQ-933 sunshiwei 20181221
    }

    public boolean startRecord() {
        if (mProjection == null || mRecording) {
            return false;
        }
        Log.d(TAG, "startRecord.. ");
        //add BUG_ID:YJSQ-933 sunshiwei 20181221 start
        int result = mAudioManager.requestAudioFocus(mFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false;
        }
        //add BUG_ID:YJSQ-933 sunshiwei 20181221 end
        return initAndStartRecorder();
    }

    public void stopRecord() {
        // A: Bug_id:YJSQ-933 chenchunyong 20181228 {
        Settings.Global.putInt(getContentResolver(), SCREEN_RECORD_STATE, 0);
        // A: }
        if(mRecording){
            //modify by wangjian for TEWBW-620 20191231 start
            //mediaRecorder.stop();
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                Log.e(TAG, "mediaRecorder.stop() error = " + e.getMessage());
            }
            //modify by wangjian for TEWBW-620 20191231 end
            mediaRecorder.reset();
            mediaRecorder.release();
            Log.d(TAG, "recorder stopped!! ");
            mRecording = false;
            mVirtualDisplay.release();
            mProjection.stop();
            mediaRecorder = null;
            MediaScannerConnection.scanFile(this, new String[]{mRecordFile.toString()}, null,null);
            Toast.makeText(this, getString(R.string.screen_record_stop_tips)+mFilePath, Toast.LENGTH_SHORT).show();
            // add BUG_ID:CQYL-185 donghao 20181025 start
            if (manager != null) {
                manager.close();
            }
            // add BUG_ID:CQYL-185 donghao 20181025 end
        }
    }

    private boolean initAndStartRecorder() {
        boolean success = false;
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // MPEG_4
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        mVideoName = dateformat.format(System.currentTimeMillis())+".mp4";
        mFilePath = getsaveDirectory() + mVideoName;
        mRecordFile = new File(mFilePath);
        mediaRecorder.setOutputFile(mFilePath);
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        //mMediaRecorder.setMaxFileSize(11476947968L);
        mediaRecorder.setOnErrorListener(new OnErrorListener() {
            @Override  
            public void onError(MediaRecorder mr, int what, int extra) {
                exceptionClean();
            }  
        }); 
        try {
            mediaRecorder.prepare();
            mVirtualDisplay = mProjection.createVirtualDisplay("MainScreen", mScreenWidth, mScreenHeight, mScreenDensity,
                                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
            mediaRecorder.start();
            // A: Bug_id:YJSQ-933 chenchunyong 20181228 {
            Settings.Global.putInt(getContentResolver(), SCREEN_RECORD_STATE, 1);
            // A: }

            Toast.makeText(this, R.string.screen_record_start_tips, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "init & start recorder!! ");
            mRecording = true;
            success = true;
            // add BUG_ID:CQYL-185 donghao 20181025 start
            if (manager != null) {
                manager.show();
            }
            // add BUG_ID:CQYL-185 donghao 20181025 end
        } catch (Exception e) {
            Log.d(TAG, "init recorder Exception ..."+e);
            exceptionClean();
        }
        return success;
    }
    private void exceptionClean(){
        try{
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mVirtualDisplay.release();
            mProjection.stop();
            mediaRecorder = null;
            mRecordFile.delete();
            mRecordFile = null;
        }catch(Exception e){
            Log.e(TAG, "exceptionClean.."+e);
        }
    }
    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ScreenRecord" + "/";
            Log.i(TAG, "getsaveDirectory rootDir = " + rootDir);
            File file = new File(rootDir);
            if (!file.exists()) {
                Log.i(TAG, "getsaveDirectory !file.exists");
                if (!file.mkdirs()) {
                    Log.i(TAG, "getsaveDirectory !file.mkdirs");
                    return null;
                }
            }
            return rootDir;
        } else {
            Log.i(TAG, "getsaveDirectory return null");
            return null;
        }
    }

    // Add by wangjian for EJQQQ-628 20200302 start

    /**
     * type==0 stop notification; type==1 go to saved path
     * @param type
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void showNotification(int type){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID = getPackageName() + NOTIFICATION_ID;
        NotificationChannel channel = new NotificationChannel(channelID, "screen_recorder", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        Intent intent = new Intent();
        boolean stop = type == 0;
        if (stop) {
            intent.setClass(this, ScreenRecordingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("status", false);
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    DocumentsContract.buildRootUri(AUTHORITY_MEDIA, "videos_root"),
                    DocumentsContract.Root.MIME_TYPE_ITEM);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                stop ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(stop ? R.string.touch_stop_screen_record : R.string.go_saved_path))
                .setContentText(mFilePath)
                .setSmallIcon(R.drawable.screen_record_select)
                .setAutoCancel(stop ? false : true)
                .setContentIntent(contentIntent)
                .setChannelId(channelID)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }
    // Add by wangjian for EJQQQ-628 20200302 end


    private void broadcastStatus(boolean status){
        Intent myintent = new Intent("com.android.screen.recording");
        myintent.putExtra("status",status);
        this.sendBroadcast(myintent);    
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }
    
    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }
}
