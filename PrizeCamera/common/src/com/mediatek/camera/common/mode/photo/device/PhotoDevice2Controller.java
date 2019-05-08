/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.mode.photo.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManager;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.device.CameraOpenException;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy.StateCallback;
import com.mediatek.camera.common.mode.Device2Controller;
import com.mediatek.camera.common.mode.photo.DeviceInfo;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.P2DoneInfo;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Configurator;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
/*prize-modify- set default photo size for picturezoom-xiaoping-20190228-start*/
import com.mediatek.camera.portability.SystemProperties;
/*prize-modify- set default photo size for picturezoom-xiaoping-20190228-end*/

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
/*prize-add AI CAMERA-huangpengfei-2019-01-10-start*/
import com.mediatek.camera.common.prize.PrizeAiSceneClassify;
/*prize-add AI CAMERA-huangpengfei-2019-01-10-end*/

/**
 * An implementation of {@link IDeviceController} with Camera2Proxy.
 */
 
// zhangguo modify public 20190422, for picselfiemode set picture size
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PhotoDevice2Controller extends Device2Controller implements
        IDeviceController,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {
    private static final Tag TAG = new Tag(PhotoDevice2Controller.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    protected static final int CAPTURE_MAX_NUMBER = 5; // zhangguo modify public 20190422, for picselfiemode set picture size
    private static final int WAIT_TIME = 5;
    //add for quick preview
    private static final String QUICK_PREVIEW_KEY = "com.mediatek.configure.setting.initrequest";
    private static final int[] QUICK_PREVIEW_KEY_VALUE = new int[]{1};
    private CaptureRequest.Key<int[]> mQuickPreviewKey = null;

    private final Activity mActivity;
	// zhangguo modify public 20190422, for picselfiemode set picture size start
    protected final CameraManager mCameraManager;
    protected final CaptureSurface mCaptureSurface;
    protected final CaptureSurface mThumbnailSurface;
	// zhangguo modify public 20190422, for picselfiemode set picture size end
    private final ICameraContext mICameraContext;
    private final Object mSurfaceHolderSync = new Object();
    private final StateCallback mDeviceCallback = new DeviceStateCallback();

    private int mJpegRotation;
    protected volatile int mPreviewWidth;// zhangguo modify public 20190422, for picselfiemode set picture size
    protected volatile int mPreviewHeight;
    private volatile Camera2Proxy mCamera2Proxy;
    private volatile Camera2CaptureSessionProxy mSession;

    private boolean mFirstFrameArrived = false;
    protected boolean mIsPictureSizeChanged = false;// zhangguo modify public 20190422, for picselfiemode set picture size
    private boolean mNeedSubSectionInitSetting = false;
    private volatile boolean mNeedFinalizeOutput = false;

    private Lock mLockState = new ReentrantLock();
    private Lock mDeviceLock = new ReentrantLock();
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;

    private String mCurrentCameraId;
    private Surface mPreviewSurface;
    private CaptureDataCallback mCaptureDataCallback;
    private Object mSurfaceObject;
    protected ISettingManager mSettingManager;// zhangguo modify public 20190422, for picselfiemode set picture size
    private DeviceCallback mModeDeviceCallback;
    protected SettingController mSettingController;// zhangguo modify public 20190422, for picselfiemode set picture size
    private PreviewSizeCallback mPreviewSizeCallback;
    private CameraDeviceManager mCameraDeviceManager;
    private SettingDevice2Configurator mSettingDevice2Configurator;
    private CaptureRequest.Builder mBuilder = null;
    private CaptureRequest.Builder mDefaultBuilder = null;
    private String mZsdStatus = "on";
    private List<OutputConfiguration> mOutputConfigs;
    private CameraCharacteristics mCameraCharacteristics;
    private ConcurrentHashMap mCaptureFrameMap = new ConcurrentHashMap<String, Boolean>();

    /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-start*/
    protected IApp mApp;// zhangguo modify public 20190422, for picselfiemode set picture size
    /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-end*/

    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
    private Object mSurafceObjectOnHd;
    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/

	/*prize-add AI CAMERA-huangpengfei-2019-01-29-start*/
    private int mPortraitFramCount = 0;
    private int mPreviewFramCount = 0;
    private int mCurrentFormatSceneId = -1;
    private boolean mNeedDelay = false;
	/*prize-add AI CAMERA-huangpengfei-2019-01-29-end*/
    /*prize-modify-add professional mode function-xiaoping-20190216-start*/
    private boolean isCloseCamera = false;
    private boolean mNeedPauseAi = false;
    /*prize-modify-add professional mode function-xiaoping-20190216-end*/

    /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-start*/
    protected Byte jpegQuality = 85;
    /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-end*/

    /**
     * this enum is used for tag native camera open state.
     */
    private enum CameraState {
        CAMERA_UNKNOWN,
        CAMERA_OPENING,
        CAMERA_OPENED,
        CAMERA_CAPTURING,
        CAMERA_CLOSING,
    }

    /**
     * PhotoDeviceController may use activity to get display rotation.
     * @param activity the camera activity.
     */
	// zhangguo modify public 20190422, for picselfiemode set picture size
    public PhotoDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[PhotoDevice2Controller]");
        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mCaptureSurface = new CaptureSurface();
        mCaptureSurface.setCaptureCallback(this);
        mThumbnailSurface = new CaptureSurface();
        mThumbnailSurface.setCaptureCallback(this);
        mThumbnailSurface.setFormat(ThumbnailHelper.FORMAT_TAG);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    // zhangguo modify public 20190422, for picselfiemode set picture size
    /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-start*/
    public PhotoDevice2Controller(@Nonnull IApp app, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[PhotoDevice2Controller]");
        mApp = app;
        mActivity = app.getActivity();
        mCameraManager = (CameraManager) app.getActivity().getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mCaptureSurface = new CaptureSurface();
        mCaptureSurface.setCaptureCallback(this);
        mThumbnailSurface = new CaptureSurface();
        mThumbnailSurface.setCaptureCallback(this);
        mThumbnailSurface.setFormat(ThumbnailHelper.FORMAT_TAG);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }
    /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-end*/

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void openCamera(DeviceInfo info) {
        String cameraId = info.getCameraId();
        boolean sync = info.getNeedOpenCameraSync();
        LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync);
        if (canOpenCamera(cameraId)) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mNeedSubSectionInitSetting = info.getNeedFastStartPreview();
                mCurrentCameraId = cameraId;
                updateCameraState(CameraState.CAMERA_OPENING);
                initSettingManager(info.getSettingManager());
                /*prize-modify-add professional mode function-xiaoping-20190216-start*/
                isCloseCamera = false;
                /*prize-modify-add professional mode function-xiaoping-20190216-end*/
// zhangguo add 20190507, for continus shot can not callback start
                if(null != mCaptureSurface){
                    mCaptureSurface.setContinusShot(false);
                }
// zhangguo add 20190507, for continus shot can not callback end
                doOpenCamera(sync);
                if (mNeedSubSectionInitSetting) {
                    mSettingManager.createSettingsByStage(1);
                } else {
                    mSettingManager.createAllSettings();
                }

                initSettings();// zhangguo modify 20190422, for picselfiemode set picture size

                mCameraCharacteristics
                        = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
                mQuickPreviewKey = CameraUtil.getAvailableSessionKeys(
                        mCameraCharacteristics, QUICK_PREVIEW_KEY);
            } catch (CameraOpenException e) {
                if (CameraOpenException.ExceptionType.SECURITY_EXCEPTION == e.getExceptionType()) {
                    CameraUtil.showErrorInfoAndFinish(mActivity,
                            CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CameraAccessException | IllegalArgumentException e) {
                CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_HARDWARE_EXCEPTION);
            } finally {
                mDeviceLock.unlock();
            }
        }
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        LogHelper.d(TAG, "[updatePreviewSurface] surfaceHolder = " + surfaceObject + " state = "
                + mCameraState + ", session :" + mSession + ", mNeedSubSectionInitSetting:"
                + mNeedSubSectionInitSetting);
        synchronized (mSurfaceHolderSync) {
            if (surfaceObject instanceof SurfaceHolder) {
                mPreviewSurface = surfaceObject == null ? null :
                        ((SurfaceHolder) surfaceObject).getSurface();
            } else if (surfaceObject instanceof SurfaceTexture) {
                mPreviewSurface = surfaceObject == null ? null :
                        new Surface((SurfaceTexture) surfaceObject);
            }
            boolean isStateReady = CameraState.CAMERA_OPENED == mCameraState;
            /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
            if (mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) {
                mSurafceObjectOnHd = surfaceObject;
            }
            /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/
            if (isStateReady && mCamera2Proxy != null) {
                boolean onlySetSurface = mSurfaceObject == null && surfaceObject != null;
                mSurfaceObject = surfaceObject;
                if (surfaceObject == null) {
                    stopPreview();
                } else if (onlySetSurface && mNeedSubSectionInitSetting) {
                    mOutputConfigs.get(0).addSurface(mPreviewSurface);
                    if (mSession != null) {
                        mSession.finalizeOutputConfigurations(mOutputConfigs);
                        mNeedFinalizeOutput = false;
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            repeatingPreview(false);
                            configSettingsByStage2();
                            repeatingPreview(false);
                        }
                    } else {
                        mNeedFinalizeOutput = true;
                    }
                } else {
                    configureSession(false);
                }
            }
        }
    }

    @Override
    public void setDeviceCallback(DeviceCallback callback) {
        mModeDeviceCallback = callback;
    }

    @Override
    public void setPreviewSizeReadyCallback(PreviewSizeCallback callback) {
        mPreviewSizeCallback = callback;
    }

    /**
     * Set the new picture size.
     *
     * @param size current picture size.
     */
    @Override
    public void setPictureSize(Size size) {
        String formatTag = mSettingController.queryValue(HeifHelper.KEY_FORMAT);
        int format = HeifHelper.getCaptureFormat(formatTag);
        mCaptureSurface.setFormat(formatTag);
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), format, CAPTURE_MAX_NUMBER);
        double ratio = (double) size.getWidth() / size.getHeight();
        ThumbnailHelper.updateThumbnailSize(ratio);
        if (ThumbnailHelper.isPostViewSupported()) {
            mThumbnailSurface.updatePictureInfo(ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    ThumbnailHelper.IMAGE_BUFFER_FORMAT,
                    CAPTURE_MAX_NUMBER);
        }
    }

    /**
     * Check whether can take picture or not.
     *
     * @return true means can take picture; otherwise can not take picture.
     */
    @Override
    public boolean isReadyForCapture() {
        boolean canCapture = mSession != null
                && mCamera2Proxy != null && getCameraState() == CameraState.CAMERA_OPENED;
        LogHelper.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
        /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
        if (canCapture == false) {
            LogHelper.i(TAG,"mSession: "+mSession+",mCamera2Proxy: "+mCamera2Proxy+",getCameraState: "+getCameraState());
        }
        /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/
        return canCapture;
    }

    @Override
    public void destroyDeviceController() {
        if (mCaptureSurface != null) {
            mCaptureSurface.release();

        }
    }

    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession(false);
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "[stopPreview]");
        abortOldSession();
    }

    @Override
    public void takePicture(@Nonnull IDeviceController.CaptureDataCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession= " + mSession);
        if (mSession != null && mCamera2Proxy != null) {
            mCaptureDataCallback = callback;
            updateCameraState(CameraState.CAMERA_CAPTURING);
            try {
                Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
                mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
                /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
                if (isNeedPlayAnimation()) {
                    mApp.getAppUi().startCaptureAnimation();
                }
                /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/
            } catch (CameraAccessException e) {
                e.printStackTrace();
                LogHelper.e(TAG, "[takePicture] error because create build fail.");
            }
        }
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
		/*prize-modify AI CAMERA-huangpengfei-2019-01-09-start*/
        LogHelper.i(TAG,"[updateGSensorOrientation]  orientation = "+orientation);
        //prize-add mFirstFrameArrived fixbug:[73985]-huangpengfei-20190402
        if(mFirstFrameArrived && mJpegRotation != orientation) {
        	mJpegRotation = orientation;
            if ((mCameraState == CameraState.CAMERA_OPENED && mBuilder != null && mSession != null)) {
                synchronized (mBuilder) {
                    try{
                        if (mBuilder != null) {
                            repeatingPreview(true);
                            LogHelper.i(TAG,"[updateGSensorOrientation]   " +"   update successful.");
                        }
                    }catch (Exception e){
                        LogHelper.i(TAG,"[updateGSensorOrientation] " +"   update fail!.");
                        e.printStackTrace();
                    }
                }
            }
        }
		/*prize-modify AI CAMERA-huangpengfei-2019-01-09-end*/
    }

    @Override
    public void closeCamera(boolean sync) {
        LogHelper.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
        if (CameraState.CAMERA_UNKNOWN != mCameraState) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                super.doCameraClosed(mCamera2Proxy);
                updateCameraState(CameraState.CAMERA_CLOSING);
                /*prize-modify-add professional mode function-xiaoping-20190216-start*/
                isCloseCamera = true;
                /*prize-modify-add professional mode function-xiaoping-20190216-end*/
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
        getTargetPreviewSize(targetRatio);
        boolean isSameSize = oldPreviewHeight == mPreviewHeight && oldPreviewWidth == mPreviewWidth;
        LogHelper.i(TAG, "[getPreviewSize] old size : " + oldPreviewWidth + " X " +
                oldPreviewHeight + " new  size :" + mPreviewWidth + " X " + mPreviewHeight);
        //if preview size don't change, but picture size changed,need do configure the surface.
        //if preview size changed,do't care the picture size changed,because surface will be
        //changed.
        if (isSameSize && mIsPictureSizeChanged) {
            configureSession(false);
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data,
                                  int format, String formatTag, int width, int height) {
        LogHelper.i(TAG, "[onPictureCallback] buffer format = " + format);
        if (mCaptureDataCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            info.mBufferFormat = format;
            info.imageHeight = height;
            info.imageWidth = width;
            if (ThumbnailHelper.isPostViewSupported()) {
                info.needUpdateThumbnail = false;
            }
            if (ThumbnailHelper.FORMAT_TAG.equalsIgnoreCase(formatTag)) {
                mCaptureDataCallback.onPostViewCallback(data);
            } else {
                mApp.getAppUi().updateScreenView(false);
                mCaptureDataCallback.onDataReceived(info);
            }
        }

    }

    @Override
    public void createAndChangeRepeatingRequest() {
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state can't request ");
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
    public CaptureSurface getModeSharedCaptureSurface() throws IllegalStateException {
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mCaptureSurface;
        }
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
        if (CameraState.CAMERA_UNKNOWN == getCameraState()
                || CameraState.CAMERA_CLOSING == getCameraState()) {
            throw new IllegalStateException("get invalid capture surface!");
        } else {
            return mThumbnailSurface.getSurface();
        }
    }

    @Override
    public Camera2CaptureSessionProxy getCurrentCaptureSession() {
        return mSession;
    }

    @Override
    public void requestRestartSession() {
        configureSession(false);
    }

    @Override
    public int getRepeatingTemplateType() {
        return Camera2Proxy.TEMPLATE_PREVIEW;
    }

    private void initSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
        settingManager.updateModeDevice2Requester(this);
        mSettingDevice2Configurator = settingManager.getSettingDevice2Configurator();
        mSettingController = settingManager.getSettingController();
    }

    private void doOpenCamera(boolean sync) throws CameraOpenException {
        if (sync) {
            mCameraDeviceManager.openCameraSync(mCurrentCameraId, mDeviceCallback, null);
        } else {
            mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
        }
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

    private void doCloseCamera(boolean sync) {
        LogHelper.i(TAG,"sync: "+sync);
        if (sync) {
            mCameraDeviceManager.closeSync(mCurrentCameraId);
        } else {
            mCameraDeviceManager.close(mCurrentCameraId);
        }
        mCaptureFrameMap.clear();
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

    private boolean canOpenCamera(String newCameraId) {
        boolean isSameCamera = newCameraId.equalsIgnoreCase(mCurrentCameraId);
        boolean isStateReady = mCameraState == CameraState.CAMERA_UNKNOWN;
        boolean value = !isSameCamera && isStateReady;
        LogHelper.i(TAG, "[canOpenCamera] new id: " + newCameraId + " current camera :" +
                mCurrentCameraId + " isSameCamera = " + isSameCamera + " current state : " +
                mCameraState + " isStateReady = " + isStateReady + " can open : " + value);
        return value;
    }

    private void configureSession(boolean isFromOpen) {
        LogHelper.i(TAG, "[configureSession] +" + ", isFromOpen :" + isFromOpen);
        mDeviceLock.lock();
        mFirstFrameArrived = false;
        try {
            if (mCamera2Proxy != null) {
                abortOldSession();
                if (isFromOpen) {
                    mOutputConfigs = new ArrayList<>();
                    android.util.Size previewSize = new android.util.Size(mPreviewWidth,
                            mPreviewHeight);
                    OutputConfiguration previewConfig = new OutputConfiguration(previewSize,
                            SurfaceTexture.class);
                    OutputConfiguration captureConfig
                            = new OutputConfiguration(mCaptureSurface.getSurface());
                    OutputConfiguration rawConfig
                            = mSettingDevice2Configurator.getRawOutputConfiguration();
                    mOutputConfigs.add(previewConfig);
                    mOutputConfigs.add(captureConfig);
                    if (rawConfig != null) {
                        mOutputConfigs.add(rawConfig);
                    }
                    if (ThumbnailHelper.isPostViewSupported()) {
                        OutputConfiguration thumbnailConfig
                                = new OutputConfiguration(mThumbnailSurface.getSurface());
                        mOutputConfigs.add(thumbnailConfig);
                    }
                    mBuilder = getDefaultPreviewBuilder();
                    mSettingDevice2Configurator.configCaptureRequest(mBuilder);
                    configureQuickPreview(mBuilder);
                    mCamera2Proxy.createCaptureSession(mSessionCallback,
                            mModeHandler, mBuilder, mOutputConfigs);
                    mIsPictureSizeChanged = false;
                    return;
                }

                if(null == mPreviewSurface){// add by zhangugo, switch camera can not connect
                    return;
                }

                List<Surface> surfaces = new LinkedList<>();
                surfaces.add(mPreviewSurface);
                surfaces.add(mCaptureSurface.getSurface());
                if (ThumbnailHelper.isPostViewSupported()) {
                    surfaces.add(mThumbnailSurface.getSurface());
                }
                mNeedFinalizeOutput = false;
                mSettingDevice2Configurator.configSessionSurface(surfaces);
                LogHelper.d(TAG, "[configureSession] surface size : " + surfaces.size());
                mBuilder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                        mModeHandler, mBuilder);
                mIsPictureSizeChanged = false;
            }
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[configureSession] error");
        } finally {
            mDeviceLock.unlock();
        }
    }

    private void configSettingsByStage2() {
        mSettingManager.createSettingsByStage(2);
        mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
        P2DoneInfo.setCameraCharacteristics(mActivity.getApplicationContext(),
                    Integer.parseInt(mCurrentCameraId));
        mSettingDevice2Configurator.configCaptureRequest(mBuilder);
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void abortOldSession() {
        if (mSession != null) {
            try {
                mSession.abortCaptures();
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[abortOldSession] exception", e);
            }
        }
        /*prize-modify-add professional mode function-xiaoping-20190216-start*/
        if (isCloseCamera) {
//            mApp.getAppUi().clearAllEffect();
        }
        /*prize-modify-add professional mode function-xiaoping-20190216-end*/
        mSession = null;
        mBuilder = null;
        mDefaultBuilder = null;
    }

    private void configureQuickPreview(Builder builder) {
        LogHelper.d(TAG, "configureQuickPreview mQuickPreviewKey:" + mQuickPreviewKey);
        if (mQuickPreviewKey != null) {
            builder.set(mQuickPreviewKey, QUICK_PREVIEW_KEY_VALUE);
        }
    }

    private void repeatingPreview(boolean needConfigBuiler) {
        LogHelper.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" +
                mCamera2Proxy + ",needConfigBuiler " + needConfigBuiler);
        if (mSession != null && mCamera2Proxy != null) {
            try {
                if (needConfigBuiler) {
                    Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                    mSession.setRepeatingRequest(builder.build(), mCaptureCallback, mModeHandler);
                } else {
                    mBuilder.addTarget(mPreviewSurface);
                    /*prize-modify-bugid:70053 Null Surface targets are not allowed-xiaoping-20190107-start*/
                    if (mPreviewSurface != null) {
                        mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
                    } else {
                        LogHelper.e(TAG,"mPreviewSurface is "+mPreviewSurface+" do not setRepeatingRequest");
                    }
                    /*prize-modify-bugid:70053 Null Surface targets are not allowed-xiaoping-20190107-end*/
                }
                mCaptureSurface.setCaptureCallback(this);
            } catch (CameraAccessException | RuntimeException e) {
                LogHelper.e(TAG, "[repeatingPreview] error");
            }
        }
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy);
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null && null != mPreviewSurface) { // add by zhangugo, switch camera can not connect
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            if (builder == null) {
                LogHelper.d(TAG, "Builder is null, ignore this configuration");
                return null;
            }
            mSettingDevice2Configurator.configCaptureRequest(builder);
            ThumbnailHelper.configPostViewRequest(builder);
            configureQuickPreview(builder);
            if (Camera2Proxy.TEMPLATE_PREVIEW == templateType) {
                builder.addTarget(mPreviewSurface);
                /*prize-modify-add portrait mode -xiaoping-20181212-start*/
                mBuilder=builder;
                /*prize-modify-add portrait mode -xiaoping-20181212-end*/

                /*prize-add set orientation for hal funtion-huangpengfei-20190323-start*/
                int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                        Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
                LogHelper.i(TAG, "[doCreateAndConfigRequest] capture: mJpegRotation ="
                        + mJpegRotation +"  rotation ="+rotation + "  templateType = " + templateType);
                HeifHelper.orientation = rotation;
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
                /*prize-add set orientation for hal funtion-huangpengfei-20190323-end*/
                /*prize-modify-bugid:73045 Third-party applications call the camera to turn on mirroring by default-xiaoping-20190326-start*/
                if (mApp != null && mApp.getAppUi() != null && mApp.getAppUi().isThirdPartyIntent()) {
                    if ("1".equals(mCurrentCameraId)) {
                        builder.set(CaptureRequest.VENDOR_MIRROR_ENABLE,1);
                    }
                }
                /*prize-modify-bugid:73045 Third-party applications call the camera to turn on mirroring by default-xiaoping-20190326-end*/
                /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-start*/
                if (SystemProperties.getInt("ro.pri.current.project",0) == 2) {
                    if (mApp != null && mApp.getAppUi().getModeItem() != null && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) {
                        jpegQuality = 100;
                    } else {
                        jpegQuality = 85;
                    }
                    builder.set(CaptureRequest.JPEG_QUALITY, jpegQuality);
                }
                /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-end*/
            } else if (Camera2Proxy.TEMPLATE_STILL_CAPTURE == templateType) {
                builder.addTarget(mCaptureSurface.getSurface());
                if ("off".equalsIgnoreCase(mZsdStatus)) {
                    builder.addTarget(mPreviewSurface);
                }
                if (ThumbnailHelper.isPostViewOverrideSupported()) {
                    builder.addTarget(mThumbnailSurface.getSurface());
                }
                ThumbnailHelper.setDefaultJpegThumbnailSize(builder);
                P2DoneInfo.enableP2Done(builder);
                CameraUtil.enable4CellRequest(mCameraCharacteristics, builder);
                int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                        Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
                LogHelper.i(TAG, "[doCreateAndConfigRequest] capture: mJpegRotation =" + mJpegRotation +"rotation ="+rotation);
                HeifHelper.orientation = rotation;
                builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
				//prize-huangzhanbin-20181008-add prize camera-start
                /*prize-modify- set default photo size for picturezoom-xiaoping-20190228-start*/
                android.util.Size pictureSize = new android.util.Size(4160,3120);
                if (SystemProperties.getInt("ro.pri.current.project",0) == 1) {
                    if (mApp != null && mApp.getAppUi().getModeItem().mModeTitle != null && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) {
                        pictureSize = new android.util.Size(8320, 6240);
                    } else {
                        pictureSize = new android.util.Size(mCaptureSurface.getWidth(), mCaptureSurface.getHeight());
                    }
                } else {
                    pictureSize = new android.util.Size(mCaptureSurface.getWidth(), mCaptureSurface.getHeight());
                    /*prize-modify- set default photo size for picturezoom-xiaoping-20190228-end*/
                }
                builder.set(CaptureRequest.JPEG_SIZE, pictureSize);
                //prize-huangzhanbin-20181008-add prize camera-end
                /*prize-modify-bugid:73045 Third-party applications call the camera to turn on mirroring by default-xiaoping-20190326-start*/
                if (mApp != null && mApp.getAppUi() != null && mApp.getAppUi().isThirdPartyIntent()) {
                    if ("1".equals(mCurrentCameraId)) {
                        builder.set(CaptureRequest.VENDOR_MIRROR_ENABLE,1);
                    }
                }
                /*prize-modify-bugid:73045 Third-party applications call the camera to turn on mirroring by default-xiaoping-20190326-end*/

                /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-start*/
                if (SystemProperties.getInt("ro.pri.current.project",0) == 2) {
                    if (mApp != null && mApp.getAppUi().getModeItem() != null && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) {
                        jpegQuality = 100;
                    } else {
                        jpegQuality = 85;
                    }
                    builder.set(CaptureRequest.JPEG_QUALITY, jpegQuality);
                }
                /*prize-modify-modify the image size after taking a photo-xiaoping-20190409-end*/
            }

        }
        return builder;
    }

    private Builder getDefaultPreviewBuilder() throws CameraAccessException {
        if (mCamera2Proxy != null && mDefaultBuilder == null) {
            mDefaultBuilder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_PREVIEW);
            ThumbnailHelper.configPostViewRequest(mDefaultBuilder);
        }
        return mDefaultBuilder;
    }

