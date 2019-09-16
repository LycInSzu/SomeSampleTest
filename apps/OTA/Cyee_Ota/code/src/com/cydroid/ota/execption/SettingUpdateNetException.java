package com.cydroid.ota.execption;

/**
 * Created by borney on 4/15/15.
 */
public class SettingUpdateNetException extends SettingUpdateException {
    public static final int ERROR_SOCKET_TIMEOUT = 100;
    public static final int ERROR_SOCKET_IO = 101;

    private int mCode = -1;

    public SettingUpdateNetException(String msg) {
        super(msg);
    }

    public SettingUpdateNetException(int code) {
        super("net exception occur !!! status = " + code);
        mCode = code;
    }

    public int getHttpStatus() {
        return mCode;
    }
}
