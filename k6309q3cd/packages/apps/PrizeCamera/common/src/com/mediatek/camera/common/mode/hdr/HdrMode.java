package com.mediatek.camera.common.mode.hdr;


import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.PhotoMode;

import javax.annotation.Nonnull;

public class HdrMode extends PhotoMode {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(HdrMode.class.getSimpleName());

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
                     boolean isFromLaunch) {
        super.init(app, cameraContext, isFromLaunch);

        LogHelper.d(TAG, "[init]");
    }
}
