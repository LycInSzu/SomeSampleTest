package com.prize.camera.feature.mode.pano;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.app.IApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class PanoModeHelper {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.pano.PanoModeHelper.class
            .getSimpleName());

    private static final String IMAGE_FORMAT = "'IMG'_yyyyMMdd_HHmmss_S";
    private ICameraContext mICameraContext;
    private IApp mApp;
    /**
     * Shutter speed auto.
     */
    public static final String EXPOSURE_TIME_AUTO = "Auto";

    /**
     * The constructor of LongExposureModeHelper.
     *
     * @param cameraContext current camera context.
     */
    public PanoModeHelper(ICameraContext cameraContext,IApp app) {
        mICameraContext = cameraContext;
        mApp = app;
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

    public void savePanoData(Bitmap bitmap){

        long dateTaken = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(IMAGE_FORMAT);
        Date date = new Date(dateTaken);
        String title = simpleDateFormat.format(date);
        String fileName = title + ".jpg";
        String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
        String mime = "image/jpeg";
        String path = fileDirectory + '/' + fileName;
        savaBitmapToDB(bitmap,path,fileName,title,dateTaken);
        storeBitmapToFile(path,bitmap);
    }

    private void storeBitmapToFile(String fileName, Bitmap bitmap) {
        File file = new File(fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 98, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException var6) {
            var6.printStackTrace();
        } catch (IOException var7) {
            var7.printStackTrace();
        }

    }

    private void savaBitmapToDB(Bitmap result,String path,String fileName,String title,long dateTaken){
        int pictureWidth = result.getWidth();
        int pictureHeight = result.getHeight();
        int orientation = 0;
        ContentValues values = new ContentValues();
        String mime = "image/jpeg";
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
        try {
            Uri mUri = mApp.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (mUri != null) {
                mApp.notifyNewMedia(mUri,true);
            }

        } catch (IllegalArgumentException e) {
            LogHelper.e(TAG,
                    "[saveImageToDatabase]Failed to write MediaStore,IllegalArgumentException:",
                    e);
        } catch (UnsupportedOperationException e) {
            LogHelper.e(TAG,
                    "[saveImageToDatabase]Failed to write MediaStore,UnsupportedOperationException:",
                    e);
        }
        LogHelper.d(TAG, "createContentValues, width : " + pictureWidth + ",height = " +
                pictureHeight + ",orientation = " + orientation);
    }

}
