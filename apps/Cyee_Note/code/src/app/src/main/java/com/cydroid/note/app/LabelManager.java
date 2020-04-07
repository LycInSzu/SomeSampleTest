package com.cydroid.note.app;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.provider.LabelContract;
import com.cydroid.note.provider.NoteShareDataManager;

import java.util.ArrayList;

public class LabelManager {
    private static final String TAG = "LabelManager";
    private static final int[] DEFAULT_LABEL = new int[]{
            R.string.default_label_inspiration,
            R.string.default_label_travel,
            R.string.default_label_meeting,
            R.string.default_label_life
    };

    private Uri mLabelUri = LabelContract.LabelContent.CONTENT_URI;
    public static final String[] LABEL_PROJECTION = new String[]{//NOSONAR
            LabelContract.LabelContent._ID,//NOSONAR
            LabelContract.LabelContent.COLUMN_LABEL_CONTENT};//NOSONAR
    private String mOrderBy = LabelContract.LabelContent._ID + " DESC";
    private int INDEX_ID = 0;
    private int INDEX_CONTENT = 1;

    private NoteAppImpl mApp;
    private ContentResolver mResolver;
    private final ArrayList<LabelHolder> mLabelCache = new ArrayList<>();
    private ArrayList<LabelDataChangeListener> mListeners = new ArrayList<>();

    public interface LabelDataChangeListener {
        void onDataChange();
    }

    public LabelManager(NoteAppImpl app) {
        mApp = app;
        mResolver = app.getContentResolver();
    }

    public void init() {
        ThreadPool threadPool = mApp.getThreadPool();
        threadPool.submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                boolean isInit = getIsInitLabel(mApp);
                if (!isInit) {
                    initDefaultLabel();
                    setIsInitLabel(mApp, true);
                }
                initCache();
                notifyListener();
                return null;
            }
        });
    }

    public void addLabelDataChangeListener(LabelDataChangeListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeLabelDataChangeListener(LabelDataChangeListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public void notifyListener() {
        synchronized (mListeners) {
            for (LabelDataChangeListener listener : mListeners) {
                listener.onDataChange();
            }
        }
    }

    public ArrayList<LabelHolder> getLabelList() {
        synchronized (mLabelCache) {
            return new ArrayList<>(mLabelCache);
        }
    }

    private void initDefaultLabel() {
        ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
        for (int resId : DEFAULT_LABEL) {
            ContentProviderOperation ops = ContentProviderOperation.newInsert(mLabelUri)
                    .withValue(LabelContract.LabelContent.COLUMN_LABEL_CONTENT, mApp.getString(resId))
                    .build();
            insertOps.add(ops);
        }
        try {
            mResolver.applyBatch(LabelContract.AUTHORITY, insertOps);
        } catch (Exception e) {
            Log.d(TAG, "initDefaultLabel fail : " + e.toString());
        }
    }

    private void initCache() {
        Cursor cursor = mResolver.query(mLabelUri, LABEL_PROJECTION, null, null, mOrderBy);
        if (cursor == null) {
            Log.d(TAG, "query label fail");
            return;
        }
        ArrayList<LabelHolder> labels = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(INDEX_ID);
                String content = cursor.getString(INDEX_CONTENT);
                labels.add(new LabelHolder(id, content));
            }
        } finally {
            NoteUtils.closeSilently(cursor);
        }
        synchronized (mLabelCache) {
            mLabelCache.addAll(labels);
        }
    }

    public int addLabel(String content) {
        ContentValues values = new ContentValues();
        values.put(LabelContract.LabelContent.COLUMN_LABEL_CONTENT, content);
        Uri uri = mApp.getContentResolver().insert(mLabelUri, values);

        int id = (int) ContentUris.parseId(uri);
        synchronized (mLabelCache) {
            mLabelCache.add(0, new LabelHolder(id, content));
        }
        notifyListener();
        return id;
    }

    public int getLabelId(String labelContent) {
        int labelId = -1;
        if (TextUtils.isEmpty(labelContent)) {
            return labelId;
        }
        for (int i = 0, len = getLabelList().size(); i < len; i++) {
            LabelHolder labelHolder = getLabelList().get(i);
            if (labelContent.equals(labelHolder.mContent)) {
                labelId = labelHolder.mId;
                break;
            }
        }
        return labelId;
    }

    public String getLabelContent(int id) {
        ArrayList<LabelManager.LabelHolder> labels = getLabelList();
        for (LabelManager.LabelHolder holder : labels) {
            if (holder.mId == id) {
                return holder.mContent;
            }
        }
        return null;
    }

    public void removeLabelById(int id) {
        String selection = LabelContract.LabelContent._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        mApp.getContentResolver().delete(mLabelUri, selection, selectionArgs);
        removeFromCache(id);
    }

    private void removeFromCache(int id) {
        synchronized (mLabelCache) {
            ArrayList<LabelHolder> labels = mLabelCache;
            LabelHolder find = null;
            for (LabelHolder holder : labels) {
                if (holder.mId == id) {
                    find = holder;
                    break;
                }
            }
            if (find != null) {
                labels.remove(find);
            }
        }
        notifyListener();
    }

    public static boolean getIsInitLabel(Context context) {
        return NoteShareDataManager.getIsInitLabel(context);
    }

    public static void setIsInitLabel(Context context, boolean init) {
        NoteShareDataManager.setIsInitLabel(context, init);
    }

    public static class LabelHolder {
        public int mId;
        public String mContent;

        public LabelHolder(int id, String content) {
            mId = id;
            mContent = content;
        }
    }
}
