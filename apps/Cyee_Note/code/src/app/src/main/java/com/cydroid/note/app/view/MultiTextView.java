package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import cyee.widget.CyeeTextView;


public class MultiTextView extends CyeeTextView {

    public MultiTextView(Context context) {
        this(context, null);
    }

    public MultiTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public MultiTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Layout layout = getLayout();
        TextPaint paint = getPaint();
        paint.setAlpha(0x80);
        CharSequence text = getText();
        float width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int lineHeight = getLineHeight();
        int lineCount = getLineCount();
        int paddingTop = getPaddingTop();

        for (int i = 0; i < lineCount; i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            int baseline = layout.getLineBaseline(i);
            CharSequence line = TextUtils.substring(text, lineStart, lineEnd);
            int nextLine = i + 1;
            int nextLineBottom = paddingTop + lineHeight * (nextLine + 1);
            if (nextLine < lineCount && nextLineBottom > height) {
                CharSequence ellipText = TextUtils.substring(text, lineStart, text.length());
                ellipText = TextUtils.ellipsize(ellipText, paint, width, TextUtils.TruncateAt.END);
                canvas.drawText(ellipText.toString(), paddingLeft, baseline, paint);
                break;
            }
            canvas.drawText(line.toString(), paddingLeft, baseline, paint);
        }
    }
}
