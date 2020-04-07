package com.cydroid.note.app;

import android.app.Activity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import com.cydroid.note.R;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;


public class GrideRecyclerViewAdapter extends RecyclerViewBaseAdapter {

    public GrideRecyclerViewAdapter(Activity activity, NoteSet set,
                                    LoadingListener loadingListener,
                                    NoteSelectionManager noteSelectionManager,
                                    boolean displayHeader) {
        super(activity, set, loadingListener, noteSelectionManager, displayHeader);
    }


    protected int getLayoutId(int viewType) {
        return (viewType == NoteItem.MEDIA_TYPE_NONE ? R.layout.note_item_no_image
                : R.layout.note_item_have_image);
    }

    @Override
    public void onViewAttachedToWindow(NoteViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!mDisplayHeader) {
            return;
        }
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
        if (lp == null) {
            return;
        }
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == 0) {
            p.setFullSpan(true);
        } else {
            p.setFullSpan(false);
        }
    }
}
