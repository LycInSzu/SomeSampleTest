package com.google.android.systemui;

import android.content.ContentResolver;
import android.provider.Settings;

import com.android.keyguard.KeyguardUpdateMonitor;

/**
 * A utility class to read/write the most recent status of Assistant from/to the persistent store.
 */
public class UserSettingsUtils {
    private static final String OPA_ENABLED_SETTING = "systemui.google.opa_enabled";

    public static void save(ContentResolver cr, boolean enabled) {
        final int user = KeyguardUpdateMonitor.getCurrentUser();
        final int en = enabled ? 1 : 0;
        Settings.Secure.putIntForUser(cr, OPA_ENABLED_SETTING, en, user);
    }

    public static boolean load(ContentResolver cr) {
        final int user = KeyguardUpdateMonitor.getCurrentUser();
        final int en = Settings.Secure.getIntForUser(cr, OPA_ENABLED_SETTING, 0, user);
        return en != 0;
    }
}
