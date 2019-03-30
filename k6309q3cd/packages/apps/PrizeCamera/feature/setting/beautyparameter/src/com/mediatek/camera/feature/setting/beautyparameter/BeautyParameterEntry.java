package com.mediatek.camera.feature.setting.beautyparameter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;

public class BeautyParameterEntry extends FeatureEntryBase {
    private BeautyParameter mBeautyParameter;
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
        return true;
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
