package com.mediatek.camera.feature.setting.picselfie;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.RotateImageView;

import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.ui.CameraAppUI;

public class PicselfieParameterViewController {


    private static final LogUtil.Tag TAG = new LogUtil.Tag(PicselfieParameterViewController.class.getSimpleName());

    private PicselfieParameter mPicselfieParameter;
    private IApp mApp;
    private MainHandler mMainHandler;
    //private View mView;
    private String mPicselfieValue = VALUES_OFF;
    private static final int INIT_PICSELFIE_BUTTON = 0;
    //private static final int SHOW_PICSELFIE_BUTTON = 1;
    //private static final int HIDE_PICSELFIE_BUTTON = 2;
    private static final int MSG_SHOW_INDICATOR = 3;
    private IAppUi.HintInfo mHintInfo;
    private RotateImageView mPicselfieIndicatorView;

    private static final String VALUES_ON = "on";
    private static final String VALUES_OFF = "off";

    private static final int PICSELFIE_PRIORITY = 10;
    private SeekBar mSizeSeekbar;
    private SeekBar mBlurSeekbar;

    public PicselfieParameterViewController(PicselfieParameter picselfieParameter, IApp app) {
        mPicselfieParameter = picselfieParameter;
        mApp = app;
        mMainHandler = new MainHandler(mApp.getActivity().getMainLooper());
        if((null != mApp.getAppUi().getModeItem() && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE)){
            mMainHandler.sendEmptyMessageDelayed(INIT_PICSELFIE_BUTTON, 500);
        }else{
            mMainHandler.sendEmptyMessage(INIT_PICSELFIE_BUTTON);
        }
        mHintInfo = new IAppUi.HintInfo();
        mHintInfo.mDelayTime = 2000;
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mHintInfo.mBackground = mApp.getActivity().getDrawable(id);
        mHintInfo.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.i("",",msg.what: "+msg.what);
            switch (msg.what) {
                case INIT_PICSELFIE_BUTTON:
                    //initUI();
                    iniIndicatorView();
                    break;
                /*case HIDE_PICSELFIE_BUTTON:
                    //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-start
                    if (mView != null && mPicselfieParameter.getPicsefileSwitchValue().equals("on")) {
                        mView.setBackgroundResource(R.drawable.picselfie_normal);
                        mPicselfieValue = "off";
                    }
                    break;
                case SHOW_PICSELFIE_BUTTON:
                    if (mView != null && mPicselfieParameter.getPicsefileSwitchValue().equals("on")) {
                        mView.setBackgroundResource(R.drawable.picselfie_press);
                        mPicselfieValue = "on";
                        mPicselfieParameter.setPicsefileValue(mPicselfieValue);
                    }
                    //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-end
                    break;*/
                case MSG_SHOW_INDICATOR:
                    //if(null != mPicselfieIndicatorView){

                    if (msg.arg1 == 1) {
                        iniIndicatorView();
                        mApp.getAppUi().addToQuickSwitcher(mPicselfieIndicatorView, PICSELFIE_PRIORITY);
                    } else {
                        if(null != mPicselfieIndicatorView){
                            mApp.getAppUi().removeFromQuickSwitcher(mPicselfieIndicatorView);
                        }
                    }
                    //}
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /*private View initUI () {
        LogHelper.i("","init ui");
        mView = ((CameraAppUI)mApp.getAppUi()).getPicselfieView();
        mView.setOnClickListener(picsfileButtonOnclickListener);
        //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-start
        if ("on".equals(mPicselfieParameter.getPicsefileSwitchValue())) {
            mView.setBackgroundResource(R.drawable.picselfie_press);
            mPicselfieValue = "on";
        }
        //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-end
        return mView;
    }*/

    /*private View.OnClickListener picsfileButtonOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LogHelper.i("","click,mPicselfieValue: "+mPicselfieValue);
            if ("on".equals(mPicselfieValue)) {
                mPicselfieValue = "off";
                mView.setBackgroundResource(R.drawable.picselfie_normal);
                mHintInfo.mHintText = mApp.getActivity().getString(R.string.picselfie_open_tips_off);
            } else {
                mPicselfieValue = "on";
                mView.setBackgroundResource(R.drawable.picselfie_press);
                mHintInfo.mHintText = mApp.getActivity().getString(R.string.picselfie_open_tips_on);
            }
            mPicselfieParameter.setPicsefileValue(mPicselfieValue);
            //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-start
            mPicselfieParameter.setPicsefileSwitch(mPicselfieValue);
            //prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-end
            mApp.getAppUi().showScreenHint(mHintInfo,200);
        }
    };*/

    /*public void setHidePicselfieButton(){
        mMainHandler.sendEmptyMessage(HIDE_PICSELFIE_BUTTON);
    }

    public void setShowPicselfieButton() {
        mMainHandler.sendEmptyMessage(SHOW_PICSELFIE_BUTTON);
    }*/

    public void showPicSelfieIndicator(boolean show){
        Message msg = mMainHandler.obtainMessage();
        msg.what = MSG_SHOW_INDICATOR;
        msg.arg1 = show ? 1 : 0;

        mMainHandler.removeMessages(MSG_SHOW_INDICATOR);
        mMainHandler.sendMessageDelayed(msg, 0);
    }

    private ImageView iniIndicatorView() {

        if(null != mPicselfieIndicatorView){

            if(FeatureSwitcher.isSupportDualCam()){
                if (null != mApp.getAppUi().getModeItem() && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE) {
                    mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_on);
                    mPicselfieValue = VALUES_ON;
                }else{
                    mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_off);
                    mPicselfieValue = VALUES_OFF;
                }
            }

            return mPicselfieIndicatorView;
        }

        Activity activity = mApp.getActivity();
        mPicselfieIndicatorView = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.picselfie_indicator, null);

        if (VALUES_ON.equals(mPicselfieParameter.getPicsefileSwitchValue()) || (null != mApp.getAppUi().getModeItem() && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE)) {
            mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_on);
            mPicselfieValue = VALUES_ON;
        }else{
            mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_off);
            mPicselfieValue = VALUES_OFF;
        }

        mPicselfieIndicatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VALUES_ON.equals(mPicselfieValue)){
                    mPicselfieValue = VALUES_OFF;
                    mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_off);
                }else{
                    mPicselfieValue = VALUES_ON;
                    mPicselfieIndicatorView.setImageResource(R.drawable.picselfie_on);
                }

                mPicselfieParameter.setPicselfieValueByClick(mPicselfieValue);
            }
        });

        return mPicselfieIndicatorView;
    }

    public String getPicselfieValue(){
        return mPicselfieValue;
    }
}
