package com.cydroid.ota.logic;

/**
 * Created by borney on 6/9/15.
 */
public interface IDownloadCallback {
    void onProgress(long totalSize, long downloadSize, double speed);

    void onError(int errorCode);
}
