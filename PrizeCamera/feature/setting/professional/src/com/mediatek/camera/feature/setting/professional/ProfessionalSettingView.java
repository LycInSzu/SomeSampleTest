package com.mediatek.camera.feature.setting.professional;

import android.app.Activity;
import android.preference.PreferenceFragment;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.R;

public class ProfessionalSettingView implements ICameraSettingView {

    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(ProfessionalSettingView.class.getSimpleName());
    private Preference mPreference;
    private Activity mActivity;
    private String mKey = null;
    private boolean mEnabled;



    public ProfessionalSettingView(Activity activity, String key) {
        mActivity = activity;
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG,"[loadView]");
    }

    @Override
    public void refreshView() {
        LogHelper.d(TAG,"[refreshView]");
        if (mPreference != null) {
            mPreference.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {

    }

    @Override
    public void setEnabled(boolean enable) {
        LogHelper.d(TAG,"[enable]");
        mEnabled = enable;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }
}
