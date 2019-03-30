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

package com.mediatek.camera.common.mode;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.feature.setting.volumekeys.VolumekeyState;//prize-add-huangpengfei-2018-12-19

import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * A common abstract {@link ICameraMode} implementation that contains some
 * utility functions and plumbing we don't want every sub-class of {@link ICameraMode}
 * to duplicate. Hence all {@link ICameraMode} implementation should sub-class this class
 * instead.
 */
public abstract class CameraModeBase implements
                            ICameraMode,
                            IAppUiListener.OnShutterButtonListener,
                            IApp.BackPressedListener,
                            IApp.OnOrientationChangeListener,
                            IApp.KeyEventListener {
    private LogUtil.Tag mTag;
    /*prize-modify for external intent-huangpengfei-2019-2-16-start*/
    protected static final String CAMERA_FACING_BACK = "back";
    protected static final String CAMERA_FACING_FRONT = "front";
    /*prize-modify for external intent-huangpengfei-2019-2-16-end*/
    protected static final String BACK_CAMERA_ID = "0";
    protected static final String FRONT_CAMERA_ID = "1";

    protected static final String KEY_CAMERA_SWITCHER = "key_camera_switcher";
    protected static final int LOWEST_PRIORITY = -1;//prize-custom the shoot key function-tangan-begin
    private static final int MSG_MODE_ON_SHUTTER_BUTTON_CLICK = 0;
    protected IApp mIApp;
    protected ICameraContext mICameraContext;
    protected DataStore mDataStore;
    protected CameraApi mCameraApi;
    protected DeviceUsage mCurrentModeDeviceUsage;
    protected DeviceUsage mNextModeDeviceUsage;
    protected ArrayList<String> mNeedCloseCameraIds = new ArrayList<>();

    private volatile String mModeDeviceStatus = MODE_DEVICE_STATE_UNKNOWN;
    protected ModeHandler mModeHandler;
    /*prize-add-haungpengfei-2018-10-24-start*/
    protected String mValue;
    private boolean mIsCameraKeyLongPressed = false;
    /*prize-add-haungpengfei-2018-10-24-end*/

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    protected final static int SCREEN_FLASH_TAKE_PICTRE = 100;
    protected final static int SCREEN_FLASH_VIEW_HIDE = 101;
    protected static final String SCREEN_FLASH_KEY = "key_screen_flash";
    protected  boolean isFlashWillOn = false;
    protected boolean isFlashOnWhenCapture = false;
    /*prize-add-screen flash-huangzhanbin-20190226-end*/

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
            boolean isFromLaunch) {
        mTag = new LogUtil.Tag(getClass().getSimpleName());
        updateModeDefinedCameraApi();
        mIApp = app;
        mIApp.getAppUi().applyAllUIEnabled(false);
        updateModeDeviceState(MODE_DEVICE_STATE_UNKNOWN);
        mICameraContext = cameraContext;
        mDataStore = cameraContext.getDataStore();
        mModeHandler = new ModeHandler(Looper.myLooper());

        app.registerBackPressedListener(this, IApp.DEFAULT_PRIORITY);
        app.registerKeyEventListener(this, IApp.DEFAULT_PRIORITY);
        app.registerOnOrientationChangeListener(this);
        app.getAppUi().registerOnShutterButtonListener(this, IAppUi.DEFAULT_PRIORITY);
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        mIApp.getAppUi().applyAllUIEnabled(false);
        mCurrentModeDeviceUsage = deviceUsage;
    }

    @Override
    public void pause(@Nonnull DeviceUsage nextModeDeviceUsage) {
        mIApp.getAppUi().applyAllUIEnabled(false);
        //must use the old device usage get the camera id.
        //because when pause activity, the newModeDeviceUsage is null.
        //so you can not change to as:newModeDeviceUsage.getNeedClosedCameraIds(
        // mCurrentModeDeviceUsage);
        mNextModeDeviceUsage = nextModeDeviceUsage;
        mNeedCloseCameraIds = mCurrentModeDeviceUsage.getNeedClosedCameraIds(nextModeDeviceUsage);
    }

    @Override
    public void unInit() {
        mIApp.unRegisterBackPressedListener(this);
        mIApp.unRegisterKeyEventListener(this);
        mIApp.unregisterOnOrientationChangeListener(this);
        mIApp.getAppUi().unregisterOnShutterButtonListener(this);
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        return false;
    }

    @Override
    public String getModeKey() {
        return getClass().getName();
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        return false;
    }

    @Override
    public boolean onShutterButtonClick() {
        LogHelper.d(mTag, "[onShutterButtonClick] UI thread");
        if (mModeHandler.hasMessages(MSG_MODE_ON_SHUTTER_BUTTON_CLICK)) {
            return true;
        }
        mModeHandler.sendEmptyMessage(MSG_MODE_ON_SHUTTER_BUTTON_CLICK);
        // TODO when return ture and when return false
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        /*prize-modify-optimize the photo animation process-xiaoping-20181121-start*/
        LogHelper.i("","stop captureanimation");
        mIApp.getAppUi().revetButtonState();
        /*prize-modify-optimize the photo animation process-xiaoping-20181121-end*/
        return false;
    }

	/*prize-change-haungpengfei-2018-10-24-start*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogHelper.d(mTag, "[onKeyDown]  keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                getVolumeKey();
                if ("0".equals(mValue)){
                    return false;
                }else if ("1".equals(mValue)){
                    int repeatCount = event.getRepeatCount();
                    if (repeatCount > 0){
                        if (mIsCameraKeyLongPressed){return true;}
                        mIsCameraKeyLongPressed = true;
                        mIApp.getAppUi().triggerShutterButtonLongPressed(LOWEST_PRIORITY);
                        mIApp.getAppUi().setVolumekeyState(VolumekeyState.VOLUME_KEY_STATE_DOWN);//prize-add-huangpengfei-2018-12-19
                        LogHelper.d(mTag, "[onKeyDown]  triggerShutterButtonLongPressed.. ");
                    }
                    return true;
                }else if ("2".equals(mValue)){
                    return false;
                }
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogHelper.d(mTag, "[onKeyUp]  keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                getVolumeKey();
                if ("0".equals(mValue)){
                    return false;
                }else if ("1".equals(mValue)){
                    if (!mIsCameraKeyLongPressed){
                        mIApp.getAppUi().triggerShutterButtonClick(LOWEST_PRIORITY);
                        LogHelper.d(mTag, "[onKeyUp]  triggerShutterButtonClick.. ");
                    }else{
                        mIApp.getAppUi().stopContinuousShot();
                        mIApp.getAppUi().setVolumekeyState(VolumekeyState.VOLUME_KEY_STATE_UP);//prize-add-huangpengfei-2018-12-19
                    }
                    mIsCameraKeyLongPressed = false;
                    return true;
                }else if ("2".equals(mValue)){
                    return false;
                }
            default:
                break;
        }
        return false;
    }

    private void getVolumeKey() {
        ISettingManager.SettingController settingController = getSettingManager().getSettingController();
        String cameraScope = mDataStore.getCameraScope(Integer.parseInt(settingController.getCameraId()));
        mValue = mDataStore.getValue("key_volumekeys", "1", cameraScope);
        LogHelper.d(mTag, "[getVolumeKey]  mValue = " + mValue);
    }
	/*prize-change-haungpengfei-2018-10-24-end*/

    @Override
    public void onOrientationChanged(int orientation) {}

    @Override
    public CameraApi getCameraApi() {
        updateModeDefinedCameraApi();
        return mCameraApi;
    }

    @Override
    public DeviceUsage getDeviceUsage(@Nonnull DataStore dataStore, DeviceUsage oldDeviceUsage) {
        ArrayList<String> openedCameraIds = new ArrayList<>();
        String cameraId = getCameraIdByFacing(dataStore.getValue(
                KEY_CAMERA_SWITCHER, null, dataStore.getGlobalScope()));
        openedCameraIds.add(cameraId);
        updateModeDefinedCameraApi();
        return new DeviceUsage(DeviceUsage.DEVICE_TYPE_NORMAL, mCameraApi, openedCameraIds);
    }

    @Override
    public boolean isModeIdle() {
        return true;
    }

    @Override
    public boolean onUserInteraction() {
        if (mIApp != null) {
            mIApp.enableKeepScreenOn(false);
        }
        return true;
    }

    /**
     * Update mode defined camera api.
     */
    protected void updateModeDefinedCameraApi() {
        if (mCameraApi == null) {
            mCameraApi = CameraApiHelper.getCameraApiType(getClass().getSimpleName());
        }
    }

    /**
     * Get setting manager instance.
     * @return the setting manager instance.
     */
    protected abstract ISettingManager getSettingManager();

    /**
     * Get the camera id according to the camera facing info.
     *
     * @param cameraFacing The input camera facing.
     * @return The camera id which has the input facing.
     */
    protected String getCameraIdByFacing(String cameraFacing) {
        String cameraId = BACK_CAMERA_ID;
        if (cameraFacing == null || CAMERA_FACING_BACK.equals(cameraFacing)) {
            cameraId = BACK_CAMERA_ID;
        } else if (CAMERA_FACING_FRONT.equals(cameraFacing)) {
            cameraId = FRONT_CAMERA_ID;
        }
        return cameraId;
    }

    protected void updateModeDeviceState(final String state) {
        mModeDeviceStatus = state;
        final String modeName = getClass().getSimpleName();
        ISettingManager settingManager = getSettingManager();
        if (settingManager != null) {
            settingManager.updateModeDeviceStateToSetting(modeName, state);
        }
        mIApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(mTag, "Change mode device state to " + state);
                mIApp.getAppUi().getShutterRootView().setContentDescription(
                        modeName + " is " + state);

            }
        });
    }

    protected String getModeDeviceStatus() {
        return mModeDeviceStatus;
    }

    /**
     * Need close camera sync:
     *    device usage type is different.
     *    camera id list is different:such as number or value is different.
     * @return if normal is vsdof and stereo mode when exit camera the value is true.
     *         change mode case,if next mode device type and api is same as is false,
     *         other case is true.
     */
    protected boolean needCloseCameraSync() {
        String currentType = mCurrentModeDeviceUsage.getDeviceType();

        // if mNextModeDeviceUsage === null means just exit camera activity.
        if (mNextModeDeviceUsage == null) {
            //but stereo type need close camera sync.
            // normal case means async, such as normal photo/video/pip mode.
            boolean isStereo = DeviceUsage.DEVICE_TYPE_STEREO.equals(currentType);
            boolean isVsdof = DeviceUsage.DEVICE_TYPE_STEREO_VSDOF.equals(currentType);
            return isStereo || isVsdof;
        }

        //change mode case:
        //if device type and api both are same as, don't need close camera sync.
        boolean isSameType = currentType.equals(mNextModeDeviceUsage.getDeviceType());
        boolean isSameApi = mCurrentModeDeviceUsage.getCameraApi().equals(
                mNextModeDeviceUsage.getCameraApi());
        return !isSameType || !isSameApi || isTeleDevice();
    }

    /**
     * if current camera id equal back camera id or
     * front camera id, return false, else return true.
     * Tele device need close camera sync,
     * may be lead to open too much camera or surface
     * not ready.eg:panorama(tele) switch to PIP
     */
    private boolean isTeleDevice() {
        if (mDataStore == null) {
            LogHelper.i(mTag, "[isTeleDevice] null mDataStore!");
            return false;
        }
        String cameraId = mDataStore.getValue(KEY_CAMERA_SWITCHER, null,
                mDataStore.getGlobalScope());
        LogHelper.d(mTag, "[isTeleDevice] cameraId:" + cameraId);
        return !(BACK_CAMERA_ID.equals(cameraId) || FRONT_CAMERA_ID.equals(cameraId));
    }

    /**
     * Mode handler run in mode thread.
     */
    protected class ModeHandler extends Handler {
        public ModeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MODE_ON_SHUTTER_BUTTON_CLICK:
                    doShutterButtonClick();
                    break;
                /*prize-add-screen flash-huangzhanbin-20190226-start*/
                case SCREEN_FLASH_TAKE_PICTRE:
                    takePictrue();
                    break;
                case SCREEN_FLASH_VIEW_HIDE:
                    mIApp.getAppUi().updateScreenView(false);
                    break;
                /*prize-add-screen flash-huangzhanbin-20190226-end*/
                default:
                    break;
            }
        }
    }

    protected boolean doShutterButtonClick() {
        return false;
    }

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    protected void takePictrue(){

    }

    protected boolean needDoScreenFlash(){
        if (mDataStore == null){
            isFlashOnWhenCapture = false;
            return false;
        }
        ISettingManager.SettingController settingController = getSettingManager().getSettingController();
        String cameraId = settingController.getCameraId();
        if (!"1".equalsIgnoreCase(cameraId)){
            isFlashOnWhenCapture = false;
            return false;
        }
        String value = mDataStore.getValue(SCREEN_FLASH_KEY,"auto",mDataStore.getCameraScope(Integer.parseInt(cameraId)));
        if ("screen".equalsIgnoreCase(value)){
            isFlashOnWhenCapture = true;
            return true;
        }else if ("auto".equalsIgnoreCase(value)){
            isFlashOnWhenCapture = isFlashWillOn;
            return isFlashWillOn;
        }
        isFlashOnWhenCapture = false;
        return false;
    }

    protected void setFlashWillOn(boolean on){
        isFlashWillOn = on;
    }

    /*prize-add-screen flash-huangzhanbin-20190226-end*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}