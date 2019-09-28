package com.cydroid.screenrecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.screenrecorder.utils.Log;
import com.cydroid.screenrecorder.utils.MTool;
import com.cydroid.screenrecorder.utils.StorageHelper;
import com.cydroid.screenrecorder.views.IndicationView;
import com.cydroid.screenrecorder.views.MyLinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This service is main class of this application. It will run
 * a service in background to capture screen and save
 * as a video.
 *
 *use MediaProjection  and  MediaRecorder
 *
 * @author fuwenzhi
 */
public class ScreenRecorderService extends Service {
    private static final String TAG = "ScreenRecorderService";

    //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
    //private static final String SCREEN_CAPTURE_FILE_NAME_TEMPLATE = "ScreenRecorder_%s.mp4";
    //private static final String SCREEN_CAPTURE_DIR_NAME = "Screenrecorder";
    private static String SCREEN_CAPTURE_FILE_NAME_TEMPLATE = "ScreenRecorder_%s.mp4";
    private static String SCREEN_CAPTURE_DIR_NAME = "Screenrecorder";
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 end
    private static final String SCREEN_CAPTURE_FILE_NAME_CHECK_AUDIO = "ScreenRecorder_CheckAudio.amr";
    public Context mContext;
    public Resources mResources;
    private WindowManager mWindowManager;
    private LayoutInflater mLayoutInflater;
    private NotificationManager mNotificationManager;
    private ActivityManager mActivityManager;
    private MediaProjectionManager mMediaProjectionManager;
    private AudioManager mAudioManager;
    private ApplicationInfo applicationInfo;
    private int myUid;
    private String packageName;
    private Notification.Builder mNotificationBuilder;
    private int mNotificationId = 2500;

    public int mStatusBarHeight;
    public int mScreenWidth;
    public int mScreenHeight;
    public MyLinearLayout mMainContainer;
    private LayoutParams mMainLayoutParams;
    public LayoutParams mIndicationViewParams;
    private LinearLayout hideLayout;
    private ImageView hideColorImageView;
    private FrameLayout hideLayoutFrameLayout;
    private LinearLayout gestureLayout;
    private LinearLayout soundLayout;
    private LinearLayout stateLayout;
    private LinearLayout timeLayout;
    private LinearLayout exitLayout;
    private TextView timerTextView;
    private ImageView stateImageView;
    private ImageView soundImageView;
    private ImageView gestureImageView;
    private IndicationView indicationView;

    public AtomicBoolean isStoppingMySelf = new AtomicBoolean(false);
    public AtomicBoolean isRecordingNow = new AtomicBoolean(false);
    public AtomicBoolean isOrientationVerticalNow = new AtomicBoolean();
    public AtomicBoolean isMyselfVisible = new AtomicBoolean(true);
    public int mDefaultYPosition;
    public int mMaxYLimited;
    public int mMinYlimited;
    public int mMainLayoutTotalWidth;
    /**
     * view's top left corner X value
     */
    public int mMainLayoutLocationX;
    /**
     * view's top left corner Y value. Note: not contain status bar height
     */
    public int mMainLayoutLocationY;

    public int mMainLayoutFirstLocationX;
    public int mMainLayoutFirstLocationY;

    public int mIndicationViewLocationX;
    public int mIndicationViewLocationY;
    public int mIndicationWidth = 0;
    public int mIndicationHeight = 0;
    private int mNavigationBarHeightOverall = 0;
    public boolean mDefaultTouchesOptionsValue;
    /**
     * total used time
     */
    private int totalRecordTime = 0;

    /**
     * gesture value
     */
    public boolean mNowTouchesOptionsValue;
    private Method forceStopPackageMethod;
    private MyLinearLayout myLinearLayout;
    private IBinder mMediaProjectionServiceBinder;
    private IMediaProjectionManager mMediaProjectionService;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private int mScreenDensity;
    private String mVideoFilePath;
    private String mVideoFileName;
    private long mVideoTime;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoFrameRate = 25;

    /**
     * record sound with MIC
     */
    private boolean isRecordSound = true;

    //GIONEE 20160920 lixiaohong add for CR01762445 begin
    public void resetIsRecordSound() {
        isRecordSound = Constant.CUSTOMER_VISUALFAN ? false : true;
    }

    //GIONEE 20160920 lixiaohong add for CR01762445 end
    private static final long LEAST_SPACE_NEED = 300 * 1024 * 1024;//300M
    private Uri mUri;
    public long mLastScreenOffTime;
    static final long AUTO_EXIT_SCREEN_OFF_TIME_OUT = 30 * 1000;//30s
    private String mDateFormat = "yyyyMMdd-HHmmss";
    public int mVerticalLastPositionY;
    public int mHorizontalLastPositionY;
    private boolean mExternalStorageAvailable = false;
    private boolean mExternalStorageWriteable = false;
    private boolean isAudioFocused = false;
    private boolean isDoingStateClickEvent = false;
    private int initLowPowerModeState;
    private boolean isRegisted = false;
    private boolean isAudioAvailable = false;
    private boolean isServiceKilled = false;
    private boolean isDurationZero = false;
    private boolean isVirtualDisplayNull = false;
    private H mHandler = new H(this);
    //GIONEE 20170704 lixiaohong add for #165394 begin
    private boolean changeFromScreenRecorder = false;
    //GIONEE 20170704 lixiaohong add for #165394 end 
    private ScreenRecorderObserver mScreenRecorderObserver;
    private HandlerThread mHandlerThread;
    private Handler mWorkerHandler;
    //录屏文件保存位置类型标识
    private static final int TYPE_INTERNAL = 0;
    private static final int TYPE_SDCARD = 1;
    private int mCurrentType = TYPE_INTERNAL;

    private static final int SCREEN_HEIGHT_EFF = 854;
    private String videoPathDir = null;
    private boolean shouldForceStopScreenRecord = false;
    private final StopScreenRecord mStopScreenRecord = new StopScreenRecord();
    // private static final Logger MLOGGER = Logger.getLogger(ScreenRecorderService.class.getSimpleName());

    //chenyee zhaocaili 20180424 add begin
    private NotificationChannel mNotificationChannel;
    private String mChannelId = "ScreenRecorderService_ChannelId";
    private String mChannelName = "ScreenRecorderService";
    //chenyee zhaocaili 20180424 add end

    @Override
    public void onCreate() {
        //chenyee zhaocaili 20180511 modify for CSW1707A-974 begin
        Log.d(TAG, "onCreate() start setLog " + Log.setLogEnableOrNot());
        //chenyee zhaocaili 20180511 modify for CSW1707A-974 end
        mContext = getApplicationContext();
        mResources = mContext.getResources();
        if (isPhoneInUse()) {
            Toast.makeText(mContext, mResources.getString(R.string.unsupport_during_call), 0).show();
            stopSelf();
        } else {
            //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
            initScreenRecorderSaveDir();
            //chenyee zhaocaili 20180421 add for CSW1703MX-70 end
            initSystemService();
            initParametersNormal();
            initParametersOrientationRelative(true);
            reflectClassesAndMethods();
            initViews();
            setViewsClickListener();
            registReceivers();
            ensureHandlerExists();
            mScreenRecorderObserver = new ScreenRecorderObserver(mWorkerHandler);
            mScreenRecorderObserver.init();
            //GIONEE 20170410 lixiaohong add for #102737 begin
            runForeground();
            //GIONEE 20170410 lixiaohong add for #102737 end
        }
    }

    //GIONEE 20170410 lixiaohong add for #102737 begin
    private static final int ID_NOTIFICATION_FOREGROUND = 0X00111;

    private void runForeground() {
        //chenyee zhaocaili 20180424 add begin
        mNotificationChannel = new NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
        mNotificationBuilder = new Notification.Builder(mContext, mChannelId);
        mNotificationBuilder.setContentTitle(getResources().getString(R.string.screenrecorder_run_foreground))
        //chenyee zhaocaili 20180424 add end
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        Notification n = mNotificationBuilder.build();
        n.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(ID_NOTIFICATION_FOREGROUND, n);
    }

