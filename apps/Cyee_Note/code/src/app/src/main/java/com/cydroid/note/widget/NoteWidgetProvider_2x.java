package com.cydroid.note.widget;

import com.cydroid.note.R;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.data.NoteItem;

public class NoteWidgetProvider_2x extends NoteWidgetProvider {

    private static NoteWidgetProvider_2x sNoteWidgetProvide;

    public static NoteWidgetProvider_2x getInstance() {
        if (null == sNoteWidgetProvide) {
            sNoteWidgetProvide = new NoteWidgetProvider_2x();
        }
        return sNoteWidgetProvide;
    }

    @Override
    protected int getWidgetType() {
        return NoteWidgetProvider.WIDGET_TYPE_2X;
    }

    @Override
    protected int getRemoteViewLayoutId(int mediaType) {
         //GIONEE wanghaiyan 2017-02-06 modify for 67596 begin
        //return R.layout.widget_2x;
        return mediaType == NoteItem.MEDIA_TYPE_IMAGE ? R.layout.widget_2x_image : R.layout.widget_2x_no_image;
    }

   
    @Override
    //protected boolean shouldDisplayImage(SlidingWindow.NoteEntry entry) {
    //   return false;
    // }
    protected boolean shouldDisplayImage(SlidingWindow.NoteEntry entry) {
        return (null != entry && entry.mediaType == NoteItem.MEDIA_TYPE_IMAGE);
    }
    //GIONEE wanghaiyan 2017-02-06 modify for 67596 end
    @Override
    protected int[] getBackgroundBitmapSize() {
        return getBackgroundBitmapSize(R.dimen.widget_bg_widht_2x, R.dimen.widget_bg_height_2x);
    }

}
