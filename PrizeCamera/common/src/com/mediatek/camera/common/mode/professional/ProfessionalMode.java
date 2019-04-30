package com.mediatek.camera.common.mode.professional;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.photo.PhotoMode;

import javax.annotation.Nonnull;

public class ProfessionalMode extends PhotoMode {
    IArcProgressBarUI mIArcProgressBarUI;
    private final int INITUI = 0;
    private final int UPDATEUISTATE = 1;
    private UiHandler mUiHandler;
    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        LogHelper.i("","");
        super.init(app, cameraContext, isFromLaunch);
        mIArcProgressBarUI = app.getAppUi().getArcProgressBarUI();
        mUiHandler = new UiHandler(app.getActivity().getMainLooper());
        mUiHandler.sendEmptyMessage(INITUI);
    }

    @Override
    public void unInit() {
        super.unInit();
        Message message = new Message();
        message.what = UPDATEUISTATE;
        message.obj = false;
        mUiHandler.sendMessage(message);

    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        LogHelper.i("","");
        Message message = new Message();
        message.what = UPDATEUISTATE;
        message.obj = true;
        mUiHandler.sendMessage(message);

    }

    private final class UiHandler extends Handler {
        public UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INITUI:
                    mIArcProgressBarUI.initUI();
                    break;
                case UPDATEUISTATE:
                    mIArcProgressBarUI.updateUIState((boolean)msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {
        super.onPreviewCallback(data, format);
        mIArcProgressBarUI.restoreSelected();
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        mIArcProgressBarUI.clearEffect();
        return super.onCameraSelected(newCameraId);
    }

    @Override
    protected void prePareAndCloseCamera(boolean needSync, String cameraId) {
//        mIArcProgressBarUI.clearEffect();
        super.prePareAndCloseCamera(needSync, cameraId);

    }
}
