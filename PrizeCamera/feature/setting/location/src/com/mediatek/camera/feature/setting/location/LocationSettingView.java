package com.mediatek.camera.feature.setting.location;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;

public class LocationSettingView implements ICameraSettingView {
    private OnLocationClickListener mOnLocationClickListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled = true;

    public LocationSettingView(String key) {
        mKey = key;
    }

    public void setOnLocationClickListener(OnLocationClickListener onLocationClickListener) {
        mOnLocationClickListener = onLocationClickListener;
    }

    public interface OnLocationClickListener{
        void onLocationClicked(boolean checked);
    }


    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.i("","");
        fragment.addPreferencesFromResource(R.xml.location_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.location_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.location_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = (Boolean) newValue;
                mChecked = checked;
                mOnLocationClickListener.onLocationClicked(checked);
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

    }

    @Override
    public void setEnabled(boolean enable) {
        mEnabled = enable;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
