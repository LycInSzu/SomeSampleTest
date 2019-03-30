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

package com.mediatek.camera.common.mode.photo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUi.AnimationData;
import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.photo.device.DeviceControllerFactory;
import com.mediatek.camera.common.mode.photo.device.IDeviceController;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.DataCallbackInfo;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.DeviceCallback;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.CaptureDataCallback;
import com.mediatek.camera.common.mode.photo.device.IDeviceController.PreviewSizeCallback;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomMode;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.relation.StatusMonitor.StatusChangeListener;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.storage.MediaSaver.MediaSaverListener;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.common.mode.photo.heif.HeifWriter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import android.content.Intent;//prize-add for external intent-huangpengfei-2019-2-16
import com.mediatek.camera.portability.SystemProperties;
/**
 * Normal photo mode that is used to take normal picture.
 */
public class PhotoMode extends CameraModeBase implements CaptureDataCallback,
        DeviceCallback, PreviewSizeCallback, IMemoryManager.IMemoryListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PhotoMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_FORMTAT = "key_format";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IDeviceController mIDeviceController;
    protected PhotoModeHelper mPhotoModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = true;
    protected String mCameraId;//prize-modify for model merging-huangpengfei-2019-02-23

    private ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private byte[] mPreviewData;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    //make sure it is in capturing to show the saving UI.
    private int mCapturingNumber = 0;
    private boolean mIsMatrixDisplayShow = false;
    private Object mPreviewDataSync = new Object();
    private Object mCaptureNumberSync = new Object();
    private HandlerThread mAnimationHandlerThread;
    private Handler mAnimationHandler;
    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;
    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
    private boolean isTakePicture = false;
    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    protected StatusMonitor mStatusMonitor;
    private static final String KEY_SCREEN_FLASH = "key_screen_flash";
    /*prize-add-screen flash-huangzhanbin-20190226-end*/

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);

        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));

        /*prize-add for external intent-huangpengfei-2019-2-16-start*/
        Intent intent = app.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        if (actionMode != null){
            if ("portrait".equals(actionMode)){
                mCameraId = "1";
                mDataStore.setValue(KEY_CAMERA_SWITCHER, CAMERA_FACING_FRONT, mDataStore.getGlobalScope(), true);
            }else if ("ai".equals(actionMode)){
                mDataStore.setValue("key_ai", "on", mDataStore.getCameraScope(0),false);
            }
        }
        /*prize-add for external intent-huangpengfei-2019-2-16-end*/

        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        DeviceControllerFactory deviceControllerFactory = new DeviceControllerFactory();
        /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-start*/
        mIDeviceController = deviceControllerFactory.createDeviceController(app,
                mCameraApi, mICameraContext);
        /*prize-modify-add for Set different preview sizes for different modes-xiaoping-20181205-end*/

        /*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/
        app.getAppUi().setDeviceContrl(mIDeviceController);
        /*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/

        initSettingManager(mCameraId);
        initStatusMonitor();
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        ThumbnailHelper.setApp(app);
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mPhotoModeHelper = new PhotoModeHelper(cameraContext);
        createAnimationHandler();
        LogHelper.d(TAG, "[init]- ");
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        mIsResumed = true;
        /*prize-add for external intent-huangpengfei-2019-2-16-start*/
        Intent intent = mIApp.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        if (actionMode != null){
            if ("portrait".equals(actionMode)){
                mCameraId = "1";
                mIApp.getAppUi().updateCameraId(1);
                mDataStore.setValue(KEY_CAMERA_SWITCHER, CAMERA_FACING_FRONT, mDataStore.getGlobalScope(), true);
            }else if ("uhd".equals(actionMode)){
                mCameraId = "0";
                mIApp.getAppUi().updateCameraId(0);
                mDataStore.setValue(KEY_CAMERA_SWITCHER, CAMERA_FACING_BACK, mDataStore.getGlobalScope(), true);
            }else if ("ai".equals(actionMode)){
                mCameraId = "0";
                mIApp.getAppUi().updateCameraId(0);
                mDataStore.setValue(KEY_CAMERA_SWITCHER, CAMERA_FACING_BACK, mDataStore.getGlobalScope(), true);
                mDataStore.setValue("key_ai", "on", mDataStore.getCameraScope(0),false);
            }
        }
        /*prize-add for external intent-huangpengfei-2019-2-16-end*/
        initSettingManager(mCameraId);
        initStatusMonitor();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(
                mICameraContext.getStorageService().getCaptureStorageSpace());
        mMemoryState = IMemoryManager.MemoryAction.NORMAL;
        mIDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
        if (this instanceof PictureZoomMode && isTakePicture) {
            mIApp.getAppUi().setCaptureStateOnHD(true);
        }
        /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/
        mMemoryManager.removeListener(this);
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        //camera animation.
        // this animation is no need right now so just mark it
        // if some day need can open it.
