package com.prize.camera.feature.mode.filter;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManager;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraOpenException;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.mode.Device2Controller;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class FilterDevice2Controller extends Device2Controller implements IFilterDeviceController ,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.filter.FilterDevice2Controller.class.getSimpleName());
    private static final int CAPTURE_FORMAT = ImageFormat.JPEG;
    private static final int CAPTURE_MAX_NUMBER = 2;
    private static final int WAIT_TIME = 5;

    private final Activity mActivity;
    private CameraCharacteristics mCameraCharacteristics;
    private final CameraManager mCameraManager;
    private final CaptureSurface mCaptureSurface;
    private final ICameraContext mICameraContext;
    private final Camera2Proxy.StateCallback mDeviceCallback = new DeviceStateCallback();
    private Object mSurfaceObject;
    private final Lock mLockState = new ReentrantLock();
    private final Object mSurfaceHolderSync = new Object();

    private ISettingManager mSettingManager;
    private ISettingManager.SettingDevice2Configurator mSettingDevice2Configurator;
    private ISettingManager.SettingController mSettingController;

    private int mJpegRotation;
    private String mCurrentCameraId;
    private CameraDeviceManager mCameraDeviceManager;
    private volatile Camera2Proxy mCamera2Proxy;
    private Surface mPreviewSurface;
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;
    private volatile Camera2CaptureSessionProxy mSession;

    private JpegCallback mJpegCallback;
    private DeviceCallback mModeDeviceCallback;
    private PreviewSizeCallback mPreviewSizeCallback;

    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;
    private boolean mFirstFrameArrived = false;
    private boolean mIsPictureSizeChanged = false;
    private final Lock mDeviceLock = new ReentrantLock();
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_SHUTTER_SPEED = "key_shutter_speed";

    private volatile boolean mIsSessionAbortCalled = false;
    private int mCurrentValue = -1;
    private CaptureRequest.Builder mBuilder = null;
    private SurfaceTexture mFilterSurfaceTexture = null;

    private PreviewCallback mPreviewCallback;

    /**
     * The enum is used for tag native camera state.
     */
    private enum CameraState {
        CAMERA_UNKNOWN, //initialize state.
        CAMERA_OPENING, //between open camera and open done callback.
        CAMERA_OPENED, //when camera open done.
        CAMERA_CLOSING,
    }

    public FilterDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context){

        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mCaptureSurface = new CaptureSurface();
        mCaptureSurface.setCaptureCallback(this);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraDeviceManagerFactory.CameraApi.API2);
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraDeviceManagerFactory.CameraApi.API2);
    }

    @Override
    public void openCamera(DeviceInfo info) {
        String cameraId = info.getCameraId();
        LogHelper.i(TAG,"openCamera cameraId = " + cameraId);
        initSettingManager(info.getSettingManager());
        if (canOpenCamera(cameraId)){
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mCurrentCameraId = cameraId;
                initSettings();

                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
                updateCameraState(CameraState.CAMERA_OPENING);
                mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
            } catch (CameraOpenException e) {
                if (CameraOpenException.ExceptionType.SECURITY_EXCEPTION == e.getExceptionType()) {
                    CameraUtil.showErrorInfoAndFinish(mActivity,
                            CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CameraAccessException e) {
                CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_HARDWARE_EXCEPTION);
            } finally {
                mDeviceLock.unlock();
            }
        }
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
            LogHelper.d(TAG, "[updatePreviewSurface] surfaceHolder = " + surfaceObject + ",state = "
                    + mCameraState);
            synchronized (mSurfaceHolderSync) {
                mSurfaceObject = surfaceObject;
                if (surfaceObject instanceof SurfaceHolder) {
                    mPreviewSurface = surfaceObject == null ? null :
                            ((SurfaceHolder) surfaceObject).getSurface();
                } else if (surfaceObject instanceof SurfaceTexture) {
                    mPreviewSurface = surfaceObject == null ? null :
                            new Surface((SurfaceTexture) surfaceObject);
                }
                boolean isStateReady = CameraState.CAMERA_OPENED == mCameraState;
                if (isStateReady && mCamera2Proxy != null) {
                    if (surfaceObject != null) {
                        configureSession();
                    } else {
                        stopPreview();
                    }
                }
            }
    }

    @Override
    public void setDeviceCallback(DeviceCallback callback) {
        mModeDeviceCallback = callback;
    }


    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession();
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "[stopPreview]");
        abortOldSession();
    }

    @Override
    public void takePicture(@Nonnull JpegCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession = " + mSession
                + ",mCamera2Proxy = " + mCamera2Proxy);
        if (mSession == null || mCamera2Proxy == null) {
            return;
        }
        mJpegCallback = callback;
        CaptureRequest.Builder builder;
        try {
            builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mCaptureSurface.getSurface());
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[takePicture] error create build fail.");
            return;
        }

        //set the jpeg orientation
        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCurrentCameraId),
                mJpegRotation, mActivity);
        builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
        //set take picture command.
        try {
            mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void stopCapture() {

        LogHelper.i(TAG, "[stopCapture] mSession= " + mSession + ",isAborting " + mSession);
        mIsSessionAbortCalled = true;
        try {

            mSession.abortCaptures();
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[stopCapture] CameraAccessException " + e);
        }
    }

    @Override
    public void setNeedWaitPictureDone(boolean needWaitPictureDone) {

    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        mJpegRotation = orientation;
    }

    @Override
    public void closeCamera(boolean sync) {

        LogHelper.i(TAG, "[closeCamera] + sync = " + sync + ",current state : " + mCameraState);
        if (CameraState.CAMERA_UNKNOWN != mCameraState) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                super.doCameraClosed(mCamera2Proxy);
                updateCameraState(CameraState.CAMERA_CLOSING);
                abortOldSession();
                if (mModeDeviceCallback != null) {
                    mModeDeviceCallback.beforeCloseCamera();
                }
                doCloseCamera(sync);
                updateCameraState(CameraState.CAMERA_UNKNOWN);
                recycleVariables();
                mCaptureSurface.releaseCaptureSurface();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                super.doCameraClosed(mCamera2Proxy);
                mDeviceLock.unlock();
            }
            recycleVariables();
        }
        mCurrentCameraId = null;
        LogHelper.i(TAG, "[closeCamera] -");
    }

    @Override
    public Size getPreviewSize(double targetRatio) {
        int oldPreviewWidth = mPreviewWidth;
        int oldPreviewHeight = mPreviewHeight;
        updateTargetPreviewSize(targetRatio);
        boolean isSameSize = oldPreviewHeight == mPreviewHeight && oldPreviewWidth == mPreviewWidth;
        LogHelper.i(TAG, "[getPreviewSize], old size : " + oldPreviewWidth + " X " +
                oldPreviewHeight + ", new  size :" + mPreviewWidth + " X " + mPreviewHeight);
        //if preview size don't change, but picture size changed,need do configure the surface.
        //if preview size changed,do't care the picture size changed,because surface will be
        //changed.
        if (isSameSize && mIsPictureSizeChanged) {
            configureSession();
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void setPreviewSizeReadyCallback(PreviewSizeCallback callback) {
        mPreviewSizeCallback = callback;
    }

    @Override
    public void setPictureSize(Size size) {
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), CAPTURE_FORMAT, CAPTURE_MAX_NUMBER, mPreviewWidth, mPreviewHeight);

    }

    @Override
    public boolean isReadyForCapture() {
        boolean canCapture = mSession != null
                && mCamera2Proxy != null && getCameraState() == CameraState.CAMERA_OPENED;
        LogHelper.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
        return canCapture;
    }

    @Override
    public void destroyDeviceController() {
        if (mCaptureSurface != null) {
            mCaptureSurface.releaseCaptureSurface();
            mCaptureSurface.release();
        }

        mPreviewCallback = null;
    }

    @Override
    protected void doCameraOpened(Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[doCameraOpened]  camera2proxy = " + camera2proxy + " preview surface = "
                + mPreviewSurface + "  mCameraState = " + mCameraState);

        try {
            if (CameraState.CAMERA_OPENING == getCameraState()
                    && camera2proxy != null && camera2proxy.getId().equals(mCurrentCameraId)) {
                mCamera2Proxy = camera2proxy;
                if (mModeDeviceCallback != null) {
                    mModeDeviceCallback.onCameraOpened(mCurrentCameraId);
                }
                updateCameraState(CameraState.CAMERA_OPENED);
                updatePreviewSize();
                if (mPreviewSizeCallback != null) {
                    mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                            mPreviewHeight));
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doCameraDisconnected(Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[doCameraDisconnected]  camera2proxy = " + camera2proxy);
        if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
            CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
        }

    }

    @Override
    protected void doCameraError(Camera2Proxy camera2Proxy, int error) {
        LogHelper.i(TAG, "[doCameraError]  camera2proxy = " + camera2Proxy + " error = " + error);
        if ((mCamera2Proxy != null && mCamera2Proxy == camera2Proxy)
                || error == CameraUtil.CAMERA_OPEN_FAIL) {
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            CameraUtil.showErrorInfoAndFinish(mActivity, error);
        }
    }

    @Override
    public void onPictureCallback(byte[] data) {
        LogHelper.i(TAG, "[onPictureCallback]");
        mFirstFrameArrived = false;
        if (mJpegCallback != null) {
            mJpegCallback.onDataReceived(data);
        }
    }

    public void onPreviewFrame(byte[] data, int width, int height){
        if (mPreviewCallback != null) {
            mPreviewCallback.onPreviewFrame(data, width, height);
        }
    }

    @Override
    public void createAndChangeRepeatingRequest() {
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state, can't request ");
            return;
        }
        repeatingPreview(true);
    }

    @Override
    public CaptureRequest.Builder createAndConfigRequest(int templateType) {
        CaptureRequest.Builder builder = null;
        try {
            builder = doCreateAndConfigRequest(templateType);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return builder;
    }

    @Override
    public com.mediatek.camera.common.mode.photo.device.CaptureSurface getModeSharedCaptureSurface() throws IllegalStateException {
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Surface getModeSharedPreviewSurface() throws IllegalStateException {
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mPreviewSurface;
        }
    }

    @Override
    public Surface getModeSharedThumbnailSurface() throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Camera2CaptureSessionProxy getCurrentCaptureSession() {
        return mSession;
    }

    @Override
    public void requestRestartSession() {
        configureSession();
    }

    @Override
    public int getRepeatingTemplateType() {
        return Camera2Proxy.TEMPLATE_PREVIEW;
    }

    private void updateCameraState(CameraState state) {
        LogHelper.d(TAG, "[updateCameraState] new state = " + state + " old =" + mCameraState);
        mLockState.lock();
        try {
            mCameraState = state;
        } finally {
            mLockState.unlock();
        }
    }

    private CameraState getCameraState() {
        mLockState.lock();
        try {
            return mCameraState;
        } finally {
            mLockState.unlock();
        }
    }

    private void initSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
        settingManager.updateModeDevice2Requester(this);
        mSettingDevice2Configurator = settingManager.getSettingDevice2Configurator();
        mSettingController = settingManager.getSettingController();
    }
    private boolean canOpenCamera(String newCameraId) {
        boolean isSameCamera = newCameraId.equalsIgnoreCase(mCurrentCameraId);
        boolean isStateReady = mCameraState == CameraState.CAMERA_UNKNOWN;
        //if is same camera id, don't need open the camera.
        boolean value = !isSameCamera && isStateReady;
        LogHelper.i(TAG, "[canOpenCamera] new id: " + newCameraId + ",current camera :" +
                mCurrentCameraId + ",isSameCamera = " + isSameCamera + ", current state : " +
                mCameraState + ",isStateReady = " + isStateReady + ",can open : " + value);
        return value;
    }

    private void initSettings() throws CameraAccessException {
        mSettingManager.createAllSettings();
        mSettingDevice2Configurator.setCameraCharacteristics(mCameraManager.getCameraCharacteristics(mCurrentCameraId));
        //try to setting Relation
        Relation relation = FilterRestriction.getRestriction().getRelation("on",
                false);

       /* ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        List<String> picsize =  controller.querySupportedPlatformValues(KEY_PICTURE_SIZE);
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        String maxDefaultSize = null;
        for(String s : picsize){
            String []size = s.split("x");
            if(null != size && size.length == 2){
                float w = Float.valueOf(size[0]).floatValue();
                float h = Float.valueOf(size[1]).floatValue();
                if(Math.abs(w / h - 4f / 3) < 0.01){
                    if(!isFirst){
                        builder.append(",");
                    }
                    builder.append(s);
                    isFirst = false;
                    if(null == maxDefaultSize){
                        maxDefaultSize = s;
                    }
                }
            }
        }

        String targetString  = builder.toString();
        if(!targetString.contains(pictureSize)){
            pictureSize = maxDefaultSize;
        }
        relation.addBody(KEY_PICTURE_SIZE, pictureSize, targetString);*/

        mSettingManager.getSettingController().postRestriction(relation);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void configureSession() {
        mDeviceLock.lock();
        try {
            if (mCamera2Proxy == null) {
                return;
            }
            //if the old session is in processing, need abort before one.
            abortOldSession();
            //Prepare the new session.
            List<Surface> surfaces = new LinkedList<>();
            surfaces.add(mPreviewSurface);
            //Prepare the capture surface.
            surfaces.add(mCaptureSurface.getSurface());

            surfaces.add(mCaptureSurface.getPreviewCallbackSurface());

            mSettingDevice2Configurator.configSessionSurface(surfaces);
            LogHelper.d(TAG, "[configureSession] surface size : " + surfaces.size());
            mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
            mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                    mModeHandler, mBuilder);
            mIsPictureSizeChanged = false;
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[configureSession] error");
        } finally {
            mDeviceLock.unlock();
        }
    }

    private void doCloseCamera(boolean sync) {
        if (mCamera2Proxy != null) {
            if (sync) {
                mCameraDeviceManager.closeSync(mCurrentCameraId);
            } else {
                mCameraDeviceManager.close(mCurrentCameraId);
            }
        }
        mCamera2Proxy = null;
        synchronized (mSurfaceHolderSync) {
            mSurfaceObject = null;
            mPreviewSurface = null;
        }
    }

    private void recycleVariables() {
        mCurrentCameraId = null;
        updatePreviewSurface(null);
        mCamera2Proxy = null;
        mIsPictureSizeChanged = false;
    }

    private void abortOldSession() {
        LogHelper.d(TAG, "[abortOldSession]");
        if (mSession != null) {
            try {
                mSession.abortCaptures();
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[abortOldSession] CameraAccessException ", e);
            }
        }
        mSession = null;
        mBuilder = null;
    }

    private void repeatingPreview(boolean needConfigBuiler) {
        LogHelper.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" +
                mCamera2Proxy + ",needConfigBuiler " + needConfigBuiler);
        if (mSession == null || mCamera2Proxy == null) {
            return;
        }
        try {
            mFirstFrameArrived = false;
            if (needConfigBuiler) {
                CaptureRequest.Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
            } else {
                mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
            }
            mCaptureSurface.setCaptureCallback(this);
        } catch (CameraAccessException | RuntimeException e) {
            LogHelper.e(TAG, "[repeatingPreview] error");
        }
    }


    private void updatePreviewSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePreviewSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            double ratio = (double) width / height;
            updateTargetPreviewSize(ratio);
        }
    }


    private void updateTargetPreviewSize(double ratio) {
        try {
            CameraCharacteristics cs = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
            StreamConfigurationMap streamConfigurationMap =
                    cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size previewSizes[] =
                    streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
            int length = previewSizes.length;
            List<Size> sizes = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                sizes.add(i, new Size(previewSizes[i].getWidth(), previewSizes[i].getHeight()));
            }
            Size values = CameraUtil.getOptimalPreviewSize(mActivity, sizes, ratio, true);
            mPreviewWidth = values.getWidth();
            mPreviewHeight = values.getHeight();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[updateTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
    }

    private CaptureRequest.Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            mSettingDevice2Configurator.configCaptureRequest(builder);
            if (Camera2Proxy.TEMPLATE_PREVIEW == templateType) {
                builder.addTarget(mPreviewSurface);
                builder.addTarget(mCaptureSurface.getPreviewCallbackSurface());
            }

        }
        return builder;
    }

    /**
     * Camera session callback.
     */
    private final Camera2CaptureSessionProxy.StateCallback mSessionCallback = new
            Camera2CaptureSessionProxy.StateCallback() {

                @Override
                public void onConfigured(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigured],session = " + session);
                    mDeviceLock.lock();
                    try {
                        mSession = session;
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            mSession = session;
                            synchronized (mSurfaceHolderSync) {
                                if (mPreviewSurface != null) {
                                    repeatingPreview(false);
                                }
                            }
                        }
                    } finally {
                        mDeviceLock.unlock();
                    }
                }

                @Override
                public void onConfigureFailed(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigureFailed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }

                @Override
                public void onClosed(@Nonnull Camera2CaptureSessionProxy session) {
                    super.onClosed(session);
                    LogHelper.i(TAG, "[onClosed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }

                @Override
                public void onReady(@Nonnull Camera2CaptureSessionProxy session) {
                    super.onReady(session);
                    if (mSession == session) {
                        if (mIsSessionAbortCalled) {
                            LogHelper.d(TAG, "[onReady]");
                            mIsSessionAbortCalled = false;
                            repeatingPreview(false);
                        }
                    }
                }
            };

    /**
     * Capture callback.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            LogHelper.d(TAG,"[onCaptureStarted]");
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            if (!CameraUtil.isStillCaptureTemplate(request)) {
                return;
            }
            //LAVA_EDIT modified by jiangym for shutter sounc start
            String soundValue = mICameraContext.getDataStore().getValue("key_shutter_sound","on",mICameraContext.getDataStore().getGlobalScope());
            if ("on".equalsIgnoreCase(soundValue)){
                mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
            }
            //LAVA_EDIT end
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                                       @Nonnull CaptureRequest request, @Nonnull
                                               TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            LogHelper.d(TAG,"[onCaptureCompleted]");
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            if (mModeDeviceCallback != null && !mFirstFrameArrived) {
                mFirstFrameArrived = true;
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                                    @Nonnull CaptureRequest request, @Nonnull CaptureFailure
                                            failure) {
            super.onCaptureFailed(session, request, failure);
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
            if (CameraUtil.isStillCaptureTemplate(request)) {
                LogHelper.d(TAG, "[onCaptureFailed] the capture has failed due to a result " +
                        failure.getReason());
            }
        }
    };

    public void setFilerSurfaceTexture(SurfaceTexture surfaceTexture){
        LogHelper.d(TAG, "[setFilerSurfaceTexture] JIANGYM_DEBUG " + surfaceTexture);
        mFilterSurfaceTexture = surfaceTexture;
        //configureSession();
    }

    public void setPreviewCallback(PreviewCallback callback){
        mPreviewCallback = callback;
    }
}
