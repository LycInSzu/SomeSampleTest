package com.cydroid.note.common;

import android.content.Context;

import com.cydroid.note.common.Log;
import com.cydroid.note.app.ComponentStack;

import cyee.changecolors.ChameleonColorManager;
import cyee.changecolors.OnChangeColorListener;


public class GnChameleonObserver implements OnChangeColorListener {

    public GnChameleonObserver(Context context) {
        boolean supportChamelon = PlatformUtil.isGioneeDevice();
        if (supportChamelon) {
            try {
                ChameleonColorManager.getInstance().register(context, false);
                ChameleonColorManager.getInstance().addOnChangeColorListener(this);
            } catch (Exception e) {
                Log.d("GnChameleonObserveDebug", "Chameleon register e" + e);
            }
        }
    }

    @Override
    public void onChangeColor() {
        ComponentStack.obtain().finishActivity();
    }

}
