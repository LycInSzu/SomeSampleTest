package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.statusbar.phone.StatusBar;
import android.view.MotionEvent;

/**
 *  add this class for  EJSLYQ-66  
 */
public class WTKBouncerRootView extends FrameLayout {

    protected StatusBar mStatusBar;

    public WTKBouncerRootView(Context context) {
        this(context, null);
    }

    public WTKBouncerRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStatusBar = SysUiServiceProvider.getComponent(context, StatusBar.class);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mStatusBar.userActivity();
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

}
