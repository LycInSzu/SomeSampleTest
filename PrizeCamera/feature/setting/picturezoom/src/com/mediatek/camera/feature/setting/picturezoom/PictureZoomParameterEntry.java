package com.mediatek.camera.feature.setting.picturezoom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.portability.SystemProperties;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomModeEntry;
import com.mediatek.camera.common.setting.ICameraSetting;

public class PictureZoomParameterEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PictureZoomParameterEntry.class.getSimpleName());
    private PictureZoomParameter mPictureZoomParameter;
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public PictureZoomParameterEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !(MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore
                .ACTION_VIDEO_CAPTURE.equals(action)) && SystemProperties.getInt("ro.pri.superzoom.arcsoft", 0) == 1 ? true : false;
        LogHelper.i(TAG, "[isSupport] : " + support+",superzoom: "+(SystemProperties.getInt("ro.pri.superzoom.arcsoft", 0) == 1 ? true : false));
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return PictureZoomParameterEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        mPictureZoomParameter = new PictureZoomParameter();
        return mPictureZoomParameter;
    }
}
