/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.loader;

import android.content.Context;
import android.os.ConditionVariable;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.debug.profiler.IPerformanceProfile;
import com.mediatek.camera.common.debug.profiler.PerformanceTracker;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.hdr.HdrModeEntry;
import com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry;
import com.mediatek.camera.common.mode.photo.PhotoModeEntry;
import com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry;
import com.mediatek.camera.common.mode.video.VideoModeEntry;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
import com.mediatek.camera.feature.mode.panorama.PanoramaEntry;
import com.mediatek.camera.feature.mode.longexposure.LongExposureModeEntry;
import com.mediatek.camera.feature.setting.antiflicker.AntiFlickerEntry;
import com.mediatek.camera.feature.setting.CameraSwitcherEntry;
import com.mediatek.camera.feature.setting.ContinuousShotEntry;
import com.mediatek.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry;
import com.mediatek.camera.feature.setting.ais.AISEntry;
import com.mediatek.camera.feature.setting.dng.DngEntry;
import com.mediatek.camera.feature.setting.eis.EISEntry;
import com.mediatek.camera.feature.setting.exposure.ExposureEntry;
import com.mediatek.camera.feature.setting.facedetection.FaceDetectionEntry;
import com.mediatek.camera.feature.setting.flash.FlashEntry;
import com.mediatek.camera.feature.setting.focus.FocusEntry;
import com.mediatek.camera.feature.setting.format.FormatEntry;
import com.mediatek.camera.feature.setting.hdr.HdrEntry;
import com.mediatek.camera.feature.setting.iso.ISOEntry;
import com.mediatek.camera.feature.setting.microphone.MicroPhoneEntry;
import com.mediatek.camera.feature.setting.noisereduction.NoiseReductionEntry;
import com.mediatek.camera.feature.setting.picturesize.PictureSizeEntry;
import com.mediatek.camera.feature.setting.previewmode.PreviewModeEntry;
import com.mediatek.camera.feature.setting.scenemode.SceneModeEntry;
import com.mediatek.camera.feature.setting.selftimer.SelfTimerEntry;
import com.mediatek.camera.feature.setting.shutterspeed.ShutterSpeedEntry;
import com.mediatek.camera.feature.setting.videoquality.VideoQualityEntry;
import com.mediatek.camera.feature.setting.whitebalance.WhiteBalanceEntry;
import com.mediatek.camera.feature.setting.zoom.ZoomEntry;
import com.mediatek.camera.feature.setting.zsd.ZSDEntry;
import com.mediatek.camera.feature.setting.postview.PostViewEntry;
import com.mediatek.plugin.PluginManager;
import com.mediatek.plugin.PluginManager.PreloaderListener;
import com.mediatek.plugin.PluginUtility;
import com.mediatek.plugin.element.Extension;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*prize-modify-add mode-xiaoping-20180901-start*/
import com.mediatek.camera.feature.setting.beautyparameter.BeautyParameterEntry;
import com.mediatek.camera.feature.setting.picturezoom.PictureZoomParameterEntry;
import com.mediatek.camera.feature.setting.lowlight.LowLightParameterEntry;
import com.mediatek.camera.feature.setting.picselfie.PicselfieParameterEntry;
import com.mediatek.camera.feature.setting.watermark.WatermarkEntry;
import com.mediatek.camera.feature.setting.mirror.MirrorEntry;
import com.mediatek.camera.common.mode.beauty.FaceBeautyModeEntry;
import com.mediatek.camera.common.mode.lowlight.LowLightModeEntry;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomModeEntry;
import com.mediatek.camera.feature.setting.grid.GridEntry;
import com.mediatek.camera.feature.setting.volumekeys.VolumekeysEntry;
import com.mediatek.camera.feature.setting.cameramute.CameraMuteEntry;
import com.mediatek.camera.feature.setting.touchshutter.TouchShutterEntry;
import com.mediatek.camera.feature.setting.continuousshotnum.ContinuousShotNumEntry;
import com.mediatek.camera.common.mode.picselfie.PicselfieModeEntry;
import com.mediatek.camera.feature.setting.storagepath.StoragePathEntry;
import com.mediatek.camera.feature.setting.location.LocationEntry;
import com.mediatek.camera.feature.setting.ai.AiEntry;
import com.mediatek.camera.common.mode.professional.ProfessionalModeEntry;
import com.mediatek.camera.feature.setting.videomute.VideoMuteEntry;
/*prize-modify-add mode-xiaoping-20180901-end*/
/*prize-add-screen flash-huangzhanbin-20190226-start*/
import com.mediatek.camera.feature.setting.screenflash.ScreenFlashEntry;
import com.prize.camera.feature.mode.gif.GifModeEntry;
import com.prize.camera.feature.mode.filter.FilterModeEntry;
import com.prize.camera.feature.mode.pano.PanoModeEntry;
import com.prize.camera.feature.mode.smartscan.SmartScanModeEntry;
/*prize-add-screen flash-huangzhanbin-20190226-end*/

