package com.gionee.framework.log;

import android.os.Bundle;
import android.os.Message;
import com.cydroid.note.common.Log;

/*package*/final class Log2Logcat implements ILog {

    @Override
    public void println(Message msg) {
        final Bundle bundle = msg.getData();
        String tag = bundle.getString(Logger.TAG);
        String log = bundle.getString(Logger.LOG);
        Log.d(tag, log);
    }

    @Override
    public void printStack(Message msg) {
        final Bundle bundle = msg.getData();
        String tag = bundle.getString(Logger.TAG);
        String log = bundle.getString(Logger.LOG);
        Throwable throwable = (Throwable) msg.obj;
        Log.d(tag, log, throwable);
    }

}
