package com.mediatek.camera.feature.setting.volumekeys;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;

public class VolumekeysEntry extends FeatureEntryBase {
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public VolumekeysEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
// zhangguo add, for bug#75237 do not show this setting in dualcam
        CameraActivity ac = (CameraActivity) activity;

        if(null != ac.getAppUi() && null != ac.getAppUi().getModeItem() && ac.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE){
            return false;
        }
        return true;
    }

    @Override
    public String getFeatureEntryName() {
        return VolumekeysEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        return new Volumekeys();
    }
}
