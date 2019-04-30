package com.mediatek.camera.feature.setting.professional;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.widget.PrizePreferenceFragment;

import java.util.List;


public class PrizeProfessionalListPreferenceFragment extends PrizePreferenceFragment {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PrizeProfessionalListPreferenceFragment.class.getSimpleName());
    private List<ICameraSettingView> mEntries;
    private TextView mTvTitle;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTvTitle = (TextView) getActivity().findViewById(R.id.prize_setting_title);
        if (mTvTitle != null) {
            mTvTitle.setText(R.string.pref_camera_professional_title);
        }
        addPreferencesFromResource(R.xml.professional_list_preference);

        if (mEntries != null && mEntries.size()>= 1) {
            synchronized (this) {
                for (ICameraSettingView view : mEntries) {
                    view.loadView(this);
                }
            }
        }
    }

    public void setEntryView(List<ICameraSettingView> entries) {
        this.mEntries = entries;

    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.i(TAG, "[onResume]");
        if (mTvTitle != null) {
            mTvTitle.setText(R.string.pref_camera_professional_title);
        }
        if (mEntries != null && mEntries.size()>= 1) {
            for (ICameraSettingView view : mEntries) {
                LogHelper.i(TAG, "[onResume]  " + view.getClass().getSimpleName() + "  enable = " + view.isEnabled());
                view.refreshView();
            }
        }
    }

}
