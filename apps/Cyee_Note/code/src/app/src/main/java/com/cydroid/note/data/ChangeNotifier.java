package com.cydroid.note.data;

import android.net.Uri;

import com.cydroid.note.app.NoteAppImpl;

import java.util.concurrent.atomic.AtomicBoolean;

// This handles change notification for media sets.
public class ChangeNotifier {

    private NoteSet mMediaSet;
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);

    public ChangeNotifier(NoteSet set, Uri uri, NoteAppImpl application) {
        mMediaSet = set;
        application.getDataManager().registerChangeNotifier(uri, this);
    }

    // Returns the dirty flag and clear it.
    public boolean isDirty() {
        return mContentDirty.compareAndSet(true, false);
    }

    public void fakeChange() {
        onChange(false);
    }

    protected void onChange(boolean selfChange) {
        if (mContentDirty.compareAndSet(false, true)) {
            mMediaSet.notifyContentChanged();
        }
    }
}