package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.provider.NoteContract;

/**
 * Created by spc on 16-4-20.
 */
public class LocalLabelNoteSet extends LabelNoteSet {

    public LocalLabelNoteSet(Path path, NoteAppImpl application) {
        super(path, application);
    }

    @Override
    protected void initDataSource() {
        mBaseSet = NoteAppImpl.getContext().getDataManager().getMediaSet(LocalSource.LOCAL_SET_PATH);
        mBaseUri = NoteContract.NoteContent.CONTENT_URI;
        mNotifier = new ChangeNotifier(this, mBaseUri, NoteAppImpl.getContext()); //NOSONAR
    }
}
