package com.cydroid.note.app;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cydroid.note.R;

public class NoteItemDecoration extends RecyclerView.ItemDecoration {
    private int mColumnGap;

    public NoteItemDecoration(Context context) {
        mColumnGap = context.getResources().getDimensionPixelSize(R.dimen.home_note_item_gap);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = mColumnGap;
    }
}
