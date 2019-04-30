package com.prize.camera.feature.mode.gif;

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

public class GifModeEntry extends FeatureEntryBase {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(GifModeEntry.class.getSimpleName());
    private static final String MODE_ITEM_TYPE = "Gif";

    private static final int MODE_ITEM_PRIORITY = 7;

    public GifModeEntry(Context context, Resources resources) {
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
        return GifModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    @Override
    public Object createInstance() {
        return new GifMode();
    }

    private boolean isPlatformSupport() {

        return isSupportGifMode();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        IAppUi.ModeItem modeItem = new IAppUi.PluginModeItem();
        modeItem.mModeUnselectedIcon = mResources.getDrawable(R.drawable.gif_nor);
        modeItem.mModeSelectedIcon = mResources.getDrawable(R.drawable.gif_inpress);
        //modeItem.mShutterIcon = mResources.getDrawable(R.drawable.ic_slow_motion_shutter);
        modeItem.mType = MODE_ITEM_TYPE;
        modeItem.mPriority = MODE_ITEM_PRIORITY;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0", "1"};
        modeItem.mModeTitle = IAppUi.ModeTitle.GIF;
        return modeItem;
    }

    private boolean isSupportGifMode(){
        return true;
    }
}
