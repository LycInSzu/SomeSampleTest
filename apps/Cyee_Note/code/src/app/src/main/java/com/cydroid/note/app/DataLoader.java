package com.cydroid.note.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.cydroid.note.common.Log;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.ContentListener;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteObject;
import com.cydroid.note.data.NoteSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class DataLoader {
    private static final String TAG = "DataLoader";
    private static final int DATA_CACHE_SIZE = 100;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    private static final int MIN_LOAD_COUNT = 32;
    private static final int MAX_LOAD_COUNT = 64;

    private final NoteItem[] mData;
    private final long[] mItemVersion;
    private final long[] mSetVersion;
    private final NoteSet mNoteSet;
    private final Handler mMainHandler;
    private int mActiveStart = 0;
    private int mActiveEnd = 0;
    private int mContentStart = 0;
    private int mContentEnd = 0;
    private long mSourceVersion = NoteObject.INVALID_DATA_VERSION;
    private int mCount = 0;
    private DataListener mDataListener;
    private MyContentListener mContentListener = new MyContentListener();
    private LoadingListener mLoadingListener;
    private ReloadTask mReloadTask;
    // the data version on which last loading failed
    private long mFailedVersion = NoteObject.INVALID_DATA_VERSION;

    public DataLoader(Activity activity, NoteSet noteSet) {
        mNoteSet = noteSet;
        mData = new NoteItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, NoteObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, NoteObject.INVALID_DATA_VERSION);

        mMainHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
                    case MSG_LOAD_START:
                        if (mLoadingListener != null) mLoadingListener.onLoadingStarted();
                        return;
                    case MSG_LOAD_FINISH:
                        if (mLoadingListener != null) {
                            boolean loadingFailed = (mFailedVersion != NoteObject.INVALID_DATA_VERSION);
                            mLoadingListener.onLoadingFinished(loadingFailed);
                        }
                        return;
                }
            }
        };
    }

    public void resume() {
        mNoteSet.addContentListener(mContentListener);
        mReloadTask = new ReloadTask();
        mReloadTask.start();
    }

    public void pause() {
        mReloadTask.terminate();
        mReloadTask = null;
        mNoteSet.removeContentListener(mContentListener);
    }

    public NoteItem get(int index) {
        if (!isActive(index)) {
            return mNoteSet.getNoteItem(index, 1).get(0);
        }
        return mData[index % mData.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int getCount() {
        return mCount;
    }

    private void clearSlot(int slotIndex) {
        mData[slotIndex] = null;
        mItemVersion[slotIndex] = NoteObject.INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = NoteObject.INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;
        int end = mContentEnd;
        int start = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null) mReloadTask.notifyDirty();
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;

        NoteUtils.assertTrue(start <= end
                && end - start <= mData.length && end <= mCount);

        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;

        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = NoteUtils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mCount - length));
        int contentEnd = Math.min(contentStart + length, mCount);
        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(
                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public interface DataListener {
        void onContentChanged(int index);

        void onCountChanged(int count);
    }

    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<NoteItem> items;
    }

    private class MyContentListener implements ContentListener {
        @Override
        public void onContentDirty() {
            if (mReloadTask != null) {
                mReloadTask.notifyDirty();
            }
        }
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        @Override
        public UpdateInfo call() throws Exception {
            if (mFailedVersion == mVersion) {
                // previous loading failed, return null to pause loading
                return null;
            }
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mSourceVersion;
            info.size = mCount;
            long setVersion[] = mSetVersion;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % DATA_CACHE_SIZE;
                if (setVersion[index] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                    return info;
                }
            }
            return mSourceVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
            if (mCount != info.size) {
                mCount = info.size;
                if (mDataListener != null) mDataListener.onCountChanged(mCount);
                if (mContentEnd > mCount) mContentEnd = mCount;
                if (mActiveEnd > mCount) mActiveEnd = mCount;
            }

            ArrayList<NoteItem> items = info.items;

            mFailedVersion = NoteObject.INVALID_DATA_VERSION;
            if ((items == null) || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);
            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;
                NoteItem updateItem = items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (mItemVersion[index] != itemVersion) {
                    mItemVersion[index] = itemVersion;
                    mData[index] = updateItem;
                    if (mDataListener != null && i >= mActiveStart && i < mActiveEnd) {
                        mDataListener.onContentChanged(i);
                    }
                }
            }
            return null;
        }
    }

    /*
     * The thread model of ReloadTask
     *      *
     * [Reload Task]       [Main Thread]
     *       |                   |
     * getUpdateInfo() -->       |           (synchronous call)
     *     (wait) <----    getUpdateInfo()
     *       |                   |
     *   Load Data               |
     *       |                   |
     * updateContent() -->       |           (synchronous call)
     *     (wait)          updateContent()
     *       |                   |
     *       |                   |
     */
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading) return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        updateLoading(false);
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "unexpected interrupt: " + this);
                        }
                        continue;
                    }
                    mDirty = false;
                }
                updateLoading(true);
                long version = mNoteSet.reload();
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete) continue;
                if (info.version != version) {
                    info.size = mNoteSet.getNoteItemCount();
                    if (info.size < 0) {
                        continue;
                    }
                    info.version = version;
                }
                if (info.reloadCount > 0) {
                    info.items = mNoteSet.getNoteItem(info.reloadStart, info.reloadCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }
}
