package com.cydroid.ota.storage;

import com.cydroid.ota.bean.IUpdateInfo;

/**
 * Created by borney on 4/21/15.
 */
public interface IStorageUpdateInfo {
    void storage(IUpdateInfo info);

    IUpdateInfo getUpdateInfo();

    void clear();
}
