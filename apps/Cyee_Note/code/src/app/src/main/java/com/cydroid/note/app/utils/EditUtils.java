package com.cydroid.note.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

import com.cydroid.note.app.span.BillImageSpan;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.common.Constants;

public class EditUtils {

    private static final String TAG = "EditUtils";

    public static void insertPhotoImageSpan(SpannableStringBuilder text, PhotoImageSpan span, int start) {
        text.insert(start, Constants.MEDIA_PHOTO);
        text.setSpan(span, start, start + Constants.MEDIA_PHOTO.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void insertSoundImageSpan(SpannableStringBuilder text, SoundImageSpan span, int start) {
        text.insert(start, Constants.MEDIA_SOUND);
        text.setSpan(span, start, start + Constants.MEDIA_SOUND.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void insertBillImageSpan(SpannableStringBuilder text, BillImageSpan span, int start) {
        String str = text.toString();
        if (!str.startsWith(Constants.MEDIA_BILL, start)) {
            text.insert(start, Constants.MEDIA_BILL);
        }
        text.setSpan(span, start, start + Constants.MEDIA_BILL.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static int getCurParagraphStart(SpannableStringBuilder text, int currPosition) {
        final int length = text.length();
        if (length <= 0) {
            return 0;
        }
        CharSequence sub = text.subSequence(0, currPosition);
        int index = TextUtils.lastIndexOf(sub, Constants.CHAR_NEW_LINE);
        if (index == -1) {
            return 0;
        }
        return (index + 1);
    }

    public static int getCurParagraphEnd(SpannableStringBuilder text, int currPosition) {
        final int length = text.length();
        if (length <= 0) {
            return 0;
        }
        int index = TextUtils.indexOf(text, Constants.CHAR_NEW_LINE, currPosition);
        if (index == -1) {
            return text.length();
        }
        return index;
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "error", e);
        }
        return packageInfo != null ? packageInfo.versionName : "";
    }

}
