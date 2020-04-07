package com.gionee.framework.log;

import static com.gionee.framework.component.BaseApplication.FLAG_OPEN_LOG_TO_FILE;
import static com.gionee.framework.component.BaseApplication.MASK_LOG_FLAG;


/*package*/ final class LogFactory {

    private static final Log2Logcat LOGCAT_OPERATOR = new Log2Logcat();
    private static final Log2File LOG_FILE_OPERATOR = new Log2File();

    private static ILog sDefaultClient;

    public static ILog getDefaultLogClient() {
        if (sDefaultClient == null) {
            sDefaultClient = LOGCAT_OPERATOR;
        }
        return sDefaultClient;
    }

    public static ILog getLogcatClient() {
        return LOGCAT_OPERATOR;
    }

    public static ILog getLogFileClient() {
        return LOG_FILE_OPERATOR;
    }

    public static void setDefaultLogClient(int client) {
        if ((client & MASK_LOG_FLAG) == FLAG_OPEN_LOG_TO_FILE) {
            sDefaultClient = LOG_FILE_OPERATOR;
        } else {
            sDefaultClient = LOGCAT_OPERATOR;
        }
    }
}
