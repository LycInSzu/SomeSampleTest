package com.cydroid.ota.utils;

/**
 * Created by liuyanfeng on 15-4-16.
 */
public class Error {
    private static final int BASE_ERROR = 0x20000;
    public static final int ERROR_CODE_NETWORK_ERROR = BASE_ERROR + 1;
    public static final int ERROR_CODE_PARSER_ERROR = BASE_ERROR + 2;
    public static final int ERROR_CODE_STORAGE_NOT_MOUNTED = BASE_ERROR + 3;
    public static final int ERROR_CODE_DOWNLOAD_NO_SPACE = BASE_ERROR + 4;
    public static final int ERROR_CODE_DOWNLOAD_FILE_NOT_EXIST = BASE_ERROR + 5;
    public static final int ERROR_CODE_DOWNLOAD_FILE_WRITE_ERROR = BASE_ERROR + 6;
    public static final int ERROR_CODE_DOWNLOAD_FILE_VERIFY_FAILED = BASE_ERROR + 7;
    public static final int ERROR_CODE_INSTALL_FILE_NOT_EXIT = BASE_ERROR + 8;
    public static final int ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT = BASE_ERROR + 9;
    public static final int ERROR_CODE_DOWNLOAD_STORAGE_INTERRUPT = BASE_ERROR + 10;
    public static final int ERROR_CODE_JOB_CANCEL = BASE_ERROR + 11;

    public static final int ERROR_CODE_NO_QUESTIONNAIRE = BASE_ERROR + 12;
    public static final int ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT_NO_FILE = BASE_ERROR + 13;
    public static final int ERROR_CODE_RESUME_DOWNLOAD_BY_MOBILENET = BASE_ERROR + 14;
}
