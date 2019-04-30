package com.mediatek.camera.feature.setting.beautyparameter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.portability.SystemProperties;

public class BeautyParameterEntry extends FeatureEntryBase {
    private BeautyParameter mBeautyParameter;
    private static final LogUtil.Tag TAG = new LogUtil.Tag(BeautyParameterEntry.class.getSimpleName());
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public BeautyParameterEntry(Context context, Resources resources) {
        super(context, resources);
        LogHelper.i("","BeautyParameterEntry creat");
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !(MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore
                .ACTION_VIDEO_CAPTURE.equals(action)) && SystemProperties.getInt("ro.pri_camera_fn_facebeauty", 0) == 1 ? true : false;
        LogHelper.i(TAG, "[isSupport] : " + support);
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return BeautyParameterEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        LogHelper.i("","createInstance: ");
        if(mBeautyParameter == null){
            mBeautyParameter = new BeautyParameter();
        }
        return mBeautyParameter;
    }
}
