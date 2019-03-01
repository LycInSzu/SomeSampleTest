package com.mediatek.cellbroadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

public class CMASAlertLinearLayout extends LinearLayout {

    Context mContext;
    CMASAlertLinearLayout(Context context) {
        super(context);
        mContext = context;
    }

    public CMASAlertLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                !CmasConfigManager.isChileProfile()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
					//prize modified by liyuchong, config CMASReceiver fot TRA test,20190124-begin
                   // mContext.stopService(new Intent(mContext, CellBroadcastAlertAudio.class));
				   //prize modified by liyuchong, config CMASReceiver fot TRA test,20190124-end
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}