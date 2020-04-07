package com.cydroid.note.widget;

import com.cydroid.note.R;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.data.NoteItem;

public class NoteWidgetProvider_4x extends NoteWidgetProvider {

    private static NoteWidgetProvider_4x sNoteWidgetProvide;

    public static NoteWidgetProvider_4x getInstance() {
        if (null == sNoteWidgetProvide) {
            sNoteWidgetProvide = new NoteWidgetProvider_4x();
        }
        return sNoteWidgetProvide;
    }

    @Override
    protected int getWidgetType() {
        return NoteWidgetProvider.WIDGET_TYPE_4X;
    }

    @Override
    protected int getRemoteViewLayoutId(int mediaType) {
        return mediaType == NoteItem.MEDIA_TYPE_IMAGE ? R.layout.widget_4x_image : R.layout.widget_4x_no_image;
    }

    @Override
    protected boolean shouldDisplayImage(SlidingWindow.NoteEntry entry) {
        return (null != entry && entry.mediaType == NoteItem.MEDIA_TYPE_IMAGE);
    }

    @Override
    protected int[] getBackgroundBitmapSize() {
        return getBackgroundBitmapSize(R.dimen.widget_bg_widht_image_4x,
                R.dimen.widget_bg_height_image_4x);
    }


}
