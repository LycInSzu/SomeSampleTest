package com.cydroid.ota.ui.widget;

/**
 * @author borney
 * Created by borney on 6/8/15.
 */
public final class SimilarColor {
    private static final String TAG = "SimilarColor";
    // access the red component from a premultiplied color
    public static int getB32(int c) {
        return (c >> 0) & 0xFF;
    }

    // access the red component from a premultiplied color
    public static int getG32(int c) {
        return (c >> 8) & 0xFF;
    }

    // access the red component from a premultiplied color
    public static int getR32(int c) {
        return (c >> 16) & 0xFF;
    }

    // access the red component from a premultiplied color
    public static int getA32(int c) {
        return (c >> 24) & 0xFF;
    }

    /**
     * This takes components that are already in premultiplied form, and
     * packs them into an int in the correct device order.
     */
    public static int pack8888(int r, int g, int b, int a) {
        return (b << 0) | (g << 8) | (r << 16) | (a << 24);
    }

    public static int getSimilarColor(int color) {
        int a = getA32(color);
        int r = getR32(color);
        int g = getG32(color);
        int b = getB32(color);
        int pack8888 = pack8888((int) (r * 0.96), (int) (g * 0.75), (int) (b * 0.9), a);
        return pack8888;
    }
}
