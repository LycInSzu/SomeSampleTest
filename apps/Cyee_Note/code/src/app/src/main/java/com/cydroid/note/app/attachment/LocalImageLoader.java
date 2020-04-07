package com.cydroid.note.app.attachment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gionee.framework.log.Logger;
import com.cydroid.note.app.ImageCache;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.common.ThumbnailDecodeProcess;

import java.lang.ref.WeakReference;

public class LocalImageLoader {

    private static final String TAG = "LocalImageLoader";
    private Bitmap mLoadingBitmap;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private PicLoadErrorListener mPicLoadErrorListener;

    public LocalImageLoader(Context context) {
    }

    public void loadImage(Object data, ImageView imageView,
                          ThumbnailDecodeProcess.ThumbnailDecodeMode thumbnailDecodeMode) {
        if (data == null) {
            return;
        }
        Bitmap value = null;
        String picUri = "";
        if (data instanceof String) {
            picUri = String.valueOf(data);
        } else if (data instanceof SlidingWindow.NoteEntry) {
            picUri = ((SlidingWindow.NoteEntry) data).thumbnailUri.toString();
        }
        value = ImageCache.getInstance().getBitmapFromMemCache(picUri);
        if (value != null) {
            setImageDrawable(imageView, value, thumbnailDecodeMode);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView, thumbnailDecodeMode);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(NoteAppImpl.getContext().getResources(), mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void preLoadImage(String picUrl, int picWith, int picHeight) {
        if (TextUtils.isEmpty(picUrl)) {
            return;
        }
        if (ImageCache.getInstance().getBitmapFromMemCache(picUrl) != null) {
            return;
        }
        PreLoadPicTask preLoadPicTask = new PreLoadPicTask(picUrl, picWith, picHeight);
        NoteAppImpl.getContext().getThreadPool().submit(preLoadPicTask);
    }
    public void setLoadErrorListener(PicLoadErrorListener errorListener) {
        mPicLoadErrorListener = errorListener;
    }

    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(NoteAppImpl.getContext().getResources(), resId);
    }

    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private Object mData;
        private final WeakReference<ImageView> imageViewReference;
        private int mTargetWith;
        private int mTargetHeight;
        private ThumbnailDecodeProcess.ThumbnailDecodeMode mThumbnailDecodeMode;

        public BitmapWorkerTask(Object data, ImageView imageView,
                                ThumbnailDecodeProcess.ThumbnailDecodeMode thumbnailDecodeMode) {
            mData = data;
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            mTargetWith = params.width;
            mTargetHeight = params.height;
            mThumbnailDecodeMode = thumbnailDecodeMode;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String picUri = "";
            if (mData instanceof String) {
                picUri = String.valueOf(mData);
            } else if (mData instanceof SlidingWindow.NoteEntry) {
                picUri = ((SlidingWindow.NoteEntry) mData).thumbnailUri.toString();
            }
            Bitmap bitmap = null;

            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(mData, mTargetWith, mTargetHeight, mThumbnailDecodeMode);
            }
            if(bitmap == null && mPicLoadErrorListener != null) {
                mPicLoadErrorListener.onLoadError(picUri);
            }

            if (bitmap != null) {
                ImageCache.getInstance().addBitmapToCache(picUri, bitmap);
            }else {
                Logger.printLog(TAG, "cache bitmap null");
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap value) {
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                setImageDrawable(imageView, value, mThumbnailDecodeMode);
            }
        }

        @Override
        protected void onCancelled(Bitmap value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }

        private Bitmap processBitmap(Object data, int targetWith, int targetHeight,
                                     ThumbnailDecodeProcess.ThumbnailDecodeMode thumbnailDecodeMode) {
            Bitmap bitmap = null;
            if (data instanceof String) {
                Uri uri = Uri.parse(String.valueOf(data));
                ThumbnailDecodeProcess decodeProcess = new ThumbnailDecodeProcess(NoteAppImpl.getContext(),
                        uri, targetWith, targetHeight, thumbnailDecodeMode, false);
                bitmap = decodeProcess.getThumbnail();
            } else if (data instanceof SlidingWindow.NoteEntry) {
                SlidingWindow.NoteEntry entry = (SlidingWindow.NoteEntry) data;
                bitmap = entry.item.requestImage(entry.mediaType, entry.thumbnailUri);
                if (null == bitmap) {
                    bitmap = entry.item.requestImage(entry.mediaType, entry.originUri);
                }
            }
            return bitmap;
        }
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private void setImageDrawable(ImageView imageView, Bitmap bitmap,
                                  ThumbnailDecodeProcess.ThumbnailDecodeMode thumbnailDecodeMode) {
        if (thumbnailDecodeMode != ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = bitmap.getWidth();
            imageView.setLayoutParams(params);
        }
        if (bitmap == null) {
            imageView.setImageBitmap(mLoadingBitmap);
        }else {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    private static class PreLoadPicTask implements ThreadPool.Job {
        String picUri;
        int picWith;
        int picHeight;

        PreLoadPicTask(String picUri, int picWith, int picHeight) {
            this.picUri = picUri;
            this.picWith = picWith;
            this.picHeight = picHeight;
        }

        @Override
        public Object run(ThreadPool.JobContext jc) {
            ThumbnailDecodeProcess imageLoader = new ThumbnailDecodeProcess(NoteAppImpl.getContext(),
                    Uri.parse(picUri),
                    picWith, picHeight, ThumbnailDecodeProcess.ThumbnailDecodeMode.HEIGHT_FIXED_WIDTH_SCALE,
                    false);
            Bitmap bitmap = imageLoader.getThumbnail();
            if (null != bitmap) {
                ImageCache.getInstance().addBitmapToCache(picUri, bitmap);
            }
            return null;
        }
    }
    public interface PicLoadErrorListener {
        public void onLoadError(String picUrl);
    }
}
