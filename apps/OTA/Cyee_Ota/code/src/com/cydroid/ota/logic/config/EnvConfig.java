package com.cydroid.ota.logic.config;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by borney on 14-9-11.
 */
public final class EnvConfig {
//    private static final String TAG = "EnvConfig";

    public static final String GIONEE_OTA_TEST_FLAGE_FILE_NAME = "/otatest1234567890";

    public static final String GIONEE_OTA_TEST_PACKAGE_FLAGE_TEST = "/test";
    public static final String GIONEE_OTA_TEST_PACKAGE_FLAGE_NORMAL = "/normal";
    /* Official version for Official server */
    public static final int NORMAL_ENVIRONMENT_NORMAL_VERSION = 1;
    /* test version for Official server */
    public static final int NORMAL_ENVIRONMENT_TEST_VERSION = 2;
    /* Official version for test server */
    public static final int TEST_ENVIRONMENT_NORMAL_VERSION = -1;
    /* test version for test server */
    public static final int TEST_ENVIRONMENT_TEST_VERSION = -2;

    private static final String GIONEE_OTA_TEST_ROOT = "/testroot";

    private static final String GIONEE_OTA_TEST_ROOT_TRUE = "/true";

    private static final String GIONEE_OTA_TEST_ROOT_FALSE = "/false";

    private static final String GIONEE_OTA_TEST_LOCALSCAN = "/testlocal";

    private static final String GIONEE_OTA_TEST_PRERELEASE = "/testprelease";

    private static final String GIONEE_OTA_TEST_BACKUP = "/testbackup";

    private static final String GIONEE_OTA_TEST_TRACE = "/testtrace";

    private static final String GIONEE_OTA_DEBUG = "/debug";

    private static final String GIONEE_OTA_TEST_INQUIRE = "/inquire";
    
    public static final String OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME = "/otachangeip1234567890";
    
    public static final String SERVER_IP_FLAGE_FILE_NAME = "server.txt";

    private static final boolean isTestEnv;
    private static final boolean isTestModel;
    private static final boolean isRoot;
    private static final boolean isRootTrue;
    private static final boolean isRootFalse;
    private static final boolean isTestLocalScan;
    private static final boolean isTestPreRelease;
    private static final boolean isTestBackup;
    private static final boolean isTestTrace;
    private static final boolean isDebug;
    private static final boolean isInquire;
    private static final boolean isChangeip;

