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

package com.mediatek.camera.feature.setting.touchshutter;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.ui.prize.PrizeCameraSettingView;

/**
 * EIS setting view.
 */

public class TouchShutterSettingView extends PrizeCameraSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(TouchShutterSettingView.class.getSimpleName());

    private OnTouchShutterClickListener mListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled;

    /**
     * Listener to listen Mirror is clicked.
     */
    public interface OnTouchShutterClickListener {
        /**
         * Callback when Mirror item is clicked by user.
         *
         * @param checked True means Mirror is opened, false means Mirror is closed.
         */
        void onTouchShutterClicked(boolean checked);
    }

    /**
     * Mirror setting view constructor.
     *
     * @param key The key of setting.
     */
    public TouchShutterSettingView(String key) {
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.touch_shutter_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.touch_shutter_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.touch_shutter_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean checked = (Boolean) o;
                mChecked = checked;
                mListener.onTouchShutterClicked(checked);
                return true;
            }
        });
        mPref.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            mPref.setChecked(mChecked);
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
     * Set listener to listen Mirror is clicked.
     *
     * @param listener The instance of {@link OnTouchShutterClickListener}.
     */
    public void setTouchShutterOnClickListener(OnTouchShutterClickListener listener) {
        mListener = listener;
    }

    /**
     * Set Mirror state.
     *
     * @param checked True means Mirror is opened, false means Mirror is closed.
     */
    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    // [Add for CCT tool] Receive keycode and enable/disable Mirror @{
    public IApp.KeyEventListener getKeyEventListener() {
        return new IApp.KeyEventListener() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if ((keyCode != CameraUtil.KEYCODE_ENABLE_ZSD
                        && keyCode != CameraUtil.KEYCODE_DISABLE_ZSD)
                        || !CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                if (keyCode != CameraUtil.KEYCODE_ENABLE_ZSD
                        && keyCode != CameraUtil.KEYCODE_DISABLE_ZSD) {
                    return false;
                }
                if (mPref == null) {
                    LogHelper.e(TAG, "onKeyUp mPref  of zsd is null");
                    return false;
                } else {
                    LogHelper.d(TAG, "onKeyUp mPref of zsd is " + mPref.isEnabled());
                }

                if (keyCode == CameraUtil.KEYCODE_ENABLE_ZSD && mPref.isEnabled()) {
                    mChecked = true;
                    mListener.onTouchShutterClicked(true);
                } else if (keyCode == CameraUtil.KEYCODE_DISABLE_ZSD && mPref.isEnabled()) {
                    mChecked = false;
                    mListener.onTouchShutterClicked(false);
                }
                return true;
            }
        };
    }
    // @}

    private static final int ICONS[] = new int[]{
            R.drawable.prize_setting_touch_on,
            R.drawable.prize_setting_touch_off,
    };

    public int[] getIcons() {
        return ICONS;
    }

    public String getValue() {
        return mChecked ? VALUES_ON : VALUES_OFF;
    }

    public int getTitle() {
        return R.string.touch_shutter_title;
    }

    public void onValueChanged(String newValue){
        mChecked = VALUES_ON.equals(newValue);
        mListener.onTouchShutterClicked(mChecked);
    }

    public int getOrder(){
        return 55;
    }
}
