package com.cydroid.note.app.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import com.cydroid.note.common.Log;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.widget.NoteWidgetProvider;

import java.io.InputStream;

public class DrawableManager {

    private static final boolean DEBUG = false;
    private static final String TAG = "DrawableManager";
    private static Drawable sNoteCardNormalDrawable;
    private static Drawable sNoteCardOldDrawable;
    private static Drawable sNoteCardVeryOldDrawable;
    private static Drawable sNoteCardVeryVeryOldDrawable;

    public static Drawable getCardEffectDrawable(Context context, int effect, int w, int h) {
        if (effect == EffectUtil.EFFECT_NORMAL) {
            return getNoteCardNormalDrawable(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_OLD) {
            return getNoteCardOldDrawable(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_OLD) {
            return getNoteCardVeryOldDrawable(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_VERY_OLD) {
            return getNoteCardVeryVeryOldDrawable(context, w, h);
        }

        throw new AssertionError();
    }

    public static Bitmap getWidgetEffectBitmap(Context context, int widgetType, int effect, int w, int h) {
        if (effect == EffectUtil.EFFECT_NORMAL) {
            return getNoteWidgetNormalBitmap(context, widgetType, w, h);
        }

        if (effect == EffectUtil.EFFECT_OLD) {
            return getNoteWidgetOldBitmap(context, widgetType, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_OLD) {
            return getNoteWidgetVeryOldBitmap(context, widgetType, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_VERY_OLD) {
            return getNoteWidgetVeryVeryOldBitmap(context, widgetType, w, h);
        }
        throw new AssertionError();
    }

    private static Drawable getNoteCardNormalDrawable(Context context, int w, int h) {
        if (sNoteCardNormalDrawable == null) {
            sNoteCardNormalDrawable = createDrawable(context, "note_card_normal.png", w, h);
        }
        return sNoteCardNormalDrawable;
    }

    private static Drawable getNoteCardOldDrawable(Context context, int w, int h) {
        if (sNoteCardOldDrawable == null) {
            sNoteCardOldDrawable = createDrawable(context, "note_card_old.png", w, h);
        }
        return sNoteCardOldDrawable;
    }

    private static Drawable getNoteCardVeryOldDrawable(Context context, int w, int h) {
        if (sNoteCardVeryOldDrawable == null) {
            sNoteCardVeryOldDrawable = createDrawable(context, "note_card_very_old.png", w, h);
        }
        return sNoteCardVeryOldDrawable;
    }

    private static Drawable getNoteCardVeryVeryOldDrawable(Context context, int w, int h) {
        if (sNoteCardVeryVeryOldDrawable == null) {
            sNoteCardVeryVeryOldDrawable = createDrawable(context, "note_card_vv_old.png", w, h);
        }
        return sNoteCardVeryVeryOldDrawable;
    }

    private static Bitmap getNoteWidgetNormalBitmap(Context context, int widgetType, int w, int h) {
        if (NoteWidgetProvider.WIDGET_TYPE_2X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_2_2_normal.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_2_2_normal.png", w, h);
            }
        } else if (NoteWidgetProvider.WIDGET_TYPE_4X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_2_2_normal.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_2_2_normal.png", w, h);
            }
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetOldBitmap(Context context, int widgetType, int w, int h) {
        if (NoteWidgetProvider.WIDGET_TYPE_2X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_2_2_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_2_2_old.png", w, h);
            }
        } else if (NoteWidgetProvider.WIDGET_TYPE_4X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_4_4_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_4_4_old.png", w, h);
            }
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetVeryOldBitmap(Context context, int widgetType, int w, int h) {
        if (NoteWidgetProvider.WIDGET_TYPE_2X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_2_2_v_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_2_2_v_old.png", w, h);
            }
        } else if (NoteWidgetProvider.WIDGET_TYPE_4X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_4_4_v_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_4_4_v_old.png", w, h);
            }
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetVeryVeryOldBitmap(Context context, int widgetType, int w, int h) {
        if (NoteWidgetProvider.WIDGET_TYPE_2X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_2_2_vv_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_2_2_vv_old.png", w, h);
            }
        } else if (NoteWidgetProvider.WIDGET_TYPE_4X == widgetType) {
            if (PlatformUtil.isBusinessStyle()) {
                return createBitmap(context, "business_style_note_widget_bg_4_4_vv_old.png", w, h);
            } else {
                return createBitmap(context, "note_widget_bg_4_4_vv_old.png", w, h);
            }
        }
        throw new AssertionError();
    }

    private static Drawable createDrawable(Context context, String name, int w, int h) {
        Bitmap bitmap = createBitmap(context, name, w, h);
        if (bitmap != null) {
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            drawable.setBounds(0, 0, w, h);
            return drawable;
        }
        return null;
    }

    private static Bitmap createBitmap(Context context, String name, int w, int h) {
        int[] size = getBitmapOriginalSize(context, name);
        if (!isValidBitmapSize(size)) {
            return null;
        }
        int ow = size[0];
        int oh = size[0];

        Bitmap bitmap = decodeRawBitmap(context, name, ow, oh, w, h);
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() != w || bitmap.getHeight() != h) {
            bitmap = createTargetBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    private static int[] getBitmapOriginalSize(Context context, String rawFileName) {
        InputStream is = null;
        try {
            is = context.getAssets().open(rawFileName);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            int originalW = options.outWidth;
            int originalH = options.outHeight;
            int[] size = new int[2];
            size[0] = originalW;
            size[1] = originalH;
            return size;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }


    private static Bitmap decodeRawBitmap(Context context, String rawFileName,
                                          int ow, int oh, int tw, int th) {
        InputStream is = null;
        try {
            is = context.getAssets().open(rawFileName);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inSampleSize = calculationSampleSize(ow, oh, tw, th);
            if (DEBUG) {
                Log.d(TAG, "options.inSampleSize = " + options.inSampleSize + ",tw = " + tw
                        + ",th = " + th + ",ow = " + ow + ",oh = " + oh);
            }
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            if (bitmap == null) {
                return null;
            }
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    private static int calculationSampleSize(int originalW, int originalH, int tw, int th) {
        int inSampleSize = Math.min(originalW / tw, originalH / th);
        return inSampleSize > 1 ? inSampleSize : 1;
    }

    private static Bitmap createTargetBitmap(Bitmap bitmap, int tw, int th, boolean isRecycle) {
        Bitmap targetBitmap;
        try {
            targetBitmap = Bitmap.createBitmap(tw, th, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        Canvas canvas = new Canvas(targetBitmap);
        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect(0, 0, tw, th);
        canvas.drawBitmap(bitmap, src, dst, paint);
        if (isRecycle) {
            bitmap.recycle();
        }
        return targetBitmap;
    }

    public static Bitmap getEffectBitmap(Context context, int effect, int w, int h) {

        if (effect == EffectUtil.EFFECT_NORMAL) {
            return getNoteBgNormalBitmap(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_OLD) {
            return getNoteBgOldBitmap(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_OLD) {
            return getNoteBgVeryOldBitmap(context, w, h);
        }

        if (effect == EffectUtil.EFFECT_VERY_VERY_OLD) {
            return getNoteBgVeryVeryOldBitmap(context, w, h);
        }

        return null;
    }

    private static Bitmap getNoteBgVeryVeryOldBitmap(Context context, int w, int h) {
        if (PlatformUtil.isBusinessStyle()) {
            return getNoteBgBitmap(context, "business_style_note_bg_vv_old.png",
                    "business_style_note_bg_vv_old_bottom.png", w, h);
        } else {
            return getNoteBgBitmap(context, "note_bg_vv_old.png", "note_bg_vv_old_bottom.png", w, h);
        }
    }

    private static Bitmap getNoteBgVeryOldBitmap(Context context, int w, int h) {
        if (PlatformUtil.isBusinessStyle()) {
            return getNoteBgBitmap(context, "business_style_note_bg_very_old.png",
                    "business_style_note_bg_very_old_bottom.png", w, h);
        } else {
            return getNoteBgBitmap(context, "note_bg_very_old.png", "note_bg_very_old_bottom.png", w, h);
        }
    }

    private static Bitmap getNoteBgOldBitmap(Context context, int w, int h) {
        if (PlatformUtil.isBusinessStyle()) {
            return getNoteBgBitmap(context, "business_style_note_bg_old.png",
                    "business_style_note_bg_old_bottom.png", w, h);
        } else {
            return getNoteBgBitmap(context, "note_bg_old.png", "note_bg_old_bottom.png", w, h);
        }
    }

    private static Bitmap getNoteBgBitmap(Context context, String normalName,
                                          String bottomName, int w, int h) {
        int[] oldBmpSize = getBitmapOriginalSize(context, normalName);
        if (!isValidBitmapSize(oldBmpSize)) {
            return null;
        }
        int[] oldBottomBmpSize = getBitmapOriginalSize(context, bottomName);
        if (!isValidBitmapSize(oldBottomBmpSize)) {
            return null;
        }
        Bitmap oldBmp = decodeRawBitmap(context, normalName, oldBmpSize[0], oldBmpSize[0], w, w);
        if (oldBmp == null) {
            return null;
        }
        Bitmap oldBottomBmp = decodeRawBitmap(context, bottomName, oldBottomBmpSize[0]
                , oldBottomBmpSize[0], w, w);
        if (oldBottomBmp == null) {
            return null;
        }
        Bitmap dstBmp;
        try {
            dstBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Canvas canvas = new Canvas(dstBmp);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

        //Draw the most below
        int oldBottomBmpW = oldBottomBmp.getWidth();
        int oldBottomBmpH = oldBottomBmp.getHeight();
        float dstBottomW = (float) w;
        float oldBottomBmpScale = dstBottomW / oldBottomBmpW;
        float dstBottomH = oldBottomBmpH * oldBottomBmpScale;
        float dstBottomX = 0f;
        float dstBottomY = h - dstBottomH;
        RectF bottomRectF = new RectF(dstBottomX, dstBottomY, dstBottomX + dstBottomW,
                dstBottomY + dstBottomH);
        canvas.drawBitmap(oldBottomBmp, new Rect(0, 0, oldBottomBmpW, oldBottomBmpH),
                bottomRectF, paint);

        //Draw up from the bottom
        float leaveH = dstBottomY;
        int oldBmpW = oldBmp.getWidth();
        int oldBmpH = oldBmp.getHeight();
        float dstOldW = (float) w;
        float oldBmpScale = dstOldW / oldBmpW;
        float dstOldH = oldBmpH * oldBmpScale;
        int drawCount = (int) (leaveH / dstOldH);
        Rect srcRect = new Rect(0, 0, oldBmpW, oldBmpH);
        RectF dstRectF = new RectF();
        float top;
        for (int i = drawCount - 1; i >= 0; i--) {
            top = leaveH - (drawCount - i) * dstOldH;
            dstRectF.set(0, top, dstOldW, top + dstOldH);
            canvas.drawBitmap(oldBmp, srcRect, dstRectF, paint);
        }

        //Draw the rest
        float overPlus = leaveH - drawCount * dstOldH;
        if (overPlus > 0) {
            dstRectF.set(0, 0, dstOldW, overPlus);
            float overPlusBitmapH = overPlus / oldBmpScale;
            srcRect.set(0, (int) (oldBmpH - overPlusBitmapH), oldBmpW, oldBmpH);
            canvas.drawBitmap(oldBmp, srcRect, dstRectF, paint);
        }
        return dstBmp;
    }

    private static boolean isValidBitmapSize(int[] bitmapSize) {
        if (bitmapSize == null) {
            return false;
        }
        return !(bitmapSize[0] == 0 || bitmapSize[1] == 0);
    }

    private static Bitmap getNoteBgNormalBitmap(Context context, int w, int h) {
        Bitmap dstBmp;
        try {
            dstBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Canvas canvas = new Canvas(dstBmp);
        int color = ContextCompat.getColor(context, PlatformUtil.isBusinessStyle() ?
                R.color.new_note_activity_normal_bg_color_business_style
                : R.color.new_note_activity_normal_bg_color);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        paint.setColor(color);
        canvas.drawRect(new Rect(0, 0, w, h), paint);
        return dstBmp;
    }
}
