package com.prize.camera.feature.mode.pano;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class PanoRestriction {

    private static final String PRIZEPANORAMA_MODE_KEY =
            "com.prize.camera.feature.mode.panorama.PanoMode";
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
    private static final String KEY_ZSD = "key_zsd";
    private static final String KEY_DUAL_ZOOM = "key_dual_zoom";
    private static final String KEY_CSHOT = "key_continuous_shot";
    private static final String KEY_INO_HDR = "key_ino_hdr";
    private static final String KEY_FOCUS = "key_focus";


    static {
        sRelationGroup.setHeaderKey(PRIZEPANORAMA_MODE_KEY);
        sRelationGroup.setBodyKeys(
                KEY_CSHOT + "," + KEY_FLASH + "," + KEY_FACE_DETECTION + "," +
                        KEY_HDR + "," + KEY_ZSD + "," + KEY_DNG + "," + KEY_SELF_TIMER + "," +
                        KEY_SCENE_MODE + "," +
                        KEY_COLOR_EFFECT + "," + KEY_AIS);
        sRelationGroup.addRelation(
                new Relation.Builder(PRIZEPANORAMA_MODE_KEY, "on")
                        .addBody(KEY_CSHOT, "off", "off")
                        .addBody(KEY_FLASH, "off", "off")
                        .addBody(KEY_FACE_DETECTION, "off", "off")
                        .addBody(KEY_HDR, "off", "off")
                        .addBody(KEY_ZSD, "off", "off")
                        .addBody(KEY_DNG, "off", "off")
                        .addBody(KEY_SELF_TIMER, "0", "0")
                        .addBody(KEY_SCENE_MODE, "off", "off")
                        .addBody(KEY_COLOR_EFFECT, "none", "none")
                        .addBody(KEY_AIS, "off", "off")
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

    static RelationGroup get3aRestriction(){
        return s3aRelation;
    }

    private static RelationGroup s3aRelation = new RelationGroup();
    static {
        s3aRelation.setHeaderKey(PRIZEPANORAMA_MODE_KEY);
        s3aRelation.setBodyKeys(KEY_FOCUS + "," + KEY_EXPOSURE + "," + KEY_WHITE_BALANCE
                + "," + KEY_DUAL_ZOOM);
        s3aRelation.addRelation(
                new Relation.Builder(PRIZEPANORAMA_MODE_KEY, "on")
                        .addBody(KEY_FOCUS, "auto", "auto")
                        .addBody(KEY_EXPOSURE, "exposure-lock", "true")
                        .addBody(KEY_WHITE_BALANCE, "white-balance-lock", "true")
                        .addBody(KEY_DUAL_ZOOM, "limit", "limit")
                        .build());
        s3aRelation.addRelation(
                new Relation.Builder(PRIZEPANORAMA_MODE_KEY, "off")
                        .addBody(KEY_FOCUS, null, null)
                        .addBody(KEY_EXPOSURE, "exposure-lock", "false")
                        .addBody(KEY_WHITE_BALANCE, "white-balance-lock", "false")
                        .addBody(KEY_DUAL_ZOOM, "on", "on, off")
                        .build());
    }
}
