package com.cydroid.note.app.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.cydroid.note.R;
import com.cydroid.note.common.NoteUtils;

public class EditLabelView extends ViewGroup {
    private int mScreenWidth;
    private int mHorizonPadding;
    private int mHorizonMargin;
    private int mVerticalMargin;

    public EditLabelView(Context context) {
        this(context, null);
    }

    public EditLabelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditLabelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

/*    public EditLabelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }*/

    private void init(Context context) {
        Resources res = context.getResources();
        mScreenWidth = NoteUtils.sScreenWidth;
        mHorizonPadding = res.getDimensionPixelSize(R.dimen.edit_note_item_margin);
        mHorizonMargin = res.getDimensionPixelSize(R.dimen.edit_note_label_item_horizon_margin);
        mVerticalMargin = res.getDimensionPixelSize(R.dimen.edit_note_label_item_vertical_margin);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            final View child = getChildAt(index);
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getWrapHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int right = mScreenWidth - mHorizonPadding;
        int row = 0;
        int totalX = l + mHorizonPadding;
        int totalY = 0;
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (totalX == l + mHorizonPadding) {
                totalX += width;
            } else {
                totalX += (mHorizonMargin + width);
            }
            if (totalX > right) {
                row++;
                totalX = l + mHorizonPadding + width;
            }
            totalY = (row + 1) * (height + mVerticalMargin);
            child.layout(totalX - width, totalY - height, totalX, totalY);
        }
    }

    private int getWrapHeight() {
        final int count = getChildCount();
        int right = mScreenWidth - mHorizonPadding;
        int row = 0;
        int totalX = mHorizonPadding;
        int totalY = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (totalX == mHorizonPadding) {
                totalX += width;
            } else {
                totalX += (mHorizonMargin + width);
            }
            if (totalX > right) {
                row++;
                totalX = mHorizonPadding + width;
            }
            totalY = (row + 1) * (height + mVerticalMargin);
        }
        return totalY + mVerticalMargin;
    }

}