import javax.annotation.Nonnull;
/*prize-modify-open eis for k6309 AW-xiaoping-20190306-start*/
import android.os.SystemProperties;
/*prize-modify-open eis for k6309 AW-xiaoping-20190306-end*/
/**
 * Used for load the features.
 */
public class FeatureLoader {
    private static final Tag TAG = new Tag(FeatureLoader.class.getSimpleName());
    private static final String CAMERA_SWITCH = "com.mediatek.camera.feature.setting.CameraSwitcherEntry";
    private static final String CONTINUOUSSHOT = "com.mediatek.camera.feature.setting.ContinuousShotEntry";
    private static final String DNG = "com.mediatek.camera.feature.setting.dng.DngEntry";
    private static final String SELFTIME = "com.mediatek.camera.feature.setting.selftimer.SelfTimerEntry";
    private static final String FACE_DETECTION = "com.mediatek.camera.feature.setting.facedetection.FaceDetectionEntry";
    private static final String FLASH = "com.mediatek.camera.feature.setting.flash.FlashEntry";
    private static final String HDR = "com.mediatek.camera.feature.setting.hdr.HdrEntry";
    private static final String PICTURE_SIZE = "com.mediatek.camera.feature.setting.picturesize.PictureSizeEntry";
    private static final String PREVIEW_MODE = "com.mediatek.camera.feature.setting.previewmode.PreviewModeEntry";
    private static final String VIDEO_QUALITY = "com.mediatek.camera.feature.setting.videoquality.VideoQualityEntry";
    private static final String ZOOM = "com.mediatek.camera.feature.setting.zoom.ZoomEntry";
    private static final String FOCUS = "com.mediatek.camera.feature.setting.focus.FocusEntry";
    private static final String EXPOSURE = "com.mediatek.camera.feature.setting.exposure.ExposureEntry";
    private static final String MICHROPHONE = "com.mediatek.camera.feature.setting.microphone.MicroPhoneEntry";
    private static final String NOISE_REDUCTION = "com.mediatek.camera.feature.setting.noisereduction.NoiseReductionEntry";
    private static final String EIS = "com.mediatek.camera.feature.setting.eis.EISEntry";
    private static final String AIS = "com.mediatek.camera.feature.setting.ais.AISEntry";
    private static final String SCENE_MODE = "com.mediatek.camera.feature.setting.scenemode.SceneModeEntry";
    private static final String WHITE_BALANCE = "com.mediatek.camera.feature.setting.whitebalance.WhiteBalanceEntry";
    private static final String ANTI_FLICKER = "com.mediatek.camera.feature.setting.antiflicker.AntiFlickerEntry";
    private static final String ZSD = "com.mediatek.camera.feature.setting.zsd.ZSDEntry";
    private static final String ISO = "com.mediatek.camera.feature.setting.iso.ISOEntry";
    private static final String AE_AF_DEBUG = "com.mediatek.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry";
    private static final String SDOF_PHOTO_MODE = "com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry";
    private static final String PANORAMA_MODE = "com.mediatek.camera.feature.mode.panorama.PanoramaEntry";
    private static final String SHUTTER_SPEED = "com.mediatek.camera.feature.setting.shutterspeed.ShutterSpeedEntry";
    private static final String LONG_EXPUSURE_MODE = "com.mediatek.camera.feature.mode.longexposure.LongExposureModeEntry";
    private static final String PHOTO_MODE = "com.mediatek.camera.common.mode.photo.PhotoModeEntry";
    private static final String VIDEO_MODE = "com.mediatek.camera.common.mode.video.VideoModeEntry";
    private static final String INTENT_PHOTO_MODE = "com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry";
    private static final String INTENT_VIDEO_MODE = "com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry";
    private static final String SLOW_MOTION_MODE = "com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry";
    private static final String FORMATS = "com.mediatek.camera.feature.setting.format.FormatEntry";

