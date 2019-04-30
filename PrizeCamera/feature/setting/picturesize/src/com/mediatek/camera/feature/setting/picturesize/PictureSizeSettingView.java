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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;
/*prize-add-huangpengfei-2018-10-25-start*/
import com.mediatek.camera.common.widget.PrizeSettingDialog;
import com.mediatek.camera.prize.PrizeLifeCycle;
/*prize-add-huangpengfei-2018-10-25-end*/

import java.util.ArrayList;
import java.util.List;

/**
 * Picture size setting view.
 */
public class PictureSizeSettingView implements ICameraSettingView,
        PictureSizeSelector.OnItemClickListener,PrizeLifeCycle {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(PictureSizeSettingView.class.getSimpleName());

    private Activity mActivity;
    private Preference mPref;
    private OnValueChangeListener mListener;
    private String mKey;
    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private String mSummary;
    private PictureSizeSelector mSizeSelector;
    private boolean mEnabled;
    /*prize-add-huangpengfei-2018-10-25-start*/
    private PrizeSettingDialog mPrizeSettingDialog;
    private String[] mDialogData;
    private static final double DEGRESSIVE_RATIO = 0.5;
    private static final int MAX_COUNT = 2;
    private List<String> mShowValues;
    /*prize-add-huangpengfei-2018-10-25-end*/
    private String mPictureZoomSize;
    private String mCameraId;
    private boolean isHasPictureZoomSize;
    /**
     * Listener to listen picture size value changed.
     */
    public interface OnValueChangeListener {
        /**
         * Callback when picture size value changed.
         *
         * @param value The changed picture size, such as "1920x1080".
         */
        void onValueChanged(String value);
    }

    /**
     * Picture size setting view constructor.
     *
     * @param key The key of picture size.
     */
    public PictureSizeSettingView(String key) {
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG, "[loadView]");
        mActivity = fragment.getActivity();

        /*prize-remove-huangpengfei-2018-10-25-start*/
        /*if (mSizeSelector == null) {
            mSizeSelector = new PictureSizeSelector();
            mSizeSelector.setOnItemClickListener(this);
        }*/
        /*prize-remove-huangpengfei-2018-10-25-end*/

        fragment.addPreferencesFromResource(R.xml.picturesize_preference);
        mPref = (Preference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.picture_size_setting);
        mPref.setContentDescription(mActivity.getResources()
                .getString(R.string.picture_size_content_description));
        mPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                /*prize-change-huangpengfei-2018-10-25-start*/
                LogHelper.d(TAG, "[onPreferenceClick]");
                    mPrizeSettingDialog = new PrizeSettingDialog(mActivity, mPref.getTitle());
                    mPrizeSettingDialog.init();
                    mPrizeSettingDialog.show();
                    mShowValues = filterValuesOnShown(mEntryValues);
                    mDialogData = formatDialogData(mShowValues);
                    mPrizeSettingDialog.setSelectValue(mSummary);
                    mPrizeSettingDialog.initData(mDialogData);
                    mPrizeSettingDialog.setOnItemClickListener(new PrizeSettingDialog.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String summary = mDialogData[position];
                            String value = mShowValues.get(position);
                            mSummary = summary;
                            mSelectedValue = value;
                            //mSummary = PictureSizeHelper.getPixelsAndRatio(value);
                            if (mListener != null) {
                                mListener.onValueChanged(value);
                            }
                            refreshView();
                        }
                    });
                /*mSizeSelector.setValue(mSelectedValue);
                mSizeSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mActivity.getFragmentManager()
                        .beginTransaction();
				//prize-tangan-add prize camera-begin
                transaction.setCustomAnimations(R.anim.setting_fragment_in,R.anim.setting_fragment_out,R.anim.setting_fragment_pop_in,R.anim.setting_fragment_pop_out);
                //prize-tangan-add prize camera-end
				transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mSizeSelector, "picture_size_selector").commit();*/
                return true;
            }

        });
        mPref.setEnabled(mEnabled);
        if (mSelectedValue != null) {
            //mSummary = PictureSizeHelper.getPixelsAndRatio(mSelectedValue);
            String s = PictureSizeHelper.getPixelsAndRatio(mSelectedValue);
            mSummary = s + "  " + mSelectedValue;
        }
		/*prize-change-huangpengfei-2018-10-25-end*/
    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            LogHelper.d(TAG, "[refreshView]");
            mPref.setSummary(mSummary);
            mPref.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {
        LogHelper.d(TAG, "[unloadView]");
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Set listener to listen the changed picture size value.
     *
     * @param listener The instance of {@link OnValueChangeListener}.
     */
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    /**
     * Set the default selected value.
     *
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the picture sizes supported.
     *
     * @param entryValues The picture sizes supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
        /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-start*/
        if ("0".equals(mCameraId)  && isHasPictureZoomSize ) {
            mEntryValues.remove(mPictureZoomSize);
        }
        /*prize-modify-set picturesize of picturezoom -xiaoping-20181017-end*/
    }

    @Override
    public void onItemClick(String value) {
        mSelectedValue = value;
        mSummary = PictureSizeHelper.getPixelsAndRatio(value);
        if (mListener != null) {
            mListener.onValueChanged(value);
        }
    }
    /*prize-add-huangpengfei-2018-10-25-start*/
    private String[] formatDialogData(List<String> strList){
        if (strList == null){
            return null;
        }
        String[] strings = new String[strList.size()];
        String temp;
        for (int i = 0;i < strList.size(); i++){
            temp = strList.get(i);
            String s = PictureSizeHelper.getPixelsAndRatio(temp);
            strings[i] = s + "  " + temp;
        }
        return strings;
    }

    private List<String> filterValuesOnShown(List<String> entryValue) {
        PictureSizeHelper.setFilterParameters(DEGRESSIVE_RATIO, MAX_COUNT);
        List<String> tempValues;
        tempValues = PictureSizeHelper.filterSizes(entryValue);
       return  tempValues;
    }

    @Override
    public void onPause() {
        if (mPrizeSettingDialog != null){
            mPrizeSettingDialog.dismiss();
        }
    }
    /*prize-add-huangpengfei-2018-10-25-end*/
    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-start*/
    public void setPictureZoomSize(String pictureZoomSize) {
        mPictureZoomSize = pictureZoomSize;
    }

    public void setCameraId(String cameraId) {
        mCameraId = cameraId;
    }

    public void setHasPictureZoomSize(boolean hasPictureZoomSize) {
        isHasPictureZoomSize = hasPictureZoomSize;
    }

    /*prize-modify-add get maxpicturesize for picturezoom-xiaoping-20181207-end*/
}
