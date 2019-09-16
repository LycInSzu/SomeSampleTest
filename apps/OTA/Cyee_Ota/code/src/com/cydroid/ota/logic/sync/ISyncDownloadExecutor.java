package com.cydroid.ota.logic.sync;

import com.cydroid.ota.bean.IUpdateInfo;

/**
 * Created by liuyanfeng on 15-4-24.
 */
public interface ISyncDownloadExecutor {
    boolean isContinue();

    IUpdateInfo getUpgradeInfo();

    boolean isStart();

    boolean isReadyToDownload();

    boolean isAutoUpgrade();

    boolean isRootState();
}
