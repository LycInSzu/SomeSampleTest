package com.mediatek.camera.feature.setting.videomute;

import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;

public class VideoMuteSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VideoMuteSettingView.class.getSimpleName());

    private OnVideoMuteClickListener mOnVideoMuteClickListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled = true;

    public VideoMuteSettingView(String key) {
        this.mKey = key;
    }

    public void setOnVideoMuteClickListener(OnVideoMuteClickListener muteClickListener) {
        mOnVideoMuteClickListener = muteClickListener;
    }

    public interface OnVideoMuteClickListener {
        void onVideoMuteClicked(boolean checked);
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.i("","");
        fragment.addPreferencesFromResource(R.xml.videomute_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.videomute_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.cameramute_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = (Boolean) newValue;
                mChecked = checked;
                mOnVideoMuteClickListener.onVideoMuteClicked(checked);
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
