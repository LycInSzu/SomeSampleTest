package com.cydroid.ota.ui.widget;

/**
 * Created by kangjj on 15-6-8.
 */
public interface ITheme {

    void onChameleonChanged(Chameleon chameleon);

    final class Chameleon {
        public int AppbarColor_A1;
        public int BackgroudColor_B1;
        public int AccentColor_G1;
        public int ContentColorPrimaryOnBackgroud_C1;
        public int ContentColorSecondaryOnBackgroud_C2;
        public int ContentColorThirdlyOnBackgroud_C3;
    }
}