    private void stopForeground() {
        stopForeground(true);
    }

    //GIONEE 20170410 lixiaohong add for #102737 end
    private void setSoundEnabled(boolean enabled) {
        soundLayout.setEnabled(enabled);
        soundLayout.setClickable(enabled);
    }

    //判断内部存储
    private boolean isSpaceEnough() {
        final long availableSpace = getAvailableInternalMemorySize();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Log.d(TAG, "isSpaceEnough: space left = " + availableSpace/1024/1024 + " M");
        return availableSpace >= LEAST_SPACE_NEED;
    }

    private void initSystemService() {
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mMediaProjectionServiceBinder = ServiceManager.getService(MEDIA_PROJECTION_SERVICE);
        mMediaProjectionService = IMediaProjectionManager.Stub.asInterface(mMediaProjectionServiceBinder);
        mMediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                Log.d(TAG, "focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback 
                Log.d(TAG, "focusChange == AudioManager.AUDIOFOCUS_GAIN");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                Log.d(TAG, "focusChange == AudioManager.AUDIOFOCUS_LOSS");
                isAudioFocused = false;
                mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            }
        }
    };

    private void registReceivers() {
        if (!isRegisted) {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mFilter.addAction(Intent.ACTION_SCREEN_ON);
            mFilter.addAction(Intent.ACTION_SHUTDOWN);
            mFilter.addAction("android.intent.action.PHONE_STATE");
            //chenyee zhaocaili 20180511 modify for CSW1707A-974 begin
            mFilter.addAction("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED"); //MTKLog 开关广播
            //chenyee zhaocaili 20180511 modify for CSW1707A-974 end
            registerReceiver(mReceiver, mFilter);
            isRegisted = true;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        isServiceKilled = true;
        releaseMediaResource();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isRecordingNow.get()) {
            exitScreenRecorder(false);
            return;
        }
        //chenyee zhaocaili 20180720 modify for CSW1803A-1182 begin
        boolean isOldOrientationVertical = isOrientationVerticalNow.get();
        boolean isNewOrientationVertical = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isOldOrientationVertical != isNewOrientationVertical){
            isOrientationVerticalNow.getAndSet(isNewOrientationVertical);
            initParametersOrientationRelative(false);
            resetMainContainer();
            //chenyee zhaocaili 20180706 modify for CSW1703CX-1113 begin
            if (Constant.HAS_NAVIGATIONBAR) {
                mIndicationViewLocationX = mIndicationViewLocationX + mNavigationBarHeightOverall;
            }
            //chenyee zhaocaili 20180706 modify for CSW1703CX-1113 end
            resetIndicationView();
        }
        //chenyee zhaocaili 20180720 modify for CSW1803A-1182 end
    }

    private void notifyWMUpdateViewLayout(View view, LayoutParams layoutParams) {
        if (view != null) {
            mWindowManager.updateViewLayout(view, layoutParams);
        }
    }

    private void resetMainContainer() {

        if (isOrientationVerticalNow.get()) {
            mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5;
        } else {
            if (Constant.HAS_NAVIGATIONBAR) {
                mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5;
            } else {
                mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5 * 2;
            }
        }
        //mMainLayoutParams.x = mMainLayoutLocationX;
        mMainLayoutParams.y = mMainLayoutLocationY;
        notifyWMUpdateViewLayout(mMainContainer, mMainLayoutParams);
    }

    private void resetIndicationView() {
        mIndicationViewParams.x = mIndicationViewLocationX;
        mIndicationViewParams.y = mMainLayoutLocationY;
        notifyWMUpdateViewLayout(indicationView, mIndicationViewParams);
    }


    private boolean isOrientationVerticalNow() {
        return mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * inital mormal parameters
     */
    private void initParametersNormal() {
        applicationInfo = mContext.getApplicationInfo();
        myUid = applicationInfo.uid;
        packageName = applicationInfo.packageName;
        isOrientationVerticalNow.getAndSet(isOrientationVerticalNow());
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        initVideoWidthAndHeight(metrics);
        //mMediaRecorder = new MediaRecorder();
        mMediaProjectionCallback = new MediaProjectionCallback();
        mDefaultTouchesOptionsValue = MTool.getTouchesOptionsValue(mContext);
        initLowPowerModeState = getLowPowerModelState();
        //GIONEE 20160827 lixiaohong add for CR01753564 begin
        mNowTouchesOptionsValue = mDefaultTouchesOptionsValue;
        //GIONEE 20160920 lixiaohong add for CR01762572 begin
        if (Constant.CUSTOMER_VISUALFAN) {
            mNowTouchesOptionsValue = true;
            MTool.setTouchesOptionsValue(mContext, true);
        }
    }

    private void initVideoWidthAndHeight(DisplayMetrics metrics) {
        if (Constant.HAS_NAVIGATIONBAR) {
            int mNavigationBarHeight = getNavigationBarHeight();
            if (metrics.widthPixels > metrics.heightPixels) {
                mVideoWidth = metrics.widthPixels + mNavigationBarHeight;
                mVideoHeight = metrics.heightPixels;
            } else {
                mVideoWidth = metrics.widthPixels;
                mVideoHeight = metrics.heightPixels + mNavigationBarHeight;
            }
        } else {
            mVideoWidth = metrics.widthPixels;
            mVideoHeight = metrics.heightPixels;
        }
    }

    /**
     * depends on screen current orientation
     */
    public void initParametersOrientationRelative(boolean isOnCreateCall) {
        //chenyee zhaocaili 20180426 add for CSW1703A-1992 begin
        /*if (StorageHelper.isSDCardInserted(getApplicationContext())
                && isSDcardSpaceEnough() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mCurrentType = TYPE_SDCARD;
        }else {
            mCurrentType = TYPE_INTERNAL;
        }*/
        //chenyee zhaocaili 20180426 add for CSW1703A-1992 end
        if (isOrientationVerticalNow.get()) {//screen vertical now
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
            mStatusBarHeight = getStatusBarHeight();
            mNavigationBarHeightOverall = getNavigationBarHeight();
            mIndicationWidth = mResources.getDimensionPixelSize(R.dimen.indication_width);
            mIndicationHeight = mResources.getDimensionPixelSize(R.dimen.indication_height);
            mMainLayoutTotalWidth = 5 * mResources.getDimensionPixelSize(R.dimen.single_menu_width);
            if (isMyselfVisible.get()) {
                mMainLayoutLocationX = mScreenWidth - mMainLayoutTotalWidth;
            } else {
                mMainLayoutLocationX = mScreenWidth;
            }
            mMainLayoutLocationY = mScreenHeight - 7 * mStatusBarHeight;
            mMainLayoutFirstLocationX = mScreenWidth / 2 - 2 * mResources.getDimensionPixelSize(R.dimen.single_menu_width);
            mMainLayoutFirstLocationY = mMainLayoutLocationY;
            mMaxYLimited = mScreenHeight - 5 * mStatusBarHeight;
            mMinYlimited = mStatusBarHeight * 3;
            mIndicationViewLocationX = mScreenWidth - mIndicationWidth;
            mIndicationViewLocationY = mMainLayoutLocationY;
            if (mVerticalLastPositionY != 0) {
                mMainLayoutLocationY = mVerticalLastPositionY;
                mIndicationViewLocationY = mVerticalLastPositionY;
            }
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
            mStatusBarHeight = getStatusBarHeight();
            mNavigationBarHeightOverall = getNavigationBarHeight();
            mIndicationWidth = mResources.getDimensionPixelSize(R.dimen.indication_width);
            mIndicationHeight = mResources.getDimensionPixelSize(R.dimen.indication_height);
            mMainLayoutTotalWidth = 5 * mResources.getDimensionPixelSize(R.dimen.single_menu_width);
            if (isMyselfVisible.get()) {
                mMainLayoutLocationX = mScreenWidth - mMainLayoutTotalWidth;
            } else {
                mMainLayoutLocationX = mScreenWidth;
            }
            mMainLayoutLocationY = mScreenHeight - 4 * mStatusBarHeight;
            if (Constant.HAS_NAVIGATIONBAR) {
                mMainLayoutFirstLocationX = (mScreenWidth + mNavigationBarHeightOverall) / 2 - 2 * mResources.getDimensionPixelSize(R.dimen.single_menu_width);
            } else {
                mMainLayoutFirstLocationX = mScreenWidth / 2 - 2 * mResources.getDimensionPixelSize(R.dimen.single_menu_width);
            }
            mMainLayoutFirstLocationY = mMainLayoutLocationY;
            //mMaxYLimited = mScreenHeight - 3*mStatusBarHeight+mIndicationHeight;
            mMaxYLimited = mScreenHeight - 3 * mStatusBarHeight;
            mMinYlimited = mStatusBarHeight * 2;
            mIndicationViewLocationX = mScreenWidth - mIndicationWidth;
            mIndicationViewLocationY = mMainLayoutLocationY;
            if (mHorizontalLastPositionY != 0) {
                mMainLayoutLocationY = mHorizontalLastPositionY;
                mIndicationViewLocationY = mHorizontalLastPositionY;
            }
        }
        if (Constant.DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("ScreenWidth=" + mScreenWidth)
                    .append(" ScreenHight=" + mScreenHeight)
                    .append(" StatusBar Height=" + mStatusBarHeight)
                    .append(" Indication Width=" + mIndicationWidth)
                    .append(" Indication Height=" + mIndicationHeight)
                    .append(" MainLayout Total Width=" + mMainLayoutTotalWidth)
                    .append(" MainLayout Location X=" + mMainLayoutLocationX)
                    .append(" MainLayout Location Y=" + mMainLayoutLocationY)
                    .append(" MaxYLimited=" + mMaxYLimited)
                    .append(" MinYlimited=" + mMinYlimited);
            Log.d(TAG, builder.toString());
        }
    }

    private void initRecorder() {
        checkAudioAvailableWhenClick();
        mMediaRecorder = new MediaRecorder();
        if (isAudioAvailable && isRecordSound) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (isRecordSound && isAudioAvailable) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(320 * 1000);
            mMediaRecorder.setAudioSamplingRate(44100);
        }
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mMediaRecorder.setVideoFrameRate(mVideoFrameRate);

        // Chenyee xionghg 20180226 modify for CSW1703A-112 begin
        int[] videoSize = getVideoSize(mVideoWidth, mVideoHeight);
        mMediaRecorder.setVideoSize(videoSize[0], videoSize[1] /*mVideoHeight == SCREEN_HEIGHT_EFF ? SCREEN_HEIGHT_EFF + 10 : mVideoHeight*/);
        // Chenyee xionghg 20180226 modify for CSW1703A-112 end
        //GIONEE 20160824 lixiaohong modify for CR01750787 begin
        mMediaRecorder.setOutputFile(mCurrentType == TYPE_INTERNAL ? getVideoPath() : getVideoPathFromSDcard());
        //GIONEE 20160824 lixiaohong modify for CR01750787 end
    }

    // Chenyee xionghg 20180226 add for CSW1703A-112 begin
    private int[] getVideoSize(int originWidth, int originHeight) {
        int[] result = new int[2];
        result[0] = originWidth;
        result[1] = originHeight;

        //GIONEE 20170426 lixiaohong modify for #119614 begin
        //eg.[480x854 is not supported by codec, suggest to set it as 480x864]
        if (originHeight == SCREEN_HEIGHT_EFF) {
            result[1] = SCREEN_HEIGHT_EFF + 10;
        }
        //GIONEE 20170426 lixiaohong modify for #119614 end

        // 1703最高支持1080*1920个像素的录屏，所以这里需要等比缩小输出视频的宽高
        //chenyee zhaocaili 20181024 add for CSW1805A-321 begin
        if (originWidth * originHeight > 1080 * 1920) {
            //chenyee zhaocaili 20181024 add for CSW1805A-321 end
            if (originWidth < originHeight) {  // 竖屏下宽设为720
                result[0] = 720;
                //chenyee zhaocaili 20180426 add for CSW1703A-2315 begin
                if (originHeight == 2246 || originHeight == 2390) {
                //chenyee zhaocaili 20180426 add for CSW1703A-2315 end
                    result[1] = 1496;
                //chenyee zhaocaili 2018824 add for CSW1703A-3693 begin
                } else if (originHeight == 2160 || originHeight == 2159) {
                //chenyee zhaocaili 2018824 add for CSW1703A-3693 end
                    result[1] = 1440;
                //chenyee zhaocaili 20180426 add for CSW1703A-1992 begin
                //chenyee zhaocaili 20181024 add for CSW1805A-321 begin
                } else if (originHeight >= 2258) {
                    result[1] = 1560;
                //chenyee zhaocaili 20181024 add for CSW1805A-321 end
                } else {
                    result[1] = originHeight * 2 / 3;
                //chenyee zhaocaili 20180426 add for CSW1703A-1992 end
                }
            } else {  // 横屏下高设为720
                result[1] = 720;
                //chenyee zhaocaili 20180426 add for CSW1703A-2315 begin
                if (originWidth == 2246 || originWidth == 2390) {
                //chenyee zhaocaili 20180426 add for CSW1703A-2315 end
                    result[0] = 1496;
                //chenyee zhaocaili 2018824 add for CSW1703A-3693 begin
                } else if (originWidth == 2160 || originWidth == 2159) {
                //chenyee zhaocaili 2018824 add for CSW1703A-3693 end
                    result[0] = 1440;
                //chenyee zhaocaili 20180426 add for CSW1703A-1992 begin
                //chenyee zhaocaili 20181024 add for CSW1805A-321 begin
                } else if (originWidth >= 2258) {
                    result[0] = 1560;
                //chenyee zhaocaili 20181024 add for CSW1805A-321 end
                } else {
                    result[0] = originWidth * 2 / 3;
                //chenyee zhaocaili 20180426 add for CSW1703A-1992 begin
                }
            }
        //chenyee zhaocaili 20180611 add for CSW1802A-43 begin
        }else if (originWidth * originHeight > 720 * 1280){
            float scale = 0.8f;//(float) 1280 / Math.max(originWidth, originHeight);
            result[0] = (int)(originWidth * scale);
            result[1] = (int)(originHeight * scale);
        }
        //chenyee zhaocaili 20180611 add for CSW1802A-43 end

        Log.d(TAG, "getVideoSize: origin width=" + originWidth + ", height=" + originHeight +
                "; videoWidth=" + result[0] + ", videoHeight=" + result[1]);
        // Chenyee xionghg 20180319 add for CSW1703A-482 begin
        mVideoWidth = result[0];
        mVideoHeight = result[1];
        // Chenyee xionghg 20180319 add for CSW1703A-482 end
        return result;
    }
    // Chenyee xionghg 20180226 add for CSW1703A-112 end

    private boolean prepareRecorder() {
        boolean success = true;
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "call mMediaRecorder.prepare() occur IllegalStateException.");
            success = false;
        } catch (IOException e) {
            Log.e(TAG, "call mMediaRecorder.prepare() occur IOException.");
            success = false;
        } catch (Exception e) {
            Log.e(TAG, "call mMediaRecorder.prepare() occur Exception.");
            success = false;
        }
        return success;
    }

    private String getVideoPath() {
        mVideoTime = System.currentTimeMillis();
        String mVideoDate = new SimpleDateFormat(mDateFormat).format(new Date(mVideoTime));
        mVideoFileName = String.format(SCREEN_CAPTURE_FILE_NAME_TEMPLATE, mVideoDate);
        //GIONE  20161217 lixiaohong modify for #46896 begin
        File mScreenCaptureDir = null;
        if (Constant.CUSTOMER_INDIA) {
            mScreenCaptureDir = new File(Environment.getExternalStorageDirectory(), SCREEN_CAPTURE_DIR_NAME);
        } else {
            mScreenCaptureDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), SCREEN_CAPTURE_DIR_NAME);
        }

        //GIONE  20161217 lixiaohong modify for #46896 end
        if (!mScreenCaptureDir.exists()) {
            mScreenCaptureDir.mkdirs();
        }
        mVideoFilePath = new File(mScreenCaptureDir, mVideoFileName).getAbsolutePath();
        return mVideoFilePath;
    }

    private void updateVideoUri() {
        ContentValues values = new ContentValues();
        ContentResolver resolver = mContext.getContentResolver();
        long dateSeconds = mVideoTime / 1000;
        values.put(MediaStore.Video.VideoColumns.DATA, mVideoFilePath);
        values.put(MediaStore.Video.VideoColumns.TITLE, mVideoFileName);
        values.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, mVideoFileName);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, mVideoTime);
        values.put(MediaStore.Video.VideoColumns.DATE_ADDED, dateSeconds);
        values.put(MediaStore.Video.VideoColumns.DATE_MODIFIED, dateSeconds);
        values.put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.VideoColumns.DURATION, totalRecordTime * 1000);
        Log.d(TAG, "updateVideoUri: before insert mUri = " + mUri + ", values = " + values);
        try {
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            Log.e(TAG, "updateVideoUri: insert failed: " + e.getMessage(), e);
        }
        Log.d(TAG, "updateVideoUri:  after insert mUri = " + mUri);
        //autolly update the media database
        //chenyee zhaocaili 20180507 modify for CSW1703A-2100 begin
        if (mUri == null && !reNamed){
            Log.d(TAG, "insert media provider failed, resave again!");
            reNamed = true;
            reSaveScreenRecorder(resolver);
        }else {
            notifyMediaScanner(mVideoFilePath);
        }
        //chenyee zhaocaili 20180507 modify for CSW1703A-2100 end
    }

    //chenyee zhaocaili 20180507 add for CSW1703A-2100 begin
    private  boolean reNamed = false;
    private void reSaveScreenRecorder(ContentResolver resolver){
        String oldVideoPath = mVideoFilePath;
        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
        String newVideoPath = mCurrentType == TYPE_INTERNAL? getVideoPath() : getVideoPathFromSDcard();
        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
        Log.d(TAG, "oldVideoPath= " + oldVideoPath + "   newVideoPath=" + newVideoPath);

        File file = new File(oldVideoPath);
        Log.d(TAG, "file.exists()= " + file.exists());
        if (file.exists()){
            boolean reNameOk = file.renameTo(new File(newVideoPath));
            Log.d(TAG, "reNameOk = " + reNameOk);
            if (reNameOk){
                updateVideoUri();
                resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.VideoColumns.DATA + "=?", new String[]{oldVideoPath});
            }else {
                notifyMediaScanner(null);
            }
        }
    }

    private void notifyMediaScanner(String vedioPath){
        if (vedioPath == null || vedioPath.isEmpty()){
            notifyAtStatusbar(null);
        }else {
            //chenyee zhaocaili 20180711 modify for SWW1618OTA-779 begin
            notifyAtStatusbar(mUri);
            MediaScannerConnection.scanFile(mContext, new String[]{vedioPath}, new String[]{"mp4"}, null);
            //chenyee zhaocaili 20180711 modify for SWW1618OTA-779 end
        }
    }
    //chenyee zhaocaili 20180507 add for CSW1703A-2100 end

    //GIONEE 20160817 lixiaohong modify for CR01747671 begin
    private VirtualDisplay createVirtualDisplay() throws IllegalStateException {
        if (mMediaProjection == null) {
            Log.d(TAG, "createVirtualDisplay() mMediaProjection == null");
        }
		//This is the key code. MediaProjection capture the screen and show it on the sixth param(a surface, here is MediaRecorder's surface)
        //After this ,we start record the screen.
        return mMediaProjection.createVirtualDisplay(packageName, mVideoWidth, mVideoHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }
    //GIONEE 20160817 lixiaohong modify for CR01747671 end


    private Intent getMediaProjectionIntent(int uid, String packageName, boolean permanentGrant)
            throws RemoteException {
        IMediaProjection projection = mMediaProjectionService.createProjection(uid, packageName,
                MediaProjectionManager.TYPE_SCREEN_CAPTURE, permanentGrant);
        Intent intent = new Intent();
        intent.putExtra(MediaProjectionManager.EXTRA_MEDIA_PROJECTION, projection.asBinder());
        return intent;
    }

    /**
     * reflect class or method
     */
    private void reflectClassesAndMethods() {
        try {
            forceStopPackageMethod = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * inital view,layout etc.
     */
    public void initViews() {
        initIndicationView();
        initMainContainer();
    }

    private void initMainContainer() {
        mMainContainer = (MyLinearLayout) mLayoutInflater.inflate(R.layout.main_layout, null);
        mMainContainer.setMyService(this);
        mMainLayoutParams = new WindowManager.LayoutParams();
        mMainLayoutParams.format = PixelFormat.RGBA_8888;
        mMainLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mMainLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        // Gionee <GN_Oversea_Bug> <xiaopeng> <20170719> for 160439 begin
        //chenyee 20170904 guoxt add for 201889 begin
        //mMainLayoutParams.type = LayoutParams.TYPE_TYPE_TOAST;
        mMainLayoutParams.type = LayoutParams.TYPE_PHONE;
        //chenyee 20170904 guoxt add for 201889 end
        // Gionee <GN_Oversea_Bug> <xiaopeng> <20170719> for 160439 end
        mMainLayoutParams.x = mMainLayoutFirstLocationX;
        mMainLayoutParams.y = mMainLayoutFirstLocationY;
        mMainLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mMainLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mMainLayoutParams.setTitle("screenRecorderMainView");
        mWindowManager.addView(mMainContainer, mMainLayoutParams);
        myLinearLayout = (MyLinearLayout) mMainContainer.findViewById(R.id.my_linearlayout);
        myLinearLayout.requestFocus();

        hideLayout = (LinearLayout) mMainContainer.findViewById(R.id.hide_main_layout);
        hideLayoutFrameLayout = (FrameLayout) mMainContainer.findViewById(R.id.hide_main_layout_frame);
        hideLayoutFrameLayout.setVisibility(View.GONE);
        hideColorImageView = (ImageView) mMainContainer.findViewById(R.id.hide_main_layout_img_color);
        hideColorImageView.setVisibility(View.GONE);
        gestureLayout = (LinearLayout) mMainContainer.findViewById(R.id.record_gesture);
        soundLayout = (LinearLayout) mMainContainer.findViewById(R.id.record_sound);
        stateLayout = (LinearLayout) mMainContainer.findViewById(R.id.record_state);
        timeLayout = (LinearLayout) mMainContainer.findViewById(R.id.record_time);
        //exitLayout = (LinearLayout)mMainContainer.findViewById(R.id.record_exit);

        timerTextView = (TextView) mMainContainer.findViewById(R.id.record_time_content);

        stateImageView = (ImageView) mMainContainer.findViewById(R.id.record_state_img);
        soundImageView = (ImageView) mMainContainer.findViewById(R.id.record_sound_img);
        //GIONEE 20160920 lixiaohong add for CR01762445 begin
        resetIsRecordSound();
        soundImageView.setBackgroundResource(isRecordSound ? R.drawable.sound_normal : R.drawable.sound_close);
        //GIONEE 20160920 lixiaohong add for CR01762445 end
        gestureImageView = (ImageView) mMainContainer.findViewById(R.id.record_gesture_img);
        //GIONEE 20160827 lixiaohong add for CR01753564 begin
        gestureImageView.setBackgroundResource(mNowTouchesOptionsValue ? R.drawable.gesture_normal : R.drawable.gesture_disabled);
        //GIONEE 20160827 lixiaohong add for CR01753564 end 
    }


    private void initIndicationView() {
        indicationView = new IndicationView(this);
        mIndicationViewParams = new WindowManager.LayoutParams();
        mIndicationViewParams.format = PixelFormat.RGBA_8888;
        mIndicationViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mIndicationViewParams.gravity = Gravity.TOP | Gravity.LEFT;
        //chenyee 20170904 guoxt add for 201889 begin
        //mIndicationViewParams.type = LayoutParams.TYPE_TOAST;
        mIndicationViewParams.type = LayoutParams.TYPE_PHONE;
        //chenyee 20170904 guoxt add for 201889 end
        if (isOrientationVerticalNow.get()) {
            mIndicationViewParams.x = mIndicationViewLocationX;
        } else {
            if (Constant.HAS_NAVIGATIONBAR) {
                mIndicationViewParams.x = mIndicationViewLocationX - mNavigationBarHeightOverall;
            } else {
                mIndicationViewParams.x = mIndicationViewLocationX;
            }
        }
        //mIndicationViewParams.x = mIndicationViewLocationX;
        mIndicationViewParams.y = mIndicationViewLocationY;
        //mIndicationViewParams.x = 200;
        //mIndicationViewParams.y = 0;
        mIndicationViewParams.width = LayoutParams.WRAP_CONTENT;
        mIndicationViewParams.height = LayoutParams.WRAP_CONTENT;
        mIndicationViewParams.setTitle("IndicationView");
        mWindowManager.addView(indicationView, mIndicationViewParams);
        indicationView.setVisibility(View.INVISIBLE);
    }

    public void updateIndicationViewPositionY(int y) {
        mIndicationViewParams.y = y;
        notifyWMUpdateViewLayout(indicationView, mIndicationViewParams);
    }

    private void setViewsClickListener() {
        indicationView.setOnClickListener(new ClickListener(TYPE_CLICK_INDICATIONVIEW));
        hideLayout.setOnClickListener(new ClickListener(TYPE_CLICK_HIDELAYOUT));
        gestureLayout.setOnClickListener(new ClickListener(TYPE_CLICK_GESTURELAYOUT));
        soundLayout.setOnClickListener(new ClickListener(TYPE_CLICK_SOUNDLAYOUT));
        stateLayout.setOnClickListener(new ClickListener(TYPE_CLICK_STATELAYOUT));
    }

    static final int TYPE_CLICK_INDICATIONVIEW = 0;
    static final int TYPE_CLICK_HIDELAYOUT = 1;
    static final int TYPE_CLICK_GESTURELAYOUT = 2;
    static final int TYPE_CLICK_SOUNDLAYOUT = 3;
    static final int TYPE_CLICK_STATELAYOUT = 4;

    private final class ClickListener implements View.OnClickListener {
        private final int type;

        public ClickListener(int type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            switch (type) {
                case TYPE_CLICK_INDICATIONVIEW:
                    indicationViewClick();
                    break;
                case TYPE_CLICK_HIDELAYOUT:
                    hideLayoutClick();
                    break;
                case TYPE_CLICK_GESTURELAYOUT:
                    gestureLayoutClick();
                    break;
                case TYPE_CLICK_SOUNDLAYOUT:
                    soundLayoutClick();
                    break;
                case TYPE_CLICK_STATELAYOUT:
                    stateLayoutClick();
                    break;
                default:
                    //Ignore
            }
        }
    }

    private void indicationViewClick() {
        showMainLayoutAnimation();
    }

    private void hideLayoutClick() {
        if (Constant.HAS_NAVIGATIONBAR) {
            mIndicationViewLocationX = mIndicationViewLocationX + mNavigationBarHeightOverall;
            resetIndicationView();
        }
        hideMainLayoutAnimation();
    }

    private void soundLayoutClick() {
        if (isRecordingNow.get()) {
            return;
        }
        if (isRecordSound) {
            isRecordSound = false;
            soundImageView.setBackgroundResource(R.drawable.sound_close);
        } else {
            isRecordSound = true;
            soundImageView.setBackgroundResource(R.drawable.sound_normal);
        }
    }

    private void stateLayoutClick() {
        // Chenyee xionghg 20180314 modify for CSW1705A-2136 begin
        // 插入sd卡的情况下优先保存至sd卡
        // mCurrentType = TYPE_INTERNAL;
        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
        //mCurrentType = TYPE_SDCARD;
        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
        if (isDoingStateClickEvent) {
            return;
        }
        isDoingStateClickEvent = true;
        if (isRecordingNow.get()) {
            exitScreenRecorder(false);
        } else {
            // if (!isSpaceEnough()) {
            //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
            //mCurrentType = TYPE_SDCARD;
            //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
            if (!isSDcardSpaceEnough() || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                boolean showSpaceNotEnough = true;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // if (isSDcardSpaceEnough()) {
                    if (isSpaceEnough()) {
                        // mCurrentType = TYPE_SDCARD;
                        mCurrentType = TYPE_INTERNAL;
                        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
                        //showSpaceNotEnough = false;
                        if (!isQuotaExceeded()){
                            showSpaceNotEnough = false;
                        }
                        //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
                    }
                }
                if (showSpaceNotEnough) {
                    //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
                    isDoingStateClickEvent = false;
                    //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
                    Toast.makeText(mContext, mResources.getString(R.string.record_space_not_enough), 0).show();
                    return;
                }
            }
            stopRecording();
            mWorkerHandler.post(() -> {
                mTimeOutTask.setReason(mContext.getResources().getString(R.string.initial_timeout));
                mHandler.postDelayed(mTimeOutTask, Constant.TIMEOUT);
                initRecorder();
                mHandler.removeCallbacks(mTimeOutTask);

                mTimeOutTask.setReason(mContext.getResources().getString(R.string.initial_timeout));
                mHandler.postDelayed(mTimeOutTask, Constant.TIMEOUT);
                if (prepareRecorder()) {
                    mHandler.sendEmptyMessage(H.MSG_MEDIARECORDER_PREPARE_OK);
                } else {
                    //chenyee zhaocaili 20180514 modify for CSW1703CX-551 begin
                    if (isQuotaExceeded()){
                        Toast.makeText(mContext, mResources.getString(R.string.record_space_not_enough), Toast.LENGTH_SHORT).show();
                        mHandler.removeCallbacks(mTimeOutTask);
                        return;
                    }else {
                        mHandler.sendEmptyMessage(H.MSG_MEDIARECORDER_PREPARE_FAILED);
                        return;
                    }
                    //chenyee zhaocaili 20180514 modify for CSW1703CX-551 end
                }
                mHandler.removeCallbacks(mTimeOutTask);
            });
        }
        // Chenyee xionghg 20180314 modify for CSW1705A-2136 end
        mHandler.sendEmptyMessageDelayed(H.MSG_DELAY_CLICK_STATE_LAYOUT, 1000);
    }

    private void gestureLayoutClick() {
        mNowTouchesOptionsValue = !mNowTouchesOptionsValue;
        gestureImageView.setBackgroundResource(mNowTouchesOptionsValue ? R.drawable.gesture_normal : R.drawable.gesture_disabled);
        //GIONEE 20170704 lixiaohong add for #165394 begin
        changeFromScreenRecorder = (mDefaultTouchesOptionsValue != mNowTouchesOptionsValue);
        //GIONEE 20170704 lixiaohong add for #165394 end       
        MTool.setTouchesOptionsValue(mContext, mNowTouchesOptionsValue);
    }

    private State startRecordScreen() {
        if (mMediaProjection == null) {
            Intent mediaProjectionIntent = null;
            try {
                mediaProjectionIntent = getMediaProjectionIntent(myUid, packageName, true);
            } catch (RemoteException e) {
                return State.REMOTE_EXCEPTION;
            }
            if (mediaProjectionIntent != null) {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionIntent);
                mMediaProjection.registerCallback(mMediaProjectionCallback, mHandler);
            }
        }
        try {
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            return State.ILLEGAL_STATE_EXCEPTION;
        } catch (SecurityException e) {
            return State.SECURITY_EXCEPTION;
        }
        stateImageView.setBackgroundResource(R.drawable.stop_record_normal);
        if (isRecordSound && isAudioAvailable) {
            soundImageView.setBackgroundResource(R.drawable.sound_disabled);
        } else {
            soundImageView.setBackgroundResource(R.drawable.sound_close);
        }
        setSoundEnabled(false);
        startTimer();
        isRecordingNow.getAndSet(true);
        return State.OK;
    }

    private void notifyAtStatusbar(Uri uri) {
        //GIONEE 20160922 lixiaohong add for merge code begin
        mNotificationManager.cancel(mNotificationId);
        //chenyee zhaocaili 20180424 add begin
        mNotificationChannel = new NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
        mNotificationBuilder = new Notification.Builder(mContext, mChannelId);
        //chenyee zhaocaili 20180424 add end

        if (uri == null || isDurationZero) {
            mNotificationBuilder.setTicker(mResources.getString(com.cydroid.screenrecorder.R.string.record_saved_fail))
                    .setContentTitle(mResources.getString(R.string.record_saved_fail))
                    .setContentText(mResources.getString(R.string.record_explain))
                    .setSmallIcon(R.drawable.icon)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true);
        } else {
            Intent launchIntent = new Intent(Intent.ACTION_VIEW);
            //GIONEE 20161221 lixiaohong modify for #51629 begin
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                launchIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            //GIONEE 20161221 lixiaohong modify for #51629 end 
            Log.d(TAG, "notifyAtStatusbar() mUri = " + uri);
            launchIntent.setDataAndType(uri, "video/mp4");
            launchIntent.putExtra("fromNotification", true);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mNotificationBuilder.setTicker(mResources.getString(com.cydroid.screenrecorder.R.string.record_saved_success))
                    .setContentTitle(mResources.getString(R.string.record_saved_success))
                    .setContentText(mResources.getString(R.string.record_click_to_play))
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(PendingIntent.getActivity(mContext, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true);
        }
        //GIONEE 20160922 lixiaohong add for merge code end
        Notification n = mNotificationBuilder.build();
        //n.flags |= Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(mNotificationId, n);
    }

    private void stopTimer() {
        mWorkerHandler.removeCallbacks(mSecondTick);
        // no need
        // totalRecordTime = 0;
    }

    private void startTimer() {
        totalRecordTime = 0;
        mWorkerHandler.postAtTime(mSecondTick, SystemClock.uptimeMillis() / 1000 * 1000 + 1000);
    }

    private void updateRecordTime(int time) {
        if (isRecordingNow.get()) {
            //GIONEE 20160811 lixiaohong add for CR01744113 begin
            if (shouldForceStopScreenRecord) {
                Log.d(TAG, "updateRecordTime() have not enough space,force save recording video.");
                //GIONEE 20160829 lixiaohong add for CR01753528 begin
                Toast.makeText(mContext, mResources.getString(R.string.record_space_not_enough), Toast.LENGTH_SHORT).show();
                //GIONEE 20160829 lixiaohong add for CR01753528 end
                exitScreenRecorder(false);
            }
            //GIONEE 20160811 lixiaohong add for CR01744113 end
            timerTextView.setText(convertTime(time));
        }
    }

    private final Runnable mSecondTick = new Runnable() {
        @Override
        public void run() {
            shouldForceStopScreenRecord = (mCurrentType == 0) ? (getAvailableInternalMemorySize() <= AVAILABLE_SPACE_LIMIT) : (getVolumeSpace() <= AVAILABLE_SPACE_LIMIT);
            Log.d(TAG, "mSecondTick-run() mCurrentType = " + mCurrentType + " shouldForceStopScreenRecord = " + shouldForceStopScreenRecord);
            mHandler.post(() -> updateRecordTime(++totalRecordTime));
            mWorkerHandler.postAtTime(this, SystemClock.uptimeMillis() / 1000 * 1000 + 1000);
        }
    };

    /**
     * @param y not contain status bar height
     */
    public void updateViewsLocationY(int y) {
        mMainLayoutLocationY = y;
        if (isOrientationVerticalNow()) {
            mVerticalLastPositionY = mMainLayoutLocationY;
        } else {
            mHorizontalLastPositionY = mMainLayoutLocationY;
        }

        updateIndicationViewPositionY(mMainLayoutLocationY);
        updateMainLayoutPositionY(mMainLayoutLocationY);
    }

    private void hideMainLayoutAnimation() {
        indicationView.setVisibility(View.VISIBLE);
        mMainLayoutParams.x = mScreenWidth;
        mMainLayoutParams.y = mMainLayoutLocationY;
        isMyselfVisible.getAndSet(false);
        mMainContainer.setVisibility(View.INVISIBLE);

    }

    public void showMainLayoutAnimation() {
        if (!isMyselfVisible.get()) {
            if (isOrientationVerticalNow.get()) {
                mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5;
            } else {
                if (Constant.HAS_NAVIGATIONBAR) {
                    mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5;
                } else {
                    mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth / 5 * 2;
                }
            }
            //mMainLayoutParams.x = mScreenWidth - mMainLayoutTotalWidth - mIndicationWidth/5;
            //chenyee zhaocaili 20181017 add for CSW1805A-538 begin
            soundImageView.setBackgroundResource(isRecordSound ? R.drawable.sound_normal : R.drawable.sound_disabled);
            //chenyee zhaocaili 20181017 add for CSW1805A-538 end
            mMainLayoutParams.y = mMainLayoutLocationY;
            notifyWMUpdateViewLayout(mMainContainer, mMainLayoutParams);
            isMyselfVisible.getAndSet(true);
            indicationView.setVisibility(View.INVISIBLE);
            mMainContainer.setVisibility(View.VISIBLE);

        }
    }

    public static final class H extends Handler {
        public static final int MSG_CHECK_SCREEN_OFF_TIME = 0x01;
        public static final int MSG_DELAY_CLICK_STATE_LAYOUT = 0x02;
        public static final int MSG_AUDIO_CONFLICT = 0x03;
        public static final int MSG_MEDIARECORDER_PREPARE_OK = 0x04;
        public static final int MSG_STOP_SERVICE = 0X05;
        public static final int MSG_MEDIARECORDER_PREPARE_FAILED = 0x06;

        private final WeakReference<ScreenRecorderService> mServiceWeakReference;

        public H(ScreenRecorderService service) {
            mServiceWeakReference = new WeakReference<ScreenRecorderService>(service);
        }

        public void handleMessage(android.os.Message msg) {
            ScreenRecorderService mService = mServiceWeakReference.get();
            if (mService == null) return;
            switch (msg.what) {
                case MSG_CHECK_SCREEN_OFF_TIME:
                    mService.handleMsgCheckScreenOffTime();
                    break;
                case MSG_DELAY_CLICK_STATE_LAYOUT:
                    mService.isDoingStateClickEvent = false;
                    break;
                case MSG_AUDIO_CONFLICT:
                    mService.handleMsgAudioConflict();
                    break;
                case MSG_MEDIARECORDER_PREPARE_OK:
                    mService.handleMsgPrepareOK();
                    break;
                case MSG_STOP_SERVICE:
                    mService.handleStopService((Boolean) msg.obj);
                    break;
                case MSG_MEDIARECORDER_PREPARE_FAILED:
                    mService.handleMsgPrepareFailed();
                    break;
                default:
                    break;
            }
        }
    }

    public void handleMsgCheckScreenOffTime() {
        if (System.currentTimeMillis() - mLastScreenOffTime >= AUTO_EXIT_SCREEN_OFF_TIME_OUT) {
            exitScreenRecorder(false);
        }
    }

    public void handleMsgAudioConflict() {
        isRecordSound = false;
        soundImageView.setBackgroundResource(R.drawable.sound_close);
        setSoundEnabled(false);
        Toast.makeText(mContext, mResources.getString(R.string.audio_input_not_available), Toast.LENGTH_SHORT).show();
    }

    public enum State {
        OK, REMOTE_EXCEPTION, ILLEGAL_STATE_EXCEPTION, SECURITY_EXCEPTION
    }

    public void handleMsgPrepareOK() {
        if (!isRecordingNow.get() && !isPhoneInUse() && !isServiceKilled) {
            boolean shouldShowToastInfo = true;
            String msg = null;
            switch (startRecordScreen()) {
                case REMOTE_EXCEPTION:
                    msg = mResources.getString(R.string.tip_remote_exception);
                    break;
                case ILLEGAL_STATE_EXCEPTION:
                    msg = mResources.getString(R.string.audio_input_not_available/*tip_illegalstate_exception*/);//MAYBE AUDIO CONFLICT
                    break;
                case SECURITY_EXCEPTION:
                    msg = mResources.getString(R.string.tip_security_exception);
                    break;
                case OK:
                    shouldShowToastInfo = false;
                    try {
                        if (Constant.HAS_NAVIGATIONBAR) {
                            mIndicationViewLocationX = mIndicationViewLocationX + mNavigationBarHeightOverall;
                            resetIndicationView();
                        }
                        hideMainLayoutAnimation();
                        mMainLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        notifyWMUpdateViewLayout(mMainContainer, mMainLayoutParams);
                        hideLayoutFrameLayout.setVisibility(View.VISIBLE);
                        hideColorImageView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            if (shouldShowToastInfo) {
                forceStop(msg);
            }
        }
    }

    public void handleMsgPrepareFailed() {
        forceStop(mContext.getResources().getString(R.string.initial_failed));
    }

    public void forceStop(String reasonTip) {
        Toast.makeText(mContext, reasonTip, Toast.LENGTH_LONG).show();
        mHandler.removeCallbacks(mTimeOutTask);
        mHandler.postDelayed(() -> exitScreenRecorder(true), 800);
    }

    private CharSequence convertTime(int timeInSecond) {
        if (timeInSecond <= 0) return "00:00:00";
        return getHour(timeInSecond) + ":" + getMinute(timeInSecond) + ":" + getSecond(timeInSecond);
    }

    private String getHour(int timeInSecond) {
        int hour = totalRecordTime / (60 * 60);
        if (hour < 10) {
            return "0" + String.valueOf(hour);
        }
        return String.valueOf(hour);
    }

    private String getMinute(int timeInSecond) {
        int minute = totalRecordTime % (60 * 60) / 60;
        if (minute < 10) {
            return "0" + String.valueOf(minute);
        }
        return String.valueOf(minute);
    }

    private String getSecond(int timeInSecond) {
        int second = totalRecordTime % (60 * 60) % 60;
        if (second < 10) {
            return "0" + String.valueOf(second);
        }
        return String.valueOf(second);
    }

    public void updateMainLayoutPositionY(int y) {
        mMainLayoutParams.y = y;
        notifyWMUpdateViewLayout(mMainContainer, mMainLayoutParams);
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mMediaProjection = null;
        }
    }

    /**
     * kill self
     *
     * @param isForceStopSelf true  force stop self; false normally stop self.
     */
    public void exitScreenRecorder(boolean isForceStopSelf) {
        restoreDefaultValues();
        mStopScreenRecord.isForceStopService(isForceStopSelf);
        mWorkerHandler.post(mStopScreenRecord);
    }

    public void handleStopService(final boolean isForceStopSelf) {
        Log.d(TAG, "handleStopService: isForceStopSelf=" + isForceStopSelf);
        try {
            if (indicationView != null) {
                mWindowManager.removeView(indicationView);
                indicationView = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "handleStopService: remove indiacationView occur exception." + e.getMessage());
        }
        try {
            if (mMainContainer != null) {
                mWindowManager.removeView(mMainContainer);
                mMainContainer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "handleStopService: remove mMainContainer occur exception."  + e.getMessage());
        }
        //GIONEE 20170410 lixiaohong add for #102737 begin
        stopForeground();
        if (isRegisted) {
            unregisterReceiver(mReceiver);
            mHandlerThread.quit();
            mHandlerThread = null;
            if (mScreenRecorderObserver != null) {
                mScreenRecorderObserver.destroy();
            }
            isRegisted = false;
        }
        //GIONEE 20170410 lixiaohong add for #102737 end
        // wait media scan to complete
        mHandler.postDelayed(() -> {
            if (isForceStopSelf) {
                forceStopSelf();
            } else {
                stopSelf();
            }
        }, 1000);
    }


    private final class StopScreenRecord implements Runnable {
        private boolean isForceStopService;

        public void isForceStopService(boolean isForceStopService) {
            this.isForceStopService = isForceStopService;
        }

        @Override
        public void run() {
            if (isRecordingNow.get()) {
                boolean shoudSendSuccessNotification = true;
                if (mVirtualDisplay == null) {
                    isVirtualDisplayNull = true;
                    notifyAtStatusbar(null);
                    isRecordingNow.getAndSet(false);
                }
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                } catch (Exception exception) {
                    Log.e(TAG, "exception in mMediaRecorder.stop(): ", exception);
                    shoudSendSuccessNotification = false;
                    File tempFile = new File(mVideoFilePath);
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    notifyAtStatusbar(null);
                }
                stopTimer();
                judgeDurationTime(mVideoFilePath);
                try {
                    mMediaProjection.stop();
                    mVirtualDisplay.release();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (isAudioFocused) {
                    mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
                }
                mMediaRecorder = null;
                if (shoudSendSuccessNotification) {
                    updateVideoUri();
                }
                isRecordingNow.getAndSet(false);
            }
            mHandler.obtainMessage(H.MSG_STOP_SERVICE, isForceStopService).sendToTarget();
        }
    }

    /**
     * force stop self .
     */
    private void forceStopSelf() {
        try {
            forceStopPackageMethod.invoke(mActivityManager, packageName);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void restoreDefaultValues() {
        Log.d(TAG, "restoreDefaultValues() mDefaultTouchesOptionsValue = " + mDefaultTouchesOptionsValue);
        MTool.setTouchesOptionsValue(mContext, mDefaultTouchesOptionsValue);
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    //see manifest provider android:authorities
    private static final long AVAILABLE_SPACE_LIMIT = 50 * 1024 * 1024;//50M

    private void stopRecording() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
    }

    public String getVideoPathFromSDcard() {
        String videoPathDir = StorageHelper.getSDcardRecordVideoDir(getApplicationContext());
        if (videoPathDir != null && !StorageHelper.isInternalPath(videoPathDir)) {
            //GIONE  20161217 lixiaohong modify for #46896 begin
            File dir = new File(videoPathDir + File.separator + (Constant.CUSTOMER_INDIA ? SCREEN_CAPTURE_DIR_NAME : "Movies/Screen Recorder"));
            //GIONE  20161217 lixiaohong modify for #46896 end
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mVideoTime = System.currentTimeMillis();
            String mVideoDate = new SimpleDateFormat(mDateFormat).format(new Date(mVideoTime));
            mVideoFileName = String.format(SCREEN_CAPTURE_FILE_NAME_TEMPLATE, mVideoDate);
            mVideoFilePath = new File(dir, mVideoFileName).getAbsolutePath();
            this.videoPathDir = videoPathDir;
        }
        return mVideoFilePath;
    }

    public long getVolumeSpace() {
        return StorageHelper.getVolumeSpace(getApplicationContext(), this.videoPathDir);
    }

    public boolean isSDcardSpaceEnough() {
        String videoPathDir = StorageHelper.getSDcardRecordVideoDir(getApplicationContext());
        if (videoPathDir != null && !StorageHelper.isInternalPath(videoPathDir)) {
            this.videoPathDir = videoPathDir;
            long volumeSpace = StorageHelper.getVolumeSpace(getApplicationContext(), videoPathDir);
            Log.d(TAG, "isSDcardSpaceEnough: space left = " + volumeSpace/1024/1024 + " M");
            return volumeSpace >= LEAST_SPACE_NEED;
        }
        return false;
    }

    private final Runnable mRecordStateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!getRecordState()) {
                mHandler.sendEmptyMessage(H.MSG_AUDIO_CONFLICT);
            }
        }
    };

    private boolean isPhoneInUse() {
        boolean isPhoneInUse = false;
        TelecomManager tm = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            isPhoneInUse = tm.isInCall();
        }
        return isPhoneInUse;
    }

    public boolean isMediatekPlatform() {
        String platform = SystemProperties.get("ro.mediatek.platform");
        Log.d(TAG, "isMediatekPlatform() platform = " + platform);
        return platform != null && (platform.startsWith("MT") || platform.startsWith("mt"));
    }

    private void checkAudioAvailableWhenClick() {
        if (isMediatekPlatform()) {
            isAudioAvailable = true;
        } else {
            if (isRecordSound) {
                isAudioAvailable = getRecordState();
            }
        }
    }

    public boolean getRecordState() {
        int minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, (minBuffer * 100));
        short[] point = new short[minBuffer];
        int readSize = 0;
        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        } else {
            readSize = audioRecord.read(point, 0, point.length);
            if (readSize <= 0) {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                return false;
            } else {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                return true;
            }
        }
    }

    private void releaseMediaResource() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "releaseMediaResource() release mMediaRecorder occur exception." + e.getMessage());
        }
        try {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "releaseMediaResource() release mMediaProjection mVirtualDisplay occur exception." + e.getMessage());
        }
        mMediaRecorder = null;
    }

    private void judgeDurationTime(final String mUri) {
        if (totalRecordTime < 1800) {
            String duration = "";
            android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
            try {
                mmr.setDataSource(mUri);
                duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            } catch (Exception e) {
                Log.e(TAG, "judgeDurationTime() MediaMetadataRetriever exception -> ", e);
                //chenyee zhaocaili 20180514 add for CSW1703CX-551 begin
                if (isQuotaExceeded()){
                    Toast.makeText(mContext, mResources.getString(R.string.record_space_not_enough), Toast.LENGTH_SHORT).show();
                }
                //chenyee zhaocaili 20180514 add for CSW1703CX-551 end
            } finally {
                mmr.release();
            }
            //chenyee zhaocaili 20180523 modify for CSW1707A-1163 begin
            if (duration == null){
                Log.d(TAG, "screen recorder duration is null");
                isDurationZero = true;
                return;
            }else {
                isDurationZero = duration.equals("0") ? true : false;
            }
            //chenyee zhaocaili 20180523 modify for CSW1707A-1163 end
        }
        // Chenyee xionghg 20180314 add for CSW1705A-2133 begin
        // 增加文件大小判断
        File f = new File(mUri);
        if (!f.exists()) {
            isDurationZero = true;
            return;
        }

        long length = f.length();
        Log.d(TAG, "judgeDurationTime: file size = " + length / 1024 / 1024.0 + " MB");
        if (length == 0) {
            isDurationZero = true;
        }
        // Chenyee xionghg 20180314 add for CSW1705A-2133 end
    }

    //GIONEE 20170509 lixiaohong add for #130735 begin
    public void stopScreenRecordIfNeed() {
        if (!isMediatekPlatform()) {
            exitScreenRecorder(false);
        }
    }

    //GIONEE 20170509 lixiaohong add for #130735 end
    private int getNavigationBarHeight() {
        int navigationId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (navigationId > 0) {
            return getResources().getDimensionPixelSize(navigationId);
        }
        return 0;
    }

    public void ensureHandlerExists() {
        mHandlerThread = new HandlerThread("ScreenRecorder#", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mWorkerHandler = new Handler(mHandlerThread.getLooper());
        if (!isMediatekPlatform()) {
            mWorkerHandler.post(mRecordStateRunnable);
        }
    }

    private int getLowPowerModelState() {
        return Settings.Global.getInt(mContext.getContentResolver(), Constant.POWER_MODE_SETTING, Constant.POWER_MODE_NORMAL);
    }

    private final class ScreenRecorderObserver extends ContentObserver {
        private final Uri POWERMODE_URI = Global.getUriFor(Constant.POWER_MODE_SETTING);
        private final Uri SHOWTOUCHES_URI = Settings.System.getUriFor(Settings.System.SHOW_TOUCHES);

        public ScreenRecorderObserver(Handler handler) {
            super(handler);
        }

        public void init() {
            mContext.getContentResolver().registerContentObserver(POWERMODE_URI, false, this);
            mContext.getContentResolver().registerContentObserver(SHOWTOUCHES_URI, false, this);
        }

        public void destroy() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (POWERMODE_URI.equals(uri)) {
                int afterChangeState = getLowPowerModelState();
                Log.d(TAG, "ScreenRecorderObserver after change lowPowerModeState =" + afterChangeState);
                if (initLowPowerModeState != afterChangeState && (initLowPowerModeState == Constant.POWER_MODE_EXTREME || afterChangeState == Constant.POWER_MODE_EXTREME)) {
                    Log.d(TAG, "ScreenRecorderObserver changed low power mode state   exit screenrecorder");
                    exitScreenRecorder(false);
                }
            } else if (SHOWTOUCHES_URI.equals(uri)) {
                mNowTouchesOptionsValue = Settings.System.getInt(getContentResolver(), Settings.System.SHOW_TOUCHES, 0) == 1 ? true : false;
                //GIONEE 20170704 lixiaohong add for #165394 begin
                Log.d(TAG, "ScreenRecorderObserver-onChange(boolean,Uri) changeFromScreenRecorder = " + changeFromScreenRecorder);
                if (!changeFromScreenRecorder) {
                    mDefaultTouchesOptionsValue = mNowTouchesOptionsValue;
                }
                changeFromScreenRecorder = false;
                //GIONEE 20170704 lixiaohong add for #165394 end
                mHandler.post(() -> gestureImageView.setBackgroundResource(mNowTouchesOptionsValue ? R.drawable.gesture_normal : R.drawable.gesture_disabled));
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mLastScreenOffTime = System.currentTimeMillis();
                mHandler.removeMessages(H.MSG_CHECK_SCREEN_OFF_TIME);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(H.MSG_CHECK_SCREEN_OFF_TIME), AUTO_EXIT_SCREEN_OFF_TIME_OUT);
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mHandler.removeMessages(H.MSG_CHECK_SCREEN_OFF_TIME);
            } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                exitScreenRecorder(false);
            } else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                stopScreenRecordIfNeed();
            //chenyee zhaocaili 20180511 modify for CSW1707A-974 begin
            } else if("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED".equals(action)){
                Log.setLogEnableOrNot();
            //chenyee zhaocaili 20180511 modify for CSW1707A-974 end
            } else {
                stopScreenRecordIfNeed();
            }
        }
    };

    private final TimeOutRunnable mTimeOutTask = new TimeOutRunnable();

    class TimeOutRunnable implements Runnable {
        private String reasonTip;

        public void setReason(String reasonTip) {
            this.reasonTip = reasonTip;
        }

        @Override
        public void run() {
            forceStop(reasonTip);
        }
    }

    //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
    private void initScreenRecorderSaveDir(){
        if ("MEXICO_LANIX".equals(SystemProperties.get("ro.cy.custom"))){
            SCREEN_CAPTURE_FILE_NAME_TEMPLATE = "Grabador de pantalla_%s.mp4";
            SCREEN_CAPTURE_DIR_NAME = "Grabador de pantalla";
        }else {
            SCREEN_CAPTURE_FILE_NAME_TEMPLATE = "ScreenRecorder_%s.mp4";
            SCREEN_CAPTURE_DIR_NAME = "Screenrecorder";
        }
    }
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 end

    //chenyee zhaocaili 20180514 add for CSW1703CX-551 begin
    private boolean isQuotaExceeded(){
        if (mCurrentType == TYPE_INTERNAL){
            String path = StorageHelper.getInternalRootDir(mContext) + "/QuotaExceeded" + Math.random();
            File file = new File(path);
            boolean result = false; // 文件是否创建成功
            try {
                result = file.createNewFile();
                Log.d(TAG, "create file = " + result + "    path = " + path);
            } catch (IOException e) {
                Log.e(TAG, "mkdirs exception = " + e.getMessage());
                if (e.getMessage().contains("Quota exceeded")){
                    return true;
                }
            }

            if (result){
                try {
                    FileOutputStream fos = new FileOutputStream(file);

                    String data = "hello, I am just a test string";
                    byte[] buffer = data.getBytes();

                    fos.write(buffer, 0, buffer.length);
                    fos.flush();
                    fos.close();
                    Log.d(TAG, "readSaveFile: \n");
                } catch (Exception e) {
                    Log.e(TAG, "write file exception = " + e.getMessage());
                    if (e.getMessage().contains("Quota exceeded")){
                        return true;
                    }
                }finally {
                    file.delete();
                }
            }

        }
        return false;
    }
    //chenyee zhaocaili 20180514 add for CSW1703CX-551 end
}
