package com.cydroid.note.encrypt;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.PlatformUtil;

/**
 * Created by wuguangjie on 16-4-15.
 */
public class PasswordSetSuccessedActivity extends StandardActivity implements StandardActivity.StandardAListener {

    private final String SYSTEM_HOME_KEY_DOWN = "homekey";
    private final String SYSTEM_HOME_KEY_LONG_PRESS = "recentapps";
    private static final String SYSTEM_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String EXTRA_REASON = "reason";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.password_set_successed_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.password_set_successed_first_in_layout);
        setNoteRootViewBackgroundColor();
        TextView firstTipTextView = (TextView) findViewById(R.id.first_tip);
        String firstTip = getResources().getString(R.string.password_set_successed_tips_first);
        if (PlatformUtil.isGioneeDevice()) {
            firstTipTextView.setText(firstTip);
        } else {
            firstTipTextView.setText(firstTip.replace("Note", ""));
        }
        register();
        findViewById(R.id.success).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getApplicationContext(), EncryptMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
                finish();
            }
        });
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SYSTEM_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mSafeReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mSafeReceiver);
        super.onDestroy();
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {
    }

    private BroadcastReceiver mSafeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SYSTEM_SCREEN_OFF.equals(action)) {
                PasswordSetSuccessedActivity.this.finish();
            } else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(EXTRA_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY_DOWN)
                        || TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG_PRESS)) {
                    PasswordSetSuccessedActivity.this.finish();
                }
            }
        }
    };
}
