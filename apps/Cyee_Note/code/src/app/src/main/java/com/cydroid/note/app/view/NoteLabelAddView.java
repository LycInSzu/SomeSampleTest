package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.utils.InputTextNumLimitHelp;
import com.cydroid.note.common.NoteUtils;

public class NoteLabelAddView extends LinearLayout implements View.OnClickListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "NoteLabelAddView";
    private EditText mInputMsgView;
    private ImageView mOkMsgView;
    private OnAddLabelListener mOnAddLabelListener;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;
    private Context mContext;

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(mInputMsgView, 0);
            }
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onOkBtnClicked();
            }
            return true;
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.label_custom_edit_button) {
            onOkBtnClicked();
        }
    }

    public interface OnAddLabelListener {
        void onAddLabel(String newLabelName);
    }

    public NoteLabelAddView(Context context) {
        this(context, null);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void initWatcher(EditText editText) {
        mInputTextNumLimitHelp = new InputTextNumLimitHelp(editText, 30, 15, 15);
        mInputTextNumLimitHelp.setTextChangedListener(mTextChangedListener);
    }

    private InputTextNumLimitHelp.TextChangedListener mTextChangedListener = new InputTextNumLimitHelp.
            TextChangedListener() {

        @Override
        public void onTextChange(Editable s) {
            NoteLabelAddView.this.onTextChanged();
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DEBUG) {
            Log.d(TAG, "onFinishInflate");
        }
        mInputMsgView = (EditText) findViewById(R.id.label_custom_edit_text);
        initWatcher(mInputMsgView);
        mInputMsgView.setOnEditorActionListener(mOnEditorActionListener);
        mInputMsgView.requestFocus();
        setImeVisibility(true);
        mOkMsgView = (ImageView) findViewById(R.id.label_custom_edit_button);
        tintImageViewDrawable(R.color.label_custom_edit_button_unable_color);
        mOkMsgView.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        mInputMsgView.setOnEditorActionListener(null);
        if (mInputTextNumLimitHelp != null) {
            mInputTextNumLimitHelp.unRegisterWatcher();
        }
        mInputTextNumLimitHelp = null;
        mOnAddLabelListener = null;
        mOnEditorActionListener = null;

    }

    public void setOnAddLabelListener(OnAddLabelListener listener) {
        mOnAddLabelListener = listener;
    }

    private void onTextChanged() {
        updateOkButton();
    }

    private void updateOkButton() {
        final boolean hasText = !TextUtils.isEmpty(mInputMsgView.getText().toString().trim());
        if (hasText) {
            tintImageViewDrawable(R.color.system_stress_color);
        } else {
            tintImageViewDrawable(R.color.label_custom_edit_button_unable_color);
        }
    }

    private void tintImageViewDrawable(int colorsId) {
        Drawable icon = ContextCompat.getDrawable(mContext, R.drawable.label_custom_add_sure);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(tintIcon, ContextCompat.getColor(mContext, colorsId));
        mOkMsgView.setImageDrawable(tintIcon);
    }

    private void onOkBtnClicked() {
        String labelName = mInputMsgView.getText().toString().trim();
         //Chenyee wanghaiyan 2018-1-29 modify for CSW1702A-2729 begin
        //labelName = NoteUtils.lineSpaceFilter(labelName);
        //Chenyee wanghaiyan 2018-1-29 modify for CSW1702A-2729 end
        mInputMsgView.setText("");
        mInputMsgView.requestFocus();
        setImeVisibility(true);
        if (!TextUtils.isEmpty(labelName)) {
            if (mOnAddLabelListener != null) {
                mOnAddLabelListener.onAddLabel(labelName);
            }
        }
    }


    private void setImeVisibility(final boolean visible) {
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

