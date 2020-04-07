package com.cydroid.note.app.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.cydroid.note.app.RecyclerViewBaseAdapter;
import com.cydroid.note.encrypt.EncryptSelectRecyclerViewAdapter;

public class NoteRecyclerView extends RecyclerView {
    private Adapter mAdapter;

    public NoteRecyclerView(Context context) {
        this(context, null);
    }

    public NoteRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initListener();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        notifyVisibleRangeChanged();
    }

    private void initListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                notifyVisibleRangeChanged();
            }
        });
    }

    private void notifyVisibleRangeChanged() {
        Adapter adapter = mAdapter;
        if (adapter == null) {
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

        if (adapter instanceof EncryptSelectRecyclerViewAdapter) {
            ((EncryptSelectRecyclerViewAdapter) adapter).notifyVisibleRangeChanged(visibleStart, visibleEnd);
        } else {
            RecyclerViewBaseAdapter baseAdapter = (RecyclerViewBaseAdapter) adapter;
            if (baseAdapter.getIsDisplayHeader()) {
                baseAdapter.notifyVisibleRangeChanged(visibleStart, visibleEnd > 1 ? (visibleEnd - 1) : visibleStart);
            } else {
                baseAdapter.notifyVisibleRangeChanged(visibleStart, visibleEnd);
            }
        }
    }
}
