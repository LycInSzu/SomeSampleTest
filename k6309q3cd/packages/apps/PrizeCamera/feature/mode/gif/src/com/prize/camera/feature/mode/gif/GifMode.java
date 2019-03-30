package com.prize.camera.feature.mode.gif;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.storage.MediaSaver;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import com.prize.camera.feature.mode.gif.IGifDeviceController.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class GifMode extends CameraModeBase implements JpegCallback,DeviceCallback,PreviewSizeCallback,GifMerge.GifCallback,GifViewController.OnGifStopClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(GifMode.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";

    private static final String JPEG_CALLBACK = "jpeg callback";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";
    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";

    private Activity mActivity;
    private String mCameraId;
    private ModeMainHandler mModeMainHandler;
    private GifModeHelper mGifModeHelper;
    private IGifDeviceController mIDeviceController;
    private ISettingManager mISettingManager;
    private volatile boolean mIsResumed = false;
    private byte[] mPreviewData;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Object mPreviewDataSync = new Object();
    private HandlerThread mAnimationHandlerThread;
    private Handler mAnimationHandler;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;
    private StatusMonitor.StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IAppUiListener.ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private GifViewController mGifViewController;
    private static final String KEY_SCREEN_FLASH = "key_screen_flash";
    private GifMerge mGifMerge;
    private static final String IMAGE_FORMAT = "'IMG'_yyyyMMdd_HHmmss_S";
    private static final String PIC_NAME_END = ".gif";
    private static final int PIC_COUNT = 20;   // picture count of gif
    private int mGifMergedNum;
    private static final int MSG_GIF_PROCESS = 10010;
    private static final int MSG_SHOW_GIF_PROGRESS = 10011;
    private static final int PROCESS_DELAY_TIME = 10;

    private static final int GIF_WIDTH_SCREEN_16_9 = 288;
    private static final int GIF_HEIGHT_SCREEN_16_9 = 512;
    private static final int GIF_WIDTH_SCREEN_4_3 = 480;
    private static final int GIF_HEIGHT_SCREEN_4_3 = 640;
    private int mGifWidth = GIF_WIDTH_SCREEN_4_3;
    private int mGifHeight = GIF_HEIGHT_SCREEN_4_3;
    public static final double ASPECT_TOLERANCE = 0.03;
    private static final long MAX_DUAL_TIME = 6*1000;
    private long mStartTime = 0;
    private long mProcessedTime = 0;





    private MediaSaver.MediaSaverListener mMediaSaverListener = new MediaSaver.MediaSaverListener() {

        @Override
        public void onFileSaved(Uri uri) {
            mIApp.notifyNewMedia(uri, true);
        }
    };


    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        super.init(app, cameraContext, isFromLaunch);
        LogHelper.d(TAG, "[init]+");
        mModeMainHandler = new ModeMainHandler(Looper.myLooper());
        mActivity = mIApp.getActivity();
        //mIApp.getAppUi().registerGestureListener(this, GESTURE_PRIORITY);
        mGifModeHelper = new GifModeHelper(cameraContext);
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        DeviceControllerFactory deviceControllerFactory = new DeviceControllerFactory();
        mIDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        initGifView();
        prepareAndOpenCamera(mCameraId);
        createAnimationHandler();
        LogHelper.d(TAG, "[init]-");
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        LogHelper.d(TAG, "[resume]+");
        super.resume(deviceUsage);
        mIsResumed = true;
        mIDeviceController.queryCameraDeviceManager();
        //mGifViewController.showView();
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(mCameraId);
        LogHelper.d(TAG, "[resume]-");
    }

    @Override
    public void pause(@Nonnull DeviceUsage nextModeDeviceUsage) {
        LogHelper.d(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        stopGifCaputre();
        mIsResumed = false;
        mGifViewController.uninit();
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else {
            clearAllCallbacks(mCameraId);
            mIDeviceController.stopPreview();
        }
        LogHelper.d(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        LogHelper.i(TAG, "[unInit]+");
        super.unInit();
        mGifViewController.uninit();
        destroyAnimationHandler();
        mIDeviceController.destroyDeviceController();
        LogHelper.i(TAG, "[unInit]-");
    }

    private void initGifView() {
        mGifViewController = new GifViewController();
        mGifViewController.init(mIApp,mCameraId,mICameraContext);
        mGifViewController.setOnGifStopClickListener(this);
    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mPhotoStatusResponder = statusMonitor.getStatusResponder(KEY_PHOTO_CAPTURE);
    }

    private void initSettingManager(String cameraId) {
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(cameraId,getModeKey(),ModeType.PHOTO,mCameraApi);
    }


    private void prepareAndOpenCamera(String cameraId){
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE,mStatusChangeListener);
        if ("1".equals(cameraId)){
            statusMonitor.registerValueChangedListener(KEY_SCREEN_FLASH, mScreenFlashChangeListener);
        }
        setFlashWillOn(false);
        //before open camera, prepare the device callback and size changed callback.
        mIDeviceController.setDeviceCallback(this);
        mIDeviceController.setPreviewSizeReadyCallback(this);
        //prepare device info.
        DeviceInfo info = new DeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        mIDeviceController.openCamera(info);

    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIDeviceController.closeCamera(needSync);
        //reset the preview size and preview data.
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mIDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(KEY_SCREEN_FLASH, mScreenFlashChangeListener);
    }

    private void recycleSettingManager(String cameraId) {
        mICameraContext.getSettingManagerFactory().recycle(cameraId);
    }


    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId);
        //first need check whether can switch camera or not.
        if (canSelectCamera(newCameraId)) {
            //trigger switch camera animation in here
            //must before mCamera = newCameraId, otherwise the animation's orientation and
            // whether need mirror is error.
            synchronized (mPreviewDataSync) {
                startSwitchCameraAnimation();
            }
            doCameraSelect(mCameraId, newCameraId);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
        if (mGifViewController != null){
            mGifViewController.setCurrentCameraId(cameraId);
            //mGifViewController.showView();
        }
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
        if (mGifViewController != null){
            mGifViewController.uninit();
        }

    }

    @Override
    public void afterStopPreview() {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {

        LogHelper.d(TAG, "[onPreviewCallback]");
        synchronized (mPreviewDataSync) {
            //Notify preview started.
            mIApp.getAppUi().applyAllUIEnabled(true);
            mIApp.getAppUi().onPreviewStarted(mCameraId);
            if (mPreviewData == null) {
                stopAllAnimations();
            }
            updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);

            mPreviewData = data;
            mPreviewFormat = format;
        }
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        return true;
    }

    @Override
    protected boolean doShutterButtonClick() {
        //Storage case
        boolean storageReady = mICameraContext.getStorageService().getCaptureStorageSpace() > 0;
        boolean isDeviceReady = mIDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed) {
            //trigger capture animation
            //LAVA_EDIT modified by jiangym at 20181113 for screen flash start
            if (needDoScreenFlash()){
                mIApp.getAppUi().updateScreenView(true);
                mModeHandler.removeMessages(SCREEN_FLASH_VIEW_HIDE);
                mModeHandler.sendEmptyMessageDelayed(SCREEN_FLASH_TAKE_PICTRE,600);
            }else {
                takePictrue();
            }
            //LAVA_EDIT modified by jiangym at 20181113 for screen flash end

        }
        return true;
    }

    //LAVA_EDIT add by jiangym at 20181113 for screen flash start
    @Override
    protected void takePictrue(){
        startCaptureAnimation();
        //mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
        updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
        disableAllUIExceptionShutter();
        mIDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
        if(null != mGifMerge){
            mGifMerge.exitCapture();
            mGifMerge = null;
        }
        long dateTaken = System.currentTimeMillis();
        Date date = new Date(dateTaken);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(IMAGE_FORMAT);
        String fileName = simpleDateFormat.format(date) + PIC_NAME_END;
        String path = mICameraContext.getStorageService().getFileDirectory() + "/"+ fileName;

        if (mGifMerge == null){
            mGifMerge = new GifMerge(mIApp,path,fileName, /*mLavaGifViewManager.getSpeed()*/20,
                    PIC_COUNT, mGifWidth, mGifHeight,GifMode.this,mICameraContext.getLocation());
            new Thread(mGifMerge).start();
            mModeMainHandler.sendEmptyMessageDelayed(MSG_GIF_PROCESS, PROCESS_DELAY_TIME);
            mStartTime = System.currentTimeMillis();
            mGifViewController.showView();

        }

       // mIDeviceController.takePicture(this);
        /*if (needDoScreenFlash()){
            mModeHandler.sendEmptyMessageDelayed(SCREEN_FLASH_VIEW_HIDE,1000);
        }*/
    }
    //LAVA_EDIT modified by jiangym at 20181113 for screen flash end

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public void onDataReceived(byte[] data) {
        LogHelper.d(TAG, "[onDataReceived] data = " + data);
        CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        mIApp.getAppUi().updateScreenView(false);//LAVA_EDIT add by jiangym at 20181113 for screen flash
        if (data != null) {
            saveData(data);
            updateThumbnail(data);
        }

        if (mIsResumed && mCameraApi == CameraDeviceManagerFactory.CameraApi.API1) {
            mIDeviceController.startPreview();
        }
        CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        //reset the switch camera to null
        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
    }


    @Override
    protected ISettingManager getSettingManager() {
        return mISettingManager;
    }

    private void onPreviewSizeChanged(int width, int height){

        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth,mPreviewHeight,mISurfaceStatusListener);
        LogHelper.d(TAG,"onPreviewSizeChanged mPreviewWidth = " + mPreviewWidth + ", mPreviewHeight = " + mPreviewHeight + ", ratio = " + mIApp.getAppUi().getPreviewAspectRatio());

        double ratio = (double)mPreviewWidth/mPreviewHeight;
        LogHelper.d(TAG,"onPreviewSizeChanged 11mPreviewWidth = " + mPreviewWidth + ", mPreviewHeight = " + mPreviewHeight + ",ratio = " + ratio);
        if (Math.abs(ratio - 4d/3) < ASPECT_TOLERANCE){
            mGifHeight = GIF_HEIGHT_SCREEN_4_3;
            mGifWidth = GIF_WIDTH_SCREEN_4_3;
        }else if (Math.abs(ratio - 16d/9) < ASPECT_TOLERANCE){
            mGifWidth = GIF_WIDTH_SCREEN_16_9;
            mGifHeight = GIF_HEIGHT_SCREEN_16_9;
        }else {
            mGifWidth = mPreviewHeight/4;
            mGifHeight = mPreviewWidth/4;
        }
    }

    private void updatePictureSizeAndPreviewSize(Size previewSize) {
        ISettingManager.SettingController controller = mISettingManager.getSettingController();
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            int captureWidth = Integer.parseInt(pictureSizes[0]);
            int captureHeight = Integer.parseInt(pictureSizes[1]);
            mIDeviceController.setPictureSize(new Size(captureWidth, captureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + captureWidth +
                    " X" + captureHeight + ",current preview size:" + mPreviewWidth + " X " +
                    mPreviewHeight + ",new value :" + width + " X " + height);
            if (width != mPreviewWidth || height != mPreviewHeight) {
                onPreviewSizeChanged(width, height);
            }
        }

    }

    @Override
    public void onPreviewSizeReady(Size previewSize) {
        updatePictureSizeAndPreviewSize(previewSize);
    }

    @Override
    public void onInitComplete(boolean result) {

    }

    @Override
    public void onGifMergeComplete(boolean result, int piccount) {
        LogHelper.d(TAG, "onGifMergeComplete result="+result+" piccount="+piccount);

        if(!result){
            stopGifCaputre();
           /* mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(mActivity, R.string.camera_wideself_takepicture_fail, Toast.LENGTH_SHORT).show();
                }
            });*/
            return;
        }

        Message msg = new Message();
        msg.what = MSG_SHOW_GIF_PROGRESS;
        msg.obj = mProcessedTime;
        mModeMainHandler.sendMessage(msg);

        mGifMergedNum += 1;
        mProcessedTime = System.currentTimeMillis()-mStartTime;
        mGifMerge.updateProcessTime(mProcessedTime);
        if (mProcessedTime < MAX_DUAL_TIME){
            if (!mModeMainHandler.hasMessages(MSG_GIF_PROCESS)){
                mModeMainHandler.sendEmptyMessageDelayed(MSG_GIF_PROCESS, PROCESS_DELAY_TIME);
            }
        }else {
            stopGifCaputre();
        }

        /*if(mGifMergedNum == PIC_COUNT){
            stopGifCaputre();
            *//*if(isSoundOn()){
                mCanPlaySound = true;
//                mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
            }*//*
        }else{
            if (!mModeMainHandler.hasMessages(MSG_GIF_PROCESS)){
                mModeMainHandler.sendEmptyMessageDelayed(MSG_GIF_PROCESS, PROCESS_DELAY_TIME);
            }
        }*/
    }

    @Override
    public void onSaveComplete(boolean success) {
        mICameraContext.getSoundPlayback().play(com.mediatek.camera.common.sound.ISoundPlayback.SHUTTER_CLICK);
        mIApp.getAppUi().applyAllUIEnabled(true);
        mIApp.getAppUi().onPreviewStarted(mCameraId);
        updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);

    }

    private void stopGifCaputre(){
        if (mGifMerge != null){
            mGifMerge.exitCapture();
        }
        mModeMainHandler.removeMessages(MSG_GIF_PROCESS);
        mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
        mIApp.getAppUi().applyAllUIEnabled(true);
        mIApp.getAppUi().onPreviewStarted(mCameraId);
        mGifViewController.uninit();
        if (mPreviewData == null) {
            stopAllAnimations();
        }
        mGifMergedNum = 0;
        mProcessedTime = 0;
        updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
    }

    private void processGifCaputre(){
        LogHelper.d(TAG,"processGifCaputre mGifMergedNum = " + mGifMergedNum + ", ProcessTime = " + mProcessedTime);
        if (/*mProcessedTime < MAX_DUAL_TIME &&*/mGifMerge != null){
            Bitmap bitmap = getPreviewBitmap(mGifWidth,mGifHeight);
            if (bitmap != null){
                mGifMerge.addBitmap(bitmap);
            }
        }
    }

    private void updateMergeProcess(long time){
        if (mGifViewController != null){
            mGifViewController.updateProgress(time);
        }
    }

    @Override
    public void onGifStopClick() {
        if (mProcessedTime != 0){
            stopGifCaputre();
        }
    }

    /**
     * Handler to handle message which comes from device controller and setting manager.
     */
    private final class ModeMainHandler extends Handler {

        public ModeMainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_GIF_PROCESS:
                    processGifCaputre();
                    break;
                case MSG_SHOW_GIF_PROGRESS:
                    updateMergeProcess(mProcessedTime);
                    break;
                default:
                    break;
            }
        }
    }

    private class MyStatusChangeListener implements StatusMonitor.StatusChangeListener{


        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG,"[onStatusChanged] key = " + key + ", value =" + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)){
                String sizes [] = value.split("x");
                int captureWidth = Integer.parseInt(sizes[0]);
                int captureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(captureWidth,captureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) captureWidth/captureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight){
                    onPreviewSizeChanged(width, height);
                }
            }

        }
    }

    private class SurfaceChangeListener implements IAppUiListener.ISurfaceStatusListener{

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {

            LogHelper.d(TAG, "[surfaceAvailable] device controller = " + mIDeviceController
                    + ",mIsResumed = " + mIsResumed + ",w = " + width + ",h = " + height);
            int waitCount = 0;
            while (!mIsResumed&&waitCount< 20){
                try {
                    LogHelper.d(TAG, "[surfaceAvailable] wait waitCount = " + waitCount);
                    waitCount ++;
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            waitCount = 0;
            while (mIsResumed&&!MODE_DEVICE_STATE_OPENED.equals(getModeDeviceStatus())&&waitCount< 20){
                try {
                    LogHelper.d(TAG, "[surfaceAvailable] camera not open wait waitCount = " + waitCount);
                    waitCount ++;
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "[surfaceChanged] device controller = " + mIDeviceController
                    + ",mIsResumed = " + mIsResumed + ",w = " + width + ",h = " + height);
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "[surfaceDestroyed] device controller = " + mIDeviceController
                    + ",mIsResumed = " + mIsResumed + ",w = " + width + ",h = " + height);
        }
    }
    private void startSwitchCameraAnimation() {
        // Prepare the animation data.
        IAppUi.AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
                mPreviewHeight, mPreviewFormat);
        // Trigger animation start.
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_SWITCH_CAMERA, data);
    }

    private void stopSwitchCameraAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_CAMERA);
    }

    private void startChangeModeAnimation() {
        IAppUi.AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
                mPreviewHeight, mPreviewFormat);
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_SWITCH_MODE, data);
    }

    private void stopChangeModeAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_MODE);
    }

    private void startCaptureAnimation() {
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
    }

    private void stopCaptureAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_CAPTURE);
    }

    private void disableAllUIExceptionShutter() {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_BUTTON, false);
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_TEXT, false);
        mIApp.getAppUi().applyAllUIVisibility(View.GONE);
    }

    private void saveData(byte[] jpegData) {
        LogHelper.d(TAG, "[saveData]");
        if (jpegData != null) {
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromExif(jpegData);
            ContentValues contentValues = mGifModeHelper.createContentValues(jpegData,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            mICameraContext.getMediaSaver().addSaveRequest(jpegData, contentValues, null,
                    mMediaSaverListener, ImageFormat.JPEG);
        }
        //reset the switch camera to null
        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
    }

    private void updateThumbnail(byte[] jpegData) {
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(jpegData, mIApp.getAppUi()
                .getThumbnailViewWidth());
        mIApp.getAppUi().updateThumbnail(bitmap);
    }

    private void doCameraSelect(String oldCamera, String newCamera) {
        LogHelper.i(TAG, "[doCameraSelect]+");
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().onCameraSelected(newCamera);
        prePareAndCloseCamera(true, oldCamera);
        recycleSettingManager(oldCamera);
        initSettingManager(newCamera);
        prepareAndOpenCamera(newCamera);
        LogHelper.i(TAG, "[doCameraSelect]-");
    }

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        boolean value = true;

        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)) {
            value = false;
        }
        LogHelper.d(TAG, "[canSelectCamera] +: " + value);
        return value;
    }

    private void stopAllAnimations() {
        LogHelper.d(TAG, "[stopAllAnimations]");
        if (mAnimationHandler == null) {
            return;
        }
        //clear the old one.
        mAnimationHandler.removeCallbacksAndMessages(null);
        mAnimationHandler.post(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(TAG, "[stopAllAnimations] run");
                //means preview is started, so need notify switch camera animation need stop.
                stopSwitchCameraAnimation();
                //need notify change mode animation need stop if is doing change mode.
                stopChangeModeAnimation();
                //stop the capture animation if is doing capturing.
                stopCaptureAnimation();
            }
        });
    }

    private void createAnimationHandler() {
        LogHelper.i(TAG, "[createAnimationHandler]+");
        mAnimationHandlerThread = new HandlerThread("Animation_handler");
        mAnimationHandlerThread.start();
        mAnimationHandler = new Handler(mAnimationHandlerThread.getLooper());
        LogHelper.i(TAG, "[createAnimationHandler]-");
    }

    private void destroyAnimationHandler() {
        LogHelper.i(TAG, "[destroyAnimationHandler]+");
        if (mAnimationHandlerThread.isAlive()) {
            mAnimationHandlerThread.quit();
            mAnimationHandler = null;
        }
        LogHelper.i(TAG, "[destroyAnimationHandler]-");
    }


    private IAppUi.AnimationData prepareAnimationData(byte[] data, int width, int height, int format) {
        LogHelper.d(TAG, "[prepareAnimationData] +");
        // Prepare the animation data.
        IAppUi.AnimationData animationData = new IAppUi.AnimationData();
        animationData.mData = data;
        animationData.mWidth = width;
        animationData.mHeight = height;
        animationData.mFormat = format;
        animationData.mOrientation = mGifModeHelper.getCameraInfoOrientation(mCameraId,
                mIApp.getActivity());
        animationData.mIsMirror = mGifModeHelper.isMirror(mCameraId, mIApp.getActivity());
        return animationData;
    }

    private StatusMonitor.StatusChangeListener mScreenFlashChangeListener = new StatusMonitor
            .StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            if (KEY_SCREEN_FLASH.equals(key)){
                if ("screen".equals(value)&&"1".equals(mCameraId)){
                    setFlashWillOn(true);
                }else {
                    setFlashWillOn(false);
                }
            }
        }
    };

    public Bitmap getPreviewBitmap(int width, int heigit){

        return mIApp.getAppUi().getPreviewBitmap(width,heigit);
    }

}
