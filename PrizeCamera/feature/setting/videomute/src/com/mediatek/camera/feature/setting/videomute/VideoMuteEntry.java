package com.mediatek.camera.feature.setting.videomute;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;

public class VideoMuteEntry extends FeatureEntryBase {
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public VideoMuteEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        return true;
    }

    @Override
    public String getFeatureEntryName() {
        return VideoMuteEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        return new VideoMute();
    }
}
