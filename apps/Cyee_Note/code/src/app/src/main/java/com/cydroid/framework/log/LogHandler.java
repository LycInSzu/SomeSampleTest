package com.gionee.framework.log;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class LogHandler extends Handler {
    public LogHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == Logger.FLAG_TO_PRINT_LOG) {
            LogFactory.getDefaultLogClient().println(msg);
        } else if (msg.what == Logger.FLAG_TO_PRINT_STACK_TRANCE) {
            LogFactory.getDefaultLogClient().printStack(msg);
        }
    }

}
