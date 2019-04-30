package com.mediatek.camera.feature.setting.storagepath;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.TextView;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.PrizePreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class StoragePathSelector extends PrizePreferenceFragment {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(StoragePathSelector.class.getSimpleName());

    private List<String> mEntries = new ArrayList<>();
    private List<String> mEntryValues = new ArrayList<>();
    private String mSelectedValue = null;
    private Preference.OnPreferenceClickListener mOnPreferenceClickListener
            = new StoragePathSelector.StoragePathPreferenceClickListener();
    private OnItemClickListener mListener;

    /**
     * Listener to listen the item clicked.
     */
    public interface OnItemClickListener {
        /**
         * Callback when item clicked.
         *
         * @param value The picture size clicked.
         */
        void onItemClick(String value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
        //prize-tangan-add prize camera-begin
        /*Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getActivity().getResources().getString(R.string.iso_title));
        }*/
        TextView tvTitle = (TextView) getActivity().findViewById(R.id.prize_setting_title);
        tvTitle.setText(R.string.pref_camera_storagepath_title);
        //prize-tangan-add prize camera-end
        addPreferencesFromResource(R.xml.storagepath_selector_preference);
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0 ; i < mEntryValues.size(); i++) {
            com.mediatek.camera.feature.setting.storagepath.RadioPreference preference = new RadioPreference(getActivity());
            if (mEntryValues.get(i).equals(mSelectedValue)) {
                preference.setChecked(true);
            }
            preference.setTitle(mEntries.get(i));
            preference.setOnPreferenceClickListener(mOnPreferenceClickListener);
            screen.addPreference(preference);
        }
    }

    /**
     * Self timer preference click listener.
     */
    private class StoragePathPreferenceClickListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String title = (String) preference.getTitle();
            int index = mEntries.indexOf(title);
            String value = mEntryValues.get(index);
            mListener.onItemClick(value);

            mSelectedValue = value;
            getActivity().getFragmentManager().popBackStack();
            return true;
        }
    }

    public void setEntriesAndEntryValues(List<String> entries, List<String> entryValues) {
        mEntries = entries;
        mEntryValues = entryValues;
    }

    /**
     * Set listener to listen item clicked.
     *
     * @param listener The instance of {@link OnItemClickListener}.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Set the default selected value.
     *
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the picture sizes supported.
     *
     * @param entryValues The picture sizes supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues.clear();
        mEntryValues.addAll(entryValues);
    }




}
