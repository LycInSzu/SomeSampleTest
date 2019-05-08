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
package com.mediatek.camera.feature.setting.antiflicker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;
/*prize-add-huangpengfei-2018-10-25-start*/
import com.mediatek.camera.common.widget.PrizeSettingDialog;
import com.mediatek.camera.prize.PrizeLifeCycle;
import com.mediatek.camera.ui.prize.PrizeCameraSettingView;
/*prize-add-huangpengfei-2018-10-25-end*/

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for anti flicker feature setting view.
 */

public class AntiFlickerSettingView extends PrizeCameraSettingView implements ICameraSettingView,
        AntiFlickerSelector.OnItemClickListener,PrizeLifeCycle {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(AntiFlickerSettingView.class.getSimpleName());

    private List<String> mOriginalEntries = new ArrayList<>();
    private List<String> mOriginalEntryValues = new ArrayList<>();

    private List<String> mEntries = new ArrayList<>();
    private List<String> mEntryValues = new ArrayList<>();
    private OnValueChangeListener mOnValueChangeListener;
    private Preference mPreference;
    private AntiFlickerSelector mAntiFlickerSelector;
    private String mKey;
    private Activity mActivity;
    private String mSummary = null;
    private String mSelectedValue;
    private boolean mEnabled;
    private PrizeSettingDialog mPrizeSettingDialog;//prize-add-huangpengfei-2018-10-25

    /**
     * Listener to listen anti flicker value changed.
     */
    public interface OnValueChangeListener {
        /**
         * Callback when anti flicker value changed.
         *
         * @param value The changed anti flicker.
         */
        void onValueChanged(String value);
    }

    /**
     * Anti flicker setting view.
     *
     * @param activity The camera activity.
     * @param key The key of anti flicker.
     */
    public AntiFlickerSettingView(Activity activity, String key) {
        mActivity = activity;
        mKey = key;
        String[] originalEntriesInArray = mActivity.getResources()
                .getStringArray(R.array.anti_flicker_entries);
        String[] originalEntryValuesInArray = mActivity.getResources()
                .getStringArray(R.array.anti_flicker_entryvalues);

        for (String value : originalEntriesInArray) {
            mOriginalEntries.add(value);
        }
        for (String value : originalEntryValuesInArray) {
            mOriginalEntryValues.add(value);
        }
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.anti_flicker_preference);

        /*prize-remove-huangpengfei-2018-10-25-start*/
        /*if (mAntiFlickerSelector == null) {
            mAntiFlickerSelector = new AntiFlickerSelector();
            mAntiFlickerSelector.setOnItemClickListener(this);
        }*/
        /*prize-remove-huangpengfei-2018-10-25-end*/

        mPreference = (Preference) fragment.findPreference(mKey);
        mPreference.setRootPreference(fragment.getPreferenceScreen());
        mPreference.setId(R.id.anti_flicker_setting);
        mPreference.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.pref_camera_antibanding_content_description));
        mPreference.setSummary(mSummary);
        mPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                /*prize-change-huangpengfei-2018-10-25-start*/
                LogHelper.d(TAG, "[onPreferenceClick]");
                if (mPrizeSettingDialog == null){
                    mPrizeSettingDialog = new PrizeSettingDialog(mActivity, mPreference.getTitle());
                    mPrizeSettingDialog.init();
                    mPrizeSettingDialog.show();
                    mPrizeSettingDialog.initData(mEntries);
                    mPrizeSettingDialog.setSelectValue(mSummary);
                    mPrizeSettingDialog.setOnItemClickListener(new PrizeSettingDialog.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String value = mEntryValues.get(position);
                            mSelectedValue = value;
                            mSummary = mEntries.get(position);
                            if (mOnValueChangeListener != null) {
                                mOnValueChangeListener.onValueChanged(value);
                            }
                            refreshView();
                        }
                    });
                }else {
                    mPrizeSettingDialog.initData(mEntries);
                    mPrizeSettingDialog.setSelectValue(mSummary);
                    mPrizeSettingDialog.show();
                }
                /*mAntiFlickerSelector.setValue(mSelectedValue);
                mAntiFlickerSelector.setEntriesAndEntryValues(mEntries, mEntryValues);

                FragmentTransaction transaction = mActivity.getFragmentManager()
                        .beginTransaction();
				//prize-tangan-add prize camera-begin
                transaction.setCustomAnimations(R.anim.setting_fragment_in,R.anim.setting_fragment_out,R.anim.setting_fragment_pop_in,R.anim.setting_fragment_pop_out);
                //prize-tangan-add prize camera-end
				transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mAntiFlickerSelector, "anti_flicker_selector").commit();*/
                /*prize-change-huangpengfei-2018-10-25-end*/
                return true;
            }
        });
        mPreference.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mPreference != null) {
            LogHelper.d(TAG, "[refreshView]");
            mPreference.setSummary(mSummary);
            mPreference.setEnabled(mEnabled);
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

    @Override
    public void onItemClick(String value) {
        setValue(value);
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChanged(value);
        }
    }

    /**
     * Set listener to listen the changed anti flicker value.
     *
     * @param listener The instance of {@link OnValueChangeListener}.
     */
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
        int index = mEntryValues.indexOf(mSelectedValue);
        if (index >= 0 && index < mEntries.size()) {
            mSummary = mEntries.get(index);
        }
    }

    /**
     * Set the self timer supported.
     * @param entryValues The self timer supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntries.clear();
        mEntryValues.clear();

        for (int i = 0; i < mOriginalEntryValues.size(); i++) {
            String originalEntryValue = mOriginalEntryValues.get(i);
            for (int j = 0; j < entryValues.size(); j++) {
                String entryValue = entryValues.get(j);
                if (entryValue.equals(originalEntryValue)) {
                    mEntryValues.add(entryValue);
                    mEntries.add(mOriginalEntries.get(i));
                    break;
                }
            }
        }
    }

    /*prize-add-huangpengfei-2018-10-25-start*/
    @Override
    public void onPause() {
        if (mPrizeSettingDialog != null){
            mPrizeSettingDialog.dismiss();
        }
    }
    /*prize-add-huangpengfei-2018-10-25-end*/


    private static final int ICONS[] = new int[]{
        R.drawable.prize_selector_setting_anti_auto,
                R.drawable.prize_selector_setting_anti_off,
                R.drawable.prize_selector_setting_anti_50,
                R.drawable.prize_selector_setting_anti_60,
    };

    public int[] getIcons() {
        return ICONS;
    }

    @Override
    public List<String> getEntryValues() {
        return mEntryValues;
    }

    @Override
    public List<String> getEntrys() {
        ArrayList<String> entrys = new ArrayList<>();
        entrys.add(mActivity.getResources().getString(R.string.pref_camera_antibanding_entry_auto));
        entrys.add(mActivity.getResources().getString(R.string.pref_camera_antibanding_entry_off));
        entrys.add(mActivity.getResources().getString(R.string.pref_camera_antibanding_entry_50hz));
        entrys.add(mActivity.getResources().getString(R.string.pref_camera_antibanding_entry_60hz));
        return entrys;
    }

    public String getValue() {
        return mSelectedValue;
    }

    public int getTitle() {
        return R.string.pref_camera_antibanding_title;
    }

    public void onValueChanged(String newValue){
        mSelectedValue = newValue;
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChanged(newValue);
        }
    }

    @Override
    public int getSettingType() {
        return SETTING_TYPE_LIST;
    }

    public int getOrder(){
        return 80;
    }
}
