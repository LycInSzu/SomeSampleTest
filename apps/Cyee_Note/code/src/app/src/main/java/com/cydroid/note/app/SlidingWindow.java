package com.cydroid.note.app;

import android.app.Activity;
import android.net.Uri;

import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteParser;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;

public class SlidingWindow implements DataLoader.DataListener {
    private static final String TAG = "SlidingWindow";
    private static final int CACHE_SIZE = 36;
    private final DataLoader mDataLoader;
    private NoteParser mNoteParser;
    private final NoteEntry mData[];
    private int mCount;
    private int mContentStart = 0;
    private int mContentEnd = 0;
    private int mActiveStart = 0;
    private int mActiveEnd = 0;
    private Listener mListener;
    private boolean mIsActive = false;
    private Object mLock = new Object();

    public SlidingWindow(Activity activity, NoteSet set, LoadingListener loadingListener) {
        mDataLoader = new DataLoader(activity, set);
        mDataLoader.setLoadingListener(loadingListener);
        mDataLoader.setDataListener(this);
        mData = new NoteEntry[CACHE_SIZE];
        mNoteParser = new NoteParser();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public NoteEntry get(int itemIndex) {
        synchronized (mLock) {
            return mData[itemIndex % mData.length];
        }
    }

    private boolean isActiveSlot(int itemIndex) {
        return itemIndex >= mActiveStart && itemIndex < mActiveEnd; //NOSONAR
    }

    private boolean isContentSlot(int itemIndex) {
        return itemIndex >= mContentStart && itemIndex < mContentEnd;  //NOSONAR
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mDataLoader.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mDataLoader.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareSlotContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
    }

    public void setActiveWindow(int start, int end) {

        if (!(start <= end && end - start <= mData.length && end <= mCount)) {
            NoteUtils.fail("%s, %s, %s, %s", start, end, mData.length, mCount);
        }
        NoteEntry data[] = mData;

        mActiveStart = start;
        mActiveEnd = end;

        int contentStart = start;
        int contentEnd = end;
        if (mIsActive) {
            contentStart = NoteUtils.clamp((start + end) / 2 - data.length / 2,
                    0, Math.max(0, mCount - data.length));
            contentEnd = Math.min(contentStart + data.length, mCount);
        }
        setContentWindow(contentStart, contentEnd);
    }

    private void freeSlotContent(int itemIndex) {
        synchronized (mLock) {
            NoteEntry data[] = mData;
            int index = itemIndex % data.length;
            data[index] = null;
        }
    }

    private void prepareSlotContent(final int itemIndex) {

        ThreadPool.Job<Void> prepareJob = new ThreadPool.Job<Void>() {
            @Override
            public Void run(ThreadPool.JobContext jc) {
                NoteEntry entry = new NoteEntry();
                NoteItem item = mDataLoader.get(itemIndex); // item could be null;
                entry.item = item;
                entry.path = (item == null) ? null : item.getPath();
                mNoteParser.parseNote(entry, item);
                synchronized (mLock) {
                    mData[itemIndex % mData.length] = entry;
                }
                if (mListener != null && isActiveSlot(itemIndex)) {
                    mListener.onContentChanged();
                }
                return null;
            }
        };
        NoteAppImpl.getContext().getThreadPool().submit(prepareJob);
    }

    public void resume() {
        mIsActive = true;
        mDataLoader.resume();
        if (PlatformUtil.isGioneeDevice()) {
            setActiveWindow(mActiveStart, mActiveEnd);
        }
    }

    public void pause() {
        mIsActive = false;
        mDataLoader.pause();
        if (PlatformUtil.isGioneeDevice()) {
            setActiveWindow(mActiveStart, mActiveEnd);
        }
    }

    public void destroy() {
        mDataLoader.setLoadingListener(null);
    }

    @Override
    public void onCountChanged(int count) {
        if (mCount != count) {
            mCount = count;
            if (mListener != null) {
                mListener.onCountChanged(mCount);
            }
            if (mContentEnd > mCount) {
                mContentEnd = mCount;
            }
            if (mActiveEnd > mCount) {
                mActiveEnd = mCount;
            }
        }
    }

    @Override
    public void onContentChanged(int itemIndex) {
        if (itemIndex >= mContentStart && itemIndex < mContentEnd) {
            freeSlotContent(itemIndex);
            prepareSlotContent(itemIndex);
            if (mListener != null && isActiveSlot(itemIndex)) {
                mListener.onContentChanged();
            }
        }
    }

    public interface Listener {
        void onCountChanged(int count);

        void onContentChanged();
    }

    public static class NoteEntry {
        public NoteItem item;
        public Path path;
        public int mediaType = NoteItem.MEDIA_TYPE_NONE;
        public Uri thumbnailUri = null;
        public Uri originUri = null;
        public String title = null;
        public String content = null;
        public long reminder = NoteItem.INVALID_REMINDER;
        public String time;
        public int id;
        public long timeMillis;
        public int encrytRemindReadState;
        public boolean isEncrypt;
    }
}