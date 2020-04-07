package com.cydroid.note.encrypt;

/**
 * Created by spc on 16-6-15.
 */
public interface NoteActionProgressListener {

    public void onStart(int count);

    public void onOneComplete();

    public void onAllComplete(boolean isEncrypt);
}
