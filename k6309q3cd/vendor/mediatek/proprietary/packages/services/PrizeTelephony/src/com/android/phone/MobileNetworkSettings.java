/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.phone;

import static android.provider.Telephony.Carriers.ENFORCE_MANAGED_URI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.euicc.EuiccManager;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.settings.PhoneAccountSettingsFragment;
import com.android.settingslib.RestrictedLockUtils;

import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.ModemSwitchHandler;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkPhoneConstants;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.settings.Enhanced4GLteSwitchPreference;
import com.mediatek.settings.MobileNetworkSettingsOmEx;
import com.mediatek.settings.TelephonyUtils;
import com.mediatek.settings.cdma.CdmaNetworkSettings;
import com.mediatek.settings.cdma.TelephonyUtilsEx;
import com.mediatek.telephony.MtkTelephonyManagerEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mediatek.telephony.MtkCarrierConfigManager;

/**
 * "Mobile network settings" screen.  This screen lets you
 * enable/disable mobile data, and control data roaming and other
 * network-specific mobile data features.  It's used on non-voice-capable
 * tablets as well as regular phone devices.
 *
 * Note that this Activity is part of the phone app, even though
 * you reach it from the "Wireless & Networks" section of the main
 * Settings app.  It's not part of the "Call settings" hierarchy that's
 * available from the Phone app (see CallFeaturesSetting for that.)
 */

public class MobileNetworkSettings extends Activity  {

    // CID of the device.
    private static final String KEY_CID = "ro.boot.cid";
    // CIDs of devices which should not show anything related to eSIM.
    private static final String KEY_ESIM_CID_IGNORE = "ro.setupwizard.esim_cid_ignore";
    // System Property which is used to decide whether the default eSIM UI will be shown,
    // the default value is false.
    private static final String KEY_ENABLE_ESIM_UI_BY_DEFAULT =
            "esim.enable_esim_system_ui_by_default";

    private enum TabState {
        NO_TABS, UPDATE, DO_NOTHING
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        MobileNetworkFragment fragment = (MobileNetworkFragment) getFragmentManager()
                .findFragmentById(R.id.network_setting_content);
        if (fragment != null) {
            fragment.onIntentUpdate(intent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_setting);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.network_setting_content);
        if (fragment == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.network_setting_content, new MobileNetworkFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Whether to show the entry point to eUICC settings.
     *
     * <p>We show the entry point on any device which supports eUICC as long as either the eUICC
     * was ever provisioned (that is, at least one profile was ever downloaded onto it), or if
     * the user has enabled development mode.
     */
    public static boolean showEuiccSettings(Context context) {
        EuiccManager euiccManager =
                (EuiccManager) context.getSystemService(Context.EUICC_SERVICE);
        if (!euiccManager.isEnabled()) {
            return false;
        }

        ContentResolver cr = context.getContentResolver();

        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String currentCountry = tm.getNetworkCountryIso().toLowerCase();
        String supportedCountries =
                Settings.Global.getString(cr, Settings.Global.EUICC_SUPPORTED_COUNTRIES);
        boolean inEsimSupportedCountries = false;
        if (TextUtils.isEmpty(currentCountry)) {
            inEsimSupportedCountries = true;
        } else if (!TextUtils.isEmpty(supportedCountries)) {
            List<String> supportedCountryList =
                    Arrays.asList(TextUtils.split(supportedCountries.toLowerCase(), ","));
            if (supportedCountryList.contains(currentCountry)) {
                inEsimSupportedCountries = true;
            }
        }
        final boolean esimIgnoredDevice =
                Arrays.asList(TextUtils.split(SystemProperties.get(KEY_ESIM_CID_IGNORE, ""), ","))
                        .contains(SystemProperties.get(KEY_CID, null));
        final boolean enabledEsimUiByDefault =
                SystemProperties.getBoolean(KEY_ENABLE_ESIM_UI_BY_DEFAULT, true);
        final boolean euiccProvisioned =
                Settings.Global.getInt(cr, Settings.Global.EUICC_PROVISIONED, 0) != 0;
        final boolean inDeveloperMode =
                Settings.Global.getInt(cr, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;

        return (inDeveloperMode || euiccProvisioned
                || (!esimIgnoredDevice && enabledEsimUiByDefault && inEsimSupportedCountries));
    }

    /**
     * Whether to show the Enhanced 4G LTE settings in search result.
     *
     * <p>We show this settings if the VoLTE can be enabled by this device and the carrier app
     * doesn't set {@link CarrierConfigManager#KEY_HIDE_ENHANCED_4G_LTE_BOOL} to false.
     */
    public static boolean hideEnhanced4gLteSettings(Context context) {
        List<SubscriptionInfo> sil =
                SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        // Check all active subscriptions. We only hide the button if it's disabled for all
        // active subscriptions.
        if (sil != null) {
            for (SubscriptionInfo subInfo : sil) {
                ImsManager imsManager = ImsManager.getInstance(context, subInfo.getSimSlotIndex());
                PersistableBundle carrierConfig = PhoneGlobals.getInstance()
                        .getCarrierConfigForSubId(subInfo.getSubscriptionId());
                if ((imsManager.isVolteEnabledByPlatform()
                        && imsManager.isVolteProvisionedOnDevice())
                        || carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns if DPC APNs are enforced.
     */
    public static boolean isDpcApnEnforced(Context context) {
        try (Cursor enforceCursor = context.getContentResolver().query(ENFORCE_MANAGED_URI,
                null, null, null, null)) {
            if (enforceCursor == null || enforceCursor.getCount() != 1) {
                return false;
            }
            enforceCursor.moveToFirst();
            return enforceCursor.getInt(0) > 0;
        }
    }

    public static class MobileNetworkFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener, RoamingDialogFragment.RoamingDialogListener {

        // debug data
        private static final String LOG_TAG = "NetworkSettings";
        private static final boolean DBG = "eng".equals(Build.TYPE);
        public static final int REQUEST_CODE_EXIT_ECM = 17;

        // Number of active Subscriptions to show tabs
        private static final int TAB_THRESHOLD = 2;

        // Number of last phone number digits shown in Euicc Setting tab
        private static final int NUM_LAST_PHONE_DIGITS = 4;

        // fragment tag for roaming data dialog
        private static final String ROAMING_TAG = "RoamingDialogFragment";

        //String keys for preference lookup
        public static final String BUTTON_PREFERED_NETWORK_MODE = "preferred_network_mode_key";
        private static final String BUTTON_ROAMING_KEY = "button_roaming_key";
        private static final String BUTTON_CDMA_LTE_DATA_SERVICE_KEY = "cdma_lte_data_service_key";
        public static final String BUTTON_ENABLED_NETWORKS_KEY = "enabled_networks_key";
        private static final String BUTTON_4G_LTE_KEY = "enhanced_4g_lte";
        private static final String BUTTON_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";
        private static final String BUTTON_CARRIER_SETTINGS_KEY = "carrier_settings_key";
        private static final String BUTTON_CDMA_SYSTEM_SELECT_KEY = "cdma_system_select_key";
        private static final String BUTTON_CDMA_SUBSCRIPTION_KEY = "cdma_subscription_key";
        private static final String BUTTON_CARRIER_SETTINGS_EUICC_KEY =
                "carrier_settings_euicc_key";
        private static final String BUTTON_WIFI_CALLING_KEY = "wifi_calling_key";
        private static final String BUTTON_VIDEO_CALLING_KEY = "video_calling_key";
        private static final String BUTTON_MOBILE_DATA_ENABLE_KEY = "mobile_data_enable";
        private static final String BUTTON_DATA_USAGE_KEY = "data_usage_summary";
        private static final String BUTTON_ADVANCED_OPTIONS_KEY = "advanced_options";
        private static final String CATEGORY_CALLING_KEY = "calling";
        private static final String CATEGORY_GSM_APN_EXPAND_KEY = "category_gsm_apn_key";
        private static final String CATEGORY_CDMA_APN_EXPAND_KEY = "category_cdma_apn_key";
        private static final String BUTTON_GSM_APN_EXPAND_KEY = "button_gsm_apn_key";
        private static final String BUTTON_CDMA_APN_EXPAND_KEY = "button_cdma_apn_key";

        private static final String ENHANCED_4G_MODE_ENABLED_SIM2 = "volte_vt_enabled_sim2";

        //private final BroadcastReceiver mPhoneChangeReceiver = new PhoneChangeReceiver();
        private final ContentObserver mDpcEnforcedContentObserver = new DpcApnEnforcedObserver();

        static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;

        //Information about logical "up" Activity
        private static final String UP_ACTIVITY_PACKAGE = "com.android.settings";
        private static final String UP_ACTIVITY_CLASS =
                "com.android.settings.Settings$WirelessSettingsActivity";

        //Information that needs to save into Bundle.
        private static final String EXPAND_ADVANCED_FIELDS = "expand_advanced_fields";
        //Intent extra to indicate expand all fields.
        private static final String EXPAND_EXTRA = "expandable";

        private SubscriptionManager mSubscriptionManager;
        private TelephonyManager mTelephonyManager;

        //UI objects
        private AdvancedOptionsPreference mAdvancedOptions;
        private ListPreference mButtonPreferredNetworkMode;
        private ListPreference mButtonEnabledNetworks;
        private RestrictedSwitchPreference mButtonDataRoam;
        private SwitchPreference mButton4glte;
        private Preference mLteDataServicePref;
        private Preference mEuiccSettingsPref;
        private PreferenceCategory mCallingCategory;
        private Preference mWiFiCallingPref;
        private SwitchPreference mVideoCallingPref;
        private NetworkSelectListPreference mButtonNetworkSelect;
        private MobileDataPreference mMobileDataPref;
        private DataUsagePreference mDataUsagePref;

        private static final String iface = "rmnet0"; //TODO: this will go away
        private List<SubscriptionInfo> mActiveSubInfos;

        private UserManager mUm;
        private Phone mPhone;
        private ImsManager mImsMgr;
        private MyHandler mHandler;
        private boolean mOkClicked;
        private boolean mExpandAdvancedFields;

        // We assume the the value returned by mTabHost.getCurrentTab() == slotId
        private TabHost mTabHost;

        //GsmUmts options and Cdma options
        GsmUmtsOptions mGsmUmtsOptions;
        CdmaOptions mCdmaOptions;

        private Preference mClickedPreference;
        private boolean mShow4GForLTE;
        private boolean mIsGlobalCdma;
        private boolean mUnavailable;
        /// Add for C2K OM features
        private CdmaNetworkSettings mCdmaNetworkSettings;
        //prize add by liyuchong, change network type list as custom request ,20190322-begin
		private void condorNetworkMode(){
			    if (mButtonEnabledNetworks != null) {
						mButtonEnabledNetworks.setEntries(R.array.enabled_networks_auto_choices_for_condor);
						mButtonEnabledNetworks.setEntryValues(R.array.enabled_networks_auto_values_for_condor);
						}
				if (mButtonPreferredNetworkMode != null) {
						mButtonPreferredNetworkMode.setEntries(R.array.enabled_networks_auto_choices_for_condor);
						mButtonPreferredNetworkMode.setEntryValues(R.array.enabled_networks_auto_values_for_condor);
				 }
		}
		//prize add by liyuchong, change network type list as custom request ,20190322-end
        private class PhoneCallStateListener extends PhoneStateListener {
            /*
             * Enable/disable the 'Enhanced 4G LTE Mode' when in/out of a call
             * and depending on TTY mode and TTY support over VoLTE.
             * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
             * java.lang.String)
             */
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DBG) log("PhoneStateListener.onCallStateChanged: state=" + state);

                updateWiFiCallState();
                updateVideoCallState();

                /// M: should also update enable state in other places, so exact to method
                updateScreenStatus();
                updateEnhanced4glteEnableState();
            }
            /**
             *  For CU volte feature.
             */
            @Override
            public void onServiceStateChanged(ServiceState state) {
                if (ExtensionManager.getMobileNetworkSettingsExt().customizeCUVolte()) {
                    updateEnhanced4glteEnableState();
                }
            }

            /*
             * Listen to different subId if mPhone is updated.
             */
            protected void updatePhone() {
                int newSubId = (mPhone != null
                        && SubscriptionManager.isValidSubscriptionId(mPhone.getSubId()))
                        ? mPhone.getSubId()
                        : SubscriptionManager.INVALID_SUBSCRIPTION_ID;

                // Now, listen to new subId if it's valid.
                mTelephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);

                mSubId = newSubId;
                if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
                    mTelephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }

        private final PhoneCallStateListener mPhoneStateListener = new PhoneCallStateListener();

        /**
         * Service connection code for the NetworkQueryService.
         * Handles the work of binding to a local object so that we can make
         * the appropriate service calls.
         */

        /** Local service interface */
        private INetworkQueryService mNetworkQueryService = null;

        private void setNetworkQueryService() {
            mButtonNetworkSelect = (NetworkSelectListPreference) getPreferenceScreen()
                    .findPreference(NetworkOperators.BUTTON_NETWORK_SELECT_KEY);
            if (mButtonNetworkSelect != null) {
                mButtonNetworkSelect.setNetworkQueryService(mNetworkQueryService);
            }

        }
        /** Service connection */
        private final ServiceConnection mNetworkQueryServiceConnection = new ServiceConnection() {

            /** Handle the task of binding the local object to the service */
            public void onServiceConnected(ComponentName className, IBinder service) {
                if (DBG) log("connection created, binding local service.");
                mNetworkQueryService = ((NetworkQueryService.LocalBinder) service).getService();
                setNetworkQueryService();
            }

            /** Handle the task of cleaning up the local binding */
            public void onServiceDisconnected(ComponentName className) {
                if (DBG) log("connection disconnected, cleaning local binding.");
                mNetworkQueryService = null;
                setNetworkQueryService();
            }
        };

        private void bindNetworkQueryService() {
            getContext().startService(new Intent(getContext(), NetworkQueryService.class));
            getContext().bindService(new Intent(getContext(), NetworkQueryService.class).setAction(
                        NetworkQueryService.ACTION_LOCAL_BINDER),
                        mNetworkQueryServiceConnection, Context.BIND_AUTO_CREATE);
        }

        private void unbindNetworkQueryService() {
            // unbind the service.
            getContext().unbindService(mNetworkQueryServiceConnection);
        }

        @Override
        public void onPositiveButtonClick(DialogFragment dialog) {
            mPhone.setDataRoamingEnabled(true);
            mButtonDataRoam.setChecked(true);
            MetricsLogger.action(getContext(),
                    getMetricsEventCategory(getPreferenceScreen(), mButtonDataRoam),
                    true);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            if (getListView() != null) {
                getListView().setDivider(null);
            }
        }

        public void onIntentUpdate(Intent intent) {
            if (!mUnavailable) {
                updateCurrentTab(intent);
            }
        }

        /**
         * Invoked on each preference click in this hierarchy, overrides
         * PreferenceActivity's implementation.  Used to make sure we track the
         * preference click events.
         */
        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            sendMetricsEventPreferenceClicked(preferenceScreen, preference);

            /** TODO: Refactor and get rid of the if's using subclasses */
            final int phoneSubId = mPhone.getSubId();
            if (mCdmaNetworkSettings != null &&
                mCdmaNetworkSettings.onPreferenceTreeClick(preferenceScreen, preference)) {
                return true;
            }
            /// M: Add for Plug-in @{
            if (ExtensionManager.getMobileNetworkSettingsExt()
                    .onPreferenceTreeClick(preferenceScreen, preference)) {
                return true;
            }
            /// @}
            if (preference.getKey().equals(BUTTON_4G_LTE_KEY)) {
                return true;
            } else if (mGsmUmtsOptions != null &&
                    mGsmUmtsOptions.preferenceTreeClick(preference) == true) {
                return true;
            } else if (mCdmaOptions != null &&
                    mCdmaOptions.preferenceTreeClick(preference) == true) {
                if (mPhone.isInEcm()) {

                    mClickedPreference = preference;

                    // In ECM mode launch ECM app dialog
                    startActivityForResult(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                            REQUEST_CODE_EXIT_ECM);
                }
                return true;
            } else if (preference == mButtonPreferredNetworkMode) {
                //displays the value taken from the Settings.System
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        preferredNetworkMode);
                mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
                return true;
            } else if (preference == mLteDataServicePref) {
                String tmpl = android.provider.Settings.Global.getString(
                        getActivity().getContentResolver(),
                        android.provider.Settings.Global.SETUP_PREPAID_DATA_SERVICE_URL);
                if (!TextUtils.isEmpty(tmpl)) {
                    String imsi = mTelephonyManager.getSubscriberId();
                    if (imsi == null) {
                        imsi = "";
                    }
                    final String url = TextUtils.isEmpty(tmpl) ? null
                            : TextUtils.expandTemplate(tmpl, imsi).toString();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    android.util.Log.e(LOG_TAG, "Missing SETUP_PREPAID_DATA_SERVICE_URL");
                }
                return true;
            }  else if (preference == mButtonEnabledNetworks) {
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        preferredNetworkMode);
                log("onPreferenceTreeClick settingsNetworkMode: " + settingsNetworkMode);
                /** M: Remove this for LW project, for we need set a temple value.
                mButtonEnabledNetworks.setValue(Integer.toString(settingsNetworkMode));
                */
                return true;
            } else if (preference == mButtonDataRoam) {
                // Do not disable the preference screen if the user clicks Data roaming.
                return true;
            } else if (preference == mEuiccSettingsPref) {
                Intent intent = new Intent(EuiccManager.ACTION_MANAGE_EMBEDDED_SUBSCRIPTIONS);
                startActivity(intent);
                return true;
            } else if (preference == mWiFiCallingPref || preference == mVideoCallingPref
                    || preference == mMobileDataPref || preference == mDataUsagePref) {
                return false;
            } else if (preference == mAdvancedOptions) {
                mExpandAdvancedFields = true;
                updateBody();
                return true;
            } else if (preference == mEnable4point5GPreference) {
                /// M: enable4_5_settings do nothing
                return true;
            } else {
                // if the button is anything but the simple toggle preference,
                // we'll need to disable all preferences to reject all click
                // events until the sub-activity's UI comes up.
                preferenceScreen.setEnabled(false);
                // Let the intents be launched by the Preference manager
                return false;
            }
        }

        private final SubscriptionManager.OnSubscriptionsChangedListener
                mOnSubscriptionsChangeListener
                = new SubscriptionManager.OnSubscriptionsChangedListener() {
            @Override
            public void onSubscriptionsChanged() {
                if (DBG) log("onSubscriptionsChanged:");
                /// M: add for hot swap @{
                if (TelephonyUtils.isHotSwapHanppened(
                            mActiveSubInfos, PhoneUtils.getActiveSubInfoList())) {
                    if (DBG) {
                        log("onSubscriptionsChanged:hot swap hanppened");
                    }
                    dissmissDialog(mButtonPreferredNetworkMode);
                    dissmissDialog(mButtonEnabledNetworks);

                    final Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                    return;
                }
                /// @}
                initializeSubscriptions();
            }
        };

        private int getSlotIdFromIntent(Intent intent) {
            Bundle data = intent.getExtras();
            int subId = -1;
            if (data != null) {
                subId = data.getInt(Settings.EXTRA_SUB_ID, -1);
            }
            return SubscriptionManager.getSlotIndex(subId);
        }

        private void initializeSubscriptions() {
            final Activity activity = getActivity();
            if (activity == null || activity.isDestroyed()) {
                // Process preferences in activity only if its not destroyed
                return;
            }
            int currentTab = 0;
            if (DBG) log("initializeSubscriptions:+");

            // Before updating the the active subscription list check
            // if tab updating is needed as the list is changing.
            List<SubscriptionInfo> sil = mSubscriptionManager.getActiveSubscriptionInfoList();
            MobileNetworkSettings.TabState state = isUpdateTabsNeeded(sil);

            // Update to the active subscription list
            mActiveSubInfos.clear();
            if (sil != null) {
                mActiveSubInfos.addAll(sil);
                /* M: remove for 3SIM feature
                // If there is only 1 sim then currenTab should represent slot no. of the sim.
                if (sil.size() == 1) {
                    currentTab = sil.get(0).getSimSlotIndex();
                }*/
            }

            switch (state) {
                case UPDATE: {
                    if (DBG) log("initializeSubscriptions: UPDATE");
                    currentTab = mTabHost != null ? mTabHost.getCurrentTab() : mCurrentTab;

                    if (mTabHost != null) {
                        mTabHost.clearAllTabs();
                        log("TabHost Clear.");
                    }

                    mTabHost = (TabHost) getActivity().findViewById(android.R.id.tabhost);
                    mTabHost.setup();

                    // Update the tabName. Since the mActiveSubInfos are in slot order
                    // we can iterate though the tabs and subscription info in one loop. But
                    // we need to handle the case where a slot may be empty.

                    /// M: change design for 3SIM feature @{
                    for (int index = 0; index  < mActiveSubInfos.size(); index++) {
                        String tabName = String.valueOf(mActiveSubInfos.get(index).
                                getDisplayName());
                        if (DBG) {
                            log("initializeSubscriptions:tab=" + index + " name=" + tabName);
                        }

                        mTabHost.addTab(buildTabSpec(String.valueOf(index), tabName));
                    }
                    /// @}

                    mTabHost.setOnTabChangedListener(mTabListener);
                    mTabHost.setCurrentTab(currentTab);
                    break;
                }
                case NO_TABS: {
                    if (DBG) log("initializeSubscriptions: NO_TABS");

                    if (mTabHost != null) {
                        mTabHost.clearAllTabs();
                        mTabHost = null;
                    }
                    break;
                }
                case DO_NOTHING: {
                    if (DBG) log("initializeSubscriptions: DO_NOTHING");
                    if (mTabHost != null) {
                        currentTab = mTabHost.getCurrentTab();
                    }
                    break;
                }
            }
            updatePhone(convertTabToSlot(currentTab));
            updateBody();
            if (DBG) log("initializeSubscriptions:-");
        }

        private MobileNetworkSettings.TabState isUpdateTabsNeeded(List<SubscriptionInfo> newSil) {
            TabState state = MobileNetworkSettings.TabState.DO_NOTHING;
            if (newSil == null) {
                if (mActiveSubInfos.size() >= TAB_THRESHOLD) {
                    if (DBG) log("isUpdateTabsNeeded: NO_TABS, size unknown and was tabbed");
                    state = MobileNetworkSettings.TabState.NO_TABS;
                }
            } else if (newSil.size() < TAB_THRESHOLD && mActiveSubInfos.size() >= TAB_THRESHOLD) {
                if (DBG) log("isUpdateTabsNeeded: NO_TABS, size went to small");
                state = MobileNetworkSettings.TabState.NO_TABS;
            } else if (newSil.size() >= TAB_THRESHOLD && mActiveSubInfos.size() < TAB_THRESHOLD) {
                if (DBG) log("isUpdateTabsNeeded: UPDATE, size changed");
                state = MobileNetworkSettings.TabState.UPDATE;
            } else if (newSil.size() >= TAB_THRESHOLD) {
                Iterator<SubscriptionInfo> siIterator = mActiveSubInfos.iterator();
                for(SubscriptionInfo newSi : newSil) {
                    SubscriptionInfo curSi = siIterator.next();
                    if (!newSi.getDisplayName().equals(curSi.getDisplayName())) {
                        if (DBG) log("isUpdateTabsNeeded: UPDATE, new name="
                                + newSi.getDisplayName());
                        state = MobileNetworkSettings.TabState.UPDATE;
                        break;
                    }
                }
            }
            if (DBG) {
                Log.i(LOG_TAG, "isUpdateTabsNeeded:- " + state
                        + " newSil.size()=" + ((newSil != null) ? newSil.size() : 0)
                        + " mActiveSubInfos.size()=" + mActiveSubInfos.size());
            }
            return state;
        }

        private TabHost.OnTabChangeListener mTabListener = new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (DBG) log("onTabChanged...:");
                // The User has changed tab; update the body.
                updatePhone(convertTabToSlot(Integer.parseInt(tabId)));
                mCurrentTab = Integer.parseInt(tabId);
                updateBody();
                /* updateBody() method updateScreenStatus
                   just when updateBodyAdvancedFields == true
                   so need updateScreenStatus again.
                */
                updateScreenStatus();
            }
        };

