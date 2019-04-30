package com.mediatek.camera.common.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.PrizeSettingDialog;

public class PrizeListPreference extends ListPreference {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PrizeListPreference.class.getSimpleName());

    private TextView mTvSetting;
    private PrizeSettingDialog mPrizeSettingDialog;

    public PrizeListPreference(Context context) {
        super(context);
    }

    public PrizeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mTvSetting = view.findViewById(R.id.text_setting);
        if (mTvSetting != null) {
            mTvSetting.setText(getValue());
        }


    }

    @Override
    protected void onClick() {
        //Do nothing
    }


}
