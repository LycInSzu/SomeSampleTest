package com.prize.camera.feature.mode.pano;

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

public class PanoModeEntry extends FeatureEntryBase {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.pano.PanoModeEntry.class.getSimpleName());
    private static final String MODE_ITEM_TYPE = "PrizePano";

    private static final int MODE_ITEM_PRIORITY = 7;

    public PanoModeEntry(Context context, Resources resources) {
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
        return com.prize.camera.feature.mode.pano.PanoModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    @Override
    public Object createInstance() {
        return new PanoMode();
    }

    private boolean isPlatformSupport() {

        return isSupportPano();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {

        IAppUi.ModeItem modeItem = new IAppUi.PluginModeItem();
        modeItem.mModeUnselectedIcon = mContext.getResources().getDrawable(R.drawable
                .qr_nor);
        modeItem.mModeSelectedIcon = mContext.getResources().getDrawable(R.drawable
                .qr_inpress);
        modeItem.mType = MODE_ITEM_TYPE;
        modeItem.mPriority = MODE_ITEM_PRIORITY;
        modeItem.mClassName = getFeatureEntryName();
        //modeItem.mModeName = mContext.getString(R.string.pano_dialog_title);
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0", "1"};
        modeItem.mModeTitle = IAppUi.ModeTitle.PANO;
        return modeItem;
    }

    private boolean isSupportPano(){
        return true;
    }
}
