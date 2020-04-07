package com.cydroid.note.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.app.Config;

import java.io.FileOutputStream;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static Bitmap resizeBitmapBySize(Bitmap bitmap, int targetWidth, int targetHeight, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scale = Math.max((float) targetWidth / w, (float) targetHeight / h);
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == targetHeight && height == targetWidth) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.translate((targetWidth - width) / 2f, (targetHeight - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int targetW, int targetH, boolean recycle, boolean isCropped) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == targetW && h == targetH) {
            return bitmap;
        }
        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale;
        if (isCropped) {
            scale = Math.max((float) targetW / w, (float) targetH / h);
        } else {
/*            scale = (float) targetW / w;
            targetH = Math.round(h * scale);*/
            scale = (float) targetH / h;
            targetW = Math.round(w * scale);
        }
        Bitmap target = Bitmap.createBitmap(targetW, targetH, getConfig(bitmap));
        int width = Math.round(scale * w);
        int height = Math.round(scale * h);
        Canvas canvas = new Canvas(target);
        canvas.translate((targetW - width) / 2f, (targetH - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;

    }

    public static Bitmap assembleSoundBitmap(Context context, int durationInSec) {
        Config.EditPage page = Config.EditPage.get(context);
        Drawable bg = context.getDrawable(R.drawable.edit_page_sound_bg);
        Bitmap bitmap = Bitmap.createBitmap(page.mSoundWidth, page.mSoundHeight, Bitmap.Config.ARGB_8888);
        bg.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        bg.draw(canvas);
        Paint paint = new Paint(Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(page.mSoundPointColor);
        canvas.drawCircle(page.mSoundPointOffsetLeft, page.mSoundPointOffsetRight,
                page.mSoundPointRadius, paint);
        drawText(page, canvas, durationInSec);
        return bitmap;

    }

    private static void drawText(Config.EditPage page, Canvas canvas, int durationInSec) {
        String text = NoteUtils.formatTime(durationInSec, ":");
        TextPaint paint = getTextPaint(page.mSoundDurationSize, page.mSoundDurationColor, false);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float textHeight = metrics.descent - metrics.ascent;
        float x = page.mSoundDurationOffsetLeft;
        float y = (page.mSoundHeight - textHeight) / 2 - metrics.ascent;
        canvas.drawText(text, x, y, paint);
    }

    public static void compressToFile(Bitmap bitmap, String filePath) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception e) {
            Logger.printLog(TAG, "compressToFile fail : " + e.toString());
        } finally {
            NoteUtils.closeSilently(outputStream);
        }
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    // Find the min x that 1 / x >= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(1f / scale);
        if (initialSize <= 1) {
            return 1;
        }

        return initialSize <= 8
                ? NoteUtils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    public static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    public static Bitmap createSpecifyColorBitmap(Bitmap origin, int color) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        int[] previousPixels = new int[width * height];
        origin.getPixels(previousPixels, 0, width, 0, 0, width, height);
        color &= 0x00FFFFFF;
        for (int i = 0; i < height; i++) {
            int lineStart = i * width;
            for (int j = 0; j < width; j++) {
                int pos = lineStart + j;
                previousPixels[pos] &= 0xFF000000;
                previousPixels[pos] |= color;
            }
        }
        Bitmap result = origin.copy(Bitmap.Config.ARGB_8888, true);
        result.setPixels(previousPixels, 0, width, 0, 0, width, height);
        return result;
    }
}
