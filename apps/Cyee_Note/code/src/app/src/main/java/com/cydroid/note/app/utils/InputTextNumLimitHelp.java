package com.cydroid.note.app.utils;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import com.cydroid.note.common.Log;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.view.TextLengthFilter;

import java.io.UnsupportedEncodingException;

import static java.lang.String.valueOf;


public class InputTextNumLimitHelp {
    private static final String TAG = "InputTextNumLimitHelp";

    private static final int INVALIDATE = -1;
    private static final int TYPE_ENGLISH_CHARACTER = 1;
    private static final int TYPE_BOTH = 2;
    private static final int TYPE_CHINESE_CHATACTER = 3;

    private int mEnglishCharacterMaxSize;
    private int mChineseCharacterMaxSize;
    private int mBothCharacterMaxSize;
    private EditText mInputView;
    private TextChangedListener mTextChangedListener;
    private ToastManager mToastManager;

    public interface TextChangedListener {
        void onTextChange(Editable s);
    }

    public InputTextNumLimitHelp(EditText inputView, int englishCharacterMaxSize, int chineseCharacterMaxSize,
                                 int bothCharacterMaxSize) {
        mEnglishCharacterMaxSize = englishCharacterMaxSize;
        mChineseCharacterMaxSize = chineseCharacterMaxSize;
        mBothCharacterMaxSize = bothCharacterMaxSize;
        mInputView = inputView;
        mToastManager = new ToastManager(NoteAppImpl.getContext());
        inputView.addTextChangedListener(mTextWatcher);
    }

    public void unRegisterWatcher() {
        mInputView.removeTextChangedListener(mTextWatcher);
        mInputView = null;
        mTextChangedListener = null;
        mToastManager.destroy();
    }

    public void setTextChangedListener(TextChangedListener listener) {
        mTextChangedListener = listener;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                int characterType = getInputCharactersType(s);
                setInputContentMaxSize(characterType, valueOf(s), count);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (null != mTextChangedListener) {
                mTextChangedListener.onTextChange(s);
            }
        }
    };

    private int getInputCharactersType(CharSequence input) {
        final CharSequence text = input;
        boolean isSameType = true;
        for (int i = 0, len = text.length(); i < len - 1; i++) {
            char pre = text.charAt(i);
            char next = text.charAt(i + 1);
            try {
                int preByteNum = valueOf(pre).getBytes("utf-8").length;
                int nextByteNum = valueOf(next).getBytes("utf-8").length;

                if (preByteNum != nextByteNum) {
                    isSameType = false;
                    break;
                }
            } catch (UnsupportedEncodingException e) {

            }
        }

        try {
            if (isSameType) {
                String inputString = valueOf(text.charAt(0));
                return inputString.getBytes("utf-8").length;
            }
        } catch (UnsupportedEncodingException e) {
        }
        return TYPE_BOTH;
    }

    private void setInputContentMaxSize(int inputCharacterType, String inputText, int count) {
        int maxInputSize = getInputContentMaxSize(inputCharacterType, inputText, count);
		//Chenyee wanghaiyan 2018-3-7 modify for CSW1703A-653 begin
        try {
            if (INVALIDATE != maxInputSize) {
            
                Log.d(TAG,"maxInputSize" + maxInputSize + "inputText.length()" + inputText.length());
                if (maxInputSize < inputText.length()) {
                    mInputView.setText(inputText.substring(0, maxInputSize - 1));
                    mInputView.setSelection(maxInputSize - 1);
                }
                mInputView.setFilters(new InputFilter[]{new TextLengthFilter(maxInputSize, mToastManager)});
            }
        } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
        }
		//Chenyee wanghaiyan 2018-3-7 modify for CSW1703A-653 end
    }

    private int getInputContentMaxSize(int inputCharacterType, String inputText, int count) {
        int maxInputSize = INVALIDATE;
        if (TYPE_ENGLISH_CHARACTER == inputCharacterType) {

            maxInputSize = getEnglishOrChineseMaxInputSize(inputText, count, mEnglishCharacterMaxSize);
        } else if (TYPE_CHINESE_CHATACTER == inputCharacterType) {

            maxInputSize = getEnglishOrChineseMaxInputSize(inputText, count, mChineseCharacterMaxSize);
        } else {

            maxInputSize = mBothCharacterMaxSize;
        }
        return maxInputSize;
    }

    private int getEnglishOrChineseMaxInputSize(String inputText, final int count, final int maxSize) {
        int maxInputSize = INVALIDATE;
        if (count == 1) {
            maxInputSize = maxSize;
        } else {
            if (inputText.length() >= maxSize) {
                maxInputSize = maxSize;
            }
        }
        return maxInputSize;
    }

}
