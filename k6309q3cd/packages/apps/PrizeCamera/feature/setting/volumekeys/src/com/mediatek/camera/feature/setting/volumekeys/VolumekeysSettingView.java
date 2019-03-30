package com.mediatek.camera.feature.setting.volumekeys;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.PrizeListPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.R;
import com.mediatek.camera.common.widget.PrizeSettingDialog;
import com.mediatek.camera.prize.PrizeLifeCycle;


public class VolumekeysSettingView implements ICameraSettingView,PrizeLifeCycle {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VolumekeysSettingView.class.getSimpleName());
    private final String mKey;
    private PrizeListPreference mPref;
    private boolean mEnabled;
    private String mValue;
    private Activity mContext;
    private PrizeSettingDialog mPrizeSettingDialog;

    public VolumekeysSettingView(String key) {
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG, "[loadView]");
        mContext = fragment.getActivity();
        fragment.addPreferencesFromResource(R.xml.volumekeys_preference);
        mPref = (PrizeListPreference) fragment.findPreference(mKey);
        mPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CharSequence[] entryValues = mPref.getEntryValues();
                if (mPrizeSettingDialog == null){
                    mPrizeSettingDialog = new PrizeSettingDialog(mContext, mPref.getTitle());
                    mPrizeSettingDialog.init();
                    mPrizeSettingDialog.show();
                    mPrizeSettingDialog.initData(mPref.getEntries());
                    mPrizeSettingDialog.setOnItemClickListener(new PrizeSettingDialog.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            String entry = entryValues[position].toString();
                            setValue(entry);
                            refreshView();
                            if (mOnDataChangeListener != null){
                                mOnDataChangeListener.onDataChange(entry);
                            }
                        }
                    });
                }else {
                    mPrizeSettingDialog.show();
                }
                return true;
            }
        });
        fragment.getPreferenceScreen().addPreference(mPref);
    }

    @Override
    public void refreshView() {
        if (mPref != null){
            CharSequence[] entryValues = mPref.getEntryValues();
            CharSequence[] enties = mPref.getEntries();
            for (int i = 0; i < entryValues.length; i++){
                if (entryValues[i].equals(mValue)){
                    mPref.setValue(enties[i].toString());
                }
            }
        }
    }

    @Override
    public void unloadView() {

    }

    private OnDataChangeListener mOnDataChangeListener;

    public void setValue(String value) {
        mValue = value;
    }

    @Override
    public void onPause() {
        if (mPrizeSettingDialog != null){
            mPrizeSettingDialog.dismiss();
        }
    }

    public interface OnDataChangeListener{
        void onDataChange(String value);
    }

    public void setOnDataChangeListener(OnDataChangeListener onItemClickListener){
        this.mOnDataChangeListener = onItemClickListener;
    }

    @Override
    public void setEnabled(boolean enable) {
        mEnabled = enable;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

}
