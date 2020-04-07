package com.cydroid.framework.log;

import android.os.Bundle;
import android.os.Message;
import android.os.Process;

import com.gionee.framework.storage.StorageMgr;
import com.gionee.framework.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/*package*/final class Log2File implements ILog {

    private static final String BASE_FILENAME = ".log";
    private static final String PACKAGE_NAME = "cyee/AmiNote/log";
    private static final String LOG_LINE_CONNECTOR = " : ";
    private static final String LOG_SEPARATOR = "   ";
    private static final int SIZE = 2048;
    private static final String LOG_DIRECTORY = StorageMgr.getInstance().getSdcardRootPath();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private boolean mIsOpen = false;
    private Writer mWriter;

    @Override
    public void println(Message msg) {
        if (!mIsOpen) {
            open();
        }
        final Bundle bundle = msg.getData();
        String tag = bundle.getString(Logger.TAG);
        String log = bundle.getString(Logger.LOG);
        String threadid = bundle.getString(Logger.THREAD_ID);
        final StringBuffer buffer = new StringBuffer();
        buffer.append(getCurrentTimeString()).append(LOG_SEPARATOR)
                .append(Process.myPid()).append(LOG_SEPARATOR)
                .append(threadid).append(LOG_SEPARATOR)
                .append(tag).append(LOG_LINE_CONNECTOR)
                .append(log);
        writeLine(buffer.toString());
    }

    @Override
    public void printStack(Message msg) {
        if (!mIsOpen) {
            open();
        }
        final Bundle bundle = msg.getData();
        String tag = bundle.getString(Logger.TAG);
        String log = bundle.getString(Logger.LOG);
        String threadid = bundle.getString(Logger.THREAD_ID);
        Throwable throwable = (Throwable) msg.obj;
        final StringBuffer buffer = new StringBuffer();
        buffer.append(getCurrentTimeString()).append(LOG_SEPARATOR)
                .append(Process.myPid()).append(LOG_SEPARATOR)
                .append(threadid).append(LOG_SEPARATOR)
                .append(tag).append(LOG_LINE_CONNECTOR)
                .append(log);
        writeLine(buffer.toString());
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw, true);
            throwable.printStackTrace(pw);
            pw.flush();
            sw.flush();
            writeLine(tag + LOG_LINE_CONNECTOR + sw.toString());
        } catch (Exception e) {

        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e) {
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
    }

    private static synchronized String getCurrentDate() {
        return DAY_FORMAT.format(new Date());
    }

    private static synchronized String getCurrentTimeString() {
        return TIME_FORMAT.format(new Date());
    }

    private static String getLogDirectory() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(LOG_DIRECTORY).append(File.separator)
                .append(PACKAGE_NAME).append(File.separator)
                .append(getCurrentDate()).append(BASE_FILENAME);
        return buffer.toString();
    }

    private void open() {

        File file = new File(getLogDirectory());
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (Exception e) {
                throw new LogIOException("create log dirs error!");
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new LogIOException("create log file error!");
            }
        }
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true),
                    StringUtils.ENCODING_UTF8);
            mWriter = new BufferedWriter(writer, SIZE);
        } catch (IOException e) {
            throw new LogIOException("open log file error!");
        }
        mIsOpen = true;
    }

    private void writeLine(String message) {
        try {
            mWriter.append(message);
            mWriter.append('\n');
            mWriter.flush();
        } catch (Exception e) {
            throw new LogIOException();
        }
    }

    public void dispose() {
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
