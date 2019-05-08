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
package com.mediatek.camera.feature.setting.picturesize;

import android.util.Size;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.beauty.FaceBeautyMode;
import com.mediatek.camera.common.mode.picselfie.PicselfieMode;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomMode;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.prize.camera.feature.mode.gif.GifMode;
import com.prize.camera.feature.mode.filter.FilterMode;

import java.util.ArrayList;
import java.util.List;
import android.os.SystemProperties;

import javax.annotation.Nonnull;

/**
 * Picture size setting item.
 *
 */
public class PictureSize extends SettingBase implements
        PictureSizeSettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PictureSize.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";

    private ISettingChangeRequester mSettingChangeRequester;
    private PictureSizeSettingView mSettingView;
    /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-start*/
    private String mLastPictureSize;
    private String mCurrentMode;
    /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-end*/
	//prize-custom default ratio and size-tangan-20181101-begin
    private final static String DEFAULT_BACK_RATION = android.os.SystemProperties.get("ro.camera.backdefault_rotation", String.valueOf(PictureSizeHelper.RATIO_16_9));
    private final static String DEFAULT_FRONT_RATION = android.os.SystemProperties.get("ro.camera.predefault_rotation", String.valueOf(PictureSizeHelper.RATIO_4_3));
    private final static String DEFAULT_FRONT_PICTURESIZE = android.os.SystemProperties.get("ro.camera.predefault_size", "3264x2448");
    private final static String DEFAULT_BACK_PICTURESIZE = android.os.SystemProperties.get("ro.camera.backdefault_size", "3264x2448");
	//prize-custom default ratio and size-tangan-20181101-end

    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-start*/
    private String mPictureZoomSize = android.os.SystemProperties.get("ro.pri.picturezoom.pictureize", null);
    private List<String> mSizes;
    //private final static String DEFAULT_PICTUREZOOM_PICTURESIZE = android.os.SystemProperties.get("ro.pri.picturezoom.pictureize", null);
    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-end*/

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
    }

    @Override
    public void unInit() {

    }

    @Override
    public void addViewEntry() {
        if (mSettingView == null) {
            mSettingView = new PictureSizeSettingView(getKey());
            mSettingView.setOnValueChangeListener(this);
            /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-start*/
            mSettingView.setCameraId(mSettingController.getCameraId());
            mSettingView.setPictureZoomSize(mPictureZoomSize);
            mSettingView.setHasPictureZoomSize(isHasPictureZoomSize());
            /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-end*/
            mSettingView.setContext(mActivity);
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
            mSettingView.setValue(getValue());
            mSettingView.setEntryValues(getEntryValues());
            /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-start*/
            if (PictureZoomMode.class.getName().equals(mCurrentMode) || GifMode.class.getName().equals(mCurrentMode)) {
                mSettingView.setEnabled(false);
            } else {
                mSettingView.setEnabled(getEntryValues().size() > 1);
            }
            /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-end*/
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
        return KEY_PICTURE_SIZE;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        if (mSettingChangeRequester == null) {
            PictureSizeParametersConfig parametersConfig
                    = new PictureSizeParametersConfig(this, mSettingDeviceRequester);
            mSettingChangeRequester = parametersConfig;
        }
        return (PictureSizeParametersConfig) mSettingChangeRequester;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            PictureSizeCaptureRequestConfig captureRequestConfig
                    = new PictureSizeCaptureRequestConfig(this, mSettingDevice2Requester);
            mSettingChangeRequester = captureRequestConfig;
        }
        return (PictureSizeCaptureRequestConfig) mSettingChangeRequester;
    }

    /**
     * Invoked after setting's all values are initialized.
     *
     * @param supportedPictureSize Picture sizes which is supported in current platform.
     */
    public void onValueInitialized(List<String> supportedPictureSize) {
        LogHelper.d(TAG, "[onValueInitialized], supportedPictureSize:" + supportedPictureSize);
        mSizes = supportedPictureSize;
        /*prize-modify- set default photo size for picturezoom-xiaoping-20190228-start*/
        if (mPictureZoomSize == null || mPictureZoomSize.isEmpty()) {
            if (SystemProperties.getInt("ro.pri.current.project",0) == 1) {
                mPictureZoomSize = "8320x6240";
            } else if (SystemProperties.getInt("ro.pri.current.project",0) == 3 || SystemProperties.getInt("ro.pri.current.project",0) == 4) {/*prize-modify-72249-Photo green screen on picturezoom mode -xiaoping-20190314-start*/
                mPictureZoomSize = "9216x6912";
            }
        }
        /*prize-modify- set default photo size for picturezoom-xiaoping-20190228-end*/
        getPictureZoomSize(supportedPictureSize);
        setSupportedPlatformValues(supportedPictureSize);
        setSupportedEntryValues(supportedPictureSize);
        setEntryValues(supportedPictureSize);

        double fullRatio = PictureSizeHelper.findFullScreenRatio(mActivity);
        List<Double> desiredAspectRatios = new ArrayList<>();
        desiredAspectRatios.add(fullRatio);

        /*prize-add for 18:9-huangpengfei-2019-01-18-start*/
        if (("1").equals(android.os.SystemProperties.get("ro.pri.scale.eighteen_to_nine", "0"))) {
            desiredAspectRatios.add(PictureSizeHelper.RATIO_18_9);
        }
        /*prize-add for 18:9-huangpengfei-2019-01-18-end*/

        desiredAspectRatios.add(PictureSizeHelper.RATIO_16_9);
        desiredAspectRatios.add(PictureSizeHelper.RATIO_4_3);
        /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-start*/
        if (FeatureSwitcher.getCurrentProjectValue() == 5 && (FaceBeautyMode.class.getName().equals(mCurrentMode) || FilterMode.class.getName().equals(mCurrentMode) || PicselfieMode.class.getName().equals(mCurrentMode))) {
            desiredAspectRatios.clear();
            desiredAspectRatios.add(PictureSizeHelper.RATIO_4_3);
        }
        /*prize-modify-Limit the preview size and picturesize of the algorithm mode-xiaoping-20190430-end*/
        PictureSizeHelper.setDesiredAspectRatios(desiredAspectRatios);

        String valueInStore = mDataStore.getValue(getKey(), null, getStoreScope());
        /*prize-modify-72249-Photo green screen on picturezoom mode -xiaoping-20190314-start*/
        if (!(mAppUi != null && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) && valueInStore != null
                && !supportedPictureSize.contains(valueInStore)) {
            /*prize-modify-72249-Photo green screen on picturezoom mode -xiaoping-20190314-end*/
            LogHelper.d(TAG, "[onValueInitialized], value:" + valueInStore
                    + " isn't supported in current platform");
            valueInStore = null;
            mDataStore.setValue(getKey(), null, getStoreScope(), false);
        }
        if (valueInStore == null) {
            // Default picture size is the max full-ratio size.
            List<String> entryValues = getEntryValues();
			//prize-custom default ratio-tangan-20181101-begin
            if (FeatureSwitcher.isArcsoftSupperZoomSupported() && !isFrontCamera()) {
                entryValues.remove(mPictureZoomSize);
            }
            double defaultRatio = PictureSizeHelper.RATIO_16_9;
            if("0".equals(getCameraId())){
                double defaultBackRatio = fullRatio;
                try{
                    defaultBackRatio = Double.parseDouble(DEFAULT_BACK_RATION);
                }catch (Exception e){
                    LogHelper.e(TAG, "DEFAULT_BACK_RATION=" + DEFAULT_BACK_RATION
                            + " ,parse exception");
                }
                for(int i=0;i<desiredAspectRatios.size();i++){
                    if(Math.abs((desiredAspectRatios.get(i) - defaultBackRatio)) < PictureSizeHelper.ASPECT_TOLERANCE){
                        defaultRatio = desiredAspectRatios.get(i);
                    }
                }
                /*prize-modify-use the macro switch to configure the default photo size-xiaoping-20190325-start*/
                for (String value : entryValues) {
                    if (DEFAULT_BACK_PICTURESIZE.equals(value) && PictureSizeHelper.getStandardAspectRatio(value) == defaultRatio) {
                        valueInStore = value;
                        break;
                    }
                }
                /*prize-modify-use the macro switch to configure the default photo size-xiaoping-20190325-end*/
            }else{
                double defaultFrontRatio = fullRatio;
                try{
                    defaultFrontRatio = Double.parseDouble(DEFAULT_FRONT_RATION);
                }catch (Exception e){
                    LogHelper.e(TAG, "DEFAULT_FRONT_RATION=" + DEFAULT_FRONT_RATION
                            + " ,parse exception");
                }
                for(int i=0;i<desiredAspectRatios.size();i++){
                    if(Math.abs(desiredAspectRatios.get(i) - defaultFrontRatio) < PictureSizeHelper.ASPECT_TOLERANCE ){
                        defaultRatio = desiredAspectRatios.get(i);
                    }
                }
                /*prize-modify-use the macro switch to configure the default photo size-xiaoping-20190325-start*/
                for (String value : entryValues) {
                    if (DEFAULT_FRONT_PICTURESIZE.equals(value) && PictureSizeHelper.getStandardAspectRatio(value) == defaultRatio) {
                        valueInStore = value;
                        break;
                    }
                }
            }
            LogHelper.e(TAG, "cameraid: "+getCameraId()+",defaultRatio=" + defaultRatio +",defaultsize: "+valueInStore);
            /*prize-modify-use the macro switch to configure the default photo size-xiaoping-20190325-end*/
            if (valueInStore == null) {
                for (String value : entryValues) {
                    if (PictureSizeHelper.getStandardAspectRatio(value) == defaultRatio) {
                        valueInStore = value;
                        break;
                    }
                }
            }
        }
		//prize-custom default ratio-tangan-20181101-end
        // If there is no full screen ratio picture size, use the first value in
        // entry values as the default value.
        if (valueInStore == null) {
            valueInStore = getEntryValues().get(0);
        }

        setValue(valueInStore);
        mDataStore.setValue(getKey(), valueInStore, getStoreScope(), false);//prize-custom default ratio-tangan-
    }

    @Override
    public void onValueChanged(String value) {
        LogHelper.i(TAG, "[onValueChanged], value:" + value);
        if (value == null) {
            return;
        }
        /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-start*/
        if (PictureZoomMode.class.getName().equals(mCurrentMode)) {
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        } else if (getValue() != null && !getValue().equals(value)){
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        mCurrentMode = modeKey;
        if (FeatureSwitcher.isArcsoftSupperZoomSupported() && PictureZoomMode.class.getName().equals(modeKey) && "0".equals(getCameraId())) {
            mLastPictureSize = mDataStore.getValue(getKey(),"3264x2448",getStoreScope());
            LogHelper.i(TAG,""+modeKey+",mLastPictureSize: "+mLastPictureSize);
            if (mPictureZoomSize != null) {
                onValueChanged(mPictureZoomSize);
            } else {
                onValueChanged(mLastPictureSize);
            }
        }
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        LogHelper.i(TAG,""+modeKey);
        super.onModeClosed(modeKey);
        if (PictureZoomMode.class.getName().equals(modeKey) && mLastPictureSize != null && "0".equals(getCameraId())) {
            onValueChanged(mLastPictureSize);
        }
    }
	//prize-custom default ratio-tangan-20181101-begin
    private String getCameraId(){
        return mSettingController.getCameraId();
    }
	//prize-custom default ratio-tangan-20181101-end



    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-start*/
    public void getPictureZoomSize(List<String> sizes) {
        if (sizes != null  && !isFrontCamera() && isHasPictureZoomSize()) {
            mPictureZoomSize = sizes.get(0);
            if (mSettingView != null) {
                mSettingView.setCameraId(mSettingController.getCameraId());
                mSettingView.setPictureZoomSize(mPictureZoomSize);
                mSettingView.setHasPictureZoomSize(isHasPictureZoomSize());
            }
        } else {
            LogHelper.e(TAG,"getPictureZoomSize is null");
        }
    }

    private boolean isHasPictureZoomSize() {
        if (mSizes != null) {
            return stringToSize(mSizes.get(0)).getWidth() * stringToSize(mSizes.get(0)).getHeight() >= stringToSize(mSizes.get(1)).getHeight() * stringToSize(mSizes.get(1)).getHeight() * 4;
        }
        return false;
    }

    private Size stringToSize(String stringSize) {
        if (stringSize != null) {
            String[] strings = stringSize.split("x");
            Size size = new Size(Integer.valueOf(strings[0]),Integer.valueOf(strings[1]));
            return size;
        }
        return null;
    }
    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-end*/


    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-start*/
    public IAppUi getAppUi() {
        return mAppUi;
    }
    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-end*/
}
