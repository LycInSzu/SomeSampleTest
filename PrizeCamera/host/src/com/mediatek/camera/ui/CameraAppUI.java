/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.transition.Visibility;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.IAppUiListener.OnGestureListener;
import com.mediatek.camera.common.IAppUiListener.OnModeChangeListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewAreaChangedListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewTouchedListener;
import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.IAppUiListener.OnThumbnailClickedListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.IReviewUI;
import com.mediatek.camera.common.mode.beauty.FaceBeautyModeEntry;
import com.mediatek.camera.common.mode.hdr.HdrModeEntry;
import com.mediatek.camera.common.mode.lowlight.LowLightModeEntry;
import com.mediatek.camera.common.mode.photo.PhotoModeEntry;
import com.mediatek.camera.common.mode.photo.device.IDeviceController;
import com.mediatek.camera.common.mode.photo.intent.IIntentPhotoUi;
import com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry;
import com.mediatek.camera.common.mode.picselfie.PicselfieModeEntry;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomModeEntry;
import com.mediatek.camera.common.mode.professional.IArcProgressBarUI;
import com.mediatek.camera.common.mode.professional.ProfessionalModeEntry;
import com.mediatek.camera.common.mode.video.VideoModeEntry;
import com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.PreviewFrameLayout;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
import com.mediatek.camera.feature.setting.grid.GridManager;
import com.mediatek.camera.feature.setting.picselfie.PicselfieParameterViewController;
import com.mediatek.camera.gesture.GestureManager;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.prize.PrizeArcProgressBarView;
import com.mediatek.camera.prize.PrizeCircleView;
import com.mediatek.camera.prize.PrizeModeAnimation;
import com.mediatek.camera.prize.PrizeArcProgressBarView;
import com.mediatek.camera.prize.PrizePluginModeManager;
import com.mediatek.camera.prize.PrizeRelativeLayout;
import com.mediatek.camera.prize.PrizeSeekBar;
import com.mediatek.camera.prize.PrizeTimeTask;
import com.mediatek.camera.ui.modepicker.ModePickerManager;
import com.mediatek.camera.ui.modepicker.ModeProvider;
import com.mediatek.camera.ui.photo.IntentPhotoUi;
import com.mediatek.camera.ui.preview.PreviewManager;
import com.mediatek.camera.ui.shutter.ShutterButtonManager;
import com.mediatek.camera.ui.video.VideoUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
import java.util.Timer;
import java.util.Arrays;
import com.mediatek.camera.common.prize.PrizeReadNodeValue;
import com.mediatek.camera.prize.PrizeTimeTask;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import com.mediatek.common.prizeoption.NvramUtils;
import com.android.internal.util.HexDump;
import java.io.FileOutputStream;
/*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
import java.util.concurrent.ConcurrentHashMap;
import android.os.SystemProperties; //prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start
import android.content.Intent;//prize-add for external intent-huangpengfei-2019-2-16

import com.prize.camera.feature.mode.filter.FilterModeEntry;
import com.prize.camera.feature.mode.gif.GifModeEntry;
import com.prize.camera.feature.mode.pano.PanoModeEntry;
import com.prize.camera.feature.mode.smartscan.SmartScanModeEntry;

/**
 * CameraAppUI centralizes control of views shared across modules. Whereas module
 * specific views will be handled in each Module UI. For example, we can now
 * bring the flash animation and capture animation up from each module to app
 * level, as these animations are largely the same for all modules.
 *
 * This class also serves to disambiguate touch events. It recognizes all the
 * swipe gestures that happen on the preview by attaching a touch listener to
 * a full-screen view on top of preview TextureView. Since CameraAppUI has knowledge
 * of how swipe from each direction should be handled, it can then redirect these
 * events to appropriate recipient views.
 */
