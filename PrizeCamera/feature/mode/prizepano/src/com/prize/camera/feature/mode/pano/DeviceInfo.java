package com.prize.camera.feature.mode.pano;

import com.mediatek.camera.common.setting.ISettingManager;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class DeviceInfo {
    private ISettingManager mSettingManager;
    private String mCameraId;
    private boolean mNeedSync;
    private boolean mNeedFastStartPreview;

    /**
     * Set the camera id.
     * @param cameraId which camera need opened.
     */
    void setCameraId(String cameraId) {
        mCameraId = cameraId;
    }

    /**
     * Add a setting manager to the info.
     * @param settingManager current setting manager.
     */
    void setSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
    }

    /**
     * Add whether need sync open the camera.
     * @param needSync whether need sync the camera.
     */
    void setNeedOpenCameraSync(boolean needSync) {
        mNeedSync = needSync;
    }

    /**
     * Add whether need fast start preview.
     * @param needFastStartPreview whether need fast start perview.
     */
    void setNeedFastStartPreview(boolean needFastStartPreview) {
        mNeedFastStartPreview = needFastStartPreview;
    }

    /**
     * Get the setting manager.
     * @return current setting manager.
     */
    public ISettingManager getSettingManager() {
        return mSettingManager;
    }

    /**
     * Get the camera id from info.
     * @return current camera id.
     */
    public String getCameraId() {
        return mCameraId;
    }

    /**
     * Get whether need open sync the camera.
     * @return whether open sync the camera.
     */
    public boolean getNeedOpenCameraSync() {
        return mNeedSync;
    }

    /**
     * Get whether fast start preview.
     * @return true means need fast preview.
     */
    public boolean getNeedFastStartPreview() {
        return mNeedFastStartPreview;
    }
}
