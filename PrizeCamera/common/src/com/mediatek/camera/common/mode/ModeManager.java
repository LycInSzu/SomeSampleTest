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

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import com.mediatek.camera.common.CameraContext;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.FeatureProvider;
import com.mediatek.camera.common.loader.FeatureProvider.FeatureLoadDoneListener;
/*prize-add-add mode-xiaoping-20180901-begin*/
import com.mediatek.camera.common.mode.beauty.FaceBeautyModeEntry;
import com.mediatek.camera.common.mode.lowlight.LowLightModeEntry;
import com.mediatek.camera.common.mode.photo.PhotoModeEntry;
import com.mediatek.camera.common.mode.photo.intent.IntentPhotoMode;
import com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomModeEntry;
/*prize-add-add mode-xiaoping-20180901-end*/
/*prize-add-huangpengfei-2018-11-2-start*/
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.mode.video.VideoMode;
import com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.SettingManager;
/*prize-add-huangpengfei-2018-11-2-end*/
/*prize-add for model merging-huangpengfei-2019-02-23-start*/
import android.view.View;
import com.mediatek.camera.common.mode.picselfie.PicselfieModeEntry;
import com.mediatek.camera.common.mode.video.VideoModeEntry;
/*prize-add for model merging-huangpengfei-2019-02-23-end*/
import com.mediatek.camera.common.utils.AtomAccessor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mediatek.camera.prize.FeatureSwitcher;

// prize add by zhangguo for front camera 20190401
import com.prize.camera.feature.mode.gif.GifModeEntry;
import com.prize.camera.feature.mode.filter.FilterModeEntry;
import com.prize.camera.feature.mode.pano.PanoModeEntry;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
// prize add by zhangguo end

/**
 * Used to manage camera mode type features,the main responsibilities are as follows:
 * 1.Register available mode features to mode list ui.
 * 2.Do camera mode switch.
 */
public class ModeManager implements IModeListener, IAppUiListener.OnModeChangeListener {
    private static final Tag TAG = new Tag(ModeManager.class.getSimpleName());

    private static final String EXTRA_CAPTURE_MODE = "extra_capture_mode";
    private static final String DEFAULT_CAPTURE_MODE =   /* intent for gts  begin */
            "com.mediatek.camera.common.mode.photo.PhotoModeEntry";
    private static final String DEFAULT_VIDEO_MODE =    /* intent for gts  begin */
            "com.mediatek.camera.common.mode.video.VideoModeEntry";
    private static final String DEFAULT_INTENT_PHOTO_MODE =
            "com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry";
    private static final String DEFAULT_INTENT_VIDEO_MODE =
            "com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry";
    private static final int MSG_MODE_INIT = 2;
    private static final int MSG_MODE_RESUME = 3;
    private static final int MSG_MODE_UNINIT = 4;
    private static final int MSG_MODE_PAUSE = 5;
    private static final int MSG_MODE_ON_CAMERA_SELECTED = 6;
    private static final int MSG_MODE_ON_ACTIVITY_RESULT = 7;
    private final FeatureLoadListener mPluginLoadListener =
            new FeatureLoadListener();

    private String mCurrentEntryKey;
    private CameraApi mCurrentCameraApi;
    private ICameraMode mNewMode;
    private ICameraMode mOldMode;
    private ArrayList<ICameraMode> mBusyModeList = new ArrayList<ICameraMode>();

    private IApp mApp;
    private IAppUi mAppUi;
    private ICameraContext mCameraContext;
    private DeviceUsage mCurrentModeDeviceUsage = null;

    private boolean mResumed = false;
    private ModeHandler mModeHandler;
    private AtomAccessor mAtomAccessor = new AtomAccessor();
    private volatile boolean mSelectedResult = false;
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    private int mCameraId = 0;
    private int currentCameraId = -1;
    private static final int FRONT_CAMERAID = 1;
    private static final int BACK_CAMERAID = 0;
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
    /*prize-modify-repeat switching mode after switch camera-xiaoping-20181017-start*/
    private boolean isFirstOpenCamera = true;
    /*prize-modify-repeat switching mode after switch camera-xiaoping-20181017-end*/

    /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-start*/
    private DeviceUsage mNewDeviceUsage = null;
    /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-end*/

    // zhangguo add 20190429, for bug#75110 picselfie icon state is error
    private String KEY_RESTORE_SETTINGS = "key_restore_settings";

