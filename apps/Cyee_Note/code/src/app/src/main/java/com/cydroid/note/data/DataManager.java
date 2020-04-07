package com.cydroid.note.data;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import com.cydroid.note.common.Log;

import com.gionee.framework.log.Logger;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.trash.data.TrashSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;


public class DataManager {
    private static final String TAG = "DataManager";
    public static final Object LOCK = new Object();

    private NoteAppImpl mApplication;
    private final Handler mDefaultMainHandler;
    private HashMap<Uri, NotifyBroker> mNotifierMap = new HashMap<>();
    private HashMap<String, NoteSource> mSourceMap = new LinkedHashMap<>();

    public DataManager(NoteAppImpl application) {
        mApplication = application;
        mDefaultMainHandler = new Handler(application.getMainLooper());
    }

    public synchronized void initializeSourceMap() {
        if (!mSourceMap.isEmpty()) return;

        // the order matters, the UriSource must come last
        addSource(new LocalSource(mApplication));
        addSource(new KeySource(mApplication));
        addSource(new LabelSource(mApplication));
        addSource(new TrashSource(mApplication));
    }

    // open for debug
    void addSource(NoteSource source) {
        if (source == null) return;
        mSourceMap.put(source.getPrefix(), source);
    }

    // A common usage of this method is:
    // synchronized (DataManager.LOCK) {
    //     MediaObject object = peekNoteObject(path);
    //     if (object == null) {
    //         object = createNoteObject(...);
    //     }
    // }
    public NoteObject peekNoteObject(Path path) {
        return path.getObject();
    }

    public NoteObject getMediaObject(Path path) {
        synchronized (LOCK) {
            NoteObject obj = path.getObject();
            if (obj != null) return obj;

            NoteSource source = mSourceMap.get(path.getPrefix());
            if (source == null) {
                Logger.printLog(TAG, "cannot find note source for path: " + path);
                return null;
            }

            try {
                NoteObject object = source.createMediaObject(path);
                if (object == null) {
                    Log.w(TAG, "cannot create note object: " + path);
                }
                return object;
            } catch (Throwable t) {
                Logger.printLog(TAG, "exception in creating note object: " + path + ",,,t,,," + t);
                return null;
            }
        }
    }

    public NoteObject getMediaObject(String s) {
        return getMediaObject(Path.fromString(s));
    }

    public NoteSet getMediaSet(Path path) {
        return (NoteSet) getMediaObject(path);
    }

    public NoteSet getMediaSet(String s) {
        return (NoteSet) getMediaObject(s);
    }

    public NoteSet[] getMediaSetsFromString(String segment) {
        String[] seq = Path.splitSequence(segment);
        int n = seq.length;
        NoteSet[] sets = new NoteSet[n];
        for (int i = 0; i < n; i++) {
            sets[i] = getMediaSet(seq[i]);
        }
        return sets;
    }

    public void delete(Path path) throws Exception {
        getMediaObject(path).delete();
    }

    public Uri getContentUri(Path path) {
        return getMediaObject(path).getContentUri();
    }

    public void registerChangeNotifier(Uri uri, ChangeNotifier notifier) {
        NotifyBroker broker = null;
        synchronized (mNotifierMap) {
            broker = mNotifierMap.get(uri);
            if (broker == null) {
                broker = new NotifyBroker(mDefaultMainHandler);
                mApplication.getContentResolver().registerContentObserver(uri, true, broker);
                mNotifierMap.put(uri, broker);
            }
        }
        broker.registerNotifier(notifier);
    }

    public void blockChangeNotifier(boolean block) {
        synchronized (mNotifierMap) {
            for (NotifyBroker broker : mNotifierMap.values()) {
                broker.block(block);
            }
        }
    }

    private static class NotifyBroker extends ContentObserver {
        private boolean mBlock;
        private boolean mContentChanged;
        private WeakHashMap<ChangeNotifier, Object> mNotifiers = new WeakHashMap<>();

        public NotifyBroker(Handler handler) {
            super(handler);
        }

        public synchronized void registerNotifier(ChangeNotifier notifier) {
            mNotifiers.put(notifier, null);
        }

        public void block(boolean block) {
            if (block) {
                mBlock = true;
                mContentChanged = false;
            } else {
                mBlock = false;
                if (mContentChanged) {
                    mContentChanged = false;
                    onChange(false);
                }
            }
        }

        @Override
        public synchronized void onChange(boolean selfChange) {
            if (mBlock) {
                mContentChanged = true;
                return;
            }
            for (ChangeNotifier notifier : mNotifiers.keySet()) {
                notifier.onChange(selfChange);
            }
        }
    }
}
