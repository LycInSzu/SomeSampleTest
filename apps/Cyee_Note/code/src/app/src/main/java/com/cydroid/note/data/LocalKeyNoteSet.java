package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.provider.NoteContract;

/**
 * Created by spc on 16-4-21.
 */
public class LocalKeyNoteSet extends KeyNoteSet{
    public LocalKeyNoteSet(Path path, NoteAppImpl application) {
        super(path, application);
    }

    @Override
    public synchronized void initDataSource(NoteAppImpl application) {
        mBaseUri = NoteContract.NoteContent.CONTENT_URI;
        mBaseSet = application.getDataManager().getMediaSet(LocalSource.LOCAL_SET_PATH);
        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }
}
