package com.mediatek.camera.common.mode.beauty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUi.ModeItem;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.portability.SystemProperties;

public class FaceBeautyModeEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(FaceBeautyModeEntry.class.getSimpleName());
    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public FaceBeautyModeEntry(Context context, Resources resources) {
        super(context, resources);
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
        return FaceBeautyModeEntry.class.getName();
    }

    @Override
    public Class getType() {
        return null;
    }

    @Override
    public Object createInstance() {
        return new FaceBeautyMode();
    }

    @Override
    public IAppUi.ModeItem getModeItem() {
        ModeItem modeItem = new ModeItem();
        modeItem.mType = "FaceBeauty";
        modeItem.mPriority = 5;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mResources.getString(mResources.getIdentifier("normal_mode_title",
                "string", mContext.getPackageName()));
        modeItem.mSupportedCameraIds = new String[]{"0", "1"};
        modeItem.mModeTitle = IAppUi.ModeTitle.BEAUTY;
        return modeItem;
    }
}
