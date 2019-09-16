package com.cydroid.ota.logic;

import com.cydroid.ota.bean.IUpdateInfo;

/**
 * Created by borney on 4/15/15.
 */
public interface ISystemUpdate {
    ICheckExecutor checkUpdate();

    IContextState getContextState();

    IDownloadExecutor downUpdate(IDownloadCallback callback);

    IInstallExecutor installUpdate();

    IUpdateInfo getSettingUpdateInfo();

    void registerObserver(IObserver observer);

    void unregisterObserver(IObserver observer);
}
