package com.mediatek.camera.feature.setting.beautyparameter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.ui.CameraAppUI;

public class BeautyParameterViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(BeautyParameterViewController.class.getSimpleName());
    private View focuseView;
    private double step;
    private RelativeLayout beautiful_layout;
    private RelativeLayout beautiful_contain;
    private View mBeautyParameterView;
    private LinearLayout compositeBeautiful;
    private LinearLayout smoothingBeautiful;
    private LinearLayout slimmingBeautiful;
    private LinearLayout catchlightBeautiful;
    private LinearLayout eyesEnlargementBeautiful;
    private BeautyParameter mBeautyParameter;
    private IApp mApp;
    private MainHandler mMainHandler;
    private static final int BEAUTYPARAMETER_VIEW_INIT = 0;
    private static final int BEAUTYPARAMETER_VIEW_SHOW = 1;
    private static final int BEAUTYPARAMETER_VIEW_HIDE = 2;
    private static final int BEAUTYPARAMETER_VIEW_UNINIT = 3;
    private ViewGroup mParentViewGroup;

    private String currentKey = BeautyParameter.COMPOSITE_KEY;;
    private int currentValue;
    private SeekBar mSeekBar;
    public static final double SMOTTINGMAXVALUE = 100;
    public static final double SLIMMINGMAXVALUE = 100;//128;
    public static final double TONINRMAXVALUE = 100;
    public static final double EYESENLARGVALUE = 100;

    public BeautyParameterViewController(BeautyParameter beautyParameter, IApp app) {
        mBeautyParameter = beautyParameter;
        mApp = app;
        mMainHandler = new MainHandler(mApp.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(BEAUTYPARAMETER_VIEW_INIT);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.i(TAG,",what: "+msg.what+",mModeTitle: "+mApp.getAppUi().getModeItem().mModeTitle);
            switch (msg.what){
                case BEAUTYPARAMETER_VIEW_INIT:
                    if(mBeautyParameterView == null){
                        mBeautyParameterView = initBeautyPameterView();
                    }
                    mBeautyParameterView.setVisibility(View.GONE);
                    break;
                case BEAUTYPARAMETER_VIEW_SHOW:
                    if (mBeautyParameterView != null && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.BEAUTY) {
                        mBeautyParameterView.setVisibility(View.VISIBLE);
                    }
                    break;
                case BEAUTYPARAMETER_VIEW_HIDE:
                    if (mBeautyParameterView != null) {
                        mBeautyParameterView.setVisibility(View.INVISIBLE);
                    }
                    break;
                case BEAUTYPARAMETER_VIEW_UNINIT:
                    if(mParentViewGroup != null){
                        mParentViewGroup.removeAllViews();
                        mParentViewGroup = null;
                        mBeautyParameterView = null;
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private View initBeautyPameterView() {
        mParentViewGroup = ((CameraAppUI)mApp.getAppUi()).getSettingRoot();
        mBeautyParameterView = mApp.getActivity().getLayoutInflater().inflate(R.layout.facebeauty_indicator,mParentViewGroup,true);
        beautiful_layout = (RelativeLayout)mBeautyParameterView.findViewById(R.id.beautiful_layout);
        beautiful_contain = (RelativeLayout)mBeautyParameterView.findViewById(R.id.beautiful_contain);

        compositeBeautiful = (LinearLayout)mBeautyParameterView.findViewById(R.id.composite_beautiful);
        smoothingBeautiful = (LinearLayout)mBeautyParameterView.findViewById(R.id.smoothing_beautiful);
        slimmingBeautiful = (LinearLayout)mBeautyParameterView.findViewById(R.id.slimming_beautiful);
        catchlightBeautiful = (LinearLayout)mBeautyParameterView.findViewById(R.id.catchlight_beautiful);
        eyesEnlargementBeautiful = (LinearLayout)mBeautyParameterView.findViewById(R.id.eyes_enlargement_beautiful);
        mSeekBar = (SeekBar) mBeautyParameterView.findViewById(R.id.beautifu_seekbar);
        LogHelper.i("","visibility: "+mBeautyParameterView.getVisibility()+",slimmingBeautiful: "+slimmingBeautiful+",mSeekBar: "+mSeekBar);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        compositeBeautiful.setOnClickListener(beautyparameterViewListener);
        smoothingBeautiful.setOnClickListener(beautyparameterViewListener);
        slimmingBeautiful.setOnClickListener(beautyparameterViewListener);
        catchlightBeautiful.setOnClickListener(beautyparameterViewListener);
        eyesEnlargementBeautiful.setOnClickListener(beautyparameterViewListener);
        return mBeautyParameterView;
    }

    public void uninit(){
        mMainHandler.sendEmptyMessage(BEAUTYPARAMETER_VIEW_UNINIT);

    }

    private View.OnClickListener beautyparameterViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.composite_beautiful:
                    setCompositeMode();
                    break;
                case R.id.smoothing_beautiful:
                    setSmoothingMode();
                    break;
                case R.id.slimming_beautiful:
                    setSlimmingMode();
                    break;
                case R.id.catchlight_beautiful:
                    setCatchlightMode();
                    break;
                case R.id.eyes_enlargement_beautiful:
                    setEyesEnlargementMode();
                    break;

            }
            setUiHightlight(v);
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            LogHelper.i("","progress: "+progress);
            if (currentKey.equals(BeautyParameter.COMPOSITE_KEY)) {
                setComposite( progress);
            } else {
                mBeautyParameter.setBeautyParameterProgress(currentKey,progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            LogHelper.i("",seekBar.getProgress()+"");
            mBeautyParameter.setBeautyParameterProgress(currentKey, seekBar.getProgress());
            if (currentKey.equals(BeautyParameter.COMPOSITE_KEY)) {
                mBeautyParameter.setCompositeProgress(BeautyParameter.SMOOTHING_KEY,(int) (seekBar.getProgress()));
                mBeautyParameter.setCompositeProgress(BeautyParameter.SLIMMING_KEY,(int) (seekBar.getProgress()));
                mBeautyParameter.setCompositeProgress(BeautyParameter.CATCHLIGHT_KEY,(int) (seekBar.getProgress()));
                mBeautyParameter.setCompositeProgress(BeautyParameter.EYESENLARGEMENT_KEY,(int) (seekBar.getProgress()));
            }
        }
    };

    public void setUiHightlight(View v) {

        // TODO Auto-generated method stub
        if(v != null){
            clearSeletor();
            setSeletor(((ViewGroup)v));
            refleshSeakbar();
            mBeautyParameter.setBeautyParameterProgress(currentKey,currentValue);
        }
    }

    public void clearSeletor(){
        compositeBeautiful.getChildAt(0).setSelected(false);
        smoothingBeautiful.getChildAt(0).setSelected(false);
        slimmingBeautiful.getChildAt(0).setSelected(false);
        catchlightBeautiful.getChildAt(0).setSelected(false);
        eyesEnlargementBeautiful.getChildAt(0).setSelected(false);

        ((TextView)compositeBeautiful.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)smoothingBeautiful.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)slimmingBeautiful.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)catchlightBeautiful.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)eyesEnlargementBeautiful.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_normal));
    }

    private void setSeletor(ViewGroup v) {
        // TODO Auto-generated method stub
        v.getChildAt(0).setSelected(true);
        ((TextView)v.getChildAt(1)).setTextColor(mApp.getActivity().getResources().getColor(R.color.beautiful_text_color_select));

    }

    public void showBeautyParameterView() {
        mMainHandler.sendEmptyMessageDelayed(BEAUTYPARAMETER_VIEW_SHOW,50);
    }

    public void hideBeautyParameterView() {
        mMainHandler.sendEmptyMessageDelayed(BEAUTYPARAMETER_VIEW_HIDE,50);
    }

    public void setSmoothingMode() {

        // TODO Auto-generated method stub
        currentKey = BeautyParameter.SMOOTHING_KEY;
        currentValue = mBeautyParameter.getBeautyParameterProgress(currentKey);
        focuseView = smoothingBeautiful;
        step = SMOTTINGMAXVALUE/100;
    }
    public void setCompositeMode() {

        // TODO Auto-generated method stub
        currentKey = BeautyParameter.COMPOSITE_KEY;
        currentValue = mBeautyParameter.getBeautyParameterProgress(currentKey);
        focuseView = compositeBeautiful;
        step = SMOTTINGMAXVALUE/100;
    }
    public void setSlimmingMode() {

        // TODO Auto-generated method stub
        currentKey = BeautyParameter.SLIMMING_KEY;
        currentValue = mBeautyParameter.getBeautyParameterProgress(currentKey);
        focuseView = slimmingBeautiful;
        step = SLIMMINGMAXVALUE/100;
    }
    public void setCatchlightMode() {

        // TODO Auto-generated method stub
        currentKey = BeautyParameter.CATCHLIGHT_KEY;
        currentValue = mBeautyParameter.getBeautyParameterProgress(currentKey);
        focuseView = catchlightBeautiful;
        step = TONINRMAXVALUE/100;
    }
    public void setEyesEnlargementMode() {

        // TODO Auto-generated method stub
        currentKey = BeautyParameter.EYESENLARGEMENT_KEY;
        currentValue = mBeautyParameter.getBeautyParameterProgress(currentKey);
        focuseView = eyesEnlargementBeautiful;
        step = EYESENLARGVALUE/100;
    }

    public void setCurrentMode(String key) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (key.equals(BeautyParameter.COMPOSITE_KEY)) {
                    setCompositeMode();
                } else if (key.equals(BeautyParameter.SMOOTHING_KEY)) {
                    setSmoothingMode();
                } else if (key.equals(BeautyParameter.SLIMMING_KEY)) {
                    setSlimmingMode();
                } else if (key.equals(BeautyParameter.CATCHLIGHT_KEY)) {
                    setCatchlightMode();
                } else if (key.equals(BeautyParameter.EYESENLARGEMENT_KEY)) {
                    setEyesEnlargementMode();
                }
                setUiHightlight(focuseView);
                LogHelper.i("", "key: " + key + ",currentValue: " + currentValue + ",mSeekBar: " + mSeekBar);
                seekBarChangeListener.onProgressChanged(mSeekBar, mSeekBar != null ? mSeekBar.getProgress() : 50, false);
            }
        });

    }

    public void refleshSeakbar(){
        LogHelper.d(TAG, "reflesh seakbar:"+currentValue);
        mSeekBar.setProgress(currentValue);

    }

    public void setComposite(int valus) {
        mBeautyParameter.setCompositeProgress(BeautyParameter.SMOOTHING_KEY,valus);
        mBeautyParameter.setCompositeProgress(BeautyParameter.SLIMMING_KEY,valus);
        mBeautyParameter.setCompositeProgress(BeautyParameter.CATCHLIGHT_KEY,valus);
        mBeautyParameter.setCompositeProgress(BeautyParameter.EYESENLARGEMENT_KEY,valus);
    }


    public double getStep(String key) {
        double beautystep = 0;
        if(key.equals(BeautyParameter.SMOOTHING_KEY)){
            beautystep = SMOTTINGMAXVALUE/100;
        }else if(key.equals(BeautyParameter.SLIMMING_KEY)){
            beautystep = SLIMMINGMAXVALUE/100;
        }else if(key.equals(BeautyParameter.CATCHLIGHT_KEY)){
            beautystep = TONINRMAXVALUE/100;
        }else if(key.equals(BeautyParameter.EYESENLARGEMENT_KEY)){
            beautystep = EYESENLARGVALUE/100;
        }else if(key.equals(BeautyParameter.COMPOSITE_KEY)){
            beautystep = SMOTTINGMAXVALUE/100;
        }

        return beautystep;
    }
}
