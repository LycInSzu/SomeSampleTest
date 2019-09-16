package com.cydroid.ota.logic;

import android.content.Context;

/**
 * Created by borney on 4/21/15.
 */
public final class SystemUpdateFactory {
    private static final String TAG = "SystemUpdateFactory";
    private SystemUpdateFactory() {

    }

    public static ISystemUpdate systemUpdate(Context context) {
        return SystemUpdateManager.getInstance(context);
    }


    public static ILocalUpdateExecutor localUpdate(Context context,
            IExcutorCallback callback) {
        return new ScanLocalExecutor(context, callback);
    }

    public static IAutoUpgradeSystem autoUpgrade(Context context) {
        return AutoUpgradeManager.getInstance(context);
    }

    public static IQuestionnaire questionnaire(Context context) {
        return QuestionnaireManager.getInstance(context);
    }

    public static ILocalVersionCheck localVersionCheck(Context context) {
        return new LocalVersionCheckImpl(context);
    }
}
