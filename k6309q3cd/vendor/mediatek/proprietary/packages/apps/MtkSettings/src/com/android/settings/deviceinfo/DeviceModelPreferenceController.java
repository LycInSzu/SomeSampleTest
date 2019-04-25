/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IDeviceInfoSettingsExt;

public class DeviceModelPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_RSC = "ro.boot.rsc";
    private final Fragment mHost;
    private static IDeviceInfoSettingsExt mExt;

    public DeviceModelPreferenceController(Context context, Fragment host) {
        super(context);
        mHost = host;
        mExt = UtilsExt.getDeviceInfoSettingsExt(context);
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_show_device_model);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(KEY_DEVICE_MODEL);
        if (pref != null) {
            //prize modified by liyuchong, show model name for condor in about phone,20190124-begin
            pref.setSummary(mContext.getResources().getString(R.string.model_summary,
                    getCondorDeviceModel()));
            mExt.updateSummary(pref, Build.MODEL,
                    mContext.getResources().getString(R.string.model_summary,
                            getCondorDeviceModel()));
            //prize modified by liyuchong, show model name for condor in about phone,20190124-begin
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DEVICE_MODEL;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_DEVICE_MODEL)) {
            return false;
        }
        final HardwareInfoDialogFragment fragment = HardwareInfoDialogFragment.newInstance();
        fragment.show(mHost.getFragmentManager(), HardwareInfoDialogFragment.TAG);
        return true;
    }

    public static String getDeviceModel() {
        // prize modify for bug66476 by houjian start
        String deviceName = SystemProperties.get("prize.system.boot.rsc");
        // prize modify for bug66476 by houjian end
        deviceName = !TextUtils.isEmpty(deviceName) ? deviceName : Build.MODEL + DeviceInfoUtils.getMsvSuffix();
        //prize modified by xiekui, fix bug 74122, 20190408-start
        return UtilsExt.useDeviceInfoSettingsExt() == null ? deviceName : UtilsExt.useDeviceInfoSettingsExt().customeModelInfo(deviceName);
        //prize modified by xiekui, fix bug 74122, 20190408-end
    }
    //prize add by liyuchong, show model name for condor in about phone,20190124-begin
        public static  String getCondorDeviceModel() {
            String deviceModel = SystemProperties.get("ro.pri_condor_market_model");
            return !TextUtils.isEmpty(deviceModel) ? deviceModel : "";
    }
    //prize add by liyuchong, show model name for condor in about phone,20190124-end
}
