package com.cydroid.ota.execption;

/**
 * Created by liuyanfeng on 15-4-22.
 */
public class SettingUpdateNoSpaceException extends SettingUpdateException{
    public SettingUpdateNoSpaceException(String msg) {
        super(msg);
    }
}
