package com.cydroid.note.encrypt;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.app.NewNoteActivity;
import com.cydroid.note.app.view.PasswordInputView;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.Constants;
import com.cydroid.note.provider.NoteShareDataManager;

/**
 * Created by spc on 16-3-29.
 */
public class PasswordActivity extends StandardActivity implements StandardActivity.StandardAListener {

    private PasswordInputView mPasswordInputView;
    private TextView mPasswordSettingHint;
    private TextView mForgetPassword;
    private Handler mHandler;
    private State mCurrentState;
    private String mFirstPin;
    private int mInputErrorCount;
    private boolean mModifingPassword;
    private String mPath;

    private static final int ONE_MINUTE = 1000 * 60;
    private int mConfirmPasswordErrorTimes;
    private boolean isPasswordErrorTimesExceed;
    private int mMinute;
    private int mMinuteCycleTimes;
    private Handler mMinuteCycleHandler = new Handler();

    public static final int FORGET_PASSWORD = 2;
    public static final String MODIFIED_PASSWORD = "modified_password";
    private final String SYSTEM_HOME_KEY_DOWN = "homekey";
    private final String SYSTEM_HOME_KEY_LONG_PRESS = "recentapps";
    private static final String SYSTEM_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String EXTRA_REASON = "reason";
    public static final String FROM_PASSWORD_PROTECT = "from_password_protect";
    private static final int PASSWORD_ERROR_MAX_MINUTE = 1024;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoteContentView(R.layout.password_setting_layout);
        mHandler = new Handler();
        mModifingPassword = getIntent().getBooleanExtra(MODIFIED_PASSWORD, false);
        mPath = getIntent().getStringExtra(NewNoteActivity.NOTE_ITEM_PATH);
        initView();
        setStandardAListener(this);
        register();
    }

    private void register() {
        if (mModifingPassword
                && getIntent().getBooleanExtra(FROM_PASSWORD_PROTECT, false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(SYSTEM_SCREEN_OFF);
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            registerReceiver(mSafeReceiver, filter);
        }
    }

    private void initView() {
        mForgetPassword = (TextView) findViewById(R.id.forget_password);
        mForgetPassword.setOnClickListener(mOnClickListener);
        mPasswordSettingHint = (TextView) findViewById(R.id.password_setting_hint);
        mPasswordInputView = (PasswordInputView) findViewById(R.id.password_entry);
        mPasswordInputView.setOnTextChangedLisenter(new PasswordInputView.onTextChangedLisenter() {
            @Override
            public void onTextChanged() {
                handleNext();
            }
        });

        if (!hasSetPassword() || mModifingPassword) {
            mCurrentState = State.STATE_HAS_NOT_PASSWORD;
            if (mModifingPassword) {
                setTitle(R.string.modified_password);
                setPasswordSettingHint(State.STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD);
            } else {
                setTitle(R.string.setting_password);
                setPasswordSettingHint(State.STATE_HAS_NOT_PASSWORD);
            }
            mForgetPassword.setVisibility(View.GONE);
        } else {
            mCurrentState = State.STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD;
            setTitle(R.string.verification_password);
            setPasswordSettingHint(State.STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD);
            if (NoteShareDataManager.getPasswordAndQuestionsSetSuccessful(this)) {
                mForgetPassword.setVisibility(View.VISIBLE);
            } else {
                mForgetPassword.setVisibility(View.GONE);
            }
        }
        setNoteRootViewBackgroundColor();
        if (!mModifingPassword) {
            isPasswordErrorTimesExceed();
        }
    }

    private void isPasswordErrorTimesExceed() {
        long systemTimeDifference = System.currentTimeMillis() - NoteShareDataManager.getInputPasswordErrorExceedFourSystemTime(this);
        mConfirmPasswordErrorTimes = NoteShareDataManager.getInputPasswordErrorExceedFourTimes(this);
        long passwordErrorTotalTime = mConfirmPasswordErrorTimes * ONE_MINUTE;//NOSONAR
        if (systemTimeDifference < passwordErrorTotalTime) {
            long delayTime = passwordErrorTotalTime - systemTimeDifference;
            mPasswordInputView.setVisibility(View.INVISIBLE);
            passwordError(delayTime);
            mMinute = (int) ((delayTime / ONE_MINUTE) + 1);
            if (1 > mMinute) {
                mMinute = 1;
            }
            mPasswordSettingHint.setText(getResources().getString(R.string.enter_password_later, mMinute));
            isPasswordErrorTimesExceed = true;
            mMinuteCycleHandler.removeCallbacksAndMessages(null);
            setTextCycleAfterOneMinute(mMinute);
        } else {
            NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, 0);
            isPasswordErrorTimesExceed = false;
        }
        mInputErrorCount = NoteShareDataManager.getInputPasswordErrorCount(this);
        if (mInputErrorCount > 0 && mInputErrorCount < 5) {
            mPasswordSettingHint.setText(getResources().getString(R.string.password_error_hint,
                    (5 - mInputErrorCount)));
        }
    }

    private void setTextCycleAfterOneMinute(int minute) {
        if (minute <= 1) {
            return;
        }

        mMinuteCycleTimes = minute - 1;
        mMinuteCycleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPasswordSettingHint.setText(getResources().getString(R.string.enter_password_later, mMinuteCycleTimes));
                if (mMinuteCycleTimes < 2) {
                    return;
                }
                setTextCycleAfterOneMinute(mMinuteCycleTimes);
            }
        }, ONE_MINUTE);
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mPasswordInputView)) {
            imm.hideSoftInputFromWindow(mPasswordInputView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModifingPassword
                && getIntent().getBooleanExtra(FROM_PASSWORD_PROTECT, false)) {
            unregisterReceiver(mSafeReceiver);
        }
        mMinuteCycleHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClickHomeBack() {
        hideInputMethod();
        finish();
    }

    @Override
    public void onClickRightView() {
    }

    private boolean hasSetPassword() {
        String password = NoteShareDataManager.getPassword(this);
        return !TextUtils.isEmpty(password);
    }

    private void setPasswordSettingHint(State state) {
        mPasswordSettingHint.setText(state.numericHint);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(getApplicationContext(), PasswordProtectActivity.class);
                intent.putExtra(PasswordProtectActivity.FROM_FORGET_PASSWORD, true);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
            }
            finish();
        }
    };

    private void clearPasswords() {
        mPasswordInputView.setText("");
    }

    private void handleNext() {

        String pin = mPasswordInputView.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            return;
        }

        if (mCurrentState == State.STATE_HAS_NOT_PASSWORD) {
            if (pin.length() < 4) return;
            clearPasswords();
            mFirstPin = pin;
            mCurrentState = State.STATE_ENSURE_PASSWORD;
            setPasswordSettingHint(State.STATE_ENSURE_PASSWORD);

        } else if (mCurrentState == State.STATE_ENSURE_PASSWORD
                || mCurrentState == State.STATE_ENSURE_PASSWORD_NO_MATCH) {

            if (pin.equals(mFirstPin)) {
                savePassword();
                if (!mModifingPassword) {
                    goPasswordProtect();
                } else {
                    NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, 0);
                    NoteShareDataManager.setInputPasswordErrorCount(this, 0);
                    Toast.makeText(this, R.string.successful_modification, Toast.LENGTH_SHORT).show();
                }
                finish();
            } else {
                mCurrentState = State.STATE_ENSURE_PASSWORD_NO_MATCH;
                setPasswordSettingHint(State.STATE_ENSURE_PASSWORD_NO_MATCH);
            }
        } else if (mCurrentState == State.STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD
                || mCurrentState == State.STATE_INPUT_ERROR) {
            if (isPasswordErrorTimesExceed) {
                clearPasswords();
                return;
            }

            if (pin.equals(getSavePassword())) {
                mInputErrorCount = 0;
                NoteShareDataManager.setInputPasswordErrorCount(this, mInputErrorCount);
                goNext();
                if (NoteShareDataManager.getInputPasswordErrorExceedFourTimes(this) != 0) {
                    NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, 0);
                }
                finish();
            } else {
                if (mConfirmPasswordErrorTimes > 0) {
                    mCurrentState = State.STATE_INPUT_ERROR_EXCEED_FOUR;
                    mConfirmPasswordErrorTimes = 2 * mConfirmPasswordErrorTimes;
                    setInputPasswordErrorTimesAndTime(mConfirmPasswordErrorTimes);
                    clearPasswords();
                    return;
                }

                mInputErrorCount++;
                NoteShareDataManager.setInputPasswordErrorCount(this, mInputErrorCount);
                if (mInputErrorCount >= 5) {
                    mConfirmPasswordErrorTimes = 1;
                    mCurrentState = State.STATE_INPUT_ERROR_EXCEED_FOUR;
                    setInputPasswordErrorTimesAndTime(mConfirmPasswordErrorTimes);
                } else {
                    mPasswordSettingHint.setText(getResources().getString(R.string.password_error_hint,
                            (5 - mInputErrorCount)));
                    mCurrentState = State.STATE_INPUT_ERROR;
                }
            }
        } else if (mCurrentState == State.STATE_INPUT_ERROR_EXCEED_FOUR) {
            if (pin.equals(getSavePassword())) {
                goNext();
                NoteShareDataManager.setInputPasswordErrorCount(this, 0);
                if (NoteShareDataManager.getInputPasswordErrorExceedFourTimes(this) != 0) {
                    NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, 0);
                }
                finish();
                return;
            }
            mConfirmPasswordErrorTimes = 2 * mConfirmPasswordErrorTimes;
            mCurrentState = State.STATE_INPUT_ERROR_EXCEED_FOUR;
            setInputPasswordErrorTimesAndTime(mConfirmPasswordErrorTimes);
        }
        clearPasswords();
    }

    private void setInputPasswordErrorTimesAndTime(int confirmPasswordErrorTimes) {
        if (confirmPasswordErrorTimes > PASSWORD_ERROR_MAX_MINUTE) {
            confirmPasswordErrorTimes = PASSWORD_ERROR_MAX_MINUTE;
        }
        mPasswordSettingHint.setText(getResources().getString(R.string.enter_password_later, confirmPasswordErrorTimes));
        NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, confirmPasswordErrorTimes);
        NoteShareDataManager.setInputPasswordErrorExceedFourSystemTime(this, System.currentTimeMillis());
        mPasswordInputView.setVisibility(View.INVISIBLE);
        mMinuteCycleHandler.removeCallbacksAndMessages(null);
        setTextCycleAfterOneMinute(confirmPasswordErrorTimes);
        passwordError(ONE_MINUTE * confirmPasswordErrorTimes);//NOSONAR
    }

    private void goNext() {
        if (!TextUtils.isEmpty(mPath)) {
            goEncryptDetail();
        } else if (isSetPasswordProtectQA()) {
            goEncrypt();
        } else {
            goPasswordProtect();
        }
    }

    private void goEncrypt() {
        try {
            Intent intent = new Intent(this, EncryptMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void goEncryptDetail() {
        try {
            Intent intent = new Intent();
            intent.setClass(this, EncryptDetailActivity.class);
            intent.putExtra(Constants.NOTE_IS_CRYPTED, true);
            intent.putExtra(EncryptDetailActivity.NOTE_ITEM_PATH, mPath);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private boolean isSetPasswordProtectQA() {
        return !TextUtils.isEmpty(NoteShareDataManager.getProtectQuestion(this)) &&
                !TextUtils.isEmpty(NoteShareDataManager.getPasswordProtectAnswer(this));
    }

    private void goPasswordProtect() {
        try {
            Intent intent = new Intent(this, PasswordProtectActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void passwordError(final long delayTime) {
        hideInputMethod();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clearPasswords();
                showInputMethod();
                mPasswordInputView.setVisibility(View.VISIBLE);
                setPasswordSettingHint(State.STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD);
                isPasswordErrorTimesExceed = false;
            }
        }, delayTime);
    }

    private void savePassword() {
        DES des = new DES();
        String encrypt = des.authcode(mFirstPin, DES.OPERATION_ENCODE, DES.DES_KEY);
        if (!TextUtils.isEmpty(encrypt)) {
            NoteShareDataManager.setPassword(this, encrypt);
        }
    }

    private String getSavePassword() {
        String savePassword = NoteShareDataManager.getPassword(this);
        DES des = new DES();
        return des.authcode(savePassword, DES.OPERATION_DECODE, DES.DES_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case FORGET_PASSWORD: {
                boolean hasSetPassword = intent.getBooleanExtra(
                        PasswordProtectActivity.HAS_SET_PASSWORD_QA, false);
                if (hasSetPassword) {
                    finish();
                }
                break;
            }
            default:
                break;
        }

    }

    private BroadcastReceiver mSafeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SYSTEM_SCREEN_OFF.equals(action)) {
                PasswordActivity.this.finish();
            } else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(EXTRA_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY_DOWN)
                        || TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG_PRESS)) {
                    PasswordActivity.this.finish();
                }
            }
        }
    };


    protected enum State {
        /**
         * Keep track internally of where the user is in choosing a pattern.
         */
        STATE_HAS_NOT_PASSWORD(R.string.setting_password_hint),

        STATE_ENSURE_PASSWORD(R.string.ensure_password),

        STATE_ENSURE_PASSWORD_NO_MATCH(R.string.password_not_match),

        STATE_HAS_PASSWORD_PLEASE_INPUT_PASSWORD(R.string.enter_password),

        STATE_INPUT_ERROR(R.string.password_error_hint),

        STATE_INPUT_ERROR_EXCEED_FOUR(R.string.enter_password_later);

        /**
         * @param headerMessage The message displayed at the top.
         */
        State(int hintInNumeric) {
            this.numericHint = hintInNumeric;
        }

        public final int numericHint;
    }


}