    @Override
    public void create(@Nonnull IApp app) {
        LogHelper.d(TAG, "[create]+");
        mApp = app;
        HandlerThread th = new HandlerThread("mode thread");
        th.start();
        mModeHandler = new ModeHandler(th.getLooper());
        th.getLooper().getThread().setPriority(Thread.MAX_PRIORITY);

        mCameraContext = new CameraContext();
        mCameraContext.create(mApp, mApp.getActivity());
        mAppUi = app.getAppUi();
        mAppUi.setModeChangeListener(this);

        String defaultModeKey = getDefaultModeKey();
        LogHelper.i(TAG, "[create], default mode:" + defaultModeKey);

        mNewMode = createMode(defaultModeKey);
        mModeHandler.obtainMessage(MSG_MODE_INIT,
                new MsgParam(mNewMode, true)).sendToTarget();
        //this can't be after mOldMode = mNewMode;
        mCurrentModeDeviceUsage = createDeviceUsage(mNewMode);
        mCurrentCameraApi = mNewMode.getCameraApi();
        mOldMode = mNewMode;
        mCameraContext.getFeatureProvider().registerFeatureLoadDoneListener(mPluginLoadListener);
        LogHelper.d(TAG, "[create]-");
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
        /*prize-modify for external intent-huangpengfei-2019-2-16-start*/
        Intent intent = app.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        if (actionMode != null){
            if ("portrait".equals(actionMode)){
                mAppUi.updateCameraId(FRONT_CAMERAID);
            }
        }else {
            mAppUi.updateCameraId(BACK_CAMERAID);
        }
        //mAppUi.updateCameraId(BACK_CAMERAID);
        /*prize-modify for external intent-huangpengfei-2019-2-16-end*/
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
    }

    @Override
    public void resume() {
        LogHelper.i(TAG, "[resume]");
        mCameraContext.resume();
        mResumed = true;
        mCameraContext.getFeatureProvider().updateCurrentModeKey(mNewMode.getModeKey());
        mModeHandler.obtainMessage(MSG_MODE_RESUME,
                new MsgParam(mNewMode, mCurrentModeDeviceUsage)).sendToTarget();
        /*prize-modify for external intent-huangpengfei-2019-2-16-start*/
        Intent intent = mApp.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        if (actionMode != null){
            if ("portrait".equals(actionMode)){
                mAppUi.updateCameraId(FRONT_CAMERAID);
                mCameraId = FRONT_CAMERAID;
            }else {
                mAppUi.updateCameraId(BACK_CAMERAID);
                mCameraId = BACK_CAMERAID;
            }
        }
        /*prize-modify for external intent-huangpengfei-2019-2-16-end*/

        /*prize-add-screen flash-huangzhanbin-20190226-start*/
        mAppUi.updateScreenView(false);
        /*prize-add-screen flash-huangzhanbin-20190226-end*/
    }

    @Override
    public void pause() {
        LogHelper.i(TAG, "[pause]");
        mResumed = false;
        mModeHandler.obtainMessage(MSG_MODE_PAUSE,
                new MsgParam(mNewMode, null)).sendToTarget();
        mCameraContext.pause();
        mCameraContext.getFeatureProvider().updateCurrentModeKey(null);
        /*prize-add-screen flash-huangzhanbin-20190226-start*/
        mAppUi.updateScreenView(false);
        /*prize-add-screen flash-huangzhanbin-20190226-end*/
    }

    @Override
    public void destroy() {
        LogHelper.i(TAG, "[destroy]");
        Message msg = mModeHandler.obtainMessage(MSG_MODE_UNINIT,
                new MsgParam(mNewMode, null));
        mAtomAccessor.sendAtomMessageAndWait(mModeHandler, msg);
        mModeHandler.getLooper().quit();

        mAppUi.setModeChangeListener(null);
        mCameraContext.getFeatureProvider().unregisterPluginLoadDoneListener(
                mPluginLoadListener);
        mCameraContext.destroy();
    }

