package com.cydroid.note.app.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;

import cyee.app.CyeeAlertDialog;

public class CyeeConfirmDialog {
    private static final String TAG = "CyeeConfirmDialog";
    public static final int BUTTON_POSITIVE = -1;
    public static final int BUTTON_NEGATIVE = -2;
    private CyeeAlertDialog mDialog;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(int which);
    }

    public CyeeConfirmDialog(Activity activity) {
        initDialog(activity);

    }

    private void initDialog(Context context) {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(context);
        builder.setPositiveButton(R.string.cyee_confirm_dialog_st_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(BUTTON_POSITIVE);
                        }
                    }
                });
        builder.setNegativeButton(R.string.cyee_determinate_progress_dialog_st_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(BUTTON_NEGATIVE);
                        }
                    }
                });
        mDialog = builder.create();
    }

    public void setTitle(int resid) {
        mDialog.setTitle(resid);
    }

    public void setMessage(int resid) {
        mDialog.setMessage(NoteAppImpl.getContext().getString(resid));
    }

    public void setMessage(String message) {
        mDialog.setMessage(message);
    }

    public void show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void cancel() {
        if (mDialog != null) {
            mDialog.cancel();
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mDialog.setOnCancelListener(listener);
    }
}
