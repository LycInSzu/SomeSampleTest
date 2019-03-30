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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.view.Surface;
import android.view.View;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to configure flash value to capture request.(Used only for api2).
 * What's more,settings without ui which has restriction with flash will be restricted here.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FlashRequestConfigure implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(FlashRequestConfigure.class.getSimpleName());
    private ScreenFlash mFlash;
    private Boolean mIsFlashSupported = Boolean.FALSE;
    private ISettingManager.SettingDevice2Requester mSettingDevice2Requester;
    private int mFlashMode = CameraMetadata.FLASH_MODE_OFF;

    Integer mAeState = CaptureResult.CONTROL_AE_STATE_INACTIVE;
    private boolean mNeedChangeFlashModeToTorch;
    private CaptureResult.Key<int[]> mFlashResultModeKey;
    private Context mContext;
    private int mCurrentFlashStaus;

    /**
     * Constructor of flash request configuration.
     *
     * @param flash     The flash instance.
     * @param requester setting device request to send value changed request.
     */
    public FlashRequestConfigure(ScreenFlash flash,
                                 ISettingManager.SettingDevice2Requester requester) {
        mFlash = flash;
        mSettingDevice2Requester = requester;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        boolean isFacingFront = characteristics.get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT;

        boolean mIsHardFlashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (isFacingFront&&!mIsHardFlashSupported) {
            mIsFlashSupported = true;
        } else {
            mIsFlashSupported = false;
        }
        initPlatformSupportedValues();
        initAppSupportedEntryValues();
        initSettingEntryValues();
        LogHelper.d(TAG, "[setCameraCharacteristics], isFacingFront = " + isFacingFront +
                ",mIsFlashSupported " + mIsFlashSupported);

        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mFlash.getCameraId()));
        if (deviceDescription != null){
            //mFlashResultModeKey = deviceDescription.getFlashStatusResult();
        }
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        if (mIsFlashSupported) {
            int flashmode = 0;
            if (ScreenFlash.FLASH_ON_VALUE.equalsIgnoreCase(mFlash.getValue())) {
                flashmode = 1;
                captureBuilder.set(CaptureRequest.VENDOR_SCREENFLASH_MODE, flashmode);
            } else if (ScreenFlash.FLASH_AUTO_VALUE.equalsIgnoreCase(mFlash.getValue())) {
                flashmode = 2;
                captureBuilder.set(CaptureRequest.VENDOR_SCREENFLASH_MODE, flashmode);
            } else
                captureBuilder.set(CaptureRequest.VENDOR_SCREENFLASH_MODE, flashmode);
            LogHelper.i(TAG, "[configCaptureRequest], screenflash: flashmode = " + flashmode);
        }
    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return mPreviewCallback;
    }

    @Override
    public void sendSettingChangeRequest() {
        if (mIsFlashSupported) {
            mSettingDevice2Requester.createAndChangeRepeatingRequest();
        }
    }

    private CameraCaptureSession.CaptureCallback mPreviewCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (mIsFlashSupported){
                Integer flashStatus = result.get(TotalCaptureResult.VENDOR_FLASH_STATE);
                //int[] flashStatus = result.get(mFlashResultModeKey);
                if (flashStatus!= null){
                    LogHelper.i(TAG,"flash status = " + flashStatus);
                    String flashVaule = mFlash.getValue();
                    if (ScreenFlash.FLASH_AUTO_VALUE.equals(flashVaule)){
                        if (mCurrentFlashStaus != flashStatus){
                            mCurrentFlashStaus = flashStatus;
                            mFlash.updateCurrenFlashStatus(mCurrentFlashStaus);
                        }
                    }else if (ScreenFlash.FLASH_ON_VALUE.equals(flashVaule)){
                        flashStatus = 1;
                        if (mCurrentFlashStaus != flashStatus){
                            mCurrentFlashStaus = flashStatus;
                            mFlash.updateCurrenFlashStatus(mCurrentFlashStaus);
                        }
                    }else {
                        flashStatus = 0;
                        if (mCurrentFlashStaus != flashStatus){
                            mCurrentFlashStaus = flashStatus;
                            mFlash.updateCurrenFlashStatus(mCurrentFlashStaus);
                        }
                    }
                }
            }
        }
    };

    /**
     * Initialize platform supported values.
     */
    private void initPlatformSupportedValues() {
        List<String> supportedList = new ArrayList<>();
        if (mIsFlashSupported) {
            if(mFlash.getCurrentModeType() != ICameraMode.ModeType.VIDEO){
                supportedList.add(ScreenFlash.FLASH_ON_VALUE);
                supportedList.add(ScreenFlash.FLASH_AUTO_VALUE);
            }
        }
        supportedList.add(ScreenFlash.FLASH_OFF_VALUE);
        mFlash.setSupportedPlatformValues(supportedList);
    }

    /**
     * Initialize application supported entry values. May be spec required supported values.
     */
    private void initAppSupportedEntryValues() {
        List<String> supportedList = new ArrayList<>();
        if (mIsFlashSupported) {
            if(mFlash.getCurrentModeType() != ICameraMode.ModeType.VIDEO){
                supportedList.add(ScreenFlash.FLASH_ON_VALUE);
                supportedList.add(ScreenFlash.FLASH_AUTO_VALUE);
            }
        }
        supportedList.add(ScreenFlash.FLASH_OFF_VALUE);
        mFlash.setSupportedEntryValues(supportedList);
    }

    /**
     * Initialize flash supported setting values,setting with ui will use this values to show.
     * their choices.The values is the intersection of platform supported values and application
     * supported entry values.
     */
    private void initSettingEntryValues() {
        List<String> supportedEntryList = new ArrayList<>();
        List<String> supportedList = new ArrayList<>();
        supportedEntryList.add(ScreenFlash.FLASH_OFF_VALUE);
        supportedEntryList.add(ScreenFlash.FLASH_ON_VALUE);
        supportedEntryList.add(ScreenFlash.FLASH_AUTO_VALUE);
        supportedList.addAll(supportedEntryList);
        supportedList.retainAll(mFlash.getSupportedPlatformValues());
        mFlash.setEntryValues(supportedList);
    }

    /*prize-add-huangpengfei-20190315-huangpengfei-start*/
    public void resetFlashStaus(){
        mCurrentFlashStaus = -100;
    }
    /*prize-add-huangpengfei-20190315-huangpengfei-end*/

}
