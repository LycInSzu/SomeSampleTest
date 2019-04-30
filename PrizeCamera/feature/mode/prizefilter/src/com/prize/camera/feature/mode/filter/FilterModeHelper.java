package com.prize.camera.feature.mode.filter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.location.Location;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class FilterModeHelper {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.filter.FilterModeHelper.class
            .getSimpleName());

    private static final String IMAGE_FORMAT = "'IMG'_yyyyMMdd_HHmmss_S";
    private ICameraContext mICameraContext;
    /**
     * Shutter speed auto.
     */
    public static final String EXPOSURE_TIME_AUTO = "Auto";

    /**
     * The constructor of LongExposureModeHelper.
     *
     * @param cameraContext current camera context.
     */
    public FilterModeHelper(ICameraContext cameraContext) {
        mICameraContext = cameraContext;
    }

    /**
     * create a content values from data.
     *
     * @param data          the resource file.
     * @param fileDirectory file directory.
     * @param pictureWidth  the width of content values.
     * @param pictureHeight the height of content valuse.
     * @return the content values from the data.
     */
    public ContentValues createContentValues(byte[] data, String fileDirectory, int
            pictureWidth, int pictureHeight) {
        ContentValues values = new ContentValues();
        long dateTaken = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(IMAGE_FORMAT);
        Date date = new Date(dateTaken);
        String title = simpleDateFormat.format(date);
        String fileName = title + ".jpg";
        int orientation = CameraUtil.getOrientationFromExif(data);

        String mime = "image/jpeg";
        String path = fileDirectory + '/' + fileName;

        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, mime);
        values.put(MediaStore.Images.ImageColumns.WIDTH, pictureWidth);
        values.put(MediaStore.Images.ImageColumns.HEIGHT, pictureHeight);

        values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        values.put(MediaStore.Images.ImageColumns.DATA, path);

        Location location = mICameraContext.getLocation();
        if (location != null) {
            values.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
        }
        LogHelper.d(TAG, "createContentValues, width : " + pictureWidth + ",height = " +
                pictureHeight + ",orientation = " + orientation);
        return values;
    }

    /**
     * Get the camera orientation from camera info.
     * @param cameraId the target camera id.
     * @param activity current activity.
     * @return orientation value.
     */
    public int getCameraInfoOrientation(String cameraId, Activity activity) {
        try {
            CameraManager cameraManager = (CameraManager) activity
                    .getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(cameraId);
            if (characteristics == null) {
                LogHelper.e(TAG, "[getCameraInfoOrientation] characteristics is null");
                return 0;
            }
            int orientation = characteristics
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
            return orientation;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Check the camera is need mirror or not.
     * @param cameraId current camera id.
     * @param activity current activity.
     * @return true means need mirror.
     */
    public boolean isMirror(String cameraId, Activity activity) {
        try {
            CameraManager cameraManager = (CameraManager) activity
                    .getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(cameraId);
            if (characteristics == null) {
                LogHelper.e(TAG, "[isMirror] characteristics is null");
                return false;
            }
            int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            int sensorOrientation = characteristics
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
            return (facing == CameraMetadata.LENS_FACING_FRONT);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
