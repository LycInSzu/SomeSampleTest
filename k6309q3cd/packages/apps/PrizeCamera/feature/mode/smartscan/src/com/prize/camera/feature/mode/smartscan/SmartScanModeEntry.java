package com.prize.camera.feature.mode.smartscan;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.mode.ICameraMode;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class SmartScanModeEntry extends FeatureEntryBase {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(SmartScanModeEntry.class.getSimpleName());
    private static final String MODE_ITEM_TYPE = "SmartScan";

    private static final int MODE_ITEM_PRIORITY = 7;

    public SmartScanModeEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        boolean isSupport = false;
        if (isPlatformSupport()&&!isThirdPartyIntent(activity)){
            isSupport = true;
        }
        return isSupport;
    }

    @Override
    public String getFeatureEntryName() {
        return SmartScanModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    @Override
    public Object createInstance() {
        return new SmartScanMode();
    }

    private boolean isPlatformSupport() {

        return isSupportSmartScan();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        IAppUi.ModeItem modeItem = new IAppUi.PluginModeItem();
        modeItem.mType = MODE_ITEM_TYPE;
        modeItem.mPriority = MODE_ITEM_PRIORITY;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0"};
        modeItem.mModeTitle = IAppUi.ModeTitle.SMARTSCAN;
        return modeItem;
    }

    private boolean isSupportSmartScan(){
        return true;
    }
}
