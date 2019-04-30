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

package com.mediatek.camera.feature.setting.touchshutter;

import android.content.Intent;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * TouchShutter setting.
 */

public class TouchShutter extends SettingBase implements TouchShutterSettingView.OnTouchShutterClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(TouchShutter.class.getSimpleName());

    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String KEY_TOUCH_SHUTTER = "key_touch_shutter";
    private TouchShutterSettingView mSettingView;
    private boolean mIsTouchShutterSupported = false;
    private ISettingChangeRequester mSettingChangeRequester;
    private boolean mIsThirdParty = false;

    // [Add for CCT tool] Receive keycode and enable/disable TouchShutter @{
    private IApp.KeyEventListener mKeyEventListener;
    // @}

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            mIsThirdParty = true;
        }
    }

    @Override
    public void unInit() {
        // [Add for CCT tool] Receive keycode and enable/disable Mirror @{
        if (mKeyEventListener != null) {
            mApp.unRegisterKeyEventListener(mKeyEventListener);
        }
        // @}
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        if (!mIsTouchShutterSupported) {
            return;
        }
        super.overrideValues(headerKey, currentValue, supportValues);
    }


    @Override
    public void addViewEntry() {
        if (!mIsTouchShutterSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new TouchShutterSettingView(getKey());
            mSettingView.setTouchShutterOnClickListener(this);
            // [Add for CCT tool] Receive keycode and enable/disable Mirror @{
            mKeyEventListener = mSettingView.getKeyEventListener();
            mApp.registerKeyEventListener(mKeyEventListener, IApp.DEFAULT_PRIORITY);
            // @}
        }
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setChecked(VALUE_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_TOUCH_SHUTTER;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        if (mIsThirdParty) {
            return null;
        }
        if (mSettingChangeRequester == null) {
            TouchShutterParametersConfig config = new TouchShutterParametersConfig(this, mSettingDeviceRequester);
            mSettingChangeRequester = config;
        }
        return (TouchShutterParametersConfig) mSettingChangeRequester;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mIsThirdParty) {
            return null;
        }
        if (mSettingChangeRequester == null) {
            TouchShutterCaptureRequestConfig config = new TouchShutterCaptureRequestConfig(
                    this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (TouchShutterCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public void onTouchShutterClicked(boolean checked) {
        String value = checked ? VALUE_ON : VALUE_OFF;
        LogHelper.d(TAG, "[onMirrorClicked], value:" + value);
        setValue(value);
        /*prize-modify-feature Camera front and rear settings remain the same-xiaoping-20190420-start*/
        mDataStore.setValue(getKey(), value, getBackStoreScope(), false);
        mDataStore.setValue(getKey(), value, getFrontStoreScope(), false);
        /*prize-modify-feature Camera front and rear settings remain the same-xiaoping-20190420-end*/
        mHandler.post(new Runnable() {
                @Override
                public void run() {
        mSettingChangeRequester.sendSettingChangeRequest();
    }
        });
    }

    /**
     * Initialize zsd values when platform supported values is ready.
     *
     * @param platformSupportedValues The platform supported values.
     * @param defaultValue The platform default values
     */
    public void initializeValue(List<String> platformSupportedValues,
                                String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            mIsTouchShutterSupported = true;
            mIsTouchShutterSupported = true;
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            setValue(value);
        }
    }
}
