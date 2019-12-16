package com.cydroid.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * Created by zhaocaili on 18-7-4.
 *
 * 给两个Bitmap先灰度化，然后对比像素值，计算两个bitmap的相似度
 */

public class BitmapCompare {

    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    public static float similarity(Bitmap bm1, Bitmap bm2) {
        bm1 = bitmap2Gray(bm1);
        bm2 = bitmap2Gray(bm2);
        final int bm1Width = bm1.getWidth();
        final int bm2Width = bm2.getWidth();
        final int bmHeight = bm1.getHeight();

        if (bmHeight != bm2.getHeight() || bm1Width != bm2Width)
            return 0;

        int[] pixels1 = new int[bm1Width];
        int[] pixels2 = new int[bm2Width];

        reset();
        for (int i = 0; i < bmHeight; i++) {
            bm1.getPixels(pixels1, 0, bm1Width, 0, i, bm1Width, 1);
            bm2.getPixels(pixels2, 0, bm2Width, 0, i, bm2Width, 1);

            comparePixels(pixels1, pixels2, bm1Width);
        }

        return percent(Count.sT, Count.sF + Count.sT);
    }

    private static void comparePixels(int[] pixels1, int[] pixels2, int length) {
        for (int i = 0; i < length; i++) {
            if (pixels1[i] == pixels2[i]) {
                Count.sT++;
            } else {
                Count.sF++;
            }
        }
    }

    private static float percent(int divisor, int dividend) {
        final float value = divisor * 1.0f / dividend;
        return value;
//        DecimalFormat df = new DecimalFormat(RESULT_FORMAT);
//        return df.format(value);
    }

    private static void reset() {
        Count.sT = 0;
        Count.sF = 0;
    }

    private static class Count {
        private static int sT;
        private static int sF;
    }
}
