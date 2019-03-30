package com.prize.camera.feature.mode.pano;

import com.mediatek.camera.common.debug.LogHelper;

public class UVPanoJni {

    private DataCallBack mDataCallBack;

    public UVPanoJni(){

    }

    synchronized public native int nativeInit(int width, int height, int maxWidth, int thumbnailWidth, int thumbnailHeight);

    synchronized public native int nativeUnInit();

    synchronized public native int nativeProcess(int width, int height, byte[] data);




    public void callbackData(byte[] data, int width, int height) {
        LogHelper.i("zg", "zhangguo callbackData");

        if (null != this.mDataCallBack) {
            this.mDataCallBack.onDataCallback(data, width, height);
        }
    }

    static {
        System.loadLibrary("jni_uvpano");
    }

    public void setDataCallBack(DataCallBack dataCallBack) {
        this.mDataCallBack = dataCallBack;
    }

    public interface DataCallBack {
        void onDataCallback(byte[] data, int width, int height);
    }
}
