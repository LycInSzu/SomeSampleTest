package com.cydroid.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.cydroid.note.R;
import com.cydroid.note.app.utils.InputTextNumLimitHelp;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.provider.NoteShareDataManager;

import cyee.app.CyeeAlertDialog;


public class CyeeEditDialog {

    private Activity mActivity;
    private EditText mInputText;
    private Dialog mDialog;
    private View mContent;
    private ConfirmListener mConfirmListener;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;

    public interface ConfirmListener {
        void onConfirm(String inputText);
    }

    public CyeeEditDialog(Activity activity, ConfirmListener confirmListener) {
        mActivity = activity;
        initView();
        intDialog(confirmListener);
        showInput();
    }

    public void show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void setInputText(String inputText) {
        mInputText.setText(inputText);
        if (!TextUtils.isEmpty(inputText)) {
            mInputText.setSelection(inputText.length());
        }
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void initView() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View content = inflater.inflate(R.layout.signature_input_layout, null);
        mInputText = (EditText) content.findViewById(R.id.input_text);
        InputTextNumLimitHelp help = new InputTextNumLimitHelp(mInputText, 20, 10, 15);
        mInputTextNumLimitHelp = help;
        mContent = content;
    }

    private void intDialog(ConfirmListener confirmListener) {
        mConfirmListener = confirmListener;

        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(mContent);

        builder.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editable editable = mInputText.getText();
                if (null != editable) {
                    mConfirmListener.onConfirm(editable.toString());
                }
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        mDialog = dialog;
    }

    private void showInput() {
        mInputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }

    public void destroy() {
        if (mInputTextNumLimitHelp != null) {
            mInputTextNumLimitHelp.unRegisterWatcher();
        }
    }

}
