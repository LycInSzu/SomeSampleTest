package com.cydroid.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.cydroid.note.R;

import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeTextView;

public class CyeeDeterminateProgressDialog {
    private Activity mActivity;
    private Dialog mDialog;
    private CyeeTextView mProgressNumberView;
    private CyeeTextView mMessageView;
    private View mContent;
    private ProgressBar mProgressBar;
    private String mProgressNumberFormat;
    private OnCancelListener mOnCancelListener;
    private int mTextProgressValue;

    public interface OnCancelListener {
        void onCancel();
    }

    public CyeeDeterminateProgressDialog(Activity activity) {
        this(activity, true);
    }

    public CyeeDeterminateProgressDialog(Activity activity, boolean isShowCancelButton) {
        mActivity = activity;
        initView();
        initDialog(isShowCancelButton);
    }


    private void setFormats(int length) {
        mProgressNumberFormat = "%1$-" + length + "d/%2$-" + length + "d";
    }

    private void initView() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View content = inflater.inflate(R.layout.cyee_determinate_progress_dialog_ly, null, false);
        mProgressNumberView = (CyeeTextView) content.findViewById(R.id.cyee_determinate_progress_dialog_id_progress);
        if (ChameleonColorManager.isNeedChangeColor()) {
            mProgressNumberView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
        mMessageView = (CyeeTextView) content.findViewById(R.id.cyee_determinate_progress_dialog_id_message);
        mProgressBar = (ProgressBar) content.findViewById(R.id.cyee_determinate_progress_dialog_id_progressbar);
        mProgressBar.setIndeterminate(false);
        mContent = content;
    }

    private void initDialog(boolean isShowCancelButton) {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(mContent);
        if (isShowCancelButton) {
            builder.setNegativeButton(R.string.cyee_determinate_progress_dialog_st_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mOnCancelListener != null) {
                                mOnCancelListener.onCancel();
                            }
                        }
                    });
        }
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        mDialog = dialog;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        mOnCancelListener = listener;
    }

    public void show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
	    //Gionee wanghaiyan 2017-3-28 modify for 96692 begin
        if (mDialog.isShowing() && !mActivity.isFinishing()) {
            mDialog.dismiss();
        }
	    //Gionee wanghaiyan 2017-3-28 modify for 96692 end
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public void setMax(int max) {
        mProgressBar.setMax(max);
        setFormats(Integer.toString(max).length());
        updateProgressNumber();
    }

    public void setMax(int progressMax, int textMax) {
        mTextProgressValue = textMax;
        mProgressBar.setMax(progressMax);
        setFormats(Integer.toString(textMax).length());
        mProgressNumberView.setText(String.format(mProgressNumberFormat, 0, textMax));
    }

    public int getMax() {
        return mProgressBar.getMax();
    }

    public void setProgress(int value) {
        mProgressBar.setProgress(value);
        updateProgressNumber();
    }

    public void setProgress(int progressValue, int textValue) {
        mProgressBar.setProgress(progressValue);
        mProgressNumberView.setText(String.format(mProgressNumberFormat, textValue, mTextProgressValue));

    }

    public void setProgress(int value, String format) {
        mProgressBar.setProgress(value);
        updateProgressNumber(format);
    }

    public void setMessage(int resid) {
        mMessageView.setText(resid);
    }

    public void setMessage(String title) {
        mMessageView.setText(title);
    }

    public void incrementProgressBy(int diff) {
        mProgressBar.incrementProgressBy(diff);
    }

    private void updateProgressNumber() {
        int progress = mProgressBar.getProgress();
        int max = mProgressBar.getMax();
        String format = mProgressNumberFormat;
        mProgressNumberView.setText(String.format(format, progress, max));
    }

    private void updateProgressNumber(String format) {
        int progress = mProgressBar.getProgress();
        int max = mProgressBar.getMax();
        mProgressNumberView.setText(String.format(format, progress, max));
    }
}
