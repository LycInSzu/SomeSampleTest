package com.mediatek.camera.common.widget;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import com.mediatek.camera.R;

public class PrizePreferenceFragment extends PreferenceFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = (ListView) view.findViewById(android.R.id.list);
        if (lv != null) {
            Drawable divider = getActivity().getDrawable(R.drawable.prize_setting_list_divider);
            if (divider != null) {
                lv.setDivider(divider);
            }
        }
    }
}
