package com.prize.camera.feature.mode.filter;

public class PrizeFilterJni {

    private UVFilterJniCallback mUVFilterJniCallback = null;

    public PrizeFilterJni() {

    }

    public native boolean initNative(int width, int height, int effects[]);

    public native void runNative(byte[] data, int width, int height, int format, int id, int count);

    public native void destroyNative();

    public void nativeStatusCallback(int status) {
        if (null != this.mUVFilterJniCallback) {
            this.mUVFilterJniCallback.onNativeStatusCallback(status);
        }

    }

    public void nativeDataCallback(int[] data, int width, int height) {
        if (null != this.mUVFilterJniCallback) {
            this.mUVFilterJniCallback.onNativeDataCallback(data, width, height);
        }
    }

    public void setUVFilterJniCallback(UVFilterJniCallback uvFilterJniCallback) {
        this.mUVFilterJniCallback = uvFilterJniCallback;
    }

    static {
        System.loadLibrary("jni_uvfilter");
    }

    public interface UVFilterJniCallback {
        void onNativeStatusCallback(int var1);

        void onNativeDataCallback(int[] var1, int var2, int var3);
    }

}
