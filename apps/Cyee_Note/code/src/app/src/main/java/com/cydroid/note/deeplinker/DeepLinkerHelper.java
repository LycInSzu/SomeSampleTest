package com.cydroid.note.deeplinker;

import android.content.Intent;
import android.text.TextUtils;

public final class DeepLinkerHelper {
    private DeepLinkerHelper() {
    }

    private static final String PH_POUND = "__PH_PD__";
    private static final String PH_SLASH = "__PH_SL__";
    private static final String PH_COLON = "__PH_CLN__";

    /**
     * replace some character
     * #   --->   __PH_PD__
     * /   --->   __PH_SL__
     * :   --->   __PH_CLN__
     * <p/>
     * PH=placeholder
     * PD=pound=#
     * SL=slash=/
     * CLN=colon=:
     */
    public static String encodeIntentString(String intentStr) {

        if (TextUtils.isEmpty(intentStr)) {
            return intentStr;
        }

        intentStr = intentStr.replaceAll("#", PH_POUND);
        intentStr = intentStr.replaceAll("/", PH_SLASH);
        intentStr = intentStr.replaceAll(":", PH_COLON);
        return intentStr;
    }

    public static String intent2String(Intent intent) {
        if (intent == null) {
            return null;
        }

        String intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);
        return encodeIntentString(intentUri);
    }

    public static String decodeIntentString(String intentStr) {
        if (TextUtils.isEmpty(intentStr)) {
            return intentStr;
        }

        String result = intentStr.replace(PH_POUND, "#");
        result = result.replace(PH_COLON, ":");
        return result.replace(PH_SLASH, "/");
    }

    public static String getPackageNameFromIntentString(String intentStr) {

        if (TextUtils.isEmpty(intentStr)) {
            return null;
        }

        String[] splits = intentStr.split(";");

        String packageName = null;
        for (String split : splits) {
            if (split.startsWith("package=")) {
                String[] tempArr = split.split("=");
                packageName = tempArr[1];
                break;
            }
        }

        if (TextUtils.isEmpty(packageName)) {
            for (String split : splits) {
                if (split.startsWith("component=")) {
                    String[] tempArr = split.split("=");
                    packageName = tempArr[1].split("/")[0];
                    break;
                }
            }
        }
        return packageName;
    }
}
