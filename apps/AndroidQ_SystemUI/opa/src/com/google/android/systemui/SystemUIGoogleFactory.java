package com.google.android.systemui;

import android.content.Context;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;

import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;

/**
 * Factory for Google specific behavior classes.
 */
public class SystemUIGoogleFactory extends SystemUIFactory {
    private static final String TAG = "SystemUIGoogleFactory";

    @Override
    public AssistManager provideAssistManager(DeviceProvisionedController controller,
            Context context) {
        Log.i(TAG, "support eea = " + SystemProperties.getInt("ro.odm_gsm_type_eea",0));
        if (SystemProperties.getInt("ro.odm_gsm_type_eea",0) == 1) {
            return new AssistManagerGoogle(controller, context);
        }
        return new AssistManager(controller, context);
    }
}
