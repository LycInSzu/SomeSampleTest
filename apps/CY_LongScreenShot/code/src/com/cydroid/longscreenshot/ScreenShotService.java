package com.cydroid.longscreenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.myviews.LargeImageView;
import com.cydroid.util.BallPosition;
import com.cydroid.util.BitmapCompare;
import com.cydroid.util.FloatingTouchHelper;
import com.cydroid.util.StorageManagerHelper;
import com.cydroid.util.Log;
import com.cydroid.util.ReflectUtil;
import com.cydroid.util.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;


public class ScreenShotService extends Service {
    public static final String TAG = "ScreenShotService";
    private int SCROLL_SCREEN_START_Y = 0;//depends on status bar's height
    private int SCROLL_SCREEN_END_Y = 0;
    private int SCROLL_SCREEN_STEP = 10;

    private Context mContext;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mLayoutInflater = null;
    private NotificationManager mNotificationManager = null;
    private int mNotificationId = 1000;
    private int mNotificationId_runForeground = 1001;
    private Notification.Builder mNotificationBuilder;
    //chenyee zhaocaili 20180424 add begin
    private NotificationChannel mNotificationChannel;
    private String mChannelId = "ScreenShotService_ChannelId";
    private String mChannelName = "ScreenShotService";
    //chenyee zhaocaili 20180424 add end

    private Bitmap lastStableBitmap = null;
    private List<Bitmap> mScreencapBitmapCache = new ArrayList<Bitmap>();// 所有截屏bitmap 缓存
    private int mCachedScreenShotPicNum = 0;
    // Chenyee xionghg 20180111 add for CSW1702A-2364 begin
    private FloatingTouchHelper mFloatingTouchHelper;
    // Chenyee xionghg 20180111 add for CSW1702A-2364 end
    private int mNavigationBarHeight;

    //滚动过程中点击屏幕的标志位
    private boolean isWindowTouched = false;

    /**
     * 用来存储每一张缓存的图的顶部应该截掉的位置，从屏幕顶部开始计算。
     * 每一张图底部默认截图的位置为SCROLL_SCREEN_START_Y ，
     * 特殊：第一张图顶部位置是根据顶部小球位置，最后一张图的底部位置
     * 还要考虑底部小球的位置。如果底部小球被拖动的距离等于最后一次滚
     * 屏实际的滚动距离，既最后一次滚动增加的图片片段应该被砍掉，
     * 如果底部小球被拖动的距离大于最后一次滚屏实际的滚动距离，不仅要
     * 砍掉最后一张图，而且要砍掉倒数第二张图的底部部分区域
     */
    private List<Integer> overlayPosition = new ArrayList<Integer>();
    private final ArrayList<String> mLongScreenshotNotSupportItems = new ArrayList<String>();
    // save start
    private long mImageTime;
    private boolean mSaveImageToFileSystem;
    private Uri imageUri;
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
    //private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    private static String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 end
    private String mImageFileName;
    private File mScreenshotDir;
    private String mImageFilePath;
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
    //private static final String SCREENSHOTS_DIR_NAME = "Pictures/Screenshots";
    private static String SCREENSHOTS_DIR_NAME = "Pictures/Screenshots";
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 end
    private String mDateFormat = "yyyyMMddHHmmss";

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mStatusBarHeight = 0;

    private boolean isStoppingMySelf;//目前是否正在杀死自己
    //    private int ALPHA_MSG = 1;
    private Bitmap mTempBitmap = null;//每次下滑后的截图
    private Bitmap mTempPreBitmap = null;//每次下滑的前一张图片

    private int picCacheNumIndex = 0;
    private String beforeCutString = "beforeCut";
    private boolean isFirstPic = true;
    private int finalScreenShotPicHeight = 0;// 计算总的长截屏bitmap 高度
    private int bottomBallPosition = 0;//init by mScreenHeight，记录最后一张图片的底部的截取位置
    private int topBallPosition = 0;//init by mStatusBar的高度
    private Instrumentation instrumentation = new Instrumentation();
    private int specialPostion = 0;//特殊情况，记录倒数第二张图片底部的位置。如果始终为零，则删除倒数第一张图片的操作没有进行
    private int specialHeight = 0;//特殊情况时，最后一张（原来的倒数第二张）图片应该截取的高度
    private boolean isAlreadyFirstClickedNextScreen = false;//是否是第一次点击下一屏
    private int mDefaultScreenOritation = -1;
    private View mFullScreenCheckViewLeft, mFullScreenCheckViewRight;//监听是否全屏的view
    private boolean mIsFullScreen = false;//
    private boolean isScrolledToBottom = false;//解决toast提示滚动到底部后，立即截图导致toast也在截图上的问题
    boolean isSavingPicture = false;
    boolean isAlreadyTenPagesSavedToCache = false;
    private boolean doNotSavePicture = false;
    private SoundPool mSoundPool;
    private static final int NUM_MEDIA_SOUND_STREAMS = 1;
    private int playSoundId = -1;

    private View mNotificationView;
    private TextView mNotiText;
    private boolean isNeedAnimation = true;

    //chenyee zhaocaili 20180728 add for CSW1802A-484 begin
    private boolean isDeleting = false;
    //chenyee zhaocaili 20180728 add for CSW1802A-484 end

    /**
     * 8这个数字是调试测试出来的，单位为dp，具体使用的时候要转换为px,也就是说8dp是在QQ聊天界面不管手机屏幕大小，
     * 截图的图片最终重叠的不会太多
     * 2015-11-22 修改为正对不同的分辨率赋值不同，且单位变为pix而不是dp
     */
    private int default_pos_offset = 0; //当找不到精确的拼接位置时，返回的默认偏移位置

    /**
     * 滚动后的惯性缓冲,7313设置界面极限是580毫秒；
     * 考虑qq聊天界面，会把键盘给拉出来；
     * 拨号盘列表要1300毫秒
     * S10上卡顿时最后一屏截屏重叠1300-->1500-->1600
     */
    //chenyee zhaocaili 20180830 modify for CSW1802A-1437 begin
    private int letScreenFinishScroll = 1000;
    //chenyee zhaocaili 20180830 modify for CSW1802A-1437 end
    private String mSaveImageDir;
    private boolean mHadRegisterReceivers = false;
    private Handler mScrollHandler;
    private final H mHandler = new H();
    private HandlerThread mHandlerThread;
    //chenyee zhaocaili 20180503 add for CSW1703A-2263 begin
    private boolean isNavigationBarShow = false;
    //chenyee zhaocaili 20180503 add for CSW1703A-2263 end
    //chenyee zhaocaili 20180521 add for SWW1618OTA-127 begin
    private String mTopClass;
    //chenyee zhaocaili 20180521 add for SWW1618OTA-127 end


    private View mScreenshotLayout;
    private View mScreenshotContainer;
    private ImageView mBackgroundView;
    private LargeImageView mScreenshotView;
    private ImageView mScreenshotFlash;

    //private View mBottomFunctionView;
    private TextView mShareBtn;
    private TextView mEditBtn;
    private TextView mDeleteBtn;

    private AnimatorSet mScreenshotAnimation;
    private float mBgPadding;
    private float mBgPaddingScale;
    private Bitmap mScreenBitmap;

    private ValueAnimator mScreenshotDropInAnim;
    private ValueAnimator mScreenshotFadeOutAnim;
    private boolean isFadeOutAnimRunning;

    private static final int SCREENSHOT_FLASH_TO_PEAK_DURATION = 130;
    private static final int SCREENSHOT_DROP_IN_DURATION = 430;
    private static final int SCREENSHOT_DROP_OUT_DELAY = 3000;
    private static final int SCREENSHOT_DROP_OUT_DURATION = 430;
    private static final int SCREENSHOT_DROP_OUT_SCALE_DURATION = 370;
    private static final int SCREENSHOT_FAST_DROP_OUT_DURATION = 320;
    private static final float BACKGROUND_ALPHA = 0.5f;
    private static final float SCREENSHOT_SCALE = 1f;
    private static final float SCREENSHOT_DROP_IN_MIN_SCALE = SCREENSHOT_SCALE * 0.725f;
    private static final float SCREENSHOT_DROP_OUT_MIN_SCALE = SCREENSHOT_SCALE * 0.45f;
    private static final float SCREENSHOT_FAST_DROP_OUT_MIN_SCALE = SCREENSHOT_SCALE * 0.6f;
    private static final float SCREENSHOT_DROP_OUT_MIN_SCALE_OFFSET = 0f;

    @Override
    public void onCreate() {
        //chenyee zhaocaili 20180511 add for CSW1707A-975 begin
        Log.i(TAG, "onCreate() start. setLog" + Log.setLogEnableOrNot());
        //chenyee zhaocaili 20180511 add for CSW1707A-975 end
        //chenyee zhaocaili 20181027 add for CSW1802A-1593 begin
        ChameleonColorManager.getInstance().register(this);
        //chenyee zhaocaili 20181027 add for CSW1802A-1593 end
        mContext = getApplicationContext();
        sendLSStartBroadcast();
        initSystemService();
        ensureHandlerExists();
        //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
        initPicSaveDir();
        //chenyee zhaocaili 20180421 add for CSW1703MX-70 end
    }

