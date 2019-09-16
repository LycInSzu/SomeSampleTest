package com.cydroid.ota.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.cydroid.ota.Log;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.logic.config.NetConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by liuyanfeng on 15-4-21.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    private static final String GN_SYSTEM_UPDATE_OTA = "system_update_ota_new.zip";
	//Chenyee <CY_Bug> <xuyongji> <20171204> modify for SW17W16A-2184 begin
    private static final int MIN_STORAGE_SPACE = 250 * 1024 * 1024;
	//Chenyee <CY_Bug> <xuyongji> <20171204> modify for SW17W16A-2184 end
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    public static String getDownloadFileName(long fileTotalSize, Context context) {
        Log.d(TAG, Log.getFunctionName());

        if (isInternalMemoryEnough(fileTotalSize, context)) {
            return getInternalDownloadFileName(context);
        }

        if (StorageUtils.isExSdcardInserted(context)) {
            if (isExternalMemoryEnough(fileTotalSize, context)) {
                return getExternalDownloadFileName();
            }

        }
        return null;
    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isMemoryEnoughForDownload(String fileName, long fileTotalSize) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        String filePath = fileName.substring(0, fileName.lastIndexOf(File.separator));

        if (getAvailableMemorySize(filePath) > fileTotalSize - getDownloadFileSize(fileName)) {
            return true;
        }
        return false;
    }

    public static boolean checkFinished(long fileTotalSize, String fileName) {
        long curLen = getDownloadFileSize(fileName);
        if (curLen > fileTotalSize) {
            deleteFileIfExist(fileName);
            return false;
        }
        if (fileTotalSize == curLen) {
            return true;
        }
        return false;
    }

    public static boolean verifyFileByMD5(String md5, String fileName) {
        if (TextUtils.isEmpty(md5) || TextUtils.isEmpty(fileName)) {
            return false;
        }
        File file = new File(fileName);
        try {
            String fileMd5 = getFileMd5(file);
            if (md5.equals(fileMd5)) {
                Log.d(TAG, "verifyFileByMd5() successful");
                return true;
            } /*else {
                Log.d(TAG, "verifyFileByMd5() failed filename = "
                        + file.getPath() + " ,fileMd5 = " + fileMd5
                        + " ,real md5 = " + md5);
            }*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void deleteFileIfExist(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "delete failed as fileName is empty!");
            return;
        }
        File file = new File(fileName);
        if (file.exists()) {
            boolean isDelete = file.delete();
            //Log.d(TAG, "delete file:" + fileName + " result:" + isDelete);
            Log.d(TAG, "deleteFileIfExist result:" + isDelete);
        } /*else {
            Log.d(TAG, "file not exist:" + fileName);
        }*/
    }

    public static String getAppDownloadPath(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(StorageUtils.getInternalStoragePath(context));
        builder.append(File.separator);
        builder.append(Constants.APP_DOWNLOAD_PATH);
        return builder.toString();
    }

    private static String getFileMd5(File file) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        byte[] digest = null;
        FileInputStream in = null;

        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            Log.d(TAG, "getFileMd5() file = " + file.getPath()
                    + " , file.exists() = " + file.exists()
                    + ", file.canRead() = " + file.canRead()
                    + " ,file.canWrite() = " + file.canWrite()
                    + " , file.length() =" + file.length());
            in = new FileInputStream(file);
            int byteCount;

            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }

            digest = digester.digest();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (Exception cause) {
            throw new RuntimeException("Unable to compute MD5 of \"" + file
                    + "\"", cause);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "closeInputStream() e = " + e.toString());
                }
            }
        }
        return (digest == null) ? "" : byteArrayToHexString(digest);
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toHexString((b >> 4) & 0xf));
            result.append(Integer.toHexString(b & 0xf));
        }
        return result.toString();
    }

    private static long getDownloadFileSize(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    private static String getInternalDownloadFileName(Context context) {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(StorageUtils.getInternalStoragePath(context));
        fileNameBuilder.append(File.separator);
        fileNameBuilder.append(GN_SYSTEM_UPDATE_OTA);
        return fileNameBuilder.toString();
    }

    private static String getExternalDownloadFileName() {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(StorageUtils.getSDCARDPATH());
        fileNameBuilder.append(File.separator);
        fileNameBuilder.append(GN_SYSTEM_UPDATE_OTA);
        return fileNameBuilder.toString();
    }

    private static long getInternalDownloadFileSize(Context context) {
        String downloadFileName = getInternalDownloadFileName(context);
        File file = new File(downloadFileName);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    private static long getExternalDownloadFileSize(Context context) {
        String downloadFileName = getExternalDownloadFileName();
        File file = new File(downloadFileName);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    private static boolean isExternalMemoryEnough(long fileTotalSize, Context context) {
        long externalFileSize = getExternalDownloadFileSize(context);
        if (getAvailableMemorySize(StorageUtils.getSDCARDPATH()) > fileTotalSize - externalFileSize) {
            return true;
        }
        return false;
    }

    private static boolean isInternalMemoryEnough(long fileTotalSize, Context context) {
        long internalFileSize = getInternalDownloadFileSize(context);
        if (getAvailableMemorySize(StorageUtils.getInternalStoragePath(context)) > fileTotalSize - internalFileSize) {
            return true;
        }
        return false;
    }

    private static long getAvailableMemorySize(String filePath) {
        Log.d(TAG, "filepath :" + filePath);
        StatFs stat = new StatFs(filePath);
		//Chenyee <CY_Bug> <xuyongji> <20180331> modify for CSW1705A-2458 begin
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long lowBytes = stat.getTotalBytes() / 10;
        if (isExternalStoragePath(filePath)) {
            lowBytes = MIN_STORAGE_SPACE;
        }
        return availableBlocks * blockSize - lowBytes;
		//Chenyee <CY_Bug> <xuyongji> <20180331> modify for CSW1705A-2458 end
    }


    public static List<String> getFileListPaths(String rootPath) {
        File file = new File(rootPath);
        List<String> result = new ArrayList<String>();
        if (file != null && file.exists()&&file.isDirectory()) {
            List<File> files = Arrays.asList(file.listFiles());
            Collections.sort(files, comparator);
            for (File f : files) {
                if (!f.getName().startsWith(".")) {
                    result.add(f.getAbsolutePath());
                }
            }
        }
        return result;
    }

    public static boolean isMountedPath(String path, Context context) {
        List<String> rootPaths = StorageUtils.getAllMountedStorageVolumesPath(context);
            for (String mountedPath : rootPaths) {
                if (path.startsWith(mountedPath)) {
                    return true;
                }
            }
        return false;
    }

    public static boolean isInternalStoragePath(String filePath, Context context) {
        if (StorageUtils.getInternalStoragePath(context).equals(filePath)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStoragePath(String filePath) {
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        if (externalPath.equals(filePath)) {
            return true;
        }
        return false;
    }

    private static Comparator<File> comparator = new Comparator<File>() {
        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() == true
                        && f2.isDirectory() == true) {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                } else {
                    if ((f1.isDirectory() && !f2.isDirectory()) == true) {
                        return -1;
                    } else if ((f2.isDirectory() && !f1.isDirectory())
                            == true) {
                        return 1;
                    } else {
                        return f1.getName()
                                .compareToIgnoreCase(f2.getName());
                    }
                }
            }
        }
    };
    
  
  private static File getOtachangipPath(Context context){
      String path = Environment.getExternalStorageDirectory().getAbsolutePath();
      File changeIpOtaDir = new File(path
              + EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
      if (StorageUtils.isExSdcardInserted(context)) {         
          path = StorageUtils.getSDCARDPATH();
          changeIpOtaDir = new File(path
                  + EnvConfig.OVERSEA_OTA_CHANGEIP_FLAGE_FILE_NAME);
          if (!changeIpOtaDir.exists()) {
              path = StorageUtils.getInternalStoragePath(context);
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
      return NetConfig.TEST_HOST;
  }
}