    /*prize-modify-add mode-xiaoping-20180901-start*/
    private static final String BEAUTY_MODE = "com.mediatek.camera.common.mode.beauty.FaceBeautyModeEntry";
    private static final String BEAUTY_PARAMETER = "com.mediatek.camera.feature.setting.beautyparameter.BeautyParameterEntry";
    private static final String PICTUREZOOM_MODE = "com.mediatek.camera.common.mode.picturezoom.PictureZoomModeEntry";
    private static final String PICTUREZOOM_PARAMETER = "com.mediatek.camera.feature.setting.picturezoom.PictureZoomParameterEntry";
    private static final String LOWLIGHT_MODE = "com.mediatek.camera.common.mode.lowlight.LowLightModeEntry";
    private static final String LOWLIGHT_PARAMETER = "com.mediatek.camera.feature.setting.lowlight.LowLightParameterEntry";
    private static final String PICSELFIE_PARAMETER = "com.mediatek.camera.feature.setting.picselfie.PicselfieParameterEntry";
    private static final String WATERMARK = "com.mediatek.camera.feature.setting.watermark.WatermarkEntry";
    private static final String MIRROR = "com.mediatek.camera.feature.setting.mirror.MirrorEntry";
	private static final String GRID = "com.mediatek.camera.feature.setting.grid.GridEntry";
    private static final String VOLUMEKEYS = "com.mediatek.camera.feature.setting.volumekeys.VolumekeysEntry";
	private static final String CAMERAMUTE = "com.mediatek.camera.feature.setting.cameramute.CameraMuteEntry";
    private static final String CONTINUOUSSHOTNUM = "com.mediatek.camera.feature.setting.continuousshotnum.ContinuousShotNumEntry";
    private static final String TOUCHSHUTTER = "com.mediatek.camera.feature.setting.touchshutter.TouchShutterEntry";
    private static final String PICSELFIE_MODE = "com.mediatek.camera.common.mode.picselfie.PicselfieModeEntry";
    private static final String STORAGEPATH = "com.mediatek.camera.feature.setting.storagepath.StoragePathEntry";
    private static final String LOCATION = "com.mediatek.camera.feature.setting.location.LocationEntry";
	private static final String AI = "com.mediatek.camera.feature.setting.ai.AiEntry";
	private static final String PROFESSIONAL_MODE = "com.mediatek.camera.common.mode.professional.ProfessionalModeEntry";
    private static final String VIDEOMUTE = "com.mediatek.camera.feature.setting.videomute.VideoMuteEntry";
    /*prize-modify-add mode-xiaoping-20180901-end*/
    /*prize-add-screen flash-huangzhanbin-20190226-start*/
    private static final String SCREEN_FLASH = "com.mediatek.camera.feature.setting.screenflash.ScreenFlashEntry";
    private static final String GIF_MODE = "com.prize.camera.feature.mode.gif.GifModeEntry";
    private static final String PRIZE_FILTER_MODE = "com.prize.camera.feature.mode.filter.FilterModeEntry";
    private static final String PRIZE_PANO_MODE = "com.prize.camera.feature.mode.pano.PanoModeEntry";
    private static final String SMART_SCAN_MODE = "com.prize.camera.feature.mode.smartscan.SmartScanModeEntry";
    /*prize-add-screen flash-huangzhanbin-20190226-end*/
    private static final String HDR_MODE_ENTRY = "com.mediatek.camera.common.mode.hdr.HdrModeEntry";

    private static ConcurrentHashMap<String, IFeatureEntry>
            sBuildInEntries = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, IFeatureEntry>
            sPluginEntries = new ConcurrentHashMap<>();

    /**
     * Update current mode key to feature entry, dual camera zoom need to set properties
     * in photo and video mode before open camera, this notify only update to setting feature.
     * @param context current application context.
     * @param currentModeKey current mode key.
     */
    public static void updateSettingCurrentModeKey(@Nonnull Context context,
                                                   @Nonnull String currentModeKey) {
        LogHelper.d(TAG, "[updateCurrentModeKey] current mode key:" + currentModeKey);
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
    }

