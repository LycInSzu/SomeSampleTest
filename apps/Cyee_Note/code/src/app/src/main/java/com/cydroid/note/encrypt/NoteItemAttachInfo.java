package com.cydroid.note.encrypt;

/**
 * Created by spc on 16-5-24.
 */
public class NoteItemAttachInfo {

    private String mOriginPicPath;
    private String mThumbPicPath;
    private String mSoundPath;

    public NoteItemAttachInfo() {
    }

    public String getOriginPicPath() {
        return mOriginPicPath;
    }

    public String getThumbPicPath() {
        return mThumbPicPath;
    }

    public String getSoundPath() {
        return mSoundPath;
    }

    public void setOriginPicPath(String originPicPath) {
        this.mOriginPicPath = originPicPath;
    }

    public void setThumbPicPath(String thumbPicPath) {
        this.mThumbPicPath = thumbPicPath;
    }

    public void setSoundPath(String soundPath) {
        this.mSoundPath = soundPath;
    }
}
