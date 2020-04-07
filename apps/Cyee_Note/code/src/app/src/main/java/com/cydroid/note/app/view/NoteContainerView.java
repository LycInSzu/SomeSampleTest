package com.cydroid.note.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.cydroid.note.R;
import com.cydroid.note.ai.AITipView;
import com.cydroid.note.common.NoteUtils;

/**
 * Created by spc on 16-3-31.
 */
public class NoteContainerView extends FrameLayout {

    private boolean mIMEVisible;

    public NoteContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        AITipView tipView = getAITipView();
        if (null == tipView) {
            return;
        }
        if (!mIMEVisible) {
            tipView.updatePosition(false);
        } else {
            boolean keepIMEVisbleState = !tipView.isMovedWhenIMEVisible();
            tipView.updatePosition(keepIMEVisbleState);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        AITipView tipView = getAITipView();
        if (null == tipView) {
            return;
        }
        if (h < NoteUtils.sScreenHeight * 2 / 3) {
            double ratio = 0;
            if (oldh > 0) {
                ratio = (double) h / oldh;
            }
            mIMEVisible = true;
            tipView.updatePosition(ratio);
            tipView.setIMEVisable(true);
        } else {
            tipView.setIMEVisable(false);
            mIMEVisible = false;
        }
    }

    protected void setFooterViewTop(int top) {
        AITipView tipView = getAITipView();
        if (tipView != null) {
            tipView.setMaxBottom(top);
        }
    }

    private AITipView getAITipView() {
        AITipView tipView = null;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.ai_tip_image) {
                tipView = (AITipView) child;
                break;
            }
        }
        return tipView;
    }
}
