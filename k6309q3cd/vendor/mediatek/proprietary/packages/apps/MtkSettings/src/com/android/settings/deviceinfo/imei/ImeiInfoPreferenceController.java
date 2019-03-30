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

package com.android.settings.deviceinfo.imei;

import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;

import android.app.Fragment;
import android.content.Context;
/// M: Add for updating IMEI.
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
/// M: Add for updating IMEI.
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
/// M: Add for updating IMEI. @{
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
/// @}
import com.android.settingslib.deviceinfo.AbstractSimStatusImeiInfoPreferenceController;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that manages preference for single and multi sim devices.
 */
/// M: Revise for updating IMEI.
public class ImeiInfoPreferenceController extends AbstractSimStatusImeiInfoPreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnDestroy {

    private static final String TAG = "ImeiInfoPreferenceController";

    private static final String KEY_IMEI_INFO = "imei_info";

    private final boolean mIsMultiSim;
    private final TelephonyManager mTelephonyManager;
    private final List<Preference> mPreferenceList = new ArrayList<>();
    private final Fragment mFragment;

    /// M: Add for updating IMEI.
    private final SubscriptionManager mSubscriptionManager;

    /// M: Revise for updating IMEI. @{
    public ImeiInfoPreferenceController(Context context, Fragment fragment, Lifecycle lifecycle) {
        super(context);

        mFragment = fragment;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mIsMultiSim = mTelephonyManager.getPhoneCount() > 1;

        // Get subscription manager.
        mSubscriptionManager = (SubscriptionManager) context.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        // Add this controller into lifecycle.
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }
    /// @}

    @Override
    public String getPreferenceKey() {
        return KEY_IMEI_INFO;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference preference = screen.findPreference(getPreferenceKey());
        if (!isAvailable() || preference == null || !preference.isVisible()) {
            return;
        }

        mPreferenceList.add(preference);
        updatePreference(preference, 0 /* sim slot */);

        final int imeiPreferenceOrder = preference.getOrder();
        // Add additional preferences for each sim in the device
        for (int simSlotNumber = 1; simSlotNumber < mTelephonyManager.getPhoneCount();
                simSlotNumber++) {
            final Preference multiSimPreference = createNewPreference(screen.getContext());
            multiSimPreference.setOrder(imeiPreferenceOrder + simSlotNumber);
            multiSimPreference.setKey(KEY_IMEI_INFO + simSlotNumber);
            screen.addPreference(multiSimPreference);
            mPreferenceList.add(multiSimPreference);
            updatePreference(multiSimPreference, simSlotNumber);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        final int simSlot = mPreferenceList.indexOf(preference);
        if (simSlot == -1) {
            return false;
        }

        ImeiInfoDialogFragment.show(mFragment, simSlot, preference.getTitle().toString());
        return true;
    }

    private void updatePreference(Preference preference, int simSlot) {
        /// M: Get the phone type for the specified SIM slot.
		//prize modified by liyuchong 20190328 for remove MEID shown in setting---begin
        //final int phoneType = mTelephonyManager.getCurrentPhoneTypeForSlot(simSlot);

        //if (phoneType == PHONE_TYPE_CDMA) {
        //    preference.setTitle(getTitleForCdmaPhone(simSlot));
        //    preference.setSummary(getMeid(simSlot));
        //} else {
            // GSM phone
            preference.setTitle(getTitleForGsmPhone(simSlot));
            preference.setSummary(mTelephonyManager.getImei(simSlot));
        //}
        //prize modified by liyuchong 20190328 for remove MEID shown in setting---end
    }

    private CharSequence getTitleForGsmPhone(int simSlot) {
        return mIsMultiSim ? mContext.getString(R.string.imei_multi_sim, simSlot + 1)
                : mContext.getString(R.string.status_imei);
    }

    private CharSequence getTitleForCdmaPhone(int simSlot) {
        return mIsMultiSim ? mContext.getString(R.string.meid_multi_sim, simSlot + 1)
                : mContext.getString(R.string.status_meid_number);
    }

    @VisibleForTesting
    String getMeid(int simSlot) {
        return mTelephonyManager.getMeid(simSlot);
    }

    @VisibleForTesting
    Preference createNewPreference(Context context) {
        return new Preference(context);
    }

    /// M: Register listener for updating IMEI. @{
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
            = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            Log.d(TAG, "onSubscriptionsChanged");
            for (int simSlotNumber = 0; simSlotNumber < mPreferenceList.size(); simSlotNumber++) {
                Preference imeiInfoPreference = mPreferenceList.get(simSlotNumber);
                updatePreference(imeiInfoPreference, simSlotNumber);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Register listener for updating IMEI.
        mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
    }

    @Override
    public void onDestroy() {
        // Unregister listener.
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
    }
    /// @}
}
