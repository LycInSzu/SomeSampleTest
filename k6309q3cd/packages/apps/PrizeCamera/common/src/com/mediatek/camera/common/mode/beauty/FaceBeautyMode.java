package com.mediatek.camera.common.mode.beauty;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.mode.beauty.beautyui.IBeautyUi;
import com.mediatek.camera.common.mode.photo.PhotoMode;

import javax.annotation.Nonnull;

public class FaceBeautyMode extends PhotoMode {
    private IBeautyUi beautyUi;
    protected IAppUi mAppUi;
    protected IApp mApp;
    private MainHandler mainHandler;
    private static final int MSG_INIT_UI = 0;
    private static final int MSG_HIDE_UI = 1;

    FaceBeautyMode(){}

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        super.init(app, cameraContext, isFromLaunch);
        mAppUi = app.getAppUi();
        mainHandler = new MainHandler(app.getActivity().getMainLooper());
        mainHandler.sendEmptyMessageDelayed(MSG_INIT_UI,50);
    }



    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_UI:
                    break;
                case MSG_HIDE_UI:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void unInit() {
        LogHelper.i("","");

        super.unInit();

    }


}
