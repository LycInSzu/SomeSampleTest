package com.cydroid.note.app.view;

import android.text.InputFilter;
import android.text.Spanned;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.utils.ToastManager;

public class TextLengthFilter implements InputFilter {

    private int mMaxLength;
    private ToastManager mToastManager;

    public TextLengthFilter(int maxLength, ToastManager toastManager) {
        mMaxLength = maxLength;
        mToastManager = toastManager;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        int keep = mMaxLength - (dest.length() - (dend - dstart));

        if (keep <= 0) {
            mToastManager.showToast(NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit));
            return "";
        } else if (keep >= end - start) {
            return null; // keep original
        } else {
            keep += start;
            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                --keep;
                if (keep == start) {
                    mToastManager.showToast(NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit));
                    return "";
                }
            }
            mToastManager.showToast(NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit));
            return source.subSequence(start, keep);
        }
    }

}