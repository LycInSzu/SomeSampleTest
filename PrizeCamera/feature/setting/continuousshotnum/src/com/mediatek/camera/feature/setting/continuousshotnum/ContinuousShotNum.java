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
package com.mediatek.camera.feature.setting.continuousshotnum;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.prize.PrizeDataRevert;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for self timer feature interacted with others.
 */

public class ContinuousShotNum extends SettingBase implements IContinuousShotNumViewListener.OnValueChangeListener,PrizeDataRevert {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ContinuousShotNum.class.getSimpleName());
    private static final String CONTINUOUS_SHOT_NUM_KEY = "continuous_shut_num_key";
    private ContinuousShotNumView ContinuousShotNumView;
    private List<String> mSupportValues = new ArrayList<String>();
    private String mDefaultValue = IContinuousShotNumViewListener.TWENTY_SHOTS;
    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        initSettingValue();
    }

    @Override
    public void unInit() {
    }

    @Override
    public void addViewEntry() {
        if(ContinuousShotNumView == null){
            ContinuousShotNumView = new ContinuousShotNumView(getKey(),mActivity);
        }
        ContinuousShotNumView.setOnValueChangeListener(this);
        mAppUi.addSettingView(ContinuousShotNumView);
        LogHelper.d(TAG, "[addViewEntry] getValue() :" + getValue());
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(ContinuousShotNumView);
        LogHelper.d(TAG, "[removeViewEntry]");
    }

    @Override
    public void refreshViewEntry() {
        int size = getEntryValues().size();
        if (ContinuousShotNumView != null) {
            ContinuousShotNumView.setEntryValues(getEntryValues());
            ContinuousShotNumView.setValue(getValue());
            ContinuousShotNumView.setEnabled(size > 1);
        }

    }



    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return IContinuousShotNumViewListener.KEY_CONTINUOUS_SHOT_NUM;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void postRestrictionAfterInitialized() {}

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }


    @Override
    public void onModeClosed(String modeKey) {
        LogHelper.d(TAG, "onModeClosed modeKey :" + modeKey);
        super.onModeClosed(modeKey);
    }

    private void initSettingValue() {
        mSupportValues.add(IContinuousShotNumViewListener.ONE_SHOTS);
        mSupportValues.add(IContinuousShotNumViewListener.FIVE_SHOTS);
        mSupportValues.add(IContinuousShotNumViewListener.TWENTY_SHOTS);
        mSupportValues.add(IContinuousShotNumViewListener.FORTY_SHOTS);
        mSupportValues.add(IContinuousShotNumViewListener.NINETY_SHOTS);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        if (FeatureSwitcher.getDefaultShouNum() != null && !FeatureSwitcher.getDefaultShouNum().isEmpty()) {
            mDefaultValue = FeatureSwitcher.getDefaultShouNum();
        }
        String valueInStore = mDataStore.getValue(getKey(),
                mDefaultValue, getStoreScope());
        setValue(valueInStore);
    }

    @Override
    public void clearCache() {
        mDataStore.clearCache();
        onValueChanged(mDefaultValue);
    }

    @Override
    public void onValueChanged(String value) {
        setValue(value);
        mDataStore.setValue(getKey(), value, getStoreScope(), false);
    }
}
