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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for self timer feature setting view.
 */

public class ContinuousShotNumView implements ICameraSettingView {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(ContinuousShotNumView.class.getSimpleName());

    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private IContinuousShotNumViewListener.OnValueChangeListener mOnValueChangeListener;
    private Preference mContinuousShotNumPreference;
    private ContinuousShotNumSelector mContinuousShotNumSelector;
    private Activity mContext;
    private boolean mEnabled;

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.continuous_shot_num_preference);
        mContext = fragment.getActivity();

        if (mContinuousShotNumSelector == null) {
            mContinuousShotNumSelector = new ContinuousShotNumSelector();
            mContinuousShotNumSelector.setOnItemClickListener(mOnItemClickListener);
        }

        mContinuousShotNumPreference = (Preference) fragment
                .findPreference(IContinuousShotNumViewListener.KEY_CONTINUOUS_SHOT_NUM);
        mContinuousShotNumPreference.setRootPreference(fragment.getPreferenceScreen());
        mContinuousShotNumPreference.setId(R.id.continuous_shot_num_setting);
        mContinuousShotNumPreference.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.continuous_shot_num_content_description));
        mContinuousShotNumPreference.setSummary(getSummary());
        mContinuousShotNumPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                mContinuousShotNumSelector.setValue(mSelectedValue);
                mContinuousShotNumSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mContext.getFragmentManager()
                        .beginTransaction();
				//prize-tangan-add prize camera-begin
                transaction.setCustomAnimations(R.anim.setting_fragment_in,R.anim.setting_fragment_out,R.anim.setting_fragment_pop_in,R.anim.setting_fragment_pop_out);
                //prize-tangan-add prize camera-end
				transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mContinuousShotNumSelector, "continuous_shot_num_selector").commit();
                return true;
            }
        });
        mContinuousShotNumPreference.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mContinuousShotNumPreference != null) {
            LogHelper.d(TAG, "[refreshView]");
            mContinuousShotNumPreference.setSummary(getSummary());
            mContinuousShotNumPreference.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {

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
     * Set listener to listen the changed self timer value.
     * @param listener The instance of {@link IContinuousShotNumViewListener.OnValueChangeListener}.
     */
    public void setOnValueChangeListener(IContinuousShotNumViewListener.OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the self timer supported.
     * @param entryValues The self timer supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    private IContinuousShotNumViewListener.OnItemClickListener mOnItemClickListener
            = new IContinuousShotNumViewListener.OnItemClickListener() {
        @Override
        public void onItemClick(String value) {
            mSelectedValue = value;
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChanged(value);
            }
        }
    };

    private String getSummary() {
        if (IContinuousShotNumViewListener.FIVE_SHOTS.equals(mSelectedValue)) {
            return mContext.getString(R.string.continuous_shot_num_entry_5);
        } else if (IContinuousShotNumViewListener.TWENTY_SHOTS.equals(mSelectedValue)) {
            return mContext.getString(R.string.continuous_shot_num_entry_20);
        }else {
            return mContext.getString(R.string.continuous_shot_num_entry_40);
        }
    }
}
