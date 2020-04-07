package com.cydroid.note.ai;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;

/**
 * Created by gaojt on 16-1-16.
 */
public class AISearchView extends RelativeLayout implements View.OnClickListener {
    private EditText mInputMsgView;
    private ImageView mSearchBtn;
    private OnQueryTextListener mListener;

    public interface OnQueryTextListener {
        void onQueryText(String newText);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int start,
                                  int before, int after) {
        }

        public void afterTextChanged(Editable s) {
            updateSearchBtnState();
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener =
            new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    notifyListener();
                    return true;
                }
            };

    public AISearchView(Context context) {
        this(context, null);
    }

    public AISearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AISearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInputMsgView = (EditText) findViewById(R.id.recommend_search_edit);
        mInputMsgView.addTextChangedListener(mTextWatcher);
        mInputMsgView.setOnEditorActionListener(mOnEditorActionListener);
        mSearchBtn = (ImageView) findViewById(R.id.recommend_search_btn);
        mSearchBtn.setOnClickListener(this);
        mSearchBtn.setEnabled(false);
        Drawable icon = ContextCompat.getDrawable(getContext(),
                R.drawable.note_main_activity_title_dw_search);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(getContext(),
                R.color.ai_search_drawable_tint_color));
        mSearchBtn.setImageDrawable(tintIcon);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mListener = listener;
    }

    private void updateSearchBtnState() {
        final boolean hasText = !TextUtils.isEmpty(mInputMsgView.getText());
        final boolean enableBtn = hasText;
        mSearchBtn.setEnabled(enableBtn);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mInputMsgView.removeTextChangedListener(mTextWatcher);
        mInputMsgView.setOnEditorActionListener(null);
        mTextWatcher = null;
        mOnEditorActionListener = null;
        mListener = null;
    }

    private void notifyListener() {
        if (mListener == null) {
            return;
        }
        if (TextUtils.isEmpty(mInputMsgView.getText())) {
            return;
        }
        mListener.onQueryText(mInputMsgView.getText().toString());
    }

    @Override
    public void onClick(View v) {
        notifyListener();
    }

    public void hintInputMehtod() {
        InputMethodManager imm = (InputMethodManager) NoteAppImpl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mInputMsgView)) {
            imm.hideSoftInputFromWindow(mInputMsgView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
