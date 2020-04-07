package com.cydroid.note.data;

import java.util.ArrayList;

public class NoteInfo {
    public volatile int mId = NoteItem.INVALID_ID;
    public ArrayList<Integer> mLabel = new ArrayList<>();
    public long mDateReminderInMs = NoteItem.INVALID_REMINDER;

    public String mTitle;
    public String mContent;
    public long mDateCreatedInMs;
    public long mDateModifiedInMs;
    public int mEncyptHintState;
    public int mEncrytRemindReadState;

    public NoteInfo() {
    }
}
