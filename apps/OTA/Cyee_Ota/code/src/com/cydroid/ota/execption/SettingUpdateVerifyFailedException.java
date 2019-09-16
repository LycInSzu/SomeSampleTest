package com.cydroid.ota.execption;

/**
 * Created by liuyanfeng on 15-4-23.
 */
public class SettingUpdateVerifyFailedException extends SettingUpdateException {
    public SettingUpdateVerifyFailedException(String msg) {
        super(msg);
    }
}