        private void updatePhone(int slotId) {
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(slotId);
            if (sir != null) {
                int phoneId = SubscriptionManager.getPhoneId(sir.getSubscriptionId());
                if (SubscriptionManager.isValidPhoneId(phoneId)) {
                    mPhone = PhoneFactory.getPhone(phoneId);
                }
            }
            if (mPhone == null) {
                // Do the best we can
                mPhone = PhoneGlobals.getPhone();
            }
            log("updatePhone:- slotId=" + slotId + " sir=" + sir);

            mImsMgr = ImsManager.getInstance(mPhone.getContext(), mPhone.getPhoneId());
            mTelephonyManager = new TelephonyManager(mPhone.getContext(), mPhone.getSubId());
            if (mImsMgr == null) {
                log("updatePhone :: Could not get ImsManager instance!");
            } else if (DBG) {
                log("updatePhone :: mImsMgr=" + mImsMgr);
            }

            //mPhoneStateListener.updatePhone();
        }

        private TabHost.TabContentFactory mEmptyTabContent = new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return new View(mTabHost.getContext());
            }
        };

        private TabHost.TabSpec buildTabSpec(String tag, String title) {
            return mTabHost.newTabSpec(tag).setIndicator(title).setContent(
                    mEmptyTabContent);
        }

        private void updateCurrentTab(Intent intent) {
            int slotId = getSlotIdFromIntent(intent);
            if (slotId >= 0 && mTabHost != null && mTabHost.getCurrentTab() != slotId) {
                mTabHost.setCurrentTab(slotId);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // If advanced fields are already expanded, we save it and expand it
            // when it's re-created.
            outState.putBoolean(EXPAND_ADVANCED_FIELDS, mExpandAdvancedFields);
            outState.putInt(CURRENT_TAB, mCurrentTab);
        }

        @Override
        public void onCreate(Bundle icicle) {
            log("onCreate:+++++++++++");
            super.onCreate(icicle);

            final Activity activity = getActivity();
            if (activity == null || activity.isDestroyed()) {
                Log.e(LOG_TAG, "onCreate:- with no valid activity.");
                return;
            }

             /// Add for cmcc open market @{
             mOmEx = new MobileNetworkSettingsOmEx(activity);
            /// @}
            mHandler = new MyHandler();
            mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
            mSubscriptionManager = SubscriptionManager.from(activity);
            mTelephonyManager = (TelephonyManager) activity.getSystemService(
                            Context.TELEPHONY_SERVICE);

            if (icicle != null) {
                mExpandAdvancedFields = icicle.getBoolean(EXPAND_ADVANCED_FIELDS, false);
            } else if (getActivity().getIntent().getBooleanExtra(EXPAND_EXTRA, false)) {
                mExpandAdvancedFields = true;
            }

            bindNetworkQueryService();

            addPreferencesFromResource(R.xml.network_setting_fragment);

            mButton4glte = (SwitchPreference)findPreference(BUTTON_4G_LTE_KEY);
            mButton4glte.setOnPreferenceChangeListener(this);

            mCallingCategory = (PreferenceCategory) findPreference(CATEGORY_CALLING_KEY);
            mWiFiCallingPref = findPreference(BUTTON_WIFI_CALLING_KEY);
            mVideoCallingPref = (SwitchPreference) findPreference(BUTTON_VIDEO_CALLING_KEY);
            mMobileDataPref = (MobileDataPreference) findPreference(BUTTON_MOBILE_DATA_ENABLE_KEY);
            mDataUsagePref = (DataUsagePreference) findPreference(BUTTON_DATA_USAGE_KEY);

            try {
                Context con = activity.createPackageContext("com.android.systemui", 0);
                int id = con.getResources().getIdentifier("config_show4GForLTE",
                        "bool", "com.android.systemui");
                mShow4GForLTE = con.getResources().getBoolean(id);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_TAG, "NameNotFoundException for show4GFotLTE");
                mShow4GForLTE = false;
            }
            log("mShow4GForLTE: " + mShow4GForLTE);

            //get UI object references
            PreferenceScreen prefSet = getPreferenceScreen();

            mButtonDataRoam = (RestrictedSwitchPreference) prefSet.findPreference(
                    BUTTON_ROAMING_KEY);
            mButtonPreferredNetworkMode = (ListPreference) prefSet.findPreference(
                    BUTTON_PREFERED_NETWORK_MODE);
            mButtonEnabledNetworks = (ListPreference) prefSet.findPreference(
                    BUTTON_ENABLED_NETWORKS_KEY);
            mAdvancedOptions = (AdvancedOptionsPreference) prefSet.findPreference(
                    BUTTON_ADVANCED_OPTIONS_KEY);
            mButtonDataRoam.setOnPreferenceChangeListener(this);

            mLteDataServicePref = prefSet.findPreference(BUTTON_CDMA_LTE_DATA_SERVICE_KEY);

            mEuiccSettingsPref = prefSet.findPreference(BUTTON_CARRIER_SETTINGS_EUICC_KEY);
            mEuiccSettingsPref.setOnPreferenceChangeListener(this);

            // Initialize mActiveSubInfo
            int max = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
            mActiveSubInfos = new ArrayList<SubscriptionInfo>(max);

            initIntentFilter();
            try {
                activity.registerReceiver(mReceiver, mIntentFilter);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Receiver Already registred");
            }

            mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
            mTelephonyManager.listen(
                    mPhoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE
                    | PhoneStateListener.LISTEN_SERVICE_STATE
                    );

            /// M: for screen rotate
            if (icicle != null) {
                mCurrentTab = icicle.getInt(CURRENT_TAB);
            }

            /// M: [CT VOLTE]
            activity.getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(Settings.Global.ENHANCED_4G_MODE_ENABLED),
                    true, mContentObserver);
            activity.getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(ENHANCED_4G_MODE_ENABLED_SIM2),
                    true, mContentObserver);

            activity.getContentResolver().registerContentObserver(ENFORCE_MANAGED_URI, false,
                    mDpcEnforcedContentObserver);
            log("onCreate:-");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(com.android.internal.R.layout.common_tab_settings,
                    container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)
                    || !mUm.isSystemUser()) {
                mUnavailable = true;
                getActivity().setContentView(R.layout.telephony_disallowed_preference_screen);
            } else {
                initializeSubscriptions();
                updateCurrentTab(getActivity().getIntent());
            }
        }

        /// M: Replaced with mReceiver
        /*private class PhoneChangeReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "onReceive:");
                // When the radio changes (ex: CDMA->GSM), refresh all options.
                updateBody();
            }
        }*/

        private class DpcApnEnforcedObserver extends ContentObserver {
            DpcApnEnforcedObserver() {
                super(null);
            }

            @Override
            public void onChange(boolean selfChange) {
                Log.i(LOG_TAG, "DPC enforced onChange:");
                //updateBody();
            }
        }

        @Override
        public void onDestroy() {
            unbindNetworkQueryService();
            super.onDestroy();

            final Activity activity = getActivity();
            if (activity == null) {
                Log.d(LOG_TAG, "onDestroy, activity = null");
                return;
            }

            try {
                activity.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Receiver Already unregistred");
            }
            getActivity().getContentResolver().unregisterContentObserver(
                    mDpcEnforcedContentObserver);
            ExtensionManager.getMobileNetworkSettingsExt().unRegister();
            log("onDestroy ");
            if (mCdmaNetworkSettings != null) {
                mCdmaNetworkSettings.onDestroy();
                mCdmaNetworkSettings = null;
            }
            if (mSubscriptionManager != null) {
                mSubscriptionManager
                        .removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
            }
            if (mTelephonyManager != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
            /// M: [CT VOLTE] @{
            activity.getContentResolver().unregisterContentObserver(mContentObserver);
            /// M: [CT VOLTE] @{
            if (TelephonyUtilsEx.isCtVolteEnabled()
                    && TelephonyUtilsEx.isCt4gSim(mPhone.getSubId())) {
                activity.getContentResolver().unregisterContentObserver(mNetworkObserver);
            }
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            /// @}
            /// Add for cmcc open market @{
            if (mOmEx != null) {
                mOmEx.unRegister();
            }
            /// @}

            /// M: Add for handling phone type change. @{
            if (mGsmUmtsOptions != null) {
                mGsmUmtsOptions.onDestroy();
                mGsmUmtsOptions = null;
            }
            /// @}
        }

        @Override
        public void onResume() {
            super.onResume();
            log("onResume:+");

            if (mUnavailable) {
                Log.i(LOG_TAG, "onResume:- ignore mUnavailable == false");
                return;
            }

            /// M: for C2K OM features @{
            if (mCdmaNetworkSettings != null) {
                mCdmaNetworkSettings.onResume();
            }
            /// @}
            // upon resumption from the sub-activity, make sure we re-enable the
            // preferences.
            //getPreferenceScreen().setEnabled(true);

            // Set UI state in onResume because a user could go home, launch some
            // app to change this setting's backend, and re-launch this settings app
            // and the UI state would be inconsistent with actual state
            mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());

            if (getPreferenceScreen().findPreference(BUTTON_PREFERED_NETWORK_MODE) != null
                    || getPreferenceScreen().findPreference(BUTTON_ENABLED_NETWORKS_KEY) != null)  {
                updatePreferredNetworkUIFromDb();
            }

            // NOTE: Buttons will be enabled/disabled in mPhoneStateListener
            //?updateEnhanced4gLteState();

            // Video calling and WiFi calling state might have changed.
            updateCallingCategory();

            /// M: For screen update
            updateScreenStatus();

            /// M: For plugin to update UI
            ExtensionManager.getMobileNetworkSettingsExt().onResume();

            log("onResume:-");

        }

        private boolean hasActiveSubscriptions() {
            return mActiveSubInfos.size() > 0;
        }

        private void updateBodyBasicFields(Activity activity, PreferenceScreen prefSet,
                int phoneSubId, boolean hasActiveSubscriptions) {
            Context context = activity.getApplicationContext();

            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // android.R.id.home will be triggered in onOptionsItemSelected()
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            prefSet.addPreference(mMobileDataPref);
            prefSet.addPreference(mButtonDataRoam);
            prefSet.addPreference(mDataUsagePref);

            // Customized preferences needs to be initialized with subId.
            mMobileDataPref.initialize(phoneSubId);
            mDataUsagePref.initialize(phoneSubId);

            /// M: Add for SIM Lock feature.
            mMobileDataPref.setEnabled(hasActiveSubscriptions
                    && shouldEnableCellDataPrefForSimLock());
            ExtensionManager.getMobileNetworkSettingsExt()
                    .customizeDataEnable(phoneSubId, mMobileDataPref);
            mButtonDataRoam.setEnabled(hasActiveSubscriptions);
            mDataUsagePref.setEnabled(hasActiveSubscriptions);

            // Initialize states of mButtonDataRoam.
            mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
            mButtonDataRoam.setDisabledByAdmin(false);
            if (mButtonDataRoam.isEnabled()) {
                if (RestrictedLockUtils.hasBaseUserRestriction(context,
                        UserManager.DISALLOW_DATA_ROAMING, UserHandle.myUserId())) {
                    mButtonDataRoam.setEnabled(false);
                } else {
                    mButtonDataRoam.checkRestrictionAndSetDisabled(
                            UserManager.DISALLOW_DATA_ROAMING);
                }
            }

            /// M: Add for Plug-in. @{
            if (null != mPhone) {
                ExtensionManager.getMobileNetworkSettingsExt()
                    .customizeBasicMobileNetworkSettings(prefSet, mPhone.getSubId());
            }
            /// @}
        }

        private void updateBody() {
            final Activity activity = getActivity();
            final PreferenceScreen prefSet = getPreferenceScreen();
            final int phoneSubId = mPhone.getSubId();
            final boolean hasActiveSubscriptions = hasActiveSubscriptions();

            if (activity == null || activity.isDestroyed()) {
                Log.e(LOG_TAG, "updateBody with no valid activity.");
                return;
            }

            if (prefSet == null) {
                Log.e(LOG_TAG, "updateBody with no null prefSet.");
                return;
            }

            prefSet.removeAll();

            updateBodyBasicFields(activity, prefSet, phoneSubId, hasActiveSubscriptions);

            if (mExpandAdvancedFields) {
                updateBodyAdvancedFields(activity, prefSet, phoneSubId, hasActiveSubscriptions);
            } else {
                prefSet.addPreference(mAdvancedOptions);
            }
        }

        private void updateBodyAdvancedFields(Activity activity, PreferenceScreen prefSet,
                int phoneSubId, boolean hasActiveSubscriptions) {
            boolean isLteOnCdma = mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE;

            if (DBG) {
                log("updateBody: isLteOnCdma=" + isLteOnCdma + " phoneSubId=" + phoneSubId);
            }

            prefSet.addPreference(mButtonPreferredNetworkMode);
            prefSet.addPreference(mButtonEnabledNetworks);
            prefSet.addPreference(mButton4glte);

            if (showEuiccSettings(getActivity())) {
                prefSet.addPreference(mEuiccSettingsPref);
                String spn = mTelephonyManager.getSimOperatorName();
                if (TextUtils.isEmpty(spn)) {
                    mEuiccSettingsPref.setSummary(null);
                } else {
                    mEuiccSettingsPref.setSummary(spn);
                }
            }

            int settingsNetworkMode = android.provider.Settings.Global.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                    preferredNetworkMode);

            PersistableBundle carrierConfig =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            mIsGlobalCdma = isLteOnCdma
                    && carrierConfig.getBoolean(CarrierConfigManager.KEY_SHOW_CDMA_CHOICES_BOOL);
            if (carrierConfig.getBoolean(
                    CarrierConfigManager.KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL)) {
                prefSet.removePreference(mButtonPreferredNetworkMode);
                prefSet.removePreference(mButtonEnabledNetworks);
                prefSet.removePreference(mLteDataServicePref);
            } else if (carrierConfig.getBoolean(CarrierConfigManager
                    .KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL)
                    && !mPhone.getServiceState().getRoaming()
                    && mPhone.getServiceState().getDataRegState()
                    == ServiceState.STATE_IN_SERVICE) {
                prefSet.removePreference(mButtonPreferredNetworkMode);
                prefSet.removePreference(mButtonEnabledNetworks);

                final int phoneType = mPhone.getPhoneType();
                if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    updateCdmaOptions(this, prefSet, mPhone);
                } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    updateGsmUmtsOptions(this, prefSet, phoneSubId, mNetworkQueryService);
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
                // Since pref is being hidden from user, set network mode to default
                // in case it is currently something else. That is possible if user
                // changed the setting while roaming and is now back to home network.
                settingsNetworkMode = preferredNetworkMode;
            } else if (carrierConfig.getBoolean(
                    CarrierConfigManager.KEY_WORLD_PHONE_BOOL) == true) {
                prefSet.removePreference(mButtonEnabledNetworks);
                // set the listener for the mButtonPreferredNetworkMode list preference so we can issue
                // change Preferred Network Mode.
                mButtonPreferredNetworkMode.setOnPreferenceChangeListener(this);

                updateCdmaOptions(this, prefSet, mPhone);
                updateGsmUmtsOptions(this, prefSet, phoneSubId, mNetworkQueryService);
            } else if (carrierConfig.getBoolean(
                        MtkCarrierConfigManager.MTK_KEY_SHOW_45G_OPTIONS)) {
                add4point5GPreference(prefSet);
                prefSet.removePreference(mButtonPreferredNetworkMode);
                prefSet.removePreference(mButtonEnabledNetworks);
            } else {
                prefSet.removePreference(mButtonPreferredNetworkMode);
                final int phoneType = mPhone.getPhoneType();
                int mainPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;
                IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(
                                                        ServiceManager.getService("phoneEx"));
                if (iTelEx != null) {
                    try {
                        mainPhoneId = iTelEx.getMainCapabilityPhoneId();
                    } catch (RemoteException e) {
                        log("getMainCapabilityPhoneId: remote exception");
                    }
                } else {
                    log("IMtkTelephonyEx service not ready!");
                    mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
                }
                if (TelephonyUtilsEx.isCDMAPhone(mPhone)
                    /// M: [CT VOLTE]
                    || (TelephonyUtilsEx.isCtVolteEnabled() &&
                        TelephonyUtilsEx.isCt4gSim(mPhone.getSubId()) &&
                        !TelephonyUtilsEx.isRoaming(mPhone) &&
                        (!TelephonyUtilsEx.isBothslotCtSim(mSubscriptionManager) ||
                        (mainPhoneId == mPhone.getPhoneId())))) {
                    int lteForced = android.provider.Settings.Global.getInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Global.LTE_SERVICE_FORCED + mPhone.getSubId(),
                            0);
                    log("phoneType == PhoneConstants.PHONE_TYPE_CDMA, lteForced = " + lteForced);
                    if (isLteOnCdma) {
                        if (lteForced == 0) {
                            mButtonEnabledNetworks.setEntries(
                                    R.array.enabled_networks_cdma_choices);
                            mButtonEnabledNetworks.setEntryValues(
                                    R.array.enabled_networks_cdma_values);
                        } else {
                            switch (settingsNetworkMode) {
                                case Phone.NT_MODE_CDMA:
                                case Phone.NT_MODE_CDMA_NO_EVDO:
                                case Phone.NT_MODE_EVDO_NO_CDMA:
                                    mButtonEnabledNetworks.setEntries(
                                            R.array.enabled_networks_cdma_no_lte_choices);
                                    mButtonEnabledNetworks.setEntryValues(
                                            R.array.enabled_networks_cdma_no_lte_values);
                                    break;
                                case Phone.NT_MODE_GLOBAL:
                                case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
                                case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                                case Phone.NT_MODE_LTE_ONLY:
                                    mButtonEnabledNetworks.setEntries(
                                            R.array.enabled_networks_cdma_only_lte_choices);
                                    mButtonEnabledNetworks.setEntryValues(
                                            R.array.enabled_networks_cdma_only_lte_values);
                                    break;
                                default:
                                    mButtonEnabledNetworks.setEntries(
                                            R.array.enabled_networks_cdma_choices);
                                    mButtonEnabledNetworks.setEntryValues(
                                            R.array.enabled_networks_cdma_values);
                                    break;
                            }
                        }
                    }
                    updateCdmaOptions(this, prefSet, mPhone);

                } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    if (isSupportTdscdma()) {
                        mButtonEnabledNetworks.setEntries(
                                R.array.enabled_networks_tdscdma_choices);
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_tdscdma_values);
                    } else if (!carrierConfig.getBoolean(CarrierConfigManager.KEY_PREFER_2G_BOOL)
                            && !getResources().getBoolean(R.bool.config_enabled_lte)) {
                        mButtonEnabledNetworks.setEntries(
                                R.array.enabled_networks_except_gsm_lte_choices);
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_except_gsm_lte_values);
                    } else if (!carrierConfig.getBoolean(CarrierConfigManager.KEY_PREFER_2G_BOOL)) {
                        int select = (mShow4GForLTE == true) ?
                                R.array.enabled_networks_except_gsm_4g_choices
                                : R.array.enabled_networks_except_gsm_choices;
                        mButtonEnabledNetworks.setEntries(select);
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_except_gsm_values);
                        log("!KEY_PREFER_2G_BOOL");
                    } else if (!FeatureOption.isMtkLteSupport()) {
                        mButtonEnabledNetworks.setEntries(
                                R.array.enabled_networks_except_lte_choices);
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_except_lte_values);
                    } else if (mIsGlobalCdma) {
                        mButtonEnabledNetworks.setEntries(
                                R.array.enabled_networks_cdma_choices);
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_cdma_values);
                    } else {
                        int select = (mShow4GForLTE == true) ? R.array.enabled_networks_4g_choices
                                : R.array.enabled_networks_choices;
                        mButtonEnabledNetworks.setEntries(select);
                        ExtensionManager.getMobileNetworkSettingsExt().changeEntries(
                                mButtonEnabledNetworks);
                        /// Add for C2K @{
                        if (isC2kLteSupport()) {
                            if (DBG) {
                                log("Change to C2K values");
                            }
                            mButtonEnabledNetworks.setEntryValues(
                                    R.array.enabled_networks_values_c2k);
                        } else {
                            log("!isC2kLteSupport");
                            mButtonEnabledNetworks.setEntryValues(
                                    R.array.enabled_networks_values);
                        }
                        /// @}
                    }
                    updateGsmUmtsOptions(this, prefSet, phoneSubId, mNetworkQueryService);
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
                if (isWorldMode()) {
                    mButtonEnabledNetworks.setEntries(
                            R.array.preferred_network_mode_choices_world_mode);
                    mButtonEnabledNetworks.setEntryValues(
                            R.array.preferred_network_mode_values_world_mode);
                }
                mButtonEnabledNetworks.setOnPreferenceChangeListener(this);
                if (DBG) log("settingsNetworkMode: " + settingsNetworkMode);
            }
            //prize add by liyuchong, change network type list as custom request ,20190322-begin
			condorNetworkMode();
		    //prize add by liyuchong, change network type list as custom request ,20190322-end
            final boolean missingDataServiceUrl = TextUtils.isEmpty(
                    android.provider.Settings.Global.getString(activity.getContentResolver(),
                            android.provider.Settings.Global.SETUP_PREPAID_DATA_SERVICE_URL));
            if (!isLteOnCdma || missingDataServiceUrl) {
                prefSet.removePreference(mLteDataServicePref);
            } else {
                android.util.Log.d(LOG_TAG, "keep ltePref");
            }
            /// M: add mtk feature.
            onCreateMTK(prefSet);
            updateCallingCategory();

            if (carrierConfig.getBoolean(MtkCarrierConfigManager.MTK_KEY_ROAMING_BAR_GUARD_BOOL)) {
                int order = mButtonDataRoam.getOrder();
                prefSet.removePreference(mButtonDataRoam);
                Preference sprintPreference = new Preference(activity);
                sprintPreference.setKey(BUTTON_SPRINT_ROAMING_SETTINGS);
                sprintPreference.setTitle(R.string.roaming_settings);
                Intent intentRoaming = new Intent();
                intentRoaming.setClassName("com.android.phone",
                        "com.mediatek.services.telephony.RoamingSettings");
                intentRoaming.putExtra(SubscriptionInfoHelper.SUB_ID_EXTRA, mPhone.getSubId());
                sprintPreference.setIntent(intentRoaming);
                sprintPreference.setOrder(order);
                prefSet.addPreference(sprintPreference);
            }
            // Enable link to CMAS app settings depending on the value in config.xml.
            final boolean isCellBroadcastAppLinkEnabled = activity.getResources().getBoolean(
                    com.android.internal.R.bool.config_cellBroadcastAppLinks);
            if (!mUm.isAdminUser() || !isCellBroadcastAppLinkEnabled
                    || mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS)) {
                PreferenceScreen root = getPreferenceScreen();
                Preference ps = findPreference(BUTTON_CELL_BROADCAST_SETTINGS);
                if (ps != null) {
                    root.removePreference(ps);
                }
            }

            /**
             * Listen to extra preference changes that need as Metrics events logging.
             */
            if (prefSet.findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY) != null) {
                prefSet.findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY)
                        .setOnPreferenceChangeListener(this);
            }

            if (prefSet.findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY) != null) {
                prefSet.findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY)
                        .setOnPreferenceChangeListener(this);
            }

            // Get the networkMode from Settings.System and displays it
            mButtonDataRoam.setChecked(mPhone.getDataRoamingEnabled());
            mButtonEnabledNetworks.setValue(Integer.toString(settingsNetworkMode));
            mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
            UpdatePreferredNetworkModeSummary(settingsNetworkMode);
            UpdateEnabledNetworksValueAndSummary(settingsNetworkMode);
            // Display preferred network type based on what modem returns b/18676277
            /// M: no need set mode here
            //mPhone.setPreferredNetworkType(settingsNetworkMode, mHandler
            //        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));

            /**
             * Enable/disable depending upon if there are any active subscriptions.
             *
             * I've decided to put this enable/disable code at the bottom as the
             * code above works even when there are no active subscriptions, thus
             * putting it afterwards is a smaller change. This can be refined later,
             * but you do need to remember that this all needs to work when subscriptions
             * change dynamically such as when hot swapping sims.

            boolean useVariant4glteTitle = carrierConfig.getBoolean(
                    CarrierConfigManager.KEY_ENHANCED_4G_LTE_TITLE_VARIANT_BOOL);
            int enhanced4glteModeTitleId = useVariant4glteTitle ?
                    R.string.enhanced_4g_lte_mode_title_variant :
                    R.string.enhanced_4g_lte_mode_title;

            mButtonPreferredNetworkMode.setEnabled(hasActiveSubscriptions);
            mButtonEnabledNetworks.setEnabled(hasActiveSubscriptions);
            mButton4glte.setTitle(enhanced4glteModeTitleId);
            mLteDataServicePref.setEnabled(hasActiveSubscriptions);
            Preference ps;
            ps = findPreference(BUTTON_CELL_BROADCAST_SETTINGS);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }
            ps = findPreference(CATEGORY_GSM_APN_EXPAND_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }
            ps = findPreference(CATEGORY_CDMA_APN_EXPAND_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }*/
            Preference ps;
            ps = findPreference(NetworkOperators.CATEGORY_NETWORK_OPERATORS_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }
            /*ps = findPreference(BUTTON_CARRIER_SETTINGS_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }
            ps = findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }*/
            ps = findPreference(CATEGORY_CALLING_KEY);
            if (ps != null) {
                ps.setEnabled(hasActiveSubscriptions);
            }

            /// Add for cmcc open market @{
            mOmEx.updateNetworkTypeSummary(mButtonEnabledNetworks);
            /// @}
            /// M: Add for L+W DSDS.
            if (ExtensionManager.getMobileNetworkSettingsExt().isNetworkModeSettingNeeded()) {
                updateNetworkModeForLwDsds();
            }

            /// M: Add for Plug-in @{
            if (mButtonEnabledNetworks != null) {
                log("Enter plug-in update updateNetworkTypeSummary - Enabled again!");
                ExtensionManager.getMobileNetworkSettingsExt()
                        .updateNetworkTypeSummary(mButtonEnabledNetworks);
            }
            /// @}
        }

        @Override
        public void onPause() {
            /// M: For plugin to update UI
            ExtensionManager.getMobileNetworkSettingsExt().onPause();
            super.onPause();
            if (DBG) log("onPause:+");
        }

        /**
         * Implemented to support onPreferenceChangeListener to look for preference
         * changes specifically on CLIR.
         *
         * @param preference is the preference to be changed, should be mButtonCLIR.
         * @param objValue should be the value of the selection, NOT its localized
         * display value.
         */
        public boolean onPreferenceChange(Preference preference, Object objValue) {
            sendMetricsEventPreferenceChanged(getPreferenceScreen(), preference, objValue);

            final int phoneSubId = mPhone.getSubId();
            if (onPreferenceChangeMTK(preference, objValue)) {
                return true;
            }
            if (preference == mButtonPreferredNetworkMode) {
                //NOTE onPreferenceChange seems to be called even if there is no change
                //Check if the button value is changed from the System.Setting
                mButtonPreferredNetworkMode.setValue((String) objValue);
                int buttonNetworkMode;
                buttonNetworkMode = Integer.parseInt((String) objValue);
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        preferredNetworkMode);
                log("onPreferenceChange buttonNetworkMode:"
                    + buttonNetworkMode + " settingsNetworkMode:" + settingsNetworkMode);
                if (buttonNetworkMode != settingsNetworkMode) {
                    int modemNetworkMode;
                    // if new mode is invalid ignore it
                    switch (buttonNetworkMode) {
                        case Phone.NT_MODE_WCDMA_PREF:
                        case Phone.NT_MODE_GSM_ONLY:
                        case Phone.NT_MODE_WCDMA_ONLY:
                        case Phone.NT_MODE_GSM_UMTS:
                        case Phone.NT_MODE_CDMA:
                        case Phone.NT_MODE_CDMA_NO_EVDO:
                        case Phone.NT_MODE_EVDO_NO_CDMA:
                        case Phone.NT_MODE_GLOBAL:
                        case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
                        case Phone.NT_MODE_LTE_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_ONLY:
                        case Phone.NT_MODE_LTE_WCDMA:
                        case Phone.NT_MODE_TDSCDMA_ONLY:
                        case Phone.NT_MODE_TDSCDMA_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA:
                        case Phone.NT_MODE_TDSCDMA_GSM:
                        case Phone.NT_MODE_LTE_TDSCDMA_GSM:
                        case Phone.NT_MODE_TDSCDMA_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_GSM_WCDMA:
                        case Phone.NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                            // This is one of the modes we recognize
                            modemNetworkMode = buttonNetworkMode;
                            break;
                        default:
                            loge("Invalid Network Mode (" +buttonNetworkMode+ ") chosen. Ignore.");
                            return true;
                    }

                    mButtonPreferredNetworkMode.setValue(Integer.toString(modemNetworkMode));
                    mButtonPreferredNetworkMode.setSummary(mButtonPreferredNetworkMode.getEntry());

                    /// M: 03100374, need to revert the network mode if set fail
                    mPreNetworkMode = settingsNetworkMode;

                    android.provider.Settings.Global.putInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                            buttonNetworkMode );
                    if (DBG) {
                        log("setPreferredNetworkType, networkType: " + modemNetworkMode);
                    }
                    //Set the modem network mode
                    mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                            .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
                }
            } else if (preference == mButtonEnabledNetworks) {
                mButtonEnabledNetworks.setValue((String) objValue);
                int buttonNetworkMode;
                buttonNetworkMode = Integer.parseInt((String) objValue);
                if (DBG) log("onPreferenceChange buttonNetworkMode: " + buttonNetworkMode);
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        preferredNetworkMode);

                if (DBG) {
                    log("buttonNetworkMode: " + buttonNetworkMode +
                        "settingsNetworkMode: " + settingsNetworkMode);
                }
                if (buttonNetworkMode != settingsNetworkMode ||
                        ExtensionManager.getMobileNetworkSettingsExt().isNetworkChanged(
                            mButtonEnabledNetworks, buttonNetworkMode, settingsNetworkMode,
                            mPhone)) {
                    int modemNetworkMode;
                    // if new mode is invalid ignore it
                    switch (buttonNetworkMode) {
                        case Phone.NT_MODE_WCDMA_PREF:
                        case Phone.NT_MODE_GSM_ONLY:
                        case Phone.NT_MODE_LTE_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                        case Phone.NT_MODE_CDMA:
                        case Phone.NT_MODE_CDMA_NO_EVDO:
                        case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
                        case Phone.NT_MODE_TDSCDMA_ONLY:
                        case Phone.NT_MODE_TDSCDMA_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA:
                        case Phone.NT_MODE_LTE_WCDMA:
                        case Phone.NT_MODE_TDSCDMA_GSM:
                        case Phone.NT_MODE_LTE_TDSCDMA_GSM:
                        case Phone.NT_MODE_TDSCDMA_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_GSM_WCDMA:
                        case Phone.NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                        case Phone.NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                        case Phone.NT_MODE_WCDMA_ONLY:
                        case Phone.NT_MODE_LTE_ONLY:
                        /// M: Add for C2K
                        case Phone.NT_MODE_GLOBAL:
                            // This is one of the modes we recognize
                            modemNetworkMode = buttonNetworkMode;
                            break;
                        default:
                            loge("Invalid Network Mode (" +buttonNetworkMode+ ") chosen. Ignore.");
                            return true;
                    }

                    UpdateEnabledNetworksValueAndSummary(buttonNetworkMode);

                    /// M: 03100374, need to revert the network mode if set fail
                    mPreNetworkMode = settingsNetworkMode;

                    if (ExtensionManager.getMobileNetworkSettingsExt().isNetworkUpdateNeeded(
                            mButtonEnabledNetworks,
                            buttonNetworkMode,
                            settingsNetworkMode,
                            mPhone,
                            mPhone.getContext().getContentResolver(),
                            phoneSubId, mHandler)) {
                        UpdateEnabledNetworksValueAndSummary(buttonNetworkMode);

                        android.provider.Settings.Global.putInt(
                                mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                                buttonNetworkMode );
                            //if (DBG) {
                                log("setPreferredNetworkType, networkType: " + modemNetworkMode);
                            //}
                        //Set the modem network mode
                        mPhone.setPreferredNetworkType(modemNetworkMode, mHandler
                                .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
                    }
                }
            } else if (preference == mButton4glte) {
                SwitchPreference enhanced4gModePref = (SwitchPreference) preference;
                boolean enhanced4gMode = !enhanced4gModePref.isChecked();
                enhanced4gModePref.setChecked(enhanced4gMode);
                MtkImsManager.setEnhanced4gLteModeSetting(getActivity(),
                        enhanced4gModePref.isChecked(), mPhone.getPhoneId());
            } else if (preference == mButtonDataRoam) {
                if (DBG) log("onPreferenceTreeClick: preference == mButtonDataRoam.");

                //normally called on the toggle click
                if (!mButtonDataRoam.isChecked()) {
                    PersistableBundle carrierConfig =
                            PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
                    if (carrierConfig != null && carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_DISABLE_CHARGE_INDICATION_BOOL)) {
                        mPhone.setDataRoamingEnabled(true);
                        MetricsLogger.action(getContext(),
                                getMetricsEventCategory(getPreferenceScreen(), mButtonDataRoam),
                                true);
                    } else {
                        // MetricsEvent with no value update.
                        MetricsLogger.action(getContext(),
                                getMetricsEventCategory(getPreferenceScreen(), mButtonDataRoam));
                        // First confirm with a warning dialog about charges
                        mOkClicked = false;
                        RoamingDialogFragment fragment = new RoamingDialogFragment();
                        fragment.setPhone(mPhone);
                        fragment.show(getFragmentManager(), ROAMING_TAG);
                        // Don't update the toggle unless the confirm button is actually pressed.
                        return false;
                    }
                } else {
                    mPhone.setDataRoamingEnabled(false);
                    MetricsLogger.action(getContext(),
                            getMetricsEventCategory(getPreferenceScreen(), mButtonDataRoam),
                            false);
                    return true;
                }
            } else if (preference == mVideoCallingPref) {
                // If mButton4glte is not checked, mVideoCallingPref should be disabled.
                // So it only makes sense to call phoneMgr.enableVideoCalling if it's checked.
                if ((mEnhancedButton4glte != null) && mEnhancedButton4glte.isChecked()) {
                    mImsMgr.setVtSetting((boolean) objValue);
                    return true;
                } else {
                    loge("mVideoCallingPref should be disabled if mButton4glte is not checked.");
                    mVideoCallingPref.setEnabled(false);
                    return false;
                }
            } else if (preference == getPreferenceScreen()
                    .findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY)
                    || preference == getPreferenceScreen()
                    .findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY)) {
                return true;
            } else if (preference == mEnable4point5GPreference) {
                /// M: enable4_5_settings enable volte and
                int networkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        preferredNetworkMode);
                if (mEnable4point5GPreference.isChecked()) {
                    networkMode = Phone.NT_MODE_WCDMA_PREF;
                    log("Update mode on unchecked:" + networkMode);
                } else {
                    networkMode = Phone.NT_MODE_LTE_GSM_WCDMA;
                    log("Update mode on checked:" + networkMode);
                }
                android.provider.Settings.Global.putInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        networkMode);
                //Set the modem network mode
                mPhone.setPreferredNetworkType(networkMode, mHandler
                        .obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
                return true;
            }

            /// Add for Plug-in @{
            ExtensionManager.getMobileNetworkSettingsExt().onPreferenceChange(preference, objValue);
            /// @}
            //updateBody();
            // always let the preference setting proceed.
            return true;
        }

        private boolean is4gLtePrefEnabled(PersistableBundle carrierConfig) {
            return (mTelephonyManager.getCallState(mPhone.getSubId())
                    == TelephonyManager.CALL_STATE_IDLE)
                    && mImsMgr != null
                    && mImsMgr.isNonTtyOrTtyOnVolteEnabled()
                    && carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL);
        }

        private class MyHandler extends Handler {

            static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 0;

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
                        handleSetPreferredNetworkTypeResponse(msg);
                        break;
                }
            }

            private void handleSetPreferredNetworkTypeResponse(Message msg) {
                /// M: 03100374 restore network mode in case set fail
                restorePreferredNetworkTypeIfNeeded(msg);
                final Activity activity = getActivity();
                if (activity == null || activity.isDestroyed()) {
                    // Access preferences of activity only if it is not destroyed
                    // or if fragment is not attached to an activity.
                    return;
                }

                AsyncResult ar = (AsyncResult) msg.obj;
                final int phoneSubId = mPhone.getSubId();

                if (ar.exception == null) {
                    int networkMode;
                    if (getPreferenceScreen().findPreference(
                            BUTTON_PREFERED_NETWORK_MODE) != null)  {
                        networkMode =  Integer.parseInt(mButtonPreferredNetworkMode.getValue());
                        if (DBG) {
                            log("handleSetPreferredNetwrokTypeResponse1: networkMode:" +
                                    networkMode);
                        }
                        android.provider.Settings.Global.putInt(
                                mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Global.PREFERRED_NETWORK_MODE
                                        + phoneSubId,
                                networkMode );
                    }
                    if (getPreferenceScreen().findPreference(BUTTON_ENABLED_NETWORKS_KEY) != null) {
                        networkMode = Integer.parseInt(mButtonEnabledNetworks.getValue());
                        if (DBG) {
                            log("handleSetPreferredNetwrokTypeResponse2: networkMode:" +
                                    networkMode);
                        }
                        android.provider.Settings.Global.putInt(
                                mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Global.PREFERRED_NETWORK_MODE
                                        + phoneSubId,
                                networkMode );
                    }
                    log("Start Network updated intent");
                    Intent intent = new Intent(TelephonyUtils.ACTION_NETWORK_CHANGED);
                    activity.sendBroadcast(intent);
                } else {
                    Log.i(LOG_TAG, "handleSetPreferredNetworkTypeResponse:" +
                            "exception in setting network mode.");
                    updatePreferredNetworkUIFromDb();
                }
            }
        }

        private void updatePreferredNetworkUIFromDb() {
            final int phoneSubId = mPhone.getSubId();

            int settingsNetworkMode = android.provider.Settings.Global.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                    preferredNetworkMode);

            if (DBG) {
                log("updatePreferredNetworkUIFromDb: settingsNetworkMode = " +
                        settingsNetworkMode);
            }

            UpdatePreferredNetworkModeSummary(settingsNetworkMode);
            UpdateEnabledNetworksValueAndSummary(settingsNetworkMode);
            // changes the mButtonPreferredNetworkMode accordingly to settingsNetworkMode
            mButtonPreferredNetworkMode.setValue(Integer.toString(settingsNetworkMode));
        }

        private void UpdatePreferredNetworkModeSummary(int NetworkMode) {
            // M: if is not 3/4G phone, init the preference with gsm only type @{
            if (!isCapabilityPhone(mPhone)) {
                NetworkMode = Phone.NT_MODE_GSM_ONLY;
                log("init PreferredNetworkMode with gsm only");
            }
            // @}
            switch(NetworkMode) {
                case Phone.NT_MODE_TDSCDMA_GSM_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_tdscdma_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_TDSCDMA_GSM:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_tdscdma_gsm_summary);
                    break;
                case Phone.NT_MODE_WCDMA_PREF:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_wcdma_perf_summary);
                    break;
                case Phone.NT_MODE_GSM_ONLY:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_gsm_only_summary);
                    break;
                case Phone.NT_MODE_TDSCDMA_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_tdscdma_wcdma_summary);
                    break;
                case Phone.NT_MODE_WCDMA_ONLY:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_wcdma_only_summary);
                    break;
                case Phone.NT_MODE_GSM_UMTS:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_CDMA:
                    switch (mPhone.getLteOnCdmaMode()) {
                        case PhoneConstants.LTE_ON_CDMA_TRUE:
                            mButtonPreferredNetworkMode.setSummary(
                                    R.string.preferred_network_mode_cdma_summary);
                            break;
                        case PhoneConstants.LTE_ON_CDMA_FALSE:
                        default:
                            mButtonPreferredNetworkMode.setSummary(
                                    R.string.preferred_network_mode_cdma_evdo_summary);
                            break;
                    }
                    break;
                case Phone.NT_MODE_CDMA_NO_EVDO:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_cdma_only_summary);
                    break;
                case Phone.NT_MODE_EVDO_NO_CDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_evdo_only_summary);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_tdscdma_summary);
                    break;
                case Phone.NT_MODE_LTE_ONLY:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_summary);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA_GSM:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_tdscdma_gsm_summary);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA_GSM_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_tdscdma_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_LTE_GSM_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_cdma_evdo_summary);
                    break;
                case Phone.NT_MODE_TDSCDMA_ONLY:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_tdscdma_summary);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_tdscdma_cdma_evdo_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                    if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA ||
                            mIsGlobalCdma ||
                            isWorldMode()) {
                        mButtonPreferredNetworkMode.setSummary(
                                R.string.preferred_network_mode_global_summary);
                    } else {
                        mButtonPreferredNetworkMode.setSummary(
                                R.string.preferred_network_mode_lte_summary);
                    }
                    break;
                case Phone.NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_tdscdma_cdma_evdo_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_GLOBAL:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_cdma_evdo_gsm_wcdma_summary);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_tdscdma_wcdma_summary);
                    break;
                case Phone.NT_MODE_LTE_WCDMA:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_lte_wcdma_summary);
                    break;
                default:
                    mButtonPreferredNetworkMode.setSummary(
                            R.string.preferred_network_mode_global_summary);
            }
            /// Add for Plug-in @{
            ExtensionManager.getMobileNetworkSettingsExt()
                    .updateNetworkTypeSummary(mButtonPreferredNetworkMode);
            /// @}
            /// Add for cmcc open market @{
            mOmEx.updateNetworkTypeSummary(mButtonPreferredNetworkMode);
            /// @}
        }

        private void UpdateEnabledNetworksValueAndSummary(int NetworkMode) {
            if (DBG) {
                Log.d(LOG_TAG, "NetworkMode: " + NetworkMode);
            }
            // M: if is not 3/4G phone, init the preference with gsm only type @{
            if (!isCapabilityPhone(mPhone)) {
                NetworkMode = Phone.NT_MODE_GSM_ONLY;
                log("init EnabledNetworks with gsm only");
            }
            // @}
            switch (NetworkMode) {
                case Phone.NT_MODE_TDSCDMA_WCDMA:
                case Phone.NT_MODE_TDSCDMA_GSM_WCDMA:
                case Phone.NT_MODE_TDSCDMA_GSM:
                    mButtonEnabledNetworks.setValue(
                            Integer.toString(Phone.NT_MODE_TDSCDMA_GSM_WCDMA));
                    mButtonEnabledNetworks.setSummary(R.string.network_3G);
                    break;
                case Phone.NT_MODE_WCDMA_ONLY:
				   //prize add by liyuchong, change network type list as custom request ,20190322-begin
                    mButtonEnabledNetworks.setValue(
                            Integer.toString(Phone.NT_MODE_WCDMA_ONLY));
                    mButtonEnabledNetworks.setSummary(R.string.network_3G_only_for_Condor);
                    break;
					//prize add by liyuchong, change network type list as custom request ,20190322-end
                case Phone.NT_MODE_GSM_UMTS:
                case Phone.NT_MODE_WCDMA_PREF:
                    if (!mIsGlobalCdma) {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_WCDMA_PREF));
						//prize modified by liyuchong, change network type list as custom request ,20190322-begin
                        mButtonEnabledNetworks.setSummary(R.string.network_3G_or_2G_for_Condor);
						//prize modified by liyuchong, change network type list as custom request ,20190322-end
                    } else {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA));
                        mButtonEnabledNetworks.setSummary(R.string.network_global);
                    }
                    break;
                case Phone.NT_MODE_GSM_ONLY:
                    if (!mIsGlobalCdma) {
						//prize modified by liyuchong, change network type list as custom request ,20190322-begin
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_GSM_ONLY));
                        mButtonEnabledNetworks.setSummary(R.string.network_2G_onlly_for_Condor);
						//prize modified by liyuchong, change network type list as custom request ,20190322-end
                    } else {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA));
                        mButtonEnabledNetworks.setSummary(R.string.network_global);
                    }
                    break;
                case Phone.NT_MODE_LTE_GSM_WCDMA:
                    if (isWorldMode()) {
						//prize modified by liyuchong, change network type list as custom request ,20190322-begin
                        mButtonEnabledNetworks.setSummary(
                                R.string.network_auto_for_Condor);
						//prize modified by liyuchong, change network type list as custom request ,20190322-end
                        controlCdmaOptions(false);
                        controlGsmOptions(true);
                        break;
                    }
                case Phone.NT_MODE_LTE_ONLY:
                case Phone.NT_MODE_LTE_WCDMA:
                    if (!mIsGlobalCdma) {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_GSM_WCDMA));
						//prize modified by liyuchong, change network type list as custom request ,20190322-begin
                        mButtonEnabledNetworks.setSummary(
                                R.string.network_auto_for_Condor);
						//prize modified by liyuchong, change network type list as custom request ,20190322-end
                    } else {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA));
                        mButtonEnabledNetworks.setSummary(R.string.network_global);
                    }
                    break;
                case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
                    if (isWorldMode()) {
                        mButtonEnabledNetworks.setSummary(
                                R.string.preferred_network_mode_lte_cdma_summary);
                        controlCdmaOptions(true);
                        controlGsmOptions(false);
                    } else {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_CDMA_AND_EVDO));
                        mButtonEnabledNetworks.setSummary(R.string.network_lte);
                    }
                    break;
                case Phone.NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                    mButtonEnabledNetworks.setValue(
                            Integer.toString(Phone.NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA));
                    mButtonEnabledNetworks.setSummary(R.string.network_3G);
                    break;
                case Phone.NT_MODE_CDMA:
                case Phone.NT_MODE_EVDO_NO_CDMA:
                case Phone.NT_MODE_GLOBAL:
                    /// M: For C2K @{
                    if (isC2kLteSupport()) {
                        log("Update value to Global for c2k project");
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_GLOBAL));
                    } else {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_CDMA));
                    }
                    /// @}

                    mButtonEnabledNetworks.setSummary(R.string.network_3G);
                    break;
                case Phone.NT_MODE_CDMA_NO_EVDO:
                    mButtonEnabledNetworks.setValue(
                            Integer.toString(Phone.NT_MODE_CDMA_NO_EVDO));
                    mButtonEnabledNetworks.setSummary(R.string.network_1x);
                    break;
                case Phone.NT_MODE_TDSCDMA_ONLY:
                    mButtonEnabledNetworks.setValue(
                            Integer.toString(Phone.NT_MODE_TDSCDMA_ONLY));
                    mButtonEnabledNetworks.setSummary(R.string.network_3G);
                    break;
                case Phone.NT_MODE_LTE_TDSCDMA_GSM:
                case Phone.NT_MODE_LTE_TDSCDMA_GSM_WCDMA:
                case Phone.NT_MODE_LTE_TDSCDMA:
                case Phone.NT_MODE_LTE_TDSCDMA_WCDMA:
                case Phone.NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
                case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
                    if (isSupportTdscdma()) {
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA));
                        mButtonEnabledNetworks.setSummary(R.string.network_lte);
                    } else {
                        if (isWorldMode()) {
                            controlCdmaOptions(true);
                            controlGsmOptions(false);
                        }
                        mButtonEnabledNetworks.setValue(
                                Integer.toString(Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA));
                        if (mPhone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA ||
                                mIsGlobalCdma ||
                                isWorldMode()) {
                            mButtonEnabledNetworks.setSummary(R.string.network_global);
                        } else {
						//prize modified by liyuchong, change network type list as custom request ,20190322-begin
                        mButtonEnabledNetworks.setSummary(
                                R.string.network_auto_for_Condor);
						//prize modified by liyuchong, change network type list as custom request ,20190322-end
                        }
                    }
                    break;
                default:
                    String errMsg = "Invalid Network Mode (" + NetworkMode + "). Ignore.";
                    loge(errMsg);
                    mButtonEnabledNetworks.setSummary(errMsg);
            }
            ExtensionManager.getMobileNetworkSettingsExt().
            updatePreferredNetworkValueAndSummary(mButtonEnabledNetworks, NetworkMode);
            /// Add for Plug-in @{
            if (mButtonEnabledNetworks != null) {
                log("Enter plug-in update updateNetworkTypeSummary - Enabled.");
                ExtensionManager.getMobileNetworkSettingsExt()
                        .updateNetworkTypeSummary(mButtonEnabledNetworks);
                /// Add for cmcc open market @{
                mOmEx.updateNetworkTypeSummary(mButtonEnabledNetworks);
                /// @}
            }
            /// @}
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch(requestCode) {
                case REQUEST_CODE_EXIT_ECM:
                    Boolean isChoiceYes = data.getBooleanExtra(
                            EmergencyCallbackModeExitDialog.EXTRA_EXIT_ECM_RESULT, false);
                    if (isChoiceYes && mClickedPreference != null) {
                        // If the phone exits from ECM mode, show the CDMA Options
                        mCdmaOptions.showDialog(mClickedPreference);
                    } else {
                        // do nothing
                    }
                    break;

                default:
                    break;
            }
        }

        private void updateWiFiCallState() {
            Context context = getContext();
            if (mWiFiCallingPref == null || mCallingCategory == null || context == null) {
                return;
            }

            if (!mExpandAdvancedFields) {
                return;
            }

            boolean removePref = false;
            final PhoneAccountHandle simCallManager =
                    TelecomManager.from(context).getSimCallManager();

            if (simCallManager != null) {
                Intent intent = PhoneAccountSettingsFragment.buildPhoneAccountConfigureIntent(
                        context, simCallManager);
                if ((intent != null) && (mPhone != null)) {
                    PackageManager pm = mPhone.getContext().getPackageManager();
                    List<ResolveInfo> resolutions = pm.queryIntentActivities(intent, 0);
                    if (!resolutions.isEmpty()) {
                        mWiFiCallingPref.setTitle(resolutions.get(0).loadLabel(pm));
                        mWiFiCallingPref.setSummary(null);
                        mWiFiCallingPref.setIntent(intent);
                    } else {
                        removePref = true;
                    }
                } else {
                    removePref = true;
                }
            } else if (mImsMgr == null
                    || !mImsMgr.isWfcEnabledByPlatform()
                    || !mImsMgr.isWfcProvisionedOnDevice()
                    /// M: Add for Plug-in. @{
                    || !ExtensionManager.getMobileNetworkSettingsExt()
                            .isWfcProvisioned(context, mPhone.getPhoneId())
                    /// @}
                    ) {
                removePref = true;
            } else {
                int resId = com.android.internal.R.string.wifi_calling_off_summary;
                if (mImsMgr.isWfcEnabledByUser()) {
                    boolean isRoaming = mTelephonyManager.isNetworkRoaming();
                    int wfcMode = mImsMgr.getWfcMode(isRoaming);

                    switch (wfcMode) {
                        case ImsConfig.WfcModeFeatureValueConstants.WIFI_ONLY:
                            resId = com.android.internal.R.string.wfc_mode_wifi_only_summary;
                            break;
                        case ImsConfig.WfcModeFeatureValueConstants.CELLULAR_PREFERRED:
                            resId = com.android.internal.R.string
                                    .wfc_mode_cellular_preferred_summary;
                            break;
                        case ImsConfig.WfcModeFeatureValueConstants.WIFI_PREFERRED:
                            resId = com.android.internal.R.string.wfc_mode_wifi_preferred_summary;
                            break;
                        default:
                            if (DBG) log("Unexpected WFC mode value: " + wfcMode);
                    }
                }
                /// M: Add for Plug-in. @{
                String wfcSummary = ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeWfcSummary(context, resId, mPhone.getPhoneId());
                mWiFiCallingPref.setSummary(wfcSummary);
                /// @}
            }

            if (removePref) {
                mCallingCategory.removePreference(mWiFiCallingPref);
            } else {
                mCallingCategory.addPreference(mWiFiCallingPref);
                mWiFiCallingPref.setEnabled(mTelephonyManager.getCallState(mPhone.getSubId())
                        == TelephonyManager.CALL_STATE_IDLE && hasActiveSubscriptions());

                /// M: Add for Plug-in. @{
                ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeWfcPreference(context, getPreferenceScreen(), mCallingCategory,
                                mPhone.getPhoneId());
                /// @}
            }
        }

        private void updateEnhanced4gLteState() {
            if (mButton4glte == null) {
                return;
            }

            PersistableBundle carrierConfig = PhoneGlobals.getInstance()
                    .getCarrierConfigForSubId(mPhone.getSubId());

            try {
                if ((mImsMgr == null
                        || mImsMgr.getImsServiceState() != ImsFeature.STATE_READY
                        || !mImsMgr.isVolteEnabledByPlatform()
                        || !mImsMgr.isVolteProvisionedOnDevice()
                        || carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL))) {
                    getPreferenceScreen().removePreference(mButton4glte);
                } else {
                    mButton4glte.setEnabled(is4gLtePrefEnabled(carrierConfig)
                            && hasActiveSubscriptions());
                    boolean enh4glteMode = mImsMgr.isEnhanced4gLteModeSettingEnabledByUser()
                            && mImsMgr.isNonTtyOrTtyOnVolteEnabled();
                    mButton4glte.setChecked(enh4glteMode);
                }
            } catch (ImsException ex) {
                log("Exception when trying to get ImsServiceStatus: " + ex);
                getPreferenceScreen().removePreference(mButton4glte);
            }
        }

        private void updateVideoCallState() {
            if (mVideoCallingPref == null || mCallingCategory == null) {
                return;
            }

            if (!mExpandAdvancedFields) {
                return;
            }

            log("updateVideoCallState");

            PersistableBundle carrierConfig = PhoneGlobals.getInstance()
                    .getCarrierConfigForSubId(mPhone.getSubId());

            if (mImsMgr != null
                    && mImsMgr.isVtEnabledByPlatform()
                    && mImsMgr.isVtProvisionedOnDevice()
                    && (carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_IGNORE_DATA_ENABLED_CHANGED_FOR_VIDEO_CALLS)
                        || mPhone.mDcTracker.isDataEnabled())) {
                mCallingCategory.addPreference(mVideoCallingPref);
                if ((mEnhancedButton4glte == null) || !mEnhancedButton4glte.isChecked()) {
                    log("state false");
                    mVideoCallingPref.setEnabled(false);
                    mVideoCallingPref.setChecked(false);
                } else {
                    mVideoCallingPref.setEnabled(mTelephonyManager.getCallState(mPhone.getSubId())
                            == TelephonyManager.CALL_STATE_IDLE && hasActiveSubscriptions());
                    log("state true");
                    mVideoCallingPref.setChecked(mImsMgr.isVtEnabledByUser());
                    mVideoCallingPref.setOnPreferenceChangeListener(this);
                }
            } else {
                mCallingCategory.removePreference(mVideoCallingPref);
            }
        }

        private void updateCallingCategory() {
            if (mCallingCategory == null) {
                return;
            }

            if (!mExpandAdvancedFields) {
                return;
            }

            updateWiFiCallState();
            updateVideoCallState();

            // If all items in calling category is removed, we remove it from
            // the screen. Otherwise we'll see title of the category but nothing
            // is in there.
            if (mCallingCategory.getPreferenceCount() == 0) {
                getPreferenceScreen().removePreference(mCallingCategory);
            } else {
                getPreferenceScreen().addPreference(mCallingCategory);
            }
        }

        private static void log(String msg) {
            if (DBG) {
                Log.d(LOG_TAG, msg);
            }
        }

        private static void loge(String msg) {
            Log.e(LOG_TAG, msg);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
                // Commenting out "logical up" capability. This is a workaround for issue 5278083.
                //
                // Settings app may not launch this activity via UP_ACTIVITY_CLASS but the other
                // Activity that looks exactly same as UP_ACTIVITY_CLASS ("SubSettings" Activity).
                // At that moment, this Activity launches UP_ACTIVITY_CLASS on top of the Activity.
                // which confuses users.
                // TODO: introduce better mechanism for "up" capability here.
            /*Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(UP_ACTIVITY_PACKAGE, UP_ACTIVITY_CLASS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private boolean isWorldMode() {
            boolean worldModeOn = false;
            final String configString = getResources().getString(R.string.config_world_mode);

            if (!TextUtils.isEmpty(configString)) {
                String[] configArray = configString.split(";");
                // Check if we have World mode configuration set to True only or config is set to True
                // and SIM GID value is also set and matches to the current SIM GID.
                if (configArray != null &&
                        ((configArray.length == 1 && configArray[0].equalsIgnoreCase("true"))
                                || (configArray.length == 2 && !TextUtils.isEmpty(configArray[1])
                                && mTelephonyManager != null
                                && configArray[1].equalsIgnoreCase(
                                        mTelephonyManager.getGroupIdLevel1())))) {
                    worldModeOn = true;
                }
            }

            if (DBG) {
                Log.d(LOG_TAG, "isWorldMode=" + worldModeOn);
            }

            return worldModeOn;
        }

        private void controlGsmOptions(boolean enable) {
            PreferenceScreen prefSet = getPreferenceScreen();
            if (prefSet == null) {
                return;
            }

            updateGsmUmtsOptions(this, prefSet, mPhone.getSubId(), mNetworkQueryService);

            PreferenceCategory networkOperatorCategory =
                    (PreferenceCategory) prefSet.findPreference(
                            NetworkOperators.CATEGORY_NETWORK_OPERATORS_KEY);
            Preference carrierSettings = prefSet.findPreference(BUTTON_CARRIER_SETTINGS_KEY);
            if (networkOperatorCategory != null) {
                if (enable) {
                    networkOperatorCategory.setEnabled(true);
                } else {
                    prefSet.removePreference(networkOperatorCategory);
                }
            }
            if (carrierSettings != null) {
                prefSet.removePreference(carrierSettings);
            }
        }

        private void controlCdmaOptions(boolean enable) {
            PreferenceScreen prefSet = getPreferenceScreen();
            if (prefSet == null) {
                return;
            }
            updateCdmaOptions(this, prefSet, mPhone);
            CdmaSystemSelectListPreference systemSelect =
                    (CdmaSystemSelectListPreference)prefSet.findPreference
                            (BUTTON_CDMA_SYSTEM_SELECT_KEY);
            if (systemSelect != null) {
                systemSelect.setEnabled(enable);
            }
        }

        private boolean isSupportTdscdma() {
            /// M: TODO: temple solution for MR1 changes
            /*if (getResources().getBoolean(R.bool.config_support_tdscdma)) {
                return true;
            }

            String operatorNumeric = mPhone.getServiceState().getOperatorNumeric();
            String[] numericArray = getResources().getStringArray(
                    R.array.config_support_tdscdma_roaming_on_networks);
            if (numericArray.length == 0 || operatorNumeric == null) {
                return false;
            }
            for (String numeric : numericArray) {
                if (operatorNumeric.equals(numeric)) {
                    return true;
                }
            }*/
            return false;
        }

        /**
         * Metrics events related methods. it takes care of all preferences possible in this
         * fragment(except a few that log on their own). It doesn't only include preferences in
         * network_setting_fragment.xml, but also those defined in GsmUmtsOptions and CdmaOptions.
         */
        private void sendMetricsEventPreferenceClicked(
                PreferenceScreen preferenceScreen, Preference preference) {
            final int category = getMetricsEventCategory(preferenceScreen, preference);
            if (category == MetricsEvent.VIEW_UNKNOWN) {
                return;
            }

            // Send MetricsEvent on click. It includes preferences other than SwitchPreferences,
            // which send MetricsEvent in onPreferenceChange.
            // For ListPreferences, we log it here without a value, only indicating it's clicked to
            // open the list dialog. When a value is chosen, another MetricsEvent is logged with
            // new value in onPreferenceChange.
            if (preference == mLteDataServicePref || preference == mDataUsagePref
                    || preference == mEuiccSettingsPref || preference == mAdvancedOptions
                    || preference == mWiFiCallingPref || preference == mButtonPreferredNetworkMode
                    || preference == mButtonEnabledNetworks
                    || preference == preferenceScreen.findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY)
                    || preference == preferenceScreen.findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY)
                    || preference == preferenceScreen.findPreference(BUTTON_GSM_APN_EXPAND_KEY)
                    || preference == preferenceScreen.findPreference(BUTTON_CDMA_APN_EXPAND_KEY)
                    || preference == preferenceScreen.findPreference(BUTTON_CARRIER_SETTINGS_KEY)) {
                MetricsLogger.action(getContext(), category);
            }
        }

        private void sendMetricsEventPreferenceChanged(
                PreferenceScreen preferenceScreen, Preference preference, Object newValue) {
            final int category = getMetricsEventCategory(preferenceScreen, preference);
            if (category == MetricsEvent.VIEW_UNKNOWN) {
                return;
            }

            // MetricsEvent logging with new value, for SwitchPreferences and ListPreferences.
            if (preference == mButton4glte || preference == mVideoCallingPref) {
                MetricsLogger.action(getContext(), category, (Boolean) newValue);
            } else if (preference == mButtonPreferredNetworkMode
                    || preference == mButtonEnabledNetworks
                    || preference == preferenceScreen
                            .findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY)
                    || preference == preferenceScreen
                            .findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY)) {
                // Network select preference sends metrics event in its own listener.
                MetricsLogger.action(getContext(), category, Integer.valueOf((String) newValue));
            }
        }

        private int getMetricsEventCategory(
                PreferenceScreen preferenceScreen, Preference preference) {

            if (preference == null) {
                return MetricsEvent.VIEW_UNKNOWN;
            } else if (preference == mMobileDataPref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_MOBILE_DATA_TOGGLE;
            } else if (preference == mButtonDataRoam) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_DATA_ROAMING_TOGGLE;
            } else if (preference == mDataUsagePref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_DATA_USAGE;
            } else if (preference == mLteDataServicePref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_SET_UP_DATA_SERVICE;
            } else if (preference == mAdvancedOptions) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_EXPAND_ADVANCED_FIELDS;
            } else if (preference == mButton4glte) {
                return MetricsEvent.ACTION_MOBILE_ENHANCED_4G_LTE_MODE_TOGGLE;
            } else if (preference == mButtonPreferredNetworkMode) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_SELECT_PREFERRED_NETWORK;
            } else if (preference == mButtonEnabledNetworks) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_SELECT_ENABLED_NETWORK;
            } else if (preference == mEuiccSettingsPref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_EUICC_SETTING;
            } else if (preference == mWiFiCallingPref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_WIFI_CALLING;
            } else if (preference == mVideoCallingPref) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_VIDEO_CALLING_TOGGLE;
            } else if (preference == preferenceScreen
                            .findPreference(NetworkOperators.BUTTON_AUTO_SELECT_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_AUTO_SELECT_NETWORK_TOGGLE;
            } else if (preference == preferenceScreen
                            .findPreference(NetworkOperators.BUTTON_NETWORK_SELECT_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_MANUAL_SELECT_NETWORK;
            } else if (preference == preferenceScreen
                            .findPreference(BUTTON_CDMA_SYSTEM_SELECT_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_CDMA_SYSTEM_SELECT;
            } else if (preference == preferenceScreen
                            .findPreference(BUTTON_CDMA_SUBSCRIPTION_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_CDMA_SUBSCRIPTION_SELECT;
            } else if (preference == preferenceScreen.findPreference(BUTTON_GSM_APN_EXPAND_KEY)
                    || preference == preferenceScreen.findPreference(BUTTON_CDMA_APN_EXPAND_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_APN_SETTINGS;
            } else if (preference == preferenceScreen.findPreference(BUTTON_CARRIER_SETTINGS_KEY)) {
                return MetricsEvent.ACTION_MOBILE_NETWORK_CARRIER_SETTINGS;
            } else {
                return MetricsEvent.VIEW_UNKNOWN;
            }
        }

        private void updateGsmUmtsOptions(PreferenceFragment prefFragment,
                PreferenceScreen prefScreen, final int subId, INetworkQueryService queryService) {
            // We don't want to re-create GsmUmtsOptions if already exists. Otherwise, the
            // preferences inside it will also be re-created which causes unexpected behavior.
            // For example, the open dialog gets dismissed or detached after pause / resume.
            if (mGsmUmtsOptions == null) {
                mGsmUmtsOptions = new GsmUmtsOptions(prefFragment, prefScreen, subId, queryService);
            } else {
                mGsmUmtsOptions.update(subId, queryService);
            }
        }

        private void updateCdmaOptions(PreferenceFragment prefFragment, PreferenceScreen prefScreen,
                Phone phone) {
            // We don't want to re-create CdmaOptions if already exists. Otherwise, the preferences
            // inside it will also be re-created which causes unexpected behavior. For example,
            // the open dialog gets dismissed or detached after pause / resume.
            if (mCdmaOptions == null) {
                mCdmaOptions = new CdmaOptions(prefFragment, prefScreen, phone);
            } else {
                mCdmaOptions.update(phone);
            }
        }

        private void dissmissDialog(ListPreference preference) {
            Dialog dialog = null;
            if (preference != null) {
                dialog = preference.getDialog();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }

        // -------------------- Mediatek ---------------------
        // M: Add for cmcc open market
        private MobileNetworkSettingsOmEx mOmEx;
        /// M: add for plmn list
        public static final String BUTTON_PLMN_LIST = "button_plmn_key";
        private static final String BUTTON_CDMA_ACTIVATE_DEVICE_KEY = "cdma_activate_device_key";
        private static final String BUTTON_SPRINT_ROAMING_SETTINGS = "sprint_roaming_settings";
        private static final String BUTTON_ENABLE_4_5G_SETTINGS = "enable_4_5g_settings";
        /// M: c2k 4g data only
        private static final String SINGLE_LTE_DATA = "single_lte_data";
        private static final String PROPERTY_MIMS_SUPPORT = "persist.vendor.mims_support";
        /// M: for screen rotate @{
        private static final String CURRENT_TAB = "current_tab";
        private int mCurrentTab = 0;
        /// @}
        private Preference mPLMNPreference;
        private IntentFilter mIntentFilter;
        private SwitchPreference mEnable4point5GPreference;

        /// M: 03100374 restore network mode in case set fail
        private int mPreNetworkMode = -1;
        private boolean mNetworkRegister = false;

        private Dialog mDialog;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DBG) {
                    log("action: " + action);
                }
                /// When receive aiplane mode, we would like to finish the activity, for
                //  we can't get the modem capability, and will show the user selected network
                //  mode as summary, this will make user misunderstand.(ALPS01971666)
                if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    getActivity().finish();
                } else if (action.equals(TelephonyIntents.ACTION_MSIM_MODE_CHANGED)
                        || action.equals(ModemSwitchHandler.ACTION_MD_TYPE_CHANGE)
                        || action.equals(TelephonyIntents.ACTION_LOCATED_PLMN_CHANGED)) {
                    updateScreenStatus();
                } else if (action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE)) {
                    if (DBG) {
                        log("Siwtch done Action ACTION_SET_PHONE_RAT_FAMILY_DONE received ");
                    }
                    mPhone = PhoneUtils.getPhoneUsingSubId(mPhone.getSubId());
                    updateScreenStatus();
                } else if (action.equals(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED)) {
                    // When the radio changes (ex: CDMA->GSM), refresh all options.
                    /// M: Add for handling phone type change. @{
                    dissmissDialog(mButtonPreferredNetworkMode);
                    dissmissDialog(mButtonEnabledNetworks);
                    if (mGsmUmtsOptions != null) {
                        mGsmUmtsOptions.onDestroy();
                    }
                    /// @}
                    mGsmUmtsOptions = null;
                    mCdmaOptions = null;
                    updateBody();
                /// M: Add for SIM Lock feature.
                } else if (action.equals(TelephonyIntents.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION)) {
                    handleSimLockStateChange();
                /// @}
                } else if (ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeDualVolteReceiveIntent(action)) {
                    if (null != mPhone) {
                        ExtensionManager.getMobileNetworkSettingsExt()
                                .customizeDataEnable(mPhone.getSubId(), mMobileDataPref);
                        updateScreenStatus();
                    }
                }
                /// @}
            }
        };

        /**
         * Add Preferences based on customer requirement to preference screen.
         * @param prefSet Preference screen that needs to be updated.
         */
        private void onCreateMTK(PreferenceScreen prefSet) {
            final Activity activity = getActivity();

            /// M: Add For [MTK_Enhanced4GLTE] @{
            addEnhanced4GLteSwitchPreference(prefSet);
            /// @}
            /// M: Add for plmn list @{
            if ((FeatureOption.isMtkCtaSet()
                    && !TelephonyUtilsEx.isCDMAPhone(mPhone))
                    /// M: [CT VOLTE]
                    && !(TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx.isCt4gSim(mPhone
                            .getSubId()))) {
                if (DBG) {
                    log("---addPLMNList---");
                }
                addPLMNList(prefSet);
            }
            /// M: [CT VOLTE Network UI]
            if (TelephonyUtilsEx.isCtVolteEnabled()
                    && TelephonyUtilsEx.isCt4gSim(mPhone.getSubId())) {
                if (mNetworkRegister) {
                    activity.getContentResolver().unregisterContentObserver(mNetworkObserver);
                }
                activity.getContentResolver().registerContentObserver(
                        Settings.Global.getUriFor(Settings.Global.PREFERRED_NETWORK_MODE +
                        mPhone.getSubId()),
                        true, mNetworkObserver);
                mNetworkRegister = true;
            }

            /// @}
            /// M: Add For C2K OM, OP09 will implement its own cdma network setting @{
            int mainPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;
            IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(
                                                    ServiceManager.getService("phoneEx"));
            if (iTelEx != null) {
                try {
                    mainPhoneId = iTelEx.getMainCapabilityPhoneId();
                } catch (RemoteException e) {
                    log("getMainCapabilityPhoneId: remote exception");
                }
            } else {
                log("IMtkTelephonyEx service not ready!");
                mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
            }
            if (FeatureOption.isMtkLteSupport()
                    && (isC2kLteSupport())
                     && ((TelephonyUtilsEx.isCdmaCardInserted(mPhone)
                        || TelephonyUtils.isCTLteTddTestSupport())
                        /// M:[CT VOLTE]
                        || (TelephonyUtilsEx.isCtVolteEnabled()
                                && TelephonyUtilsEx.isCt4gSim(mPhone.getSubId()) &&
                                (!TelephonyUtilsEx.isBothslotCt4gSim(mSubscriptionManager) ||
                                (mainPhoneId == mPhone.getPhoneId()))))
                    && !ExtensionManager.getMobileNetworkSettingsExt().isCtPlugin()) {
                if (mCdmaNetworkSettings != null) {
                    log("CdmaNetworkSettings destroy " + this);
                    mCdmaNetworkSettings.onDestroy();
                    mCdmaNetworkSettings = null;
                }
                mCdmaNetworkSettings = new CdmaNetworkSettings(activity, prefSet, mPhone);
                mCdmaNetworkSettings.onResume();
            } else if (mCdmaNetworkSettings != null) {
                log("onCreateMTK, destroy old CdmaNetworkSettings.");
                mCdmaNetworkSettings.onDestroy();
                mCdmaNetworkSettings = null;
            }
            /// @}

            if (null != mPhone) {
                ExtensionManager.getMobileNetworkSettingsExt()
                    .initOtherMobileNetworkSettings(getActivity(),
                            getPreferenceScreen(), mPhone.getSubId());
                ExtensionManager.getMobileNetworkSettingsExt()
                    .initOtherMobileNetworkSettings(getPreferenceScreen(), mPhone.getSubId());
            }
            /// Add for cmcc open market @{
            if (mActiveSubInfos.size() > 0) {
                mOmEx.initMobileNetworkSettings(getPreferenceScreen(), convertTabToSlot(mCurrentTab));
            }
            updateScreenStatus();
            /// @}
            /// M: for mtk 3m
            handleC2k3MScreen(prefSet);
            /// M: for mtk 4m
            handleC2k4MScreen(prefSet);
            /// M: for mtk 5m
            handleC2k5MScreen(prefSet);
        }

        /**
         * For [MTK_3SIM].
         * Convert Tab id to Slot id.
         * @param currentTab tab id
         * @return slotId
         */
        private int convertTabToSlot(int currentTab) {
            int slotId = mActiveSubInfos.size() > currentTab ?
                    mActiveSubInfos.get(currentTab).getSimSlotIndex() : 0;
            if (DBG) {
                log("convertTabToSlot: info size=" + mActiveSubInfos.size() +
                        " currentTab=" + currentTab + " slotId=" + slotId);
            }
            return slotId;
        }

        private void initIntentFilter() {
            /// M: for receivers sim lock gemini phone @{
            mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mIntentFilter.addAction(TelephonyIntents.ACTION_MSIM_MODE_CHANGED);
            mIntentFilter.addAction(ModemSwitchHandler.ACTION_MD_TYPE_CHANGE);
            mIntentFilter.addAction(TelephonyIntents.ACTION_LOCATED_PLMN_CHANGED);
            ///@}

            mIntentFilter.addAction(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);

            /// M: Add for Sim Switch @{
            mIntentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
            /// @}

            /// M: Add for SIM Lock feature.
            mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION);

            ExtensionManager.getMobileNetworkSettingsExt()
                    .customizeDualVolteIntentFilter(mIntentFilter);
        }

        /**
         * Is the phone has 3/4G capability or not.
         * @return true if phone has 3/4G capability
         */
        private boolean isCapabilityPhone(Phone phone) {
            boolean result = phone != null ? ((phone.getRadioAccessFamily()
                    & (RadioAccessFamily.RAF_UMTS | RadioAccessFamily.RAF_LTE)) > 0) : false;
            return result;
        }

        // M: Add for [MTK_Enhanced4GLTE] @{
        // Use our own button instand of Google default one mButton4glte
        private Enhanced4GLteSwitchPreference mEnhancedButton4glte;

        /**
         * Add our switchPreference & Remove google default one.
         * @param preferenceScreen
         */
        private void addEnhanced4GLteSwitchPreference(PreferenceScreen preferenceScreen) {
            final Activity activity = getActivity();
            int phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
            boolean isVolteEnabled = isVolteEnabled();
            log("[addEnhanced4GLteSwitchPreference] volteEnabled :" + isVolteEnabled);
            if (mButton4glte != null) {
                log("[addEnhanced4GLteSwitchPreference] Remove mButton4glte!");
                preferenceScreen.removePreference(mButton4glte);
            }
            boolean isCtPlugin = ExtensionManager.getMobileNetworkSettingsExt().isCtPlugin();
            log("[addEnhanced4GLteSwitchPreference] ss :" + isCtPlugin);
            if (isVolteEnabled && !isCtPlugin) {
                int order = mButtonEnabledNetworks.getOrder() + 1;
                mEnhancedButton4glte = new Enhanced4GLteSwitchPreference(activity, mPhone.getSubId());
                /// Still use Google's key, title, and summary.
                mEnhancedButton4glte.setKey(BUTTON_4G_LTE_KEY);
                /// M: [CT VOLTE]
                // show "VOLTE" for CT VOLTE SIM
                if (TelephonyUtilsEx.isCtVolteEnabled()
                        && TelephonyUtilsEx.isCtSim(mPhone.getSubId())) {
                    mEnhancedButton4glte.setTitle(R.string.hd_voice_switch_title);
                    mEnhancedButton4glte.setSummary(R.string.hd_voice_switch_summary);
                } else {
                    PersistableBundle carrierConfig =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
                    boolean useVariant4glteTitle = carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_ENHANCED_4G_LTE_TITLE_VARIANT_BOOL);
                    int enhanced4glteModeTitleId = useVariant4glteTitle ?
                            R.string.enhanced_4g_lte_mode_title_variant :
                            R.string.enhanced_4g_lte_mode_title;
                    mEnhancedButton4glte.setTitle(enhanced4glteModeTitleId);
                }
                /// M: [CT VOLTE]
                // show "VOLTE" for CT VOLTE SIM
                if (!TelephonyUtilsEx.isCtVolteEnabled()
                        || !TelephonyUtilsEx.isCtSim(mPhone.getSubId())) {
                /// @}
                    mEnhancedButton4glte.setSummary(R.string.enhanced_4g_lte_mode_summary);
                }
                mEnhancedButton4glte.setOnPreferenceChangeListener(this);
                mEnhancedButton4glte.setOrder(order);
                /// M: Customize the "Enhanced 4G LTE mode" setting. @{
                ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeEnhanced4GLteSwitchPreference(
                                preferenceScreen, mEnhancedButton4glte);
                /// @}
            } else {
                mEnhancedButton4glte = null;
            }
        }

        /**
         * Add for update the display of network mode preference.
         * @param enable is the preference or not
         */
        private void updateCapabilityRelatedPreference(boolean enable) {
            // if airplane mode is on or all SIMs closed, should also dismiss dialog
            boolean isNWModeEnabled = enable && isCapabilityPhone(mPhone);
            log("updateNetworkModePreference:isNWModeEnabled = " + isNWModeEnabled);

            /// M: Add for L+W DSDS.
            if (ExtensionManager.getMobileNetworkSettingsExt().isNetworkModeSettingNeeded()) {
                updateNetworkModePreference(mButtonPreferredNetworkMode, isNWModeEnabled);
                updateNetworkModePreference(mButtonEnabledNetworks, isNWModeEnabled);
                updateNetworkModeForLwDsds();
            }
            /// Add for [MTK_Enhanced4GLTE]
            updateEnhanced4GLteSwitchPreference();

            /// Update CDMA network settings
            if (mCdmaNetworkSettings != null) {
                mCdmaNetworkSettings.onResume();
            } else {
                log("updateCapabilityRelatedPreference don't update cdma settings");
            }
        }

        /**
         * Update the subId in mEnhancedButton4glte.
         */
        private void updateEnhanced4GLteSwitchPreference() {
            final Activity activity = getActivity();
            PersistableBundle carrierConfig =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            int phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
            if (mEnhancedButton4glte != null) {
                /// M: For Vodafone Turkey no need to show Volte Setting
                if (carrierConfig.getBoolean(
                            MtkCarrierConfigManager.MTK_KEY_SHOW_45G_OPTIONS)) {
                    if (findPreference(BUTTON_4G_LTE_KEY) != null) {
                        log("updateEnhanced4G removed for 4.5G feature");
                        getPreferenceScreen().removePreference(mEnhancedButton4glte);
                    }
                    return;
                }
                boolean showVolte = isVolteEnabled() &&
                        ((SystemProperties.getInt(PROPERTY_MIMS_SUPPORT, 1) == 1 &&
                        TelephonyUtilsEx.getMainPhoneId() == mPhone.getPhoneId()) ||
                        (SystemProperties.getInt(PROPERTY_MIMS_SUPPORT, 1) > 1 &&
                        isCapabilityPhone(mPhone)));
                if (ExtensionManager.getMobileNetworkSettingsExt().isEnhancedLTENeedToAdd(
                        showVolte, mPhone.getPhoneId())) {
                    if (findPreference(BUTTON_4G_LTE_KEY) == null) {
                        log("updateEnhanced4GLteSwitchPreference add switcher");
                        getPreferenceScreen().addPreference(mEnhancedButton4glte);
                    }
                } else {
                    if (findPreference(BUTTON_4G_LTE_KEY) != null) {
                        log("updateEnhanced4G removed");
                        getPreferenceScreen().removePreference(mEnhancedButton4glte);
                    }
                }
                if (findPreference(BUTTON_4G_LTE_KEY) != null) {
                    mEnhancedButton4glte.setSubId(mPhone.getSubId());
                    boolean enh4glteMode = MtkImsManager.isEnhanced4gLteModeSettingEnabledByUser(
                            activity, phoneId) &&
                            MtkImsManager.isNonTtyOrTtyOnVolteEnabled(activity, phoneId);
                    mEnhancedButton4glte.setChecked(enh4glteMode);
                    log("[updateEnhanced4GLteSwitchPreference] SubId = " + mPhone.getSubId()
                        + ", enh4glteMode=" + enh4glteMode);
                }
                /// M: update enabled state
                updateEnhanced4glteEnableState();
            }
        }

        private void updateEnhanced4glteEnableState() {
            if (mEnhancedButton4glte != null) {
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }

                boolean inCall = TelecomManager.from(activity).isInCall();
                boolean nontty = MtkImsManager.isNonTtyOrTtyOnVolteEnabled(activity.getApplicationContext(),
                        mPhone.getPhoneId());
                /// M: [CT VOLTE] @{
                boolean enableForCtVolte = true;
                int subId = mPhone.getSubId();
                boolean isCtSim = TelephonyUtilsEx.isCtSim(subId);

                if (TelephonyUtilsEx.isCtVolteEnabled() && isCtSim) {
                    int settingsNetworkMode = android.provider.Settings.Global.getInt(mPhone
                            .getContext().getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE + subId,
                            Phone.PREFERRED_NT_MODE);
                    enableForCtVolte = TelephonyUtilsEx.isCt4gSim(subId)
                            && (settingsNetworkMode == Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA
                            || settingsNetworkMode == Phone.NT_MODE_LTE_GSM_WCDMA
                            || settingsNetworkMode == Phone.NT_MODE_LTE_CDMA_AND_EVDO
                            || settingsNetworkMode == Phone.NT_MODE_LTE_ONLY
                            || settingsNetworkMode == Phone.NT_MODE_LTE_WCDMA);
                    if (TelephonyUtilsEx.isCtAutoVolteEnabled()) {
                        enableForCtVolte = true;
                    }
                }
                /// @}
                /// M: [CMCC DUAl VOLTE] @{
                enableForCtVolte = ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeDualVolteOpDisable(subId, enableForCtVolte);
                /// @}
                boolean simReady = true;
                if (!isCtSim) {
                    String numeric = TelephonyManager.getDefault().getSimOperator(subId);
                    simReady = (numeric.isEmpty() ? false : true);
                }
                boolean secondEnabled = ExtensionManager.getMobileNetworkSettingsExt().customizeDualCCcard(mPhone.getPhoneId());

                log("updateEnhanced4glteEnableState, incall = " + inCall + ", nontty = " + nontty
                        + ", enableForCtVolte = " + enableForCtVolte + ", simReady = " + simReady
                        + ", secondEnabled = " + secondEnabled);
                mEnhancedButton4glte.setEnabled(!inCall && nontty && hasActiveSubscriptions()
                        && enableForCtVolte && simReady && secondEnabled);
                /// M: [CMCC DUAl VOLTE] @{
                ExtensionManager.getMobileNetworkSettingsExt()
                        .customizeDualVolteOpHide(getPreferenceScreen(),
                                mEnhancedButton4glte, enableForCtVolte);
                /// @}
            }
    }
        /**
         * For [MTK_Enhanced4GLTE]
         * We add our own SwitchPreference, and its own onPreferenceChange call backs.
         * @param preference
         * @param objValue
         * @return
         */
        private boolean onPreferenceChangeMTK(Preference preference, Object objValue) {
            String volteTitle = getResources().getString(R.string.hd_voice_switch_title);
            String lteTitle = getResources().getString(R.string.enhanced_4g_lte_mode_title);
            log("[onPreferenceChangeMTK] Preference = " + preference.getTitle());

            if (((mEnhancedButton4glte == preference) || preference.getTitle().equals(volteTitle)
                    || preference.getTitle().equals(lteTitle))
                    && (preference instanceof Enhanced4GLteSwitchPreference)) {
                Enhanced4GLteSwitchPreference ltePref = (Enhanced4GLteSwitchPreference) preference;
                log("[onPreferenceChangeMTK] IsChecked = " + ltePref.isChecked());
                /// M: [CT VOLTE] @{
                if (TelephonyUtilsEx.isCtVolteEnabled() && TelephonyUtilsEx.isCtSim(
                        mPhone.getSubId())
                        && !ltePref.isChecked()) {
                    int type = TelephonyManager.getDefault().getNetworkType(mPhone.getSubId());
                    log("network type = " + type);
                    if (TelephonyManager.NETWORK_TYPE_LTE != type
                            && !TelephonyUtilsEx.isRoaming(mPhone)
                            && (TelephonyUtilsEx.getMainPhoneId() == mPhone.getPhoneId()
                            || TelephonyUtilsEx.isBothslotCt4gSim(mSubscriptionManager))) {
                        if (!TelephonyUtilsEx.isCtAutoVolteEnabled()) {
                            showVolteUnavailableDialog();
                            return false;
                        }
                    }
                }
                boolean isLtePrefChecked = !ltePref.isChecked();
                ltePref.setChecked(isLtePrefChecked);
                mEnhancedButton4glte.setChecked(isLtePrefChecked);
                log("[onPreferenceChangeMTK] IsChecked2 = " + isLtePrefChecked);
                log("[onPreferenceChangeMTK] mEnhancedButton4glte2 = " + mEnhancedButton4glte.isChecked());

                MtkImsManager.setEnhanced4gLteModeSetting(getActivity(), ltePref.isChecked(),
                        mPhone.getPhoneId());
                updateVideoCallState();
                return true;
            }
            return false;
        }
        /**
         * [CT VOLTE]When network type is not LTE, show dialog.
         */
        private void showVolteUnavailableDialog() {
            log("showVolteUnavailableDialog ...");
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String title = this.getString(R.string.alert_ct_volte_unavailable, PhoneUtils
                    .getSubDisplayName(mPhone.getSubId()));
            Dialog dialog = builder.setMessage(title).setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            log("dialog cancel mEnhanced4GLteSwitchPreference.setchecked  = "
                                    + !mEnhancedButton4glte.isChecked());
                            mEnhancedButton4glte.setChecked(false);

                        }
                    }).setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mEnhancedButton4glte.setChecked(true);
                    log("dialog ok" + " ims set " + mEnhancedButton4glte.isChecked() + " mSlotId = "
                            + SubscriptionManager.getPhoneId(mPhone.getSubId()));
                    MtkImsManager.setEnhanced4gLteModeSetting(getActivity(),
                            mEnhancedButton4glte.isChecked(), mPhone.getPhoneId());
                }
            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (KeyEvent.KEYCODE_BACK == keyCode) {
                        if (null != dialog) {
                            log("onKey keycode = back"
                                    + "dialog cancel mEnhanced4GLteSwitchPreference.setchecked  = "
                                    + !mEnhancedButton4glte.isChecked());
                            mEnhancedButton4glte.setChecked(false);
                            dialog.dismiss();
                            return true;
                        }
                    }
                    return false;
                }
            });
            mDialog = dialog;
            dialog.show();
        }

        private void addPLMNList(PreferenceScreen prefSet) {
            // add PLMNList, if c2k project the order should under the 4g data only
            // prize Modified by longzhongping, InCallUI, BUG 67844 , 2018.11.15-start
            /**
            int order = prefSet.findPreference(SINGLE_LTE_DATA) != null ?
                    prefSet.findPreference(SINGLE_LTE_DATA).getOrder() : mButtonDataRoam.getOrder();
             */
            int order = mDataUsagePref != null ?
                    mDataUsagePref.getOrder() : mButtonDataRoam.getOrder();
            // prize Modified by longzhongping, InCallUI, BUG 67844 , 2018.11.15-end
            mPLMNPreference = new Preference(getActivity());
            mPLMNPreference.setKey(MobileNetworkFragment.BUTTON_PLMN_LIST);
            mPLMNPreference.setTitle(R.string.plmn_list_setting_title);
            Intent intentPlmn = new Intent();
            intentPlmn.setClassName("com.android.phone",
                    "com.mediatek.settings.PLMNListPreference");
            intentPlmn.putExtra(SubscriptionInfoHelper.SUB_ID_EXTRA, mPhone.getSubId());
            mPLMNPreference.setIntent(intentPlmn);
            mPLMNPreference.setOrder(order + 1);
            prefSet.addPreference(mPLMNPreference);
        }

        private void updateScreenStatus() {
            final Activity activity = getActivity();
            if (activity == null) {
                log("updateScreenStatus, activity = null");
                return;
            }
            boolean isIdle = (TelephonyManager.getDefault().getCallState()
                    == TelephonyManager.CALL_STATE_IDLE);
            boolean isShouldEnabled = isIdle && TelephonyUtils.isRadioOn(mPhone.getSubId(), activity);
            if (DBG) {
                log("updateScreenStatus:isShouldEnabled = "
                    + isShouldEnabled + ", isIdle = " + isIdle);
            }
            getPreferenceScreen().setEnabled(isShouldEnabled);
            updateCapabilityRelatedPreference(isShouldEnabled);
        }

        /**
         * Whether support c2k LTE or not.
         * @return true if support else false.
         */
        private boolean isC2kLteSupport() {
            return FeatureOption.isMtkSrlteSupport()
                    || FeatureOption.isMtkSvlteSupport();
        }

        /**
         * Update the preferred network mode item Entries & Values.
         */
        private void updateNetworkModeForLwDsds() {
            /// Get main phone Id;
            /*ITelephonyEx iTelEx = ITelephonyEx.Stub.asInterface(
                    ServiceManager.getService(Context.TELEPHONY_SERVICE_EX));*/
            int mainPhoneId = getMainCapabilityPhoneId();
            /*if (iTelEx != null) {
                try{
                    mainPhoneId = getMainCapabilityPhoneId();
                } catch (RemoteException e) {
                    loge("handleLwDsdsNetworkMode get iTelEx error" + e.getMessage());
                }
            }*/
            /// If the phone main phone we should do nothing special;
            log("handleLwDsdsNetworkMode mainPhoneId = " + mainPhoneId);
            if (mainPhoneId != mPhone.getPhoneId()) {
                /// We should compare the user's setting value & modem support info;
                int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE
                        + mPhone.getSubId(), Phone.NT_MODE_GSM_ONLY);
                int currRat = mPhone.getRadioAccessFamily();
                log("updateNetworkModeForLwDsds settingsNetworkMode = "
                        + settingsNetworkMode + "; currRat = " + currRat);
                if ((currRat & RadioAccessFamily.RAF_LTE) == RadioAccessFamily.RAF_LTE) {
                    int select = mShow4GForLTE ? R.array.enabled_networks_4g_choices
                            : R.array.enabled_networks_choices;
                    mButtonEnabledNetworks.setEntries(select);
                    mButtonEnabledNetworks.setEntryValues(isC2kLteSupport() ?
                            R.array.enabled_networks_values_c2k : R.array.enabled_networks_values);
                    log("updateNetworkModeForLwDsds mShow4GForLTE = " + mShow4GForLTE);
					//prize add by liyuchong, change network type list as custom request ,20190322-begin
					condorNetworkMode();
					//prize add by liyuchong, change network type list as custom request ,20190322-end
                } else if ((currRat & RadioAccessFamily.RAF_UMTS) == RadioAccessFamily.RAF_UMTS) {
                    // Support 3/2G for WorldMode is uLWG
                    mButtonEnabledNetworks.setEntries(
                            R.array.enabled_networks_except_lte_choices);
                    if (isC2kLteSupport()) {
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_except_lte_values_c2k);
                    } else {
                        mButtonEnabledNetworks.setEntryValues(
                                R.array.enabled_networks_except_lte_values);
                    }
                    // If user select contain LTE, should set UI to 3G;
                    // NT_MODE_LTE_CDMA_AND_EVDO = 8 is the smallest value supporting LTE.
                    if (settingsNetworkMode > Phone.NT_MODE_LTE_CDMA_AND_EVDO) {
                        log("updateNetworkModeForLwDsds set network mode to 3G");
                        if (isC2kLteSupport()) {
                            mButtonEnabledNetworks.setValue(
                                    Integer.toString(Phone.NT_MODE_GLOBAL));
                        } else {
                            mButtonEnabledNetworks.setValue(
                                    Integer.toString(Phone.NT_MODE_WCDMA_PREF));
                        }
                        mButtonEnabledNetworks.setSummary(R.string.network_3G);
                    } else {
                        log("updateNetworkModeForLwDsds set to what user select. ");
                        UpdateEnabledNetworksValueAndSummary(settingsNetworkMode);
                    }
                } else {
                    // Only support 2G for WorldMode is uLtTG
                    log("updateNetworkModeForLwDsds set to 2G only.");
                    mButtonEnabledNetworks.setSummary(R.string.network_2G);
                    mButtonEnabledNetworks.setEnabled(false);
                }
            }
        }

        /**
         * Add for update the display of network mode preference.
         * @param enable is the preference or not
         */
        private void updateNetworkModePreference(ListPreference preference, boolean enable) {
            // if airplane mode is on or all SIMs closed, should also dismiss dialog
            if (preference != null) {
                preference.setEnabled(enable);
                if (!enable) {
                    dissmissDialog(preference);
                }
                if (getPreferenceScreen().findPreference(preference.getKey()) != null) {
                    updatePreferredNetworkUIFromDb();
                }
                /// Add for cmcc open market @{
                mOmEx.updateLTEModeStatus(preference);
                /// @}
            }
        }


        /**
         * For C2k Common screen, (3M, 5M).
         * @param preset
         */
        private void handleC2kCommonScreen(PreferenceScreen prefSet) {
            log("--- go to C2k Common (3M, 5M) screen ---");

            if (prefSet.findPreference(BUTTON_PREFERED_NETWORK_MODE) != null) {
                prefSet.removePreference(prefSet.findPreference(BUTTON_PREFERED_NETWORK_MODE));
            }
            if (TelephonyUtilsEx.isCDMAPhone(mPhone)) {
                if (prefSet.findPreference(BUTTON_ENABLED_NETWORKS_KEY) != null) {
                    prefSet.removePreference(prefSet.findPreference(BUTTON_ENABLED_NETWORKS_KEY));
                }
            }
        }

        /**
         * For C2k 3M.
         * @param preset
         */
        private void handleC2k3MScreen(PreferenceScreen prefSet) {
            if (!FeatureOption.isMtkLteSupport() && FeatureOption.isMtkC2k3MSupport()) {

                handleC2kCommonScreen(prefSet);
                log("--- go to C2k 3M ---");

                if (!TelephonyUtilsEx.isCDMAPhone(mPhone)) {
                    mButtonEnabledNetworks.setEntries(R.array.enabled_networks_except_lte_choices);
                    mButtonEnabledNetworks.setEntryValues(
                            R.array.enabled_networks_except_lte_values_c2k);
                }
            }
        }

        /**
         * For C2k OM 4M.
         * @param preset
         */
        private void handleC2k4MScreen(PreferenceScreen prefSet) {
            if (FeatureOption.isMtkLteSupport() && FeatureOption.isMtkC2k4MSupport()) {
                log("--- go to C2k 4M ---");

                if (PhoneConstants.PHONE_TYPE_GSM == mPhone.getPhoneType()) {
                    mButtonEnabledNetworks.setEntries(
                            R.array.enabled_networks_except_td_cdma_3g_choices);
                    mButtonEnabledNetworks.setEntryValues(
                            R.array.enabled_networks_except_td_cdma_3g_values);
                }
            }
        }

        /**
         * For C2k 5M.
         * Under 5M(CLLWG).
         * @param prefSet
         */
        private void handleC2k5MScreen(PreferenceScreen prefSet) {
            if (FeatureOption.isMtkLteSupport() && FeatureOption.isMtkC2k5MSupport()) {

                handleC2kCommonScreen(prefSet);
                log("--- go to c2k 5M ---");

                if (!TelephonyUtilsEx.isCDMAPhone(mPhone)) {
                    mButtonEnabledNetworks.setEntries(R.array.enabled_networks_4g_choices);
                    mButtonEnabledNetworks.setEntryValues(R.array.enabled_networks_values_c2k);
					//prize add by liyuchong, change network type list as custom request ,20190322-begin
					condorNetworkMode();
					//prize add by liyuchong, change network type list as custom request ,20190322-end
                }
            }
        }

        /**
         * Get main capability phone ID.
         *
         * @return Phone ID with main capability
         */
        public static int getMainCapabilityPhoneId() {
            int phoneId = 0;
            phoneId = SystemProperties.getInt(MtkPhoneConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1;
            //Log.d(LOG_TAG, "[RadioCapSwitchUtil] getMainCapabilityPhoneId " + phoneId);
            return phoneId;
        }

        /// M: if set fail, restore the preferred network type
        private void restorePreferredNetworkTypeIfNeeded(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null && mPreNetworkMode != -1 && mPhone != null) {
                final int phoneSubId = mPhone.getSubId();
                log("set failed, reset preferred network mode to " + mPreNetworkMode + ", sub id = "
                        + phoneSubId);
                android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                        mPreNetworkMode);
            }
            mPreNetworkMode = -1;
        }

        /// M: [CT VOLTE]
        private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                log("onChange...");
                updateEnhanced4GLteSwitchPreference();
            }
        };

        /// M: [CT VOLTE Network UI]
        private ContentObserver mNetworkObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                log("mNetworkObserver onChange...");
                updateBody();
            }
        };

        private boolean isVolteEnabled() {
            boolean isEnabled = false;

            if (mImsMgr != null) {
                isEnabled = mImsMgr.isVolteEnabledByPlatform();
            }
            log("isVolteEnabled = " + isEnabled);

            return isEnabled;
        }

        private boolean isWfcEnabled() {
            boolean isEnabled = false;

            if (mImsMgr != null) {
                isEnabled = mImsMgr.isWfcEnabledByPlatform();
            }

            log("isWfcEnabled = " + isEnabled);

            return isEnabled;
        }

        /// M: Add for SIM Lock feature. @{
        private boolean shouldEnableCellDataPrefForSimLock() {
            if (mPhone == null) {
                return true;
            }

            boolean enabledForSimLock = true;
            MtkTelephonyManagerEx tmEx = MtkTelephonyManagerEx.getDefault();
            int policy = tmEx.getSimLockPolicy();

            switch (policy) {
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_UNKNOWN:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_ONLY_SLOT1:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_ONLY_SLOT2:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_ALL_SLOTS_INDIVIDUAL:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_LK_SLOT1:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_LK_SLOT2:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_LK_SLOTA:
                case MtkIccCardConstants.SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_CS:
                    int simValid = tmEx.getShouldServiceCapability(mPhone.getPhoneId());
                    if (simValid != MtkIccCardConstants
                            .SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_FULL
                            && simValid != MtkIccCardConstants
                                    .SML_SLOT_LOCK_POLICY_SERVICE_CAPABILITY_PS_ONLY) {
                        enabledForSimLock = false;
                    }
                    break;

                default:
                    break;
            }

            if (!enabledForSimLock) {
                log("shouldEnableCellDataPrefForSimLock, policy=" + policy
                        + ", enabled=" + enabledForSimLock);
            }
            return enabledForSimLock;
        }

        private void handleSimLockStateChange() {
            if (mMobileDataPref != null) {
                boolean enabledForSimLock = shouldEnableCellDataPrefForSimLock();
                if (!enabledForSimLock) {
                    mMobileDataPref.setEnabled(false);
                }
            }
        }
        /// @}

        /**
         * Add Enable 4.5 Setting to UI.
         * @param prefSet current preference screen
         */
        private void add4point5GPreference(PreferenceScreen prefSet) {
            // Add disable 2G item when LTE is supported.
            mEnable4point5GPreference = (SwitchPreference)
                    prefSet.findPreference(BUTTON_ENABLE_4_5G_SETTINGS);
            if (mEnable4point5GPreference == null) {
                SwitchPreference buttonEnable4point5G = new SwitchPreference(
                        prefSet.getContext());
                int order = mButtonDataRoam.getOrder() + 1;
                if (mButtonPreferredNetworkMode != null) {
                    order = mButtonPreferredNetworkMode.getOrder();
                } else if (mButtonEnabledNetworks != null)  {
                    order = mButtonEnabledNetworks.getOrder();
                }
                mEnable4point5GPreference = buttonEnable4point5G;
                buttonEnable4point5G.setKey(BUTTON_ENABLE_4_5G_SETTINGS);
                buttonEnable4point5G.setTitle(R.string.enable_4_5g_switch);
                buttonEnable4point5G.setChecked(is4point5GSettingOn());
                buttonEnable4point5G.setOrder(order);
                prefSet.addPreference(buttonEnable4point5G);
                buttonEnable4point5G.setOnPreferenceChangeListener(this);
            }
        }


        /**
         * Check 4.5 Setting.
         * @return Check if Enable 4.5 Setting is ON or not
         */
        private boolean is4point5GSettingOn() {
            Activity activity = getActivity();
            int phoneId = SubscriptionManager.getPhoneId(mPhone.getSubId());
            boolean enh4glteMode = MtkImsManager.isEnhanced4gLteModeSettingEnabledByUser(activity,
                    phoneId) && MtkImsManager.isNonTtyOrTtyOnVolteEnabled(activity, phoneId);
            int phoneSubId = mPhone.getSubId();
            int settingsNetworkMode = android.provider.Settings.Global.getInt(
                            mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                            preferredNetworkMode);
            boolean isLteMode = false;
            if (settingsNetworkMode >= Phone.NT_MODE_LTE_CDMA_AND_EVDO) {
                isLteMode = true;
            }
            return enh4glteMode && isLteMode;
        }
    }
}

