package com.cydroid.framework.log;

//import com.gionee.amiweather.framework.ApplicationProperty;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UUF_UNUSED_FIELD", justification = "seems no problem")
public final class Logger {
    public static final int FLAG_OPEN_LOG_TO_FILE = 0x00000000;
    public static final int FLAG_OPEN_LOG_TO_LOGCAT = 0x00000001;
    public static final int MASK_LOG_FLAG = 0x00000001;
    static final int FLAG_TO_PRINT_LOG = 0x00000000;
    static final int FLAG_TO_PRINT_STACK_TRANCE = 0x00000001;
    static final String TAG = "tag";
    static final String LOG = "log";
    static final String THREAD_ID = "thread_id";
    static final String THREAD_NAME = "thread_name";
    static final String THROWABLE = "throwable";
    private static final String TAG_PREFIX;

    static {
//        if (ApplicationProperty.isGioneeVersion()) {
        TAG_PREFIX = "aminote_";
//        } else {
//            TAG_PREFIX = "WeatherOpen_";
////        }
    }

    private static final boolean DEBUG = false;
    private static final boolean OPEN_LOG_DEVICE = false;
    private static HandlerThread sHandlerThread;//NOSONAR
    private static LogHandler sLogHandler;//NOSONAR

    public static void printLog(String tag, String log) {
        if (OPEN_LOG_DEVICE) {
            Message message = Message.obtain(sLogHandler);//NOSONAR
            Bundle bundle = new Bundle();
            bundle.putString(TAG, TAG_PREFIX + tag);
            bundle.putString(LOG, log);
            final Thread thread = Thread.currentThread();
            bundle.putString(THREAD_ID, "" + thread.getId());
            bundle.putString(THREAD_NAME, thread.getName());
            message.setData(bundle);
            message.what = FLAG_TO_PRINT_LOG;
            if (sLogHandler != null) {//NOSONAR
                sLogHandler.sendMessage(message);//NOSONAR
            }
        }
    }

    public static void printStackTrace(String tag, String logMsg, Throwable throwable) {
        if (OPEN_LOG_DEVICE) {
            Message message = Message.obtain(sLogHandler);
            Bundle bundle = new Bundle();
            bundle.putString(TAG, TAG_PREFIX + tag);
            bundle.putString(LOG, logMsg);
            final Thread thread = Thread.currentThread();
            bundle.putString(THREAD_ID, "" + thread.getId());
            bundle.putString(THREAD_NAME, thread.getName());
            message.setData(bundle);
            message.obj = throwable;
            message.what = FLAG_TO_PRINT_STACK_TRANCE;
            if (sLogHandler != null) {//NOSONAR
                sLogHandler.sendMessage(message);//NOSONAR
            }
        }
    }

    public static void enableLog(final int status) {
        if (OPEN_LOG_DEVICE) {
            sHandlerThread = new HandlerThread("LogHandler") {//NOSONAR

                @Override
                protected void onLooperPrepared() {
                    super.onLooperPrepared();
                    sLogHandler = new LogHandler(sHandlerThread.getLooper());//NOSONAR
                    LogFactory.setDefaultLogClient(status);
                }

            };
            sHandlerThread.start();//NOSONAR
        }
    }

}
