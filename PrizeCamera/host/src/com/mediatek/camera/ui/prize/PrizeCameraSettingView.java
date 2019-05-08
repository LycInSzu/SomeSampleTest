package com.mediatek.camera.ui.prize;

import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;

public class PrizeCameraSettingView implements ICameraSettingView {

    public static final int SETTING_TYPE_SWITCH = 0;
    public static final int SETTING_TYPE_LIST= 1;

    public static final String VALUES_ON = "on";
    public static final String VALUES_OFF = "off";

    @Override
    public void loadView(PreferenceFragment fragment) {

    }

    @Override
    public void refreshView() {

    }

    @Override
    public void unloadView() {

    }

    @Override
    public void setEnabled(boolean enable) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public int getSettingType() {
        return SETTING_TYPE_SWITCH;
    }

    public List<String> getEntrys() {
        return null;
    }

    public List<String> getEntryValues() {
        if(getSettingType() == SETTING_TYPE_SWITCH){
            ArrayList<String> list = new ArrayList<>(2);
            list.add(VALUES_ON);
            list.add(VALUES_OFF);
            return list;
        }
        return null;
    }

    public int[] getIcons() {
        return new int[]{
            R.drawable.beautiful_catchlight_f,
            R.drawable.beautiful_catchlight_n,
        };
    }

    public String getValue() {
        return VALUES_ON;
    }

    public int getTitle() {
        return R.string.pref_camera_coloreffect_entry_none;
    }

    public void onValueChanged(String newValue){

    }

    public int getOrder(){
        return 60;
    }
}
