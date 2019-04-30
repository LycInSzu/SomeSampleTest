package com.mediatek.camera.common.mode.picselfie;

import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.view.SurfaceHolder;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import com.mediatek.camera.common.mode.photo.device.PhotoDevice2Controller;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class PicSelfieDevice2Controller extends PhotoDevice2Controller {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(PicSelfieDevice2Controller.class.getSimpleName());

    private static final String KEY_PICTURE_SIZE = "key_picture_size";

    public PicSelfieDevice2Controller(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        super(activity, context);
    }

    public PicSelfieDevice2Controller(@Nonnull IApp app, @Nonnull ICameraContext context){
        super(app, context);
    }

    protected Size getTargetPreviewSize(double ratio) {

        if("1".equals(getCameraId())){
            return super.getTargetPreviewSize(ratio);
        }

        Size values = null;
        mPreviewWidth = 640;
        mPreviewHeight = 480;
        values = new Size(mPreviewWidth, mPreviewHeight);
        LogHelper.d(TAG, "[getTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
        return values;
    }

    @Override
    public void setPictureSize(Size defaultSize) {

        if((float)defaultSize.getWidth() / defaultSize.getHeight() - 4f / 3 < 0.01){
            super.setPictureSize(defaultSize);
            return;
        }

        String formatTag = mSettingController.queryValue(HeifHelper.KEY_FORMAT);
        int format = HeifHelper.getCaptureFormat(formatTag);
        mCaptureSurface.setFormat(formatTag);

        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        List<String> picsize =  controller.querySupportedPlatformValues(KEY_PICTURE_SIZE);
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        String maxDefaultSize = null;
        Size lastSize = null;
        for(String s : picsize){
            String []size = s.split("x");
            if(null != size && size.length == 2){
                float w = Float.valueOf(size[0]).floatValue();
                float h = Float.valueOf(size[1]).floatValue();
                if(Math.abs(w / h - 4f / 3) < 0.01 && w * h < 13000000){
                    if(null == lastSize){
                        lastSize = new Size((int)w, (int)h);
                    }else if(null != lastSize && w * h > lastSize.getWidth() * lastSize.getHeight()){
                        lastSize = new Size((int)w, (int)h);
                    }
                }
            }
        }

        if(null != lastSize){
            defaultSize = lastSize;
        }

        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(defaultSize.getWidth(),
                defaultSize.getHeight(), format, CAPTURE_MAX_NUMBER);
        double ratio = (double) defaultSize.getWidth() / defaultSize.getHeight();
        ThumbnailHelper.updateThumbnailSize(ratio);
        if (ThumbnailHelper.isPostViewSupported()) {
            mThumbnailSurface.updatePictureInfo(ThumbnailHelper.getThumbnailWidth(),
                    ThumbnailHelper.getThumbnailHeight(),
                    ThumbnailHelper.IMAGE_BUFFER_FORMAT,
                    CAPTURE_MAX_NUMBER);
        }
    }

    @Override
    protected void initSettings() {
        if("1".equals(getCameraId())){
            super.initSettings();
            return;
        }
    }
}
