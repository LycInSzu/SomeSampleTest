package com.mediatek.camera.feature.setting.picselfie;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.portability.SystemProperties;
public class PicselfieParameterEntry extends FeatureEntryBase {
    private PicselfieParameter mPicselfieParameter;
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public PicselfieParameterEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        boolean suport = !isThirdPartyIntent(activity) && SystemProperties.getInt("ro.pri.selfie.arcsoft", 0) == 1 ? true : false;
        LogHelper.i("",""+suport);
        return suport;
    }

    @Override
    public String getFeatureEntryName() {
        return PicselfieParameterEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        if (mPicselfieParameter == null) {
            mPicselfieParameter = new PicselfieParameter();
        }
        return mPicselfieParameter;
    }
}