public class CameraAppUI implements IAppUi,IArcProgressBarUI,PrizeSeekBar.OnProgressChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraAppUI.class.getSimpleName());
    private final IApp mApp;
    private final int mAnimationDuration;//prize-tangan-20180921-add prizecamera-begin

    private GestureManager mGestureManager;
    private ShutterButtonManager mShutterManager;
    private ThumbnailViewManager mThumbnailViewManager;
    private PreviewManager mPreviewManager;
    //private ModePickerManager mModePickerManager;//prize-modify-huangpengfei-2018-12-19
    private QuickSwitcherManager mQuickSwitcherManager;
    private IndicatorViewManager mIndicatorViewManager;
    private SettingFragment mSettingFragment;
    private EffectViewManager mEffectViewManager;
    private OnScreenHintManager mOnScreenHintManager;
    private AnimationManager mAnimationManager;

    private final List<IViewManager> mViewManagers;

    private OnModeChangeListener mModeChangeListener;

    private ViewGroup mSavingDialog;

    private String mCurrentModeName;
    private String mCurrentCameraId = "0";
    private String mCurrentModeType;

    private ModeProvider mModeProvider;
    private Handler mConfigUIHandler = new ConfigUIHandler();
    private static final int APPLY_ALL_UI_VISIBILITY = 0;
    private static final int APPLY_ALL_UI_ENABLED = 1;
    private static final int SET_UI_VISIBILITY = 2;
    private static final int SET_UI_ENABLED = 3;
    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
    private static final int SET_CAPTUREANIMATION_VISIBILITY = 4;
    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/

    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    private int mCameraId = 0;
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
    private final OnOrientationChangeListenerImpl mOrientationChangeListener;
    /*prize-add for GridLine -huangpengfei-2018-9-26-start*/
    private GridManager mGridManager;
    private View mCameraSwitchView = null;//prize-tangan-20180921-add prizecamera
    /*prize-add for GridLine -huangpengfei-2018-9-26-end*/

    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
    private ModeItem mCurrentModeItem ;
    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
    /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-start*/
    private RotateImageView mSettingButton;
    /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-end*/
    //private View mPicselfieView;

    /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-start*/
    private int mScreenPixWidth;
    private int mScreenPixHeight;
    /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-end*/

    private String mHdrValue = "off";
    private String mPicsflieValue = "off";

    /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-start*/
    private boolean isOnSwitchCamera = false;
    /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-end*/


    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
    private boolean isCaptureFaileOnHD = false;
    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-end*/

	/*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/
    private IDeviceController mIDeviceController;
	private PrizeModeAnimation mPrizeModeAnimation;
    private boolean mPortraitCallForAiTarget = false;
	private static final int SWITCH_HDR = 5;
    private boolean mNeedDelay = false;
	/*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/

    /*prize-add for model merging-huangpengfei-2019-02-23-start*/
    private static final int SHOW_COVER = 6;
    /*prize-add for model merging-huangpengfei-2019-02-23-end*/

    private boolean isCaptureOrVideo = false;
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
    private String mCaptureType;
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/

    /*prize-modify-add professional mode function-xiaoping-20190216-start*/
    private PrizeArcProgressBarView mWhiteBalanceView;
    private PrizeArcProgressBarView mIsoView;
    private PrizeArcProgressBarView mEvView;
    private  int[] pictures= {R.drawable.scence_auto,R.drawable.scence_incandescent,R.drawable.scence_daylight,R.drawable.scence_fluorescent,R.drawable.scence_cloudy};
    private  int[] checkeds = {R.drawable.scence_auto_check,R.drawable.scence_incandescent_check,R.drawable.scence_daylight_check,R.drawable.scence_fluorescent_check,R.drawable.scence_cloudy_check};
    private int[] mWhiteBalanceValue = {1,2,5,3,6};
    private LinearLayout mArcProgressBarLayout;
    private String[] mStrings = {"A","100","200","400","800"};
    private String[] mNumberStrings = {"-3","-2","-1","0","1","2","3"};
    private String[] mWhiteBalanceStringValues ={"auto", "incandescent", "daylight","fluorescent", "cloudy-daylight"};
    private  CaptureRequest.Key<int[]> mKeyIsoRequestValue;
    private ConcurrentHashMap<String,Integer> mConcurrentHashMap;
    private DataStore mDataStore;
    private View mView;
    private ImageView mProgressControlView;
    private ImageView mProgressResetView;
    private boolean isNeedShowProgressBar = true;
    private PrizeRelativeLayout mProgressLayout;
    private LinearLayout mProfessionalTitle;
    /*prize-modify-add professional mode function-xiaoping-20190216-end*/

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    private View mScreenFlashView;
    /*prize-add-screen flash-huangzhanbin-20190226-end*/
    /*prize-modify-add professional mode function-xiaoping-20190216-end*/

	/*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
    private PrizeReadNodeValue mPrizeReadNodeValue;
    private Timer mTimer;
	private int index;
    private PrizeTimeTask mTimerTask;
    private int mLastUpValue = -1;
    private int maxUpValue;
    private static final int PRESSCAMERA= 10;
    private static final int PREVENTCAMERA= 11;
    private long mTimeInterval = 200;
    private boolean isPreventCamera = false;
    private LiftCamera.LiftCameraState mLiftCameraState = LiftCamera.LiftCameraState.UNKNOWN;
    private ImplLifaCamera mImplLifaCamera;
    private int mFirstUpValue;
    private HintInfo mLiftCameraInfo;
    private long mLastLiftingTime;
    private int mLiftingCount;
    private HintInfo mFrequentlyOpenCameraInfo;
    private static final int SET_CAMERA_SWITCH_ENABLE = 12;
    private static final int STOP_READ_NODEVALUE = 13;
    private long mLastResumeTime;
    private static final int CHANGE_TIMERTASK_TIMEINTERVAL= 14;
    private int maxValueUp;
    private int maxValueDown;
	public static final int PRIZE_FACTORY_FACTORY_INFO_OFFSET = 150;
    public static final int PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET = PRIZE_FACTORY_FACTORY_INFO_OFFSET + 94;
    private boolean isStartLiftFirst = true;
	/*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
    private boolean isSettingViewShow = false;
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/

    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-start*/
    private boolean isSelfTimerStart = false;
    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-end*/

    private static final int MAXLEVEL = 7;
    private static final int MINLEVEL = 1;
    private PrizeCircleView mPrizeCircleView;
    private PrizeSeekBar mBlurSeekBar;
    private PrizeSeekBar mSizeSeekBar;
    private ImageButton mControlButton;
    private RelativeLayout mAllBlurLayout;
    private LinearLayout mBlurLinearLayout;
    private LinearLayout mSizeLinearLayout;
    private boolean isSeekbarHasShow = false;
    private static final int BLUR_SEEKBAR = 1;
    private static final int SIZE_SEEKBAR = 2;
    private int defaultvalue = (MAXLEVEL - MINLEVEL)/2 + MINLEVEL;
    private int mLevelValue;
    private int minRadius;
    private int maxRadius;
    List<ModeItem> mModeItems;
    private static final int PLUGIN_MODE_PRIORITY = 6;
    private UVPicselfieCallback mPicselfieCallback;
    private int mPreviewWidth;
    private int mPreviewHeight;
    /*prize-modify-feature:k6203vs-can take a photo before the countdown ends-xiaoping-20190417-start*/
    private long mLastClickTime;
    private PrizePluginModeManager mPrizePluginModeManager;
    /*prize-modify-feature:k6203vs-can take a photo before the countdown ends-xiaoping-20190417-end*/
    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-start*/
    private boolean isZoomState;
    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-end*/

    /**
     * Constructor of cameraAppUi.
     * @param app The {@link IApp} implementer.
     */


    private Size mFocusPoint; // prize add by zhangguo 20190419, for bug#74593, dualcam focus point error
    private int mFilterIndex; // prize add by zhangguo 20190419, for bug#74679, filter is none after switch camera

    public CameraAppUI(IApp app) {
        mApp = app;
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mViewManagers = new ArrayList<>();
        mAnimationDuration = mApp.getActivity().getResources().getInteger(R.integer.prize_setting_animation_duration);//prize-tangan-20180921-add prizecamera-begin
    }
    /**
     * Called when activity's onCreate() is invoked.
     */
    public void onCreate() {
        /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-start*/
        getScreenPix();
        /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-end*/
        ViewGroup rootView = (ViewGroup) mApp.getActivity()
                .findViewById(R.id.app_ui_root);

        ViewGroup parentView = (ViewGroup) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.camera_ui_root, rootView, true);

        View appUI = parentView.findViewById(R.id.camera_ui_root);
        mView = appUI;
        if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
            //get navigation bar height.
            int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
            //set root view bottom margin to let the UI above the navigation bar.
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) appUI.getLayoutParams();
            if (CameraUtil.isTablet()) {
                int displayRotation = CameraUtil.getDisplayRotation(mApp.getActivity());
               LogHelper.d(TAG, " onCreate displayRotation  " + displayRotation);
                if (displayRotation == 90 || displayRotation == 270) {
                    params.leftMargin += navigationBarHeight;
                    appUI.setLayoutParams(params);
                } else {
                    params.bottomMargin += navigationBarHeight;
                    appUI.setLayoutParams(params);
                }
            } else {
                params.bottomMargin += navigationBarHeight;
                appUI.setLayoutParams(params);
            }
        }
        mModeProvider = new ModeProvider();
        String action = mApp.getActivity().getIntent().getAction();
        mGestureManager = new GestureManager(mApp.getActivity());
        mAnimationManager = new AnimationManager(mApp, this);

        mShutterManager = new ShutterButtonManager(mApp, parentView);
        mShutterManager.setVisibility(View.VISIBLE);
        mShutterManager.setOnShutterChangedListener(new OnShutterChangeListenerImpl());
        mViewManagers.add(mShutterManager);

        if (!(MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action))) {
            mThumbnailViewManager = new ThumbnailViewManager(mApp, parentView);
            mViewManagers.add(mThumbnailViewManager);
            mThumbnailViewManager.setVisibility(View.VISIBLE);
        }

        mPreviewManager = new PreviewManager(mApp);
        //Set gesture listener to receive touch event.
        mPreviewManager.setOnTouchListener(new OnTouchListenerImpl());

        /*prize-remove-hpf-2018-12-19-start*/
        /*mModePickerManager = new ModePickerManager(mApp, parentView);
        mModePickerManager.setSettingClickedListener(new OnSettingClickedListenerImpl());
        mModePickerManager.setModeChangeListener(new OnModeChangedListenerImpl());
        mModePickerManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mModePickerManager);*/
        /*prize-remove-hpf-2018-12-19-end*/

        mQuickSwitcherManager = new QuickSwitcherManager(mApp, parentView);
        mQuickSwitcherManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mQuickSwitcherManager);

        mIndicatorViewManager = new IndicatorViewManager(mApp, parentView);
        mIndicatorViewManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mIndicatorViewManager);

        mSettingFragment = new SettingFragment();

        mSettingFragment.setStateListener(new SettingStateListener());
        /*prize-add-hpf-2018-09-04-start*/
        mPrizePluginModeManager = new PrizePluginModeManager(mApp,mScreenPixWidth);
        mSettingButton = mApp.getActivity().findViewById(R.id.camera_settings);
        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetting();
            }
        });
        mSettingFragment.setOnBackPressListener(new SettingFragment.OnBackPressListener() {
            @Override
            public void onBackPress() {
                hideSetting();
            }
        });
        mGridManager = new GridManager(mApp, parentView,this);
		/*prize-add AI CAMERA-huangpengfei-2019-01-29-start*/
        mPrizeModeAnimation = new PrizeModeAnimation(mApp);
        View sceneModeClose = mApp.getActivity().findViewById(R.id.ai_scene_mode_close);
        sceneModeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPortraitCallForAiTarget = false;
                if (mPrizeModeAnimation != null){
                    mPrizeModeAnimation.hideSceneText();
                }
                mNeedDelay = true;
                mModeChangeListener.onModeSelected(PhotoModeEntry.class.getName());
            }
        });
		/*prize-add AI CAMERA-huangpengfei-2019-01-29-end*/
        /*prize-add-hpf-2018-09-04-end*/
        //mPicselfieView = mApp.getActivity().findViewById(R.id.picsfile);
        layoutSettingUI();

        mEffectViewManager = new EffectViewManager(mApp, parentView);
        mEffectViewManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mEffectViewManager);

        mOnScreenHintManager = new OnScreenHintManager(mApp, parentView);
        //call each manager's onCreate()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onCreate();
        }
        mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
        mApp.registerKeyEventListener(getKeyEventListener(), IApp.DEFAULT_PRIORITY - 1);
        // @}
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        mPrizeReadNodeValue = new PrizeReadNodeValue();
        mImplLifaCamera = new ImplLifaCamera();
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
    }

    /**
     * Called when activity's onResume() is invoked.
     */
    public void onResume() {
        /*prize-add for external intent-huangpengfei-2019-2-16-start*/
        Intent intent = mApp.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        if (actionMode == null){
            mPrizeModeAnimation.hideCover();
        }
        /*prize-add for external intent-huangpengfei-2019-2-16-end*/
        RotateLayout root = (RotateLayout) mApp.getActivity().findViewById(R.id.app_ui);
        Configuration newConfig = mApp.getActivity().getResources().getConfiguration();
        hideAlertDialog();
        LogHelper.d(TAG, "onResume orientation = " + newConfig.orientation);
        if (root != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                root.setOrientation(0, false);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                root.setOrientation(90, false);
            }
        }
        //call each manager's onResume()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onResume();
        }
        /*prize-add for external intent-huangpengfei-2019-2-16-start*/
        mConfigUIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                IntentSwitchMode();
            }
        },100);
        /*prize-add for external intent-huangpengfei-2019-2-16-end*/
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        if (FeatureSwitcher.isLiftCameraSupport() && "1".equals(mCurrentCameraId)) {
            startReadNodeValue();
            if ("1".equals(mCurrentCameraId) && System.currentTimeMillis() - mLastResumeTime < 3000) {
                mLiftingCount ++;
            } else {
                mLiftingCount = 0;
            }
            if (mLiftingCount == 3) {
                int marginTop = (int) mApp.getActivity().getResources().getDimension(R.dimen.exit_app_hint_margintop);
                showScreenHint(mFrequentlyOpenCameraInfo,marginTop);
                mLiftingCount = 0;
            }
            mLastResumeTime = System.currentTimeMillis();
        }
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
    }

    /**
     * Called when activity's onPause() is invoked.
     */
    public void onPause() {
        //call each manager's onPause()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onPause();
        }
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        stopReadNodeValue();
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
        hideAlertDialog();
        hideSetting();
        mPreviewManager.onPause();
    }

    /*prize-add for external intent-huangpengfei-2019-2-16-start*/
    public void onStop() {
        if (mPreviewManager != null){
            mPrizeModeAnimation.showCover();
        }
    }
    /*prize-add for external intent-huangpengfei-2019-2-16-end*/

    /**
     * Called when activity's onDestroy() is invoked.
     */
    public void onDestroy() {
        //call each manager's onDestroy()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onDestroy();
        }
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        stopReadNodeValue();
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
        mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
        mApp.unRegisterKeyEventListener(getKeyEventListener());
        // @}
    }

    /**
     * Called by the system when the device configuration changes while your
     * activity is running.
     * @param newConfig The new device configuration.
     */
    public void onConfigurationChanged(Configuration newConfig) {
    }

    /**
     * Update thumbnailView, when the bitmap finished update, is will be recycled
     * immediately, do not use the bitmap again!
     * @param bitmap
     *            the bitmap matched with the picture or video, such as
     *            orientation, content. suggest thumbnail view size.
     */
    public void updateThumbnail(final Bitmap bitmap) {
        if (mThumbnailViewManager != null) {
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mThumbnailViewManager.updateThumbnail(bitmap);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            });
        }
    }

    /**
     * get the width of thumbnail view.
     * @return the min value of width and height of thumbnail view.
     */
    public int getThumbnailViewWidth() {
        if (mThumbnailViewManager != null) {
            return mThumbnailViewManager.getThumbnailViewWidth();
        } else {
            return 0;
        }
    }

    @Override
    public void registerQuickIconDone() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuickSwitcherManager.registerQuickIconDone();
            }
        });
    }

    @Override
    public void registerIndicatorDone() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.registerQuickIconDone();
            }
        });
    }

    @Override
    public void registerMode(List<ModeItem> items) {
	
        ModeItem item = null;
        mModeProvider.clearAllModes();
        mModeItems = items;

        String pluginName = PrizePluginModeManager.getPluginMode(mApp.getActivity());
		//prize-tangan-20180921-add prizecamera-begin
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
//        mShutterManager.clearShutterButtons();
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            //LogHelper.i(TAG,"getName: mClassName: "+ item.mClassName+" plugin="+pluginName+" item.mType="+item.mType+" isvideo="+(item.mType.equals("Video")));

            boolean isPluginMode = false;

            if(item instanceof IAppUi.PluginModeItem){
                if(!item.mClassName.equals(pluginName)){
                    continue;
                }else{
                    isPluginMode = true;
                }
            }

            mModeProvider.registerMode(item);
            /*prize-add-add mode-xiaoping-20180901-start*/
            if (item.mType.equals("Picture")) {
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType,FeatureSwitcher.getVideoModeIndex() + 1,ShutterButtonManager.MODE_PHOTO);
            } else if (item.mType.equals("Video")) {
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_video), item.mType, FeatureSwitcher.getVideoModeIndex(), ShutterButtonManager.MODE_VIDEO);
                  /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-end*/
            }
            //prize-tangan-20180921-add prizecamera-end
            else if(item.mType.equals("SmartScan")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, PLUGIN_MODE_PRIORITY, ShutterButtonManager.MODE_SCAN, isPluginMode);
            }else if(item.mType.equals("HDR")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, FeatureSwitcher.isArcsoftNightShotSupported() ? 1: 0, ShutterButtonManager.MODE_HDR, isPluginMode);
            }else if(item.mType.equals("UHD")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, FeatureSwitcher.getVideoModeIndex() + 2,ShutterButtonManager.MODE_PICTURE_ZOOM, isPluginMode);
            }else if(item.mType.equals("LowLight")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, 0,ShutterButtonManager.MODE_LOWLIGHT_SHOT, isPluginMode);
            }else if(item.mType.equals("Professional")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, PLUGIN_MODE_PRIORITY,ShutterButtonManager.MODE_PROFESSIONAL, isPluginMode);
            }else if(item.mType.equals("Filter")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, PLUGIN_MODE_PRIORITY, ShutterButtonManager.MODE_FILTER, isPluginMode);
            }else if(item.mType.equals("Gif")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_photo), item.mType, PLUGIN_MODE_PRIORITY, ShutterButtonManager.MODE_GIF, isPluginMode);
            }else if(item.mType.equals("SlowMotion")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                        .getDrawable(
                                R.drawable.btn_video), item.mType, PLUGIN_MODE_PRIORITY, ShutterButtonManager.MODE_SLOWMOTION, isPluginMode);
            }else if(item.mType.equals("FaceBeauty")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                                .getDrawable(R.drawable.btn_photo), item.mType,
                        FeatureSwitcher.isArcsoftSupperZoomSupported() ? FeatureSwitcher.getVideoModeIndex() + 3 : FeatureSwitcher.getVideoModeIndex() + 2,ShutterButtonManager.MODE_FACE_BEAUTY, isPluginMode);
            }else if(item.mType.equals("PrizePano")){
                mShutterManager.registerShutterButton(mApp.getActivity().getResources()
                                .getDrawable(R.drawable.btn_photo), item.mType,
                        PLUGIN_MODE_PRIORITY, ShutterButtonManager.MODE_PRIZE_PANO, isPluginMode);
            }
        }
        //mModePickerManager.registerModeProvider(mModeProvider);//prize-modify-huangpengfei-2018-12-19
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
        //onCameraSelected(String.valueOf(mCameraId));
        mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShutterManager.registerDone();
                    //mShutterManager.onResume();

                }
            });
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
    }

    /**
     * Notice: This is used for mode manager to update current mode, do not
     * use it in any other place.
     */
    @Override
    public void updateCurrentMode(final String modeEntry) {
        LogHelper.d(TAG, "updateCurrentMode mode = " + modeEntry);
        mPrizeModeAnimation.showCover();//prize-add for external intent-huangpengfei-2019-2-16
        if (mModeProvider != null) {
            ModeItem item = mModeProvider.getMode(modeEntry);
            if (item == null) {
                return;
            }
            /*prize-add mirror-huangpengfei-2019-03-02-start*/
            if (item.mModeTitle == ModeTitle.VIDEO && mThumbnailViewManager != null){
                mThumbnailViewManager.setMirrorEnable(false);
            }
            /*prize-add mirror-huangpengfei-2019-03-02-end*/

            /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
            mCurrentModeItem = item;
            /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
            /*if (FeatureSwitcher.isArcsoftSelfieSupported() && getCameraId() == 1 && PhotoModeEntry.class.getName().equals(modeEntry)
                    && mPicselfieView.getVisibility() == View.GONE
                    && !FeatureSwitcher.isPortraitupported()) {
                mPicselfieView.setVisibility(View.VISIBLE);
            } else {
                mPicselfieView.setVisibility(View.GONE);
            }*/

            if (item.mModeName.equals(mCurrentModeName)) {
                mCurrentModeType = item.mType; // prize add by zhangguo, for bug#73878
                return;
            }
            mCurrentModeName = item.mModeName;
            mCurrentModeType = item.mType;
            final String[] supportTypes;
            supportTypes = mModeProvider.getModeSupportTypes(mCurrentModeName,
                    mCurrentCameraId);
            //mModePickerManager.updateCurrentModeItem(item);//prize-remnove-hpf-2018-09-04
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShutterManager.updateModeSupportType(mCurrentModeType, supportTypes);
                }
            });
        }
    }

    /*prize-add mirror-huangpengfei-2019-03-02-start*/
    @Override
    public void setMirrorEnable(boolean enable) {
        if (mThumbnailViewManager != null) {
            mThumbnailViewManager.setMirrorEnable(enable);
        }
    }
    /*prize-add mirror-huangpengfei-2019-03-02-end*/

    /*prize-add-huangpengfei-20181119-start*/
    @Override
    public void setDefaultShutterIndex(){
        mShutterManager.setDefaultShutterIndex();
    }
    /*prize-add-huangpengfei-20181119-end*/

    @Override
    public void setPreviewSize(final int width, final int height,
                               final ISurfaceStatusListener listener) {
        LogHelper.d(TAG, "[setPreviewSize]");

        mPreviewWidth = width;
        mPreviewHeight = height;

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewManager.updatePreviewSize(width, height, listener);
                if(null != mPrizeCircleView){
                    mPrizeCircleView.updatePosition(getPreviewArea());
                }
				mGridManager.updatePreviewSize(width,height);
            }
        });
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     * @return Return <code>true</code> to prevent this event from being propagated
     * further, or <code>false</code> to indicate that you have not handled
     * this event and it should continue to be propagated.
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void showScreenHint(final HintInfo info) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOnScreenHintManager.showScreenHint(info);
            }
        });
    }

    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
    @Override
    public void showScreenHint(HintInfo info, int topmargin) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOnScreenHintManager.showScreenHint(info,topmargin);
            }
        });
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        String infoString = mApp.getActivity().getString(R.string.switch_cameras_frequently);
        if (infoString.equals(info.mHintText)) {
            mConfigUIHandler.sendEmptyMessageDelayed(SET_CAMERA_SWITCH_ENABLE,3000);
        }
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
    }
    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
    @Override
    public void hideScreenHint(final HintInfo info) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOnScreenHintManager.hideScreenHint(info);
            }
        });

    }

    @Override
    public ViewGroup getModeRootView() {
        return (ViewGroup) mApp.getActivity()
                .findViewById(R.id.feature_root);
    }

    @Override
    public View getShutterRootView() {
        if (mShutterManager != null) {
            return mShutterManager.getShutterRootView();
        }
        return null;
    }

    @Override
    public PreviewFrameLayout getPreviewFrameLayout() {
        return mPreviewManager.getPreviewFrameLayout();
    }

    @Override
    public void onPreviewStarted(final String previewCameraId) {
        LogHelper.d(TAG, "onPreviewStarted previewCameraId = " + previewCameraId);
        if (previewCameraId == null) {
            return;
        }
        synchronized (mCurrentCameraId) {
            mCurrentCameraId = previewCameraId;
        }

        //mModePickerManager.onPreviewStarted(previewCameraId);//prize-modify-huangpengfei-2018-12-19
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAnimationManager.onPreviewStarted();

                // prize add by zhangguo, for bug#74490 circleview not show begin
                if(null != mPrizeCircleView){
                    mPrizeCircleView.updatePosition(getPreviewArea());
                }
                // prize add by zhangguo, for bug#74490 circleview not show end
                mGridManager.onPreviewStart();//prize-add-huangpengfei-2018-12-17
                mPrizeModeAnimation.hideModeCoverAnimation();//prize-add for external intent-huangpengfei-2019-2-16
            }
        });
        /*prize-modify-reset the state of capturefaileonhd after start preview-xiaoping-20190325-start*/
        if ("0".equals(previewCameraId) && isCaptureFaileOnHD) {
            isCaptureFaileOnHD = false;
        }
        /*prize-modify-reset the state of capturefaileonhd after start preview-xiaoping-20190325-end*/
    }

    @Override
    public void onCameraSelected(final String cameraId) {
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
		LogHelper.i(TAG,"[onCameraSelected]  cameraId: "+cameraId+",mCurrentCameraId: "+mCurrentCameraId);
        if (mCurrentCameraId.equals(cameraId)) {
            return;
        }
        /*prize-add mirror fixbug[72686]-huangpengfei-2019-03-02-start*/
        if ("0".equals(cameraId) && mThumbnailViewManager != null){
            mThumbnailViewManager.setMirrorEnable(false);
        }
        /*prize-add mirror fixbug[72686]-huangpengfei-2019-03-02-end*/

        /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-start*/
        List<String> cameraidlist = Arrays.asList(cameraId);
        mApp.getModeManger().getNewDeviceUsage().updateNeedCloseCameraId(cameraidlist);
        /*prize-modify-bugid:72030-duplicate opencamera will appear in proactive switching mode-xiaoping-20190308-end*/
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
        if (FeatureSwitcher.isLiftCameraSupport() && "0".equals(cameraId) && mConfigUIHandler.hasMessages(CHANGE_TIMERTASK_TIMEINTERVAL)) {
            mConfigUIHandler.removeMessages(CHANGE_TIMERTASK_TIMEINTERVAL);
        }
        if (FeatureSwitcher.isLiftCameraSupport() &&  "1".equals(cameraId)) {
            getMaxValue();
            startReadNodeValue();
        }
        /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
        /*prize-add for model merging-huangpengfei-2019-02-23-start*/
        mConfigUIHandler.sendEmptyMessage(SHOW_COVER);
        /*prize-add for model merging-huangpengfei-2019-02-23-end*/

        /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-start*/
        if (Integer.valueOf(cameraId) == 0  && Integer.valueOf(mCurrentCameraId) == 1) {
            isOnSwitchCamera = true;
        }
        /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-end*/
        synchronized (mCurrentCameraId) {
            mCurrentCameraId = cameraId;
        }
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*prize-remove for model merging-huangpengfei-2019-02-23-start*/
                //mShutterManager.onCameraSelected(Integer.valueOf(cameraId));
                selectMode(mCurrentModeType);
                /*prize-remove for model merging-huangpengfei-2019-02-23-end*/

                /*prize-modify-bugid:67421 the picselfie icon overlap after switch camera-xiaoping-20181023-start*/
                /*if (Integer.valueOf(cameraId) == 0 && mPicselfieView.getVisibility() == View.VISIBLE) {
                    mPicselfieView.setVisibility(View.GONE);
                    //prize-modify-add switches to third-party algorithms-xiaoping-20181026-start
                } else if (FeatureSwitcher.isArcsoftSelfieSupported() && !FeatureSwitcher.isFaceBeautyupported()
                        && Integer.valueOf(cameraId) == 1
                        && mPicselfieView.getVisibility() == View.GONE
                        && !FeatureSwitcher.isPortraitupported()) {
                    mPicselfieView.setVisibility(View.VISIBLE);
                }*/
                //prize-modify-bugid:67421 the picselfie icon overlap after switch camera-xiaoping-20181023-end
            }
        });
        /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
        //mModePickerManager.onPreviewStarted(cameraId);//prize-modify-huangpengfei-2018-12-19
    }

    /*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/
    @Override
    public void setAiEnable(boolean enable) {
        if (mIDeviceController != null){
            mIDeviceController.setAiEnable(enable);
        }
    }
	
	@Override
    public void setDeviceContrl(IDeviceController deviceContrl) {
        mIDeviceController = deviceContrl;
        LogHelper.d(TAG, "[setDeviceContrl] mNeedDelay = " + mNeedDelay);
        if (mNeedDelay){
            mIDeviceController.onCloseSceneMode(true);
        }else{
            mIDeviceController.onCloseSceneMode(false);
        }
    }
	/*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/

    @Override
    public IVideoUI getVideoUi() {
        return new VideoUI(mApp, getModeRootView());
    }

    @Override
    public IReviewUI getReviewUI() {
        ViewGroup appUI = (ViewGroup) mApp.getActivity().findViewById(R.id.app_ui);
        ViewGroup reviewRoot = (ViewGroup) appUI.getChildAt(0);
        return new ReviewUI(mApp, reviewRoot);
    }

    @Override
    public IIntentPhotoUi getPhotoUi() {
        return new IntentPhotoUi(mApp.getActivity(), getModeRootView(), this);
    }

    @Override
    public void animationStart(final AnimationType type, final AnimationData data) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAnimationManager.animationStart(type, data);
            }
        });
    }

    @Override
    public void animationEnd(final AnimationType type) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAnimationManager.animationEnd(type);
            }
        });
    }

    @Override
    public void setUIVisibility(int module, int visibility) {
        LogHelper.d(TAG, "setUIVisibility + module " + module + " visibility " + visibility);
        if (!isMainThread()) {
            Message msg = Message.obtain();
            msg.arg1 = module;
            msg.arg2 = visibility;
            msg.what = SET_UI_VISIBILITY;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "setUIVisibility - ");
        } else {
            setUIVisibilityImmediately(module, visibility);
        }

    }

    @Override
    public void setUIEnabled(int module, boolean enabled) {
        LogHelper.d(TAG, "setUIEnabled + module " + module +",isMainThread: "+isMainThread()+ " enabled " + enabled);
        if (!isMainThread()) {
            Message msg = Message.obtain();
            msg.arg1 = module;
            msg.arg2 = enabled ? 1 : 0;
            msg.what = SET_UI_ENABLED;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "setUIEnabled - ");
        } else {
            setUIEnabledImmediately(module, enabled);
        }
    }


    @Override
    public void applyAllUIVisibility(final int visibility) {
        LogHelper.d(TAG, "applyAllUIVisibility + visibility " + visibility);
        if (!isMainThread()) {
            Message msg = Message.obtain();
            msg.arg1 = visibility;
            msg.what = APPLY_ALL_UI_VISIBILITY;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "applyAllUIVisibility -");
        } else {
            applyAllUIVisibilityImmediately(visibility);
        }
    }

    @Override
    public void applyAllUIEnabled(final boolean enabled) {
        LogHelper.d(TAG, "applyAllUIEnabled + enabled " +enabled+",isMainThread: "+isMainThread());
        if (!isMainThread()) {
            Message msg = Message.obtain();
            msg.arg1 = enabled ? 1 : 0;
            msg.what = APPLY_ALL_UI_ENABLED;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "applyAllUIEnabled -");
        } else {
            applyAllUIEnabledImmediately(enabled);
        }
    }

    private void setUIVisibilityImmediately(int module, int visibility) {
        LogHelper.d(TAG, "setUIVisibilityImmediately + module " + module
                                                + " visibility " + visibility);
        configUIVisibility(module, visibility);
    }

    private void setUIEnabledImmediately(int module, boolean enabled) {
        LogHelper.d(TAG, "setUIEnabledImmediately + module " + module + " enabled " + enabled);
        configUIEnabled(module, enabled);
    }

    @Override
    public void updateBrightnessBackGround(boolean visible) {
        LogHelper.d(TAG, "setBackgroundColor visible = " + visible);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 View rootView = mApp.getActivity()
                         .findViewById(R.id.brightness_view);
                 if (visible) {
                     rootView.setVisibility(View.VISIBLE);
                 } else {
                     rootView.setVisibility(View.GONE);
                 }
            }
        });
    }

    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    @Override
    public void updateScreenView(boolean show) {
        LogHelper.i(TAG,"[updateScreenView] show: " + show + "  "+android.util.Log.getStackTraceString(new Throwable()));
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.i(TAG,"[updateScreenView] : run");
                if (mScreenFlashView == null){
                    mScreenFlashView = mApp.getActivity()
                            .findViewById(R.id.brightness_view);
                    mScreenFlashView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            return true;
                        }
                    });
                }
                if (show){
                    mScreenFlashView.setVisibility(View.VISIBLE);
                    mScreenFlashView.setBackgroundResource(R.color.screen_light_blinck);
                    /*mScreenFlashView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScreenFlashView.setBackgroundResource(R.color.screen_light_blinck);
                        }
                    },600);*/
                }else {
                    mScreenFlashView.setVisibility(View.GONE);
                }
            }
        });
    }
    /*prize-add-screen flash-huangzhanbin-20190226-end*/

    private void applyAllUIVisibilityImmediately(int visibility) {
        LogHelper.d(TAG, "applyAllUIVisibilityImmediately + visibility " + visibility);
        mConfigUIHandler.removeMessages(APPLY_ALL_UI_VISIBILITY);
        for (int count = 0; count < mViewManagers.size(); count++) {
            mViewManagers.get(count).setVisibility(visibility);
        }
        getPreviewFrameLayout().setVisibility(visibility);
        mOnScreenHintManager.setVisibility(visibility);
        /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-start*/
        if (isThirdPartyIntent()){
            mSettingButton.setVisibility(View.INVISIBLE);
            /*prize-modify-The third-party app calls the camera to take a photo and the camera icon is not hidden-xiaoping-20190304-start*/
            setCameraSwitchVisible(visibility);
            /*prize-modify-The third-party app calls the camera to take a photo and the camera icon is not hidden-xiaoping-20190304-end*/
        }else{
            // zhangguo modify, for bug#75100, panomode hide settings icon
            if(null != getModeItem() && getModeItem().mModeTitle == ModeTitle.PANO){
                mSettingButton.setVisibility(View.INVISIBLE);
            }else{
                mSettingButton.setVisibility(visibility);
            }

        }
        /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-end*/
        if (visibility == View.GONE) {
            mQuickSwitcherManager.hideQuickSwitcherImmediately();
        }
    }

