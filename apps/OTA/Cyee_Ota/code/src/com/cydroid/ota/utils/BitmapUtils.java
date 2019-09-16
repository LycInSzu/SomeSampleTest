/**
 * 
 */
package com.cydroid.ota.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author kangjj
 * 
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int HIGH_QUALITY = 100;
    private static final int LOW_QUALITY = 50;
    private static final int MAX_SIZE = 1024 * 1024;

    public static Bitmap compressFromBitmap(Bitmap image, int targetwidth, int targetHeigh, long maxSize) {
        if(null==image){
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream isBm = null;
        try {
            image.compress(CompressFormat.JPEG, HIGH_QUALITY, baos);
            Log.d(TAG, "compressFromBitmap  " + baos.toByteArray().length);
            if (baos.toByteArray().length > MAX_SIZE) {
                baos.reset();
                image.compress(CompressFormat.JPEG, LOW_QUALITY, baos);
            }

            isBm = new ByteArrayInputStream(baos.toByteArray());
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(isBm, null, newOpts);
            newOpts.inJustDecodeBounds = false;

            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            int scale = 1;

            if (w > h && w > targetwidth) {
                scale = (int) (newOpts.outWidth / targetwidth);
            } else if (w < h && h > targetHeigh) {
                scale = (int) (newOpts.outHeight / targetHeigh);
            }
            if (scale <= 0) {
                scale = 1;
            }
            newOpts.inSampleSize = scale;

            isBm = new ByteArrayInputStream(baos.toByteArray());
            Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

            return qualityCompress(bitmap, maxSize);
        } catch (Exception e) {
            Log.e(TAG, "Exception:" + e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                }
            }

            if (isBm != null) {
                try {
                    isBm.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private static Bitmap qualityCompress(Bitmap image, long maxSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream isBm = null;
        try {
            image.compress(CompressFormat.JPEG, HIGH_QUALITY, baos);
            int options = HIGH_QUALITY;
            byte[] thumbData;
            Bitmap bitmap = null;
            int step = 5;

            do {
                baos.reset();
                image.compress(CompressFormat.JPEG, options, baos);
                isBm = new ByteArrayInputStream(baos.toByteArray());
                recycleBitmap(bitmap);
                bitmap = null;
                if (options <= step) {
                    step = 1;
                }
                options -= step;
                if (options <= 0) {
                    break;
                }
                bitmap = BitmapFactory.decodeStream(isBm, null, null);
                thumbData = bmpToByteArray(bitmap, false);
                Log.d(TAG, "length = " + thumbData.length + " maxSize = "
                        + maxSize);

                isBm.close();
            } while (thumbData.length > maxSize);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                }
            }

            if (isBm != null) {
                try {
                    isBm.close();
                } catch (IOException e) {
                }
            }

            recycleBitmap(image);
        }
        return null;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, HIGH_QUALITY, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }


    public static Drawable getSettingsDrawable(Drawable icon, Context context){
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.save();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        Resources resources = context.getResources();
        Drawable drawable = resources.getDrawable(R.drawable.setting_hint);
        Rect rect = new Rect();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        rect.left = width - intrinsicWidth - resources.getDimensionPixelSize(R.dimen.gn_su_badgeview_marginright);
        rect.top = resources.getDimensionPixelSize(R.dimen.gn_su_badgeview_margintop);
        rect.right = rect.left + intrinsicWidth;
        rect.bottom = rect.top + intrinsicHeight;
        Bitmap circle = ((BitmapDrawable)drawable).getBitmap();
        canvas.drawBitmap(circle, null, rect, paint);
        paint.reset();
        paint.setColor(Color.WHITE);
        paint.setTextSize(resources.getDimensionPixelSize(R.dimen.gn_su_appupgrade_num_textsize));
        canvas.restore();
        return new BitmapDrawable(resources, tempBitmap);
    }
}
