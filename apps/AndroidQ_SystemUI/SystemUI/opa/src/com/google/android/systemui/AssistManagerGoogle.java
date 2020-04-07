package com.google.android.systemui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Secure;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;

/**
 * GoogleSystemUI-specific flavor of AssistManager.
 */
public class AssistManagerGoogle extends AssistManager {

    private final ContentResolver mContentResolver;
    private final ContentObserver mContentObserver = new AssistantSettingsObserver();
    private final OpaEnableDispatcher mOpaEnableDispatcher;
    private final AssistantStateReceiver mEnableReceiver = new AssistantStateReceiver();

    private final KeyguardUpdateMonitorCallback mUserSwitchCallback =
            new KeyguardUpdateMonitorCallback() {
                @Override
                public void onUserSwitching(int userId) {
                    updateAssistantEnabledState();
                    // Unregister and re-register observer for current user when user switches.
                    unregisterSettingsObserver();
                    registerSettingsObserver();
                    // Unregister and re-register the opa enabled for current user
                    unregisterEnableReceiver();
                    registerEnableReceiver(userId);
                }
            };

    public AssistManagerGoogle(DeviceProvisionedController controller, Context context) {
        super(controller, context);
        mContentResolver = context.getContentResolver();
        mOpaEnableDispatcher = new OpaEnableDispatcher(context, mAssistUtils);
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUserSwitchCallback);
        // Register Assistant Settings observer
        registerSettingsObserver();
        registerEnableReceiver(UserHandle.USER_CURRENT);
    }

    /**
     * Register AssistantStateReceiver for userId.
     */
    private void registerEnableReceiver(int userId) {
        mContext.registerReceiverAsUser(mEnableReceiver, new UserHandle(userId),
                new IntentFilter(mEnableReceiver.OPA_ENABLE_ACTION), null, null);
    }

    /**
     * Unregister AssistantStateReceiver
     */
    private void unregisterEnableReceiver() {
        mContext.unregisterReceiver(mEnableReceiver);
    }

    /**
     * Update Assistant enabled state depending on cached value.
     */
    private void updateAssistantEnabledState() {
        boolean isEnabled = UserSettingsUtils.load(mContentResolver);
        mOpaEnableDispatcher.dispatchOpaEnabled(isEnabled);
    }

    /**
     * Register AssistantSettingsObserver for current user.
     */
    private void registerSettingsObserver() {
        mContentResolver.registerContentObserver(Secure.getUriFor(Secure.ASSISTANT),
                false /* notifyForDescendants */, mContentObserver,
                KeyguardUpdateMonitor.getCurrentUser());
    }

    /**
     * Unregister AssistantSettingsObserver
     */
    private void unregisterSettingsObserver() {
        mContentResolver.unregisterContentObserver(mContentObserver);
    }

    /**
     * Content observer that watches for changes to assistant-related settings.
     */
    private class AssistantSettingsObserver extends ContentObserver {
        public AssistantSettingsObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateAssistantEnabledState();
        }
    }

    /**
     * Dispatch cached OPA state to listeners.
     */
    public void dispatchOpaEnabledState() {
        updateAssistantEnabledState();
    }
}
