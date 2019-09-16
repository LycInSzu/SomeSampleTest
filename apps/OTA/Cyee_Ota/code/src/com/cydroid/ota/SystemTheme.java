package com.cydroid.ota;

import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.Resources;
import com.cydroid.ota.R;
import com.cydroid.ota.ui.widget.ITheme;
import com.cydroid.ota.utils.SystemPropertiesUtils;

/**
 * Created by kangjj on 15-6-8.
 */
final public class SystemTheme {
    private static final String TAG = "SystemTheme";
    private int mDefAppbarColor_A1;
    private int mDefContentColorThirdlyOnBackgroud_C3;
    private int mDefContentColorSecondaryOnBackgroud_C2;
    private int mDefContentColorPrimaryOnBackgroud_C1;
    private int mDefAccentColor_G1;
    private int mDefBackgroudColor_B1;
    private int mDefStyle = 0;
    private static final int THEME_DARK = 2;
    private final ITheme.Chameleon sChameleon = new ITheme.Chameleon();

    protected SystemTheme(Context context) {
        Resources resources = context.getResources();
        if (SystemPropertiesUtils.getBlueStyle()){
        mDefAccentColor_G1 = resources.getColor(R.color.gn_su_anim_main_backcolor_blue);
        mDefAppbarColor_A1 = resources.getColor(R.color.gn_su_anim_main_backcolor_blue);
    } else {
        mDefAccentColor_G1 = resources.getColor(R.color.gn_su_anim_main_backcolor);
        mDefAppbarColor_A1 = resources.getColor(R.color.gn_su_anim_main_backcolor);
    }
        mDefBackgroudColor_B1 = resources.getColor(R.color.gn_su_anim_bg_defalut);
        mDefContentColorPrimaryOnBackgroud_C1 = resources.getColor(com.cyee.R.color.cyee_content_color_primary_on_backgroud_c1);
        mDefContentColorSecondaryOnBackgroud_C2 = resources.getColor(com.cyee.R.color.cyee_content_color_secondary_on_backgroud_c2);
        mDefContentColorThirdlyOnBackgroud_C3 = resources.getColor(com.cyee.R.color.cyee_content_color_thirdly_on_backgroud_c3);
    }

    /**
     * 1 light 2 dark
     * @return
     */
    private int getThemeStyle(){
        Log.d(TAG, "ThemeType=" + ChameleonColorManager.getThemeType());
        if (ChameleonColorManager.getThemeType() == THEME_DARK) {
            return mDefStyle;
        }
        return mDefStyle;
    }

    public boolean isLight() {
        return getThemeStyle() != 2;
    }

    public boolean isNeedChangeColor() {
        return ChameleonColorManager.isNeedChangeColor();
    }

    public ITheme.Chameleon getChameleon() {
        sChameleon.AppbarColor_A1 = getAppbarColor_A1();
        sChameleon.BackgroudColor_B1 = getBackgroudColor_B1();
        sChameleon.AccentColor_G1 = getAccentColor_G1();
        sChameleon.ContentColorPrimaryOnBackgroud_C1 = getContentColorPrimaryOnBackgroud_C1();
        sChameleon.ContentColorSecondaryOnBackgroud_C2 = getContentColorSecondaryOnBackgroud_C2();
        sChameleon.ContentColorThirdlyOnBackgroud_C3 = getContentColorThirdlyOnBackgroud_C3();
        return sChameleon;
    }

    private int getContentColorThirdlyOnBackgroud_C3() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3();
        }
        return mDefContentColorThirdlyOnBackgroud_C3;
    }

    private int getContentColorSecondaryOnBackgroud_C2() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager
                    .getContentColorSecondaryOnBackgroud_C2();
        }
        return mDefContentColorSecondaryOnBackgroud_C2;
    }

    private int getContentColorPrimaryOnBackgroud_C1() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
        }
        return mDefContentColorPrimaryOnBackgroud_C1;
    }

    private int getAccentColor_G1() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getAccentColor_G1();
        }
        return mDefAccentColor_G1;
    }

    private int getBackgroudColor_B1() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getBackgroudColor_B1();
        }
        return mDefBackgroudColor_B1;
    }

    private int getAppbarColor_A1() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            return ChameleonColorManager.getAppbarColor_A1();
        }
        return mDefAppbarColor_A1;
    }

    public void addTheme(ITheme theme) {
        if (theme != null) {
            theme.onChameleonChanged(sChameleon);
        }
    }
}
