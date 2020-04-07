package com.cydroid.note.data;

import com.cydroid.note.common.Log;

import java.util.ArrayList;

public abstract class NoteSource {
    private static final String TAG = "NoteSource";
    private String mPrefix;

    protected NoteSource(String prefix) {
        mPrefix = prefix;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public abstract NoteObject createMediaObject(Path path);


    // Maps a list of Paths (all belong to this NoteSource) to MediaItems,
    // and invoke consumer.consume() for each MediaItem with the given id.
    //
    // This default implementation uses getMediaObject for each Path. Subclasses
    // may override this and provide more efficient implementation (like
    // batching the database query).
    public void mapMediaItems(ArrayList<PathId> list, NoteSet.ItemConsumer consumer) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = list.get(i);
            NoteObject obj;
            synchronized (DataManager.LOCK) {
                obj = pid.path.getObject();
                if (obj == null) {
                    try {
                        obj = createMediaObject(pid.path);
                    } catch (Throwable th) {
                        Log.d(TAG, "cannot create media object: " + pid.path + ",,th,," + th);
                    }
                }
            }
            if (obj != null) {
                consumer.consume(pid.id, (NoteItem) obj);
            }
        }
    }

    public static class PathId {
        public Path path;
        public int id;

        public PathId(Path path, int id) {
            this.path = path;
            this.id = id;
        }
    }
}
