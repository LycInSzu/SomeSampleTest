package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;

public class LocalSource extends NoteSource {
    public static final String LOCAL_SET_PATH = "/local/note";
    public static final String LOCAL_ITEM_PATH = "/local/note/item";
    public static final String LOCAL_SECRET_SET_PATH = "/local/secret/";
    public static final String LOCAL_SECRET_ITEM_PATH = "/local/secret/item";
    private static final int LOCAL_NOTE_SET = 1;
    private static final int LOCAL_NOTE_ITEM = 2;
    private static final int LOCAL_SECRET_SET = 3;
    private static final int LOCAL_SECRET_ITEM = 4;

    private NoteAppImpl mApplication;
    private PathMatcher mMatcher;

    public LocalSource(NoteAppImpl context) {
        super("local");
        mApplication = context;
        mMatcher = new PathMatcher();
        mMatcher.add(LOCAL_SET_PATH, LOCAL_NOTE_SET);
        mMatcher.add(LOCAL_ITEM_PATH + "/*", LOCAL_NOTE_ITEM);
        mMatcher.add(LOCAL_SECRET_SET_PATH, LOCAL_SECRET_SET);
        mMatcher.add(LOCAL_SECRET_ITEM_PATH + "/*", LOCAL_SECRET_ITEM);
    }

    @Override
    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = mApplication;
        switch (mMatcher.match(path)) {
            case LOCAL_NOTE_SET:
                return new LocalNoteSet(path, app);
            case LOCAL_NOTE_ITEM:
                return new LocalNoteItem(path, mApplication, mMatcher.getIntVar(0));
            case LOCAL_SECRET_SET:
                return new SecretNoteSet(path, app);
            case LOCAL_SECRET_ITEM:
                return new SecretNoteItem(path, mApplication, mMatcher.getIntVar(0));
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
