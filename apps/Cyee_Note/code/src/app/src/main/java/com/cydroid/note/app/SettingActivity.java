package com.cydroid.note.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import com.cydroid.note.common.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//Chenyee wanghaiyan 2018-6-21 modify CSW1802A-202 begin
//import com.gionee.appupgrade.common.FactoryAppUpgrade;
//import com.gionee.appupgrade.common.GnAppUpgradeImple;
//import com.gionee.appupgrade.common.IGnAppUpgrade;
//Chenyee wanghaiyan 2018-6-21 modify CSW1802A-202 end
import com.cydroid.note.R;
import com.cydroid.note.app.dialog.CyeeEditDialog;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.provider.NoteShareDataManager;
import com.cydroid.note.trash.app.TrashMainActivity;
//GIONEE wanghaiyan 2016 -12-13 modify for 46059 begin
import com.cydroid.note.common.FileUtils;
//GIONEE wanghaiyan 2016 -12-13 modify for 46059 end

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cyee.app.CyeeAlertDialog;
import cyee.app.CyeeProgressDialog;
import cyee.widget.CyeeSwitch;
import cyee.widget.CyeeTextView;


public class SettingActivity extends StandardActivity implements StandardActivity.StandardAListener {
    private static final String TAG = "SettingActivity";
    private final static int REQUEST_READ_PHONE_STATE_AND_START_NEW_FEEDBACK = 1;
    private final static int REQUEST_READ_PHONE_STATE_AND_START_APP_UPGRADE = 2;
    private static final String FORMAT = "%3d%%";
    private CyeeEditDialog mSignatureInputDialog;
    private CyeeTextView mSignature;

