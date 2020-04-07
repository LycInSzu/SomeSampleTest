package com.cydroid.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import cyee.app.CyeeAlertDialog;

import com.cydroid.note.R;

public class CyeeIndeterminateProgressDialog {

    private Activity mActivity;
    private View mContent;
    private Dialog mDialog;
    private TextView mMessageView;

    public CyeeIndeterminateProgressDialog(Activity activity) {
        mActivity = activity;
        initView();
        initDialog();
    }

    private void initView() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View content = inflater.inflate(R.layout.cyee_indeterminate_progress_dialog_ly, null, false);
        ProgressBar progressBar = (ProgressBar) content.findViewById(R.id.cyee_indeterminate_progress_dialog_id_progressbar);
        if (Build.VERSION.SDK_INT >= 21) {
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.system_stress_color)));
        }
        mMessageView = (TextView) content.findViewById(R.id.cyee_indeterminate_progress_dialog_id_message);
        mContent = content;
    }

    private void initDialog() {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(mContent);
        builder.setCancelable(false);
        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        mDialog = dialog;
    }

    public void setMessage(int resid) {
        mMessageView.setText(resid);
    }

    public void setMessage(String message) {
        mMessageView.setText(message);
    }

    public void show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void hide() {
        if (mDialog.isShowing()) {
            mDialog.hide();
        }
    }

    public void dismiss() {
        if (mDialog.isShowing() && !mActivity.isFinishing() && !mActivity.isDestroyed()) {
            mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }
}

