package com.mediatek.camera.prize;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.professional.ProfessionalModeEntry;
import com.mediatek.camera.common.mode.video.VideoMode;
import com.mediatek.camera.common.mode.video.VideoModeEntry;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.R;
import com.mediatek.camera.ui.prize.PrizePluginModeFragment;

/**
 * Created by huangpengfei on 2019/3/5.
 */
public class PrizePluginModeManager {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(PrizePluginModeManager.class.getSimpleName());
    private final IAppUi mCameraAppUI;
    private static final int PLUGIN_PRIORITY = 4;
    private final IApp mApp;
    private RotateImageView mFlashIndicatorView;
    private PrizePluginModeFragment mPrizePluginModeFragment;
    private boolean mIsAnimationFinish = true;
    private long mAnimationDuration;
    private int mScreenPixWidth;
    private View FragmentRootView;

    private static final String PREF_FILE = "plugin_file";
    private static final String PLUGIN_MODE_KEY = "plugin_mode";

    public PrizePluginModeManager(IApp app, int screenPixWidth) {
        this.mCameraAppUI = app.getAppUi();
        this.mApp = app;
        this.mScreenPixWidth = screenPixWidth;
        initView();
    }

    private void initView() {
        Activity activity = mApp.getActivity();
        boolean isSupport = isSupport(activity);
        LogHelper.d(TAG, "[initView]  isSupport = " + isSupport);
        if (!isSupport){
            return;
        }
        mAnimationDuration = mApp.getActivity().getResources().getInteger(R.integer.prize_setting_animation_duration);
        FragmentRootView = mApp.getActivity().findViewById(R.id.plugin_mode_fragment_root);
        mApp.registerBackPressedListener(mBackPressedListener,IApp.DEFAULT_PRIORITY);
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.prize_plugin_icon, null);
        view.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showFragment();
            }
        });
        mCameraAppUI.addToQuickSwitcher(view,PLUGIN_PRIORITY);
        mPrizePluginModeFragment = new PrizePluginModeFragment();
        mPrizePluginModeFragment.setOnBackClickListener(new PrizePluginModeFragment.OnBackClickListener() {
            @Override
            public void onClick() {
                hideFragment();
            }
        });

        mPrizePluginModeFragment.setStateListener(new PrizePluginModeFragment.StateListener() {
            @Override
            public void onCreate() {
                FragmentRootView.setVisibility(View.VISIBLE);
                mCameraAppUI.setCameraSwitchVisible(View.GONE);
                mCameraAppUI.applyAllUIVisibility(View.GONE);
                mCameraAppUI.setUIEnabled(mCameraAppUI.SHUTTER_BUTTON, false);
            }

            @Override
            public void onDestroy() {
                FragmentRootView.setVisibility(View.GONE);
                mCameraAppUI.setCameraSwitchVisible(View.VISIBLE);
                mCameraAppUI.applyAllUIVisibility(View.VISIBLE);
                mCameraAppUI.setUIEnabled(mCameraAppUI.SHUTTER_BUTTON, true);
            }
        });

        mPrizePluginModeFragment.setOnPluginModeItemClickListener(new PrizePluginModeFragment.OnPluginModeItemClickListener() {
            @Override
            public void onItemClick(String mode) {
                //TODO Something.

                setPluginMode(mApp.getActivity(), mode);

                mCameraAppUI.selectPluginMode(mode, true);

                hideFragment();
            }
        });
    }

    private IApp.BackPressedListener mBackPressedListener =
            new IApp.BackPressedListener() {
                @Override
                public boolean onBackPressed() {
                    if (FragmentRootView.getVisibility() == View.VISIBLE){
                        hideFragment();
                        return true;
                    }
                    return false;
                }
            };

    private void showFragment() {
        if (!mIsAnimationFinish) return;
        FragmentTransaction transaction = mApp.getActivity().getFragmentManager()
                .beginTransaction();
        transaction.addToBackStack("plugin_mode_fragment");
        transaction.replace(R.id.plugin_mode_fragment_root, mPrizePluginModeFragment, "plugin")
                .commitAllowingStateLoss();

        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(mAnimationDuration);
        TranslateAnimation translateAnimation = new TranslateAnimation(mScreenPixWidth, 0, 0, 0);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        translateAnimation.setDuration(mAnimationDuration);
        translateAnimation.setStartOffset(0);
        translateAnimation.setFillAfter (true);
        translateAnimation.setFillEnabled(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.d(TAG, "[showSetting] onAnimationStart..." );
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                LogHelper.d(TAG, "[showSetting]   onAnimationRepeat..." );
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "[showSetting]   onAnimationEnd..." );
                mIsAnimationFinish =true;
                FragmentRootView.clearAnimation();
            }
        });
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        FragmentRootView.startAnimation(animationSet);
        mIsAnimationFinish =false;
    }

    private void hideFragment(){
        if (!mIsAnimationFinish) return;
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setDuration(mAnimationDuration);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, mScreenPixWidth, 0, 0);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        translateAnimation.setDuration(mAnimationDuration);
        translateAnimation.setStartOffset(0);
        translateAnimation.setFillAfter (true);
        translateAnimation.setFillEnabled(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.d(TAG, "[hideSetting]   onAnimationStart..." );
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                LogHelper.d(TAG, "[hideSetting]   onAnimationRepeat..." );
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                boolean stateSaved = mApp.getActivity().getFragmentManager().isStateSaved();
                LogHelper.d(TAG, "[hideSetting]   onAnimationEnd... stateSaved = " + stateSaved);
                mIsAnimationFinish =true;
                FragmentRootView.clearAnimation();
                if (stateSaved) return;//prize-add for bug[70653]-huangpengfei-2019-01-16
                mApp.getActivity().getFragmentManager().popBackStackImmediate("plugin_mode_fragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        FragmentRootView.startAnimation(animationSet);
        mIsAnimationFinish =false;
    }

    public boolean isSupport(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !(MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore
                .ACTION_VIDEO_CAPTURE.equals(action)) && FeatureSwitcher.isSupportPlugin();
        LogHelper.d(TAG, "[isSupport] : " + support);
        return support;
    }

    public static void setPluginMode(Context context, String mode){
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        preferences.edit().putString(PLUGIN_MODE_KEY, mode).commit();
    }

    public static String getPluginMode(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return preferences.getString(PLUGIN_MODE_KEY, ProfessionalModeEntry.class.getName());
    }
}
