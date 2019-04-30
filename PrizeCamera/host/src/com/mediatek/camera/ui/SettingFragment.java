/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;
//prize-tangan-20180921-add prize camera-begin
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.widget.PrizePreferenceFragment;
import com.mediatek.camera.feature.setting.professional.PrizeProfessionalListPreferenceFragment;
import android.widget.TextView;
import android.app.FragmentTransaction;

import com.mediatek.camera.prize.PrizeLifeCycle;
import com.mediatek.camera.common.relation.DataStore;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
//prize-tangan-20180921-add prize camera-end
/**
 * Provide setting UI for camera.
 */
public class SettingFragment extends PrizePreferenceFragment implements IApp.BackPressedListener {/*prize-add specialty mode -hpf-2018-09-10-start*/
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SettingFragment.class.getSimpleName());

    private List<ICameraSettingView> mSettingViewList = new ArrayList<>();
    private StateListener mStateListener;
    private Toolbar mToolbar;

    /*prize-add-hpf-2018-09-04-start*/
    private OnBackPressListener mOnBackPressListener;
    private Preference mPreference;
    private List<ICameraSettingView> mEntries;
    private PrizeProfessionalListPreferenceFragment mSProfessionalList;
    private TextView mTvTitle;
    private CameraActivity mActivity;
    private DataStore mDataStore;

    public interface OnBackPressListener{
        void onBackPress();
    }

    public void setOnBackPressListener(OnBackPressListener onBackPressListener) {
        mOnBackPressListener = onBackPressListener;
    }
    /*prize-add-hpf-2018-09-04-end*/

    /**
     * Listener to listen setting fragment's state.
     */
    public interface StateListener {
        /**
         * Callback when setting fragment is created.
         */
        public void onCreate();

        /**
         * Callback when setting fragment is resumed.
         */
        public void onResume();

        /**
         * Callback when setting fragment is paused.
         */
        public void onPause();

        /**
         * Callback when setting fragment is destroyed.
         */
        public void onDestroy();
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.d(TAG, "[onCreate]");
        if (mStateListener != null) {
            mStateListener.onCreate();
        }
        super.onCreate(savedInstanceState);
		/*prize-add-hpf-2018-09-04-start*/
        /*mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getActivity().getResources().getString(R.string.setting_title));
            mToolbar.setTitleTextColor(
                    getActivity().getResources().getColor(android.R.color.white));
            mToolbar.setNavigationIcon(
                    getActivity().getResources().getDrawable(R.drawable.ic_setting_up));
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogHelper.i(TAG, "[onClick], activity:" + getActivity());
                    if (getActivity() != null) {
                        getActivity().getFragmentManager().popBackStack();
                    }
                }
            });
        }*/
        mActivity = (CameraActivity) getActivity();
        initBackPressedListener();
        mDataStore = new DataStore(mActivity);
        mTvTitle = (TextView) mActivity.findViewById(R.id.prize_setting_title);
        if (mTvTitle != null) {
            mTvTitle.setText(R.string.setting_title);
        }
        View back = mActivity.findViewById(R.id.prize_setting_back);
        if (back != null) {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogHelper.i(TAG, "[onClick], activity:" + mActivity);
                    int stack = mActivity.getFragmentManager().getBackStackEntryCount();
                    if (mOnBackPressListener != null && stack < 2){
                        mOnBackPressListener.onBackPress();
                        return;
                    }
                    /*prize-add-hpf-2018-09-04-end*/
                    if (mActivity != null) {
                        mActivity.getFragmentManager().popBackStack();
                    }
                }
            });
        }

        View reset = mActivity.findViewById(R.id.prize_setting_reset);
        if (reset != null) {
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showResetDialog();
                }
            });
        }
        addPreferencesFromResource(R.xml.camera_preferences);

        synchronized (this) {
		/*prize-add -hpf-2018-09-10-start*/
            mEntries = new ArrayList<>();
            for (ICameraSettingView view : mSettingViewList) {
                LogHelper.i(TAG, "[onCreate]" + view.getClass().getSimpleName() + "  enabled = " + view.isEnabled());
                if (view instanceof com.mediatek.camera.feature.setting.whitebalance.WhiteBalanceSettingView ||
                        view instanceof com.mediatek.camera.feature.setting.scenemode.SceneModeSettingView ||
                        view instanceof com.mediatek.camera.feature.setting.iso.ISOSettingView){
                    if (!mEntries .contains(view)){
                        mEntries.add(view);
                    }
                    continue;
                }
                if (view instanceof com.mediatek.camera.feature.setting.ais.AISSettingView){
                    continue;
                }
                view.loadView(this);
            }
            if (mEntries.size() > 0){
//                addPreferencesFromResource(R.xml.professional_preference);
//                initProfessionalPreference();
            }
        }
    }

    private void initBackPressedListener(){
        mActivity.registerBackPressedListener(this,1);
    }

    @Override
    public boolean onBackPressed() {
        LogHelper.i(TAG, "[onBackPressed]");
        int stack = mActivity.getFragmentManager().getBackStackEntryCount();
        if (mOnBackPressListener != null && stack < 2){
            mOnBackPressListener.onBackPress();
            return true;
        }
        if (mActivity != null) {
            mActivity.getFragmentManager().popBackStack();
        }
        return true;
    }

    private void showResetDialog(){
        LogHelper.i(TAG, "[onClick] showResetDialog...");
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.camera_setting_reset_title)
                .setMessage(R.string.camera_setting_reset_message)
                .setNegativeButton(mActivity.getResources()
                        .getString(R.string.setting_dialog_cancel),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(mActivity.getResources()
                        .getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogHelper.i(TAG, "[onClick] yes...");
                        mDataStore.resetSettingsData();
                        mActivity.reset();
                        IAppUi appUi = mActivity.getAppUi();
                        ((CameraAppUI)appUi).hideSetting();
                    }
                }).create();
        dialog.show();
    }

    private void initProfessionalPreference(){
        mPreference = (Preference) getPreferenceManager().findPreference("key_professional");
        mPreference.setRootPreference(getPreferenceScreen());
        mPreference.setId(R.id.professional_setting);
        mPreference.setContentDescription(mActivity.getResources()
                .getString(R.string.pref_camera_professional_content_description));
        mPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                switchFragment();
                return false;
            }
        });
    }

    private void switchFragment() {
        if (mSProfessionalList == null){
            mSProfessionalList = new PrizeProfessionalListPreferenceFragment();
        }
        mSProfessionalList.setEntryView(mEntries);
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.setting_fragment_in,R.anim.setting_fragment_out,R.anim.setting_fragment_pop_in,R.anim.setting_fragment_pop_out);
        transaction.addToBackStack(null);
        transaction.replace(R.id.setting_container,
                mSProfessionalList, "specialty_list").commit();
    }
    /*prize-add -hpf-2018-09-10-end*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogHelper.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        LogHelper.d(TAG, "[onResume]");
        super.onResume();
		/*prize-add specialty mode -hpf-2018-09-10-start*/
        if (mTvTitle != null) {
            mTvTitle.setText(R.string.setting_title);
        }
		/*prize-add specialty mode -hpf-2018-09-10-end*/
        synchronized (this) {
            for (ICameraSettingView view : mSettingViewList) {
                view.refreshView();
            }
        }
        if (mStateListener != null) {
            mStateListener.onResume();
        }
    }

    @Override
    public void onPause() {
        LogHelper.d(TAG, "[onPause]");
        super.onPause();
        if (mStateListener != null) {
            mStateListener.onPause();
        }
        /*prize-add-huangpengfei-2018-10-25-start*/
        for (ICameraSettingView view : mSettingViewList) {
            if (view instanceof PrizeLifeCycle){
                ((PrizeLifeCycle) view).onPause();
            }
        }
        /*prize-add-huangpengfei-2018-10-25-end*/
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "[onDestroy]");
        super.onDestroy();
        synchronized (this) {
            for (ICameraSettingView view : mSettingViewList) {
                view.unloadView();
            }
        }
        if (mStateListener != null) {
            mStateListener.onDestroy();
        }
        mActivity.unRegisterBackPressedListener(this);//prize-add-huangpengfei-2018-11-2
    }

    /**
     * Add setting view instance to setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    public synchronized void addSettingView(ICameraSettingView view) {
        LogHelper.i(TAG, "[addSettingView], view:" + view);
        if (view == null) {
            LogHelper.w(TAG, "[addSettingView], view:" + view, new Throwable());
            return;
        }
        if (!mSettingViewList.contains(view)) {
            mSettingViewList.add(view);
        }
    }

    /**
     * Remove setting view instance from setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    public synchronized void removeSettingView(ICameraSettingView view) {
        mSettingViewList.remove(view);
    }

    /**
     * Refresh setting view.
     */
    public synchronized void refreshSettingView() {
        for (ICameraSettingView view : mSettingViewList) {
            view.refreshView();
        }
    }

    /**
     * Whether setting view tree has any visible child or not. True means it has at least
     * one visible child, false means it don't has any visible child.
     *
     * @return False if setting view tree don't has any visible child.
     */
    public synchronized boolean hasVisibleChild() {
        if (ICameraSettingView.JUST_DISABLE_UI_WHEN_NOT_SELECTABLE) {
            return mSettingViewList.size() > 0;
        }

        boolean visible = false;
        for (ICameraSettingView view : mSettingViewList) {
            if (view.isEnabled()) {
                visible = true;
            }
        }
        return visible;
    }
}
