package com.mediatek.camera.feature.setting.cameramute;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.ui.prize.PrizeCameraSettingView;

public class CameraMuteSettingView extends PrizeCameraSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraMuteSettingView.class.getSimpleName());

    private OnCameraMuteClickListener mCameraMuteClickListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled = true;

    public CameraMuteSettingView(String key) {
        this.mKey = key;
    }

    public void setCameraMuteClickListener(OnCameraMuteClickListener muteClickListener) {
        mCameraMuteClickListener = muteClickListener;
    }

    public interface OnCameraMuteClickListener{
        void onCameraMuteClicked(boolean checked);
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.i("","");
        fragment.addPreferencesFromResource(R.xml.cameramute_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.cameramute_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.cameramute_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = (Boolean) newValue;
                mChecked = checked;
                mCameraMuteClickListener.onCameraMuteClicked(checked);
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

    public int[] getIcons() {
        return new int[]{
                R.drawable.prize_setting_mute_on,
                R.drawable.prize_setting_mute_off,
        };
    }

    public String getValue() {
        return mChecked ? VALUES_ON : VALUES_OFF;
    }

    public int getTitle() {
        return R.string.pref_camera_cameramute_title;
    }

    public void onValueChanged(String newValue){
        mChecked = VALUES_ON.equals(newValue);
        mCameraMuteClickListener.onCameraMuteClicked(mChecked);
    }

    public int getOrder(){
        return 53;
    }
}
