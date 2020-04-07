package com.cydroid.note.app.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.gionee.framework.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wuguangjie on 16-7-13.
 */
public class APKUtils {

    private static final String TAG = "APKUtils";

    /**
     * 静默安装
     *
     * @param apkPath
     * @return
     */
    public static boolean installApk(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return false;
        }

        String[] args = {"pm", "install", "-r", apkPath};
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream inIs = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int read = -1;
            process = processBuilder.start();
            baos.write('/');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data); //NOSONAR
        } catch (Exception e) {
            Logger.printStackTrace(TAG, "install error", e);
        } finally {
            try {
                baos.close();
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                Logger.printStackTrace(TAG, "io error", e);
            }
            if (process != null) {
                process.destroy();
            }
        }
        if (result.contains("Success")) {
            return true;
        }
        return false;
    }

    /**
     * 调用下载管理器进行下载
     *
     * @param context
     * @param url
     * @param path
     * @return
     */
    public static long downloadApk(Context context, String url, String path) {
        if (null == context || TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return -1;
        }

        String apkName = url.substring(url.lastIndexOf("/") + 1);
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.PAUSED_WAITING_FOR_NETWORK);
        request.setDestinationInExternalPublicDir(path, apkName);
        return downloadManager.enqueue(request);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile.isConnected();
    }

    /**
     * 判断apk是否已安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
