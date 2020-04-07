package com.google.android.systemui;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;

import com.android.internal.app.AssistUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.phone.ButtonDispatcher;
import com.android.systemui.statusbar.phone.StatusBar;

import java.util.ArrayList;

/**
 * Utility class to dispatch the current OPA enabled state to the UI.
 */
public class OpaEnableDispatcher {

    private final Context mContext;
    private final AssistUtils mAssistUtils;
    private static final String OPA_COMPONENT_NAME = "com.google.android.googlequicksearchbox/" +
        "com.google.android.voiceinteraction.GsaVoiceInteractionService";

    public OpaEnableDispatcher(Context context, AssistUtils assistUtils) {
        mContext = context;
        mAssistUtils = assistUtils;
    }

    public void dispatchOpaEnabled(boolean enabled) {
        dispatchUnchecked(enabled && isGsaCurrentAssistant());
    }

    private void dispatchUnchecked(boolean enabled) {
        StatusBar bar = ((SystemUIApplication) mContext.getApplicationContext()).getComponent(
                StatusBar.class);
        if (bar == null) {
            return;
        }
        // A: AQJB-671 wangjian 20190215 {
        if (bar.getNavigationBarView() == null) {
            return;
        }
        // A: }
        ButtonDispatcher homeDispatcher = bar.getNavigationBarView().getHomeButton();
        ArrayList<View> views = homeDispatcher.getViews();
        for (int i = 0; i < views.size(); ++i) {
            View v =  views.get(i);
            ((OpaLayout) v).setOpaEnabled(enabled);
        }
    }

    private boolean isGsaCurrentAssistant() {
        ComponentName assistant = mAssistUtils.getAssistComponentForUser(
                KeyguardUpdateMonitor.getCurrentUser());
        return assistant != null
                && OPA_COMPONENT_NAME.equals(assistant.flattenToString());
    }
}