package com.cydroid.note.app;

import com.gionee.framework.log.Logger;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NoteSelectionManager {

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;
    public static final int CANCEL_ALL_MODE = 4;

    private static final String TAG = "NoteSelectionManager";
    private static final boolean DEBUG = false;

    private Set<Path> mClickedSet;
    private NoteSet mSourceNoteSet;

    private SelectionListener mListener;

    private boolean mInverseSelection;
    private boolean mInSelectionMode;
    private int mTotal;
    private long mSourceVersion;

    public interface SelectionListener {
        void onSelectionModeChange(int mode);

        void onSelectionChange(Path path, boolean selected);
    }

    public NoteSelectionManager() {
        mClickedSet = new HashSet<>();
        mTotal = -1;
    }

    public void deSelectAll() {
        mInverseSelection = false;
        mClickedSet.clear();
        if (mListener != null) {
            mListener.onSelectionModeChange(CANCEL_ALL_MODE);
        }
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;

        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public ArrayList<Path> getSelected() {
        return getSelected(Integer.MAX_VALUE);
    }

    public ArrayList<Path> getSelected(int maxSelection) {
        ArrayList<Path> selected = new ArrayList<>();

        if (mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = Math.min(total - index, NoteSet.MEDIAITEM_BATCH_FETCH_COUNT);
                ArrayList<NoteItem> list = mSourceNoteSet.getNoteItem(index, count);
                for (NoteItem item : list) {
                    Path id = item.getPath();
                    if (!mClickedSet.contains(id)) {
                        selected.add(id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
                index += count;
            }
        } else {
            for (Path id : mClickedSet) {
                selected.add(id);
                if (selected.size() > maxSelection) {
                    return null;
                }
            }
        }
        return selected;
    }

    private int getTotalCount() {
        if (mSourceNoteSet == null) return 0;

        long sourceVersion = mSourceNoteSet.getDataVersion();
        if (mTotal < 0 || mSourceVersion != sourceVersion) {
            mSourceVersion = sourceVersion;
            mTotal = mSourceNoteSet.getNoteItemCount();
        }
        return mTotal;
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (DEBUG) {
            Logger.printLog(TAG, "count = " + count + ",mInverseSelection = "
                    + mInverseSelection + ",getTotalCount() = " + getTotalCount());
        }
        if (mInverseSelection) {
            count = getTotalCount() - count;
        }
        return count;
    }

    public boolean inSelectAllMode() {
        return mInverseSelection && mClickedSet.size() == 0;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;

        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void setSourceMediaSet(NoteSet set) {
        mSourceNoteSet = set;
        mSourceVersion = set.getDataVersion();
        mTotal = -1;
    }

    public void selectAll() {
        mInverseSelection = true;
        mClickedSet.clear();
        mTotal = -1;
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public void toggle(Path path) {
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
        }

        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
    }

    public boolean isItemSelected(Path itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }
}
