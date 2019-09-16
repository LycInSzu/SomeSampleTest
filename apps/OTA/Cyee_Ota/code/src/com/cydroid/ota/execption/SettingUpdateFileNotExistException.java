package com.cydroid.ota.execption;

/**
 * Created by liuyanfeng on 15-4-22.
 */
public class SettingUpdateFileNotExistException extends SettingUpdateException {
    public SettingUpdateFileNotExistException(String msg) {
        super(msg);
    }
}
