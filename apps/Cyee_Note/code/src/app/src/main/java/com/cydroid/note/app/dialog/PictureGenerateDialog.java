package com.cydroid.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.cydroid.note.R;

public class PictureGenerateDialog extends Dialog {
    private TextView mTextView;

    public PictureGenerateDialog(Context context) {
        super(context);
    }

    protected PictureGenerateDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public PictureGenerateDialog(Context context, int theme) {
        super(context, theme);
        setOwnerActivity((Activity) context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_generate_dialog);
        mTextView = (TextView) findViewById(R.id.loading_textview);
    }

    public void setMessage(String str) {
        if (mTextView != null) {
            mTextView.setText(str);
        }
    }

}
