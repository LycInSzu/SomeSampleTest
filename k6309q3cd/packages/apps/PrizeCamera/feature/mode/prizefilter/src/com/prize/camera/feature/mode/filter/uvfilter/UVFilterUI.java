package com.prize.camera.feature.mode.filter.uvfilter;

import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mediatek.camera.R;
import com.android.camera.uvfilter.UVFilterLinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abel on 2018/5/30/0030.
 */

public class UVFilterUI implements View.OnClickListener {

    private static final String TAG = "UVFilterUI";

    private int UVFILTER_SMALL_PREVIEW_HEIHGT  = 120;
    private int UVFILTER_SMALL_PREVIEW_WIDTH   = 120;

    private UVFilterLinearLayout mUVFilterLinearLayoutSub = null;

    private List<SurfaceView> mSmlPrvwList = new ArrayList<>();
    private List<Integer> mSmlPrvwIds = new ArrayList<>(Arrays.asList(
            R.id.sv_sml_prvw_0, R.id.sv_sml_prvw_1, R.id.sv_sml_prvw_2,
            R.id.sv_sml_prvw_3, R.id.sv_sml_prvw_4, R.id.sv_sml_prvw_5,
            R.id.sv_sml_prvw_6, R.id.sv_sml_prvw_7, R.id.sv_sml_prvw_8
    ));

    public int getWidth() {
        return UVFILTER_SMALL_PREVIEW_WIDTH;
    }

    public int getHeight() {
        return UVFILTER_SMALL_PREVIEW_HEIHGT;
    }

    public List<SurfaceView> initUVFilterSubItem(ViewGroup rootView, int width, int height) {
        mUVFilterLinearLayoutSub  = (UVFilterLinearLayout)rootView.findViewById(R.id.uvfilter_sub_ll);
        UVFILTER_SMALL_PREVIEW_HEIHGT = height;
        UVFILTER_SMALL_PREVIEW_WIDTH = width;
        RelativeLayout.LayoutParams sllParas = (RelativeLayout.LayoutParams)mUVFilterLinearLayoutSub.getLayoutParams();
        sllParas.width  = ViewGroup.LayoutParams.WRAP_CONTENT;
        sllParas.height = UVFILTER_SMALL_PREVIEW_HEIHGT;
        sllParas.bottomMargin = rootView.getContext().getResources().getDimensionPixelSize(R.dimen.blur_layout_marginbottom);
        //sllParas.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        mUVFilterLinearLayoutSub.setLayoutParams(sllParas);

        for (int i = 0; i < mSmlPrvwIds.size(); i++) {
            SurfaceView msp = (SurfaceView)rootView.findViewById(mSmlPrvwIds.get(i));
            //msp.setOnClickListener(this);

            LinearLayout.LayoutParams mspParas = (LinearLayout.LayoutParams)msp.getLayoutParams();
            mspParas.width  = UVFILTER_SMALL_PREVIEW_WIDTH;
            mspParas.height = UVFILTER_SMALL_PREVIEW_HEIHGT;
            msp.setLayoutParams(mspParas);

            mSmlPrvwList.add(msp);
        }

        return mSmlPrvwList;
    }

    @Override
    public void onClick(View v) {

    }
}
