package com.prize.camera.feature.mode.pano;

import com.mediatek.camera.common.utils.Size;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public interface IPanoDeviceController {

    /**
     * This interface is used to callback jpeg data to mode.
     */
    interface JpegCallback {
        /**
         * Notify jpeg data is generated.
         *
         * @param data The Jpeg data.
         */
        void onDataReceived(byte[] data);
    }

    /**
     * This callback to notify device state.
     */
    interface DeviceCallback {
        /**
         * Notified when camera opened done with camera id.
         *
         * @param cameraId the camera is opened.
         */
        void onCameraOpened(String cameraId);

        /**
         * Notified before do close camera.
         */
        void beforeCloseCamera();

        /**
         * Notified call stop preview immediately.
         */
        void afterStopPreview();

        /**
         * When preview data is received,will fired this function.
         *
         * @param data   the preview data.
         * @param format the preview format.
         */
        void onPreviewCallback(byte[] data, int format);
    }

    /**
     * This callback is used for notify camera is opened,you can use it for get parameters..
     */
    interface PreviewSizeCallback {
        /**
         * When camera is opened will be called.
         *
         * @param previewSize current preview size.
         */
        void onPreviewSizeReady(Size previewSize);
    }

    /**
     * should update device manager when mode resume, before open camera.
     */
    void queryCameraDeviceManager();


    /**
     * open camera with specified camera id.
     *
     * @param info the camera info which will be opened.
     */
    void openCamera(DeviceInfo info);

    /**
     * update preview surface.
     *
     * @param surfaceObject surface holder instance.
     */
    void updatePreviewSurface(Object surfaceObject);

    /**
     * Set a callback for device.
     *
     * @param callback the device callback.
     */
    void setDeviceCallback(com.prize.camera.feature.mode.pano.IPanoDeviceController.DeviceCallback callback);

    /**
     * For API1 will directly call start preview.
     * For API2 will first create capture session and then set repeating requests.
     */
    void startPreview();

    /**
     * For API1 will directly call stop preview.
     * For API2 will call session's abort captures.
     */
    void stopPreview();

    /**
     * For API1 will directly call takePicture.
     * For API2 will call STILL_CAPTURE capture.
     *
     * @param callback jpeg data callback.
     */
    void takePicture(@Nonnull com.prize.camera.feature.mode.pano.IPanoDeviceController.JpegCallback callback);

    /**
     * Stop the current active capture.
     */
    void stopCapture();

    /**
     * Set to need wait for picture done when the progress bar reach to 100%.
     *
     * @param needWaitPictureDone Whether need wait picture done or not.
     */
    void setNeedWaitPictureDone(boolean needWaitPictureDone);

    /**
     * update current GSensor orientation.the value will be 0/90/180/270;
     *
     * @param orientation current GSensor orientation.
     */
    void updateGSensorOrientation(int orientation);

    /**
     * close camera.
     *
     * @param sync whether need sync call.
     */
    void closeCamera(boolean sync);

    /**
     * Get the preview size with target ratio.
     *
     * @param targetRatio current ratio.
     * @return current preview size.
     */
    Size getPreviewSize(double targetRatio);

    /**
     * Set a camera opened callback.
     *
     * @param callback camera opened callback.
     */
    void setPreviewSizeReadyCallback(com.prize.camera.feature.mode.pano.IPanoDeviceController.PreviewSizeCallback callback);

    /**
     * Set the new picture size.
     *
     * @param size current picture size.
     */
    void setPictureSize(Size size);

    /**
     * Check whether can take picture or not.
     *
     * @return true means can take picture; otherwise can not take picture.
     */
    boolean isReadyForCapture();

    /**
     * When don't need the device controller need destroy the device controller.
     * such as handler.
     */
    void destroyDeviceController();

    void setPortraitValueChanged(int value, boolean isNeedRepeatingRequest);

    interface PreviewCallback {
        void onPreviewFrame(byte[] data, int width, int height);
    }

    void createAndChangeRepeatingRequest();
}
