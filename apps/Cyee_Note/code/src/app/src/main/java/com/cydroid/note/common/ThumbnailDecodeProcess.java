package com.cydroid.note.common;


import android.app.filecrypt.zyt.filesdk.FileCryptUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.FileConfuseSession;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ThumbnailDecodeProcess {

    public static final String TAG = "ThumbnailDecodeProcess";

    public Uri mUri;
    private int mWidth;
    private int mHeight;
    private int mSizeThreshold;
    private Context mContext;
    private boolean mIsCrypted;
    public ThumbnailDecodeMode mThumbnailDecodeMode;

    public ThumbnailDecodeProcess(Context context, Uri uri, int thumbnailWith, int thumbnailHeight
            , ThumbnailDecodeMode decodeMode, boolean isCrypted) {
        mContext = context.getApplicationContext();
        mUri = uri;
        mWidth = thumbnailWith;
        mHeight = thumbnailHeight;
        mSizeThreshold = context.getResources().getDimensionPixelSize
                (R.dimen.attach_selector_pic_size_threshold);
        mThumbnailDecodeMode = decodeMode;
        mIsCrypted = isCrypted;
    }

    public Bitmap getThumbnail() {
        InputStream is = null;
        try {
            int[] size = DecodeUtils.loadBitmapSize(mContext, mUri, mIsCrypted);
            if (size == null) {
                return null;
            }
            boolean decodeFromSecuritySpace = PlatformUtil.isSecurityOS() && mIsCrypted;
            if (decodeFromSecuritySpace) {
                is = FileCryptUtil.getDecryptInputStream(EncryptUtil.getSecuritySpacePath(mUri.getPath()));
            } else if (mIsCrypted) {
                is = FileConfuseSession.open().backupConfuse(mUri.getPath());
            } else {
                is = mContext.getContentResolver().openInputStream(mUri);
            }
            if (null == is) {
                return null;
            }
            return decodeScaleThumbnail(is, size[0], size[1], mWidth, mHeight);
        } catch (FileNotFoundException e) {
            Logger.printLog(TAG, "decodeProcess FileNotFoundException:" + e);
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeProcess rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    private Bitmap decodeScaleThumbnail(InputStream is, int originW, int originH,
                                        int targetW, int targetH) {
        int rotate = DecodeUtils.decodeImageRotate(mUri);
        float scale = 0;
        if (rotate == 90 || rotate == 270) {
            scale = Math.max((float) targetW / originH, (float) targetH / originW);
        } else {
            scale = Math.max((float) targetW / originW, (float) targetH / originH);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        options.inMutable = true;
        Bitmap result = BitmapFactory.decodeStream(is, null, options);
        if (result == null) {
            return null;
        }
        if (rotate != 0) {
            result = DecodeUtils.rotate(result, rotate);
        }
        return resizeThumbnail(result, targetW, targetH);
    }

    private Bitmap resizeThumbnail(Bitmap bitmap, int targetW, int targetH) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == targetW && h == targetH) {
            return bitmap;
        }
        float scale = 0;
        switch (mThumbnailDecodeMode) {
            case CUT_WIDTH_AND_HEIGHT:
                scale = Math.max((float) targetW / w, (float) targetH / h);
                break;
            case WIDTH_FIXED_HEIGHT_SCALE:
                scale = (float) targetW / w;
                targetH = Math.round(h * scale);
                break;
            case HEIGHT_FIXED_WIDTH_SCALE:
                float tempScale = (float) targetH / h;
                int tempTargetW = Math.round(w * tempScale);
                if (tempTargetW >= mSizeThreshold) {
                    scale = tempScale;
                    targetW = tempTargetW;
                } else {
                    scale = Math.max((float) targetW / w, (float) targetH / h);
                }
                break;
            case WITH_AND_HEIGHT_SCALE:
            default:
                break;
        }
        Bitmap target = Bitmap.createBitmap(targetW, targetH, getConfig(bitmap));
        int width = Math.round(scale * w);
        int height = Math.round(scale * h);
        Canvas canvas = new Canvas(target);
        canvas.translate((targetW - width) / 2f, (targetH - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        bitmap.recycle();
        return target;
    }

    private Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public enum ThumbnailDecodeMode {
        CUT_WIDTH_AND_HEIGHT, WIDTH_FIXED_HEIGHT_SCALE, HEIGHT_FIXED_WIDTH_SCALE, WITH_AND_HEIGHT_SCALE
    }

}
