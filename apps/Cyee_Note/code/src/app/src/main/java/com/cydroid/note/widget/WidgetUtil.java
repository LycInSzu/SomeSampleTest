package com.cydroid.note.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;

import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.LocalNoteSet;
import com.cydroid.note.data.NoteParser;
import com.cydroid.note.provider.NoteContract;

import java.text.SimpleDateFormat;

public class WidgetUtil {

    public static void updateWidget(String title, String jsonContent, int noteId, long modifiedTime,
                                    long reminderInMs) {
        SlidingWindow.NoteEntry entry = new SlidingWindow.NoteEntry();
        entry.title = title;
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
		if(NoteUtils.gnIPFlag){
			entry.time = NoteUtils.formatDateTime(modifiedTime, new SimpleDateFormat(NoteParser.DATE_FORMAT_IP));
		}else{
			entry.time = NoteUtils.formatDateTime(modifiedTime, new SimpleDateFormat(NoteParser.DATE_FORMAT));
		}
		//GIONEE wanghaiyan 2017-3-2 modify for 77724 end
        entry.reminder = reminderInMs;
        entry.id = noteId;
        entry.timeMillis = modifiedTime;
        NoteParser parser = new NoteParser();
        parser.parseNoteContent(jsonContent, entry);
        WidgetUtil.updateAllWidgets(entry);
    }


    public static void updateAllWidgets() {
        Context context = NoteAppImpl.getContext();
        int[] appWidgetIds2x = getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_2x.class));
        updateAllWidgetsFromDb(context, appWidgetIds2x, NoteWidgetProvider.WIDGET_TYPE_2X);
        int[] appWidgetIds4x = getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_4x.class));
        updateAllWidgetsFromDb(context, appWidgetIds4x, NoteWidgetProvider.WIDGET_TYPE_4X);
    }

    public static void updateOneWidget(int widgetId, int widgetType, SlidingWindow.NoteEntry entry) {
        NoteWidgetProvider widgetProvide = null;
        if (NoteWidgetProvider.WIDGET_TYPE_2X == widgetType) {
            widgetProvide = NoteWidgetProvider_2x.getInstance();
        } else {
            widgetProvide = NoteWidgetProvider_4x.getInstance();
        }
        widgetProvide.update(widgetId, entry);
    }

    public static void updateAllWidgets(SlidingWindow.NoteEntry entry) {
        Context context = NoteAppImpl.getContext();
        int[] appWidgetIds2x = getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_2x.class));
        updateAllWidgets(appWidgetIds2x, NoteWidgetProvider.WIDGET_TYPE_2X, entry);
        int[] appWidgetIds4x = getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_4x.class));
        updateAllWidgets(appWidgetIds4x, NoteWidgetProvider.WIDGET_TYPE_4X, entry);
    }

    private static void updateAllWidgets(int[] appWidgetIds, int widgetType,
                                         SlidingWindow.NoteEntry entry) {
        if (0 == appWidgetIds.length) {
            return;
        }
        for (int i = 0, len = appWidgetIds.length; i < len; i++) {
            updateOneWidget(appWidgetIds[i], widgetType, entry);
        }
    }

    private static void updateAllWidgetsFromDb(final Context context, final int[] appWidgetIds,
                                               final int widgetType) {
        if (0 == appWidgetIds.length) {
            return;
        }
        if (appWidgetIds.length > 0) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    SlidingWindow.NoteEntry entry = getLatestNoteEntry(context);
                    for (int i = 0, len = appWidgetIds.length; i < len; i++) {
                        updateOneWidget(appWidgetIds[i], widgetType, entry);
                    }
                }
            };
            new Thread(runnable).start();
        }
    }

    public static int[] getAppwidgetIds(ComponentName componentName) {
        return AppWidgetManager.getInstance(NoteAppImpl.getContext()).getAppWidgetIds(componentName);
    }

    public static SlidingWindow.NoteEntry getLatestNoteEntry(Context context) {
        Cursor cursor = context.getContentResolver().query(NoteContract.NoteContent.CONTENT_URI,
                LocalNoteSet.NOTE_PROJECTION, null, null, NoteContract.NoteContent.COLUMN_DATE_MODIFIED + " DESC");
        if (null == cursor) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(LocalNoteItem.INDEX_ID);
                String title = cursor.getString(LocalNoteItem.INDEX_TITLE);
                String content = cursor.getString(LocalNoteItem.INDEX_CONTENT);
                long dateModifiedInMs = cursor.getLong(LocalNoteItem.INDEX_DATE_MODIFIED);
                long dateReminderInMs = cursor.getLong(LocalNoteItem.INDEX_REMINDER);
                SlidingWindow.NoteEntry entry = new SlidingWindow.NoteEntry();
                entry.title = title;
				//GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
				if(NoteUtils.gnIPFlag){
					entry.time = NoteUtils.formatDateTime(dateModifiedInMs, new SimpleDateFormat(NoteParser.DATE_FORMAT_IP));
				}else{
					entry.time = NoteUtils.formatDateTime(dateModifiedInMs, new SimpleDateFormat(NoteParser.DATE_FORMAT));
				}
				//GIONEE wanghaiyan 2017-3-2 modify for 77724 end
                entry.reminder = dateReminderInMs;
                entry.id = id;
                entry.timeMillis = dateModifiedInMs;
                NoteParser noteParse = new NoteParser();
                noteParse.parseNoteContent(content, entry);
                return entry;
            }
        } finally {
            NoteUtils.closeSilently(cursor);
        }
        return null;
    }
}
