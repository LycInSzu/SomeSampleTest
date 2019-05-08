package com.mediatek.camera.feature.setting.grid;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.ui.prize.PrizeCameraSettingView;

public class GridSettingView extends PrizeCameraSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(GridSettingView.class.getSimpleName());

    private OnGridClickListener mListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled = true;

    public GridSettingView(String key) {
        this.mKey = key;
    }

    public void setGridOnClickListener(OnGridClickListener listener) {
        this.mListener = listener;
    }

    /**
     * Listener to listen zsd is clicked.
     */
    public interface OnGridClickListener {
        /**
         * Callback when zsd item is clicked by user.
         *
         * @param checked True means zsd is opened, false means zsd is closed.
         */
        void onGridClicked(boolean checked);
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        LogHelper.d(TAG, "[loadView]");
        fragment.addPreferencesFromResource(R.xml.grid_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.grid_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.grid_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean checked = (Boolean) o;
                mChecked = checked;
                mListener.onGridClicked(checked);
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




    private static final int ICONS[] = new int[]{
            R.drawable.prize_setting_grid_on,
            R.drawable.prize_setting_grid_off,
    };

    public int[] getIcons() {
        return ICONS;
    }

    public String getValue() {
        return mChecked ? VALUES_ON : VALUES_OFF;
    }

    public int getTitle() {
        return R.string.pref_camera_grid_title;
    }

    public void onValueChanged(String newValue){
        mChecked = VALUES_ON.equals(newValue);
        mListener.onGridClicked(mChecked);
    }

    public int getOrder(){
        return 65;
    }
}
