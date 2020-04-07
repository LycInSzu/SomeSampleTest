package com.cydroid.note.app.span;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.gionee.framework.log.Logger;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.DecodeUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.photoview.PhotoViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class PhotoImageSpan extends ReplacementSpan implements AbstractClickSpan, JsonableSpan, OnlyImageSpan {
    private static final boolean DEBUG = false;
    private static final String TAG = "PhotoImageSpan";
    public static final String ORIGIN_URI = "origin_uri";
    public static final String THUMB_URI = "thumb_uri";
    public static final String PIC_WIDTH = "pic_width";
    public static final String PIC_HEIGHT = "pic_height";
    private static final PhotoImageSpan[] EMPTY_ITEM = new PhotoImageSpan[0];
    private SpannableStringBuilder mText;
    private Uri mThumbUri;
    private Uri mOriginUri;
    private Context mContext;
    private ImageSpanWatcher mSpanWatcher;

    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private int mImageShiftSize = 0;
    private boolean mOrignalPicExit = true;
    private Drawable mCacheDrawable;
    private boolean mIsEncrypt;
    private OnImageSpanChangeListener mListener;

    public PhotoImageSpan(Context context, SpannableStringBuilder builder, Uri thumbUri,
                          Uri originUri, boolean isEncrypt, int picWidth, int picHeight) {
        initData(context, builder, thumbUri, originUri, isEncrypt);
        if (picWidth == 0 && picHeight == 0) {
            initPhotoSize(context, originUri);
        } else {
            adjustPhotoSize(context, picWidth, picHeight, thumbUri);
        }
        prepareCacheDrawableAsync();
    }

    public PhotoImageSpan(Context context, SpannableStringBuilder builder, Uri thumbUri,
                          Uri originUri, Bitmap bitmap, boolean isEncrypt) {
        initData(context, builder, thumbUri, originUri, isEncrypt);
        if (null != bitmap) {
            mImageWidth = bitmap.getWidth();
            mImageHeight = bitmap.getHeight();
        }
        mCacheDrawable = new BitmapDrawable(context.getResources(), bitmap);
        mCacheDrawable.setBounds(0, 0, mImageWidth, mImageHeight);
    }

    public void initSpan(int spanStart) {
        setSpanWatcher(spanStart);
    }

    public void setOnImageSpanChangeListener(OnImageSpanChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {
        if (mText != stringBuilder) {
            mText = stringBuilder;
        }
    }

    private void initData(Context context, SpannableStringBuilder builder, Uri thumbUri, Uri originUri,
                          boolean isEncrypt) {
        mContext = context;
        mIsEncrypt = isEncrypt;
        mText = builder;
        mThumbUri = thumbUri;
        mOriginUri = originUri;
        mSpanWatcher = new ImageSpanWatcher();
        mImageShiftSize = Config.EditPage.get(context).mImageShiftSize;
    }

    private void adjustPhotoSize(Context context, int picWidth, int picHeight, Uri originUri) {
        Config.EditPage page = Config.EditPage.get(context);
        setScalePhotoSize(page, picWidth, picHeight, originUri);

    }

    private void setDefalutPhotoSize(Config.EditPage page) {
        mImageWidth = page.mImageWidth;
        mImageHeight = page.mImageHeight;
    }

    private void initPhotoSize(Context context, Uri originUri) {
        Config.EditPage page = Config.EditPage.get(context);
        int[] originSize = DecodeUtils.loadBitmapSize(context, originUri, mIsEncrypt);
        if (EncryptUtil.isInValidSize(originSize)) {
            mOrignalPicExit = false;
            int[] thumbSize = DecodeUtils.loadBitmapSize(context, mThumbUri, mIsEncrypt);
            if (EncryptUtil.isInValidSize(thumbSize)) {
                setDefalutPhotoSize(page);
            } else {
                setScalePhotoSize(page, thumbSize[0], thumbSize[1], mThumbUri);
            }
        } else {
            setScalePhotoSize(page, originSize[0], originSize[1], originUri);
        }
        mImageShiftSize = page.mImageShiftSize;
    }

    private void setScalePhotoSize(Config.EditPage page, int picWidth, int picHeight, Uri originUri) {
        mImageWidth = page.mImageWidth;
        int rotate = DecodeUtils.decodeImageRotate(originUri);
        float origWidth = picWidth;
        float origHeight = picHeight;
        float scale = 0;
        if (rotate == 90 || rotate == 270) {
            scale = origHeight / mImageWidth;
            mImageHeight = (int) ((1 / scale) * origWidth);
        } else {
            scale = origWidth / mImageWidth;
            mImageHeight = (int) ((1 / scale) * origHeight);
        }
    }

    private void prepareCacheDrawableAsync() {
        if (mCacheDrawable != null) {
            return;
        }
        final Handler mainHandler = new Handler(NoteAppImpl.getContext().getMainLooper());
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        threadPool.submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                Bitmap bitmap = decodeThumbBitmapFromCache(NoteAppImpl.getContext(), mThumbUri);
                if (bitmap != null) {
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    if (mOrignalPicExit && (w != mImageWidth || h != mImageHeight)) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }
                if (bitmap == null) {
                    bitmap = decodeThumbBitmapFromOriginFile(NoteAppImpl.getContext(), mImageWidth,
                            mImageHeight, mOriginUri);
                }
                final Drawable drawable;
                if (bitmap != null) {
                    drawable = new BitmapDrawable(NoteAppImpl.getContext().getResources(), bitmap);
                } else {
                    setDefalutPhotoSize(Config.EditPage.get(NoteAppImpl.getContext()));
                    drawable = Config.EditPage.getDefaultImageDrawable(NoteAppImpl.getContext());
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCacheDrawable = drawable;
                        mCacheDrawable.setBounds(0, 0, mImageWidth, mImageHeight);
                        if (mListener != null) {
                            mListener.onImageChanged();
                        }
                    }
                });
                return null;
            }
        });
    }

    private Bitmap decodeThumbBitmapFromCache(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (mIsEncrypt) {
            return DecodeUtils.decodeBitmap(uri.getPath(), mIsEncrypt);
        }

        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String filePath = uri.getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                if (DEBUG) {
                    Logger.printLog(TAG, "decodeThumbBitmapFromCache no exists");
                }
                return null;
            }
            Bitmap bitmap = DecodeUtils.decodeBitmap(filePath, mIsEncrypt);
            if (DEBUG) {
                Logger.printLog(TAG, "decodeThumbBitmapFromCache 1 bitmap = " + bitmap);
            }

            return bitmap;
        }

        Bitmap bitmap = DecodeUtils.decodeBitmap(context, uri);
        if (DEBUG) {
            Logger.printLog(TAG, "decodeThumbBitmapFromCache 2 bitmap = " + bitmap);
        }
        return bitmap;
    }

    private Bitmap decodeThumbBitmapFromOriginFile(Context context, int tw, int th, Uri originUri) {
        if (originUri == null) {
            return null;
        }
        if (ContentResolver.SCHEME_FILE.equals(originUri.getScheme())) {
            String filePath = originUri.getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                if (DEBUG) {
                    Logger.printLog(TAG, "decodeThumbBitmapFromOriginFile no exists");
                }
                return null;
            }
        }
        int rotate = DecodeUtils.decodeImageRotate(originUri);
        Bitmap bitmap = DecodeUtils.decodeThumbnail(context, originUri, tw, th, rotate, true,
                mIsEncrypt);
        if (DEBUG) {
            Logger.printLog(TAG, "decodeThumbBitmapFromOriginFile bitmap = " + bitmap);
        }
        return bitmap;
    }

    public static PhotoImageSpan[] get(SpannableStringBuilder text, int start, int end) {
        PhotoImageSpan[] items = text.getSpans(start, end, PhotoImageSpan.class);
        if (items.length == 1) {
            PhotoImageSpan item = items[0];
            int iStart = text.getSpanStart(item);
            String iText = text.toString();
            if (!iText.startsWith(Constants.MEDIA_PHOTO, iStart)) {
                item.removePhotoImageSpan();
                return EMPTY_ITEM;
            }
        }
        return items;
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (null == mText) {
            return;
        }
        int start = mText.getSpanStart(this);
        if (currSelection == start) {
            int newSel = start + Constants.MEDIA_PHOTO.length();
            Selection.setSelection(mText, newSel);
        }
    }

    private void removePhotoImageSpan() {
       // mText.removeSpan(PhotoImageSpan.this);
       if(mText !=null){
		mText.removeSpan(PhotoImageSpan.this);
       }
    }

    private void setSpanWatcher(int spanStart) {
        mText.setSpan(mSpanWatcher, spanStart, spanStart + Constants.MEDIA_PHOTO.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -mImageHeight;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return mImageWidth;
    }


    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int baseLine, int bottom, Paint paint) {
        int lastLinePos = baseLine - (bottom - top) + mImageShiftSize;
        int nextLinePos = baseLine + mImageShiftSize;
        int transY = lastLinePos + ((nextLinePos - lastLinePos) - mImageHeight) / 2;
        canvas.translate(x, transY);
        if (mCacheDrawable != null) {
            mCacheDrawable.draw(canvas);

        }
        canvas.translate(-x, -transY);
    }

    @Override
    public void onClick(View view) {
        if (mContext == null || (mIsEncrypt)) {
            return;
        }
        Intent intent = new Intent(mContext, PhotoViewActivity.class);
        PhotoImageSpan[] spans = mText.getSpans(0, mText.length(), PhotoImageSpan.class);
        int length = spans.length;
        String[] uriStr = new String[length];
        //TODO: How about curIndex?
        int currentUri = 0;
        for (int i = 0; i < spans.length; i++) {
            uriStr[i] = spans[i].mOriginUri.toString();
            if (mOriginUri == spans[i].mOriginUri) {
                currentUri = i;
            }
        }
        if (DEBUG) {
            Logger.printLog(TAG, "currentUri = " + currentUri + ", image length = " + uriStr.length);
        }

        intent.putExtra("currentImage", currentUri);
        intent.putExtra("imageUris", uriStr);
        mContext.startActivity(intent);
    }

    @Override
    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int minX = paddingLeft + 1;
        int maxX = paddingLeft + mImageWidth - 1;
        int clickX = (int) event.getX();
        int clickY = (int) event.getY();
        return !(clickX < minX || clickX > maxX || clickY >= lineBottom);
    }

    @Override
    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = mText.getSpanStart(this);
        int end = mText.getSpanEnd(this);
        int flags = mText.getSpanFlags(this);

        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, PhotoImageSpan.class.getName());
        jsonObject.put(ORIGIN_URI, mOriginUri.toString());
        jsonObject.put(THUMB_URI, mThumbUri.toString());
        jsonObject.put(PIC_WIDTH, mImageWidth);
        jsonObject.put(PIC_HEIGHT, mImageHeight);
    }

    @Override
    public void recycle() {
        if (mCacheDrawable != null) {
            if (mCacheDrawable instanceof BitmapDrawable
                    && mCacheDrawable != Config.EditPage.getDefaultImageDrawable(NoteAppImpl.getContext())) {
                ((BitmapDrawable) mCacheDrawable).getBitmap().recycle();
            }
        }
        if (mSpanWatcher != null) {
            mText.removeSpan(mSpanWatcher);
            mSpanWatcher = null;
        }
//        if (null != mCacheDrawable) {
//            ((BitmapDrawable) mCacheDrawable).getBitmap().recycle();
//            mCacheDrawable = null;
//        }
        mCacheDrawable = null;
        mListener = null;
        mText = null;
        mContext = null;
    }

    /**
     * A JsonableSpan must have a static public field named AAPPLYER
     */
    public static final JsonableSpan.Applyer<PhotoImageSpan> APPLYER = new JsonableSpan.Applyer<PhotoImageSpan>() {
        @Override
        public PhotoImageSpan applyFromJson(JSONObject json, SpannableStringBuilder builder,
                                            Context context, boolean isEncrypt) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            int picWidth = 0;
            int picHeight = 0;
            if (json.has(PIC_WIDTH)) {
                picWidth = json.getInt(PIC_WIDTH);
            }
            if (json.has(PIC_HEIGHT)) {
                picHeight = json.getInt(PIC_HEIGHT);
            }
            Uri thumbUri = Uri.parse(json.getString(THUMB_URI));
            Uri originUri = Uri.parse(json.getString(ORIGIN_URI));

            PhotoImageSpan span = new PhotoImageSpan(context, builder, thumbUri, originUri, isEncrypt,
                    picWidth, picHeight);
            builder.setSpan(span, start, end, flag);
            span.initSpan(start);
            return span;
        }
    };

    private class ImageSpanWatcher implements SpanWatcher {
        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what == PhotoImageSpan.this) {
                checkDeleteRedundancyPhotoTag(text, start, end);
                if (mSpanWatcher != null) {
                    mText.removeSpan(mSpanWatcher);
                    mSpanWatcher = null;
                }
                NoteUtils.deleteImageFile(mOriginUri, mThumbUri, mIsEncrypt);
                recycle();
                Runtime.getRuntime().gc();
            }
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancyPhotoTag(Spannable spanText, int start, int end) {
            int redundancyPhotoTagLength = Constants.MEDIA_PHOTO.length() - 1;
            if (redundancyPhotoTagLength == (end - start)) {
                String redundancyPhotoTag = Constants.MEDIA_PHOTO.substring(0, redundancyPhotoTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end) {
                    String substring = text.substring(start, end);
                    boolean hasRedundancyPhotoTag = redundancyPhotoTag.equals(substring);
                    if (hasRedundancyPhotoTag) {
                        mText.delete(start, end);
                    }
                }
            }
        }
    }

}
