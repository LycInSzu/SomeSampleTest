package com.cydroid.note.data;


import com.cydroid.note.common.ThreadPool;

import java.util.ArrayList;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class NoteSet extends NoteObject {
    public static final int MEDIAITEM_BATCH_FETCH_COUNT = 50;
    private WeakHashMap<ContentListener, Object> mListeners = new WeakHashMap<>();

    public interface ItemConsumer {
        void consume(int index, NoteItem item);
    }


    public NoteSet(Path path, long version) {
        super(path, version);
    }

    public int getNoteItemCount() {
        return 0;
    }

    // Returns the media items in the range [start, start + count).
    //
    // The number of note items returned may be less than the specified count
    // if there are not enough media items available. The number of
    // note items available may not be consistent with the return value of
    // getNoteItemCount() because the contents of database may have already
    // changed.
    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        return new ArrayList<NoteItem>();
    }

    // Reload the content. Return the current data version. reload() should be called
    // in the same thread as getMediaItem(int, int)
    public abstract long reload();

    // Enumerate all note items in this note set
    // in an efficient order. ItemConsumer.consumer() will be
    // called for each note item with its index.
    public void enumerateNoteItems(ItemConsumer consumer, final ThreadPool.JobContext jc) {
        enumerateNoteItems(consumer, 0, jc);
    }

    // The default implementation uses getNoteItem() for enumerateNoteItems().
    // Subclasses may override this and use more efficient implementations.
    // Returns the number of items enumerated.
    protected void enumerateNoteItems(ItemConsumer consumer, int startIndex, final ThreadPool.JobContext jc) {
        int total = getNoteItemCount();
        int start = 0;
        while (start < total) {
            if (jc.isCancelled()) {
                return;
            }
            int count = Math.min(MEDIAITEM_BATCH_FETCH_COUNT, total - start);
            ArrayList<NoteItem> items = getNoteItem(start, count);
            for (int i = 0, n = items.size(); i < n; i++) {
                if (jc.isCancelled()) {
                    return;
                }
                NoteItem item = items.get(i);
                consumer.consume(startIndex + start + i, item);
            }
            start += count;
        }
    }

    // NOTE: The MediaSet only keeps a weak reference to the listener. The
    // listener is automatically removed when there is no other reference to
    // the listener.
    public void addContentListener(ContentListener listener) {
        synchronized (mListeners) {
            mListeners.put(listener, null);
        }
    }

    public void removeContentListener(ContentListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    // This should be called by subclasses when the content is changed.
    public void notifyContentChanged() {
        synchronized (mListeners) {
            Set<ContentListener> set = mListeners.keySet();
            for (ContentListener listener : set) {
                listener.onContentDirty();
            }
        }
    }
}
