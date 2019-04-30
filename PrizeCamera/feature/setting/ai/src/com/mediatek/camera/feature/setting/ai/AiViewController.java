package com.mediatek.camera.feature.setting.ai;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.feature.setting.hdr.HdrViewController;
import com.mediatek.camera.R;
import com.mediatek.camera.common.prize.PrizeAiSceneClassify;

/**
 * Created by huangpengfei on 2018/12/3.
 */
class AiViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(HdrViewController.class.getSimpleName());

    private static final String AI_OFF_VALUE = "off";
    private static final String AI_ON_VALUE = "on";
    private final IApp mApp;
    private final Ai mAi;
    private Handler mMainHandler;
    private static final int AI_VIEW_INIT = 0;
    private static final int AI_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int AI_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int AI_VIEW_UPDATE_QUICK_SWITCH_ICON = 3;
    private static final int AI_VIEW_UPDATE_SCENE_ICON = 4;
    private ImageView mAiEntryView;
    private int AI_PRIORITY = 11;

    public AiViewController(Ai ai,IApp app) {
        mApp = app;
        mAi = ai;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(AI_VIEW_INIT);
    }

    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(AI_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    public void addQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(AI_VIEW_ADD_QUICK_SWITCH);
    }

    public void updateSceneIcon(int sceneId) {
        mMainHandler.obtainMessage(AI_VIEW_UPDATE_SCENE_ICON, sceneId).sendToTarget();
    }

    private ImageView initAiEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.ai_icon, null);
        view.setOnClickListener(mAiEntryListener);
        return view;
    }

    public void removeQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(AI_VIEW_REMOVE_QUICK_SWITCH);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "[handleMessage] msg.what: " + msg.what);
            switch (msg.what) {
                case AI_VIEW_INIT:
                    mAiEntryView = initAiEntryView();
                    break;
                case AI_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mAiEntryView, AI_PRIORITY);
                    if ("on".equals(mAi.getValue())){
                        selectEntryViewScene(PrizeAiSceneClassify.PRIZE_AI_ON);
                    }else{
                        selectEntryViewScene(PrizeAiSceneClassify.PRIZE_AI_OFF);
                    }
                    break;
                case AI_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mAiEntryView);
                    break;
                case AI_VIEW_UPDATE_SCENE_ICON:
                    selectEntryViewScene((int) msg.obj);
                    break;
                case AI_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mAiEntryView.setVisibility(View.VISIBLE);
                    } else {
                        mAiEntryView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    private void selectEntryViewScene(int sceneId) {
        LogHelper.d(TAG, "[selectEntryViewScene] sceneId: " + sceneId);
        if (mAiEntryView == null){
            LogHelper.d(TAG, "[selectEntryViewScene] return...");
            return;
        }
        switch (sceneId){
            case PrizeAiSceneClassify.PRIZE_SCENE_GOURMET:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_gourmet);
                break;
            case PrizeAiSceneClassify.PRIZE_SCENE_GREENERY:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_flower);
                break;
            /*case PrizeAiSceneClassify.PRIZE_SCENE_PORTRAIT:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_portrait);
                break;*/
            case PrizeAiSceneClassify.PRIZE_SCENE_LANDSCAPE:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_landscape);
                break;
            /*case PrizeAiSceneClassify.PRIZE_SCENE_NIGHT:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_night);
                break;
            case PrizeAiSceneClassify.PRIZE_SCENE_BACKLIT:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_backlight);
                break;*/
            case PrizeAiSceneClassify.PRIZE_SCENE_SUNSET:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_sunset);
                break;
            case PrizeAiSceneClassify.PRIZE_SCENE_BEACH:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_beach);
                break;
            case PrizeAiSceneClassify.PRIZE_SCENE_BLUESKY:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_bluesky);
                break;
            case PrizeAiSceneClassify.PRIZE_SCENE_URBAN:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_urban);
                break;
            case PrizeAiSceneClassify.PRIZE_AI_ON:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_on);
                break;
            case PrizeAiSceneClassify.PRIZE_AI_OFF:
                mAiEntryView.setImageResource(R.drawable.prize_ic_ai_off);
                break;
                default:
                    mAiEntryView.setImageResource(R.drawable.prize_ic_ai_on);
        }
    }

    private final View.OnClickListener mAiEntryListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            v.setEnabled(false);
            String value = mAi.getValue();
            value = "on".equals(value)?"off":"on";
            mAi.onAiValueChanged(value);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.setEnabled(true);
                }
            },500);
        }
    };

    public void picselfieTurnsOn(){
        if(null != mAiEntryView){
            mAiEntryView.performClick();
        }
    }
}
