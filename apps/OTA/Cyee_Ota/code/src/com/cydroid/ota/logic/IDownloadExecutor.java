package com.cydroid.ota.logic;

/**
 * Created by borney on 4/16/15.
 */
public interface IDownloadExecutor extends ICancelable {
    int INTERRUPT_REASON_DEFAULT = 100;
    int INTERRUPT_REASON_BY_STORAGE = INTERRUPT_REASON_DEFAULT + 1;
    int INTERRUPT_REASON_BY_NET = INTERRUPT_REASON_DEFAULT + 2;

    void start();

    void pause();

    void restart();

    int getInterruptReason();

    long getDownloadedFileSize();

    int getDownloadProgress();

}
