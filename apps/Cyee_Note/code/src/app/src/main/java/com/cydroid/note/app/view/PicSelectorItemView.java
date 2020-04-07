package com.cydroid.note.app.view;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cydroid.note.R;

public class PicSelectorItemView extends FrameLayout {

    private static final int IMAGEVIEW_ID = 1;
    private static final int CHECKBOX_ID = 2;
    private static final int SELECT_STATE_ID = 3;
    private int mCheckBoxMargin;
    private ImageView mImageView;
    private ImageView mCheckBox;

    public PicSelectorItemView(Context context) {
        super(context);
        initView(context);
    }

    public PicSelectorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PicSelectorItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mCheckBoxMargin = context.getResources().getDimensionPixelOffset
                (R.dimen.attach_selector_checkbox_margin);
        mImageView = new ImageView(context);
        mImageView.setId(IMAGEVIEW_ID);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                R.drawable.attach_pic_selector_bg, null));
        FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageViewParams.width = context.getResources().getDimensionPixelOffset
                (R.dimen.attach_selector_pic_default_widht);
        imageViewParams.height = context.getResources().getDimensionPixelOffset
                (R.dimen.attach_selector_pic_height);
        addView(mImageView, imageViewParams);

        FrameLayout.LayoutParams checkBoxParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mCheckBox = new ImageView(context);
        mCheckBox.setId(CHECKBOX_ID);
        addView(mCheckBox, checkBoxParams);


        FrameLayout.LayoutParams selectStateParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView selectorStateView = new ImageView(context);
        selectorStateView.setId(SELECT_STATE_ID);
        selectorStateView.setImageDrawable
                (context.getResources().getDrawable(R.drawable.attach_pic_selector_bg));
        addView(selectorStateView, selectStateParams);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public ImageView getCheckBox() {
        return mCheckBox;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            if (child.getId() == IMAGEVIEW_ID) {
                layoutImageView(child);
            } else if (child.getId() == CHECKBOX_ID) {
                layoutCheckBox(child);
            } else {
                layout(child);
            }
        }
    }

    private void layoutImageView(View imageVie) {
        imageVie.layout(0, 0, imageVie.getMeasuredWidth(), imageVie.getMeasuredHeight());
    }

    private void layoutCheckBox(View checkBox) {
        int left = getMeasuredWidth() - checkBox.getMeasuredWidth() - mCheckBoxMargin;
        checkBox.layout(left, mCheckBoxMargin, left + checkBox.getMeasuredWidth(),
                mCheckBoxMargin + checkBox.getMeasuredHeight());
    }

    private void layout(View selectorStateView) {
        selectorStateView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}