// zhangguo modify protected 20190422, for picselfiemode set picture size
    protected Size getTargetPreviewSize(double ratio) {
        Size values = null;
        /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-start*/
        boolean is640Preview = false;
        /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-end*/
        try {
            CameraCharacteristics cs = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
            StreamConfigurationMap streamConfigurationMap =
                    cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size previewSizes[] =
                    streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
            int length = previewSizes.length;
            List<Size> sizes = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-start*/
                if (SystemProperties.getInt("ro.pri.current.project",0) == 5 && previewSizes[i].getWidth() == 640 && previewSizes[i].getHeight() == 480) {
                    is640Preview = true;
                }
                /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-end*/
                sizes.add(i, new Size(previewSizes[i].getWidth(), previewSizes[i].getHeight()));
            }

            LogHelper.i(TAG,"previewsize: "+sizes);
            /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-start*/
//            values = CameraUtil.getOptimalPreviewSize(mActivity, sizes, ratio, true);
            /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-start*/
            if (SystemProperties.getInt("ro.pri.current.project",0) == 5) {
                if (mApp.getAppUi() != null && mApp.getAppUi().getModeItem() != null && (mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.BEAUTY
                        || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.FILTER
                        || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE
                        || "on".equals(mApp.getSettingValue("key_picsefile","off",Integer.valueOf(getCameraId())))) && is640Preview) {
                    values = new Size(640,480);
                    /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-end*/
                } else {
                    values = CameraUtil.getOptimalPreviewSize(mApp, sizes, ratio, true);
                }
            } else {
                values = CameraUtil.getOptimalPreviewSize(mApp, sizes, ratio, true);
            }
            /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-end*/
            mPreviewWidth = values.getWidth();
            mPreviewHeight = values.getHeight();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[getTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
        return values;
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
            getTargetPreviewSize(ratio);
        }
    }

    private void updatePictureSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePictureSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            setPictureSize(new Size(width, height));
        }
    }


    @Override
    public void doCameraOpened(@Nonnull Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[onOpened]  camera2proxy = " + camera2proxy + " preview surface = "
                + mPreviewSurface + "  mCameraState = " + mCameraState + "camera2Proxy id = "
                + camera2proxy.getId() + " mCameraId = " + mCurrentCameraId);
            try {
                if (CameraState.CAMERA_OPENING == getCameraState()
                        && camera2proxy != null && camera2proxy.getId().equals(mCurrentCameraId)) {
                    mCamera2Proxy = camera2proxy;
                    mFirstFrameArrived = false;
                    if (mModeDeviceCallback != null) {
                        mModeDeviceCallback.onCameraOpened(mCurrentCameraId);
                    }
                    updateCameraState(CameraState.CAMERA_OPENED);
                    ThumbnailHelper.setCameraCharacteristics(mCameraCharacteristics,
                            mActivity.getApplicationContext(), Integer.parseInt(mCurrentCameraId));
                    mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
                    updatePreviewSize();
                    updatePictureSize();

                    if (mPreviewSizeCallback != null) {
                        mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                                mPreviewHeight));
                    }
                    if (mNeedSubSectionInitSetting) {
                        configureSession(true);
                    } else {
                        mSettingController.addViewEntry();
                        mSettingController.refreshViewEntry();
                    }
                    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
                    IAppUi.ModeItem modeItem = mApp.getAppUi().getModeItem();
                    if (modeItem != null){//prize-modify check null for bug[70830]-huangpengfei-2019-01-19
                        if (modeItem.mModeTitle == IAppUi.ModeTitle.PICTUREZOOM && mSurafceObjectOnHd != null) {
                            updatePreviewSurface(mSurafceObjectOnHd);
                        }
                    }
                    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void doCameraDisconnected(@Nonnull Camera2Proxy camera2proxy) {
        LogHelper.i(TAG, "[onDisconnected] camera2proxy = " + camera2proxy);
        if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
            CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
        }
    }

    @Override
    public void doCameraError(@Nonnull Camera2Proxy camera2Proxy, int error) {
        LogHelper.i(TAG, "[onError] camera2proxy = " + camera2Proxy + " error = " + error);
        if ((mCamera2Proxy != null && mCamera2Proxy == camera2Proxy)
                || error == CameraUtil.CAMERA_OPEN_FAIL
                || error == CameraUtil.CAMERA_ERROR_EVICTED) {
            updateCameraState(CameraState.CAMERA_UNKNOWN);
            CameraUtil.showErrorInfoAndFinish(mActivity, error);
        }
    }

    /**
     * Camera session callback.
     */
    private final Camera2CaptureSessionProxy.StateCallback mSessionCallback = new
            Camera2CaptureSessionProxy.StateCallback() {

                @Override
                public void onConfigured(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigured],session = " + session
                            + ", mNeedFinalizeOutput:" + mNeedFinalizeOutput);
                    mDeviceLock.lock();
                    try {
                        mSession = session;
                        if (mNeedFinalizeOutput) {
                            mSession.finalizeOutputConfigurations(mOutputConfigs);
                            mNeedFinalizeOutput = false;
                            if (CameraState.CAMERA_OPENED == getCameraState()) {
                                synchronized (mSurfaceHolderSync) {
                                    if (mPreviewSurface != null) {
                                        repeatingPreview(false);
                                        configSettingsByStage2();
                                        repeatingPreview(false);
                                    }
                                }
                            }
                            return;
                        }
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
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
            };

    /**
     * Capture callback.
     */
    private final CaptureCallback mCaptureCallback = new CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            if (CameraUtil.isStillCaptureTemplate(request)) {
                LogHelper.d(TAG, "[onCaptureStarted] capture started, frame: " + frameNumber);
                mCaptureFrameMap.put(String.valueOf(frameNumber), Boolean.FALSE);
                mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            if (CameraUtil.isStillCaptureTemplate(request)
                    && P2DoneInfo.checkP2DoneResult(partialResult)) {
                //p2done comes, it can do next capture
                long num = partialResult.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))) {
                    mCaptureFrameMap.put(String.valueOf(num), Boolean.TRUE);
                }
                LogHelper.d(TAG, "[onCaptureProgressed] P2done comes, frame: " + num);
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (mCamera2Proxy == null
                    || mModeDeviceCallback == null
                    || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            if (CameraUtil.isStillCaptureTemplate(result)) {
                long num = result.getFrameNumber();
                if (mCaptureFrameMap.containsKey(String.valueOf(num))
                        && Boolean.FALSE == mCaptureFrameMap.get(String.valueOf(num))) {
                    mFirstFrameArrived = true;
                    updateCameraState(CameraState.CAMERA_OPENED);
                    mModeDeviceCallback.onPreviewCallback(null, 0);
                }
                mCaptureFrameMap.remove(String.valueOf(num));
            } else if (!CameraUtil.isStillCaptureTemplate(result) && !mFirstFrameArrived) {
                mFirstFrameArrived = true;
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }

            /*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/
            if (mSurfaceHolderSync != null) {
                synchronized (mSurfaceHolderSync) {
                    if (result != null && !mNeedPauseAi) {
                        Integer aiScene = result.get(CaptureResult.VENDOR_AICAMERA_MODE);
                        boolean portraitCallForAiTarget = mApp.getAppUi().getPortraitCallForAiTarget();
                        /*LogHelper.d(TAG, "[onCaptureCompleted]  CAMEAR-AI   portraitCallForAiTarget = " +
                                portraitCallForAiTarget + "  mCurrentFormatSceneId = " + mCurrentFormatSceneId);*/
                        if (portraitCallForAiTarget) {
                            Integer faceNum = result.get(CaptureResult.VENDOR_AICAMERA_PICSELFIE_STATE);//face num
                            if (faceNum != null) {
                                LogHelper.d(TAG, "[onCaptureCompleted] CAMEAR-AI   faceNum = " + faceNum);
                                if (faceNum == 0) {
                                    mPortraitFramCount++;
                                } else {
                                    mPortraitFramCount = 0;
                                }
                            } else {
                                LogHelper.d(TAG, "[onCaptureCompleted] CAMEAR-AI faceNum = " + faceNum);
                            }
                            LogHelper.d(TAG, "[onCaptureCompleted]  CAMEAR-AI mPortraitFramCount = " + mPortraitFramCount);
                            if (mPortraitFramCount >= 100) {
                                mApp.getAppUi().setPortraitCallForAiTarget(false);
                                updateAiScene(PrizeAiSceneClassify.PRIZE_SCENE_PHOTO);
                                mNeedDelay = true;
                            }
                        }
                        if (aiScene != null) {
                            int newFormatSceneId = formatClassify(aiScene);
                            //LogHelper.d(TAG, "[onCaptureCompleted] CAMEAR-AI newFormatSceneId = " + newFormatSceneId);
                            if (mCurrentFormatSceneId == newFormatSceneId) {
                                return;
                            }
                            if (mAiEnable && mSettingManager != null) {
                                mSettingManager.setAiSceneId(newFormatSceneId);
                            }
                            //LogHelper.d(TAG, "[onCaptureCompleted] mNeedDelay = " + mNeedDelay);
                            //To prevent crashes when switching mode quickly.  @{
                            if (mNeedDelay){
                                mPreviewFramCount ++;
                                if (mPreviewFramCount < 150)
                                    return;
                            }
                            //  @}
                            if (updateAiScene(newFormatSceneId)) {
                                mCurrentFormatSceneId = newFormatSceneId;
                                mPreviewFramCount = 0;
                                mNeedDelay = false;
                            }
                        } else {
                            LogHelper.d(TAG, "[onCaptureCompleted] CAMEAR-AI aiScene = " + aiScene);
                        }
                    }
                }
            }
            /*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogHelper.e(TAG, "[onCaptureFailed], framenumber: " + failure.getFrameNumber()
                    + ", reason: " + failure.getReason() + ", sequenceId: "
                    + failure.getSequenceId() + ", isCaptured: " + failure.wasImageCaptured()
                    +",isStillCapture: "+CameraUtil.isStillCaptureTemplate(request));
            if (mCamera2Proxy == null || session.getDevice() != mCamera2Proxy.getCameraDevice()) {
                return;
            }
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
            if (mModeDeviceCallback != null && CameraUtil.isStillCaptureTemplate(request)) {
                mCaptureFrameMap.remove(String.valueOf(failure.getFrameNumber()));
                updateCameraState(CameraState.CAMERA_OPENED);
                mModeDeviceCallback.onPreviewCallback(null, 0);
                /*prize-modify-add-Restore the camera's status when the photo generation file is abnormal-xiaoping-20181126-start*/
                mModeDeviceCallback.captureFailed();
                /*prize-modify-add-Restore the camera's status when the photo generation file is abnormal-xiaoping-20181126-end*/

            }
        }
    };

    @Override
    public void setZSDStatus(String value) {
        mZsdStatus = value;
    }

    @Override
    public void setFormat(String value) {
        LogHelper.i(TAG, "[setCaptureFormat] value = " + value + " mCameraState = " +
                getCameraState());
        if (CameraState.CAMERA_OPENED == getCameraState() && mCaptureSurface != null) {
            int format = HeifHelper.getCaptureFormat(value);
            mCaptureSurface.setFormat(value);
            mCaptureSurface.updatePictureInfo(format);
        }
    }

    /*prize-modify-add portrait mode -xiaoping-20181212-start*/
    @Override
    public <T> void  setParameterRequest(CaptureRequest.Key<T> key, T value) {
        /*prize-modify-bugid:70714 Camera preview interface is stuck-xiaoping-20190114-start*/
        LogHelper.i(TAG,"mCameraState: "+mCameraState+",mSession: "+mSession+",mBuilder: "+mBuilder);
            if ((mCameraState == CameraState.CAMERA_OPENED || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PROFESSIONAL) && mBuilder != null && mSession != null) {
                synchronized (mBuilder) {
                    LogHelper.i(TAG,"key: "+key+",value: "+value+",mBuilder: "+mBuilder);
                    if (mBuilder != null) {
                            mBuilder.set(key,value);
                        try {
                            mSession.setRepeatingRequest(mBuilder.build(),mCaptureCallback,mModeHandler);
                        } catch (Exception e) {//prize-modify for bug[72316]-huangpengfei-20190306
                            e.printStackTrace();
                        }
                    }
                }

            }
        /*prize-modify-bugid:70714 Camera preview interface is stuck-xiaoping-20190114-end*/
    }
    /*prize-modify-add portrait mode -xiaoping-20181212-end*/

    /*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/

    @Override
    public void onCloseSceneMode(boolean state) {
        mNeedDelay = state;
        mPreviewFramCount = 0;
    }

    @Override
    public boolean updateAiScene(int scene) {
        if (mBuilder != null && mSession != null){
            synchronized (mBuilder) {
                if (mBuilder != null && mSession != null) {
                    switch (scene){
                        case PrizeAiSceneClassify.PRIZE_AI_OFF:
                            mBuilder.set(CaptureRequest.VENDOR_AICAMERA_ENABLE, 0);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI PRIZE_AI_OFF");
                            break;
                        case PrizeAiSceneClassify.PRIZE_AI_ON:
                            mBuilder.set(CaptureRequest.VENDOR_AICAMERA_ENABLE, 1);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI PRIZE_AI_ON");
                            break;
                        case PrizeAiSceneClassify.PRIZE_SCENE_PORTRAIT:
                            mApp.getAppUi().switchPortraitMode();
                            mApp.getAppUi().setPortraitCallForAiTarget(true);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI switchPortraitMode");
                            break;
                        case PrizeAiSceneClassify.PRIZE_SCENE_PHOTO:
                            mApp.getAppUi().switchPhotoMode();
                            mApp.getAppUi().setPortraitCallForAiTarget(false);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI switchPhotoMode");
                            break;
                        case PrizeAiSceneClassify.PRIZE_SCENE_NIGHT:
                            mApp.getAppUi().switchNightMode();
                            mApp.getAppUi().setPortraitCallForAiTarget(false);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI switchNightMode");
                            break;
                        case PrizeAiSceneClassify.PRIZE_SCENE_BACKLIT:
                            mApp.getAppUi().switchHdrMode();
                            mApp.getAppUi().setPortraitCallForAiTarget(false);
                            LogHelper.i(TAG, "[updateAiScene] CAMEAR-AI switchHdrMode");
                            break;
                        default:
                    }
                    try {
                        mSession.setRepeatingRequest(mBuilder.build(), mCaptureCallback, mModeHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    return true;
                }else{
                    LogHelper.i(TAG, "[updateAiScene] AA mBuilder = " + mBuilder + "  mSession = " + mSession);
                    return false;
                }
            }
        }else{
            LogHelper.i(TAG, "[updateAiScene] BB mBuilder = " + mBuilder + "  mSession = " + mSession);
            return false;
        }
    }

    private boolean mAiEnable = false;

    @Override
    public void setAiEnable(boolean enable) {
        LogHelper.d(TAG, "[setAiEnable] enable = "+enable);
        mAiEnable = enable;
        int sceneId = enable? PrizeAiSceneClassify.PRIZE_AI_ON : PrizeAiSceneClassify.PRIZE_AI_OFF;
        updateAiScene(sceneId);
        if (mSettingManager != null){
            mSettingManager.setAiSceneId(sceneId);
        }else{
            LogHelper.d(TAG, "[setAiEnable] mSettingManager is null.");
        }
    }

    @Override
    public void pauseAi(boolean pause) {
        LogHelper.d(TAG, "[pauseAi] pause = " + pause);
        mNeedPauseAi = pause;
    }

    private int formatClassify(int id){
        LogHelper.d(TAG, "[formatClassify]  id = " + id + "  mAiEnable = " + mAiEnable);
        if (!mAiEnable){
            return PrizeAiSceneClassify.PRIZE_AI_OFF;
        }
        int formatSceneId = 0;
        switch (id){
            case PrizeAiSceneClassify.AISD_SCENE_GOURMET:
                formatSceneId = PrizeAiSceneClassify.PRIZE_SCENE_GOURMET;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_GREENERY:
                formatSceneId = PrizeAiSceneClassify.PRIZE_SCENE_GREENERY;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_PORTRAIT:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_PORTRAIT;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_LANDSCAPE:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_LANDSCAPE;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_NIGHT:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_NIGHT;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_BACKLIT:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_BACKLIT;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_SUNSET:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_SUNSET;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_BEACH:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_BEACH;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_BLUESKY:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_BLUESKY;
                break;
            case PrizeAiSceneClassify.AISD_SCENE_URBAN:
                formatSceneId =  PrizeAiSceneClassify.PRIZE_SCENE_URBAN;
                break;
            default:
                break;
        }
        return formatSceneId;
    }
    /*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/


    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-start*/
    @Override
    public void updateCharacteristics(String cameraid) {
        if (mCameraManager != null && (mCameraState ==CameraState.CAMERA_OPENING || mCameraState == CameraState.CAMERA_OPENED)) {
            try {
                mCameraManager.getCameraCharacteristics(mCurrentCameraId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-end*/

    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
    public boolean isNeedPlayAnimation () {
        if (mApp != null && (mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.LOWLIGHT
                || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.BEAUTY
                || "on".equals(mApp.getAppUi().getHdrValue()) || "on".equals(mApp.getAppUi().getPicsflieValue())
                || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE)) {
            return true;
        }
        return false;
    }
    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/

// zhangguo modify 20190422, for picselfiemode set picture size start
    protected void initSettings(){

    }

    protected String getCameraId(){
        return mCurrentCameraId;
    }
// zhangguo modify 20190422, for picselfiemode set picture size end
}
