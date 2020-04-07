package com.cydroid.note.encrypt;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.app.utils.InputTextNumLimitHelp;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.provider.NoteShareDataManager;

import java.util.regex.Pattern;

import cyee.widget.CyeeButton;

/**
 * Created by spc on 16-4-12.
 */
public class PasswordProtectActivity extends StandardActivity implements StandardActivity.StandardAListener {

    public static final String MODIFIED_PROTECT_QA = "modified_QA";
    public static final String HAS_SET_PASSWORD_QA = "has_set_password_QA";
    public static final String FROM_FORGET_PASSWORD = "from_forget_password";

    private EditText mAnswerView;
    private EditText mQuestion;
    private CyeeButton mConfirm;
    private CyeeButton mCancle;
    private ImageView mMoreQ;
    private Dialog mQuestionSelectDialog;
    private String[] mPasswordProtectQuestions;
    private String mCurrentQuestion;
    private boolean mModifingQA;
    private int mPosition = -1;
    private boolean mIsFromForgetPassword;
    private InputTextNumLimitHelp mAnswerInputTextNumLimitHelp;
    private InputTextNumLimitHelp mQuestionInputTextNumLimitHelp;
    private TextView mTipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    private void initView() {
        setStandardAListener(this);
        initTitle();
        setNoteContentView(R.layout.password_protect_layout);
        EditText answerView = (EditText) findViewById(R.id.vsq_answer_edit_id);
        mQuestion = (EditText) findViewById(R.id.vsq_question_edit_id);
        mQuestion.setText(mCurrentQuestion);
        mAnswerView = answerView;
        mAnswerView.requestFocus();
        mTipTextView = (TextView) findViewById(R.id.vsq_tip_text_id);

        mMoreQ = (ImageView) findViewById(R.id.more_question);
        setMoreQState(false);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.question_layout);
        if (mIsFromForgetPassword) {
            mQuestion.setEnabled(false);
            mMoreQ.setVisibility(View.INVISIBLE);
            relativeLayout.setEnabled(false);
        } else {
            mMoreQ.setVisibility(View.VISIBLE);
            relativeLayout.setOnClickListener(mClickListener);
        }
        mConfirm = (CyeeButton) findViewById(R.id.vsq_confirm_btn);
        mConfirm.setEnabled(!TextUtils.isEmpty(mAnswerView.getText()));
        mConfirm.setTextColor(getTextColor());
        mConfirm.setOnClickListener(mClickListener);
        mCancle = (CyeeButton) findViewById(R.id.vsq_cancle_btn);
        if (mIsFromForgetPassword || mModifingQA) {
            mCancle.setVisibility(View.GONE);
        } else {
            mCancle.setOnClickListener(mClickListener);
        }
        setNoteRootViewBackgroundColor();
        mAnswerInputTextNumLimitHelp = new InputTextNumLimitHelp(mAnswerView, 30, 10, 15);
        mAnswerInputTextNumLimitHelp.setTextChangedListener(mTextChangedListener);
        mQuestionInputTextNumLimitHelp = new InputTextNumLimitHelp(mQuestion, 39, 12, 12);

