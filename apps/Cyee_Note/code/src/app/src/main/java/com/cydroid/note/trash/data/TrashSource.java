package com.cydroid.note.trash.data;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.data.NoteObject;
import com.cydroid.note.data.NoteSource;
import com.cydroid.note.data.Path;
import com.cydroid.note.data.PathMatcher;

/**
 * Created by xiaozhilong on 7/1/16.
 */
public class TrashSource extends NoteSource {

    public static final String TRASH_SET_PATH = "/trash/note";
    public static final String TRASH_ITEM_PATH = "/trash/note/item";

    private static final int TRASH_NOTE_SET = 1;
    private static final int TRASH_NOTE_ITEM = 2;

    private NoteAppImpl mApplication;
    private PathMatcher mMatcher;

    public TrashSource(NoteAppImpl context) {
        super("trash");
        mApplication = context;
        mMatcher = new PathMatcher();
        mMatcher.add(TRASH_SET_PATH, TRASH_NOTE_SET);
        mMatcher.add(TRASH_ITEM_PATH + "/*", TRASH_NOTE_ITEM);
    }

    @Override
    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = mApplication;
        switch (mMatcher.match(path)) {
            case TRASH_NOTE_SET:
                return new TrashNoteSet(path, app);
            case TRASH_NOTE_ITEM:
                return new TrashNoteItem(path, mApplication, mMatcher.getIntVar(0));
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
