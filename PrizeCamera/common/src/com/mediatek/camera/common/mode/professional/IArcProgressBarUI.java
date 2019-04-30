package com.mediatek.camera.common.mode.professional;

public interface IArcProgressBarUI {

    void initUI();
    void uninUI();
    void updateUIState(boolean isShow);
    void onItemSelcted(int item);
    void restoreSelected();
    void clearEffect();

}