        if (mIsFromForgetPassword) {
            mTipTextView.setText(R.string.confirm_protect_question);
        }
    }

    private InputTextNumLimitHelp.TextChangedListener mTextChangedListener = new InputTextNumLimitHelp.
            TextChangedListener() {

        @Override
        public void onTextChange(Editable s) {
            mConfirm.setEnabled(!TextUtils.isEmpty(s));
            mConfirm.setTextColor(getTextColor());
        }
    };

    private int getTextColor() {
        if (!TextUtils.isEmpty(mAnswerView.getText())) {
            return getResources().getColor(R.color.enable_text_color);
        } else {
            return getResources().getColor(R.color.disable_text_color);
        }
    }

    private void initTitle() {
        if (mModifingQA) {
            setTitle(R.string.modified_protect_question);
        } else {
            if (hasSetProtectAnswer()) {
                setTitle(R.string.verify_sq_activity_title);
            } else {
                setTitle(R.string.setting_password_protect_QA);
            }
        }
    }

    private void initData() {
        mIsFromForgetPassword = getIntent().getBooleanExtra(FROM_FORGET_PASSWORD, false);
        mModifingQA = getIntent().getBooleanExtra(MODIFIED_PROTECT_QA, false);
        mPasswordProtectQuestions = getResources().getStringArray(R.array.password_protect_question);
        String saveQuestion = getSaveQuestion();
        if (TextUtils.isEmpty(saveQuestion)) {
            mCurrentQuestion = mPasswordProtectQuestions[0];
        } else {
            mCurrentQuestion = saveQuestion;
        }
    }

    private void setMoreQState(boolean isSelectState) {
        int drawableId = isSelectState ? R.drawable.password_qa_select_up
                : R.drawable.password_qa_select_down;
        mMoreQ.setImageDrawable(ContextCompat.getDrawable(this, drawableId));
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
    }

    @Override
    protected void onDestroy() {
        mQuestionInputTextNumLimitHelp.unRegisterWatcher();
        mAnswerInputTextNumLimitHelp.unRegisterWatcher();
        super.onDestroy();
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
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {
    }

    private boolean isQACorrected() {
        String question = mQuestion.getText().toString();
        String anwser = mAnswerView.getText().toString();
        if (TextUtils.isEmpty(anwser)) {
            return false;
        }
        return question.equals(getSaveQuestion()) && anwser.equals(getSaveAnswer());
    }

    private String getSaveQuestion() {
        DES des = new DES();
        String saveQuestion = NoteShareDataManager.getProtectQuestion(this);
        return des.authcode(saveQuestion, DES.OPERATION_DECODE, DES.DES_KEY);
    }

    private String getSaveAnswer() {
        DES des = new DES();
        String saveAnswer = NoteShareDataManager.getPasswordProtectAnswer(this);
        return des.authcode(saveAnswer, DES.OPERATION_DECODE, DES.DES_KEY);
    }

    private void confirmResponse() {
        Pattern pattern = Pattern.compile("^+\\s*+$");
        if (pattern.matcher(mQuestion.getText().toString()).find()
                || pattern.matcher(mAnswerView.getText().toString()).find()) {
            Toast.makeText(this, R.string.protect_question_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasSetProtectAnswer() && !mModifingQA || mIsFromForgetPassword) {
            checkProtectQA();
        } else {
            handleInputProtectQA();
        }
    }

    private void gotoSecrect() {
        try {
            Intent intent = new Intent(this, EncryptMainActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
        if (NoteShareDataManager.getInputPasswordErrorExceedFourTimes(this) != 0) {
            NoteShareDataManager.setInputPasswordErrorExceedFourTimes(this, 0);
        }

        if (NoteShareDataManager.getInputPasswordErrorCount(this) != 0) {
            NoteShareDataManager.setInputPasswordErrorCount(this, 0);
        }
        finish();
    }

    private void goPasswrodSetSuccess() {
        try {
            NoteShareDataManager.setPasswordAndQuestionsSetSuccessful(this, true);
            Intent intent = new Intent(this, PasswordSetSuccessedActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void checkProtectQA() {
        if (isQACorrected()) {
            if (mIsFromForgetPassword) {
                goModifyPassword();
            } else {
                gotoSecrect();
            }
            finish();
        } else {
            Toast.makeText(this, R.string.answer_incorrect, Toast.LENGTH_SHORT).show();
        }
    }

    private void goModifyPassword() {
        try {
            Intent intent = new Intent(this, PasswordActivity.class);
            intent.putExtra(PasswordActivity.MODIFIED_PASSWORD, true);
            intent.putExtra(PasswordActivity.FROM_PASSWORD_PROTECT, true);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void dismissDialog() {
        if (mQuestionSelectDialog != null && mQuestionSelectDialog.isShowing()) {
            mQuestionSelectDialog.dismiss();
            mQuestionSelectDialog = null;
        }
    }

    private void handleInputProtectQA() {
        String answer = mAnswerView.getText().toString();
        DES des = new DES();
        String saveQuestion = des.authcode(mQuestion.getText().toString(), DES.OPERATION_ENCODE, DES.DES_KEY);
        String saveAnswer = des.authcode(answer, DES.OPERATION_ENCODE, DES.DES_KEY);
        NoteShareDataManager.setProtectQuestion(this, saveQuestion);
        NoteShareDataManager.setPasswordProtectAnswer(this, saveAnswer);
        if (!mModifingQA) {
            goPasswrodSetSuccess();
        } else {
            Toast.makeText(this, R.string.successful_modification, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean hasSetProtectAnswer() {
        String protectQuestion = NoteShareDataManager.getPasswordProtectAnswer(this);
        return !TextUtils.isEmpty(protectQuestion);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.question_layout:
                    initQuestionSelectDialog();
                    break;
                case R.id.vsq_cancle_btn:
                    finish();
                    break;
                case R.id.vsq_confirm_btn:
                    confirmResponse();
                    break;
                case R.id.cancel:
                    dismissDialog();
                    break;
                default:
                    break;
            }
        }
    };

    private void initQuestionSelectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.password_protect_question, null);
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(mClickListener);
        ListView listView = (ListView) view.findViewById(R.id.question_list);
        EncryptQuestionsAdapter adapter = new EncryptQuestionsAdapter(this, mQuestion.getText().toString(),
                mPasswordProtectQuestions);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mPasswordProtectQuestions.length) {
                    mCurrentQuestion = mPasswordProtectQuestions[position];
                    mQuestion.setText(mCurrentQuestion);
                    mPosition = position;
                }
                dismissDialog();
            }
        });

        mQuestionSelectDialog = new Dialog(this, R.style.PasswordQuestionsDialogTheme);
        mQuestionSelectDialog.setCanceledOnTouchOutside(true);
        mQuestionSelectDialog.setContentView(view);
        mQuestionSelectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setMoreQState(false);
            }
        });

        Window window = mQuestionSelectDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        mQuestionSelectDialog.show();
        setMoreQState(true);
    }

    private class EncryptQuestionsAdapter extends BaseAdapter {
        private String[] mQuestionLists;
        private Context mContext;

        public EncryptQuestionsAdapter(Context context, String question, String[] questionLists) {
            this.mContext = context;
            this.mQuestionLists = questionLists;
            initPosition(question);
        }

        private void initPosition(String question) {
            for (int i = 0, len = mPasswordProtectQuestions.length; i < len; i++) {
                if (mPasswordProtectQuestions[i].equals(question)) {
                    mPosition = i;
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            if (null != mQuestionLists) {
                return mQuestionLists.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (null != mQuestionLists) {
                return mQuestionLists[position];
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (null == convertView) {
                holder = new Holder();
                convertView = View.inflate(mContext, R.layout.protect_qustion_list_item, null);
                holder.textView = (TextView) convertView.findViewById(R.id.question_list_item);
                holder.choiseRadioButton = (RadioButton) convertView.findViewById(R.id.question_list_item_select);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.textView.setText(mQuestionLists[position]);
            if (position == mPosition) {
                holder.choiseRadioButton.setChecked(true);
            } else {
                holder.choiseRadioButton.setChecked(false);
            }
            return convertView;
        }
    }

    private static class Holder {
        public TextView textView;
        public RadioButton choiseRadioButton;
    }
}
