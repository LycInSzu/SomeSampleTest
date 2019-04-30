/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.screenflash;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

/**
 * Class used to handle flash feature flow.
 */
public class ScreenFlash extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ScreenFlash.class.getSimpleName());

    public static final String FLASH_AUTO_VALUE = "auto";
    public static final String FLASH_OFF_VALUE = "off";
    public static final String FLASH_ON_VALUE = "screen";
    private static final String FLASH_DEFAULT_VALUE = "auto";
    public static int SCREEN_FLASH_PRIORITY = 35;
    public static final String SCREEN_FLASH_KEY = "key_screen_flash";
    private static final String KEY_CSHOT = "key_continuous_shot";
    private static final String VALUE_CSHOT_START = "start";
    private static final String VALUE_CSHOT_STOP = "stop";
    private boolean isRecording = false;
    private ICameraMode.ModeType mModeType;
    private FlashParameterConfigure mFlashParameterConfigure;
    private ICaptureRequestConfigure mFlashRequestConfigure;
    private FlashViewController mFlashViewController;
    private ISettingChangeRequester mSettingChangeRequester;

    private static final String VIDEO_STATUS_KEY = "key_video_status";
    private static final String VIDEO_STATUS_RECORDING = "recording";
    private static final String VIDEO_STATUS_PREVIEW = "preview";
    // [Add for CCT tool] Receive keycode and set flash on/set flash off @{
    private IApp.KeyEventListener mKeyEventListener;
    private IApp mIapp;
    private String mModeKey;
    private static final int MSG_UPDATE_FLASH_STATUS= 101;
    private int mCurrentStatus = 0; private
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_FLASH_STATUS){
                int status = (int) msg.obj;
                if (status == mCurrentStatus){
                    if (mCurrentStatus == 1&&!isRecording){
                        mFlashViewController.updateFlashIndicator(FLASH_ON_VALUE);
                        mStatusResponder.statusChanged(SCREEN_FLASH_KEY, FLASH_ON_VALUE);
                    }else {
                        mFlashViewController.updateFlashIndicator(FLASH_OFF_VALUE);
                        mStatusResponder.statusChanged(SCREEN_FLASH_KEY, FLASH_OFF_VALUE);
                    }
                }
            }
        }
    };

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        String value = mDataStore.getValue(SCREEN_FLASH_KEY, FLASH_DEFAULT_VALUE, getStoreScope());
        setValue(value);
        mIapp = app;
        if (mFlashViewController == null) {
            mFlashViewController = new FlashViewController(this, app);
        }
        mStatusMonitor.registerValueChangedListener(VIDEO_STATUS_KEY, mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_CSHOT, mStatusChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable flash @{
        mKeyEventListener = mFlashViewController.getKeyEventListener();
        mApp.registerKeyEventListener(mKeyEventListener, IApp.DEFAULT_PRIORITY);
        // @}
        //mAppUi.registerGestureListener(mOnGestureListener, SCREEN_FLASH_PRIORITY);//prize-remove-huangpengfei-20190423
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(VIDEO_STATUS_KEY,
                mStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_CSHOT, mStatusChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable flash @{
        if (mKeyEventListener != null) {
            mApp.unRegisterKeyEventListener(mKeyEventListener);
        }
        // @}
        mHandler.removeMessages(MSG_UPDATE_FLASH_STATUS);
        mFlashViewController.updateFlashIndicator(FLASH_OFF_VALUE);
        //mAppUi.unregisterGestureListener(mOnGestureListener);//prize-remove-huangpengfei-20190423

    }

    @Override
    public void addViewEntry() {
        mFlashViewController.addQuickSwitchIcon();
        mFlashViewController.showQuickSwitchIcon(getEntryValues().size() > 1);
    }

    @Override
    public void removeViewEntry() {
        mFlashViewController.removeQuickSwitchIcon();
    }

    @Override
    public void refreshViewEntry() {
        int num = getEntryValues().size();
        if (num <= 1) {
            mFlashViewController.showQuickSwitchIcon(false);
        } else {
            mFlashViewController.showQuickSwitchIcon(true);
        }
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        mModeType = modeType;
        mModeKey = modeKey;
    }

    @Override
    public void onModeClosed(String modeKey) {
        mFlashViewController.hideFlashChoiceView();
        /*prize-add fixbug:[73980]-huangpengfei-20190409-start*/
        mHandler.removeMessages(MSG_UPDATE_FLASH_STATUS);
        /*prize-add fixbug:[73980]-huangpengfei-20190409-end*/
        super.onModeClosed(modeKey);
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return SCREEN_FLASH_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        if (mFlashParameterConfigure == null) {
            mFlashParameterConfigure = new FlashParameterConfigure(this, mSettingDeviceRequester);
        }
        mSettingChangeRequester = mFlashParameterConfigure;
        return mFlashParameterConfigure;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mFlashRequestConfigure == null) {
            mFlashRequestConfigure = new FlashRequestConfigure(this, mSettingDevice2Requester);
        }
        mSettingChangeRequester = mFlashRequestConfigure;
        return mFlashRequestConfigure;
    }

    /*prize-modify-huangpengfei-20190314-start*/
    /*@Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        LogHelper.d(TAG, "[overrideValues] headerKey = " + headerKey
                + " ,currentValue = " + currentValue + ",supportValues = " + supportValues);
        if (headerKey.equals("key_scene_mode") && mSettingController.queryValue("key_scene_mode")
                .equals("hdr")) {
            return;
        }
        String lastValue = getValue();
        if ((headerKey.equals("key_hdr")) && currentValue != null && (currentValue != lastValue)) {
            onFlashValueChanged(currentValue);
        }
        if (!headerKey.equals("key_hdr")) {
            super.overrideValues(headerKey, currentValue, supportValues);
            if (!lastValue.equals(getValue())) {
                Relation relation = FlashRestriction.getFlashRestriction()
                        .getRelation(getValue(), true);
                mSettingController.postRestriction(relation);
            }
        }
    }*/

    @Override
    public void postRestrictionAfterInitialized() {
        /*Relation relation = FlashRestriction.getFlashRestriction().getRelation(getValue(), false);
        if (relation != null) {
            mSettingController.postRestriction(relation);
        }*/
    }

    public void resetFlashStatus(){
        if (mFlashRequestConfigure != null){
            FlashRequestConfigure flashRequestConfigure = (FlashRequestConfigure)mFlashRequestConfigure;
            flashRequestConfigure.resetFlashStaus();
        }
    }
    /*prize-modify-huangpengfei-20190314-end*/

    /**
     * Get current mode type.
     *
     * @return mModeType current mode type.
     */
    public ICameraMode.ModeType getCurrentModeType() {
        return mModeType;
    }

    /**
     * Called when flash value changed.
     *
     * @param value The new value.
     */
    public void onFlashValueChanged(String value) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!value.equals(ScreenFlash.super.getValue())) {
                    LogHelper.d(TAG, "[onFlashValueChanged] value = " + value);
                    setValue(value);
                    mSettingController.postRestriction(
                            FlashRestriction.getFlashRestriction().getRelation(value, true));
                    mSettingController.refreshViewEntry();
                    mSettingChangeRequester.sendSettingChangeRequest();
                    mDataStore.setValue(SCREEN_FLASH_KEY, value, getStoreScope(), false, true);
                }
            }
        });
    }

    // zhangguo add start
    @Override
    public synchronized String getValue() {
        String curValue = super.getValue();

        if(mModeType == ICameraMode.ModeType.VIDEO && FLASH_ON_VALUE.equals(curValue)){
            return FLASH_OFF_VALUE;
        }

        return curValue;
    }

    public synchronized String getLocalValue(){
        String curValue = super.getValue();

        if(mModeType == ICameraMode.ModeType.VIDEO && FLASH_ON_VALUE.equals(curValue)){
            return FLASH_OFF_VALUE;
        }

        return curValue;
    }
    // zhangguo add end

    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor
            .StatusChangeListener() {

        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] + key " + key + "," +
                    "value " + value);
            switch (key) {
                case VIDEO_STATUS_KEY:
                    if (mFlashViewController == null) {
                        return;
                    }
                    if (VIDEO_STATUS_RECORDING.equals(value)) {
                        isRecording = true;
                        mFlashViewController.updateFlashIndicator(FLASH_OFF_VALUE);
                    } else if (VIDEO_STATUS_PREVIEW.equals(value)) {
                        isRecording = false;
                        mFlashViewController.updateFlashIndicator(getValue());
                    }

                    break;
                case KEY_CSHOT:
                    if (mFlashRequestConfigure == null) {
                        return;
                    }
                    if (VALUE_CSHOT_START.equals(value)) {
                        //mFlashViewController.updateFlashEntryIconWhenCS(true);
                    } else if (VALUE_CSHOT_STOP.equals(value)) {;
                        //mFlashViewController.updateFlashEntryIconWhenCS(false);
                    }

                    break;
                default:
                    break;
            }
            LogHelper.d(TAG, "[onStatusChanged] -");
        }
    };

    private IAppUiListener.OnGestureListener mOnGestureListener = new IAppUiListener.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            mFlashViewController.hideFlashChoiceView();
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onLongPress(float x, float y) {
            mFlashViewController.hideFlashChoiceView();
            return false;
        }
    };
	
    public String getModeKey(){
        return mModeKey;
    }
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    public void updateCurrenFlashStatus(int status){
        if (isRecording){
            return;
        }
        int num = getEntryValues().size();
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_UPDATE_FLASH_STATUS;
        if (num <= 1) {
            if (mHandler.hasMessages(MSG_UPDATE_FLASH_STATUS)){
                mHandler.removeMessages(MSG_UPDATE_FLASH_STATUS);
                msg.obj = 0;
                mCurrentStatus = status;
                mHandler.sendMessage(msg);
            }
            return;
        }
        LogHelper.d(TAG,"updateCurrenFlashStatus flash status = " + status);
        msg.obj = status;
        mCurrentStatus = status;
        mHandler.sendMessageDelayed(msg,800);
    }
}