//prize-added by tangan-custom ui-begin
    @Override
    public void addCameraSwitch(View view) {
        RelativeLayout camera_switch = (RelativeLayout) mApp.getActivity().findViewById(R.id.camera_switch);
        // prize modify by zhangugo 20190419, for bug#74647 switch icon is gone when slowmotionmode pauseactivity then back to camera start
        if(view != null){
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    camera_switch.removeAllViews();
                    camera_switch.addView(view);
                    mCameraSwitchView = view;
                }
            });
        }
        // prize modify by zhangugo 20190419, for bug#74647 switch icon is gone when slowmotionmode pauseactivity then back to camera end
    }

    @Override
    public void setCameraSwitchVisible(int visibility) {
        LogHelper.d(TAG, "[setCameraSwitchVisible] visibility = " + visibility);
        RelativeLayout camera_switch = (RelativeLayout) mApp.getActivity().findViewById(R.id.camera_switch);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                camera_switch.setVisibility(visibility);
            }
        });
    }

    /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-start*/
    private boolean canShowCameraSwitcher(){
        if (getModeItem().mModeTitle == ModeTitle.LOWLIGHT || getModeItem().mModeTitle == ModeTitle.PICTUREZOOM ||
                getModeItem().mModeTitle == ModeTitle.PROFESSIONAL ||
                getModeItem().mModeTitle == ModeTitle.HDR || getModeItem().mModeTitle == ModeTitle.PANO ||
                getModeItem().mModeTitle == ModeTitle.SMARTSCAN){
            return false;
        }else{
            return true;
        }
    }
    /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-end*/

    public void setCameraSwitchEnabled(boolean  enabled) {
        LogHelper.i(TAG,"enable: "+enabled);
        RelativeLayout camera_switch = (RelativeLayout) mApp.getActivity().findViewById(R.id.camera_switch);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i= 0;i<camera_switch.getChildCount();i++){
                    camera_switch.getChildAt(i).setEnabled(enabled);

                }
            }
        });
    }
	//prize-added by tangan-custom ui-end

    private void applyAllUIEnabledImmediately(boolean enabled) {
        LogHelper.d(TAG, "applyAllUIEnabledImmediately + enabled " + enabled);
        mConfigUIHandler.removeMessages(APPLY_ALL_UI_ENABLED);
        for (int count = 0; count < mViewManagers.size(); count++) {
            mViewManagers.get(count).setEnabled(enabled);
        }
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
        if (mSettingButton != null) {
            mSettingButton.setEnabled(enabled);
        }
        /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/
		setCameraSwitchEnabled(enabled);//prize-added by tangan-custom ui-begin
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public void clearPreviewStatusListener(final ISurfaceStatusListener listener) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewManager.clearPreviewStatusListener(listener);
            }
        });
    }

    @Override
    public void registerOnPreviewTouchedListener(OnPreviewTouchedListener listener) {

    }

    @Override
    public void unregisterOnPreviewTouchedListener(OnPreviewTouchedListener listener) {

    }

    @Override
    public void registerOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener) {
        mPreviewManager.registerPreviewAreaChangedListener(listener);
    }

    @Override
    public void unregisterOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener) {
        mPreviewManager.unregisterPreviewAreaChangedListener(listener);
    }

    @Override
    public void registerGestureListener(OnGestureListener listener, int priority) {
        mGestureManager.registerGestureListener(listener, priority);
    }

    @Override
    public void unregisterGestureListener(OnGestureListener listener) {
        mGestureManager.unregisterGestureListener(listener);
    }

    @Override
    public void registerOnShutterButtonListener(OnShutterButtonListener listener, int priority) {
        mShutterManager.registerOnShutterButtonListener(listener, priority);
    }

    @Override
    public void unregisterOnShutterButtonListener(OnShutterButtonListener listener) {
        mShutterManager.unregisterOnShutterButtonListener(listener);
    }

    @Override
    public void setThumbnailClickedListener(OnThumbnailClickedListener listener) {
        if (mThumbnailViewManager != null) {
            mThumbnailViewManager.setThumbnailClickedListener(listener);
        }
    }

    @Override
    public void setModeChangeListener(OnModeChangeListener listener) {
        mModeChangeListener = listener;
    }

    @Override
    public void triggerModeChanged(String newMode) {
        //mModePickerManager.modeChanged(newMode);//prize-modify-huangpengfei-2018-12-19
    }

    @Override
    public void triggerShutterButtonClick(int currentPriority) {
        /*prize-modify-bugid:68322  also take a photo by pressing when saving a continuous photo-xiaoping-20181119-start*/
        if (mShutterManager.getVisibility() == View.VISIBLE && mShutterManager.isEnabled()) {
            mShutterManager.triggerShutterButtonClicked(currentPriority);
        }
        /*prize-modify-bugid:68322  also take a photo by pressing when saving a continuous photo-xiaoping-20181119-end*/
    }

    @Override
    public void triggerShutterButtonLongPressed(int currentPriority) {
        mShutterManager.triggerShutterButtonLongPressed(currentPriority);
    }

    /*prize-add-huangpengfei-20181121-start*/
    @Override
    public void stopContinuousShot() {
        mShutterManager.stopContinuousShot();
    }

    @Override
    public void onContinuousShotStarted() {
        mIDeviceController.pauseAi(true);
    }

    @Override
    public void onContinuousShotStopped() {
        mIDeviceController.pauseAi(false);
    }
    /*prize-add-huangpengfei-20181121-end*/

    @Override
    public void addToQuickSwitcher(View view, int priority) {
        mQuickSwitcherManager.addToQuickSwitcher(view, priority);
    }

    @Override
    public void removeFromQuickSwitcher(View view) {
        mQuickSwitcherManager.removeFromQuickSwitcher(view);
    }

    @Override
    public void addToIndicatorView(View view, int priority) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.addToIndicatorView(view, priority);
            }
        });
    }

    @Override
    public void removeFromIndicatorView(View view) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.removeFromIndicatorView(view);
            }
        });
    }

    @Override
    public void addSettingView(ICameraSettingView view) {
        mSettingFragment.addSettingView(view);
    }

    @Override
    public void removeSettingView(ICameraSettingView view) {
        mSettingFragment.removeSettingView(view);
    }

    @Override
    public void refreshSettingView() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSettingFragment.refreshSettingView();
            }
        });
    }

    @Override
    public void updateSettingIconVisibility() {
        boolean visible = mSettingFragment.hasVisibleChild();
        /*prize-modify-huangpengfei-2018-12-19-start*/
        //mModePickerManager.setSettingIconVisible(visible);
        if (isThirdPartyIntent()){
            mSettingButton.setVisibility(View.INVISIBLE);
        }else{
            mSettingButton.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
        }
        /*prize-modify-huangpengfei-2018-12-19-end*/
    }

    @Override
    public void showSavingDialog(String message, boolean isNeedShowProgress) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup root = (ViewGroup) mApp.getActivity().getWindow().getDecorView();
                TextView text;
                if (mSavingDialog == null) {
                    mSavingDialog = (ViewGroup) mApp.getActivity().getLayoutInflater()
                            .inflate(R.layout.rotate_dialog, root, false);
                    View progress = mSavingDialog.findViewById(R.id.dialog_progress);
                    text = (TextView) mSavingDialog.findViewById(R.id.dialog_text);
                    if (isNeedShowProgress) {
                        progress.setVisibility(View.VISIBLE);
                    } else {
                        progress.setVisibility(View.GONE);
                    }
                    if (message != null) {
                        text.setText(message);
                    } else {
                        text.setText(R.string.saving_dialog_default_string);
                    }
                    root.addView(mSavingDialog);
                    int orientation = mApp.getGSensorOrientation();
                    if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                        int compensation = CameraUtil.getDisplayRotation(mApp.getActivity());
                        orientation = orientation + compensation;
                        CameraUtil.rotateViewOrientation(mSavingDialog, orientation, false);
                    }
                    mSavingDialog.setVisibility(View.VISIBLE);
                } else {
                    text = (TextView) mSavingDialog.findViewById(R.id.dialog_text);
                    text.setText(message);
                }
            }
        });
    }
    @Override
    public void hideSavingDialog() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSavingDialog != null) {
                    ViewGroup root = (ViewGroup) mApp.getActivity().getWindow().getDecorView();
                    mSavingDialog.setVisibility(View.GONE);
                    root.removeView(mSavingDialog);
                    mSavingDialog = null;
                }
            }
        });
    }

    @Override
    public void setEffectViewEntry(View view) {
        mEffectViewManager.setViewEntry(view);
    }

    @Override
    public void attachEffectViewEntry() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEffectViewManager.attachViewEntry();
            }
        });
    }

    @Override
    public void showQuickSwitcherOption(View optionView) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuickSwitcherManager.showQuickSwitcherOption(optionView);
            }
        });
    }

    @Override
    public void hideQuickSwitcherOption() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuickSwitcherManager.hideQuickSwitcherOption();
            }
        });
    }

    protected void removeTopSurface() {
        mPreviewManager.removeTopSurface();
    }

    private void layoutSettingUI() {
        LinearLayout settingRootView = (LinearLayout) mApp.getActivity()
                .findViewById(R.id.setting_ui_root);
        if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
            // Get the preview height don't contain navigation bar height.
            Point size = new Point();
            mApp.getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            LogHelper.d(TAG, "[layoutSettingUI], preview size don't contain navigation:" + size);
            LinearLayout settingContainer = (LinearLayout) settingRootView
                    .findViewById(R.id.setting_container);
            LinearLayout.LayoutParams containerParams =
                    (LinearLayout.LayoutParams) settingContainer.getLayoutParams();
            containerParams.height = size.y;
            settingContainer.setLayoutParams(containerParams);

            LinearLayout settingTail = (LinearLayout) settingRootView
                    .findViewById(R.id.setting_tail);
            //get navigation bar height.
            int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
            LogHelper.d(TAG, "[layoutSettingUI], navigationBarHeight:" + navigationBarHeight);
            //set setting tail view height as navigation bar height.
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) settingTail.getLayoutParams();
            params.height = navigationBarHeight;
            settingTail.setLayoutParams(params);
        }
    }

    /*prize-change-hpf-2018-09-04-start*/
    private boolean mIsAnimationFinish = true;
    private void showSetting() {
        /*prize-modify-bugid 71670 optimize UI Caton of ProfessionalMode-xiaoping-20190221-start*/
        if (mCurrentModeItem.mModeTitle == ModeTitle.PROFESSIONAL) {
            setProfessionalViewVisibility(false);
            if (mProgressLayout != null) {
                mProgressLayout.setVisibility(View.GONE);
            }

        }
        /*prize-modify-bugid 71670 optimize UI Caton of ProfessionalMode-xiaoping-20190221-end*/
        LogHelper.d(TAG, "[showSetting]");
        View settingUiRoot = mApp.getActivity().findViewById(R.id.setting_ui_root);
        if (settingUiRoot.getVisibility() == View.VISIBLE){
            return;
        }
        FragmentTransaction transaction = mApp.getActivity().getFragmentManager()
                .beginTransaction();
        transaction.addToBackStack("setting_fragment");
        transaction.replace(R.id.setting_container, mSettingFragment, "Setting")
                .commitAllowingStateLoss();
        View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
        if (!mIsAnimationFinish) return;
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
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
                view.clearAnimation();
                /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
                isSettingViewShow = true;
                /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/
            }
        });
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        view.startAnimation(animationSet);
        mIsAnimationFinish =false;
    }

    public void hideSetting() {
        LogHelper.d(TAG, "[hideSetting]");
        /*mApp.getActivity().getFragmentManager().popBackStackImmediate("setting_fragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE);*/
        View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
        if (view.getVisibility() == View.GONE){
            return;
        }
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
                view.clearAnimation();
                if (stateSaved) return;//prize-add for bug[70653]-huangpengfei-2019-01-16
                mApp.getActivity().getFragmentManager().popBackStackImmediate("setting_fragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                /*prize-modify-bugid 71670 optimize UI Caton of ProfessionalMode-xiaoping-20190221-start*/
                if (mArcProgressBarLayout != null && mCurrentModeItem.mModeTitle == ModeTitle.PROFESSIONAL && isNeedShowProgressBar) {
                    setProfessionalViewVisibility(true);
                    if (mProgressLayout != null) {
                        mProgressLayout.setVisibility(View.VISIBLE);
                    }
                }
                /*prize-modify-bugid 71670 optimize UI Caton of ProfessionalMode-xiaoping-20190221-end*/
                /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
                isSettingViewShow = false;
                /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/
            }
        });
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        view.startAnimation(animationSet);
        mIsAnimationFinish =false;
        /*prize-modify-bugid:72247 The prompt is not updated after changing the storage location-xiaoping-20190307-start*/
        mApp.getModeManger().getCameraContext().getStorageService().updateStorageFullHint();
        /*prize-modify-bugid:72247 The prompt is not updated after changing the storage location-xiaoping-20190307-end*/
    }
    /*prize-change-hpf-2018-09-04-end*/

    private void hideAlertDialog() {
        CameraUtil.hideAlertDialog(mApp.getActivity());
    }

    /*prize-modify for model merging-huangpengfei-2019-03-25-start*/
    private void selectMode(String newShutterType){
/*
        String picturezoom = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom);
        if (SystemProperties.getInt("ro.pri.current.project",0) == 1)  {
            picturezoom = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom_bl);
        }

        if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_beauty))) {
            mModeChangeListener.onModeSelected(FaceBeautyModeEntry.class.getName());
        } else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo))) {
            if(("Photo").equals(mCurrentModeName)){
                mModeChangeListener.onModeSelected(IntentPhotoModeEntry.class.getName());
            }else{
                mModeChangeListener.onModeSelected(PhotoModeEntry.class.getName());
            }
        } else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_video))) {

            if(("Video").equals(mCurrentModeName)){
                mModeChangeListener.onModeSelected(IntentVideoModeEntry.class.getName());
            }else{
                mModeChangeListener.onModeSelected(VideoModeEntry.class.getName());
            }
        } else if (shutterType.equals(picturezoom)) { //prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start
            mModeChangeListener.onModeSelected(PictureZoomModeEntry.class.getName());
        } else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_lowlight))) {
            mModeChangeListener.onModeSelected(LowLightModeEntry.class.getName());
        } else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picsefile))) {
            mModeChangeListener.onModeSelected(PicselfieModeEntry.class.getName());
        } else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.pref_camera_professional_title))) {
            mModeChangeListener.onModeSelected(ProfessionalModeEntry.class.getName());
        }else if (shutterType.equals((String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_aperture))) {
            mModeChangeListener.onModeSelected(SdofPhotoEntry.class.getName());
        }
*/
		
		
		if (newShutterType.equals("FaceBeauty")) {
                mModeChangeListener.onModeSelected(FaceBeautyModeEntry.class.getName());
		} else if (newShutterType.equals("Picture")) {
            if(("Photo").equals(mCurrentModeName)){
                if(isThirdPartyIntent() && getModeItem().mModeTitle != ModeTitle.INTENTPHOTO){
                    mModeChangeListener.onModeSelected(IntentPhotoModeEntry.class.getName());
                }
            }else{
                mModeChangeListener.onModeSelected(PhotoModeEntry.class.getName());
            }
        } else if (newShutterType.equals("Video")) {

            if(("Video").equals(mCurrentModeName)){
                mModeChangeListener.onModeSelected(IntentVideoModeEntry.class.getName());
            }else{
                mModeChangeListener.onModeSelected(VideoModeEntry.class.getName());
            }
        } else if (newShutterType.equals("UHD")) { //prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start
            mModeChangeListener.onModeSelected(PictureZoomModeEntry.class.getName());
        } else if (newShutterType.equals("LowLight")) {
            mModeChangeListener.onModeSelected(LowLightModeEntry.class.getName());
        } else if (newShutterType.equals("Picselfie")) {
            mModeChangeListener.onModeSelected(PicselfieModeEntry.class.getName());
        } else if (newShutterType.equals("Professional")) {
            mModeChangeListener.onModeSelected(ProfessionalModeEntry.class.getName());
        }else if (newShutterType.equals("SdofPicture")) {
            mModeChangeListener.onModeSelected(SdofPhotoEntry.class.getName());
        }else if(newShutterType.equals("HDR")){
            mModeChangeListener.onModeSelected(HdrModeEntry.class.getName());
        }else if(newShutterType.equals("SmartScan")){
            mModeChangeListener.onModeSelected(SmartScanModeEntry.class.getName());
        }else if(newShutterType.equals("Gif")){
            mModeChangeListener.onModeSelected(GifModeEntry.class.getName());
        }else if(newShutterType.equals("Filter")){
            mModeChangeListener.onModeSelected(FilterModeEntry.class.getName());
        }else if(newShutterType.equals("SlowMotion")){
            mModeChangeListener.onModeSelected(SlowMotionEntry.class.getName());
        }else if(newShutterType.equals("PrizePano")){
            mModeChangeListener.onModeSelected(PanoModeEntry.class.getName());
        }
    }
    /*prize-modify for model merging-huangpengfei-2019-03-25-end*/

    /**
     * Shutter change listener implementer.
     */
    private class OnShutterChangeListenerImpl implements
                                           ShutterButtonManager.OnShutterChangeListener {

        @Override
        public void onShutterTypeChanged(String newShutterType) {
            mCurrentModeType = newShutterType;
            ModeItem item = mModeProvider.getModeEntryName(mCurrentModeName, mCurrentModeType);
            selectMode(newShutterType);
//            mModeChangeListener.onModeSelected(item.mClassName);
            /*prize-add-add mode-xiaoping-20180901-end*/
			/*prize-add AI CAMERA-huangpengfei-2019-01-29-start*/
            mPortraitCallForAiTarget = false;
            if (mPrizeModeAnimation != null){
                mPrizeModeAnimation.hideSceneText();
            }
            LogHelper.d(TAG, "[onShutterTypeChanged]  CAMEAR-AI   mPortraitCallForAiTarget = " + mPortraitCallForAiTarget);
        }
    }

    @Override
    public void switchPortraitMode() {
        LogHelper.d(TAG, "[switchPortraitMode]  CAMEAR-AI   mPortraitCallForAiTarget = " + mPortraitCallForAiTarget);
        if (mPrizeModeAnimation != null && mModeChangeListener != null){

            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPrizeModeAnimation.updateSceneModeTitle(R.string.shutter_type_photo_picsefile);
                    mPrizeModeAnimation.showSceneModeTextAnimation();

                    if(null != getModeItem() && getModeItem().mModeTitle != ModeTitle.FICSEFILE){
                        mModeChangeListener.onModeSelected(PicselfieModeEntry.class.getName());
                    }
                }
            });
        }else{
            LogHelper.d(TAG, "[switchPortraitMode]  CAMEAR-AI   mPrizeModeAnimation = " +
                    mPrizeModeAnimation + "  mModeChangeListener = " + mModeChangeListener);
        }
    }

    @Override
    public void switchPhotoMode(){
        LogHelper.d(TAG, "[switchPhotoMode]  CAMEAR-AI   mPortraitCallForAiTarget = " + mPortraitCallForAiTarget);
        if (mPrizeModeAnimation != null && mModeChangeListener != null){
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPrizeModeAnimation.hideSceneText();

                    if(!FeatureSwitcher.isSupportDualCam()){
                        View picselfie = mQuickSwitcherManager.getQuickSwitcher(PicselfieParameterViewController.PICSELFIE_PRIORITY);
                        if(null != picselfie){
                            Object tag = picselfie.getTag();
                            if(null != tag && tag instanceof String){
                                if("on".equals(tag)){
                                    picselfie.performClick();
                                }
                            }
                        }
                    }

                    if(null != getModeItem() && getModeItem().mModeTitle != ModeTitle.PHOTO){
                        mModeChangeListener.onModeSelected(PhotoModeEntry.class.getName());
                    }
                }
            });

        }else{
            LogHelper.d(TAG, "[switchPhotoMode]  CAMEAR-AI   mPrizeModeAnimation = " +
                    mPrizeModeAnimation + "  mModeChangeListener = " + mModeChangeListener);
        }
    }

    @Override
    public void switchNightMode() {
        LogHelper.d(TAG, "[switchNightMode]  CAMEAR-AI   mPortraitCallForAiTarget = " + mPortraitCallForAiTarget);
        if (mPrizeModeAnimation != null && mModeChangeListener != null){
            mPrizeModeAnimation.updateSceneModeTitle(R.string.shutter_type_photo_lowlight);
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPrizeModeAnimation.showSceneModeTextAnimation();
                }
            });
            mModeChangeListener.onModeSelected(LowLightModeEntry.class.getName());
        }else{
            LogHelper.d(TAG, "[switchNightMode]  CAMEAR-AI   mPrizeModeAnimation = " +
                    mPrizeModeAnimation + "  mModeChangeListener = " + mModeChangeListener);
        }
    }

    @Override
    public void switchHdrMode() {
        Message msg = Message.obtain();
        msg.obj = "on";
        msg.what = SWITCH_HDR;
        mConfigUIHandler.sendMessage(msg);
    }

    @Override
    public void setPortraitCallForAiTarget(boolean target) {
        mPortraitCallForAiTarget = target;
        LogHelper.d(TAG, "[setPortraitCallForAiTarget]  CAMEAR-AI   mPortraitCallForAiTarget = " + mPortraitCallForAiTarget);
    }

    @Override
    public boolean getPortraitCallForAiTarget() {
        return mPortraitCallForAiTarget;
    }
	/*prize-add AI CAMERA-huangpengfei-2019-01-29-end*/

    /**
     * Setting state listener implementer.
     */
    private class SettingStateListener implements SettingFragment.StateListener {

        @Override
        public void onCreate() {
            View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
            view.setVisibility(View.VISIBLE);
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-start*/
            setCameraSwitchVisible(View.GONE);
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-end*/
            applyAllUIVisibility(View.GONE);
            setUIEnabled(SHUTTER_BUTTON, false);
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onDestroy() {
            View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
            view.setVisibility(View.GONE);
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-start*/
            if (canShowCameraSwitcher()){
                setCameraSwitchVisible(View.VISIBLE);
            }
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-end*/
            applyAllUIVisibility(View.VISIBLE);
            setUIEnabled(SHUTTER_BUTTON, true);
        }
    }

    /**
     * Implementer of onTouch listener.
     */
    private class OnTouchListenerImpl implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mGestureManager != null) {
                Rect rect = new Rect();
                getShutterRootView().getHitRect(rect);
                Configuration config = mApp.getActivity().getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (motionEvent.getRawY() > rect.top) {
                        //If the touch point is below shutter, ignore it.
                        return true;
                    }
                } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (motionEvent.getRawX() > rect.top) {
                        //If the touch point is below shutter, ignore it.
                        return true;
                    }
                }
                /*prize-modify-bugid:74285 Camera front-touch photo camera is not implemented-xiaoping-2019041-start*/
                // modify by zhangguo, for bug#74604, capture when touchs up
                /*prize-modify for bug[74777]-huangpengfei-20190424-start*/
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && /*mCameraId == 1 &&*/ "on".equals(mApp.getSettingValue("key_touch_shutter","off",mCameraId)) && mCurrentModeItem.mModeTitle != ModeTitle.VIDEO) {
                    /*prize-modify-feature:k6203vs-can take a photo before the countdown ends-xiaoping-20190417-start*/
                    boolean canTriggerShutter = false;
                    for (int count = 0; count < mViewManagers.size(); count++) {
                        AbstractViewManager vm = (AbstractViewManager) mViewManagers.get(count);
                        if (vm instanceof ShutterButtonManager){
                            canTriggerShutter = vm.getView().isEnabled();
                        }
                    }
                    LogHelper.i(TAG,"[onTouch]  canTriggerShutter = " + canTriggerShutter);
                    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-start*/
                    if (!isZoomState && canTriggerShutter){
                        if (System.currentTimeMillis() - mLastClickTime >400) {
                            mLastClickTime = System.currentTimeMillis();
                            triggerShutterButtonClick(-1);
                        }
                    } else {
                        isZoomState = false;
                    }
                    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-end*/
                    /*prize-modify for bug[74777]-huangpengfei-20190424-end*/
                    /*prize-modify-feature:k6203vs-can take a photo before the countdown ends-xiaoping-20190417-end*/
                }
                /*prize-modify-bugid:74285 Camera front-touch photo camera is not implemented-xiaoping-2019041-end*/
                mGestureManager.getOnTouchListener().onTouch(view, motionEvent);
                if (mPrizeCircleView != null) {
                    mPrizeCircleView.onTouchEvent(motionEvent);

                    if(null != mPicselfieCallback){
                        mPicselfieCallback.onPicselfieDataChanged();
                    }
                }
            }
            return true;
        }
    }

    /**
     *  Implementer of setting button click listener.
     */
    private class OnSettingClickedListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mSettingFragment.hasVisibleChild()) {
                showSetting();
            }
        }
    }

    /**
     * The implementer of OnModeChangeListener.
     */
    private class OnModeChangedListenerImpl implements ModePickerManager.OnModeChangedListener {

        @Override
        public void onModeChanged(String modeName) {
            mCurrentModeName = modeName;
            ModeItem item = mModeProvider.getModeEntryName(mCurrentModeName, mCurrentModeType);
            mCurrentModeType = item.mType; // prize add by zhangguo, for bug#73878
            mModeChangeListener.onModeSelected(item.mClassName);
            //mModePickerManager.updateCurrentModeItem(item);//prize-modify-huangpengfei-2018-12-19
            String[] supportTypes =
                    mModeProvider.getModeSupportTypes(item.mModeName, mCurrentCameraId);
            mShutterManager.updateModeSupportType(mCurrentModeType, supportTypes);
            mShutterManager.updateCurrentModeShutter(item.mType, item.mShutterIcon);
        }
    }

    /**
     * Implementer of OnOrientationChangeListener.
     */
    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            LogHelper.i(TAG,"[onOrientationChanged]  orientation = " + orientation);
            if (mSavingDialog != null) {
                int compensation = CameraUtil.getDisplayRotation(mApp.getActivity());
                orientation = orientation + compensation;
                CameraUtil.rotateViewOrientation(mSavingDialog, orientation, true);
            }
			//prize-tangan-20180921-add prizecamera-begin
            if(mCameraSwitchView != null){
                CameraUtil.rotateViewOrientation(mCameraSwitchView, orientation, true);
            }
            if(mSettingButton != null){
                CameraUtil.rotateViewOrientation(mSettingButton, orientation, true);
            }
            /*prize-add AI CAMERA-huangpengfei-2019-01-09-start*/
            if (mIDeviceController != null){
                mIDeviceController.updateGSensorOrientation(orientation);
            }
            /*prize-add AI CAMERA-huangpengfei-2019-01-09-end*/
			//prize-tangan-20180921-add prizecamera-end
        }
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class ConfigUIHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogHelper.d(TAG, "handleMessage what =  " + msg.what);
            switch (msg.what) {
                case APPLY_ALL_UI_VISIBILITY:
                    //call each manager's setVisibility()
                    int visibility = msg.arg1;
                    LogHelper.d(TAG, "[handleMessage] visibility = " + visibility);
                    for (int count = 0; count < mViewManagers.size(); count++) {
                        mViewManagers.get(count).setVisibility(visibility);
                    }
                    getPreviewFrameLayout().setVisibility(visibility);
                    mOnScreenHintManager.setVisibility(visibility);
                    /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-start*/
                    if (isThirdPartyIntent()){
                        mSettingButton.setVisibility(View.INVISIBLE);
                        /*prize-modify-The third-party app calls the camera to take a photo and the camera icon is not hidden-xiaoping-20190304-start*/
                        setCameraSwitchVisible(visibility);
                        /*prize-modify-The third-party app calls the camera to take a photo and the camera icon is not hidden-xiaoping-20190304-end*/
                    }else{
                        mSettingButton.setVisibility(visibility);
                    }
                    /*prize-modify-bugid:67061  not allowed to click the setting item when recording video-xiaoping-20181025-end*/
                    if (visibility == View.GONE) {
                        mQuickSwitcherManager.hideQuickSwitcherImmediately();
                    }
                    break;
                case APPLY_ALL_UI_ENABLED:
                    //call each manager's setEnabled()
                    boolean enabled = msg.arg1 == 1;
                    for (int count = 0; count < mViewManagers.size(); count++) {
                        mViewManagers.get(count).setEnabled(enabled);
                    }
                    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-start*/
                    if (mSettingButton != null) {
                        mSettingButton.setEnabled(enabled);
                    }
                    /*prize-modify-other buttons are not clickable when taking pictures-xiaoping-20181030-end*/
                    if (mLiftingCount != LiftCamera.CONDITIONNUMBER) { //prize-modify-add front lift camera interaction-xiaoping-20190304
                        setCameraSwitchEnabled(enabled);//prize-added by tangan-custom ui-begin
                    }
                    break;
                case SET_UI_VISIBILITY:
                    configUIVisibility(msg.arg1, msg.arg2);
                    break;
                case SET_UI_ENABLED:
                    configUIEnabled(msg.arg1, msg.arg2 == 1);
                    break;
                /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
                case SET_CAPTUREANIMATION_VISIBILITY:
                    if (mShutterManager.getCaptureAnimation() != null && mShutterManager.getCaptureAnimation().getVisibility() == View.VISIBLE) {
                        mShutterManager.getCaptureAnimation().stop();
                        mShutterManager.getCaptureAnimation().setVisibility(View.GONE);
                        configUIVisibility(IAppUi.SHUTTER_BUTTON,View.VISIBLE);
                        mShutterManager.removeMessageOfStopAnimation();
                    }
                    break;
                /*prize-modify-add animation of takepictur-xiaoping-20181105-end*/
				/*prize-add AI CAMERA-huangpengfei-2019-01-29-start*/
                case SWITCH_HDR:
                    String value = (String) msg.obj;
                    if (!mHdrValue.equals(value) && mPrizeModeAnimation != null && mQuickSwitcherManager != null){
                        mQuickSwitcherManager.switchHdr();
                    }else{
                        LogHelper.d(TAG, "[switchNightMode]  CAMEAR-AI   mPrizeModeAnimation = " +
                                mPrizeModeAnimation + "  mQuickSwitcherManager = " + mQuickSwitcherManager);
                    }
                    break;
				/*prize-add AI CAMERA-huangpengfei-2019-01-29-end*/
                /*prize-add for model merging-huangpengfei-2019-02-23-start*/
                case SHOW_COVER:
                    mPrizeModeAnimation.showSwitchCameraAnimation1();
                    break;
                /*prize-add for model merging-huangpengfei-2019-02-23-end*/
                /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
                case PRESSCAMERA:
                    mImplLifaCamera.pressCamera();
                    break;
                case PREVENTCAMERA:
                    mImplLifaCamera.preventCamera();
                    break;
                case SET_CAMERA_SWITCH_ENABLE:
                    configUIEnabled(IAppUi.CAMERA_SWITCHER,true);
                    mLiftingCount = 0;
                    break;
                case STOP_READ_NODEVALUE:
                    stopReadNodeValue();
                    break;
                case CHANGE_TIMERTASK_TIMEINTERVAL:
                    changeTimeInterval();
                    break;
                /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
                default:
                    break;
            }
        }
    }

    private void configUIVisibility(int module, int visibility) {
        LogHelper.d(TAG, "configUIVisibility + module " + module + " visibility " + visibility);
        switch (module) {
            case QUICK_SWITCHER:
                mQuickSwitcherManager.setVisibility(visibility);
                break;
            case MODE_SWITCHER:
                //mModePickerManager.setVisibility(visibility);//prize-modify-huangpengfei-2018-12-19
                break;
            case THUMBNAIL:
                if (mThumbnailViewManager != null) {
                    mThumbnailViewManager.setVisibility(visibility);
                    /*prize-modify-bug:71385-the photo imaging interface also displays a thumbnail icon-xiaoping-20190219-start*/
                } else if (isThirdPartyIntent()) {
                    mApp.getActivity().findViewById(R.id.thumbnail).setVisibility(visibility);
                    /*prize-modify-bug:71385-the photo imaging interface also displays a thumbnail icon-xiaoping-20190219-end*/
                }
                break;
            case SHUTTER_BUTTON:
                mShutterManager.setVisibility(visibility);
                break;
            case INDICATOR:
                mIndicatorViewManager.setVisibility(visibility);
                break;
            case PREVIEW_FRAME:
                getPreviewFrameLayout().setVisibility(visibility);
                break;
            case SCREEN_HINT:
                mOnScreenHintManager.setVisibility(visibility);
                break;
                /*prize-add for GridLine -huangpengfei-2018-9-26-start*/
            case GRID:
                mGridManager.setVisibility(visibility);
                break;
                /*prize-add for GridLine -huangpengfei-2018-9-26-end*/
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-start*/
            case CAMERA_SWITCHER:
                LogHelper.d(TAG, "[configUIVisibility] canShowCameraSwitcher() = " + canShowCameraSwitcher());
                if (View.VISIBLE == visibility && canShowCameraSwitcher()) {
                    setCameraSwitchVisible(View.VISIBLE);
                } else {
                    setCameraSwitchVisible(View.INVISIBLE);
                }
                break;
            /*prize-add fixbug[72016][72056]-huangpengfei-2019-02-27-end*/
            default:
                break;
        }
    }

    private void configUIEnabled(int module, boolean enabled) {
        LogHelper.d(TAG, "configUIEnabled + module " + module + " enabled " + enabled);
        switch (module) {
            case QUICK_SWITCHER:
                mQuickSwitcherManager.setEnabled(enabled);
                setCameraSwitchEnabled(enabled);//prize-added by tangan-custom ui-begin
                break;
            case MODE_SWITCHER:
                //mModePickerManager.setEnabled(enabled);//prize-modify-huangpengfei-2018-12-19
                break;
            case THUMBNAIL:
                if (mThumbnailViewManager != null) {
                    mThumbnailViewManager.setEnabled(enabled);
                }
                break;
            case SHUTTER_BUTTON:
                mShutterManager.setEnabled(enabled);
                break;
            case INDICATOR:
                mIndicatorViewManager.setEnabled(enabled);
                break;
            case PREVIEW_FRAME:
                break;
            case GESTURE:
                mPreviewManager.setEnabled(enabled);
                break;
            case SHUTTER_TEXT:
                mShutterManager.setTextEnabled(enabled);
                break;
            /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
            case CAMERA_SWITCHER:
                setCameraSwitchEnabled(enabled);
                break;
            /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/
            default:
                break;
        }
    }

    private void dumpUIState(AppUIState state) {
        if (state != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("mIndicatorEnabled:")
                    .append(state.mIndicatorEnabled)
                    .append(", mIndicatorVisibleState:")
                    .append(state.mIndicatorVisibleState)
                    .append(", mModeSwitcherEnabled:")
                    .append(state.mModeSwitcherEnabled)
                    .append(", mModeSwitcherVisibleState:")
                    .append(state.mModeSwitcherVisibleState)
                    .append(", mQuickSwitcherEnabled:")
                    .append(state.mQuickSwitcherEnabled)
                    .append(", mQuickSwitcherVisibleState: ")
                    .append(state.mQuickSwitcherVisibleState)
                    .append(", mShutterButtonEnabled:")
                    .append(state.mShutterButtonEnabled)
                    .append(", mShutterButtonVisibleState:")
                    .append(state.mShutterButtonVisibleState)
                    .append(", mThumbnailEnabled:")
                    .append(state.mThumbnailEnabled)
                    .append(", mThumbnailVisibleState:")
                    .append(state.mThumbnailVisibleState)
                    .append(", mPreviewFrameVisibleState:")
                    .append(state.mPreviewFrameVisibleState)
                    .toString();
            LogHelper.i(TAG, "[dumpUIState]: " + builder);
        }
    }

    // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
    public IApp.KeyEventListener getKeyEventListener() {
        return new IApp.KeyEventListener() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                if (!CameraUtil.isNeedInitSetting(keyCode)) {
                    return false;
                }
                showSetting();
                hideSetting();
                return false;
            }
        };
    }
    // @}

    /*prize-add-beauty parameter ui-xiaoping-20180911-start*/
    public ViewGroup getSettingRoot() {
        return (ViewGroup) mApp.getActivity()
                .findViewById(R.id.preview_button);
    }
    /*prize-add-beauty parameter ui-xiaoping-20180911-end*/

    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    @Override
    public void updateCameraId(int cameraid) {
        mCameraId = cameraid;
    }

    @Override
    public int getCameraId() {
        return mCameraId;
    }

    @Override
    public ModeItem getModeItem() {
        return mCurrentModeItem;
    }


    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/

    public View getPicselfieView() {
        //return mPicselfieView;
        return null;
    }

    /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-start*/
    public void getScreenPix() {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager)mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metric);

        mScreenPixWidth = metric.widthPixels;
        mScreenPixHeight = metric.heightPixels;
    }

    @Override
    public int getScreenPixHeight() {
        return mScreenPixHeight;
    }

    @Override
    public int getScreenPixWidth() {
        return mScreenPixWidth;
    }
    /*prize-modify-bugid:67064 adjust the position of the focus area-xiaoping-20181102-end*/

    /*prize-modify-add animation of takepicture-xiaoping-20181105-start*/
    @Override
    public void revetButtonState() {
        if (mShutterManager.getCaptureAnimation() != null && mShutterManager.getCaptureAnimation().getVisibility() == View.VISIBLE) {
            Message msg = Message.obtain();
            msg.arg1 = View.GONE;
            msg.what = SET_CAPTUREANIMATION_VISIBILITY;
            mConfigUIHandler.sendMessage(msg);
        }
    }

    @Override
    public String getHdrValue() {
        return mHdrValue;
    }

    @Override
    public void setPicsflieValue(String picsflieValue) {
        mPicsflieValue = picsflieValue;
    }

    @Override
    public String getPicsflieValue() {
        return mPicsflieValue;
    }

    @Override
    public void setHdrValue(String s) {
        mHdrValue = s;
    }
    /*prize-modify-add animation of takepicture-xiaoping-20181105-end*/

    /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-start*/
    @Override
    public boolean isOnSwitchCamera() {
        return isOnSwitchCamera;
    }

    @Override
    public void setSwitchCameraState(boolean state) {
        isOnSwitchCamera = state;
    }
    /*prize-modify-bugid:67812 photo mode shows beauty parameter view-xiaoping-20181109-end*/

    /*prize-modify-get preview radio -xiaoping-20181115-start*/
    @Override
    public double getPreviewAspectRatio() {
        if (mPreviewManager != null) {
            return mPreviewManager.getPreviewAspectRatio();
        }
        LogHelper.i(TAG,"mPreviewManager is null.return default ratio ");
        return 1.3333;
    }
    /*prize-modify-get preview radio -xiaoping-20181115-end*/

    /*prize-add-huangpengfei-2018-12-17-start*/
    public RectF getPreviewArea() {
        return mPreviewManager.getPreviewArea();
    }

    @Override
    public void setVolumekeyState(int state) {
        mShutterManager.setVolumekeyState(state);
    }


    /*prize-modify-bug:71385-the photo imaging interface also displays a thumbnail icon-xiaoping-20190219-start*/
    @Override
    public boolean isThirdPartyIntent() {
        /*prize-modify-bug:71385-the photo imaging interface also displays a thumbnail icon-xiaoping-20190219-end*/
        String action = mApp.getActivity().getIntent().getAction();
        boolean value = MediaStore.ACTION_IMAGE_CAPTURE.equals(action) ||
                MediaStore.ACTION_VIDEO_CAPTURE.equals(action);
        return value;
    }
    /*prize-add-huangpengfei-2018-12-17-end*/

    /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
    @Override
    public boolean isRTL() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }
    /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/

    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/
    @Override
    public boolean isInterruptCaptureOnHD() {
        return isCaptureFaileOnHD;
    }

    @Override
    public void setCaptureStateOnHD(boolean captureState) {
        LogHelper.i(TAG,"captureStateOnPictureZoom: "+captureState);
        isCaptureFaileOnHD = captureState;

    }
    /*prize-modify-bugid:69430 Take a photo and press the HOme button to exit and then get stuck on picturezoom-xiaoping-20190109-start*/

    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-start*/
    @Override
    public void updateCameraCharacteristics() {
        mIDeviceController.updateCharacteristics(String.valueOf(mCameraId));
    }
    /*prize-modify-Take a green screen on picturezoom mode -xiaoping-20190117-end*/

    /*prize-modify-bugid:70831 Switch mode error-xiaoping-20190119-start*/
    @Override
    public boolean isCaptureOrVideo() {
        return isCaptureOrVideo;
    }

    @Override
    public void updateCaptureOrVideoState(boolean isCaptureOrVideo,String capturetype) {
        this.isCaptureOrVideo = isCaptureOrVideo;
        mCaptureType = capturetype;
    }
    /*prize-modify-bugid:70831 Switch mode error-xiaoping-20190119-end*/

    /*prize-add for external intent-huangpengfei-2019-2-16-start*/
    private void IntentSwitchMode() {
        Intent intent = mApp.getActivity().getIntent();
        String actionMode = intent.getStringExtra("action_mode");
        LogHelper.i(TAG,"[IntentSwitchMode] actionMode = " + actionMode);
        if (actionMode != null){
            if (mPrizeModeAnimation != null){
                mPrizeModeAnimation.showCover();
                mPrizeModeAnimation.hideSceneText();
            }
            switch (actionMode){
                case "portrait":
                    startPortraitMode();
                    break;
                case "uhd":
                    startUhdMode();
                    break;
                case "ai":
                    startAiMode();
                    break;
            }
        }
        intent.removeExtra("action_mode");
    }

    private void startUhdMode(){
        LogHelper.i(TAG,"[startUhdMode]");
        if (mCurrentCameraId.equals("0") && getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM){
            LogHelper.i(TAG,"[startUhdMode] return");
            return;
        }
        String modeName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom);
        /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 1) {
            modeName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom_bl);
        }
        /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-end*/
        mShutterManager.updateModeTitle();
        mModeChangeListener.onModeSelected(PictureZoomModeEntry.class.getName());

        mShutterManager.switchToShutterByIntent(modeName);
    }

    private void startPortraitMode(){
        LogHelper.i(TAG,"[startPortraitMode]");
        if (mCurrentCameraId.equals("1") && getModeItem().mModeTitle == ModeTitle.FICSEFILE){
            LogHelper.i(TAG,"[startPortraitMode] return");
            return;
        }
        String modeName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picsefile);
        mShutterManager.updateModeTitle();
        mModeChangeListener.onModeSelected(PicselfieModeEntry.class.getName());
        mShutterManager.switchToShutterByIntent(modeName);
    }

    private void startAiMode(){
        LogHelper.i(TAG,"[startAMode]");
        if (mCurrentCameraId.equals("0") && getModeItem().mModeTitle == ModeTitle.PHOTO){
            LogHelper.i(TAG,"[startAiMode] return");
            return;
        }
        String modeName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo);
        mShutterManager.updateModeTitle();
        mModeChangeListener.onModeSelected(PhotoModeEntry.class.getName());
        mShutterManager.switchToShutterByIntent(modeName);

    }
    /*prize-add for external intent-huangpengfei-2019-2-16-end*/


    /*prize-modify-add professional mode function-xiaoping-20190216-start*/
    @Override
    public void initUI() {
        if (mArcProgressBarLayout != null) {
            return;
        }
        mArcProgressBarLayout = mApp.getActivity().findViewById(R.id.progress_layout);
        mProgressLayout = mApp.getActivity().findViewById(R.id.progress_layout_rlt);
        mProgressControlView = mApp.getActivity().findViewById(R.id.progress_layout_controlview);
        mProgressResetView = mApp.getActivity().findViewById(R.id.progress_layout_reset);
        mProfessionalTitle = mApp.getActivity().findViewById(R.id.professional_title);
        mConcurrentHashMap = new ConcurrentHashMap<>();
        mDataStore = mApp.getModeManger().getCameraContext().getDataStore();
        mWhiteBalanceView = mApp.getActivity().findViewById(R.id.progress1);
        mWhiteBalanceView.setDraggingEnabled(true);
        mWhiteBalanceView.setOnMenuItemClickListener(new PrizeArcProgressBarView.OnMenuItemClickListener() {
            @Override
            public void onItemClick(int item) {
                if (mArcProgressBarLayout.getVisibility() == View.VISIBLE) {
                    mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AWB_MODE,mWhiteBalanceValue[item]);
                    mDataStore.setValue("key_white_balance",mWhiteBalanceStringValues[item],mDataStore.getCameraScope(mCameraId),true);
                    mConcurrentHashMap.put("whiteblance",item);
                }
            }
        });
        mWhiteBalanceView.setAllParameters(1000,4,0,pictures,checkeds,0);

        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mApp.getActivity())
                .getDeviceDescriptionMap().get(String.valueOf(mCameraId));
        mKeyIsoRequestValue = deviceDescription.getKeyIsoRequestMode();
        mIsoView = (PrizeArcProgressBarView)mApp.getActivity().findViewById(R.id.progress2);
        mIsoView.setDraggingEnabled(true);
        mIsoView.setOnMenuItemClickListener(new PrizeArcProgressBarView.OnMenuItemClickListener() {
            @Override
            public void onItemClick(int item) {
                if (mArcProgressBarLayout.getVisibility() == View.VISIBLE) {
                    int[] mode = new int[1];
                    if (!"A".equals(mStrings[item])) {
                        mode[0] = Integer.parseInt(mStrings[item]);
                    } else {
                        mode[0] = Integer.parseInt("0");
                    }
                    mIDeviceController.setParameterRequest(mKeyIsoRequestValue,mode);
                    mDataStore.setValue("key_iso",String.valueOf(mode[0]),mDataStore.getCameraScope(mCameraId),true);
                    mConcurrentHashMap.put("iso",item);
                }
            }
        });
        mIsoView.setAllParameters(1000, 4, 0, mStrings, 1);

        mEvView = (PrizeArcProgressBarView) mApp.getActivity().findViewById(R.id.progress3);
        mEvView.setDraggingEnabled(true);
        mEvView.setOnMenuItemClickListener(new PrizeArcProgressBarView.OnMenuItemClickListener() {
            @Override
            public void onItemClick(int item) {
                if (mArcProgressBarLayout.getVisibility() == View.VISIBLE) {
                    mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(mNumberStrings[item]));
                    mDataStore.setValue("key_exposure",mNumberStrings[item],mDataStore.getCameraScope(mCameraId),true);
                    mConcurrentHashMap.put("ev", item);
                    if (item > 3) {
                        setTextColor(Color.WHITE);
                    } else {
                        setTextColor(Color.WHITE);
                    }
                }
            }
        });
        mEvView.setAllParameters(1000, 6, 3, mNumberStrings, 1);
        mProgressControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNeedShowProgressBar) {
                    setProfessionalViewVisibility(false);
                    isNeedShowProgressBar = false;
                    mProgressControlView.setImageResource(R.drawable.prize_professional_expand);
                } else {
                    setProfessionalViewVisibility(true);
                    isNeedShowProgressBar = true;
                    mProgressControlView.setImageResource(R.drawable.prize_professional_hide);
                }
            }
        });

        mProgressResetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();

            }
        });
    }

    @Override
    public void uninUI() {

    }

    @Override
    public void updateUIState(boolean isShow) {
        if (mArcProgressBarLayout != null) {
            cleaSelected();
            setProfessionalViewVisibility(isShow && isNeedShowProgressBar);
        }
        if (mProgressLayout != null) {
            mProgressLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onItemSelcted(int item) {

    }

    @Override
    public void restoreSelected() {
        if (mConcurrentHashMap == null || mIDeviceController == null || mDataStore == null) {
            return;
        }
        if (mConcurrentHashMap.containsKey("whiteblance")) {
            mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AWB_MODE,mWhiteBalanceValue[mConcurrentHashMap.get("whiteblance")]);
            mDataStore.setValue("key_white_balance",mWhiteBalanceStringValues[mConcurrentHashMap.get("whiteblance")],mDataStore.getCameraScope(mCameraId),true);
        }
        if (mConcurrentHashMap.containsKey("iso")) {
            int[] mode = new int[1];
            if (mConcurrentHashMap.get("iso") == 0) {
                mode[0] = 0;
            } else {
                mode[0] = Integer.parseInt(mStrings[mConcurrentHashMap.get("iso")]);
            }
            mIDeviceController.setParameterRequest(mKeyIsoRequestValue,mode);
            mDataStore.setValue("key_iso",String.valueOf(mode[0]),mDataStore.getCameraScope(mCameraId),true);
        }
        if (mConcurrentHashMap.containsKey("ev")) {
            mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,Integer.valueOf(mNumberStrings[mConcurrentHashMap.get("ev")]));
            mDataStore.setValue("key_exposure",mNumberStrings[mConcurrentHashMap.get("ev")],mDataStore.getCameraScope(mCameraId),true);
        }

    }


    @Override
    public IArcProgressBarUI getArcProgressBarUI() {
        return CameraAppUI.this;
    }

    public void reset() {
        if (mArcProgressBarLayout != null) {
            mWhiteBalanceView.reset(0);
            mIsoView.reset(0);
            mEvView.reset(3);
        }
        cleaSelected();
        if (mConcurrentHashMap != null && mConcurrentHashMap.size() >0) {
            mConcurrentHashMap.clear();
        }

        // zhangguo add for bug#74475 reset settings start
        DataStore ds = mApp.getModeManger().getCameraContext().getDataStore();
        if(null != ds){
            ds.setValue("picsefile_switch", "off", ds.getCameraScope(mCameraId),true);
        }

        if(getModeItem() != null && (getModeItem().mModeTitle == ModeTitle.PHOTO || getModeItem().mModeTitle == ModeTitle.APERTURE)){
            View picselfie = mQuickSwitcherManager.getQuickSwitcher(PicselfieParameterViewController.PICSELFIE_PRIORITY);
            if(null != picselfie && "on".equals(picselfie.getTag())){
                picselfie.performClick();
            }
        }

        if(null != mBlurSeekBar){
            mBlurSeekBar.reset();
        }

        if(null != mSizeSeekBar){
            mSizeSeekBar.reset();
        }

        if(null != mPrizeCircleView){
            mPrizeCircleView.reset();
        }
        // zhangguo add for bug#74475 reset settings end
    }

    private void cleaSelected() {
        if (mDataStore == null || mIDeviceController == null) {
            return;
        }
        mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AWB_MODE,mWhiteBalanceValue[0]);
        mDataStore.setValue("key_white_balance",mWhiteBalanceStringValues[0],mDataStore.getCameraScope(mCameraId),true);
        int[] mode = new int[1];
        mode[0] = 0;
        mIDeviceController.setParameterRequest(mKeyIsoRequestValue,mode);
        mDataStore.setValue("key_iso",String.valueOf(mode[0]),mDataStore.getCameraScope(mCameraId),true);
        mIDeviceController.setParameterRequest(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(mNumberStrings[3]));
        mDataStore.setValue("key_exposure",mNumberStrings[3],mDataStore.getCameraScope(mCameraId),true);
    }

    @Override
    public void clearEffect() {
        cleaSelected();
    }

    private void setTextColor(int color) {
        mIsoView.setTetxtColor(color);
        mEvView.setTetxtColor(color);
    }

    @Override
    public void clearAllEffect() {
        if (mConcurrentHashMap == null) {
            return;
        }
        if (((mConcurrentHashMap.containsKey("whiteblance")) && mConcurrentHashMap.get("whiteblance") != 0)
                || ((mConcurrentHashMap.containsKey("iso")) && mConcurrentHashMap.get("iso") != 0)
                || ((mConcurrentHashMap.containsKey("ev")) && mConcurrentHashMap.get("ev") != 3)) {
            cleaSelected();
        }
    }

    private void setProfessionalViewVisibility(boolean isShow) {
        if (mArcProgressBarLayout != null) {
            mArcProgressBarLayout.setVisibility(isShow ? View.VISIBLE:View.GONE);
        }
        if (mProfessionalTitle != null) {
            mProfessionalTitle.setVisibility(isShow ? View.VISIBLE: View.GONE);
        }
    }
    /*prize-modify-add professional mode function-xiaoping-20190216-end*/

    /*prize-add for model merging-huangpengfei-2019-02-23-start*/
    @Override
    public void cameraSwitchPerformClick() {
        if (mCameraSwitchView != null){
            mCameraSwitchView.performClick();
        }
    }
    /*prize-add for model merging-huangpengfei-2019-02-23-end*/

    /*prize-add-huangpengfei-2019-02-28-start*/
    @Override
    public void hideQuickIconExceptFlash(){
        if(mQuickSwitcherManager != null){
            mQuickSwitcherManager.hideQuickIconExceptFlash();
        }
        if (mSettingButton != null){
            mSettingButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showQuickIconExceptFlash() {
        if (mSettingButton != null && !isThirdPartyIntent()){
            mSettingButton.setVisibility(View.VISIBLE);
        }
        if (mCurrentCameraId.equals("0") && !(getModeItem().mModeTitle == ModeTitle.PHOTO)){
            LogHelper.i(TAG,"[showQuickIconExceptFlash] return");
            return;
        }
        if(mQuickSwitcherManager != null){
            mQuickSwitcherManager.showQuickIconExceptFlash();
        }
    }
    /*prize-add-huangpengfei-2019-02-28-end*/
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-start*/
    public String getCaptureType() {
        return mCaptureType;
    }
    public boolean isSettingViewShow() {
        return isSettingViewShow;
    }

    public boolean isPluginPageShow() {
        if (mPrizePluginModeManager != null){
            return mPrizePluginModeManager.isPluginFragmentShowed();
        }
        return false;
    }
    /*prize-modify-Camera application should be closed as pressing 2 times back button-xiaoping-20190301-end*/

    /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
    private void startReadNodeValue() {
        LogHelper.i(TAG, "startReadNodeValue");
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mLiftCameraInfo = new IAppUi.HintInfo();
        mLiftCameraInfo.mDelayTime = 3000;
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mLiftCameraInfo.mBackground = mApp.getActivity().getDrawable(id);
        mLiftCameraInfo.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mLiftCameraInfo.mHintText = mApp.getActivity().getString(R.string.camera_not_raised);

        mFrequentlyOpenCameraInfo = new IAppUi.HintInfo();
        mFrequentlyOpenCameraInfo.mDelayTime = 3000;
        int resid = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mFrequentlyOpenCameraInfo.mBackground = mApp.getActivity().getDrawable(resid);
        mFrequentlyOpenCameraInfo.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mFrequentlyOpenCameraInfo.mHintText = mApp.getActivity().getString(R.string.switch_cameras_frequently);
        if (mTimerTask == null) {
            mTimerTask = new PrizeTimeTask() {
                @Override
                public void run() {
                    /*prize-modify-bugid:72637 Switching back and forth, the camera interface prompts the camera to not pop up-xiaoping-20190314*/
                    if (isStartLiftFirst && ( maxValueUp <= 0 || maxValueDown <= 0)) {
                        maxValueUp = 300;
                        maxValueDown = 300;
                    }
                    if (mLiftCameraState == LiftCamera.LiftCameraState.START_UP || mLiftCameraState == LiftCamera.LiftCameraState.START_DOWN) {
                        LogHelper.i(TAG,"index: "+index+",getUpValue: "+mPrizeReadNodeValue.getUpValue()+",getDownValue: "+mPrizeReadNodeValue.getDownValue()+",time: "+(System.currentTimeMillis() - mLastLiftingTime));
                        if (mLiftCameraState == LiftCamera.LiftCameraState.START_UP && System.currentTimeMillis() - mLastLiftingTime >2800 && Math.abs(Integer.valueOf(mPrizeReadNodeValue.getUpValue()) - maxValueUp) < (maxValueUp / 20 * 2)) {
                            LogHelper.e(TAG,"It is detected that the camera has not been properly raised and lowered, and it is forced to end");
                            mImplLifaCamera.updateLiftCameraState(LiftCamera.LiftCameraState.END);
                        }
                    }
                    if ( mLiftCameraState == LiftCamera.LiftCameraState.START_UP && System.currentTimeMillis() - mLastLiftingTime >3000 &&   Integer.valueOf(mPrizeReadNodeValue.getUpValue()) < maxValueUp*0.3) {
                        LogHelper.i(TAG,"mTimeInterval: "+mTimeInterval+",index: "+index+",getUpValue: "+Integer.valueOf(mPrizeReadNodeValue.getUpValue()));
                        mConfigUIHandler.sendEmptyMessage(PREVENTCAMERA);
                    }
                    if (isStartLiftFirst && ( maxValueUp <= 0 || maxValueDown <= 0)) {
                        if (mLiftCameraState == LiftCamera.LiftCameraState.START_UP) {
                            maxValueUp = Integer.valueOf(mPrizeReadNodeValue.getUpValue());
                        } else if (mLiftCameraState == LiftCamera.LiftCameraState.START_DOWN) {
                            maxValueDown = Integer.valueOf(mPrizeReadNodeValue.getDownValue());
                            isStartLiftFirst = false;
                        }
                    }
                    if (System.currentTimeMillis() - mLastLiftingTime > 1500 && Math.abs(mLastUpValue  - Integer.valueOf(mPrizeReadNodeValue.getUpValue())) <= (maxValueUp / 20)
                            && ((mLiftCameraState == LiftCamera.LiftCameraState.START_UP && Math.abs(Integer.valueOf(mPrizeReadNodeValue.getUpValue()) - maxValueUp) < (maxValueUp / 20 * 2))
                            || (mLiftCameraState == LiftCamera.LiftCameraState.START_DOWN && Math.abs(Integer.valueOf(mPrizeReadNodeValue.getDownValue()) - maxValueDown) < (maxValueDown / 20 * 2)))) {
                        maxUpValue = Integer.valueOf(mPrizeReadNodeValue.getUpValue());
                        LogHelper.i(TAG,"maxUpValue or maxDownValue"+maxUpValue+",mLiftCameraState: "+mLiftCameraState+",index: "+index);
                        if (mImplLifaCamera.getLiftCameraState() != LiftCamera.LiftCameraState.END) {
                            mImplLifaCamera.updateLiftCameraState(LiftCamera.LiftCameraState.END);
                        }
                    }
                    mLastUpValue = Integer.valueOf(mPrizeReadNodeValue.getUpValue());
                    if (index == 0) {
                        mFirstUpValue = mLastUpValue;
                    }
                    if (mLiftCameraState == LiftCamera.LiftCameraState.END && mCameraId == 1 && (maxValueUp -Integer.valueOf(mPrizeReadNodeValue.getUpValue())) > (maxValueUp * 0.5)) {
                        LogHelper.i(TAG, "maxUpValue: " + maxUpValue + ",mLastUpValue: " + mLastUpValue);
                        mConfigUIHandler.sendEmptyMessage(PRESSCAMERA);
                    }
                    if (mLiftCameraState == LiftCamera.LiftCameraState.START_UP || mLiftCameraState == LiftCamera.LiftCameraState.START_DOWN) {
                        index++;
                    }
                }
            };
            mTimer.schedule(mTimerTask,0, mTimeInterval);
        }

    }

    private void stopReadNodeValue() {
        LogHelper.i(TAG,"stopReadNodeValue");
        index = 0;
        mLastUpValue = -1;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    @Override
    public LiftCamera getLifaCamera() {
        return mImplLifaCamera;
    }

    class ImplLifaCamera implements LiftCamera {

        @Override
        public void pressCamera() {
            mApp.getActivity().finish();
        }

        @Override
        public void preventCamera() {
            updateLiftCameraState(LiftCameraState.ABNORMAL);
            isPreventCamera = true;
            int marginTop = (int) mApp.getActivity().getResources().getDimension(R.dimen.exit_app_hint_margintop);
            showScreenHint(mLiftCameraInfo,marginTop);
        }

        @Override
        public void updateLiftCameraState(LiftCameraState liftCameraState) {
            if (!FeatureSwitcher.isLiftCameraSupport() || mLiftCameraState == liftCameraState) {
                return;
            }
            LogHelper.i(TAG,"liftCameraState: "+liftCameraState+",mCameraId: "+mCameraId);
            mLiftCameraState = liftCameraState;
            if (liftCameraState == LiftCameraState.START_UP || liftCameraState == LiftCameraState.START_DOWN) {
                index = 0;
                maxUpValue = 0;
//                setTimeInterval(200);
                mLastLiftingTime = System.currentTimeMillis();
            } else {
//                setTimeInterval(200);
            }
            if (liftCameraState == LiftCameraState.END) {
                index = 0;
                mFirstUpValue = 0;
                if (mCameraId == 0) {
                    mConfigUIHandler.sendEmptyMessageDelayed(STOP_READ_NODEVALUE,20);
                } /*else {
                    if (mConfigUIHandler.hasMessages(CHANGE_TIMERTASK_TIMEINTERVAL)) {
                        mConfigUIHandler.removeMessages(CHANGE_TIMERTASK_TIMEINTERVAL);
                    }
                    mConfigUIHandler.sendEmptyMessageDelayed(CHANGE_TIMERTASK_TIMEINTERVAL,3000);
                }*/
            }
        }

        @Override
        public LiftCameraState getLiftCameraState() {
            return mLiftCameraState;
        }

        @Override
        public void setTimeInterval(long timeInterval) {
            mTimeInterval = timeInterval;
        }

        @Override
        public long getLastLiftingTime() {
            return mLastLiftingTime;
        }

        @Override
        public void updateLiftCount(int count) {
            mLiftingCount = count;
        }

        @Override
        public int getLiftCount() {
            return mLiftingCount;
        }
    }

    private void changeTimeInterval() {
        if (mTimer != null) {
            mTimerTask.setPeriod(500);
        }
    }

    public void  getMaxValue() {
        String PRIZE_USER_PATH = "/mnt/vendor/nvdata/APCFG/APRDEB/PRIZE_FACTORY_INFO";
        int tempValueUp = -1;
        int tempValueDown = -1;
        try{
            INvram agent = INvram.getService();
            if (agent == null) {
                LogHelper.e(TAG, "-----readProInfo----NvRAMAgent is null");
                return;
            }

            String buff = "";
            StringBuffer sb = null;
            try {
                buff = agent.readFileByName(PRIZE_USER_PATH, 244);
                LogHelper.e(TAG, " zzj -----readProInfo-------RAW buff=" + buff);
                byte[] buffArr = HexDump.hexStringToByteArray(buff.substring(0, buff.length() - 1));
                LogHelper.e(TAG, " zzj -----buffer[5] ");
                char c = 0;
                sb = new StringBuffer();
                if(sb ==null)
                    LogHelper.e(TAG, " zzj  sb is null");
                for (byte b : buffArr) {
                    c = (char) b;
                    sb.append(String.valueOf(c));
                }
            } catch (Exception e) {
                LogHelper.e(TAG, " zzj readFileByName wrong");
                //e.printStackTrace();
            }
            //String nvResult = Utils.readProInfo(Utils.PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET, 6);
            LogHelper.i(TAG, " getNvramInfo buff=" + String.valueOf(buff) );
            if(sb!=null)
            {
                String nvResult = convertNvChars(sb.toString());
                nvResult = convertNvChars(NvramUtils.readFactoryNvramInfo(PRIZE_FACTORY_CAMERA_HALL_CALI_OFFSET, 6));
                if (nvResult == null || nvResult.length() <6) {
                    return;
                }
                String cameraHallCaliNv = nvResult.substring(nvResult.length() - 6);
                String calidataTop = cameraHallCaliNv.substring(0, 3);
                String calidatabottom = cameraHallCaliNv.substring(3, cameraHallCaliNv.length());
                LogHelper.i(TAG, "-----read calidata calidataTop= "+calidataTop+"  calidatabottom= "+calidatabottom+" \n");
                tempValueUp = Integer.parseInt(calidataTop);
                tempValueDown = Integer.parseInt(calidatabottom);
            }
        }
        catch(Exception e){
            LogHelper.e(TAG, "-----INvram.getService is wrong");
        }

        try {
            String nvResult = convertNvChars(NvramUtils.readFactoryNvramInfo(341, 6));
            if(nvResult !=null && (tempValueUp <= 0 || tempValueDown <= 0))
            {
                String cameraHallCaliNv = nvResult.substring(nvResult.length() - 6);
                String calidataTop = cameraHallCaliNv.substring(0, 3);
                String calidatabottom = cameraHallCaliNv.substring(3, cameraHallCaliNv.length());
                tempValueUp = Integer.parseInt(calidataTop);
                tempValueDown = Integer.parseInt(calidatabottom);
                LogHelper.i(TAG, "-----read calidata calidataTop= "+calidataTop+"  calidatabottom= "+calidatabottom+" \n");
            }
        } catch (Exception e) {
            LogHelper.e(TAG, "-----INvram.getService is wrong");
        }
        LogHelper.i(TAG,"tempValueUp: "+tempValueUp+",tempValueDown: "+tempValueDown);
        if (tempValueUp > 0) {
            maxValueUp = tempValueUp;
        }
        if (tempValueDown > 0) {
            maxValueDown = tempValueDown;
        }
        if (tempValueUp > 0 && tempValueDown > 0) {
            isStartLiftFirst = false;
        }
    }

    public  String convertNvChars(String conStr) {
        if (conStr == null) {
            return "";
        }
        if (conStr.equals("")) {
            return "";
        }
        String str = conStr;
        int strLength = str.length();
        for (int i = 0; i < strLength; i++) {
            char ch = str.charAt(i);
            if ((ch >= 0x00 && ch <= 0x08)
                    || (ch >= 0x0b && ch <= 0x0c)
                    || (ch >= 0x0e && ch <= 0x1f)) {
                str = str.replace(ch, ' ');
            }
        }

        return str;
    }


    private void writeFileData(String filePath, String data) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            byte[] bytes = data.getBytes();
            fout.write(bytes);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/

    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-start*/
    @Override
    public void setSelfTimerState(boolean start) {
        isSelfTimerStart = start;
    }

    @Override
    public boolean getSelfTimerState() {
        LogHelper.i(TAG,"isSelfTimerStart: "+isSelfTimerStart);
        return isSelfTimerStart;
    }
    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-end*/

    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
    @Override
    public void startCaptureAnimation() {
        if (mShutterManager != null) {
            mShutterManager.startCaptureAnimation();
        }
    }
    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
	
	
    private void initBlurUI() {
        mAllBlurLayout = (RelativeLayout) mApp.getActivity().findViewById(R.id.layout_all_blur);
        mBlurLinearLayout = (LinearLayout)mApp.getActivity().findViewById(R.id.layout_blur);
        mSizeLinearLayout = (LinearLayout)mApp.getActivity().findViewById(R.id.layout_size);
        mPrizeCircleView = (PrizeCircleView)mApp.getActivity().findViewById(R.id.cicle);
        mBlurSeekBar = (PrizeSeekBar)mApp.getActivity().findViewById(R.id.seekbar_blur);
        mSizeSeekBar = (PrizeSeekBar)mApp.getActivity().findViewById(R.id.seekbar_size);
        minRadius = (int) mApp.getActivity().getResources().getDimension(R.dimen.blur_size_aperture_radius_min);
        maxRadius = (int) mApp.getActivity().getResources().getDimension(R.dimen.blur_size_aperture_radius_max );
        mControlButton = (ImageButton) mApp.getActivity().findViewById(R.id.control_view);
        mControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSeekbarHasShow) {
                    setSeekbarShow(false);
                    isSeekbarHasShow = false;
                    mControlButton.setImageResource(R.drawable.blur_expand);
                } else {
                    setSeekbarShow(true);
                    isSeekbarHasShow = true;
                    mControlButton.setImageResource(R.drawable.blur_shrink);
                }
            }
        });
        mBlurSeekBar.setOnProgressChangeListener(this);
        mBlurSeekBar.setTagId(BLUR_SEEKBAR);
        mSizeSeekBar.setOnProgressChangeListener(this);
        mSizeSeekBar.setTagId(SIZE_SEEKBAR);
        initValue();
    }

    private void setSeekbarShow(boolean isSeekbarNeedShow) {
        int visibilty = isSeekbarNeedShow ? View.VISIBLE : View.INVISIBLE;
        if (mBlurSeekBar != null && mBlurSeekBar != null) {
            mBlurLinearLayout.setVisibility(visibilty);
            mSizeLinearLayout.setVisibility(visibilty);
        }
    }

    private void initValue() {
        mSizeSeekBar.setSelectProgress((defaultvalue-1)*(mSizeSeekBar.getMaxProgress()/(MAXLEVEL - MINLEVEL)),true);
        mBlurSeekBar.setSelectProgress((defaultvalue-1)*(mBlurSeekBar.getMaxProgress()/(MAXLEVEL - MINLEVEL)),true);
    }

    @Override
    public void onChange(int selectProgress, int id) {
        if (id == SIZE_SEEKBAR) {
            int maxWidth = getSurfaceTextureView().getWidth() / 2;
            maxWidth = maxWidth - maxWidth / 6;

            float radius = ((float)selectProgress / mBlurSeekBar.getMaxProgress()) * maxWidth;

            if(radius > maxWidth){
                radius = maxWidth;
            }else if(radius < minRadius){
                radius = minRadius;
            }

            mPrizeCircleView.setCircleRadius(radius);
            mPrizeCircleView.invalidate();
        }else if(id == BLUR_SEEKBAR){

        }

        if(null != mPicselfieCallback){
            mPicselfieCallback.onPicselfieDataChanged();
        }
    }

    @Override
    public float getRadious() {
        if(null != mPrizeCircleView){
            return mPrizeCircleView.getCircleRadius();
        }
        return 0;
    }

    public int getPicselfieStrength(){
        if(null != mBlurSeekBar){
            return mBlurSeekBar.getLevel();
        }
        return 0;
    }

    @Override
    public ArrayList<Integer> getCircleCoordiNate() {
        ArrayList<Integer> list = new ArrayList<>();

        if(null == mPrizeCircleView){
            return list;
        }

        list.add((int) mPrizeCircleView.getCenterX());
        list.add((int) mPrizeCircleView.getCenterY());
        return list;
    }

    public void selectPluginMode(String mode, boolean scroll, boolean delay){
        if(null != mModeChangeListener){

            if(scroll){
                if(null != mModeItems){
                    //mShutterManager.clearShutterButtons();

                    registerMode(mModeItems);
                }
                mShutterManager.scrollToLastShutter();
            }

            if(mPortraitCallForAiTarget){
                mPortraitCallForAiTarget = false;
                if (mPrizeModeAnimation != null){
                    mApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPrizeModeAnimation.hideSceneText();
                        }
                    });
                }
            }

            if(delay){
                mConfigUIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mModeChangeListener.onModeSelected(mode);
                    }
                }, 200);
            }else{
                mModeChangeListener.onModeSelected(mode);
            }
        }
    }
	
    public Bitmap getPreviewBitmap(int width, int heigit){
        if (mPreviewManager != null){
            return mPreviewManager.getTextureBitmap(width,heigit);
        }
        return null;
    }

    public void showBlurView(final boolean show, UVPicselfieCallback callback){
        mPicselfieCallback = callback;
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null == mAllBlurLayout){
                    initBlurUI();
                }

                mAllBlurLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                mPrizeCircleView.setVisibility(show ? View.VISIBLE : View.GONE);

                if(!show){
                    setSeekbarShow(false);
                    isSeekbarHasShow = false;
                    mControlButton.setImageResource(R.drawable.blur_expand);
                }else{
                    if(null != mPrizeCircleView){
                        mPrizeCircleView.updatePosition(getPreviewArea());
                    }
                }
            }
        });
    }
	
	public TextureView getSurfaceTextureView(){
        if(null != mPreviewManager){
            return mPreviewManager.getSurfaceTextureView();
        }
        return  null;
    }

    public Size getPreviewSize(){
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    public void setSettingIconVisible(boolean visible){
        if(null != mSettingButton){
            mSettingButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setTexutreUpdateCallback(TexutreUpdateCallback callback){
        if (mPreviewManager != null){
            mPreviewManager.setTexutreUpdateCallback(callback);
        }
    }

    public QuickSwitcherManager getmQuickSwitcherManager(){
        return mQuickSwitcherManager;
    }

    // prize add by zhangguo 20190419, for bug#74593, dualcam focus point error
    public void setFocusPoint(Size point){
        mFocusPoint = point;
    }

    public Size getFocusPoint(){

        if(null == mFocusPoint){
            return new Size(getSurfaceTextureView().getWidth() / 2, getSurfaceTextureView().getHeight() / 2);
        }

        return mFocusPoint;
    }
	
 // prize add by zhangguo 20190419, for bug#74679, filter is none after switch camera start
    public void setFilterIndex(int index){
        mFilterIndex = index;
    }

    public int getFilterIndex(){
        return mFilterIndex;
    }
  // prize add by zhangguo 20190419, for bug#74679, filter is none after switch camera end

    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-start*/
    @Override
    public void setZoomState(boolean isZoom) {
        isZoomState = isZoom;
    }
    /*prize-modify-75105 do not trigger touch screen when adjusting the focus-xiaoping-20190425-end*/
}
