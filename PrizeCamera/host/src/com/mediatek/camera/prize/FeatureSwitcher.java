package com.mediatek.camera.prize;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.portability.SystemProperties;
public class FeatureSwitcher {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(FeatureSwitcher.class.getSimpleName());
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    public static boolean isArcsoftSupperZoomSupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.superzoom.arcsoft", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isArcsoftSupperZoomSupported(), enabled:" + enabled);
        return enabled;
    }

    public static boolean isArcsoftNightShotSupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.nightshot.arcsoft", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isArcsoftNightShotSupported(), enabled:" + enabled);
        return enabled;
    }

    public static boolean isArcsoftHDRSupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.hdr.arcsoft", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isArcsoftHDRSupported(), enabled:" + enabled);
        return enabled;
    }

    public static boolean isArcsoftSelfieSupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.selfie.arcsoft", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isArcsoftSelfieSupported(), enabled:" + enabled);
        return enabled;
    }

    public static boolean isFaceBeautyupported() {
        boolean enabled = SystemProperties.getInt("ro.pri_camera_fn_facebeauty", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isFaceBeautyupported(), enabled:" + enabled);
        return enabled;
    }

    /*prize-modify-add portrait mode -xiaoping-20181212-start*/
    public static boolean isPortraitupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.portrait.mode", 0) == 1 ? true : false;
        LogHelper.i(TAG, "isPortraitupported(), enabled:" + enabled);
        return enabled;
    }

    public static int getVideoModeIndex() {
        int index = 2;
        // zhangguo modify 20190425, delete portrait mode from slidebar start
        /*if (isPortraitupported() && isArcsoftNightShotSupported()) {
            index = 2;
        } else if (isPortraitupported() || isArcsoftNightShotSupported()) {
            index = 1;
        }*/
        if (isArcsoftNightShotSupported()) {
            index = 2;
        }
        // zhangguo modify 20190425, delete portrait mode from slidebar end
        else {
            index = 1;
        }
        return index;
    }
    /*prize-modify-add portrait mode -xiaoping-20181212-end*/

    /*prize-modify-add False focus for front camera-xiaoping-20181214-start*/
    public static boolean isFalseFocusSupported() {
        boolean enabled = SystemProperties.getInt("ro.pri.false.focus", 0) == 1 ? true : false;
        LogHelper.i(TAG,""+enabled);
        return enabled;
    }
    /*prize-modify-add False focus for front camera-xiaoping-20181214-end*/

    /*prize-add-huangpengfei-2019-1-23-start*/
    public static boolean isFrontModeNormal() {
        boolean normal = SystemProperties.getInt("ro.pri_def_front_mode_normal", 0) == 1 ? true : false;
        LogHelper.i(TAG,"[isFrontModeNormal]  normal = "+normal);
        return normal;
    }
    /*prize-add-huangpengfei-2019-1-23-end*/

    /*prize-modify-add front lift camera interaction-xiaoping-20190304-start*/
    public static boolean isLiftCameraSupport() {
        boolean isSupport = SystemProperties.getInt("ro.pri.liftcamera.support", 0) == 1 ? true : false;
        LogHelper.i(TAG,"isLiftCameraSupport = "+isSupport);
        return isSupport;
    }
    /*prize-modify-add front lift camera interaction-xiaoping-20190304-end*/

    /**
     * add for xiaoping 20190320
     * 
     * Get the switch value of the project to check the code
     * value 0:default value,Does not represent any project
     * value 1:k6203q Blu & KD
     * value 2:k6203q vs
     * value 3:K6309Q_Allview
     * value 4:K6309Q_Condor
     * @return
     */
    public static int getCurrentProjectValue() {
        int value = SystemProperties.getInt("ro.pri.current.project",0);
        LogHelper.i(TAG,"current project tag = "+value);
        return value;
    }

    /*prize-modify-Configure switches and defaults for each camera setting item-xiaoping-20190328-start*/
    public static String getStoragePathDefaultValue() {
        String value = android.os.SystemProperties.get("ro.pri.storagepath.default.value", "phone");
        LogHelper.i(TAG,"StoragePathDefaultValue: "+value);
        return value;
    }

    public static String getBrandWaterDefaultValue() {
        String value = android.os.SystemProperties.get("ro.pri.brandwatermark.default.value", VALUE_ON);
        LogHelper.i(TAG,"BrandWaterDefaultValue: "+value);
        return value;
    }

    public static String getDefaultVideoSize(int cameraid) {
        String value = null;
        if (cameraid == 1) {
            value = android.os.SystemProperties.get("ro.pri.front.default.videosize", null);
        } else {
            value = android.os.SystemProperties.get("ro.pri.back.default.videosize", null);
        }
        LogHelper.i(TAG,"cameraid: "+cameraid+",DefaultVideoSize: "+value);
        return value;
    }
    /*prize-modify-Configure switches and defaults for each camera setting item-xiaoping-20190328-end*/

    /*prize-mdoify-feature add settings of continuousshotnum-xiaoping-20190403-start*/
    public static String getDefaultShouNum() {
        String value = android.os.SystemProperties.get("ro.pri.continuousshotnum.default.value", "20");
        LogHelper.i(TAG,"defaultShouNum: "+value);
        return value;
    }

    public static boolean isContinuousShotnumSupport() {
        boolean isSupport = SystemProperties.getInt("ro.pri.continuousshotnum.on", 0) == 1 ? true : false;
        LogHelper.i(TAG,"isContinuousShotnumSupport = "+isSupport);
        return isSupport;
    }
    /*prize-mdoify-feature add settings of continuousshotnum-xiaoping-20190403-end*/
	
    public static boolean isSupportPlugin(){
        return SystemProperties.getInt("ro.pri.mode.plugin", 0) == 1;
    }

    public static boolean isSupportDualCam(){
        return "1".equals(android.os.SystemProperties.get("ro.pri.mode.aperture", "0"));
    }

    public static boolean isSupportNightCam(){
        return "1".equals(android.os.SystemProperties.get("ro.pri.mode.nightcam", "0"));
    }

    public static boolean isSupportSlowMotion(){
        return SystemProperties.getInt("ro.vendor.mtk_slow_motion_support", 0) == 1;
    }

    public static boolean isSupportUVSelfie(){
        return "1".equals(android.os.SystemProperties.get("ro.pri.feature.uvbokeh", "0"));
    }

    public static boolean isAiSupport(){
        return "1".equals(android.os.SystemProperties.get("ro.pri.ai.scene", "0"));
    }

    /**add for xiaoping 20190428 Configure the focus mode of the video
     * value:0 Default focus mode continuous-video
     * value 1:Preview autofocus, video requires manual focus
     * value 2:Video preview and video require manual focus  macro
     * @return
     */
    public static int getVideoFocusMode() {
        int value = SystemProperties.getInt("ro.pri.current.project",0);
        LogHelper.i(TAG,"getVideoFocusMode: "+value);
        return value;
    }
}
