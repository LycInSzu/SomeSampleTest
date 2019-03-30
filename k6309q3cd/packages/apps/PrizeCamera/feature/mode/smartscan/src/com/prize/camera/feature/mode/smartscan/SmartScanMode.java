package com.prize.camera.feature.mode.smartscan;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;
import com.prize.camera.feature.mode.smartscan.decoding.DecodingRunnable;
import com.prize.camera.feature.mode.smartscan.util.BitmapUtil;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.prize.camera.feature.mode.smartscan.ISmartScanDeviceController.JpegCallback;
import com.prize.camera.feature.mode.smartscan.ISmartScanDeviceController.DeviceCallback;
import com.prize.camera.feature.mode.smartscan.ISmartScanDeviceController.PreviewSizeCallback;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.storage.MediaSaver;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.R;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class SmartScanMode extends CameraModeBase implements JpegCallback, DeviceCallback, PreviewSizeCallback, DecodingRunnable.DecodingResultListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SmartScanMode.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";

    private static final String JPEG_CALLBACK = "jpeg callback";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";
    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";

    private Activity mActivity;
    private String mCameraId;
    private ModeMainHandler mModeMainHandler;
    private SmartScanModeHelper mModeHelper;
    private ISmartScanDeviceController mIDeviceController;
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
    private SmartScanViewController mViewController;
    private static final String KEY_SCREEN_FLASH = "key_screen_flash";
    private DecodingRunnable mDecodingRunable;
    private final static int MSG_DECODING_BITMAP = 1000;
    private static final int REQUEST_CODE_SCAN_GALLERY = 100;
    private boolean isScanFromGallery = false;

    private View.OnClickListener mGoGalleryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
            innerIntent.setType("image/*");
            mActivity.startActivityForResult(innerIntent, REQUEST_CODE_SCAN_GALLERY);
        }
    };
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
        mModeHelper = new SmartScanModeHelper(cameraContext);
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        DeviceControllerFactory deviceControllerFactory = new DeviceControllerFactory();
        mIDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();
        initSmartScanView();
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
        mViewController.showView();
        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(mCameraId);
        LogHelper.d(TAG, "[resume]-");
    }

    @Override
    public void pause(@Nonnull DeviceUsage nextModeDeviceUsage) {
        LogHelper.d(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        mDecodingRunable.stopScan();
        mDecodingRunable = null;
        mViewController.uninit();
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
        mViewController.uninit();
        destroyAnimationHandler();
        mIDeviceController.destroyDeviceController();
        LogHelper.i(TAG, "[unInit]-");
    }

    private void initSmartScanView() {
        mViewController = new SmartScanViewController();
        mViewController.init(mIApp, mCameraId, mICameraContext, mGoGalleryClickListener);
    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mPhotoStatusResponder = statusMonitor.getStatusResponder(KEY_PHOTO_CAPTURE);
    }

    private void initSettingManager(String cameraId) {
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(cameraId, getModeKey(), ModeType.PHOTO, mCameraApi);
    }


    private void prepareAndOpenCamera(String cameraId) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        if ("1".equals(cameraId)) {
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
        //initPortraitType();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN_GALLERY:
                    handleAlbumPic(data);
                    break;
            }
        }
    }


    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
        if (mViewController != null) {
            mViewController.showView();
            drawViewfinder();
        }
        if (mDecodingRunable == null) {
            mDecodingRunable = new DecodingRunnable();
            new Thread(mDecodingRunable).start();
            mDecodingRunable.setDecodingResultListener(this);
            mModeMainHandler.sendEmptyMessageDelayed(MSG_DECODING_BITMAP, 500);
        }
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
        if (mViewController != null) {
            mViewController.uninit();
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
            //mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_BUTTON,false);
            //mIApp.getAppUi().setUIVisibility(IAppUi.THUMBNAIL,View.INVISIBLE);
            mIApp.getAppUi().setSettingIconVisible(false);


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

        return true;
    }

    @Override
    protected void takePictrue() {

    }

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
        mIApp.getAppUi().updateScreenView(false);
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
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mISettingManager;
    }

    private void onPreviewSizeChanged(int width, int height) {

        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
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
    public void onDecodeFail() {
        if (isScanFromGallery) {
            isScanFromGallery = false;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, mActivity.getString(R.string.smart_scan_fail), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (mModeMainHandler.hasMessages(MSG_DECODING_BITMAP)) {
            mModeMainHandler.removeMessages(MSG_DECODING_BITMAP);
        }
        mModeMainHandler.sendEmptyMessageDelayed(MSG_DECODING_BITMAP, 500);
    }

    @Override
    public void onDecodeSuccess(Result result) {
        handleDecode(result);
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
            LogHelper.d(TAG, "[handleMessage] " + msg.what);
            switch (msg.what) {
                case MSG_DECODING_BITMAP:
                    decodeBitmap();
                    break;
                default:
                    break;
            }
        }
    }

    private class MyStatusChangeListener implements StatusMonitor.StatusChangeListener {


        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ", value =" + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String sizes[] = value.split("x");
                int captureWidth = Integer.parseInt(sizes[0]);
                int captureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(captureWidth, captureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) captureWidth / captureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            }

        }
    }

    private class SurfaceChangeListener implements IAppUiListener.ISurfaceStatusListener {

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {

            LogHelper.d(TAG, "[surfaceAvailable] device controller = " + mIDeviceController
                    + ",mIsResumed = " + mIsResumed + ",w = " + width + ",h = " + height);
            int waitCount = 0;
            while (!mIsResumed && waitCount < 20) {
                try {
                    LogHelper.d(TAG, "[surfaceAvailable] wait waitCount = " + waitCount);
                    waitCount++;
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            waitCount = 0;
            while (mIsResumed && !MODE_DEVICE_STATE_OPENED.equals(getModeDeviceStatus()) && waitCount < 20) {
                try {
                    LogHelper.d(TAG, "[surfaceAvailable] camera not open wait waitCount = " + waitCount);
                    waitCount++;
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
    }

    private void saveData(byte[] jpegData) {
        LogHelper.d(TAG, "[saveData]");
        if (jpegData != null) {
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromExif(jpegData);
            ContentValues contentValues = mModeHelper.createContentValues(jpegData,
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
        //mIApp.getAppUi().applyAllUIEnabled(false);
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
        animationData.mOrientation = mModeHelper.getCameraInfoOrientation(mCameraId,
                mIApp.getActivity());
        animationData.mIsMirror = mModeHelper.isMirror(mCameraId, mIApp.getActivity());
        return animationData;
    }

    private StatusMonitor.StatusChangeListener mScreenFlashChangeListener = new StatusMonitor
            .StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            if (KEY_SCREEN_FLASH.equals(key)) {
                if ("screen".equals(value) && "1".equals(mCameraId)) {
                    setFlashWillOn(true);
                } else {
                    setFlashWillOn(false);
                }
            }
        }
    };

    private void handleAlbumPic(Intent data) {
        if (data != null) {
            isScanFromGallery = true;
            mModeMainHandler.removeMessages(MSG_DECODING_BITMAP);
            Uri uri = data.getData();
            Bitmap scanBitmap = BitmapUtil.decodeUri(mIApp.getActivity(), uri, 500, 500);
            if (scanBitmap != null) {

                if (mDecodingRunable == null) {
                    mDecodingRunable = new DecodingRunnable();
                    new Thread(mDecodingRunable).start();
                    mDecodingRunable.setDecodingResultListener(this);
                }
                mDecodingRunable.addDecodeBitmap(scanBitmap);
            }
        }

    }

    private void decodeBitmap() {
        Bitmap bitmap = getTextureBitmap();
        if (bitmap != null && mDecodingRunable != null) {
            mDecodingRunable.addDecodeBitmap(bitmap);
        }
    }

    private Bitmap getTextureBitmap() {
        return mIApp.getAppUi().getPreviewBitmap(mPreviewHeight, mPreviewWidth);
    }

    public void handleDecode(Result result) {
        if (isScanFromGallery) {
            isScanFromGallery = false;
        }
        String resultString = result.getText();
        LogHelper.d(TAG, "handleDecode resultString = " + resultString);
        accessCodeInfo(resultString);
        //FIXME
    }

    public void drawViewfinder() {
        if (mViewController != null) {
            mViewController.drawViewfinder();
        }

    }

    private void accessCodeInfo(String info) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        String infoNew = info.replace("HTTP:", "http:");
        if (!isNetworkAdd(info)) {
            infoNew = "http://www.google.com?search =" + info;
        }
        LogHelper.i(TAG, "[accessCodeInfo info = " + infoNew);
        Uri content_url = Uri.parse(infoNew);
        intent.setData(content_url);
        try {
            mIApp.getActivity().startActivity(intent);
        } catch (Exception e) {
            /*e.printStackTrace();
            copyCodeInfo(info);
            Toast.makeText(mCameraActivity, mCameraActivity.getString(R.string.scan_msg_url_invalid), Toast.LENGTH_LONG)
                    .show();*/
        }
    }

    private boolean isNetworkAdd(String resultStr) {
        String patternStr = "\\b(([\\w-]+://?|www[.])[^\\s()<>]+(?:\\([\\w\\d]+\\)|([^[:punct:]\\s]|/)))";
        Pattern exp = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        if (exp.matcher(resultStr).matches()) {
            return true;
        }
        return false;
    }
}
