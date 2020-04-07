package com.cydroid.note.app.inputbackup;


import com.cydroid.note.app.dataupgrade.DataUpgrade;
import com.cydroid.note.common.NoteUtils;

import java.util.ArrayList;

class OldNoteInfo {
    private long mId;
    private String mNoteTitle;
    private String mContent;
    private String mLabel;
    private String mLabelId;
    private ArrayList<SubInfo> mSubs;

    public OldNoteInfo(long id, String noteTitle, String content, String label) {
        mId = id;
        mNoteTitle = noteTitle;
        mContent = content;
        mLabel = label;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mNoteTitle;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getLabelId() {
        return mLabelId;
    }

    public void setLabelId(String labelId) {
        mLabelId = labelId;
    }

    public ArrayList<SubInfo> getSubInfos() {
        return mSubs;
    }

    public String getContent() {
        return mContent;
    }

    public void resolveMedia() {
        ArrayList<SubInfo> subInfos = getSubInfos(mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subInfos != null) {
            for (SubInfo subData : subInfos) {
                if (subData.isMedia()) {
                    subData.resolveMedia();
                }
            }
            mSubs = subInfos;
        }
    }

    private ArrayList<SubInfo> getSubInfos(String content, String prefix, String suffix) {

        ArrayList<Integer> prefixs = NoteUtils.indexofs(content, prefix);
        if (prefixs == null) {
            return null;
        }
        ArrayList<Integer> suffixs = NoteUtils.indexofs(content, suffix);
        if (suffixs == null) {
            return null;
        }
        if (prefixs.size() != suffixs.size()) {
            return null;
        }
        final int prefixLength = prefix.length();
        final int suffixLength = suffix.length();

        ArrayList<SubInfo> subInfos = new ArrayList();
        int ps = 0;
        int pe;
        for (int i = 0, size = prefixs.size(); i < size; i++) {
            int start = prefixs.get(i);
            pe = start;
            if (pe > 0) {
                subInfos.add(new SubInfo((content.substring(ps, pe)), false));
            }
            ps = start;
            int end = suffixs.get(i) + suffixLength;
            pe = end;
            subInfos.add(new SubInfo((content.substring(ps + prefixLength, pe - suffixLength)), true));
            ps = end;
        }
        int length = content.length();
        if (ps < length) {
            subInfos.add(new SubInfo((content.substring(ps, length)), false));
        }
        return subInfos;
    }
}
