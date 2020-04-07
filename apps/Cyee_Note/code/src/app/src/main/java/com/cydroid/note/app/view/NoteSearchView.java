package com.cydroid.note.app.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import com.cydroid.note.common.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.app.utils.InputTextNumLimitHelp;

public class NoteSearchView extends LinearLayout implements View.OnClickListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "NoteSearchView";
    private EditText mInputMsgView;
    private ImageView mClearMsgView;
    private OnQueryTextListener mOnQueryChangeListener;
    private CharSequence mOldQueryText;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(mInputMsgView, 500);
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int start,
                                  int before, int after) {
            NoteSearchView.this.onTextChanged(s);
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return true;
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_clear_btn) {
            onClearBtnClicked();
        }
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String newText);
    }

    public NoteSearchView(Context context) {
        this(context, null);
    }

    public NoteSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DEBUG) {
            Log.d(TAG, "onFinishInflate");
        }
        mInputMsgView = (EditText) findViewById(R.id.search_input_msg_edit_text);
        mInputMsgView.addTextChangedListener(mTextWatcher);
        mInputMsgView.setOnEditorActionListener(mOnEditorActionListener);
        mInputMsgView.requestFocus();
        //GIONEE wanghaiyan 2016-12-3 modify for 28315 for begin 
        //setImeVisibility(true);
        //GIONEE wanghaiyan 2016-12-3 modify for 28315 for begin
        mClearMsgView = (ImageView) findViewById(R.id.search_clear_btn);
        mClearMsgView.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow");
        }
        mInputTextNumLimitHelp = new InputTextNumLimitHelp(mInputMsgView, 30, 15, 15);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        mInputMsgView.removeTextChangedListener(mTextWatcher);
        if (mInputTextNumLimitHelp != null) {
            mInputTextNumLimitHelp.unRegisterWatcher();
        }
        mInputMsgView.setOnEditorActionListener(null);
        mTextWatcher = null;
        mOnEditorActionListener = null;
        mOnQueryChangeListener = null;

    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    private void onTextChanged(CharSequence newText) {
        updateClearButton();
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
    }

    private void updateClearButton() {
        final boolean hasText = !TextUtils.isEmpty(mInputMsgView.getText());
        final boolean showClearBtn = hasText;
        mClearMsgView.setVisibility(showClearBtn ? VISIBLE : GONE);
    }

    private void onClearBtnClicked() {
        mInputMsgView.setText("");
        mInputMsgView.requestFocus();
        setImeVisibility(true);
    }

    private void setImeVisibility(final boolean visible) {
	 Log.d(TAG,"visible+"+visible);
        if (visible) {
            post(mShowImeRunnable);
        } else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mInputMsgView.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void clearFocus() {
        mInputMsgView.clearFocus();
        setImeVisibility(false);
        super.clearFocus();
    }
}
