package com.cydroid.note.app.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.cydroid.note.app.attachment.PicSelectorAdapter;

public class AttachPicRecycleView extends RecyclerView {
    private PicSelectorAdapter mAdapter;
    private int mScrollDirection;
    public static final int SCROLL_LEFT = 0;
    public static final int SCROLL_RIGHT = 1;

    public AttachPicRecycleView(Context context) {
        this(context, null);
        initListener();
    }

    public AttachPicRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initListener();
    }

    public AttachPicRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initListener();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PicSelectorAdapter) adapter;
    }

    private void initListener() {
        addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    notifyVisibleRangeChanged();
                    mAdapter.preLoadPic(mScrollDirection);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx >= 0) {
                    mScrollDirection = SCROLL_RIGHT;
                } else {
                    mScrollDirection = SCROLL_LEFT;
                }
            }
        });
    }

    private void notifyVisibleRangeChanged() {
        if (mAdapter == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        int childCount = layoutManager.getChildCount();
        int visibleStart = 0;
        int visibleEnd = 0;
        if (childCount > 0) {
            visibleStart = layoutManager.getPosition(layoutManager.getChildAt(0));
            visibleEnd = layoutManager.getPosition(layoutManager.getChildAt(childCount - 1)) + 1;
        }
        mAdapter.notifyVisibleRangeChanged(visibleStart, visibleEnd);
    }
}