    @Override
    public void onModeSelected(@Nonnull String newModeKey) {
        LogHelper.i(TAG, "[onModeSelected], (" + mCurrentEntryKey + " -> " + newModeKey + ")");
        if (newModeKey.equals(mCurrentEntryKey)) {
            //return;//prize-modify for bug[70707]-huangpengfei-2019-01-16
        }
        if (!mResumed) {
            LogHelper.d(TAG, "[onModeSelected], don't do mode change for state isn't resumed," +
                    " so return");
            return;
        }

        /*prize-add for model merging-huangpengfei-2019-02-23-start*/
        boolean isNeedSwitchCameraFirst = checkAndSelectCamera(newModeKey);
        if (isNeedSwitchCameraFirst) return;
        /*prize-add for model merging-huangpengfei-2019-02-23-end*/

        mNewMode = createMode(newModeKey);
        DeviceUsage newUsage = createDeviceUsage(mNewMode);
        mModeHandler.obtainMessage(MSG_MODE_PAUSE,
                new MsgParam(mOldMode, newUsage)).sendToTarget();
        // Disable ui immediately when switch mode
        mAppUi.applyAllUIEnabled(false);
        mModeHandler.obtainMessage(MSG_MODE_UNINIT,
                new MsgParam(mOldMode, null)).sendToTarget();
        mAppUi.updateCurrentMode(mCurrentEntryKey);
        mModeHandler.obtainMessage(MSG_MODE_INIT,
                new MsgParam(mNewMode, false)).sendToTarget();
        mModeHandler.obtainMessage(MSG_MODE_RESUME,
                new MsgParam(mNewMode, newUsage)).sendToTarget();
        //cache stereo mode avoid GC if not idle and remove when idle.
        cacheModeByIdleStatus();
        mCurrentModeDeviceUsage = newUsage;
        mOldMode = mNewMode;
    }

    @Override
    public boolean onCameraSelected(@Nonnull String cameraFacing) {
        LogHelper.i(TAG, "[onCameraSelected], switch to camera:" + cameraFacing);

        // Disable ui immediately when switch camera.
        mAppUi.applyAllUIEnabled(false);
        Message msg = mModeHandler.obtainMessage(MSG_MODE_ON_CAMERA_SELECTED,
                new MsgParam(mNewMode, cameraFacing));
        mAtomAccessor.sendAtomMessageAndWait(mModeHandler, msg);
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
        mAppUi.updateCameraId(Integer.valueOf(cameraFacing));
        mCameraId = Integer.valueOf(cameraFacing);
        mPluginLoadListener.onBuildInLoadDone(cameraFacing,CameraApi.API2);
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
        return mSelectedResult;
    }

    @Override
    public boolean onUserInteraction() {
        return mNewMode.onUserInteraction();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Message msg = mModeHandler.obtainMessage(MSG_MODE_ON_ACTIVITY_RESULT,
                new MsgParam(mNewMode, data));
        msg.arg1 = requestCode;
        msg.arg2 = resultCode;
        mAtomAccessor.sendAtomMessageAndWait(mModeHandler, msg);
    }

