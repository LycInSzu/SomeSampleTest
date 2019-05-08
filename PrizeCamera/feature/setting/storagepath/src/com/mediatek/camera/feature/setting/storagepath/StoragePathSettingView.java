package com.mediatek.camera.feature.setting.storagepath;

import android.app.Activity;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.preference.PrizeListPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.widget.PrizeSettingDialog;
import com.mediatek.camera.prize.PrizeLifeCycle;
import com.mediatek.camera.ui.prize.PrizeCameraSettingView;

import java.util.ArrayList;
import java.util.List;
public class StoragePathSettingView extends PrizeCameraSettingView implements ICameraSettingView,StoragePathSelector.OnItemClickListener,PrizeLifeCycle {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(StoragePathSettingView.class.getSimpleName());

    private Activity mActivity;
    private Preference mPref;
    private String mKey;
    private String mSelectedValue;
    private OnValueChangeListener mOnValueChangeListener;
    private List<String> mOriginalEntries = new ArrayList<>();
    private List<String> mOriginalEntryValues = new ArrayList<>();
    private List<String> mEntries = new ArrayList<>();
    private List<String> mEntryValues = new ArrayList<>();
    private String mSummary;
    private StoragePathSelector mSelector;
    private boolean mEnabled;
    private PrizeSettingDialog mPrizeSettingDialog;
    @Override
    public void onItemClick(String value) {
        setValue(value);
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChanged(value);
        }
    }

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

    public StoragePathSettingView (String key, Activity activity) {
        mActivity = activity;
        mKey = key;
        String[] originalEntriesInArray = mActivity.getResources()
                .getStringArray(R.array.storage_path_entries);
        String[] originalEntryValuesInArray = mActivity.getResources()
                .getStringArray(R.array.storage_path_entryvalues);

        for (String value : originalEntriesInArray) {
            mOriginalEntries.add(value);
        }
        for (String value : originalEntryValuesInArray) {
            mOriginalEntryValues.add(value);
        }
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG, "[loadView]");
        fragment.addPreferencesFromResource(R.xml.storagepath_preference);
        mPref = (Preference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.storagepath_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.storagepath_content_description));
        mPref.setSummary(mSummary);
        mPref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(android.preference.Preference preference) {
                        /*prize-change-huangpengfei-2018-10-25-start*/
                        LogHelper.d(TAG, "[onPreferenceClick]");
                        if (mPrizeSettingDialog == null){
                            mPrizeSettingDialog = new PrizeSettingDialog(mActivity, mPref.getTitle());
                            mPrizeSettingDialog.init();
                            mPrizeSettingDialog.show();
                            mPrizeSettingDialog.setSelectValue(mSummary);
                            mPrizeSettingDialog.initData(mEntries);
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
                            mPrizeSettingDialog.setSelectValue(mSummary);
                            mPrizeSettingDialog.initData(mEntries);
                            mPrizeSettingDialog.show();
                        }
                        return true;
                    }
                });
        mPref.setEnabled(mEnabled);
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

    }

    @Override
    public void setEnabled(boolean enable) {
        mEnabled = enable;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

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

    private static final int[] mIcons = new int[]{
        R.drawable.prize_selector_storage_phone,
        R.drawable.prize_selector_storage_sdcard,
    };

    public int[] getIcons() {
        return mIcons;
    }

    @Override
    public List<String> getEntryValues() {
        return mEntryValues;
    }

    public void setContext(Activity activity){
        mActivity = activity;
    }

    @Override
    public List<String> getEntrys() {
        return mEntries;
    }

    public String getValue() {
        return mSelectedValue;
    }

    public int getTitle() {
        return R.string.pref_camera_storagepath_title;
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
        return 100;
    }
}