    /**
     * Notify setting feature before open camera, this event only need to notify setting feature.
     * @param context the context.
     * @param cameraId want to open which camera.
     * @param cameraApi use which api.
     */
    public static void notifySettingBeforeOpenCamera(@Nonnull Context context,
                                                     @Nonnull String cameraId,
                                                     @Nonnull CameraApi cameraApi) {
        LogHelper.d(TAG, "[notifySettingBeforeOpenCamera] id:" + cameraId + ", api:" + cameraApi);
        //don't consider plugin feature? because plugin feature need more time to load
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
        Iterator iterator = sBuildInEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry item = (Map.Entry) iterator.next();
            IFeatureEntry entry = (IFeatureEntry) item.getValue();
            if (ICameraSetting.class.equals(entry.getType())) {
                entry.notifyBeforeOpenCamera(cameraId, cameraApi);
            }
        }
    }

    /**
     * Load plugin feature entries, should be called in non-ui thread.
     * @param context the application context.
     * @return the plugin features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadPluginFeatures(
            final Context context) {
        return sPluginEntries;
    }

    /**
     * Load build in feature entries, should be called in non-ui thread.
     * @param context the application context.
     * @return the build-in features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadBuildInFeatures(Context context) {
        if (sBuildInEntries.size() > 0) {
            return sBuildInEntries;
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG,
                "Build-in Loading");
        profile.start();
        sBuildInEntries = new ConcurrentHashMap<>(loadClasses(context));
        profile.stop();
        return sBuildInEntries;
    }

    private static LinkedHashMap<String, IFeatureEntry> loadClasses(Context context) {
        LinkedHashMap<String, IFeatureEntry> entries = new LinkedHashMap<>();
        DeviceSpec deviceSpec = CameraApiHelper.getDeviceSpec(context);
		/*prize-remove-huangpengfei-2018-11-15-start*/
        /*IFeatureEntry postviewEntry = new PostViewEntry(context, context.getResources());
        postviewEntry.setDeviceSpec(deviceSpec);
        entries.put(POST_VIEW, postviewEntry);*/
		/*prize-remove-huangpengfei-2018-11-15-start*/
        IFeatureEntry cameraSwitchEntry = new CameraSwitcherEntry(context, context.getResources());
        cameraSwitchEntry.setDeviceSpec(deviceSpec);
        entries.put(CAMERA_SWITCH, cameraSwitchEntry);

        IFeatureEntry continuousShotEntry = new ContinuousShotEntry(context,
                context.getResources());
        continuousShotEntry.setDeviceSpec(deviceSpec);
        entries.put(CONTINUOUSSHOT, continuousShotEntry);

        /*prize-remove-huangpengfei-2018-11-15-start*/
        /*IFeatureEntry dngEntry = new DngEntry(context, context.getResources());
        dngEntry.setDeviceSpec(deviceSpec);
        entries.put(DNG, dngEntry);*/
        /*prize-remove-huangpengfei-2018-11-15-end*/

        IFeatureEntry selfTimeEntry = new SelfTimerEntry(context, context.getResources());
        selfTimeEntry.setDeviceSpec(deviceSpec);
        entries.put(SELFTIME, selfTimeEntry);

        IFeatureEntry faceDetectionEntry = new FaceDetectionEntry(context, context.getResources());
        faceDetectionEntry.setDeviceSpec(deviceSpec);
        entries.put(FACE_DETECTION, faceDetectionEntry);

        IFeatureEntry flashEntry = new FlashEntry(context, context.getResources());
        flashEntry.setDeviceSpec(deviceSpec);
        entries.put(FLASH, flashEntry);

        IFeatureEntry hdrEntry = new HdrEntry(context, context.getResources());
        hdrEntry.setDeviceSpec(deviceSpec);
        entries.put(HDR, hdrEntry);

        IFeatureEntry pictureSizeEntry = new PictureSizeEntry(context, context.getResources());
        pictureSizeEntry.setDeviceSpec(deviceSpec);
        entries.put(PICTURE_SIZE, pictureSizeEntry);

        IFeatureEntry previewModeEntry = new PreviewModeEntry(context, context.getResources());
        previewModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PREVIEW_MODE, previewModeEntry);

        IFeatureEntry videoQualityEntry = new VideoQualityEntry(context, context.getResources());
        videoQualityEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_QUALITY, videoQualityEntry);

        IFeatureEntry zoomEntry = new ZoomEntry(context, context.getResources());
        zoomEntry.setDeviceSpec(deviceSpec);
        entries.put(ZOOM, zoomEntry);

        IFeatureEntry focusEntry = new FocusEntry(context, context.getResources());
        focusEntry.setDeviceSpec(deviceSpec);
        entries.put(FOCUS, focusEntry);

        IFeatureEntry exposureEntry = new ExposureEntry(context, context.getResources());
        exposureEntry.setDeviceSpec(deviceSpec);
        entries.put(EXPOSURE, exposureEntry);

        IFeatureEntry microPhoneEntry = new MicroPhoneEntry(context, context.getResources());
        microPhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(MICHROPHONE, microPhoneEntry);

        IFeatureEntry noiseReductionEntry = new NoiseReductionEntry(context, context.getResources());
        noiseReductionEntry.setDeviceSpec(deviceSpec);
        entries.put(NOISE_REDUCTION, noiseReductionEntry);

        /*prize-modify-open eis for k6309 AW-xiaoping-20190306-start*/
            IFeatureEntry EisPhoneEntry = new EISEntry(context, context.getResources());
            EisPhoneEntry.setDeviceSpec(deviceSpec);
            entries.put(EIS, EisPhoneEntry);
        /*prize-modify-open eis for k6309 AW-xiaoping-20190306-end*/
        IFeatureEntry aisEntry = new AISEntry(context, context.getResources());
        aisEntry.setDeviceSpec(deviceSpec);
        entries.put(AIS, aisEntry);

        IFeatureEntry sceneModeEntry = new SceneModeEntry(context, context.getResources());
        sceneModeEntry.setDeviceSpec(deviceSpec);
        entries.put(SCENE_MODE, sceneModeEntry);

        IFeatureEntry whiteBalanceEntry = new WhiteBalanceEntry(context, context.getResources());
        whiteBalanceEntry.setDeviceSpec(deviceSpec);
        entries.put(WHITE_BALANCE, whiteBalanceEntry);

        IFeatureEntry antiFlickerEntry = new AntiFlickerEntry(context, context.getResources());
        antiFlickerEntry.setDeviceSpec(deviceSpec);
        entries.put(ANTI_FLICKER, antiFlickerEntry);

        IFeatureEntry zsdEntry = new ZSDEntry(context, context.getResources());
        zsdEntry.setDeviceSpec(deviceSpec);
        entries.put(ZSD, zsdEntry);

        IFeatureEntry isoEntry = new ISOEntry(context, context.getResources());
        isoEntry.setDeviceSpec(deviceSpec);
        entries.put(ISO, isoEntry);

        IFeatureEntry aeAfDebugEntry = new AaaRoiDebugEntry(context, context.getResources());
        aeAfDebugEntry.setDeviceSpec(deviceSpec);
        entries.put(AE_AF_DEBUG, aeAfDebugEntry);

        if (("1").equals(android.os.SystemProperties.get("ro.pri.mode.aperture", "0"))) {
            IFeatureEntry sDofPhotoEntry = new SdofPhotoEntry(context, context.getResources());
            sDofPhotoEntry.setDeviceSpec(deviceSpec);
            entries.put(SDOF_PHOTO_MODE, sDofPhotoEntry);
        }

        /*IFeatureEntry panoramaEntry = new PanoramaEntry(context, context.getResources());
        panoramaEntry.setDeviceSpec(deviceSpec);
        entries.put(PANORAMA_MODE, panoramaEntry);*/

        IFeatureEntry shutterSpeedEntry = new ShutterSpeedEntry(context, context.getResources());
        shutterSpeedEntry.setDeviceSpec(deviceSpec);
        entries.put(SHUTTER_SPEED, shutterSpeedEntry);

        IFeatureEntry longExposureEntry = new LongExposureModeEntry(context,
                context.getResources());
        longExposureEntry.setDeviceSpec(deviceSpec);
        entries.put(LONG_EXPUSURE_MODE, longExposureEntry);

        IFeatureEntry photoEntry = new PhotoModeEntry(context, context.getResources());
        photoEntry.setDeviceSpec(deviceSpec);
        entries.put(PHOTO_MODE, photoEntry);

        IFeatureEntry videoEntry = new VideoModeEntry(context, context.getResources());
        videoEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_MODE, videoEntry);

        IFeatureEntry intentVideoEntry = new IntentVideoModeEntry(context, context.getResources());
        intentVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_VIDEO_MODE, intentVideoEntry);

        IFeatureEntry intentPhotoEntry = new IntentPhotoModeEntry(context, context.getResources());
        intentPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_PHOTO_MODE, intentPhotoEntry);

        IFeatureEntry slowMotionEntry = new SlowMotionEntry(context, context.getResources());
        slowMotionEntry.setDeviceSpec(deviceSpec);
        entries.put(SLOW_MOTION_MODE, slowMotionEntry);

        IFeatureEntry formatsEntry = new FormatEntry(context, context.getResources());
        formatsEntry.setDeviceSpec(deviceSpec);
        entries.put(FORMATS, formatsEntry);

        /*prize-add-add mode-xiaoping-20180901-start*/
        if (("1").equals(android.os.SystemProperties.get("ro.pri_camera_fn_facebeauty", "0"))) {
            IFeatureEntry faceBeautyEntry = new FaceBeautyModeEntry(context, context.getResources());
            faceBeautyEntry.setDeviceSpec(deviceSpec);
            entries.put(BEAUTY_MODE, faceBeautyEntry);

            IFeatureEntry beautyParameterEntry = new BeautyParameterEntry(context, context.getResources());
            beautyParameterEntry.setDeviceSpec(deviceSpec);
            entries.put(BEAUTY_PARAMETER, beautyParameterEntry);
        }

        if (("1").equals(android.os.SystemProperties.get("ro.pri.superzoom.arcsoft", "0"))) {
            IFeatureEntry picturezoomEntry = new PictureZoomModeEntry(context, context.getResources());
            picturezoomEntry.setDeviceSpec(deviceSpec);
            entries.put(PICTUREZOOM_MODE, picturezoomEntry);

            IFeatureEntry pictureZoomParameterEntry = new PictureZoomParameterEntry(context, context.getResources());
            pictureZoomParameterEntry.setDeviceSpec(deviceSpec);
            entries.put(PICTUREZOOM_PARAMETER, pictureZoomParameterEntry);
        }

        if (("1").equals(android.os.SystemProperties.get("ro.pri.nightshot.arcsoft", "0"))) {
            IFeatureEntry lowLightModeEntry = new LowLightModeEntry(context, context.getResources());
            lowLightModeEntry.setDeviceSpec(deviceSpec);
            entries.put(LOWLIGHT_MODE, lowLightModeEntry);

            IFeatureEntry lowlightParameterEntry = new LowLightParameterEntry(context, context.getResources());
            lowlightParameterEntry.setDeviceSpec(deviceSpec);
            entries.put(LOWLIGHT_PARAMETER, lowlightParameterEntry);
        }

        if (("1").equals(android.os.SystemProperties.get("ro.pri.selfie.arcsoft", "0")) || ("1").equals(android.os.SystemProperties.get("ro.pri.portrait.mode", "0"))) {
            IFeatureEntry picselfieParameterEntry = new PicselfieParameterEntry(context, context.getResources());
            picselfieParameterEntry.setDeviceSpec(deviceSpec);
            entries.put(PICSELFIE_PARAMETER, picselfieParameterEntry);
        }

        if(("1").equals(android.os.SystemProperties.get("ro.pri.brand.watermark", "0"))){
            IFeatureEntry watermarkEntry = new WatermarkEntry(context, context.getResources());
            watermarkEntry.setDeviceSpec(deviceSpec);
            entries.put(WATERMARK, watermarkEntry);
        }

        IFeatureEntry mirrorEntry = new MirrorEntry(context, context.getResources());
        mirrorEntry.setDeviceSpec(deviceSpec);
        entries.put(MIRROR, mirrorEntry);

        IFeatureEntry touchShutterEntry = new TouchShutterEntry(context, context.getResources());
        touchShutterEntry.setDeviceSpec(deviceSpec);
        entries.put(TOUCHSHUTTER, touchShutterEntry);

        if (("1").equals(android.os.SystemProperties.get("ro.pri.continuousshotnum.on", "0"))) {
            IFeatureEntry continuousShotNumEntry = new ContinuousShotNumEntry(context, context.getResources());
            continuousShotNumEntry.setDeviceSpec(deviceSpec);
            entries.put(CONTINUOUSSHOTNUM, continuousShotNumEntry);
        }
		IFeatureEntry gridEntry = new GridEntry(context, context.getResources());
        gridEntry.setDeviceSpec(deviceSpec);
        entries.put(GRID, gridEntry);

        IFeatureEntry volumekeysEntry = new VolumekeysEntry(context, context.getResources());
        volumekeysEntry.setDeviceSpec(deviceSpec);
        entries.put(VOLUMEKEYS, volumekeysEntry);

        IFeatureEntry cameraMuteEntry = new CameraMuteEntry(context, context.getResources());
        cameraMuteEntry.setDeviceSpec(deviceSpec);
        entries.put(CAMERAMUTE, cameraMuteEntry);

        if (("1").equals(android.os.SystemProperties.get("ro.pri.portrait.mode", "0"))) {
            IFeatureEntry picselfieEntry = new PicselfieModeEntry(context, context.getResources());
            picselfieEntry.setDeviceSpec(deviceSpec);
            entries.put(PICSELFIE_MODE,picselfieEntry);
        }

        /*prize-modify-increase external storage-xiaoping-20190111-start*/
        IFeatureEntry storagepathMuteEntry = new StoragePathEntry(context, context.getResources());
        storagepathMuteEntry.setDeviceSpec(deviceSpec);
        entries.put(STORAGEPATH, storagepathMuteEntry);
        /*prize-modify-increase external storage-xiaoping-20190111-end*/

        // add location info -xiaoping-20181221
        IFeatureEntry locationEntry = new LocationEntry(context, context.getResources());
        locationEntry.setDeviceSpec(deviceSpec);
        entries.put(LOCATION, locationEntry);

        if (("1").equals(android.os.SystemProperties.get("ro.pri.ai.scene", "0"))) {
            IFeatureEntry aiEntry = new AiEntry(context, context.getResources());
            aiEntry.setDeviceSpec(deviceSpec);
            entries.put(AI, aiEntry);
        }
        /*prize-add-add mode-xiaoping-20180901-end*/

        /*prize-modify-add professional mode function-xiaoping-20190216-start*/
        if (("1").equals(android.os.SystemProperties.get("ro.pri.professional.mode", "0"))) {
            IFeatureEntry professionalentry = new ProfessionalModeEntry(context, context.getResources());
            professionalentry.setDeviceSpec(deviceSpec);
            entries.put(PROFESSIONAL_MODE, professionalentry);
        }
        /*prize-modify-add professional mode function-xiaoping-20190216-end*/

        /*prize-add-screen flash-huangzhanbin-20190226-start*/
        if (("1").equals(android.os.SystemProperties.get("ro.pri.flash.front", "0"))) {
            IFeatureEntry screenFlashEntry = new ScreenFlashEntry(context, context.getResources());
            screenFlashEntry.setDeviceSpec(deviceSpec);
            entries.put(SCREEN_FLASH, screenFlashEntry);
        }
        /*prize-add-screen flash-huangzhanbin-20190226-end*/

        /*prize-modifu-add Increase recording mute switch control-xiaoping-20190320-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 2) {
            IFeatureEntry videoMuteEntry = new VideoMuteEntry(context, context.getResources());
            videoMuteEntry.setDeviceSpec(deviceSpec);
            entries.put(VIDEOMUTE, videoMuteEntry);
        }
        /*prize-modifu-add Increase recording mute switch control-xiaoping-20190320-end*/
		
        IFeatureEntry gifModeEntry = new GifModeEntry(context, context.getResources());
        gifModeEntry.setDeviceSpec(deviceSpec);
        entries.put(GIF_MODE, gifModeEntry);

        IFeatureEntry filterModeEntry = new FilterModeEntry(context, context.getResources());
        filterModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PRIZE_FILTER_MODE, filterModeEntry);

        IFeatureEntry panoModeEntry = new PanoModeEntry(context, context.getResources());
        panoModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PRIZE_PANO_MODE, panoModeEntry);

        IFeatureEntry smartScanEntry = new SmartScanModeEntry(context, context.getResources());
        smartScanEntry.setDeviceSpec(deviceSpec);
        entries.put(SMART_SCAN_MODE, smartScanEntry);
		
        IFeatureEntry hdrModeEntry = new HdrModeEntry(context, context.getResources());
        hdrModeEntry.setDeviceSpec(deviceSpec);
        entries.put(HDR_MODE_ENTRY, hdrModeEntry);

        return entries;
    }
}