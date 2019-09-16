package com.cydroid.ota.logic;

/**
 * Created by liuyanfeng on 15-6-16.
 */
public interface IAutoUpgradeDispatcher {
    void notifyAutoOperation(boolean isDownloadComplete);

    void notifyAutoCheck();

    void notifyAutoDownloadComplete();
}