    private String getDefaultModeKey() {
        String defaultModeKey = DEFAULT_CAPTURE_MODE;   /* intent for gts  begin */
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)) {
            defaultModeKey = DEFAULT_INTENT_PHOTO_MODE;
        } else if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            defaultModeKey = DEFAULT_INTENT_VIDEO_MODE;
        } else if (MediaStore.INTENT_ACTION_VIDEO_CAMERA.equals(action)) {    /* intent for gts  begin */
            defaultModeKey = DEFAULT_VIDEO_MODE;
        }
        // Check capture mode is assigned by 3rd party APP or not.
        String extraCaptureMode = intent.getStringExtra(EXTRA_CAPTURE_MODE);
        LogHelper.i(TAG, "[getDefaultModeKey]extraCaptureMode = " + extraCaptureMode);
        if (extraCaptureMode != null) {
            defaultModeKey = extraCaptureMode;
        }
        return defaultModeKey;
    }

    private ICameraMode createMode(String entryKey) {
        String tempEntryKey = entryKey;
        ICameraMode cameraMode = mCameraContext.getFeatureProvider().getInstance(
                new FeatureProvider.Key<>(tempEntryKey, ICameraMode.class),
                null,
                false);
        // if current entry key can not create mode, back to default camera mode.
        if (cameraMode == null) {
            tempEntryKey = DEFAULT_CAPTURE_MODE;  /* intent for gts  begin */
            cameraMode = mCameraContext.getFeatureProvider().getInstance(
                new FeatureProvider.Key<>(tempEntryKey, ICameraMode.class),
                null,
                false); // don't check support, because camera may not opened.
        }
        mCurrentEntryKey = tempEntryKey;
        mCameraContext.getFeatureProvider().updateCurrentModeKey(cameraMode.getModeKey());
        LogHelper.i(TAG, "[createMode] entryKey:" + mCurrentEntryKey);
        return cameraMode;
    }

    private DeviceUsage createDeviceUsage(ICameraMode currentMode) {
        if (mOldMode != null) {
            //update current old mode device usage again.
            mCurrentModeDeviceUsage = mOldMode.getDeviceUsage(mCameraContext.getDataStore(), null);
            mCurrentModeDeviceUsage = mCameraContext.getFeatureProvider().updateDeviceUsage(
                    mOldMode.getModeKey(), mCurrentModeDeviceUsage);
        }
        DeviceUsage newDeviceUsage = currentMode.getDeviceUsage(mCameraContext.getDataStore(),
                mCurrentModeDeviceUsage);
        String modeKey = currentMode.getModeKey();
        /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-start*/
        mNewDeviceUsage = newDeviceUsage;
        /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-end*/
        return mCameraContext.getFeatureProvider().updateDeviceUsage(modeKey, newDeviceUsage);
    }

    /**
     * An implement of {@link FeatureLoadDoneListener} for mode.
     */
    private class FeatureLoadListener implements FeatureLoadDoneListener {
        @Override
        public void onBuildInLoadDone(String cameraId, CameraApi cameraApi) {
            LogHelper.d(TAG, "[onBuildInLoadDone]+ api:" + cameraApi +
                    ", current api:" + mCurrentCameraApi +
                    ",camId:" + cameraId);
            List<IAppUi.ModeItem> modeItems = new ArrayList<>();
            if (cameraApi.equals(mCurrentCameraApi)) {
                modeItems =
                        mCameraContext.getFeatureProvider().getAllModeItems(mCurrentCameraApi);
                /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
                if (modeItems.size() > 0 && mCameraId != currentCameraId) {
                    mAppUi.registerMode(modeItems);
                    /*prize-modify-repeat switching mode after switch camera-xiaoping-20181017-start*/
                    if (isFirstOpenCamera) {
                        mAppUi.updateCurrentMode(mCurrentEntryKey);
                        isFirstOpenCamera = false;
                    }
                    /*prize-modify-repeat switching mode after switch camera-xiaoping-20181017-end*/
                    currentCameraId = mCameraId;
                }
                /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
            }
            LogHelper.d(TAG, "[onBuildInLoadDone]- modes:" + modeItems.size());
        }

        @Override
        public void onPluginLoadDone(String cameraId, CameraApi cameraApi) {
            LogHelper.d(TAG, "[onPluginLoadDone]+ api:" + cameraApi +
                    ", current api:" + mCurrentCameraApi +
                    ",camId:" + cameraId);
            List<IAppUi.ModeItem> modeItems = new ArrayList<>();
            if (cameraApi.equals(mCurrentCameraApi)) {
                modeItems =
                        mCameraContext.getFeatureProvider().getAllModeItems(mCurrentCameraApi);
                if (modeItems.size() > 0) {
                    mAppUi.registerMode(modeItems);
                    mAppUi.updateCurrentMode(mCurrentEntryKey);
                }
            }
            LogHelper.d(TAG, "[onPluginLoadDone]- mode num:" + modeItems.size());
        }
    }

    private void cacheModeByIdleStatus() {
        LogHelper.d(TAG, "[cacheModeByIdleStatus] idle:" + mNewMode.isModeIdle() + ",size:"
                 + mBusyModeList.size());
        if (!mNewMode.isModeIdle()) {
            mBusyModeList.add(mNewMode);
        }
        for (int i = 0; i < mBusyModeList.size(); i++) {
            if (mBusyModeList.get(i).isModeIdle()) {
                LogHelper.d(TAG, "[cacheModeByIdleStatus] mBusyModeList :" + mBusyModeList.get(i));
                mBusyModeList.remove(i);
            }
        }
    }

    /**
     * Mode handler message parameter.
     */
    private class MsgParam {
        /**
         * Construct messge parameter.
         * @param mode current mode
         * @param obj parameter
         */
        public MsgParam(ICameraMode mode, Object obj) {
            mMode = mode;
            mObj = obj;
        }
        public ICameraMode mMode;
        public Object mObj;
    }

    /**
     * Mode handler run in mode thread.
     */
    private class ModeHandler extends Handler {

        /**
         * Construct mode handler.
         * @param looper thread looper
         */
        public ModeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MsgParam param = (MsgParam) msg.obj;
            if (param == null || param.mMode == null) {
                LogHelper.i(TAG, "[handleMessage] null mode!!");
                return;
            }

            switch (msg.what) {
                case MSG_MODE_INIT:
                    Boolean isFromLaunch = (Boolean) param.mObj;
                    param.mMode.init(mApp, mCameraContext, isFromLaunch.booleanValue());
                    break;

                case MSG_MODE_RESUME:
                    param.mMode.resume((DeviceUsage) param.mObj);
                    break;

                case MSG_MODE_PAUSE:
                    param.mMode.pause((DeviceUsage) param.mObj);
                    break;

                case MSG_MODE_UNINIT:
                    param.mMode.unInit();
                    break;

                case MSG_MODE_ON_CAMERA_SELECTED:
                    mSelectedResult = param.mMode.onCameraSelected((String) param.mObj);
                    break;

                case MSG_MODE_ON_ACTIVITY_RESULT:
                    param.mMode.onActivityResult(msg.arg1,msg.arg2, (Intent) param.mObj);
                    break;

                default:
                    break;
            }
        }
    }
	
	/*prize-add-huangpengfei-2018-10-29-start*/
    public ICameraMode getCurrentMode(){
        return mNewMode;
    }

    public void reset(){
        if (mNewMode instanceof PhotoMode){
            SettingManager settingManager = (SettingManager) ((PhotoMode) mNewMode).getSettingManager();
            settingManager.reset();
        }
        if (mNewMode instanceof VideoMode){
            SettingManager settingManager = (SettingManager) ((VideoMode) mNewMode).getSettingManager();
            settingManager.reset();
        }
        boolean isNomal = ("1").equals(android.os.SystemProperties.get("ro.pri_def_front_mode_normal", "0"));
        boolean isSupportFacebeauty = ("1").equals(android.os.SystemProperties.get("ro.pri_camera_fn_facebeauty", "0"));
        if (currentCameraId == 1){
            if (!isNomal && isSupportFacebeauty){
                onModeSelected(FaceBeautyModeEntry.class.getName());
            }else{
                onModeSelected(PhotoModeEntry.class.getName());
            }
        }else{
            onModeSelected(PhotoModeEntry.class.getName());
        }
        mAppUi.setDefaultShutterIndex();

        // zhangguo add 20190429, for bug#75110 picselfie icon state is error start
        StatusMonitor statusMonitor = mCameraContext.getStatusMonitor(String.valueOf(currentCameraId));
        StatusMonitor.StatusResponder responder = statusMonitor.getStatusResponder(KEY_RESTORE_SETTINGS);
        responder.statusChanged(KEY_RESTORE_SETTINGS, null);
        // zhangguo add 20190429, for bug#75110 picselfie icon state is error end
    }

    /*prize-add for model merging-huangpengfei-2019-02-23-start*/
    public boolean checkAndSelectCamera(String modeKey){
        if (FaceBeautyModeEntry.class.getName().equals(modeKey)||
                PicselfieModeEntry.class.getName().equals(modeKey)||
                VideoModeEntry.class.getName().equals(modeKey)||
                PhotoModeEntry.class.getName().equals(modeKey)||
                FilterModeEntry.class.getName().equals(modeKey) ||
                SdofPhotoEntry.class.getName().equals(modeKey) ||
                GifModeEntry.class.getName().equals(modeKey) ||
                IntentVideoModeEntry.class.getName().equals(modeKey)|| //prize-modify-bug camera switch icon hide after switch front camera
                IntentPhotoModeEntry.class.getName().equals(modeKey)){
            mAppUi.setCameraSwitchVisible(View.VISIBLE);
        }else {
            mAppUi.setCameraSwitchVisible(View.GONE);

            if(LowLightModeEntry.class.getName().equals(modeKey) && FeatureSwitcher.isSupportNightCam()){
                //mApp.notifyCameraSelected("3");
                return false;
            }

            if (mCameraId != 0){
                mAppUi.cameraSwitchPerformClick();
                return true;
            }
        }
        return false;
    }
    /*prize-add for model merging-huangpengfei-2019-02-23-end*/
	/*prize-add-huangpengfei-2018-10-29-end*/

    /**
     * add for xiaoping 20181116 get the value in datastore
     * @return
     */
    public ICameraContext getCameraContext() {
        return mCameraContext;
    }

    /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-start*/
    public DeviceUsage getNewDeviceUsage() {
        return mNewDeviceUsage;
    }
    /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-end*/
}
