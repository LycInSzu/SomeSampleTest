package com.cydroid.note.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import com.cydroid.note.common.Log;

import com.cydroid.note.R;

import cyee.changecolors.ChameleonColorManager;

/**
 * Created by spc on 16-10-12.
 */
public class ColorThemeHelper {
    private static final int DEFAULT_RIPPLE_COLOR = 0x33000000;
    private static final boolean sSupportChamelon = false;

    public static int getActionBarBgColor(Context context, boolean isSecuritySpace) {

        if (isSecuritySpace) {
            return ContextCompat.getColor(context, R.color.action_bar_private_space_color);
        }
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getAppbarColor_A1();
        }
        return PlatformUtil.isBusinessStyle() ?
                ContextCompat.getColor(context, R.color.abstract_note_activity_layout_head_default_bg_business_style)
                : ContextCompat.getColor(context, R.color.abstract_note_activity_layout_head_default_bg);
    }

    public static int getActionBarTextColor(Context context, boolean isSecuritySpace) {
        if (isSecuritySpace) {
            return ContextCompat.getColor(context, R.color.setting_title_bg);
        }
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
        }
        return ContextCompat.getColor(context, R.color.note_main_activity_title_layout_title_text_color);
    }

    public static ColorStateList getActionBarIconColor(Context context, boolean isSecuritySpace) {
        if (isSecuritySpace) {
           return new ColorStateList(
                    new int[][]{{-android.R.attr.state_enabled},
                            {android.R.attr.state_enabled}},
                    new int[]{
                            ContextCompat.getColor(context, R.color.new_note_title_share_color_disable),
                            ContextCompat.getColor(context, R.color.new_note_title_color_enable)});
        }
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return new ColorStateList(
                    new int[][]{{-android.R.attr.state_enabled},
                            {android.R.attr.state_enabled}},
                    new int[]{ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(),
                            ChameleonColorManager.getContentColorPrimaryOnAppbar_T1()});
        }
        ColorStateList businessstyle = new ColorStateList(
                new int[][]{{-android.R.attr.state_enabled},
                        {android.R.attr.state_enabled}},
                new int[]{
                        ContextCompat.getColor(context, R.color.new_note_title_share_color_disable_business_style),
                        ContextCompat.getColor(context, R.color.new_note_title_color_enable_business_style)});

        ColorStateList normalStyle = new ColorStateList(
                new int[][]{{-android.R.attr.state_enabled},
                        {android.R.attr.state_enabled}},
                new int[]{
                        ContextCompat.getColor(context, R.color.new_note_title_share_color_disable),
                        ContextCompat.getColor(context, R.color.new_note_title_color_enable)});
        return PlatformUtil.isBusinessStyle() ? businessstyle : normalStyle;
    }


    public static int getStatusBarColor(Context context, boolean isSecuritySpace) {

        if (isSecuritySpace) {
            return ContextCompat.getColor(context, R.color.action_bar_private_space_color);
        }
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getStatusbarBackgroudColor_S1();
        }
        return PlatformUtil.isBusinessStyle() ?
                ContextCompat.getColor(context, R.color.abstract_note_activity_layout_head_default_bg_business_style)
                : ContextCompat.getColor(context, R.color.abstract_note_activity_layout_head_default_bg);
    }

    public static int getRootViewBgColor(Context context, boolean isSecuritySpace) {
        if (isSecuritySpace) {
            return ContextCompat.getColor(context, R.color.abstract_note_activity_root_business_bg_color);
        }
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getBackgroudColor_B1();
        }
        return PlatformUtil.isBusinessStyle() ?
                ContextCompat.getColor(context, R.color.abstract_note_activity_root_business_bg_color)
                : ContextCompat.getColor(context, R.color.abstract_note_activity_root_bg_color);
    }

    public static ColorStateList setFooterBarTextColor(Context context, boolean isSecuritySpace) {
        if (isSecuritySpace
                || !(sSupportChamelon && ChameleonColorManager.isNeedChangeColor())) {
            return new ColorStateList(
                    new int[][]{{-android.R.attr.state_enabled},
                            {android.R.attr.state_enabled}},
                    new int[]{ContextCompat.getColor(context, R.color.action_bar_text_color_unabled),
                            ContextCompat.getColor(context, R.color.action_bar_text_color_normal)});
        }

        return new ColorStateList(
                new int[][]{{-android.R.attr.state_enabled},
                        {android.R.attr.state_enabled}},
                new int[]{ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(),
                        ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()});
    }

    public static ColorStateList getFooterBarIconColor(Context context, boolean isSecuritySpace) {
        if (isSecuritySpace
                || !(sSupportChamelon && ChameleonColorManager.isNeedChangeColor())) {
            ColorStateList colorLists = new ColorStateList(
                    new int[][]{{-android.R.attr.state_enabled},
                            {android.R.attr.state_enabled}},
                    new int[]{
                            ContextCompat.getColor(context, R.color.action_bar_image_color_unabled),
                            ContextCompat.getColor(context, R.color.action_bar_image_color_normal)});
            return colorLists;
        }
        return new ColorStateList(
                new int[][]{{-android.R.attr.state_enabled},
                        {android.R.attr.state_enabled}},
                new int[]{ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3(),
                        ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()});
    }

    public static boolean isDarkBgColor(Context context, boolean isSecuritySpace) {
        return (getRootViewBgColor(context, isSecuritySpace) & 0x00FFFFFF) < 0x00888888;
    }

    public static int getContentNormalTextColor(Context context, boolean isSecuritySpace) {
        return isDarkBgColor(context, isSecuritySpace) ? ContextCompat.getColor(context, R.color.big_text_dark_bg_color)
                : ContextCompat.getColor(context, R.color.big_text_white_bg_color);
    }

    public static int getContentSmallTextColor(Context context, boolean isSecuritySpace) {
        return isDarkBgColor(context, isSecuritySpace) ? ContextCompat.getColor(context, R.color.small_text_dark_bg_color)
                : ContextCompat.getColor(context, R.color.small_text_white_bg_color);
    }

    public static int getContentColorSecondaryOnBackgroud_C2(Context context) {
        if (sSupportChamelon && ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
        }
        return ContextCompat.getColor(context, R.color.default_ccs_color);
    }

    public static Drawable getPressBg(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ColorStateList colors = new ColorStateList(new int[][] { {android.R.attr.state_pressed},
                    {android.R.attr.state_focused}, {}}, new int[] {DEFAULT_RIPPLE_COLOR,
                    DEFAULT_RIPPLE_COLOR, DEFAULT_RIPPLE_COLOR});
            return new RippleDrawable(colors, null, null);
        } else {
            final Drawable pressedDrawable = new ColorDrawable(DEFAULT_RIPPLE_COLOR);
            StateListDrawable bg = new StateListDrawable();
            bg.addState(new int[] {android.R.attr.state_pressed}, pressedDrawable);
            return bg;
        }
    }

}
