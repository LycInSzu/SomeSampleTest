package com.prize.camera.feature.mode.gif;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class GifRestriction {

    private static final String GIF_MODE_KEY =
            "com.prize.camera.feature.mode.portrait.GifMode";
    private static final String VALUE_OFF = "off";
    private static RelationGroup sRelationGroup = new RelationGroup();


    private static final String KEY_EIS = "key_eis";
    private static final String KEY_SCENE_MODE = "key_scene_mode";
    private static final String KEY_MICROPHONE = "key_microphone";
    private static final String KEY_HDR = "key_hdr";
    private static final String KEY_COLOR_EFFECT = "key_color_effect";
    private static final String KEY_FLASH = "key_flash";
    private static final String KEY_WHITE_BALANCE = "key_white_balance";
    private static final String KEY_NOISE_REDUCTION = "key_noise_reduction";
    private static final String KEY_ANTI_FLICKER = "key_anti_flicker";
    private static final String KEY_IMAGE_PROPERTIES = "key_image_properties";
    private static final String KEY_BRIGHTNESS = "key_brightness";
    private static final String KEY_CONTRAST = "key_contrast";
    private static final String KEY_HUE = "key_hue";
    private static final String KEY_SATURATION = "key_saturation";
    private static final String KEY_SHARPNESS = "key_sharpness";
    private static final String KEY_FACE_DETECTION = "key_face_detection";
    private static final String KEY_AIS = "key_ais";
    private static final String KEY_DNG = "key_dng";
    private static final String KEY_SELF_TIMER = "key_self_timer";
    private static final String KEY_EXPOSURE = "key_exposure";
    private static final String KEY_DUAL_ZOOM = "key_dual_zoom";
    private static final String KEY_CSHOT = "key_continuous_shot";
    private static final String KEY_CAMERA_SWITCHER = "key_camera_switcher";
    private static final String KEY_SCREEN_FLASH = "key_screen_flash";


    static {
        sRelationGroup.setHeaderKey(GIF_MODE_KEY);
        sRelationGroup.setBodyKeys(
                KEY_EIS + "," +
                        KEY_SCENE_MODE + "," +
                        KEY_MICROPHONE + "," +
                        KEY_HDR + "," +
                        KEY_COLOR_EFFECT + "," +
                        KEY_FLASH + "," +
                        KEY_WHITE_BALANCE + "," +
                        KEY_NOISE_REDUCTION + "," +
                        KEY_ANTI_FLICKER + "," +
                        KEY_IMAGE_PROPERTIES + "," +
                        KEY_BRIGHTNESS + "," +
                        KEY_CONTRAST + "," +
                        KEY_HUE + "," +
                        KEY_SATURATION + "," +
                        KEY_SHARPNESS + "," +
                        KEY_FACE_DETECTION + "," +
                        KEY_AIS + "," +
                        KEY_DNG + "," +
                        //KEY_SELF_TIMER + "," +
                        KEY_EXPOSURE + "," +
                        KEY_DUAL_ZOOM + "," +
                        KEY_CSHOT + "," +
                        KEY_SCREEN_FLASH + "," +
                        KEY_CAMERA_SWITCHER);
        sRelationGroup.addRelation(
                new Relation.Builder(GIF_MODE_KEY, "on")
                        .addBody(KEY_EIS, VALUE_OFF, VALUE_OFF)
                        .addBody(KEY_SCENE_MODE, VALUE_OFF, VALUE_OFF)
                        .addBody(KEY_HDR, VALUE_OFF, VALUE_OFF)
                        .addBody(KEY_NOISE_REDUCTION, VALUE_OFF, VALUE_OFF)
                        .addBody(KEY_MICROPHONE, VALUE_OFF, VALUE_OFF)
                        .addBody(KEY_BRIGHTNESS, "middle", "middle")
                        .addBody(KEY_CONTRAST, "middle", "middle")
                        .addBody(KEY_HUE, "middle", "middle")
                        .addBody(KEY_SATURATION, "middle", "middle")
                        .addBody(KEY_SHARPNESS, "middle", "middle")
                        .addBody(KEY_WHITE_BALANCE, "auto", "auto")
                        .addBody(KEY_ANTI_FLICKER, "auto", "auto")
                        .addBody(KEY_COLOR_EFFECT, "none", "none")
                        .addBody(KEY_FACE_DETECTION, "off", "off")
                        .addBody(KEY_AIS, "off", "off")
                        .addBody(KEY_DNG, "off", "off")
                        //.addBody(KEY_SELF_TIMER, "0", "0")
                        .addBody(KEY_EXPOSURE, "0", "0")
                        .addBody(KEY_DUAL_ZOOM, "off", "off")
                        .addBody(KEY_CSHOT, "off", "off")
                        .addBody(KEY_FLASH, "off", "off")
                        .addBody(KEY_SCREEN_FLASH, "off", "off")
                        .build());
    }

    /**
     * Slow motion restriction witch are have setting ui.
     *
     * @return restriction list.
     */
    static RelationGroup getRestriction() {
        return sRelationGroup;
    }
}
