package com.cydroid.ota.logic.sync;

/**
 * Created by liuyanfeng on 15-4-16.
 */
public interface ISyncCallback {
    void onResult(Object... objects);

    void onError(int errorCode);
}
