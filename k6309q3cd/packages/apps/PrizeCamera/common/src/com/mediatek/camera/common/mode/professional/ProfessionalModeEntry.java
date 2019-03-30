package com.mediatek.camera.common.mode.professional;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.portability.SystemProperties;

public class ProfessionalModeEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ProfessionalModeEntry.class.getSimpleName());
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public ProfessionalModeEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !(MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore
                .ACTION_VIDEO_CAPTURE.equals(action)) && SystemProperties.getInt("ro.pri.professional.mode", 0) == 1 ? true : false;
        LogHelper.i(TAG, "[isSupport] : " + support);
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return ProfessionalModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return null;
    }

    @Override
    public Object createInstance() {
        return new ProfessionalMode();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        IAppUi.ModeItem modeItem = new IAppUi.PluginModeItem();
        modeItem.mType = "Professional";
        modeItem.mPriority = 6;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0"};
        modeItem.mModeTitle = IAppUi.ModeTitle.PROFESSIONAL;
        return modeItem;
    }
}
