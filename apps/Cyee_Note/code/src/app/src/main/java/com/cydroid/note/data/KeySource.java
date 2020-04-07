package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;

public class KeySource extends NoteSource {
    public static final String KEY_SET_PATH = "/key/note";
    public static final String KEY_SECRET_SET_PATH = "/key/secret/note";
    private static final int KEY_NOTE_SET = 1;
    private static final int KEY_SECRET_SET = 2;

    private NoteAppImpl mApplication;
    private PathMatcher mMatcher;

    public KeySource(NoteAppImpl context) {
        super("key");
        mApplication = context;
        mMatcher = new PathMatcher();
        mMatcher.add(KEY_SET_PATH, KEY_NOTE_SET);
        mMatcher.add(KEY_SECRET_SET_PATH, KEY_SECRET_SET);
    }

    @Override
    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = mApplication;
        switch (mMatcher.match(path)) {
            case KEY_NOTE_SET:
                return new LocalKeyNoteSet(path, app);
            case KEY_SECRET_SET:
                return new SecretKeyNoteSet(path, app);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
