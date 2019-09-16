
package com.cydroid.ota.logic.config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.cydroid.ota.utils.StorageUtils;

import android.content.Context;
import com.cydroid.ota.Log;
import android.os.Environment;
/**
 * @author
 */
public class EnvConfigHelper {
    public static final String TAG = "EnvConfigHelper";

    public static int getEnvironment(Context context) {
    	String path = getTestPath(context);
        File dirOtaTest = new File(path
                + EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        File dirNormalTestVersion = new File(path
                + EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME
                + EnvConfig.GIONEE_OTA_TEST_PACKAGE_FLAGE_NORMAL);
        File dirTestTestVersion = new File(path
                + EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME
                + EnvConfig.GIONEE_OTA_TEST_PACKAGE_FLAGE_TEST);
        int environment = EnvConfig.NORMAL_ENVIRONMENT_NORMAL_VERSION;
        if (dirOtaTest.exists()) {
            if (dirNormalTestVersion.exists()) {
                environment = EnvConfig.NORMAL_ENVIRONMENT_TEST_VERSION;
            } else if (dirTestTestVersion.exists()) {
                environment = EnvConfig.TEST_ENVIRONMENT_TEST_VERSION;
            } else {
                environment = EnvConfig.TEST_ENVIRONMENT_NORMAL_VERSION;
            }
        }
        Log.d(TAG, "getEnvironment() environment = " + environment);
        return environment;
    }

    public static boolean isChangeip(Context context) {

    	File changeOtaDir = getOtachangipPath(context);    	
    	if (changeOtaDir.exists()) {// if change ip exists, must be change ip mode
    		return true;
    	}  else {// otherwise, normal mode
    		return false;
    	}
    }
    
    private static File getOtachangipPath(Context context){
    	String path =  Environment.getExternalStorageDirectory().getAbsolutePath();
    	File changeIpOtaDir = new File(path
    			+ EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
    	if (StorageUtils.isExSdcardInserted(context)) {    		
			path = StorageUtils.getSDCARDPATH();
	    	changeIpOtaDir = new File(path
	    			+ EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
	    	if (!changeIpOtaDir.exists()) {
	    		path = StorageUtils.getInternalStoragePath(context);;
    	    	changeIpOtaDir = new File(path
    	    			+ EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
	    	}
    	} else {
    		path = StorageUtils.getInternalStoragePath(context);;
	    	changeIpOtaDir = new File(path
	    			+ EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
    	}
    	return changeIpOtaDir;
    }
    
    private static String getTestPath(Context context){
    	String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    	File changeIpOtaDir = new File(path
    			+ EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
    	if (StorageUtils.isExSdcardInserted(context)) {    		
    		Log.d(TAG, "getTestPath() path1 = " + path);
			path = StorageUtils.getSDCARDPATH();
	    	changeIpOtaDir = new File(path
	    			+ EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
	    	Log.d(TAG, "getTestPath() path2 = " + path);
	    	if (!changeIpOtaDir.exists()) {
	    		path = StorageUtils.getInternalStoragePath(context);
    	    	changeIpOtaDir = new File(path
    	    			+ EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
    	    	Log.d(TAG, "getTestPath() path3 = " + path);
	    	}
    	} else {
    		path = StorageUtils.getInternalStoragePath(context);
	    	changeIpOtaDir = new File(path
	    			+ EnvConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
    	}
    	Log.d(TAG, "getTestPath()  path= " + path);
    	return path;
    }
    
    public static String initHttpCommunicatorHost(Context context) {
    	String HOST = NetConfig.NORMAL_HOST;
    	File changeIpOtaDir = getOtachangipPath(context);
    	String otaServerIp = "http://" + readServerTxtFile(changeIpOtaDir);
        if (null != otaServerIp && otaServerIp.trim().length() > 0) {
            HOST = otaServerIp;
        }
    	return HOST;
    }
    
    private static String readServerTxtFile(File parentDir) {
    	
    	File[] files = parentDir.listFiles();
    	FileReader fr = null;
    	BufferedReader br = null;
    	try{
    	       for (File file : files) {
    	            if (file.getName().equals(EnvConfig.SERVER_IP_FLAGE_FILE_NAME)) {
    	                fr = new FileReader(file);
    	                br = new BufferedReader(fr);
    	                String otaServerIp = br.readLine();
    	                return otaServerIp;
    	            }
    	        }
    	} catch (Exception e){
    	    e.printStackTrace();
    	} finally {
    	    try {
    	           if (null != br) {
    	                br.close();
    	            }
    	            if (null != fr) {
    	                fr.close();
    	            }
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	}
    	return NetConfig.NORMAL_HOST;
    }
    
    public static boolean isTestEnv(Context context) {
        int environment = getEnvironment(context);
        if (environment == EnvConfig.TEST_ENVIRONMENT_NORMAL_VERSION
                || environment == EnvConfig.TEST_ENVIRONMENT_TEST_VERSION) {
            return  true;
        } else {
            return false;
        }
    }
    
    public static boolean isTestModel(Context context) {
        int environment = getEnvironment(context);
        if (environment == EnvConfig.NORMAL_ENVIRONMENT_TEST_VERSION
                || environment == EnvConfig.TEST_ENVIRONMENT_TEST_VERSION) {
            return  true;
        } else {
            return false;
        }
    }
}
