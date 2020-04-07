package com.cydroid.note.data;

import android.net.Uri;

import com.cydroid.note.encrypt.NoteActionProgressListener;

public class NoteObject {
    public static final long INVALID_DATA_VERSION = -1;
    private static long sVersionSerial = 0;
    protected final Path mPath;
    protected long mDataVersion;

    public NoteObject(Path path, long version) {
        path.setObject(this);
        mPath = path;
        mDataVersion = version;
    }

    public static synchronized long nextVersionNumber() {
        return ++NoteObject.sVersionSerial;
    }

    public Path getPath() {
        return mPath;
    }

    public void delete() throws Exception {
        throw new UnsupportedOperationException();
    }

    public void encrypt(NoteActionProgressListener listener)throws Exception {
        throw new UnsupportedOperationException();
    }

    public void decrypt(NoteActionProgressListener listener)throws Exception {
        throw new UnsupportedOperationException();
    }

    public void rotate(int degrees) {
        throw new UnsupportedOperationException();
    }

    public Uri getContentUri() {
        throw new UnsupportedOperationException();
    }

    public Uri getPlayUri() {
        throw new UnsupportedOperationException();
    }

    public synchronized long getDataVersion() {
        return mDataVersion;
    }
}