//        synchronized (mPreviewDataSync) {
//            if (mNeedCloseCameraIds != null && mPreviewData != null) {
//                startChangeModeAnimation();
//            }
//        }
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else {
            clearAllCallbacks(mCameraId);
            mIDeviceController.stopPreview();
        }
        LogHelper.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();
        destroyAnimationHandler();
        mIDeviceController.destroyDeviceController();
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

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP) {

            /*prize-add-screen flash-huangzhanbin-20190226-start*/
            if (needDoScreenFlash()){
                mIApp.getAppUi().updateScreenView(true);
                mModeHandler.removeMessages(SCREEN_FLASH_VIEW_HIDE); // prize add by zhangguo for bug#73462 picture do not callback when show screen flash
                mModeHandler.sendEmptyMessageDelayed(SCREEN_FLASH_TAKE_PICTRE,600);
            }else {
                takePictrue();
            }
            /*prize-add-screen flash-huangzhanbin-20190226-end*/

        }
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public void onDataReceived(DataCallbackInfo dataCallbackInfo) {
        //when mode receive the data, need save it.
        byte[] data = dataCallbackInfo.data;
        int format = dataCallbackInfo.mBufferFormat;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview+",isTakePicture: "+isTakePicture);
        mModeHandler.removeMessages(SCREEN_FLASH_VIEW_HIDE); // prize add by zhangguo for bug#73462 picture do not callback when show screen flash
        mIApp.getAppUi().updateScreenView(false);//add for screen flash by huangzhanbin
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        if (data != null) {
            if (format == ImageFormat.JPEG) {
                /*prize-modify-bugid:70624 Probably unable to enter the album after continuous shooting-xiaoping-20190115-start*/
                if (isTakePicture) {
                    saveData(data);
                } else {
                    LogHelper.w(TAG,"No photo action is currently performed, the current photo is not saved");
                }
                /*prize-modify-bugid:70624 Probably unable to enter the album after continuous shooting-xiaoping-20190115-end*/
            } else if (format == HeifHelper.FORMAT_HEIF){
                //check memory to decide whether it can take next picture.
                //if not, show saving
                ISettingManager.SettingController controller
                  = mISettingManager.getSettingController();
                String dngState = controller.queryValue(KEY_DNG);
                long saveDataSize = data.length;
                if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                    saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
                }
                synchronized (mCaptureNumberSync) {
                    mCapturingNumber ++;
                    mMemoryManager.checkOneShotMemoryAction(saveDataSize);
                }
                HeifHelper heifHelper = new HeifHelper(mICameraContext);
                ContentValues values = heifHelper.getContentValues(dataCallbackInfo.imageWidth,
                        dataCallbackInfo.imageHeight);
                LogHelper.i(TAG, "onDataReceived,heif values =" +values.toString());
                mICameraContext.getMediaSaver().addSaveRequest(data, values, null,
                        mMediaSaverListener, HeifHelper.FORMAT_HEIF);
                //reset the switch camera to null
                synchronized (mPreviewDataSync) {
                    mPreviewData = null;
                }
            }

        }
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                    mIDeviceController.startPreview();
                }
            }
        }
        //update thumbnail
        if (data != null && needUpdateThumbnail) {
            if (format == ImageFormat.JPEG) {
                updateThumbnail(data);
            } else if (format == HeifHelper.FORMAT_HEIF) {
/*                HeifHelper heifHelper = new HeifHelper(mICameraContext);
                int width = dataCallbackInfo.imageWidth;
                int height = dataCallbackInfo.imageHeight;
                Bitmap thumbnail = heifHelper.createBitmapFromYuv(data,
                        width, height, mIApp.getAppUi().getThumbnailViewWidth());
                mIApp.getAppUi().updateThumbnail(thumbnail);*/
            }

        }
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        }
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true);
        /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
        if (data != null && mIsResumed && !(this instanceof PictureZoomMode && mIApp.getAppUi().isInterruptCaptureOnHD())) {
            //will update the thumbnail
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data,
                    ThumbnailHelper.POST_VIEW_FORMAT,
                    ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation);
                mIApp.getAppUi().updateThumbnail(bitmap);
        } else if (this instanceof PictureZoomMode && mIApp.getAppUi().isInterruptCaptureOnHD()) {
            mIApp.getAppUi().setCaptureStateOnHD(false);
        }
        /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, false);
    }

    @Override
    /*protected*/public ISettingManager getSettingManager() {//prize-change-huangpengfei-2018-11-2
        return mISettingManager;
    }

    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
    }

    @Override
    public void afterStopPreview() {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {
        // Because we want use the one preview frame for doing switch camera animation
        // so will dismiss the later frames.
        // The switch camera data will be restore to null when camera close done.
        LogHelper.i(TAG,"format: "+format+",mIsResumed: "+mIsResumed+",isTakePicture: "+isTakePicture);
        if (!mIsResumed) {
            return;
        }
        synchronized (mPreviewDataSync) {
            //Notify preview started.
            if (!mIsMatrixDisplayShow) {
                /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
                if (!isTakePicture  ||  mIApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.INTENTPHOTO) { //prize-modify-bugid:67836
                    mIApp.getAppUi().applyAllUIEnabled(true);
                }
                /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/
            }
            mIApp.getAppUi().onPreviewStarted(mCameraId);
            if (mPreviewData == null) {
                stopAllAnimations();
            }
            updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);

            mPreviewData = data;
            mPreviewFormat = format;
        }
    }

    /*prize-modify-add-Restore the camera's status when the photo generation file is abnormal-xiaoping-20181126-start*/
    @Override
    public void captureFailed() {
        LogHelper.e(TAG,"Photo file generation failed, forced to restore camera status");
        mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
        mIApp.getAppUi().applyAllUIEnabled(true);
        isTakePicture = false;
        mIApp.getAppUi().updateCaptureOrVideoState(false,"capture");
        mIApp.getAppUi().revetButtonState();
    }
    /*prize-modify-add-Restore the camera's status when the photo generation file is abnormal-xiaoping-20181126-end*/

    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    @Override
    protected void takePictrue(){
        //trigger capture animation
        startCaptureAnimation();
        mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
        updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
        disableAllUIExceptionShutter();
        mIDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
        mIDeviceController.takePicture(this);
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
        isTakePicture = true;
        mIApp.getAppUi().updateCaptureOrVideoState(true,"capture");
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/

        mModeHandler.sendEmptyMessageDelayed(SCREEN_FLASH_VIEW_HIDE,2000); // prize add by zhangguo for bug#73462 picture do not callback when show screen flash

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
    /*prize-add-screen flash-huangzhanbin-20190226-end*/

    private void onPreviewSizeChanged(int width, int height) {
        //Need reset the preview data to null if the preview size is changed.
        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
    }

    protected void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId,
            boolean needFastStartPreview) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);
        /*prize-add-screen flash-huangzhanbin-20190226-start*/
        if ("1".equals(cameraId)){
            statusMonitor.registerValueChangedListener(KEY_SCREEN_FLASH, mScreenFlashChangeListener);
        }
        /*prize-add-screen flash-huangzhanbin-20190226-end*/
        //before open camera, prepare the device callback and size changed callback.
        mIDeviceController.setDeviceCallback(this);
        mIDeviceController.setPreviewSizeReadyCallback(this);
        //prepare device info.
        DeviceInfo info = new DeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        info.setNeedOpenCameraSync(needOpenCameraSync);
        info.setNeedFastStartPreview(needFastStartPreview);
        mIDeviceController.openCamera(info);
    }

    protected void prePareAndCloseCamera(boolean needSync, String cameraId) {
        LogHelper.i(TAG,"needSync: "+needSync+",cameraId: "+cameraId);
        /*prize-modify-bugid:67815 67800 press the Home button while taking a photo to exit and enter the camera, the camera is abnormal-xiaoping-20181107-start*/
        isTakePicture = false;
        mIApp.getAppUi().updateCaptureOrVideoState(false,"capture");
        mIApp.getAppUi().revetButtonState();
        /*prize-modify-bugid:67815 67800 press the Home button while taking a photo to exit and enter the camera, the camera is abnormal-xiaoping-20181107-end*/
        clearAllCallbacks(cameraId);
        mIDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size and preview data.
        mPreviewData = null;
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mIDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(KEY_FORMTAT, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(
                KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);
        /*prize-add-screen flash-huangzhanbin-20190226-start*/
        statusMonitor.unregisterValueChangedListener(
                KEY_SCREEN_FLASH, mScreenFlashChangeListener);
        /*prize-add-screen flash-huangzhanbin-20190226-end*/
    }

    private void initSettingManager(String cameraId) {
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(
                cameraId,
                getModeKey(),
                ModeType.PHOTO,
                mCameraApi);
    }

    private void recycleSettingManager(String cameraId) {
        mICameraContext.getSettingManagerFactory().recycle(cameraId);
    }

    private void createAnimationHandler() {
        mAnimationHandlerThread = new HandlerThread("Animation_handler");
        mAnimationHandlerThread.start();
        mAnimationHandler = new Handler(mAnimationHandlerThread.getLooper());
    }

    private void destroyAnimationHandler() {
        if (mAnimationHandlerThread.isAlive()) {
            mAnimationHandlerThread.quit();
            mAnimationHandler = null;
        }
    }

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        boolean value = true;

        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)) {
            value = false;
        }
        LogHelper.d(TAG, "[canSelectCamera] +: " + value);
        return value;
    }

    private void doCameraSelect(String oldCamera, String newCamera) {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mIApp.getAppUi().onCameraSelected(newCamera);
        prePareAndCloseCamera(true, oldCamera);
        recycleSettingManager(oldCamera);
        initSettingManager(newCamera);

        /*prize-add for model merging-huangpengfei-2019-02-23-start*/
        prepareAndOpenCamera(false, newCamera, true);
        /*prize-add for model merging-huangpengfei-2019-02-23-end*/

        /*prize-modify-fix Camera opencamera twice-xiaoping-20181122-start*/
        boolean isNomal = ("1").equals(android.os.SystemProperties.get("ro.pri_def_front_mode_normal", "0"));
/*        if (isNomal){
            prepareAndOpenCamera(false, newCamera, true);
        }else if ((Integer.valueOf(oldCamera) == 0  && mIApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.BEAUTY)
                || ((Integer.valueOf(oldCamera) == 1) && mIApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO)
                || mIApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.INTENTPHOTO) {
            prepareAndOpenCamera(false, newCamera, true);
        }*/
        /*prize-modify-fix Camera opencamera twice-xiaoping-20181122-end*/
    }

    private MediaSaverListener mMediaSaverListener = new MediaSaverListener() {

        @Override
        public void onFileSaved(Uri uri) {
            mIApp.notifyNewMedia(uri, true);
            synchronized (mCaptureNumberSync) {
                mCapturingNumber--;
                if (mCapturingNumber == 0) {
                    mMemoryState = IMemoryManager.MemoryAction.NORMAL;
                    mIApp.getAppUi().hideSavingDialog();
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
                    mIApp.getAppUi().applyAllUIEnabled(true);
                    isTakePicture = false;
                    mIApp.getAppUi().updateCaptureOrVideoState(false,"capture");
                    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
//                    mIApp.getAppUi().revetButtonState();
                    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/

                    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/
                }
            }
            LogHelper.d(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
                    + mCapturingNumber);
        }
    };

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

    private void startSwitchCameraAnimation() {
        // Prepare the animation data.
        AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
                mPreviewHeight, mPreviewFormat);
        // Trigger animation start.
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_SWITCH_CAMERA, data);
    }

    private void stopSwitchCameraAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_CAMERA);
    }

    private void startChangeModeAnimation() {
        AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
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

    private AnimationData prepareAnimationData(byte[] data, int width, int height, int format) {
        // Prepare the animation data.
        AnimationData animationData = new AnimationData();
        animationData.mData = data;
        animationData.mWidth = width;
        animationData.mHeight = height;
        animationData.mFormat = format;
        animationData.mOrientation = mPhotoModeHelper.getCameraInfoOrientation(mCameraId,
                mIApp.getActivity());
        animationData.mIsMirror = mPhotoModeHelper.isMirror(mCameraId, mIApp.getActivity());
        return animationData;
    }

    private void updatePictureSizeAndPreviewSize(Size previewSize) {
        ISettingManager.SettingController controller = mISettingManager.getSettingController();
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
                    " X" + mCaptureHeight + ",current preview size:" + mPreviewWidth + " X " +
                    mPreviewHeight + ",new value :" + width + " X " + height);
            if (width != mPreviewWidth || height != mPreviewHeight) {
                onPreviewSizeChanged(width, height);
            }
        }

    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mPhotoStatusResponder = statusMonitor.getStatusResponder(KEY_PHOTO_CAPTURE);
    }


    private void saveData(byte[] data) {
        if (data != null) {
            //check memory to decide whether it can take next picture.
            //if not, show saving
            ISettingManager.SettingController controller = mISettingManager.getSettingController();
            String dngState = controller.queryValue(KEY_DNG);
            long saveDataSize = data.length;
            if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
            }
            synchronized (mCaptureNumberSync) {
                mCapturingNumber ++;
                mMemoryManager.checkOneShotMemoryAction(saveDataSize);
            }
            /*prize-modify-increase external storage-xiaoping-20190111-start*/
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            /*prize-modify-bugid:72856-Change camera default storage to sd card-xiaoping-20190322-start*/
            String defaultvalue = "phone";
            if (SystemProperties.getInt("ro.pri.current.project",0) == 3) {
                defaultvalue = "sd";
            }
            if (mIApp.isExtranelStorageMount() /*&& mIApp.isHasPermissionForSD() modify for bug 71247*/
                    && "sd".equals(mIApp.getSettingValue("key_storagepath",defaultvalue,mIApp.getAppUi().getCameraId()))) {
                /*prize-modify-bugid:72856-Change camera default storage to sd card-xiaoping-20190322-end*/
                fileDirectory = mIApp.getCameraDirectorySD();
            } else {
                fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            }
            /*prize-modify-increase external storage-xiaoping-20190111-end*/
            Size exifSize = CameraUtil.getSizeFromExif(data);
            ContentValues contentValues = mPhotoModeHelper.createContentValues(data,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            mICameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                    mMediaSaverListener);
            //reset the switch camera to null
            synchronized (mPreviewDataSync) {
                mPreviewData = null;
            }
        }
    }

    private void disableAllUIExceptionShutter() {
        mIApp.getAppUi().applyAllUIEnabled(false);
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
//        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_BUTTON, true);
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/
        mIApp.getAppUi().setUIEnabled(IAppUi.SHUTTER_TEXT, false);
    }

    private void updateThumbnail(byte[] data) {
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(data, mIApp.getAppUi()
                .getThumbnailViewWidth());
        mIApp.getAppUi().updateThumbnail(bitmap);
    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        if (state == IMemoryManager.MemoryAction.STOP && mCapturingNumber != 0) {
            //show saving
            LogHelper.d(TAG, "memory low, show saving");
            mIApp.getAppUi().showSavingDialog(null, true);
            mIApp.getAppUi().applyAllUIVisibility(View.INVISIBLE);
        }
    }

    /**
     * surface changed listener.
     */
    private class SurfaceChangeListener implements ISurfaceStatusListener {

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceAvailable,device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIDeviceController != null && mIsResumed) {
                            mIDeviceController.updatePreviewSurface(surfaceObject);
                        }
                    }
                });
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceChanged, device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIDeviceController != null && mIsResumed) {
                            mIDeviceController.updatePreviewSurface(surfaceObject);
                        }
                    }
                });
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceDestroyed,device controller = " + mIDeviceController);
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            } else if (KEY_FORMTAT.equalsIgnoreCase(key)) {
                mIDeviceController.setFormat(value);
                LogHelper.i(TAG, "[onStatusChanged] key = " + key + ", set sCaptureFormat = " + value);
            }
        }
    }
}
