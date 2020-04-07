package com.cydroid.note.app;

import android.app.Activity;

import com.cydroid.note.R;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;

/**
 * Created by spc on 16-7-8.
 */
public class ListRecyclerViewAdapter extends RecyclerViewBaseAdapter {

    public ListRecyclerViewAdapter(Activity activity, NoteSet set,
                                   LoadingListener loadingListener,
                                   NoteSelectionManager noteSelectionManager,
                                   boolean displayHeader) {
        super(activity, set, loadingListener, noteSelectionManager, displayHeader);
    }

    protected int getLayoutId(int viewType) {
        return (viewType == NoteItem.MEDIA_TYPE_NONE ? R.layout.list_note_item_have_no_image
                : R.layout.list_note_item_hava_image);
    }
}
