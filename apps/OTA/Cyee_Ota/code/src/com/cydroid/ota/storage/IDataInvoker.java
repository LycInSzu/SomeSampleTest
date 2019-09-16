package com.cydroid.ota.storage;

/**
 * Created by borney on 4/14/15.
 */
public interface IDataInvoker {
    IStorage settingStorage();

    IStorage pushStorage();

    IStorage wlanAutoStorage();

    IStorageUpdateInfo settingUpdateInfoStorage();
}
