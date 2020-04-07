package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;

public class LabelSource extends NoteSource {
    public static final String LABEL_SET_PATH = "/label/note";
    public static final String SECRET_LABEL_SET_PATH = "/label/secret/note";
    private static final int LABEL_NOTE_SET = 1;
    private static final int SECRET_LABEL_NOTE_SET = 2;

    private NoteAppImpl mApplication;
    private PathMatcher mMatcher;

    public LabelSource(NoteAppImpl context) {
        super("label");
        mApplication = context;
        mMatcher = new PathMatcher();
        mMatcher.add(LABEL_SET_PATH, LABEL_NOTE_SET);
        mMatcher.add(SECRET_LABEL_SET_PATH, SECRET_LABEL_NOTE_SET);
    }

    @Override
    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = mApplication;
        switch (mMatcher.match(path)) {
            case LABEL_NOTE_SET:
                return new LocalLabelNoteSet(path, app);
            case SECRET_LABEL_NOTE_SET:
                return new SecretLabelNoteSet(path, app);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