    private void showToast(int StrId) {
        String message = getString(StrId);
        new ToastManager(this).showToast(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewLayout();
        initView();
    }

    private void setViewLayout() {
        setTitle(R.string.setting_title);
        setStandardAListener(this);
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for begin
        if(!FileUtils.gnEncryptionSpaceSupport){
           setNoteContentView(R.layout.setting_content_no_encrypt_layout);
        }else{
           setNoteContentView(R.layout.setting_content_layout);
        }
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for end
    }

    private void initView() {
        ((TextView)findViewById(R.id.inscribed_signature_hint)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        ((TextView)findViewById(R.id.setting_signature_text)).setTextColor(
                ColorThemeHelper.getContentSmallTextColor(this, false));
	    //GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
	    /*
        ((TextView)findViewById(R.id.ai_setting_name_view)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        ((TextView)findViewById(R.id.ai_setting_tip_text)).setTextColor(
                ColorThemeHelper.getContentSmallTextColor(this, false));
        */
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 end
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for begin
        if(!FileUtils.gnEncryptionSpaceSupport){
        }else{
           ((TextView)findViewById(R.id.setting_secret_name_view)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
           ((TextView)findViewById(R.id.setting_secret_tip_text)).setTextColor(
                ColorThemeHelper.getContentSmallTextColor(this, false));
        }
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for end
        ((TextView)findViewById(R.id.setting_list_trash)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        // GIONEE wanghaiyan 2016-12-01 modify for 36866 begin
	    /*
        ((TextView)findViewById(R.id.setting_list_update)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        ((TextView)findViewById(R.id.setting_list_feedback)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        ((TextView)findViewById(R.id.setting_list_aboutus)).setTextColor(
                ColorThemeHelper.getContentNormalTextColor(this, false));
        */
        //GIONEE wanghaiyan 2016-12-01 modify for 36866 end
        findViewById(R.id.setting_list_signature).setOnClickListener(mOnClickListener);
        findViewById(R.id.setting_list_trash).setOnClickListener(mOnClickListener);
	    //GIONEE wanghaiyan 2016-12-01 modify for 36866 begin
	    /*
        findViewById(R.id.setting_list_feedback).setOnClickListener(mOnClickListener);
        findViewById(R.id.setting_list_aboutus).setOnClickListener(mOnClickListener);
        findViewById(R.id.setting_list_update).setOnClickListener(mOnClickListener);
        */
        //GIONEE wanghaiyan 2016-12-01 modify for 36866 end
        setNoteRootViewBackgroundColor();
        mSignature = (CyeeTextView) findViewById(R.id.setting_signature_text);
        setSignature(NoteShareDataManager.getSignatureText(this));
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
        /*
        final CyeeSwitch cyeeSwitch = (CyeeSwitch) findViewById(R.id.ai_setting_switch);
        cyeeSwitch.setChecked(NoteShareDataManager.isAISwitchOpen(this));
        RelativeLayout aiSettingLayout = (RelativeLayout) findViewById(R.id.ai_setting_layout);
        aiSettingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAISwitchOpen = NoteShareDataManager.isAISwitchOpen(getApplicationContext());
                cyeeSwitch.setChecked(!isAISwitchOpen);
                NoteShareDataManager.setAISwitchValue(SettingActivity.this, !isAISwitchOpen);
                String openOrClose = getResources().getString(R.string.encrypt_switch_close);
                if (!isAISwitchOpen) {
                    openOrClose = getResources().getString(R.string.encrypt_switch_open);
                }
            }
        });
        */
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 end
        
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for begin
        if(!FileUtils.gnEncryptionSpaceSupport){
        }else{
			final CyeeSwitch secretRemindSwitch = (CyeeSwitch) findViewById(R.id.setting_secret_switch);
			secretRemindSwitch.setChecked(NoteShareDataManager.isSecretSwitchOpen(this));
			RelativeLayout aiSettingSecretLayout = (RelativeLayout) findViewById(R.id.setting_secret_layout);
			aiSettingSecretLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				    boolean isAISecretSwitchOpen = NoteShareDataManager.isSecretSwitchOpen(getApplicationContext());
				    secretRemindSwitch.setChecked(!isAISecretSwitchOpen);
				    NoteShareDataManager.setAISecretSwitchValue(SettingActivity.this, !isAISecretSwitchOpen);
				    String openOrClose = getResources().getString(R.string.encrypt_switch_close);
				    if (!isAISecretSwitchOpen) {
				        openOrClose = getResources().getString(R.string.encrypt_switch_open);
				    }
				}
			});
        }
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for end
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       
    }


    private void startAboutUs() {
        try {
            Intent intent = new Intent();
            intent.setClass(this, AboutUsActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void showSignatureDialog() {
        if (!validActivityEnv()) {
            return;
        }
        if (null == mSignatureInputDialog) {
            mSignatureInputDialog = new CyeeEditDialog(SettingActivity.this, mConfirmListener);
        }
        mSignatureInputDialog.setInputText(mSignature.getText().toString());
        //Chenyee wanghaiyan 2017-12-09 modify for CSW1702A-368 begin
        if(!isFinishing()) {
            mSignatureInputDialog.show();
        }
        //Chenyee wanghaiyan 2017-12-09 modify for CSW1702A-368 end
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.setting_list_signature:
                    showSignatureDialog();
                    break;
                case R.id.setting_list_trash:
                    startTrashPage();
                    break;
                default:
                    break;
            }
        }
    };

    private void startTrashPage() {
        try {
            Intent intent = new Intent(this, TrashMainActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "TrashMainActivity is not found : " + e);
        }
    }



    private boolean validActivityEnv() {
        return !(isFinishing() || isDestroyed());
    }

    private CyeeEditDialog.ConfirmListener mConfirmListener = new CyeeEditDialog.ConfirmListener() {
        @Override
        public void onConfirm(String inputText) {
            String signature = "";
            if (!isSpaceText(inputText)) {
                signature = inputText;
            }

            setSignature(signature);
            NoteShareDataManager.saveSignature(SettingActivity.this, signature);
        }
    };

    private boolean isSpaceText(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        int len = text.length();
        StringBuilder builder = new StringBuilder();
        builder.append("\\s")
                .append("{")
                .append(len)
                .append("}");
        Pattern p = Pattern.compile(builder.toString());
        Matcher matcher = p.matcher(text);
        return matcher.matches();
    }

    private void setSignature(String signature) {
        if (TextUtils.isEmpty(signature)) {
            mSignature.setVisibility(View.GONE);
        } else {
            mSignature.setVisibility(View.VISIBLE);
        }
        mSignature.setText(signature);
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {

    }

    

    private void showRemind() {
        Toast.makeText(this, R.string.authorization_failed, Toast.LENGTH_SHORT).show();
    }
}
