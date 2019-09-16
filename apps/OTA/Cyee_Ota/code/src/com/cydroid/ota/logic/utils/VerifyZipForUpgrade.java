package com.cydroid.ota.logic.utils;

import com.cydroid.ota.Log;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by liuyanfeng on 15-6-9.
 */
public class VerifyZipForUpgrade {
    private static final String TAG = "VerifyZipForUpgrade";

    public static boolean verifyZipForUpgrade(String fileName, boolean isRoot) {
        Log.d(TAG, "verifyZipForUpgrade = " + fileName + "isRoot :" + isRoot);
        File file = null;

        if (!isAsci(fileName)) {
            return false;
        }

        file = new File(fileName);
        if (!file.exists()) {
            return false;
        }
        Log.d(TAG, "verifyZipForUpgrade begin");
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
           /* ZipEntry propEntry = zipFile.getEntry("system/build.prop");
            if (propEntry != null) {
                return checkFullPackage(zipFile, propEntry, isRoot);
            } else if (!isRoot) {
                return checkIncPackage(zipFile);
            } else {
                return false;
            }*/
			//Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-305 begin
            if (!isRoot) {
                return checkIncPackage(zipFile);
            } else {
                return false;
            }
			//Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-305 end

        } catch (ZipException ze) {
            ze.printStackTrace();
            Log.e(TAG, "checkUpdatePackage() " + ze.toString());
        } catch (IOException ie) {
            ie.printStackTrace();
            Log.e(TAG, "checkUpdatePackage() " + ie.toString());
        } finally {
            closeZipFile(zipFile);
        }
        Log.d(TAG, "checkUpdatePackage end");
        return false;

    }

    private static boolean isAsci(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 126 || chars[i] < 32) {
                return false;
            }
        }

        return true;
    }

	//Chenyee <CY_Bug> <xuyongji> <20171115> remove for SW17W16A-1269 begin
    /*private static boolean checkFullPackage(ZipFile zipFile, ZipEntry propEntry, boolean isRoot) throws IOException {
        String line = null;
        String buildTime = "";
        String projectID = SystemPropertiesUtils.getModel().replaceAll(" ","");

        BufferedReader bReader = new BufferedReader(new InputStreamReader(
            zipFile.getInputStream(propEntry),
            HTTP.UTF_8));
        try {
            while ((line = bReader.readLine()) != null) {
                if (line.contains("ro.build.date.utc")) {
                    buildTime = line.substring(line.indexOf("=") + 1);
                    Log.d(TAG, "checkFullPackage() buildTime = " + buildTime);
                }
                if (line.contains("ro.product.model")) {
                    String model = line.substring(line.indexOf("=") + 1).replaceAll(" ","");
                    Log.d(TAG, "projectID :" + projectID + "model : " + model);
                    if (!projectID.equals(model)) {
                        Log.d(TAG, "error : this is not the same project ");
                        return false;
                    }
                    return checkBuildTimeIsLater(buildTime, isRoot);
                }
            }
        } finally {
            if (bReader != null) {
                bReader.close();
            }
        }

        Log.e(TAG, "checkFullPackage() build.prop not contains ro.gn.gnproductid");
        return false;
    }*/
	//Chenyee <CY_Bug> <xuyongji> <20171115> remove for SW17W16A-1269 end

	//Chenyee <CY_Bug> <xuyongji> <20171115> modify for SW17W16A-1269 begin
    private static boolean checkBuildTimeIsLater(String buildTime) throws IOException {
        try {
            Integer time1 = Integer.valueOf(buildTime.trim());
            Integer time2 = Integer.valueOf(SystemPropertiesUtils.getBuildTime().trim());
            if (time1 >= time2) {
                return true;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "checkFullPackage () " + e.toString());
        }
        return false;
    }

    private static boolean checkIncPackage(ZipFile zipFile) throws IOException {
        ZipEntry scriptEntry = zipFile.getEntry("META-INF/com/google/android/updater-script");
        String NAME_DATE_NUM = "(!less_than_int(";
        String NAME_DEV_NUM = "getprop(\"ro.product.device\") == \"";

        if (scriptEntry == null) {
            Log.e(TAG, "checkIncPackage() updater-script is null");
            return false;
        }
        
        if (isSameZipFile(zipFile)){
            Log.e(TAG, "is already an upgraded version");
            return false;
        }
        
		//Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 begin
        String fingerPrint = SystemPropertiesUtils.getfingerPrint();
        int type = getOtaType(zipFile);
		//Check the zipfile type
        if (type == -1) {
        	return false;
        }

        BufferedReader bReader = new BufferedReader(new InputStreamReader(
            zipFile.getInputStream(scriptEntry),
            HTTP.UTF_8));
        String line = null;
        String devName = null;
        String buildTime = null;

        try {
            while ((line = bReader.readLine()) != null) {
				//for diff package, check the fingerprint
            	if (type == 0) {            	
	                if (line.indexOf(fingerPrint) != -1) {
	                    return true;
	                }
            	} 
            	if (devName == null) {
            		devName = extractVerInfo(line, NAME_DEV_NUM, '"');
            	}
            	if (buildTime == null) {
            		buildTime = extractVerInfo(line, NAME_DATE_NUM, ',');
            	}
				//for full package, check the device name and build time
            	if (devName != null && buildTime != null && type == 1) {
	            	if (!devName.equals(SystemPropertiesUtils.getDeviceName().trim())) {
	                	return false;
	                }
	            	return checkBuildTimeIsLater(buildTime);
            	}
            }
        } finally {
            if (bReader != null) {
                bReader.close();
            }
        }
		//Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 end

        return false;
    }
    
    private static String extractVerInfo(String strSrc, String strTag, char tag) {
        int nIndexStart = strSrc.indexOf(strTag);
        if (nIndexStart != -1) {
            strSrc = strSrc.substring(nIndexStart + strTag.length());
            int nIndexEnd = strSrc.indexOf(tag);
            if (nIndexEnd != -1) {
                return strSrc.substring(0, nIndexEnd);
            }
        }
        return null;
    }
	//Chenyee <CY_Bug> <xuyongji> <20171115> modify for SW17W16A-1269 end
    
	//Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 begin
    private static int getOtaType(ZipFile zipFile) throws IOException{
    	ZipEntry typeEntry = zipFile.getEntry("type.txt");
    	if (typeEntry == null) {
    		return -1;
    	}
    	BufferedReader bReader = new BufferedReader(new InputStreamReader(
                zipFile.getInputStream(typeEntry),
                HTTP.UTF_8));
    	try {	    	
	    	String line = bReader.readLine();
	    	if (line != null) {
                return Integer.parseInt(line);
            }    		
    	} finally {
        	if (bReader != null) {
                bReader.close();
        	}
        }    	
    	return -1;
    }
	//Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 end

    private static void closeZipFile(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isSameZipFile(ZipFile zipFile) throws IOException{
        Log.d(TAG, "isSameZipFile begin");
        ZipEntry metadataEntry = zipFile.getEntry("META-INF/com/android/metadata");

        if (metadataEntry == null) {
            Log.e(TAG, "checkIncPackage() metadata is null");
            return false;
        }

        BufferedReader bReader = new BufferedReader(new InputStreamReader(
                zipFile.getInputStream(metadataEntry),
                HTTP.UTF_8));
        String line = null;
        String zipTime = null;
        String buildTime = SystemPropertiesUtils.getBuildTime().toString().replaceAll(" ","");

        while ((line = bReader.readLine()) != null) {
            if (line.contains("post-timestamp")) {
                zipTime = line.substring(line.indexOf("=") + 1).toString().replaceAll(" ","");
                Log.d(TAG, "isSameZipFile() zipTime = " + zipTime + ",buildTime ="+ buildTime);
                if (zipTime.equals(buildTime)){
                    return true;
                }
            }
        }
        
        return false;
    }

}
