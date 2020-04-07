package com.cydroid.note.encrypt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.cydroid.note.R;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.Constants;

/**
 * Created by spc on 16-4-19.
 */
public class EncryptSettingActivity extends StandardActivity implements StandardActivity.StandardAListener,
        View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStandardAListener(this);
        setTitle(R.string.setting_title);
        setNoteContentView(R.layout.encrypt_setting_layout);
        setNoteRootViewBackgroundColor();

        findViewById(R.id.modified_encrypt).setOnClickListener(this);
        findViewById(R.id.modified_password_protect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modified_encrypt:
                goModifyPassword();
                break;
            case R.id.modified_password_protect:
                goModifyQA();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {
    }

    private void goModifyPassword() {
        try {
            Intent intent = new Intent(this, PasswordActivity.class);
            intent.putExtra(PasswordActivity.MODIFIED_PASSWORD, true);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void goModifyQA() {
        try {
            Intent intent = new Intent(this, PasswordProtectActivity.class);
            intent.putExtra(PasswordProtectActivity.MODIFIED_PROTECT_QA, true);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }
}
