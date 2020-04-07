package com.cydroid.note.data;

import android.net.Uri;
import android.os.Handler;

import com.cydroid.note.app.LabelManager;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.Future;
import com.cydroid.note.common.FutureListener;
import com.cydroid.note.common.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public abstract class LabelNoteSet extends NoteSet implements FutureListener<ArrayList<NoteItem>> {
    private static final String TAG = "LabelNoteSet";

    private NoteAppImpl mApp;
    protected Uri mBaseUri;
    protected NoteSet mBaseSet;
    private final Handler mMainHandler;
    private Future<ArrayList<NoteItem>> mLoadTask;
    private int mLabel;
    protected ChangeNotifier mNotifier;
    private ArrayList<NoteItem> mLoadBuffer;
    private ArrayList<NoteItem> mLabelNotes = new ArrayList<>();
    private ArrayList<LabelManager.LabelHolder> mLabels;

    public LabelNoteSet(Path path, NoteAppImpl application) {
        super(path, nextVersionNumber());
        mApp = application;
        mMainHandler = new Handler(application.getMainLooper());
        initDataSource();
    }

    protected abstract void initDataSource();

    @Override
    public Uri getContentUri() {
        return mBaseUri;
    }

    @Override
    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        ArrayList<NoteItem> list = new ArrayList<>();
        ArrayList<NoteItem> noteItems = mLabelNotes;
        int end = start + count;
        int size = noteItems.size();
        if (size == 0 || start >= size) {
            return list;
        }
        if (end > size) {
            end = size;
        }
        List<NoteItem> subList = noteItems.subList(start, end);
        list.addAll(subList);
        return list;
    }

    @Override
    public int getNoteItemCount() {
        return mLabelNotes.size();
    }

    @Override
    public synchronized long reload() {
        if (mNotifier.isDirty()) {
            if (mLoadTask != null) {
                mLoadTask.cancel();
            }
            mLoadTask = mApp.getThreadPool().submit(new LabelNoteLoader(), this);
        }

        if (mLoadBuffer != null) {
            mLabelNotes = mLoadBuffer;
            mLoadBuffer = null;
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<NoteItem>> future) {
        if (mLoadTask != future) {
            return;        // ignore, wait for the latest task
        }
        mLoadBuffer = future.get();
        if (mLoadBuffer == null) {
            mLoadBuffer = new ArrayList<NoteItem>();
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
            }
        });
    }

    public synchronized void setLabel(int label) {
        mLabel = label;
        mLoadBuffer = null;
        mLabelNotes = new ArrayList<>();
        mNotifier.fakeChange();
    }

    private boolean containLabel(ArrayList<Integer> itemLabel) {
        int label = mLabel;
        return itemLabel.contains(label);
    }

    public void setLabels(ArrayList<LabelManager.LabelHolder> labels) {
        synchronized (this) {
            mLabels = labels;
            mDataVersion = nextVersionNumber();
        }
        notifyContentChanged();
    }

    private boolean containLabel(int label) {
        synchronized (this) {
            ArrayList<LabelManager.LabelHolder> labels = mLabels;
            for (LabelManager.LabelHolder holder : labels) {
                if (holder.mId == label) {
                    return true;
                }
            }
        }
        return false;
    }

    private class LabelNoteLoader implements ThreadPool.Job<ArrayList<NoteItem>> {
        @Override
        public ArrayList<NoteItem> run(final ThreadPool.JobContext jc) {
            final ArrayList<NoteItem> labelItems = new ArrayList<>();
            if (!containLabel(mLabel)) {
                return labelItems;
            }
            mBaseSet.enumerateNoteItems(new NoteSet.ItemConsumer() {
                @Override
                public void consume(int index, NoteItem item) {
                    if (containLabel(item.getLabel())) {
                        labelItems.add(item);
                    }
                }
            }, jc);

            return labelItems;
        }
    }
}
