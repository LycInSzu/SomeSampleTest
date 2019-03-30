package com.mediatek.camera.feature.setting.screenflash;

import android.content.Context;
import android.util.AttributeSet;

import com.mediatek.camera.common.widget.RotateImageView;

/**
 * Created by guo.zhang on 2018/6/20.
 */

public class ScreenFlashImageView extends RotateImageView{


    private boolean mSuerEnable;
    private ScreenFlash mFlash;

    public ScreenFlashImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFlash(ScreenFlash flash){
        mFlash = flash;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mSuerEnable = enabled;
        //enableFilter(false);
        super.setEnabled(enabled);
    }

    public void freshFlashState(){
        //enableFilter(false);
        super.setEnabled(mSuerEnable);
    }
}
