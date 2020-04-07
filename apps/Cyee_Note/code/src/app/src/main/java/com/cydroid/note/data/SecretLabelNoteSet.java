package com.cydroid.note.data;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.provider.NoteContract;

/**
 * Created by spc on 16-4-20.
 */
public class SecretLabelNoteSet extends LabelNoteSet{

    public SecretLabelNoteSet(Path path, NoteAppImpl application) {
        super(path, application);
    }

    @Override
    protected void initDataSource() {
        mBaseSet = NoteAppImpl.getContext().getDataManager().getMediaSet(LocalSource.LOCAL_SECRET_SET_PATH);
        mBaseUri = NoteContract.NoteContent.SECRET_CONTENT_URI;
        mNotifier = new ChangeNotifier(this, mBaseUri, NoteAppImpl.getContext());//NOSONAR
    }
}
