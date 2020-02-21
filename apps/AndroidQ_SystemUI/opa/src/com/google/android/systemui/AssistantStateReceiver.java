package com.google.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.internal.app.AssistUtils;

/**
 * A receiver that receives a broadcast to tell SystemUI whether or not to enable OPA.
 */
public class AssistantStateReceiver extends BroadcastReceiver {

    private static final String TAG = "AssistantStateReceiver";
    public static final String OPA_ENABLE_ACTION = "com.google.android.systemui.OPA_ENABLED";

    @Override
    public void onReceive(Context context, Intent intent) {
        final boolean enabled = intent.getBooleanExtra("OPA_ENABLED", false);
        Log.i(TAG, "Received " + intent + " with enabled = " + enabled);

        UserSettingsUtils.save(context.getContentResolver(), enabled);
        new OpaEnableDispatcher(context, new AssistUtils(context)).dispatchOpaEnabled(enabled);
    }
}