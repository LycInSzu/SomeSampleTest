package com.cydroid.note.web;

import android.app.DownloadManager;
import android.net.Uri;

import com.cydroid.note.common.Constants;
import com.cydroid.note.common.PlatformUtil;

public class OnlineImageDownloadHandler {

    private static final String DOWNLOAD_ONLINE_PIC_DIR;

    static {
        String[] rootArray = Constants.ROOT_FILE.getPath().split("/");
        String picPath = rootArray[rootArray.length - 1];
        DOWNLOAD_ONLINE_PIC_DIR = picPath + Constants.NOTE;
    }

    public static void download(DownloadManager downloadManager, String url,
                                String fileName, String title) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir(DOWNLOAD_ONLINE_PIC_DIR, fileName);
            request.setTitle(title);
            if(!PlatformUtil.isGioneeDevice()) {
                request.setMimeType("image/*");
                request.allowScanningByMediaScanner();
            }
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            downloadManager.enqueue(request);
        } catch (Exception e) {
        }
    }
}
