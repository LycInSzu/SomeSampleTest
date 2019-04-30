package com.mediatek.camera.ui.photo;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.mode.beauty.beautyui.IBeautyUi;

public class FaceBeautyView implements IBeautyUi,View.OnClickListener {
    private View mView;
    private Activity mActivity;
    private ViewGroup mParentViewGroup;
    private TextView mTextView;
    private View focuseView;
    private RelativeLayout beautiful_layout;
    private RelativeLayout beautiful_contain;

    private LinearLayout compositeBeautiful;
    private LinearLayout smoothingBeautiful;
    private LinearLayout slimmingBeautiful;
    private LinearLayout catchlightBeautiful;
    private LinearLayout eyesEnlargementBeautiful;
    public FaceBeautyView() {
    }

    public FaceBeautyView(IApp app, ViewGroup parentViewGroup) {
        mActivity = app.getActivity();
        mParentViewGroup = parentViewGroup;
        initUI();
    }

    @Override
    public void initUI() {
        mView = mActivity.getLayoutInflater().inflate(R.layout.facebeauty_indicator,mParentViewGroup,true);
        mView.setVisibility(View.VISIBLE);
        LogHelper.i("xiaop","visibility: "+mView.getVisibility());
        beautiful_layout = (RelativeLayout)mView.findViewById(R.id.beautiful_layout);
        beautiful_contain = (RelativeLayout)mView.findViewById(R.id.beautiful_contain);
        compositeBeautiful = (LinearLayout)mView.findViewById(R.id.composite_beautiful);
        smoothingBeautiful = (LinearLayout)mView.findViewById(R.id.smoothing_beautiful);
        slimmingBeautiful = (LinearLayout)mView.findViewById(R.id.slimming_beautiful);
        catchlightBeautiful = (LinearLayout)mView.findViewById(R.id.catchlight_beautiful);
        eyesEnlargementBeautiful = (LinearLayout)mView.findViewById(R.id.eyes_enlargement_beautiful);

        compositeBeautiful.setOnClickListener(this);
        smoothingBeautiful.setOnClickListener(this);
        slimmingBeautiful.setOnClickListener(this);
        catchlightBeautiful.setOnClickListener(this);
        eyesEnlargementBeautiful.setOnClickListener(this);

    }

    @Override
    public void updateUi() {

    }

    @Override
    public void hide() {
        LogHelper.i("","set visibility gone");
        mView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.composite_beautiful:
                focuseView = compositeBeautiful;;
                break;
            case R.id.smoothing_beautiful:
                focuseView = smoothingBeautiful;
                break;
            case R.id.slimming_beautiful:
                focuseView = slimmingBeautiful;
                break;
            case R.id.catchlight_beautiful:
                focuseView = catchlightBeautiful;
                break;
            case R.id.eyes_enlargement_beautiful:
                focuseView = eyesEnlargementBeautiful;
                break;

        }
        setUiHightlight(v);
    }

    public void setUiHightlight(View v) {

        // TODO Auto-generated method stub
        if(v != null){
            clearSeletor();
            setSeletor(((ViewGroup)v));
//            refleshSeakbar();
//            mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_MODE).setValue(currentKey);
        }
    }

    public void clearSeletor(){
        compositeBeautiful.getChildAt(0).setSelected(false);
        smoothingBeautiful.getChildAt(0).setSelected(false);
        slimmingBeautiful.getChildAt(0).setSelected(false);
        catchlightBeautiful.getChildAt(0).setSelected(false);
        eyesEnlargementBeautiful.getChildAt(0).setSelected(false);

        ((TextView)compositeBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)smoothingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)slimmingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)catchlightBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)eyesEnlargementBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
    }

    private void setSeletor(ViewGroup v) {
        // TODO Auto-generated method stub
        v.getChildAt(0).setSelected(true);
        ((TextView)v.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_select));

    }
}
