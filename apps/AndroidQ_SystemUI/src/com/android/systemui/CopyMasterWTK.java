package com.android.systemui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import android.os.RemoteException;
import android.os.Environment;
import android.os.SystemProperties;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.android.systemui.R;

/**
 * preset mp3 mp4
 */
public class CopyMasterWTK {

    private static final String TAG = "CopyMasterWTK";
    private static final String IS_FIRST_BOOT = "is_first_boot";
    Context mContext;

    List<CopierItem> cpItemList = new ArrayList<CopierItem>();
    List<File> mSrcFiles = new ArrayList<File>();

    public CopyMasterWTK(Context context) {
        mContext = context;
    }

    public void startCopy() {
        ContentResolver cs = mContext.getContentResolver();
        if (Settings.Global.getInt(cs, IS_FIRST_BOOT, 1) == 1) {
            new Thread(() -> {
                //add by wangjian for YJSQ-1551 20181229 start
                defaultEnableLauncherNotificationDot();
                //add by wangjian for YJSQ-1551 20181229 end
                initCopierConfig();
                if (doAllItemCopy()) {
                    Settings.Global.putInt(cs, IS_FIRST_BOOT, 0);
                    Log.d(TAG, "WTK_Copy_Finished");
                }
            }).start();
        }
    }

    /**
     * init coperis config
     */
    private void initCopierConfig() {
        CopierItem copierItem = null;
        try {
            XmlPullParser mXmlPullParser;
            mXmlPullParser = mContext.getResources().getXml(R.xml.copier_config);

            int mEventType = mXmlPullParser.getEventType();
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = mXmlPullParser.getName();

                    if ("Copier".equals(name)) {
                        copierItem = new CopierItem();
                        copierItem.srcPath = mXmlPullParser.getAttributeValue(null, "srcPath");
                        copierItem.dstPath = mXmlPullParser.getAttributeValue(null, "dstPath");
                    }
                } else if (mEventType == XmlPullParser.END_TAG) {
                    String tagName = mXmlPullParser.getName();

                    if (copierItem != null && "Copier".equals(tagName)) {
                        cpItemList.add(copierItem);
                    }
                }
                mEventType = mXmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean doAllItemCopy() {
        boolean allFileCopyOK = true;
        for (CopierItem copier : cpItemList) {
            Log.d(TAG, "dstPath: " + copier.dstPath);
            Log.d(TAG, "srcPath: " + copier.srcPath);
            if (!doCopy(copier.srcPath, copier.dstPath)) {
                allFileCopyOK = false;
            }
        }

        return allFileCopyOK;
    }

    private boolean doCopy(String srcPath, String dstPath) {
        boolean allFileCopyOK = true;

        initSrcFileList(srcPath);
        for (File file : mSrcFiles) {
            String srcParentPath = file.getParent();
            srcParentPath = srcParentPath.replace(srcPath, dstPath);
            File srcParentFile = new File(srcParentPath);
            if (!srcParentFile.exists()) {
                srcParentFile.mkdir();
            }

            String srcRealPath = file.getPath();
            String dstRealPath = srcRealPath.replace(srcPath, dstPath);
            if (dstRealPath.endsWith(".mp4")) {
                dstRealPath = dstRealPath.replace("Music", "Movies");
            }
            if (!copyFile(srcRealPath, dstRealPath)) {
                allFileCopyOK = false;
            }
        }

        return allFileCopyOK;
    }

    private void initSrcFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    initSrcFileList(file.getPath());
                } else {
                    mSrcFiles.add(file);
                }
            }
        }
    }

    private boolean copyFile(String srcFile, String dstFile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        int fileSize = 0;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            int readLength;
            byte[] buffer = new byte[1024];
            while ((readLength = fis.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, readLength);
                fileSize += readLength;
            }
            if (fileSize == 0) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        MediaScannerConnection.scanFile(mContext.getApplicationContext(),
                new String[]{dstFile}, null, null);
        return true;
    }

    class CopierItem {
        String srcPath;
        String dstPath;
    }

    //add by wangjian for YJSQ-1551 20181229 start
    private void defaultEnableLauncherNotificationDot() {
        String listeners = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
        NotificationManager mNm = mContext.getSystemService(NotificationManager.class);
        ComponentName cn = new ComponentName("com.android.launcher3", "com.android.launcher3.notification.NotificationListener");
        mNm.setNotificationListenerAccessGranted(cn, true);
    }
    //add by wangjian for YJSQ-1551 20181229 end

}

