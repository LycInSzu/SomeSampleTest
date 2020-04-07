package com.gionee.framework.log;

public final class LogIOException extends RuntimeException {

    private String mMessage;

    public LogIOException(String detailMessage) {
        super(detailMessage);
        mMessage = detailMessage;
    }

    @Override
    public String getMessage() {
        return mMessage == null ? "write file error!!!" : mMessage;
    }

    public LogIOException() {
        super();
    }

}