    private void registReceivers() {
        if (!mHadRegisterReceivers) {
            MyReceiver.init(this);
            registerReceiver(MyReceiver.mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)); // 监听home键
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(MyReceiver.CHENYEE_ACTION_SINGLEHAND);
            mIntentFilter.addAction(MyReceiver.CHENYEE_ACTION_ALARM);
            mIntentFilter.addAction(MyReceiver.CHENYEE_ACTION_PHONESTATE);
            registerReceiver(MyReceiver.mReceiver, mIntentFilter);
            mHadRegisterReceivers = true;
        }
    }

    private void printPhoneInfo() {
        try {
            Log.d(TAG,
                    "\n-----print info begin-----"
                    + "\n    Build.MODEL = " + android.os.Build.MODEL
                    + "\n    versionName = " + getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName
                    + "\n    mScreenWidth = " + mScreenWidth
                    + "\n    mScreenHeight = " + mScreenHeight
                    + "\n    mStatusBarHeight = " + mStatusBarHeight
                    + "\n    SCROLL_SCREEN_START_Y = " + SCROLL_SCREEN_START_Y
                    + "\n    SCROLL_SCREEN_END_Y = " + SCROLL_SCREEN_END_Y
                    + "\n    SCROLL_SCREEN_STEP = " + SCROLL_SCREEN_STEP
                    + "\n    default_pos_offset = " + default_pos_offset
                    + "\n-----print info end-----"
                    );
        } catch (Exception e) {
            Log.e(TAG, "printPhoneInfo: " + e);
        }
    }

    /**
     * 取消设置服务为前台服务
     */
    private void myStopForeground() {
        Log.d(TAG, "stop self, releaseSound and stopForeground");
        sendLSEndBroadcast();
        unRegisterReceiver();
        //CHENYEE 20170516 lixiaohong add for #141427 begin
        releaseSound();
        //CHENYEE 20170516 lixiaohong add for #141427 end
        // Chenyee xionghg 20180111 add for CSW1702A-2364 begin
        mFloatingTouchHelper.restoreFloatingTouchIfNeeded();
        // Chenyee xionghg 20180111 add for CSW1702A-2364 end
        mLongScreenshotNotSupportItems.clear();
        mScrollHandler.removeCallbacksAndMessages(null);
        mHandlerThread.quit();
        mHandlerThread = null;
        stopForeground(true);
    }

    private boolean isLongScreenshotNotSupport() {
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> mRunningTaskInfos = mActivityManager.getRunningTasks(1);
        String className;
        for (RunningTaskInfo runningTaskInfo : mRunningTaskInfos) {
            ComponentName mComponentName = runningTaskInfo.topActivity;
            className = mComponentName.getClassName();
            //chenyee zhaocaili 20180521 add for SWW1618OTA-127 begin
            mTopClass = mComponentName.getPackageName();
            //chenyee zhaocaili 20180521 add for SWW1618OTA-127 end
            Log.i(TAG, "top activity is: " + mComponentName.toShortString());
            for (String item : mLongScreenshotNotSupportItems) {
                if (className.equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否已经开启了单手模式
     * @return
     */
    private boolean isInSingleHandMode() {
        return !SystemProperties.get("persist.sys.SHandRatioF", "1.0").equals("1.0");
    }

    private void myToast(int textId) {
        Log.v(TAG, "  toast");
        if (mContext == null) {
            Log.v(TAG, " myToast  mContext === null");
        }
        Toast.makeText(mContext, textId, Toast.LENGTH_SHORT).show();
    }

    public boolean isScreenOrientationHorizontal() {
        //限定长截屏只能在竖屏的时候使用
        Display mDisplay = mWindowManager.getDefaultDisplay();
        int orientation = mDisplay.getRotation();
        return orientation != 0;
    }

    private void isFullScreen(boolean isFull) {
        Log.d(TAG, String.format("isFullScreen=%b, isSavingPicture=%b", isFull, isSavingPicture));
        if (isSavingPicture) {
            return;
        }
        if (isFull) {
            myToast(R.string.long_screen_shot_not_surport_full_screen);
            stopSelfNormally();
        }
    }

    private void addPicToCache(Bitmap bitmap) {
        picCacheNumIndex++;
        Log.d(TAG, "add cached pic, cached number=" + picCacheNumIndex);
        //savePic(bitmap, afterCutString);
        mScreencapBitmapCache.add(bitmap);
    }

    private void savePic(Bitmap bitmap, String tag) {
        if (Tools.DEBUG_SAVE_TEMP_BITMAP) {
            try {
                FileOutputStream out = new FileOutputStream("/mnt/sdcard/" + "Pictures/" + tag + ".png");// "/mnt/sdcard/"
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 begin

    /**
     * method for 3D-touch
     */
    private void scrollScreen() {
        Log.d(TAG, "LongScreenShot-scrollScreen() start");
        int scrollX = mScreenWidth / 4;
        int scrollTime = 3;//毫秒
        int moveTimes = 0;
        //chenyee zhaocaili 20180428 add for CSW1703CX-197 begin
        try {
            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, scrollX, SCROLL_SCREEN_START_Y, 0));// OK
            Log.v(TAG, "scroll screen    action_down");
            for (int i = SCROLL_SCREEN_START_Y; i >= SCROLL_SCREEN_END_Y + SCROLL_SCREEN_STEP; i -= SCROLL_SCREEN_STEP) {
                // Log.v(TAG, "scroll screen    action_move  times=" + (++moveTimes) + "  Y_position==" + i);
                /**如果因为突发事件停止滚动，需要发送ACTION_CANCEL事件，否则会响应屏幕长按操作，也不能发送ACTION_UP事件，否则会响应屏幕点击事件**/
                if (isStoppingMySelf){
                    instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, scrollX, SCROLL_SCREEN_END_Y, 0));
                    return;
                }
                if (!isWindowTouched){
                    instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + scrollTime, MotionEvent.ACTION_MOVE, scrollX, i, 0));
                }else {
                    //chenyee zhaocaili 20180814 modify for CSW1802A-1108 begin
                    instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, scrollX, SCROLL_SCREEN_END_Y, 0));
                    //chenyee zhaocaili 20180814 modify for CSW1802A-1108 end
                    sendMyMessage(mHandler, H.MSG_CAPTURE_SCREEN);
                    return;
                }

            }
            //Gionee <gn_by_LongScreenShot> <liuweiming> <20170516> add for #84452 begin
            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + scrollTime, MotionEvent.ACTION_MOVE, scrollX, SCROLL_SCREEN_END_Y + 5, 0));
            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + scrollTime, MotionEvent.ACTION_MOVE, scrollX, SCROLL_SCREEN_END_Y + 2, 0));
            //Gionee <gn_by_LongScreenShot> <liuweiming> <20170516> add for #84452 begin
            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + scrollTime, MotionEvent.ACTION_MOVE, scrollX, SCROLL_SCREEN_END_Y, 0));
            Log.v(TAG, "scroll screen    action_move  times=" + (++moveTimes) + "  Y_position==" + SCROLL_SCREEN_END_Y);
            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, scrollX, SCROLL_SCREEN_END_Y, 0));
            Log.v(TAG, "scroll screen    action_up  1");
            Log.d(TAG, "LongScreenShot-scrollScreen() end");
            sendMyMessage(mHandler, H.MSG_CAPTURE_SCREEN);
        }catch (Exception e){
            Log.d(TAG, "scrollScreen exception " + e);
            isWindowTouched = true;
            sendMyMessage(mHandler, H.MSG_CAPTURE_SCREEN);
        }
        //chenyee zhaocaili 20180428 add for CSW1703CX-197 end
    }
    //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 end

    private void addCachedScreenShotPicNum() {
        // Log.v(TAG, "addScreenShotPicNum    before add  num==" + mCachedScreenShotPicNum);
        mCachedScreenShotPicNum++;// 截屏数目+1
        Log.v(TAG, "addScreenShotPicNum, after add  num=" + mCachedScreenShotPicNum);
    }

    private void prepareNextScreen() {
        setFirstBitmapPosition();
        //截屏之后延迟200ms以可以及时隐藏输入法等
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendMyMessage(mHandler, H.MSG_ADD_NOTIFICATION);
    }

    private void setSavePictureFlag(boolean saving) {
        Log.v(TAG, "prepareSavePicture  isSavingPicture=" + isSavingPicture);
        isSavingPicture = saving;
    }

    private class SavePictureTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "SavePictureThread#run() start");
            if (doNotSavePicture) {
                return null;
            }

            if (!isAlreadyTenPagesSavedToCache) {//缓存的图片还没有到10张
                Log.v(TAG, "-----isAlreadyTenPagesSavedToCache == false");
                if (!isScrolledToBottom) {//没有滚动到底部， 点击保存之后，才去截取最后一屏,
                    Log.v(TAG, "-----isScrolledToBottom == false");
                    lastStableBitmap = captureScreen();
                    if (lastStableBitmap == null) {
                        Log.e(TAG, " 点击保存  没有截图成功");
                        mHandler.removeMessages(H.MSG_FORBID_SCREEN_SHOT);
                        mHandler.obtainMessage(H.MSG_FORBID_SCREEN_SHOT, ScreenShotService.this).sendToTarget();
                        return null;
                    }
                } else {//未满10张，但已经滚动到底,那么最后一屏，就是缓存的最后一张
                    Log.d(TAG, "-----isScrolledToBottom == true");
                    lastStableBitmap = mScreencapBitmapCache.get(mScreencapBitmapCache.size() - 1);
                }
            }

            if (BallPosition.getmSmallBallPossion() != mScreenHeight && mScreencapBitmapCache.size() >= 2){ //不只有第一屏，小球调整过高度，记录最后一张图片的底部位置
                Log.v(TAG, "-save-button---------BallPossion.getmSmallBallPossion() != mScreenHeight  =" + BallPosition.getmSmallBallPossion());
                if (BallPosition.getmSmallBallPossion() < mScreenHeight / 5 * 3) {
                    BallPosition.setmSmallBallPossion(mScreenHeight / 5 * 3);
                } else if (BallPosition.getmSmallBallPossion() > mScreenHeight) {
                    BallPosition.setmSmallBallPossion(mScreenHeight);
                }
                bottomBallPosition = BallPosition.getmSmallBallPossion();
                Log.v(TAG, "-save-button---------after adjust   BallPossion.getmSmallBallPossion() =" + BallPosition.getmSmallBallPossion());
                if (isAlreadyTenPagesSavedToCache) {//如果是已经保存10屏，生成最后一张图片
                    Log.v(TAG, "  如果是已经保存10屏，生成最后一张图片 ");
                    //CHENYEE 20170414 lixiaohong modify for #113179 begin
                    int mBitmapHeight = mScreencapBitmapCache.get(mScreencapBitmapCache.size() - 1).getHeight();
                    int mPositionY = 0;
                    int mHeight = bottomBallPosition;
                    Log.d(TAG, "SavePictureThread-run() mBitmapHeight = " + mBitmapHeight + " mPositionY = " + mPositionY + " mHeight = " + mHeight);
                    if (mPositionY + mHeight > mBitmapHeight) {
                        mHeight = mBitmapHeight - mPositionY;
                        Log.d(TAG, "SavePictureThread-run() new mHeight = " + mHeight);
                    }
                    lastStableBitmap = Bitmap.createBitmap(mScreencapBitmapCache.get(mScreencapBitmapCache.size() - 1), 0, 0, mScreenWidth, mHeight);
                    Log.v(TAG, "   如果是已经保存10屏，生成最后一张图片  lastStableBitmap.getHeight()====" + lastStableBitmap.getHeight());
                    //CHENYEE 20170414 lixiaohong modify for #113179 end
                }
                mScreencapBitmapCache.remove(mScreencapBitmapCache.size() - 1);//这样是为了避免Toast也被截图截到了
                mScreencapBitmapCache.add(lastStableBitmap);//

            } else if (BallPosition.getmSmallBallPossion() != mStatusBarHeight && mScreencapBitmapCache.size() == 0 && !isAlreadyFirstClickedNextScreen) {
                //是第一屏，没点击下一屏，调整过高度 ，记录第一张图片的上部位置
                if (BallPosition.getmSmallBallPossion() < 0) {
                    BallPosition.setmSmallBallPossion(0);
                } else if (BallPosition.getmSmallBallPossion() > mScreenHeight / 5 * 2) {
                    BallPosition.setmSmallBallPossion(mScreenHeight / 5 * 2);
                }
                topBallPosition = BallPosition.getmSmallBallPossion();
                Log.d(TAG, "clicke save :是第一屏，没点击下一屏，调整过高度 ，记录第一张图片的上部位置  topBallPosition===" + topBallPosition);
                lastStableBitmap = Bitmap.createBitmap(lastStableBitmap, 0, topBallPosition, mScreenWidth, mScreenHeight - topBallPosition);//截图图片用于保存
            } else if (mScreencapBitmapCache.size() == 1 && isAlreadyFirstClickedNextScreen) {//点击next之后，mScreencapBitmapCache.size = 1,之前=0
                //只有一张图，点击过下一屏，调整过底部小球的高度 ，记录第一张图片的底部位置
                if (BallPosition.getmSmallBallPossion() > mScreenHeight) {
                    BallPosition.setmSmallBallPossion(mScreenHeight);
                } else if (BallPosition.getmSmallBallPossion() < mScreenHeight / 5 * 3) {
                    BallPosition.setmSmallBallPossion(mScreenHeight / 5 * 3);
                }
                bottomBallPosition = BallPosition.getmSmallBallPossion();
                if (topBallPosition == mStatusBarHeight) {
                    Log.d(TAG, "是第一屏，点击过下一屏，没有调整过高度 ，记录第一张图片的上部位置  bottomBallPosition==" + BallPosition.getmSmallBallPossion() + "  topBallPosition==" + topBallPosition);
                    Log.v(TAG, "  topBallPosition  调整上部小球的位置为0");
                    topBallPosition = 0;//如果没有拖动过顶部的小球，则保存图片时也要保存状态栏
                } else {
                    if (topBallPosition > mScreenHeight / 5 * 2) {
                        topBallPosition = mScreenHeight / 5 * 2;
                    }
                    Log.v(TAG, "是第一屏，点击过下一屏，调整过高度 ，记录第一张图片的上部位置  bottomBallPosition==" + BallPosition.getmSmallBallPossion() + "  topBallPosition==" + topBallPosition);
                }
                //Gionee yuchao 20151217 modify for CR01609425 begin
                if (bottomBallPosition > lastStableBitmap.getHeight()) {
                    lastStableBitmap = Bitmap.createBitmap(lastStableBitmap, 0, topBallPosition, mScreenWidth, lastStableBitmap.getHeight() - topBallPosition);//截图图片用于保存
                } else {
                    lastStableBitmap = Bitmap.createBitmap(lastStableBitmap, 0, topBallPosition, mScreenWidth, bottomBallPosition - topBallPosition);//截图图片用于保存
                }
                //Gionee yuchao 20151217 modify for CR01609425 end
            }

            Log.d(TAG, "total cached bitmap size=" + mScreencapBitmapCache.size());
            saveAllCachedBitmap();//for debug
            if (mScreencapBitmapCache.size() > 1){// 如果多于一屏截图，则进行拼接处理，否则直接通知显示不用处理
                for (int i = 0; i < mScreencapBitmapCache.size(); i++) {
                    if (i >= 1 && i < mScreencapBitmapCache.size()) {//i>0从缓存的第二张开始
                        Bitmap preBitmap = mScreencapBitmapCache.get(i - 1);
                        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 begin
                        int tempOverlayPosition = findTargetOverlayPosition(preBitmap, mScreencapBitmapCache.get(i), i);
                        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 end
                        if (tempOverlayPosition == -1) {
                            Log.w(TAG, "没找到呀， 我退出了");
                            myToast(R.string.long_screen_shot_this_page_is_not_good_enough);
                            sendMyMessage(mHandler, H.MSG_FAIL_TO_SAVE_PIC);
                            return null;
                        } else {
                            Log.v(TAG, "cache number " + i + " find target overlay position==" + tempOverlayPosition);
                            overlayPosition.add(tempOverlayPosition);
                        }
                    }
                }

                caculateTotalScreenShotPicHeight();//
                mScreenBitmap = cutAndComposeFinalBitmap();
            } else {
                mScreenBitmap = lastStableBitmap;// 如只有一张截图，则直接显示这张截图到状态栏即可
                if (mScreenBitmap == null) {
                    Log.v(TAG, "最后一张图  此界面可能被禁止截屏了   lastStableBitmap == null   ===");
                    sendMyMessage(mHandler, H.MSG_FORBID_SCREEN_SHOT);
                    return null;
                }
            }
            saveFinalPicture(mScreenBitmap);
            return null;
        }

        @Override
        protected void onPreExecute() {
            setSavePictureFlag(true);
            setNotiViewText();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setSavePictureFlag(false);
            removeView(mNotificationView);
            BallPosition.setmSmallBallPossion(0);
            if (isNeedAnimation){
                startAnimation(mScreenWidth, mScreenHeight, true, hasNavigationBar());
            }else {
                myToast(R.string.long_screen_shot_save_success);
                sendMyMessage(mHandler, H.MSG_STOP_SERVICE, 1000);
            }
        }
    }

    private Bitmap captureScreen() {
        return SurfaceControl.screenshot(mScreenWidth, mScreenHeight);
    }

    //CHENYEE 20171208 LIXIAOHONG MODIFY FOR REFLECT END
    static final int[] INTARRAY = {32, 61, 79, 160, 268, 276, 286, 318};

    private int[] getPositionIntArray() {
        int[] temp = new int[8];
        for (int i = 0; i < INTARRAY.length; i++) {
            temp[i] = Tools.dip2px(this, INTARRAY[i]);
        }
        return temp;
    }

    /**
     * 根据传入bitmap 遍历保存时截图找到最后需要整合的位置
     *
     * @param lastOneRowbitmapitmap 上一张图片的取的最底部的一条区域
     * @param j                     在mScreencapBitmapCache中的index为j的图片，作为寻找对象
     * @return
     */
    private int findTargetOverlayPosition(Bitmap preBitmap, Bitmap nextBitmap, int cacheIndex) {
        return findTargetOverlayPositionNew(preBitmap, nextBitmap, cacheIndex);
    }
    // a. 单条对比竖线像素相同率的阈值
    private static final double SINGLE_LINE_POINT_SIMILARITY_THRESHOLD = Build.HARDWARE.equalsIgnoreCase("mt6735")? 0.93 : 0.94;
    // b. 单条对比竖线不同像素平均相似度的阈值
    private static final double SINGLE_LINE_PIXEL_SIMILARITY_THRESHOLD = 0.98;
    // c. 8条对比竖线平均像素相同率的阈值
    //chenyee zhaocaili 20180427 modify for CSW1707A-424 begin
    private static final double TOTAL_SIMILARITY_THRESHOLD =
            Build.HARDWARE.equalsIgnoreCase("mt6765")? 0.95 : (Build.HARDWARE.equalsIgnoreCase("mt6735")? 0.93 : 0.98);
    //chenyee zhaocaili 20180521 add for SWW1618OTA-122 end
    //chenyee zhaocaili 20180427 modify for CSW1707A-424 end

    // 满足以下条件的 offset 即认为是合理的覆盖位置: 单条竖线对比结果同时超过阈值 a、b, 所有竖线对比的平均像素相同率超过阈值 c
    private int findTargetOverlayPositionNew(Bitmap preBitmap, Bitmap nextBitmap, int cacheIndex) {
        //chenyee zhaocaili 201808014 add for CSW1802A-1163 begin
        if (mTopClass != null){
            if (!mTopClass.equals("com.android.chrome") && !mTopClass.contains("browser")){
                if (cacheIndex < mScreencapBitmapCache.size() - 1){
                    Log.d(TAG, "findTargetOverlayPositionNew return default pos " + (SCROLL_SCREEN_END_Y + default_pos_offset) + ",  cacheIndex = " + cacheIndex);
                    return SCROLL_SCREEN_END_Y + default_pos_offset;
                }
            }
        }
        //chenyee zhaocaili 201808014 add for CSW1802A-1163 end
        int startPos = -Tools.dip2px(this, 20);
        // 刘海屏状态栏高度大于24dp，取2倍高度来对比，正常24dp的机型取3倍高度来对比
        int picHeight = mStatusBarHeight > Tools.dip2px(this, 24) ? mStatusBarHeight * 2 : mStatusBarHeight * 3;
        int compareTimes = mStatusBarHeight * 2;
        // if cacheIndex is last picture , compareTimes will increase
        if (cacheIndex == mScreencapBitmapCache.size() - 1) {
            Log.v(TAG, "findTargetOverlayPositionNew: bottomBallPosition=" + bottomBallPosition);
            if (bottomBallPosition < SCROLL_SCREEN_START_Y) {
                compareTimes = bottomBallPosition - SCROLL_SCREEN_END_Y;
            } else {
                compareTimes = SCROLL_SCREEN_START_Y - SCROLL_SCREEN_END_Y;
            }
        }
        Log.d(TAG, "findTargetOverlayPositionNew: cacheIndex=" + cacheIndex + ", picHeight=" + picHeight + ", compareTimes=" + compareTimes);

        int[] xPositions = getPositionIntArray();
        // 从上一副图片中取出8条对比竖线的像素，高为 picHeight
        int[][] prePixels = new int[8][picHeight];
        // 从下一副图片中取出8条对比竖线的像素，高为 compareTimes - startPos + picHeight
        int nextHeight = compareTimes - startPos + picHeight;
        int[][] nextPixels = new int[8][nextHeight];

        //chenyee zhaocaili 20180814 modify for CSW1802A-1163 begin
        double tempTotal = 0;
        int tempOffset = startPos;
        //chenyee zhaocaili 20180814 modify for CSW1802A-1163 end

        for (int i = 0; i < 8; i++) {
            preBitmap.getPixels(prePixels[i], 0, 1, xPositions[i], SCROLL_SCREEN_START_Y - picHeight, 1, picHeight);
            nextBitmap.getPixels(nextPixels[i], 0, 1, xPositions[i], SCROLL_SCREEN_END_Y - picHeight + startPos, 1, nextHeight);
        }

        double totalSameRate, pointSameRate, pixelSimilarity;
        int offset, diffPoints;
        boolean currentOk;

        for (int i = 0; i < compareTimes - startPos; i++) {
            offset = i + startPos;
            totalSameRate = 0.0;
            currentOk = true;
            for (int j = 0; j < 8; j++) {
                diffPoints = 0;
                pixelSimilarity = 0.0;
                for (int k = 0; k < picHeight; k++) {
                    if (prePixels[j][k] != nextPixels[j][i+k]) {
                        diffPoints++;
                        pixelSimilarity += computePixelSimilarity(prePixels[j][k], nextPixels[j][i+k]);
                    }
                }
                pointSameRate = (picHeight - diffPoints) * 1.0 / picHeight;
                totalSameRate += pointSameRate;
                pixelSimilarity = diffPoints == 0 ? 1.0 : pixelSimilarity / diffPoints;

                //chenyee zhaocaili 20180814 modify for CSW1802A-1163 begin
                /*if ((pointSameRate > 0.92 || j > 0) && diffPoints > 0) {  // 输出可能的点，完全相同则没必要输出
                    Log.d(TAG, "pixel compare: offset=" + offset + ", whichLine=" + j +
                            ", 像素相同率: " + pointSameRate + ", 不同像素相似度: " + pixelSimilarity);
                }
                if (pointSameRate < SINGLE_LINE_POINT_SIMILARITY_THRESHOLD || pixelSimilarity < SINGLE_LINE_PIXEL_SIMILARITY_THRESHOLD) {
                    currentOk = false;
                    break;
                }*/
            }

            if (currentOk) {
                totalSameRate /= 8;
                if (tempTotal < totalSameRate){
                    tempTotal = totalSameRate;
                    tempOffset = offset;
                }
                /*if (totalSameRate > TOTAL_SIMILARITY_THRESHOLD) {
                    Log.d(TAG, "findTargetOverlayPositionNew: find in offset=" + offset + ", absValue=" +
                            (SCROLL_SCREEN_END_Y + offset) + ", totalSameRate=" + totalSameRate);
                    return SCROLL_SCREEN_END_Y + offset;
                }*/
            }
        }
        if (tempTotal >= 0.85){
            Log.d(TAG, "findTargetOverlayPositionNew: find in offset=" + tempOffset + ", absValue=" +
                    (SCROLL_SCREEN_END_Y + tempOffset) + ", totalSameRate=" + tempTotal);
            return SCROLL_SCREEN_END_Y + tempOffset;
        }

        //chenyee zhaocaili 20180814 modify for CSW1802A-1163 end

        Log.d(TAG, "findTargetOverlayPositionNew: return default " + (SCROLL_SCREEN_END_Y + default_pos_offset));
        return SCROLL_SCREEN_END_Y + default_pos_offset;
    }

    private double computePixelSimilarity(int p1, int p2) {
        int dr = Math.abs((p1 & 0x00ff0000) >> 16 - (p2 & 0x00ff0000) >> 16);
        int dg = Math.abs((p1 & 0x0000ff00) >> 8 - (p2 & 0x0000ff00) >> 8);
        int db = Math.abs(p1 & 0x000000ff - p2 & 0x000000ff);
        return (255 * 3 - dr - dg - db) / (3.0 * 255);
    }
    // Chenyee xionghg 20180323 add for CSW1703A-415 end

    int compareBitmapPixels;

    /**
     * 目的是判断前一张图片和滚屏后的图片是否相同，通过取部分区域来对比
     * 取SCROLL_SCREEN_END_Y下面的SCROLL_SCREEN_END_Y+2*mStatusBarHeight位置的一小条图片，宽度为屏幕宽度的2/3
     * 高度为picHeight。如果第一次没找到，则再换个高度再取4个像素来比较
     *
     * @param preBitmap
     * @param nowBitmap
     * @return
     */
    private boolean isScrollToBottom(Bitmap preBitmap, Bitmap nextBitmap) {
        Bitmap preSmall1 = null;
        Bitmap preSmall2 = null;
        Bitmap preSmall3 = null;

        Bitmap nextSmall1 = null;
        Bitmap nextSmall2 = null;
        Bitmap nextSmall3 = null;

        int compareScrolledToBottomPos = mStatusBarHeight * 9;//取中间位置，防止被上下滑动小球截取
        int picHeight = mStatusBarHeight * 4;//mScreenHeight/5*2 < picHeight < mScreenHeight/5*3
        int picWidth = 1;
        boolean isSame1 = false;
        boolean isSame2 = false;
        boolean isSame3 = false;

        preSmall1 = Bitmap.createBitmap(preBitmap, mScreenWidth / 8, compareScrolledToBottomPos, picWidth, picHeight);
        preSmall2 = Bitmap.createBitmap(preBitmap, mScreenWidth / 4, compareScrolledToBottomPos, picWidth, picHeight);
        preSmall3 = Bitmap.createBitmap(preBitmap, mScreenWidth / 4 * 3, compareScrolledToBottomPos, picWidth, picHeight);

        for (int i = -10; i < 10; i++) {
            nextSmall1 = Bitmap.createBitmap(nextBitmap, mScreenWidth / 8, compareScrolledToBottomPos + i, picWidth, picHeight);
            nextSmall2 = Bitmap.createBitmap(nextBitmap, mScreenWidth / 4, compareScrolledToBottomPos + i, picWidth, picHeight);
            nextSmall3 = Bitmap.createBitmap(nextBitmap, mScreenWidth / 4 * 3, compareScrolledToBottomPos + i, picWidth, picHeight);

            //chenyee zhaocaili 20180828 add for CSW1802A-1437 begin
            float similarity1 = BitmapCompare.similarity(preSmall1, nextSmall1);
            float similarity2 = BitmapCompare.similarity(preSmall2, nextSmall2);
            float similarity3 = BitmapCompare.similarity(preSmall3, nextSmall3);
            float averageSimilarity = (similarity1 + similarity2 + similarity3) / 3;
            if (averageSimilarity >= 0.98 || i == 0){
                isScrolledToBottom = performHorizontalCheck(preBitmap, nextBitmap, compareScrolledToBottomPos, compareScrolledToBottomPos + i);
                if (isScrolledToBottom){
                    break;
                }
            }

            nextSmall1.recycle();
            nextSmall2.recycle();
            nextSmall3.recycle();

            /*int sameNum = 0;
            if (Arrays.equals(bitmapToByteArray(preSmall1), bitmapToByteArray(nextSmall1))) {
                Log.v(TAG, "1--same--");
                isSame1 = true;
                sameNum++;
            } else {
                Log.v(TAG, "1-not same--");
                isSame1 = false;
            }

            if (Arrays.equals(bitmapToByteArray(preSmall2), bitmapToByteArray(nextSmall2))) {
                Log.v(TAG, "2--same--");
                isSame2 = true;
                sameNum++;
            } else {
                Log.v(TAG, "2-not same--");
                isSame2 = false;
            }

            if (Arrays.equals(bitmapToByteArray(preSmall3), bitmapToByteArray(nextSmall3))) {
                Log.v(TAG, "3--same--");
                isSame3 = true;
                sameNum++;
            } else {
                Log.v(TAG, "3-not same--");
                isSame3 = false;
            }

            nextSmall1.recycle();
            nextSmall2.recycle();
            nextSmall3.recycle();

            //chenyee zhaocaili 20180814 modify for CSW1802A-1163 begin
            if (isSame1 && isSame2 && isSame3) {
                isScrolledToBottom = performHorizontalCheck(preBitmap, nextBitmap, compareScrolledToBottomPos, compareScrolledToBottomPos + i);
                if (isScrolledToBottom){
                    break;
                }
            }else if (i == 0 && sameNum >= 2){
                isScrolledToBottom = performHorizontalCheck(preBitmap, nextBitmap, compareScrolledToBottomPos, compareScrolledToBottomPos);
                if (isScrolledToBottom){
                    break;
                }
            }
            //chenyee zhaocaili 20180814 modify for CSW1802A-1163 end*/
            //chenyee zhaocaili 20180828 add for CSW1802A-1437 end
        }

        if (isScrolledToBottom) {
            // YouJuAgent.onEvent(mContext, "滚动到底部");
            myToast(R.string.long_screen_shot_scrolled_to_bottom);
        }
        Log.d(TAG, "compare bitmap, isScrolledToBottom = " + isScrolledToBottom);
        return isScrolledToBottom;
    }

    //chenyee zhaocaili 20180814 modify for CSW1802A-1163 begin
    private boolean performHorizontalCheck(Bitmap bitmap1, Bitmap bitmap2, int pos1, int pos2){
        boolean same;
        Bitmap pre = Bitmap.createBitmap(bitmap1,50, pos1, mScreenWidth - 100, 300);
        Bitmap next = Bitmap.createBitmap(bitmap2, 50, pos2, mScreenWidth - 100, 300);
        float similarity = BitmapCompare.similarity(pre,next);
        Log.d(TAG, "bitmap similarity = " + similarity);
        if (similarity > 0.99){
            same = true;
        }else {
            same = false;
        }
        pre.recycle();
        next.recycle();
        return same;
    }
    //chenyee zhaocaili 20180814 modify for CSW1802A-1163 end

    /**
     * 计算最终图片的高度
     */
    private void caculateTotalScreenShotPicHeight() {
        Log.v(TAG, "caculateTotalScreenShotPicHeight:");
        for (int i = 0; i < overlayPosition.size(); i++) {
            Log.d(TAG, "--overlayPosition->" + i + " = " + overlayPosition.get(i));
        }

        if (mScreencapBitmapCache.size() == 1) {
            finalScreenShotPicHeight = mScreencapBitmapCache.get(0).getHeight();
            Log.v(TAG, "caculateTotalScreenShotPicHeight  only have one picture==" + finalScreenShotPicHeight);
        } else {
            for (int i = 0; i < overlayPosition.size(); i++) {//第二张图片到倒数第二张图片
                if (i == 0) {
                    finalScreenShotPicHeight += (SCROLL_SCREEN_START_Y - overlayPosition.get(i));//第一张图片,根据顶部小球的位置
                    Log.d(TAG, "caculateTotalScreenShotPicHeight->" + i + " = " + finalScreenShotPicHeight);
                } else if (0 < i && i < (overlayPosition.size() - 1)) {
                    finalScreenShotPicHeight += (SCROLL_SCREEN_START_Y - overlayPosition.get(i));
                    Log.d(TAG, "caculateTotalScreenShotPicHeight->" + i + " = " + finalScreenShotPicHeight);
                } else if (i == (overlayPosition.size() - 1)) {
                    //最后一张图片需要截取的高度
                    Log.v(TAG, "caculateTotalScreenShotPicHeight last cached picture   bottomBallPosition===" + bottomBallPosition);
                    if (overlayPosition.get(overlayPosition.size() - 1) >= bottomBallPosition) {//特殊情况发生，说明要删掉最后一张图片，更改倒数第二张图片的底部位置
                        specialPostion = bottomBallPosition;
                        //现在的倒数第二张图片应该截图的图片的高度，要剪掉一部份
                        specialHeight = (SCROLL_SCREEN_START_Y - overlayPosition.get(overlayPosition.size() - 2)) - (overlayPosition.get(overlayPosition.size() - 1) - bottomBallPosition);
                        Log.v(TAG, "caculateTotalScreenShotPicHeight  specialHeight====" + specialHeight);
                        finalScreenShotPicHeight = finalScreenShotPicHeight - (overlayPosition.get(overlayPosition.size() - 1) - bottomBallPosition);
                        Log.v(TAG, "caculateTotalScreenShotPicHeight ==特殊情况==" + i + "==" + finalScreenShotPicHeight);
                        mScreencapBitmapCache.remove(mScreencapBitmapCache.size() - 1);//删掉最后一张图片,刚才的倒数第二张图片就变为倒数第一张了
                        overlayPosition.remove(overlayPosition.size() - 1);//删掉最后一张图片保存的位置
                        Log.v(TAG, "caculateTotalScreenShotPicHeight 特殊情况，去掉了原来的倒数第一张图片  更新 cache size==" + mScreencapBitmapCache.size());
                        Log.v(TAG, "caculateTotalScreenShotPicHeight 特殊情况， specialPostion ==" + specialPostion);
                    } else {
                        finalScreenShotPicHeight += (bottomBallPosition - overlayPosition.get(overlayPosition.size() - 1));
                        Log.d(TAG, "caculateTotalScreenShotPicHeight->" + i + " = " + finalScreenShotPicHeight);
                    }
                }
            }
        }
        Log.i(TAG, "final caculateTotalScreenShotPicHeight = " + finalScreenShotPicHeight);
    }

    private static void gc() {
        System.gc();
        // 表示java虚拟机会做一些努力运行已被丢弃对象（即没有被任何对象引用的对象）的 finalize
        // 方法，前提是这些被丢弃对象的finalize方法还没有被调用过
        System.runFinalization();
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(source, x, y, width, height);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = Bitmap.createBitmap(source, x, y, width, height);
        }
        return bitmap;
    }

    /**
     * 截取并合并成最后的图片
     *
     * @return 合成后的图片
     */
    private Bitmap cutAndComposeFinalBitmap() {
        Bitmap tempCutBitmap = null;
        Bitmap tempFinalBitmap = null;
        int tempHeight = 0;
        int tempSize = mScreencapBitmapCache.size();
        Log.v(TAG, "cutAndComposeFinalBitmap, tempSize = " + tempSize);
        tempFinalBitmap = Bitmap.createBitmap(mScreenWidth, finalScreenShotPicHeight, Config.ARGB_8888); // 创建一个空的整个大小的bitmap

        Canvas cv = new Canvas(tempFinalBitmap);
        tempCutBitmap = createBitmap(mScreencapBitmapCache.get(0), 0, 0, mScreenWidth, SCROLL_SCREEN_START_Y);
        tempHeight = tempCutBitmap.getHeight();
        cv.drawBitmap(tempCutBitmap, 0, 0, null);// 画第一张bitmap ，画板刚开始从0，0开始画

        Log.v(TAG, "cutAndComposeFinalBitmap, first picture tempHeight = " + tempHeight);
        savePic(tempCutBitmap, "save0");
        for (int i = 1; i < mScreencapBitmapCache.size() - 1; i++)//
        {
            tempCutBitmap = createBitmap(mScreencapBitmapCache.get(i), 0, overlayPosition.get(i), mScreenWidth, (SCROLL_SCREEN_START_Y - overlayPosition.get(i)));
            savePic(tempCutBitmap, "save" + i);

            cv.drawBitmap(tempCutBitmap, 0, tempHeight, new Paint());//根据之前画的总高度开始画
            tempHeight += tempCutBitmap.getHeight();
            Log.v(TAG, "cutAndComposeFinalBitmap, i=" + i + "  tempHeight=" + tempHeight);
        }

        Log.v(TAG, "overlayPosition.get(tempSize-1) = " + overlayPosition.get(tempSize - 1));
        //最后一张图
        Log.v(TAG, "cutAndComposeFinalBitmap, last picture specialPostion = " + specialPostion);
        if (specialPostion == 0) {//没有发生特殊情况********
            Log.v(TAG, " -cutAndComposeFinalBitmap() Here --- 没有发生特殊情况");
            //CHENYEE 20170414 lixiaohong modify for #113179 begin
            int mBitmapHeight = mScreencapBitmapCache.get(tempSize - 1).getHeight();
            int mPositionY = overlayPosition.get(tempSize - 1);
            int mHeight = bottomBallPosition - overlayPosition.get(tempSize - 1);
            Log.d(TAG, "cutAndComposeFinalBitmap, mBitmapHeight=" + mBitmapHeight + " mPositionY=" + mPositionY + " mHeight=" + mHeight);
            if (mPositionY + mHeight > mBitmapHeight) {
                mHeight = mBitmapHeight - mPositionY;
                Log.d(TAG, " -cutAndComposeFinalBitmap() new mHeight = " + mHeight);
            }
            tempCutBitmap = Bitmap.createBitmap(mScreencapBitmapCache.get(tempSize - 1), 0, mPositionY, mScreenWidth, mHeight);
            Log.v(TAG, " -cutAndComposeFinalBitmap() There --- 没有发生特殊情况");
            //CHENYEE 20170414 lixiaohong modify for #113179 end
        } else {
            Log.v(TAG, " cutAndComposeFinalBitmap  发生特殊情况");
            tempCutBitmap = createBitmap(mScreencapBitmapCache.get(tempSize - 1), 0, overlayPosition.get(tempSize - 1), mScreenWidth, specialHeight);
        }

        Log.v(TAG, "tempCutBitmap height==" + tempCutBitmap.getHeight());
        savePic(tempCutBitmap, "save" + (tempSize - 1));
        cv.drawBitmap(tempCutBitmap, 0, tempHeight, new Paint());
        tempHeight += tempCutBitmap.getHeight();
        Log.d(TAG, "cutAndComposeFinalBitmap, last picture tempHeight = " + tempHeight);
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存 //store
        cv.restore();// 存储
        return tempFinalBitmap;
    }


    private void saveAllCachedBitmap() {
        if (Tools.saveAllCachedBitmap) {
            Log.v(TAG, "saveAllCachedBitmap== ");
            for (int i = 0; i < mScreencapBitmapCache.size(); i++) {
                try {
                    FileOutputStream out = new FileOutputStream("/mnt/sdcard/" + "Pictures/savecachedPic" + i + ".png");// "/mnt/sdcard/"
                    if (mScreencapBitmapCache.get(i).compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasNavigationBar() {
        try {
            //chenyee zhaocaili 20180503 modify for CSW1703A-2263 begin
            return android.view.WindowManagerGlobal.getWindowManagerService().hasNavigationBar() && isNavigationBarShow;
            //chenyee zhaocaili 20180503 modify for CSW1703A-2263 end
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initParameters() {
        // Log.v(TAG, "initParameters");
        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170721> add for #91401 begin
        int statusId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int navigationId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (statusId > 0) {
            mStatusBarHeight = getResources().getDimensionPixelSize(statusId);
        }
        if (navigationId > 0) {
            mNavigationBarHeight = getResources().getDimensionPixelSize(navigationId);
        }

        mLayoutParams = new WindowManager.LayoutParams();

        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170811> add for #97147 begin
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170811> add for #97147 end

        //Gionee liuweiming for #71321  20170309 start
        SCROLL_SCREEN_START_Y = mStatusBarHeight * 21;
        //Gionee liuweiming for #71321  20170309  end
        SCROLL_SCREEN_END_Y = mStatusBarHeight * 6;

        //chenyee zhaocaili 20180829 modify for CSW1802SN-16 begin
        default_pos_offset = getDefaultOffsetPos();

        bottomBallPosition = hasNavigationBar() ? mScreenHeight - mNavigationBarHeight : mScreenHeight;
        topBallPosition = mStatusBarHeight;
        handleSpecialCase();
    }

    private void handleSpecialCase(){
        //chenyee zhaocaili 20180809 add for CSW1802A-1119 begin
        if (SystemProperties.get("ro.cy.projectid", "unknown").contains("CSW1802")){
            SCROLL_SCREEN_START_Y = mStatusBarHeight * 20;
        }
        //chenyee zhaocaili 20180809 add for CSW1802A-1119 end

        //chenyee zhaocaili 20180521 add for SWW1618OTA-127 begin
        if (mTopClass != null){
            if (mTopClass.equals("com.android.chrome")){
                SCROLL_SCREEN_STEP = 1;
            }else if (mTopClass.contains("browser")){
                SCROLL_SCREEN_STEP = 5;
            }
        }
        //chenyee zhaocaili 20180521 add for SWW1618OTA-127 end
    }
    //chenyee zhaocaili 20180829 modify for CSW1802SN-16 end

    /**当背景为非纯色或不规则图案时，通过该偏移值寻找拼接点**/
    private int getDefaultOffsetPos(){
        int offset = 20;
        if (mScreenHeight == 2560) {
            offset = 40;    //M2017
        } else if (mScreenHeight == 2160) {
            offset = 28;    //M7
        } else if (mScreenHeight == 1920) {
            offset = 28;    //S10 7533 1617
        } else if (mScreenHeight == 1440) {
            offset = 18;    //大金刚2 17G16 17W08
        } else if (mScreenHeight == 1280) {
            offset = 20;    //F109
        } else if (mScreenHeight == 1520 || mScreenHeight == 1500) {
            offset = 15;    //1707
        } else if (mScreenHeight == 800) {
            offset = 11;//TODO 待以后适配项目时调试
        }
        return offset;
    }

    private void initSystemService() {
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initFullScreenViews(){
        mFullScreenCheckViewLeft = mLayoutInflater.inflate(R.layout.full_screen_use, null);
        mFullScreenCheckViewRight = mLayoutInflater.inflate(R.layout.full_screen_use, null);

        mFullScreenCheckViewRight.setOnTouchListener(mFullViewTouchListener);
        mFullScreenCheckViewLeft.setOnTouchListener(mFullViewTouchListener);

        mFullScreenCheckViewRight.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout full view bottom=" + mFullScreenCheckViewRight.getBottom());
                DisplayMetrics dm = new DisplayMetrics();
                //chenyee zhaocaili 20180828 modify for CSW1802SN-16 begin
                mWindowManager.getDefaultDisplay().getRealMetrics(dm);
                //chenyee zhaocaili 20180828 modify for CSW1802SN-16 end
                int viewHeight = mFullScreenCheckViewRight.getHeight();
                if (viewHeight == dm.widthPixels || viewHeight == dm.heightPixels) {
                    mIsFullScreen = true;
                } else {
                    mIsFullScreen = false;
                }
                //chenyee zhaocaili 20180503 add for CSW1703A-2263 begin
                if (mFullScreenCheckViewRight.getBottom() == mScreenHeight - mStatusBarHeight){
                    isNavigationBarShow = false;
                }else {
                    isNavigationBarShow = true;
                }

                Log.d(TAG, "mFullScreenCheckViewRight  mIsFullScreen === " + mIsFullScreen + ",  isNavigationBarShow = " + isNavigationBarShow);
                //chenyee zhaocaili 20180503 add for CSW1703A-2263 end
                isFullScreen(mIsFullScreen);
            }
        });
        Log.d(TAG, "before add mFullScreenCheckViewRight to window  mIsFullScreen=" + mIsFullScreen);
        addView(Constant.FULLVIEW_LEFT, mFullScreenCheckViewLeft);
        addView(Constant.FULLVIEW_RIGHT, mFullScreenCheckViewRight);
    }

    private void initNotificationView(){
        mNotificationView = mLayoutInflater.inflate(R.layout.notification_layout, null);
        mNotiText = (TextView)mNotificationView.findViewById(R.id.text);
        mNotiText.setText(R.string.screen_scroll_tip);
        mNotificationView.setOnTouchListener(mFullViewTouchListener);
    }

    private void setNotiViewText(){
        mNotiText.setText(R.string.long_screen_shot_save_pic);
    }

    private View.OnTouchListener mFullViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "Window is Touched");
            isWindowTouched = true;
            return true;
        }
    };

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        sendLSEndBroadcast();
        unRegisterReceiver();
        recycleBitmaps();
        super.onDestroy();
    }

    private void recycleBitmaps(){
        if (mScreenBitmap != null && !mScreenBitmap.isRecycled()){
            mScreenBitmap.recycle();
            mScreenBitmap = null;
        }
        if (lastStableBitmap != null && !lastStableBitmap.isRecycled()){
            lastStableBitmap.recycle();
            lastStableBitmap = null;
        }

        for (Bitmap mBitmap : mScreencapBitmapCache) {
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }
        //CHENYEE 20170414 lixiaohong add for recycle Bitmap end
        mScreencapBitmapCache.removeAll(mScreencapBitmapCache);
    }

    private void unRegisterReceiver() {
        if (mHadRegisterReceivers) {
            try {
                unregisterReceiver(MyReceiver.mHomeKeyEventReceiver);
                unregisterReceiver(MyReceiver.mReceiver);
                mHadRegisterReceivers = false;
            }catch (Exception e){
                Log.e(TAG, "unregisterReceiver err : " + e.getMessage());
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void setFirstBitmapPosition() {
        if (!isAlreadyFirstClickedNextScreen) {//如果是第一次点击向下滚动
            topBallPosition = BallPosition.getmSmallBallPossion();
            if (topBallPosition <= mStatusBarHeight) {//表示 上面的小球位置没有被改变
                Log.v(TAG, "first bitmap's top ball position not change.   ballPosition==" + BallPosition.getmSmallBallPossion());
                overlayPosition.add(0);//用户没有更改小球位置，则要保存第一张图片的状态栏
            } else {
                //确认第一张图片 上面的小球有有有有变化
                Log.v(TAG, "first bitmap's top ball position changed.  ballPosition==" + BallPosition.getmSmallBallPossion());
                //坐标检测，防止生成bitmap失败
                if (topBallPosition > mScreenHeight / 5 * 2) {
                    topBallPosition = mScreenHeight / 5 * 2;
                }
                overlayPosition.add(topBallPosition);//保存用户实际位置
            }
            isAlreadyFirstClickedNextScreen = true;
        }
    }

    public void stopSelfNormally() {
        if (isStoppingMySelf) {
            return;
        }
        isStoppingMySelf = true;

        removeView(mFullScreenCheckViewRight);
        removeView(mFullScreenCheckViewLeft);
        removeView(mNotificationView);
        myStopForeground();
        stopSelf();
    }

    /*
     * function:停止screenshotservice
     */
    public void stopSelfBySelf() {
        if (isStoppingMySelf) {
            return;
        }
        isStoppingMySelf = true;
        Log.v(TAG, "stopSelfBySelf, isStoppingMySelf=" + isStoppingMySelf);

        removeView(mFullScreenCheckViewRight);
        removeView(mFullScreenCheckViewLeft);
        removeView(mNotificationView);
        myStopForeground();//停止自己作为前台服务
        if (isSavingPicture) {//正常杀死自己
            Log.d(TAG, "stopSelfBySelf, now service is really to be killed");
            Intent m = new Intent(this, ScreenShotService.class);
            this.stopService(m);
        } else {
            Log.d(TAG, "ScreenShotService will force stop by am");
            Method mForecStopPkgMethod = ReflectUtil.getMethodByParameters(Constant.CLS_ACTIVITYMANAGER, "forceStopPackage", String.class);
            ActivityManager mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            ReflectUtil.invokeNoReturnMethod(mForecStopPkgMethod, mAm, Constant.PKG_LONGSCREENSHOT);
        }
    }

    public byte[] bitmapToByteArray(Bitmap tempbitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempbitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void sendMyMessage(Handler handler, int what) {
        handler.removeMessages(what);
        handler.obtainMessage(what, ScreenShotService.this).sendToTarget();
    }

    private void sendMyMessage(Handler handler, int what, long delay) {
        handler.removeMessages(what);
        handler.sendMessageDelayed(handler.obtainMessage(what, ScreenShotService.this), delay);

    }

    //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 begin
    private void saveFinalPicture(Bitmap screenshotbitmap) {
        Log.v(TAG, "saveFinalPicture:");
        if (null == screenshotbitmap) {
            Log.e(TAG, "null == longscreenshotbitmap   !!");
            sendMyMessage(mHandler, H.MSG_FAIL_TO_SAVE_PIC);
            return;
        }

        if (mkdirToSaveImage()) {
            mSaveImageToFileSystem = saveImageToFileSystem(mImageFilePath, screenshotbitmap);
        }

        if (mSaveImageToFileSystem) {
            imageUri = insertUri();
        }

        if (imageUri == null) {
            Log.d(TAG, "imageUri == null");
            imageUri = queryUri(mImageFilePath);
        }
        Log.i(TAG, "imageUri = " + imageUri);
        if (imageUri == null) {
            sendMyMessage(mHandler, H.MSG_FAIL_TO_SAVE_PIC);
        }
        //stopSelfBySelf();
    }

    private Uri insertUri() {
        mImageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(mImageTime));
        mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);
        long dateSeconds = mImageTime / 1000;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.DATA, mImageFilePath);
        values.put(MediaStore.Images.ImageColumns.TITLE, mImageFileName);//
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, mImageFileName);//
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, mImageTime);
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.ImageColumns.WIDTH, mScreenWidth);
        values.put(MediaStore.Images.ImageColumns.HEIGHT, finalScreenShotPicHeight);
        return mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri queryUri(String path) {
        // Chenyee xionghg 20180126 add for CSW1705A-1293 begin
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        // Chenyee xionghg 20180126 add for CSW1705A-1293 end
        File file = new File(path);
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (!file.exists()) {
            Log.w(TAG, "File:" + path + " not exist.");
            return null;
        } else {
            String params[] = new String[]{path};
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"rowId"}, "_data LIKE ?", params, null);
            if (cursor == null) return null;
            Log.v(TAG, " cursor.getCount() ===" + cursor.getCount());
            if (cursor.getCount() == 1) {
                uri = FileProvider.getUriForFile(mContext, Constant.PROVIDER_AUTHORITY, file);
                Log.v(TAG, "Uri  fromFile  toString=" + uri.toString() + "   uri=" + uri);
                return uri;
            }
            cursor.close();
            Log.v(TAG, "the path file no uri in DB");
            return null;
        }
    }

    // Chenyee xionghg 20180129 modify for CSW1702A-2799 begin
    private boolean saveImageToFileSystem(String path, Bitmap bitmap) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            File outFile = new File(path);
            OutputStream out = new FileOutputStream(outFile);

            boolean bCompressOK;
            try {
                bCompressOK = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);//50比较长的图片微信发送会失败
                Log.d(TAG, "compress bitmap to file, bCompressOK = " + bCompressOK);
            } finally {
                out.close();
                Log.v(TAG, "OutputStream  close");
            }

            if (!bCompressOK && outFile.exists()) {
                boolean deleted = outFile.delete();
                Log.i(TAG, "delete target file after compress failed, isSuccess=" + deleted);
            }

            return bCompressOK;
        } catch (IOException e) {
            Log.e(TAG, "saveImageToFileSystem: ", e);
            sendMyMessage(mHandler, H.MSG_FAIL_TO_SAVE_PIC);
            return false;
        }
    }

    private boolean mkdirToSaveImage() {
        mImageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat(mDateFormat).format(new Date(mImageTime));
        mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);
        if (null == mSaveImageDir) {
            mSaveImageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        mScreenshotDir = new File(mSaveImageDir, SCREENSHOTS_DIR_NAME);
        if (!mScreenshotDir.exists() && !mScreenshotDir.mkdirs()) {
            Log.w(TAG, "failed to create screenshot dir");
            sendMyMessage(mHandler, H.MSG_FAIL_TO_SAVE_PIC);
            return false;
            //throw new IllegalStateException("Cant't create dir : " + mScreenshotDir);
        }
        mImageFilePath = mScreenshotDir.getAbsolutePath() + File.separator + mImageFileName;
        Log.i(TAG, "mImageFilePath=" + mImageFilePath);
        return true;
    }
    //Gionee <gn_by_LongScreenShot> <liuweiming> <20170515> add for #84429 end

    private void initSoundPool() {
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(NUM_MEDIA_SOUND_STREAMS)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        mSoundPool.setOnLoadCompleteListener(mLoadCompleteListener);
    }

    private SoundPool.OnLoadCompleteListener mLoadCompleteListener = new SoundPool.OnLoadCompleteListener() {
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d(TAG, "mLoadCompleteListener-onLoadComplete() playSoundId = " + playSoundId + " status = " + status);
            if (status == 0 && playSoundId > 0) {
                mSoundPool.play(playSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
            new SavePictureTask().execute(null, null);
        }
    };

    private void saveAndPlaySound() {
        Log.v(TAG, "ScreenShotService-saveAndPlaySound()");
        try {
            initSoundPool();
            playSoundId = mSoundPool.load(Constant.SOUND_FILE_PATH, 1);
        } catch (Exception e) {
            Log.e(TAG, "ScreenShotService-saveAndPlaySound() occur exception -> " + e.getMessage());
        }
    }

    private void releaseSound() {
        if (mSoundPool != null) {
            playSoundId = -1;
            mSoundPool.release();
            mSoundPool = null;
        }
    }
    //CHENYEE 20170516 lixiaohong add for #141427 end

    //chenyee zhaocaili 20180421 add for CSW1703MX-70 begin
    private void initPicSaveDir(){
        if ("MEXICO_LANIX".equals(SystemProperties.get("ro.cy.custom"))){
            SCREENSHOT_FILE_NAME_TEMPLATE = "Captura de pantalla_%s.png";
            SCREENSHOTS_DIR_NAME = "Fotos/Capturas de pantalla";
        }else {
            SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot_%s.png";
            SCREENSHOTS_DIR_NAME = "Pictures/Screenshots";
        }
    }
    //chenyee zhaocaili 20180421 add for CSW1703MX-70 end


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        Log.d(TAG, "onConfigurationChanged isLandscape = " + isLandscape);
        if (isLandscape){
            myToast(R.string.long_screen_shot_not_surport_landscape);
            setAnimationFlag(false);
            removeView(mNotificationView);
            sendMyMessage(mHandler, H.MSG_STOP_SERVICE, 1000);
        }
    }

    private void initDisplayView(){
        mScreenshotLayout = mLayoutInflater.inflate(R.layout.global_screenshot, null);
        mBackgroundView = (ImageView) mScreenshotLayout.findViewById(R.id.global_screenshot_background);
        mScreenshotContainer = mScreenshotLayout.findViewById(R.id.global_screenshot_container);
        mScreenshotView = (LargeImageView) mScreenshotLayout.findViewById(R.id.global_screenshot);
        mScreenshotFlash = (ImageView) mScreenshotLayout.findViewById(R.id.global_screenshot_flash);
        mScreenshotLayout.setFocusable(true);
        mScreenshotView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Intercept and ignore all touch events
                if (!isFadeOutAnimRunning){
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        mScreenshotFadeOutAnim.pause();
                    }else if (event.getAction() == KeyEvent.ACTION_UP){
                        mScreenshotFadeOutAnim.setStartDelay(SCREENSHOT_DROP_OUT_DELAY);
                        mScreenshotFadeOutAnim.start();
                    }

                }
                return false;
            }
        });

        mScreenshotLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                endAnimation();
                return  true;
            }
        });

        // Setup the window that we are going to use

        mScreenshotDropInAnim = createScreenshotDropInAnimation();
        mScreenshotFadeOutAnim = createScreenshotDropOutAnimation(mScreenWidth, mScreenHeight, true, hasNavigationBar());

        // Scale has to account for both sides of the bg
        mBgPadding = (float) mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_bg_padding);
        mBgPaddingScale = mBgPadding / mScreenWidth;
        Log.d(TAG, "mBgPaddingScale = "+ mBgPaddingScale);
    }

    /**
     * Starts the animation after taking the screenshot
     */
    private void startAnimation(int w, int h, boolean statusBarVisible, boolean navBarVisible) {
        // Add the view for the animation
        mScreenshotView.setBitmap(mScreenBitmap);
        mScreenshotLayout.requestFocus();

        // Setup the animation with the screenshot just taken
        endAnimation();

        addView(Constant.SCREENSHOTVIEW, mScreenshotLayout);

        mScreenshotAnimation = new AnimatorSet();
        mScreenshotAnimation.playSequentially(mScreenshotDropInAnim, mScreenshotFadeOutAnim);
        mScreenshotAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Save the screenshot once we have a bit of time now
                removeView(mScreenshotLayout);

                // Clear any references to the bitmap
                mScreenshotView.setBitmap(null);
                //chenyee zhaocaili 20180728 add for CSW1802A-484 begin
                if (!isDeleting){
                    stopSelfBySelf();
                }
                //chenyee zhaocaili 20180728 add for CSW1802A-484 end
            }
        });
        mScreenshotLayout.post(new Runnable() {
            @Override
            public void run() {
                mScreenshotContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mScreenshotContainer.buildLayer();
                mScreenshotAnimation.start();
                Log.d(TAG, "startAnimation");
            }
        });
    }
    private ValueAnimator createScreenshotDropInAnimation() {
        final float flashPeakDurationPct = ((float) (SCREENSHOT_FLASH_TO_PEAK_DURATION)
                / SCREENSHOT_DROP_IN_DURATION);
        final float flashDurationPct = 2f * flashPeakDurationPct;
        final Interpolator flashAlphaInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float x) {
                // Flash the flash view in and out quickly
                if (x <= flashDurationPct) {
                    return (float) Math.sin(Math.PI * (x / flashDurationPct));
                }
                return 0;
            }
        };
        final Interpolator scaleInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float x) {
                // We start scaling when the flash is at it's peak
                if (x < flashPeakDurationPct) {
                    return 0;
                }
                return (x - flashDurationPct) / (1f - flashDurationPct);
            }
        };
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(SCREENSHOT_DROP_IN_DURATION);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBackgroundView.setAlpha(0f);
                mBackgroundView.setVisibility(View.VISIBLE);
                mScreenshotContainer.setAlpha(0f);
                mScreenshotContainer.setTranslationX(0f);
                mScreenshotContainer.setTranslationY(0f);
                mScreenshotContainer.setScaleX(SCREENSHOT_SCALE + mBgPaddingScale);
                mScreenshotContainer.setScaleY(SCREENSHOT_SCALE + mBgPaddingScale);
                mScreenshotContainer.setVisibility(View.VISIBLE);
                mScreenshotFlash.setAlpha(0f);
                mScreenshotFlash.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                mScreenshotFlash.setVisibility(View.GONE);
            }
        });
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (Float) animation.getAnimatedValue();
                float scaleT = (SCREENSHOT_SCALE + mBgPaddingScale)
                        - scaleInterpolator.getInterpolation(t)
                        * (SCREENSHOT_SCALE - SCREENSHOT_DROP_IN_MIN_SCALE);
                mBackgroundView.setAlpha(scaleInterpolator.getInterpolation(t) * BACKGROUND_ALPHA);
                mScreenshotContainer.setAlpha(t);
                mScreenshotContainer.setScaleX(scaleT);
                mScreenshotContainer.setScaleY(scaleT);
                mScreenshotFlash.setAlpha(flashAlphaInterpolator.getInterpolation(t));
            }
        });
        return anim;
    }
    private ValueAnimator createScreenshotDropOutAnimation(int w, int h, boolean statusBarVisible,
                                                           boolean navBarVisible) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setStartDelay(SCREENSHOT_DROP_OUT_DELAY);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isFadeOutAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFadeOutAnimRunning = true;
                mBackgroundView.setVisibility(View.GONE);
                mScreenshotContainer.setVisibility(View.GONE);
                mScreenshotContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        if (!statusBarVisible || !navBarVisible) {
            // There is no status bar/nav bar, so just fade the screenshot away in place
            anim.setDuration(SCREENSHOT_FAST_DROP_OUT_DURATION);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = (Float) animation.getAnimatedValue();
                    float scaleT = (SCREENSHOT_DROP_IN_MIN_SCALE + mBgPaddingScale)
                            - t * (SCREENSHOT_DROP_IN_MIN_SCALE - SCREENSHOT_FAST_DROP_OUT_MIN_SCALE);
                    mBackgroundView.setAlpha((1f - t) * BACKGROUND_ALPHA);
                    mScreenshotContainer.setAlpha(1f - t);
                    mScreenshotContainer.setScaleX(scaleT);
                    mScreenshotContainer.setScaleY(scaleT);
                }
            });
        } else {
            // In the case where there is a status bar, animate to the origin of the bar (top-left)
            final float scaleDurationPct = (float) SCREENSHOT_DROP_OUT_SCALE_DURATION
                    / SCREENSHOT_DROP_OUT_DURATION;
            final Interpolator scaleInterpolator = new Interpolator() {
                public float getInterpolation(float x) {
                    if (x < scaleDurationPct) {
                        // Decelerate, and scale the input accordingly
                        return (float) (1f - Math.pow(1f - (x / scaleDurationPct), 2f));
                    }
                    return 1f;
                }
            };

            // Determine the bounds of how to scale
            float halfScreenWidth = (w - 2f * mBgPadding) / 2f;
            float halfScreenHeight = (h - 2f * mBgPadding) / 2f;
            final float offsetPct = SCREENSHOT_DROP_OUT_MIN_SCALE_OFFSET;
            final PointF finalPos = new PointF(
                    -halfScreenWidth + (SCREENSHOT_DROP_OUT_MIN_SCALE + offsetPct) * halfScreenWidth,
                    -halfScreenHeight + (SCREENSHOT_DROP_OUT_MIN_SCALE + offsetPct) * halfScreenHeight);

            // Animate the screenshot to the status bar
            anim.setDuration(SCREENSHOT_DROP_OUT_DURATION);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    isFadeOutAnimRunning = true;
                    float t = (Float) animation.getAnimatedValue();
                    float scaleT = (SCREENSHOT_DROP_IN_MIN_SCALE + mBgPaddingScale)
                            - scaleInterpolator.getInterpolation(t)
                            * (SCREENSHOT_DROP_IN_MIN_SCALE - SCREENSHOT_DROP_OUT_MIN_SCALE);
                    mBackgroundView.setAlpha((1f - t) * BACKGROUND_ALPHA);
                    mScreenshotContainer.setAlpha(1f - scaleInterpolator.getInterpolation(t));
                    mScreenshotContainer.setScaleX(scaleT);
                    mScreenshotContainer.setScaleY(scaleT);
                    mScreenshotContainer.setTranslationX(t * finalPos.x);
                    mScreenshotContainer.setTranslationY(t * finalPos.y);
                }
            });
        }
        return anim;
    }

    private void initFunctionView(){
        mShareBtn = (TextView)mScreenshotLayout.findViewById(R.id.share);
        mEditBtn = (TextView)mScreenshotLayout.findViewById(R.id.edit);
        mDeleteBtn = (TextView)mScreenshotLayout.findViewById(R.id.delete);
        mShareBtn.setOnClickListener(mFunctionBtnClickListener);
        mEditBtn.setOnClickListener(mFunctionBtnClickListener);
        mDeleteBtn.setOnClickListener(mFunctionBtnClickListener);
    }

    private View.OnClickListener mFunctionBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.share:
                    share();
                    break;
                case R.id.edit:
                    edit();
                    break;
                case R.id.delete:
                    delete();
                    break;
            }
            endAnimation();
        }
    };

    private void share(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
    }

    private void edit(){
        Intent intent = new Intent();
        intent.setAction("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri,"image/*");
        intent.putExtra("scale",true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{
            startActivity(intent);
        }catch (Exception ex){
            Log.e(TAG, "start cut activity err " + ex.getMessage());
        }
    }

    private void delete(){
        if (mImageFilePath != null){
            File file = new File(mImageFilePath);
            if (file.exists()){
                //chenyee zhaocaili 20180728 add for CSW1802A-484 begin
                isDeleting = true;
                removeView(mFullScreenCheckViewRight);
                removeView(mFullScreenCheckViewLeft);
                removeView(mNotificationView);
                //chenyee zhaocaili 20180904 modify for CSW1802A-1553 begin
                CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(getApplicationContext());
                //chenyee zhaocaili 20180904 modify for CSW1802A-1553 end
                builder.setTitle(R.string.delete);
                builder.setMessage(R.string.delete_message);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        file.delete();
                        getContentResolver().delete(imageUri, null, null);
                        dialog.dismiss();
                    }
                });
                //chenyee zhaocaili 20181027 add for CSW1802A-1593 begin
                final CyeeAlertDialog dialog = builder.create();
                //chenyee zhaocaili 20181027 add for CSW1802A-1593 end
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(TAG, "delete dialog dismiss");
                        stopSelfBySelf();
                    }
                });
                dialog.show();
                //chenyee zhaocaili 20180728 add for CSW1802A-484 end
            }
        }
    }

    private void addView(String viewName, View view){
        mLayoutParams = new LayoutParams();
        if (viewName.equals(Constant.NOTIFICATIONVIEW)){
            mLayoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            mLayoutParams.format = PixelFormat.TRANSPARENT;
            mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_FULLSCREEN | mLayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

            mLayoutParams.width = LayoutParams.MATCH_PARENT;
            mLayoutParams.height = getResources().getDimensionPixelSize(R.dimen.notification_view_height);
            mLayoutParams.x = 0;
            mLayoutParams.y = 0;
        }else if (viewName.equals(Constant.FULLVIEW_LEFT)
                || viewName.equals(Constant.FULLVIEW_RIGHT)){
            mLayoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            mLayoutParams.format = PixelFormat.TRANSPARENT;
            mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLayoutParams.height = LayoutParams.MATCH_PARENT;
            if (viewName.equals(Constant.FULLVIEW_RIGHT)){
                mLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
                mLayoutParams.width = mScreenWidth * 3 / 4 - 1;

            }else {
                mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                mLayoutParams.width = mScreenWidth / 4 - 1;
            }
        }else if(viewName.equals(Constant.SCREENSHOTVIEW)){
            mLayoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            mLayoutParams.format = PixelFormat.TRANSLUCENT;
            mLayoutParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

            mLayoutParams.width = LayoutParams.MATCH_PARENT;
            mLayoutParams.height = LayoutParams.MATCH_PARENT;
            mLayoutParams.x = 0;
            mLayoutParams.y = 0;
            mLayoutParams.setTitle("ScreenshotAnimation");
        }

        mWindowManager.addView(view, mLayoutParams);
    }

    private void removeView(View view){
        if (mWindowManager != null && view != null && view.getVisibility() == View.VISIBLE){
            try {
                mWindowManager.removeView(view);
            }catch (Exception e){
                Log.e(TAG, "remove view err :" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setAnimationFlag(boolean flag){
        isNeedAnimation = flag;
    }

    private void endAnimation(){
        if (mScreenshotAnimation != null) {
            if (mScreenshotAnimation.isStarted()) {
                mScreenshotAnimation.end();
            }
            mScreenshotAnimation.removeAllListeners();
        }
    }

    /**发送长截屏开始和结束的广播给SystemUI,禁止在长截屏期间再截屏**/
    private void sendLSStartBroadcast(){
        sendBroadcast(new Intent("com.cydroid.action.LS_RUNNING_START"));
        Log.d(TAG, "send longscreenshot start broadcast");
    }

    private void sendLSEndBroadcast(){
        sendBroadcast(new Intent("com.cydroid.action.LS_RUNNING_END"));
        Log.d(TAG, "send longscreenshot end broadcast");
    }


    private void ensureHandlerExists() {
        mHandlerThread = new HandlerThread("LongScreenShot#", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mScrollHandler = new Handler(mHandlerThread.getLooper());
        mScrollHandler.post(() -> {
            String[] items = ScreenShotService.this.getResources().getStringArray(R.array.longscreenshot_not_support_items);
            mLongScreenshotNotSupportItems.clear();
            for (String item : items) {
                mLongScreenshotNotSupportItems.add(item);
            }
            if (isLongScreenshotNotSupport()) {
                mHandler.obtainMessage(H.MSG_LONGSCREENSHOT_NOT_SUPPORT, 0/*reason*/, 0/*don't care*/, ScreenShotService.this).sendToTarget();
            } else if (isScreenOrientationHorizontal()) {
                mHandler.obtainMessage(H.MSG_LONGSCREENSHOT_NOT_SUPPORT, 1/*reason*/, 0/*don't care*/, ScreenShotService.this).sendToTarget();
            } else if (isInSingleHandMode()) {
                mHandler.obtainMessage(H.MSG_LONGSCREENSHOT_NOT_SUPPORT, 3/*reason*/, 0/*don't care*/, ScreenShotService.this).sendToTarget();
            } else {
                mSaveImageDir = StorageManagerHelper.getSaveImagePath(this);
                if (mSaveImageDir == null) {
                    mHandler.obtainMessage(H.MSG_LONGSCREENSHOT_NOT_SUPPORT, 2/*reason*/, 0/*don't care*/, ScreenShotService.this).sendToTarget();
                } else {
                    mHandler.obtainMessage(H.MSG_LONGSCREENSHOT_SUPPORT, ScreenShotService.this).sendToTarget();
                }
            }
        });
    }

    public static class H extends Handler {
        public static final int MSG_CAPTURE_SCREEN = 0x102;
        public static final int MSG_FAIL_TO_SAVE_PIC = 0x103;
        public static final int MSG_FORBID_SCREEN_SHOT = 0x104;
        public static final int MSG_RUN_FORGROUND = 0x105;
        public static final int MSG_LONGSCREENSHOT_NOT_SUPPORT = 0x106;
        public static final int MSG_LONGSCREENSHOT_SUPPORT = 0x107;
        public static final int MSG_STOP_SERVICE = 0x108;
        public static final int MSG_START_CAPTURE = 0x109;
        public static final int MSG_ADD_NOTIFICATION = 0x110;

        public H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null) return;
            ScreenShotService mService = (ScreenShotService) msg.obj;
            switch (msg.what) {
                case MSG_CAPTURE_SCREEN:
                    mService.handleCaptureMessage();
                    break;
                case MSG_RUN_FORGROUND:
                    mService.handleMsgRunForground();
                    break;
                case MSG_FORBID_SCREEN_SHOT:
                    mService.handleForbidScreenShot();
                    break;
                case MSG_FAIL_TO_SAVE_PIC:
                    mService.handleMsgFailToSavePic();
                    break;
                case MSG_LONGSCREENSHOT_SUPPORT:
                    mService.handleLongScreenshotSupport();
                    break;
                case MSG_LONGSCREENSHOT_NOT_SUPPORT:
                    mService.handleLongScreenshotNotSupport(msg.arg1);
                    break;
                case MSG_STOP_SERVICE:
                    mService.handleStopService();
                    break;
                case MSG_START_CAPTURE:
                    mService.handleStartCapture();
                    break;
                case MSG_ADD_NOTIFICATION:
                    mService.handleAddNotificationView();
                    break;
                default:
                    break;
            }
        }
    }

    public void handleCaptureMessage() {
        // TODO: sleep操作可以用sendEmptyMessageDelayed代替，后台操作更安全
        try {
            //chenyee zhaocaili 20180830 modify for CSW1802A-1437 begin
            Thread.sleep(isWindowTouched? 600 : letScreenFinishScroll);//滑到底，部分界面会有回弹现象，等待回弹结束再截取
            //chenyee zhaocaili 20180830 modify for CSW1802A-1437 end
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "capture screen after scroll...");
        mTempBitmap = captureScreen();
        if (mTempBitmap == null) {
            Log.e(TAG, "mTempBitmap == null");
            sendMyMessage(mHandler, H.MSG_FORBID_SCREEN_SHOT);
            return;
        }
        savePic(mTempBitmap, beforeCutString + mScreencapBitmapCache.size() + 1);
        // Log.v(TAG, "mTempBitmap == null  is " + (mTempBitmap == null ? "true" : "false"));
        Log.v(TAG, "mTempPreBitmap == null  is " + (mTempPreBitmap == null ? "true" : "false"));
        if (isScrollToBottom(mTempPreBitmap, mTempBitmap)) {
            //disableBottomBallDrag();//最后一屏重复，说明用户想继续向下滚动，截取更多图片，所以根据此禁止用户拖动底部的小球
        } else {
            mTempPreBitmap = mTempBitmap;
            addPicToCache(mTempBitmap);//滚完屏，如果不想同，就加入缓存
            addCachedScreenShotPicNum();
            mTempBitmap = null;//释放
        }
        //滚动屏幕完毕，截图完毕，显示覆盖层
        Log.v(TAG, "scroll end, capture end, show cover layer");
        //sendMyMessage(mHandler, H.MSG_SHOW_DRAG_UP_VIEW);
        //滚屏结束，截图完毕，设置底部的小球位置
        // Log.v(TAG, "scroll end,capture end,  setmBallPosition   ---1---" + BallPosition.getmSmallBallPossion());
        BallPosition.setmSmallBallPossion(mScreenHeight);
        // Log.v(TAG, "scroll end,capture end,  setmBallPosition   ---2---" + BallPosition.getmSmallBallPossion());

        AutoCaptureNextScreen();
    }

    /**
     * 让service成为前台service，避免被系统软件管家kill,然后导致禁止屏幕没有恢复
     */
    public void handleMsgRunForground() {
        Log.d(TAG, "---handleMsgRunForground");
        Intent unlockIntent = new Intent();
        unlockIntent.setAction("com.cydroid.action.DISABLE_KEYGUARD");
        //chenyee zhaocaili 20180424 add begin
        mNotificationChannel = new NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
        mNotificationBuilder = new Notification.Builder(getApplicationContext(), mChannelId);
        //chenyee zhaocaili 20180424 add end
        mNotificationBuilder.setContentTitle(getResources().getString(R.string.long_screen_shot_run_foreground))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(PendingIntent.getBroadcast(mContext, 0, unlockIntent, 0))
                .setAutoCancel(true);
        Notification n = mNotificationBuilder.build();
        n.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(mNotificationId_runForeground, n);
        //Gionee <gn_by_LongScreenShot> <liuweiming> <20170414> add for #79540 end
        //addView(Constant.NOTIFICATIONVIEW, mNotificationView);
        sendMyMessage(mHandler, H.MSG_START_CAPTURE);
    }

    public void handleForbidScreenShot() {
        Log.v(TAG, "forbidScreenShot");
        mNotificationManager.cancel(mNotificationId);//取消之前的通知。
        myToast(R.string.long_screen_shot_forbided);
        stopSelfNormally();
    }

    public void handleMsgFailToSavePic() {
        Log.v(TAG, "handleMsgFailToSavePic");
        myToast(R.string.long_screen_shot_fail_to_generate_pic);
        mNotificationManager.cancel(mNotificationId);
        stopSelfBySelf();
    }

    public void handleLongScreenshotNotSupport(int reason) {
        int id = R.string.current_ui_not_support;
        switch (reason){
            case 0:
                id = R.string.current_ui_not_support;
                break;
            case 1:
                id = R.string.long_screen_shot_not_surport_landscape;
                break;
            case 2:
                id = R.string.storage_has_no_space;
                break;
            case 3:
                id = R.string.long_screen_shot_single_hand_not_surport;
                break;
        }
        removeView(mNotificationView);
        myToast(id);
        mScrollHandler.removeCallbacksAndMessages(null);
        mHandlerThread.quit();
        mHandlerThread = null;
        mLongScreenshotNotSupportItems.clear();
        stopSelf();
    }

    public void handleStopService() {
        stopSelfBySelf();
    }

    public void handleLongScreenshotSupport() {
        registReceivers();
        initParameters();
        printPhoneInfo();
        initFullScreenViews();
        initNotificationView();
        initDisplayView();
        initFunctionView();
        mNotificationManager.cancel(mNotificationId);//取消之前的通知。
        // Chenyee xionghg 20180111 add for CSW1702A-2364 begin
        mFloatingTouchHelper = new FloatingTouchHelper(getApplicationContext());
        mFloatingTouchHelper.saveFloatingTouchState();
        // Chenyee xionghg 20180111 add for CSW1702A-2364 end
        Log.d(TAG, "-----init environment end");
        sendMyMessage(mHandler, H.MSG_RUN_FORGROUND);
    }

    public void handleAddNotificationView(){
        addView(Constant.NOTIFICATIONVIEW, mNotificationView);
    }

    public void handleStartCapture(){
        prepareNextScreen();
        mScrollHandler.post(mNextScreenAction);
    }

    private void AutoCaptureNextScreen(){
        if (mCachedScreenShotPicNum == 10) {
            Log.d(TAG, " mCachedScreenShotPicNum==10 ");
            isAlreadyTenPagesSavedToCache = true;
            myToast(R.string.long_screen_shot_can_not_scroll_any_more);
            if (!StorageManagerHelper.isAvailableForSpecifyVolumePath(mContext, mSaveImageDir)) {
                myToast(R.string.storage_is_full);
                stopSelfBySelf();
                return;
            }
            if (!isSavingPicture) {
                Log.v(TAG, "real to save pic");
                saveAndPlaySound();
            }
        }else if (isWindowTouched || isScrolledToBottom){
            if (!isSavingPicture) {
                Log.v(TAG, "real to save pic");
                saveAndPlaySound();
            }
        }else {
            mScrollHandler.post(mNextScreenAction);
        }
    }

    private final Runnable mNextScreenAction = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mNextScreenAction-run() start.");
            if (isAlreadyTenPagesSavedToCache) {
                return;
            }
            if (isFirstPic) {//第一张图 特殊处理
                isFirstPic = false;
                Log.d(TAG, "is first picture, mScreenShotPicNum = 1");
                mTempBitmap = captureScreen();
                if (mTempBitmap == null) {
                    //第一张图  此界面可能被禁止截屏了
                    Log.e(TAG, "is first picture, mTempBitmap == null, other app may forbidden capture screen now");
                    mHandler.removeMessages(H.MSG_FORBID_SCREEN_SHOT);
                    mHandler.obtainMessage(H.MSG_FORBID_SCREEN_SHOT, ScreenShotService.this).sendToTarget();
                    return;
                }
                savePic(mTempBitmap, beforeCutString + 0);
                addPicToCache(mTempBitmap);//添加第一张图
                addCachedScreenShotPicNum();
                mTempPreBitmap = mTempBitmap;//保存为临时图片，用来和后面的图片对比
                mScrollHandler.post(() -> scrollScreen());
                BallPosition.setmSmallBallPossion(mScreenHeight);
            } else if (mCachedScreenShotPicNum > 1) {
                Log.v(TAG, "not first picture     mScreenShotPicNum >1");
                mScrollHandler.post(() -> scrollScreen());
            }
        }
    };
}
