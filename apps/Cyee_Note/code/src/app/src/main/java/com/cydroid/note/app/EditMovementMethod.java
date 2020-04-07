package com.cydroid.note.app;

import android.app.Activity;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

import com.cydroid.note.app.span.AbstractClickSpan;
import com.cydroid.note.encrypt.EncryptDetailActivity;

public class EditMovementMethod extends ArrowKeyMovementMethod {
    private static final int MOVE_DISTANCE = 5;
    private Activity mActivity;
    private int mDownX;
    private int mDownY;
    private long mKeyDownTime = 0;

    public EditMovementMethod(Activity activity) {
        mActivity = activity;
    }

    private boolean isMoved(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int add = (x - mDownX) * (x - mDownX) + (y - mDownY) * (y - mDownY);
        int distance = (int) Math.sqrt(add);
        return distance > MOVE_DISTANCE;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            mDownX = (int) event.getX();
            mDownY = (int) event.getY();
            mKeyDownTime = System.currentTimeMillis();
            if(mActivity instanceof NewNoteActivity){
                ((NewNoteActivity)mActivity).hideSoftInput();
            }else {
                ((EncryptDetailActivity)mActivity).hideSoftInput();
            }

        }
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);
            int lineBottom = layout.getLineBottom(line);

            AbstractClickSpan[] link = buffer.getSpans(off, off, AbstractClickSpan.class);

            if (action == MotionEvent.ACTION_UP) {
                if (link.length != 0) {
                    if (!isMoved(event) && link[0].isClickValid(widget, event, lineBottom)) {
                        long keyUpTime = System.currentTimeMillis();
                        if (!isLongTouch(mKeyDownTime, keyUpTime)) {
                            link[0].onClick(widget);
                        }
                        return true;
                    }
                }
                mKeyDownTime = 0;
                if(mActivity instanceof NewNoteActivity){
                    ((NewNoteActivity)mActivity).enterEditMode();
                }else {
                    ((EncryptDetailActivity)mActivity).enterEditMode();
                }
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    private boolean isLongTouch(long downTime, long upTime) {
        if (upTime-downTime > 200){
            return true;
        }
        return false;
    }

    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }
}