    static {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        Log.d(TAG, "sdcardPath = " + sdcardPath);
        StringBuilder path = new StringBuilder(sdcardPath);
        path.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        path.append(GIONEE_OTA_TEST_ROOT);
        File testRoot = new File(path.toString());
        isRoot = testRoot.exists();
//        Log.d(TAG, "TEST_ROOT = " + path.toString() + " isRoot = " + testRoot.exists());

        String rootTrue = new StringBuilder(path.toString()).append(GIONEE_OTA_TEST_ROOT_TRUE).toString();
        File testRootTrue = new File(rootTrue);
        isRootTrue = testRootTrue.exists();
//        Log.d(TAG, "TEST_ROOT_TRUE = " + rootTrue.toString() + " isRootTrue = " + testRootTrue.exists());

        String rootFalse = new StringBuffer(path.toString()).append(GIONEE_OTA_TEST_ROOT_FALSE).toString();
        File testRootFalse = new File(rootFalse);
        isRootFalse = testRootFalse.exists();
//        Log.d(TAG, "TEST_ROOT_FALSE = " + rootFalse.toString() + " isRootFalse = " + testRootFalse.exists());

        StringBuilder localpath = new StringBuilder(sdcardPath);
        localpath.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        localpath.append(GIONEE_OTA_TEST_LOCALSCAN);
        File testLocalScan = new File(localpath.toString());
        isTestLocalScan = testLocalScan.exists();
//        Log.d(TAG, "TEST_LOCALSCAN = " + localpath.toString() + " isTestLocalScan = " + testLocalScan.exists());

        StringBuilder prelease = new StringBuilder(sdcardPath);
        prelease.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        prelease.append(GIONEE_OTA_TEST_PRERELEASE);
        File testPrelease = new File(prelease.toString());
        isTestPreRelease = testPrelease.exists();
//        Log.d(TAG, "TEST_PRERELEASE = " + prelease.toString() + " isTestPreRelease = " + testPrelease.exists());

        StringBuffer backup = new StringBuffer(sdcardPath);
        backup.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        backup.append(GIONEE_OTA_TEST_BACKUP);
        File testBackup = new File(backup.toString());
        isTestBackup = testBackup.exists();
//        Log.d(TAG, "TEST_BACKUP = " + prelease.toString() + " isTestBackup = " + testBackup.exists());

        StringBuffer trace = new StringBuffer(sdcardPath);
        trace.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        trace.append(GIONEE_OTA_TEST_TRACE);
        File testTrace = new File(trace.toString());
        isTestTrace = testTrace.exists();
//        Log.d(TAG, "TEST_TRACE = " + prelease.toString() + " isTestTrace = " + testTrace.exists());

        StringBuffer debug = new StringBuffer(sdcardPath);
        debug.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        debug.append(GIONEE_OTA_DEBUG);
        File debugfile = new File(debug.toString());
        isDebug = debugfile.exists();
//        Log.d(TAG, "TEST_DEBUG = " + prelease.toString() + " isDebug = " + debugfile.exists());

        StringBuffer inquire = new StringBuffer(sdcardPath);
        inquire.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        inquire.append(GIONEE_OTA_TEST_INQUIRE);
        File inquireFile = new File(inquire.toString());
        isInquire = inquireFile.exists();


        int environment = getEnvironment(sdcardPath);
        if (environment == TEST_ENVIRONMENT_NORMAL_VERSION
                || environment == TEST_ENVIRONMENT_TEST_VERSION) {
            isTestEnv = true;
        } else {
            isTestEnv = false;
        }
        System.out.println( ",isTestEnv = " + isTestEnv);
        if (environment == NORMAL_ENVIRONMENT_TEST_VERSION
                || environment == TEST_ENVIRONMENT_TEST_VERSION) {
            isTestModel = true;
        } else {
            isTestModel = false;
        }
        
        //add by cuijiuyu
        StringBuffer changip = new StringBuffer(sdcardPath);
        changip.append(OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
        File changipFile = new File(changip.toString());
        isChangeip = changipFile.exists();
        System.out.println("envconfig changip path = " + changip.toString() + ", isChangeip = " + isChangeip);

    }

    public static boolean isTestEnv() {
        return isTestEnv;
    }
    
    public static boolean isChangeip() {
        return isChangeip;
    }

    public static boolean isTestRoot() {
        return isRoot;
    }

    public static boolean isTestRootTrue() {
        return isRootTrue;
    }

    public static boolean isTestRootFalse() {
        return isRootFalse;
    }

    public static boolean isTestLocalScan() {
        return isTestLocalScan;
    }

    public static boolean isTestPrelease() {
        return isTestPreRelease;
    }

    public static  boolean isTestBackup(){
        return isTestBackup;
    }

    public static boolean isTestTrace() {
        return isTestTrace;
    }

    /*public static boolean isDebug() {
        return true;//isDebug;
    }*/

    public static boolean isTestModel() {
        return isTestModel;
    }

    public static boolean isInquire() {
        return isInquire;
    }

    static int getEnvironment(String sdcardPath) {
        StringBuilder rootPath = new StringBuilder();
        rootPath.append(sdcardPath);
        rootPath.append(GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        File dirOtaTest = new File(rootPath.toString());

        StringBuilder normalPath = new StringBuilder();
        normalPath.append(rootPath);
        normalPath.append(GIONEE_OTA_TEST_PACKAGE_FLAGE_NORMAL);
        File dirNormalTestVersion = new File(normalPath.toString());

        StringBuilder testVersionPath = new StringBuilder();
        testVersionPath.append(rootPath);
        testVersionPath.append(GIONEE_OTA_TEST_PACKAGE_FLAGE_TEST);
        File dirTestTestVersion = new File(testVersionPath.toString());

        int environment = NORMAL_ENVIRONMENT_NORMAL_VERSION;
        if (dirOtaTest.exists()) {
            if (dirNormalTestVersion.exists()) {
                environment = NORMAL_ENVIRONMENT_TEST_VERSION;
            } else if (dirTestTestVersion.exists()) {
                environment = TEST_ENVIRONMENT_TEST_VERSION;
            } else {
                environment = TEST_ENVIRONMENT_NORMAL_VERSION;
            }
        }

//        Log.d(TAG, "getEnvironment() environment = " + environment);
        return environment;
    }
    
}
