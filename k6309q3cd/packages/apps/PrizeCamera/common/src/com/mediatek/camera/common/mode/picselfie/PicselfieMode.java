package com.mediatek.camera.common.mode.picselfie;

import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.prize.IBuzzyStrategy;
import com.mediatek.camera.common.prize.YuvBackBuzzyStrategy;
import com.mediatek.camera.portability.SystemProperties;

import java.util.Timer;
import java.util.TimerTask;
import com.mediatek.camera.R;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PicselfieMode extends PhotoMode {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PicselfieMode.class.getSimpleName());
    private IBuzzyStrategy mBuzzyStrategy;
    private UiHandler mUiHandler;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private static final int MSG_READ_SURFACE_DATA = 1;
    private IAppUi.HintInfo mOcclusionPromptHint;
    private boolean mIsSimulateDualCamera;
    private IAppUi.HintInfo mBlurringHint;

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        LogHelper.i(TAG,"");
        super.init(app, cameraContext, isFromLaunch);
        /*prize-modify fixbug[72495]-huangpengfei-20190309-start*/
        mIsSimulateDualCamera = ("1").equals(android.os.SystemProperties.get("ro.pri.simulate.dual.camera.tip", "0"));
        if (mIsSimulateDualCamera) {
            if (mBuzzyStrategy == null) {
                mBuzzyStrategy = new YuvBackBuzzyStrategy();
            }
            if (mUiHandler == null) {
                mUiHandler = new UiHandler(mIApp.getActivity().getMainLooper());
            }
            startTimer();
            mOcclusionPromptHint = new IAppUi.HintInfo();
            mOcclusionPromptHint.mDelayTime = 3000;
            int id = app.getActivity().getResources().getIdentifier("hint_text_background",
                    "drawable", app.getActivity().getPackageName());
            mOcclusionPromptHint.mBackground = app.getActivity().getDrawable(id);
            mOcclusionPromptHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
            mOcclusionPromptHint.mHintText = app.getActivity().getString(
                    R.string.secondary_camera_blocked);
        }
        mBlurringHint = new IAppUi.HintInfo();
        mBlurringHint.mDelayTime = 3000;
        int id = app.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", app.getActivity().getPackageName());
        mBlurringHint.mBackground = app.getActivity().getDrawable(id);
        mBlurringHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mBlurringHint.mHintText = app.getActivity().getString(R.string.camera_slr_mode_tips);
        /*prize-modify fixbug[72495]-huangpengfei-20190309-end*/
    }

    @Override
    protected void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId, boolean needFastStartPreview) {
        LogHelper.i(TAG,"");
        super.prepareAndOpenCamera(needOpenCameraSync, cameraId, needFastStartPreview);
        openSecondaryCamera();

    }

    @Override
    protected void prePareAndCloseCamera(boolean needSync, String cameraId) {
        LogHelper.i(TAG,"");
        super.prePareAndCloseCamera(needSync, cameraId);
        closeSecondaryCamera();
    }

    private final class UiHandler extends Handler{
        public UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_READ_SURFACE_DATA:
                    post(mRunnable);
                    break;
                default:
                    break;
            }
        }
    }

    public void openSecondaryCamera() {
        LogHelper.i(TAG,"");
        if (mBuzzyStrategy != null) {
            mBuzzyStrategy.attachSurfaceViewLayout();
            if (mUiHandler != null) {
                mUiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mBuzzyStrategy.openCamera();
                        mBuzzyStrategy.startPreview();
                    }
                });
            }
        }
    }

    public void closeSecondaryCamera() {
        LogHelper.i(TAG,"");
        // TODO Auto-generated method stub
        if (mBuzzyStrategy != null) {
            mBuzzyStrategy.detachSurfaceViewLayout();
            mBuzzyStrategy.closeCamera();
        }
    }

    private void startTimer() {
        /*prize-add for model merging-huangpengfei-2019-02-23-start*/
        if (!mIsSimulateDualCamera && "1".equals(mCameraId)){
            return;
        }
        /*prize-add for model merging-huangpengfei-2019-02-23-end*/
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if(mUiHandler != null){
                        mUiHandler.sendEmptyMessage(MSG_READ_SURFACE_DATA);
                    }
                }
            };
            int time = mBuzzyStrategy != null ? mBuzzyStrategy.getCheckTime() : 700;
            mTimer.schedule(mTimerTask, 0, time);
        }
    }

    private void stopTimer() {
        LogHelper.i(TAG,"stopTimer");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBuzzyStrategy == null) {
                return;
            }
            /*prize-modify-add professional mode function-xiaoping-20190216-start*/
            if (mIApp.getAppUi().getCameraId() == 1) {
                mIDeviceController.setParameterRequest(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE,1);
                mIApp.getAppUi().setPicsflieValue("on");
            } else if (mIApp.getAppUi().getCameraId() == 0) {
                if (mBuzzyStrategy.isOcclusion()) {
                    mIApp.getAppUi().showScreenHint(mOcclusionPromptHint,(int) mIApp.getActivity().getResources().getDimension(R.dimen.hdr_tips_margin_top));
                    mIDeviceController.setParameterRequest(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE,0);
                    mIApp.getAppUi().setPicsflieValue("off");
                } else {
                    mIDeviceController.setParameterRequest(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE,1);
                    mIApp.getAppUi().setPicsflieValue("on");
                }
            }
            /*prize-modify-add professional mode function-xiaoping-20190216-end*/

        }
    };

    @Override
    public void unInit() {
        LogHelper.i(TAG,"");
        super.unInit();
        /*prize-modify-bugid:70062 does not change the picsefile value here-xiaoping-20181224-start*/
        stopTimer();
        if (mUiHandler != null) {
            mUiHandler.removeCallbacks(mRunnable);
            mUiHandler = null;
        }
        if (mIApp != null && "on".equals(mIApp.getAppUi().getPicsflieValue())) {
            mIApp.getAppUi().setPicsflieValue("off");
        }
        /*prize-modify-bugid:70062 does not change the picsefile value here-xiaoping-20181224-end*/
    }

    /*prize-modify-bugid:70062 picsefile change oter mode occur camera crash -xiaoping-20190107-start*/
    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        super.pause(nextModeDeviceUsage);
        stopTimer();
        if (mUiHandler != null) {
            mUiHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        if (SystemProperties.getInt("ro.pri.current.project",0) == 2) {
            mIApp.getAppUi().showScreenHint(mBlurringHint,(int) mIApp.getActivity().getResources().getDimension(R.dimen.hdr_tips_margin_top));
        }
        startTimer();
    }
    /*prize-modify-bugid:70062 picsefile change oter mode occur camera crash -xiaoping-20190107-end*/
}
