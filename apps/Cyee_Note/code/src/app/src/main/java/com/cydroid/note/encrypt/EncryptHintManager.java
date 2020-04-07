package com.cydroid.note.encrypt;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cydroid.note.R;
import java.util.regex.Pattern;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;
//GIONEE wanghaiyan 2016-12-08 modify for 43513 begin
import cyee.changecolors.ChameleonColorManager;
//GIONEE wanghaiyan 2016-12-08 modify for 43513 end

/**
 * Created by spc on 16-4-15.
 */
public class EncryptHintManager {

    private final static Pattern sKeyWordsPattern = Pattern.compile
            ("[^0-9]+\\d{4}\\s{1,}\\d{4}+[^0-9]" +
                    "|^\\d{4}\\s{1,}\\d{4}+[^0-9]" +
                    "|[^0-9]+\\d{4}\\s{1,}\\d{4}+$" +
                    "|^\\d{4}\\s{1,}\\d{4}+$" +
                    "|[0-9]+(?![0-9]{5,11})[0-9A-Za-z]{5,11}+[^0-9A-Za-z]" +
                    "|[A-Za-z]+(?![a-zA-Z]{5,11})[0-9A-Za-z]{5,11}+[^0-9A-Za-z]"+
                    "|[0-9]+(?![0-9]{5,11})[0-9A-Za-z]{5,11}+$" +
                    "|[A-Za-z]+(?![a-zA-Z]{5,11})[0-9A-Za-z]{5,11}+$"+
                    "|([^0-9]+[0-9]\\d{5})+[^0-9]" +
                    "|([^0-9]+[0-9]\\d{5})+$" +
                    "|^\\d{6}+[^0-9]" +
                    "|^\\d{6}+$");

    private CyeeAlertDialog mEncryptHint;
    private final EncryptHintListner mEncryptHintListner;

    public EncryptHintManager(EncryptHintListner listner) {
        mEncryptHintListner = listner;
    }

    public void showEncryptHint(final Activity activity) {
        View view = LayoutInflater.from(activity).inflate(R.layout.encrypt_hint_dialog, null);
        final CyeeCheckBox checkBox = (CyeeCheckBox) view.findViewById(R.id.encrypt_hint_dialog_checkBox);
	    //GIONEE wanghaiyan 2016-12-08 modify for 43513 begin
	    if(ChameleonColorManager.isNeedChangeColor()){
           checkBox.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
	    }
        //GIONEE wanghaiyan 2016-12-08 modify for 43513 end
        CyeeTextView cyeeTextView = (CyeeTextView) view.findViewById(R.id.encrypt_hint_dialog_content);
        cyeeTextView.setText(R.string.encrypt_hint_content);
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(activity);
        builder.setTitle(R.string.encrypt_hint_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.encrypt_hint_manager_dialog_sure,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEncryptHintListner.onEncrypt(!checkBox.isChecked());
                    }
                });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEncryptHintListner.onCancleEncrypt(!checkBox.isChecked());
                dismissDialog();
                activity.finish();
            }
        });
        mEncryptHint = builder.create();
        Window window = mEncryptHint.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        mEncryptHint.show();
    }

    public void dismissDialog() {
        if (null != mEncryptHint && mEncryptHint.isShowing()) {
            mEncryptHint.dismiss();
        }
    }

    public boolean shouldShowEncryptHint(String text) {
        return sKeyWordsPattern.matcher(text).find();
    }

    public interface EncryptHintListner {

        public void onEncrypt(boolean hintAgain);

        public void onCancleEncrypt(boolean hintAgain);
    }
}
