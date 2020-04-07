package com.cydroid.note.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.cydroid.note.R;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.NewNoteActivity;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.app.effect.DrawableManager;
import com.cydroid.note.app.effect.EffectUtil;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.NoteItem;

public abstract class NoteWidgetProvider extends AppWidgetProvider {

    public static final int WIDGET_TYPE_2X = 1;
    public static final int WIDGET_TYPE_4X = 2;

    private Handler mMainHandler;
    private Handler mThreadHandle;
    private Object mLock = new Object();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0, len = appWidgetIds.length; i < len; i++) {
            final int widgetId = appWidgetIds[i];
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final SlidingWindow.NoteEntry entry = WidgetUtil.getLatestNoteEntry(NoteAppImpl.getContext());
                    getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            update(widgetId, entry);
                        }
                    });
                }
            };
            getThreadHandle().post(runnable);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void update(int widgetId, SlidingWindow.NoteEntry entry) {
        if (null == entry) {
            updateDefaultWidget(widgetId);
        } else {
            updateWidget(widgetId, entry);
        }
    }

    public void updateDefaultWidget(int appWidgetId) {
        Context context = NoteAppImpl.getContext();
        RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                getRemoteViewLayoutId(NoteItem.MEDIA_TYPE_NONE));
        setTitleAndAlarmState(remoteView, null);
        remoteView.setTextViewText(R.id.widget_content, "");
        remoteView.setTextViewText(R.id.widget_time, "");
        setWidgetBackground(remoteView, System.currentTimeMillis());

        PendingIntent pendingIntent = getPendingIntent(appWidgetId, NoteItem.INVALID_ID);
        remoteView.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteView);
    }

    private void setTitleAndAlarmState(RemoteViews remoteView, SlidingWindow.NoteEntry entry) {
        if (shouldHideTitleLayout(entry)) {
            remoteView.setViewVisibility(R.id.widget_title, View.GONE);
            remoteView.setViewVisibility(R.id.widget_alarm, View.GONE);
            remoteView.setViewVisibility(R.id.title_layout, View.GONE);
        } else {
            remoteView.setViewVisibility(R.id.title_layout, View.VISIBLE);
            remoteView.setViewVisibility(R.id.widget_title, View.VISIBLE);
            remoteView.setViewVisibility(R.id.widget_alarm,
                    NoteItem.INVALID_REMINDER == entry.reminder ? View.GONE : View.VISIBLE);
            remoteView.setImageViewResource(R.id.widget_alarm, R.drawable.note_item_reminder);
            remoteView.setTextViewText(R.id.widget_title, entry.title);
        }
    }

    private boolean shouldHideTitleLayout(SlidingWindow.NoteEntry entry) {
        return (null == entry)
                || (TextUtils.isEmpty(entry.title) && NoteItem.INVALID_REMINDER == entry.reminder);
    }

    protected abstract int getWidgetType();

    protected abstract int getRemoteViewLayoutId(int mediaType);

    protected abstract boolean shouldDisplayImage(SlidingWindow.NoteEntry entry);

    protected abstract int[] getBackgroundBitmapSize();

    protected Bitmap getDisplayBitmap(Context context, SlidingWindow.NoteEntry entry) {
        int width = Config.WidgetPage.getInstance(context).mWidth;
        int height = Config.WidgetPage.getInstance(context).mHeight;
        ThumbnailDecodeProcess decodeProcess = new ThumbnailDecodeProcess(context, entry.thumbnailUri,
                width, height, ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT, false);
        Bitmap bitmap = decodeProcess.getThumbnail();
        if (null == bitmap) {
            decodeProcess = new ThumbnailDecodeProcess(context, entry.originUri,
                    width, height, ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT, false);
            bitmap = decodeProcess.getThumbnail();
        }
        return bitmap;
    }

    protected PendingIntent getPendingIntent(int appWidgetId, int noteId) {
        Intent intent = new Intent();
        intent.setClass(NoteAppImpl.getContext(), NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, "/" + noteId);
        return PendingIntent.getActivity(NoteAppImpl.getContext(), appWidgetId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void updateWidget(final int appWidgetId, final SlidingWindow.NoteEntry entry) {
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                Context context = NoteAppImpl.getContext();
                RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                        getRemoteViewLayoutId(entry.mediaType));
                setTitleAndAlarmState(remoteView, entry);

                remoteView.setTextViewText(R.id.widget_content, entry.content);
                //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 begin
                String newDate = NoteUtils.formateTime(entry.time,NoteAppImpl.getContext());
                remoteView.setTextViewText(R.id.widget_time, newDate);
                //remoteView.setTextViewText(R.id.widget_time, entry.time);
                //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 end
                setWidgetBackground(remoteView, entry.timeMillis);
                if (shouldDisplayImage(entry)) {
                    Bitmap bitmap = getDisplayBitmap(context, entry);
                    if (null != bitmap) {
                        remoteView.setImageViewBitmap(R.id.widget_photo, bitmap);
                    } else {
                        remoteView.setImageViewResource(R.id.widget_photo, R.drawable.image_span_default_drawable);
                    }
                }
                PendingIntent pendingIntent = getPendingIntent(appWidgetId, entry.id);
                remoteView.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteView);
            }
        };
        getThreadHandle().post(updateRunnable);
    }

    protected final void setWidgetBackground(RemoteViews remoteViews, long noteModifiedTime) {
        EffectUtil util = new EffectUtil(System.currentTimeMillis());
        int effect = util.getEffect(noteModifiedTime);
        int[] size = getBackgroundBitmapSize();
        Bitmap bitmap = DrawableManager.getWidgetEffectBitmap(NoteAppImpl.getContext(),
                getWidgetType(), effect, size[0], size[1]);
        remoteViews.setImageViewBitmap(R.id.widget_backgroud, bitmap);
    }

    protected int[] getBackgroundBitmapSize(int widthResId, int heightResId) {
        int[] size = new int[2];
        int width = NoteAppImpl.getContext().getResources().getDimensionPixelOffset
                (widthResId);
        int height = NoteAppImpl.getContext().getResources().getDimensionPixelOffset
                (heightResId);
        size[0] = width;
        size[1] = height;
        return size;
    }

    protected final Handler getThreadHandle() {
        synchronized (mLock) {
            if (null == mThreadHandle) {
                Looper looper = (NoteAppImpl.getContext()).getNoteBackgroundLooper();
                mThreadHandle = new Handler(looper);
            }
            return mThreadHandle;
        }
    }

    protected final Handler getMainHandler() {
        synchronized (mLock) {
            if (null == mMainHandler) {
                mMainHandler = new Handler(Looper.getMainLooper());
            }
            return mMainHandler;
        }
    }
}