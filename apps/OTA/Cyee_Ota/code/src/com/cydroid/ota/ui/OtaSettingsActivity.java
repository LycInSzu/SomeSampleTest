package com.cydroid.ota.ui;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import java.util.HashMap;
import java.util.Map;

import cyee.app.CyeeActionBar;
import cyee.changecolors.ChameleonColorManager;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreferenceActivity;
import cyee.preference.CyeePreferenceGroup;
import cyee.preference.CyeeSwitchPreference;
import com.cydroid.ota.R;

public class OtaSettingsActivity extends CyeePreferenceActivity {
    private static final String TAG = "OtaSettingsActivity";
    private CyeePreference mUpgradeFromLocalView;
    private QuestionnairePreference mFeedbackView;
    private CyeePreference mImprovingInfoView;
    private CyeeSwitchPreference mAutoUpgradeSettingsView;
    private CyeePreferenceGroup mGroup = null;
    protected DialogFragment mCurrentDialogFragment = null;
    private static final String KEY_SETTING_SCREEN = "setting_screen";
    private static final String KEY_UPGRADE_FROM_LOCAL = "upgrade_from_local";
    private static final String KEY_IMPROVE_INFO = "version_improving_information";
    private static final String KEY_AUTO_CHECK_SETTINGS = "auto_check_settings";
    private static final String KEY_FEEDBACK_INFORMATION = "feedback_information";
    private IStorage mSettingStorage;
    private Context mContext;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        SettingUpdateApplication application = (SettingUpdateApplication) getApplication();
        if (!application.getSystemTheme().isNeedChangeColor()) {
            setTheme(R.style.Theme_Light_NormalTheme);
            if (SystemPropertiesUtils.getBlueStyle()) {
                setTheme(R.style.Theme_Light_BlueTheme);
            }
        }
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        mContext = this;
        CyeeActionBar actionBar = getCyeeActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_HOME_AS_UP
                    | CyeeActionBar.DISPLAY_SHOW_TITLE
                    | CyeeActionBar.DISPLAY_SHOW_HOME);
            actionBar.setTitle(getString(R.string.gn_su_action_bar_settings_title));
        } else {
            Log.d(TAG, "actionBar is null!");
        }
        initSettingPreference();
    }

    @Override
    protected final void onResume() {
        super.onResume();
        updateFeedbackItems();
        updateFeedbackView();
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    private void updateFeedbackView(){

        IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                .questionnaire(mContext).getQuestionnaireInfo();
        boolean isRead = SettingUpdateDataInvoker.getInstance(this).settingStorage()
                .getBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ, false);
        if(questionnaireInfo != null && questionnaireInfo.getStatus() == 0
                && !isRead){
            mFeedbackView.setBadgeViewVisibility(true);
        }else{
            mFeedbackView.setBadgeViewVisibility(false);
        }
    }

    @Override
    protected final void onPause() {
        super.onPause();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSettingPreference() {
        addPreferencesFromResource(R.xml.ota_settings_preference);
        mGroup = (CyeePreferenceGroup) findPreference(KEY_SETTING_SCREEN);
        mUpgradeFromLocalView = (CyeePreference) findPreference(
                KEY_UPGRADE_FROM_LOCAL);
        mUpgradeFromLocalView.setOnPreferenceClickListener(mClickListener);
        mImprovingInfoView = (CyeePreference) findPreference(KEY_IMPROVE_INFO);
        mImprovingInfoView.setOnPreferenceClickListener(mClickListener);
        mAutoUpgradeSettingsView = (CyeeSwitchPreference) findPreference(
                KEY_AUTO_CHECK_SETTINGS);
        mAutoUpgradeSettingsView.setOnPreferenceChangeListener(mChangeListener);
		//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 begin
        if (SystemPropertiesUtils.isDPFlag()) {
            mAutoUpgradeSettingsView.setSummary(R.string.gn_su_string_settings_menu_setting_summary_dp);
        }
		//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 end
        mFeedbackView = (QuestionnairePreference) findPreference(
                KEY_FEEDBACK_INFORMATION);
        mFeedbackView.setOnPreferenceClickListener(mClickListener);
        init();
    }

    private void init() {
        mSettingStorage = SettingUpdateDataInvoker.getInstance(
                mContext).settingStorage();
        String otaReleaseNote = mSettingStorage.getString(
                Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASENOTE, "");
        String otaReleaseNoteUrl = mSettingStorage.getString(
                Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASEURL, "");
        boolean haveNotOtaReleaseNote = otaReleaseNote.equals("null")
                || otaReleaseNote.equals("");
        boolean haveNotOtaReleaseNoteUrl = "null".equals(otaReleaseNoteUrl)
                || "".equals(otaReleaseNoteUrl);
        if (haveNotOtaReleaseNote && haveNotOtaReleaseNoteUrl) {
            mGroup.removePreference(mImprovingInfoView);
        }

        IStorage wlanAutoStorage = SettingUpdateDataInvoker
                .getInstance(mContext).wlanAutoStorage();

        //Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
		if (wlanAutoStorage
                .getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, mContext.getResources().getBoolean(
                        R.bool.auto_download_only_wlan)) && !mAutoUpgradeSettingsView.isChecked()) {
            mAutoUpgradeSettingsView.setChecked(true);
        }
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
    }

    private void doImproveInfo() {
        if (NetworkUtils.isMobileNetwork(mContext) && !mSettingStorage
                .getBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, false)) {
            showMobileNetworkAllowDialog();
            mSettingStorage.registerOnkeyChangeListener(mImproveListener);
            return;
        }
        Intent detailIntent = new Intent();
        detailIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        detailIntent.setClass(mContext, DetailsInfoActivity.class);
        detailIntent.putExtra(DetailsInfoActivity.IS_CURRENT_VERSION_INFO,
                true);
        detailIntent.putExtra(DetailsInfoActivity.MROE_INFO_URL,
                mSettingStorage.getString(
                        Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASEURL,
                        ""));
        detailIntent.putExtra(DetailsInfoActivity.INTERNAL_VERSION,
                SystemPropertiesUtils.getInternalVersion());
        startActivity(detailIntent);
    }

    private void dismissFragementDialog() {
        synchronized (DialogFactory.class) {
            if (mCurrentDialogFragment != null) {
                mCurrentDialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    private void dialogShow(int id) {
        if (mCurrentDialogFragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(mCurrentDialogFragment, "dailog_" + id);
            ft.commitAllowingStateLoss();
        }
    }

    private void doUpgradeFromLocal() {
        synchronized (DialogFactory.class) {
            dismissFragementDialog();
            mCurrentDialogFragment = DialogFactory
                    .newInstance(R.id.DIALOG_ID_UPGRADE_LOCALSELECTIONS);
            dialogShow(R.id.DIALOG_ID_UPGRADE_LOCALSELECTIONS);
        }
    }

    private void doFeedbackInfomation() {
        IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                .questionnaire(mContext).getQuestionnaireInfo();
        if (questionnaireInfo == null || TextUtils.isEmpty(
                questionnaireInfo.getQuestionnaireUrl())) {
            return;
        }
        Intent questionnaireIntent = new Intent();
        questionnaireIntent.setClass(mContext,
                QuestionnaireActivity.class);
        startActivity(questionnaireIntent);
    }

    private void updateFeedbackItems() {
        IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                .questionnaire(mContext).getQuestionnaireInfo();
        if (questionnaireInfo == null || TextUtils.isEmpty(
                questionnaireInfo.getQuestionnaireUrl())) {
            mGroup.removePreference(mFeedbackView);
            return;
        }
    }

    private boolean isCanUseNet() {
        if (NetworkUtils.isWIFIConnection(mContext)) {
            return true;
        }
        if (NetworkUtils.isMobileNetwork(mContext)) {
            boolean enable = mSettingStorage.getBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, false);
            if (enable) {
                return true;
            } else {
                Log.d(TAG, "false");
                showMobileNetworkAllowDialog();
                return false;
            }
        }

        showNetworkError();

        return false;
    }

    private void showMobileNetworkAllowDialog() {
        synchronized (DialogFactory.class) {
            dismissFragementDialog();
            mCurrentDialogFragment = DialogFactory
                    .newInstance(R.id.DIALOG_ID_MOBILE_PERMISSION);
            dialogShow(R.id.DIALOG_ID_MOBILE_PERMISSION);
        }
    }

    private void showNetworkError() {
        Toast.makeText(mContext, R.string.gn_su_layout_main_expend_text_network_error,
                Toast.LENGTH_SHORT).show();
    }

    private CyeePreference.OnPreferenceClickListener mClickListener = new CyeePreference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(CyeePreference arg0) {
            final String key = arg0.getKey();
            Log.d(TAG, "onPreferenceClick key = " + key);
            if (KEY_UPGRADE_FROM_LOCAL.equals(key)) {
                doUpgradeFromLocal();
            } else if (KEY_IMPROVE_INFO.equals(key)) {
                doImproveInfo();
            } else if (KEY_FEEDBACK_INFORMATION.equals(key)) {
                if (isCanUseNet()) {
                    doFeedbackInfomation();
                } else {
                    mSettingStorage.registerOnkeyChangeListener(mFeedbackListener);
                }
            }
            return false;
        }

    };

    private IStorage.OnKeyChangeListener mImproveListener = new IStorage.OnKeyChangeListener() {
        @Override
        public void onKeyChange(String key) {
            if (Key.Setting.KEY_MOBILE_NET_ENABLE.equals(key)) {
                if (mSettingStorage.getBoolean(key, false)) {
                    doImproveInfo();
                }
                mSettingStorage.unregisterOnkeyChangeListener(this);
            }
        }
    };

    private IStorage.OnKeyChangeListener mFeedbackListener = new IStorage.OnKeyChangeListener() {
        @Override
        public void onKeyChange(String key) {
            if (Key.Setting.KEY_MOBILE_NET_ENABLE.equals(key)) {
                if (mSettingStorage.getBoolean(key, false)) {
                    doFeedbackInfomation();
                }
                mSettingStorage.unregisterOnkeyChangeListener(this);
            }
        }
    };

    private CyeePreference.OnPreferenceChangeListener mChangeListener = new CyeePreference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(CyeePreference cyeePreference,
                                          Object obj) {
            Log.d(TAG, "onPreferenceChange==>" + obj);
            IStorage wlanAutoStorage = SettingUpdateDataInvoker
                    .getInstance(mContext)
                    .wlanAutoStorage();
            Boolean isChecked = (Boolean) obj;
            wlanAutoStorage
                    .putBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH,
                            isChecked);
            return true;
        }
    };
}
