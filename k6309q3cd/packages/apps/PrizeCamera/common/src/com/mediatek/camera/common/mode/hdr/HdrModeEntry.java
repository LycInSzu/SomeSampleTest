package com.mediatek.camera.common.mode.hdr;

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
import com.mediatek.camera.common.mode.hdr.HdrMode;

public class HdrModeEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(HdrModeEntry.class.getSimpleName());
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public HdrModeEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        return  !isThirdPartyIntent(activity);
    }

    @Override
    public String getFeatureEntryName() {
        return HdrModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return null;
    }

    @Override
    public Object createInstance() {
        return new HdrMode();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        IAppUi.ModeItem modeItem = new IAppUi.ModeItem();
        modeItem.mType = "HDR";
        modeItem.mPriority = 1;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0", "1"};
        modeItem.mModeTitle = IAppUi.ModeTitle.HDR;
        return modeItem;
    }
}
