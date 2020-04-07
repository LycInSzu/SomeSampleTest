package com.cydroid.note.app.view;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.utils.ToastManager;

import java.io.File;

/**
 * Created by wuguangjie on 16-7-13.
 */
public class DownLoadCompleteBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            String mimetype = dManager.getMimeTypeForDownloadedFile(myDwonloadID);
            if ("application/octet-stream".equals(mimetype)
                    || (!TextUtils.isEmpty(mimetype) && mimetype.startsWith("image/"))) {
                // image
                new ToastManager(context).showToast(R.string.download_compelete);
            } else if ("application/vnd.android.package-archive".equals(mimetype)) {
                //apk
                try {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(myDwonloadID);
                    Cursor cursor = dManager.query(query);
                    if (cursor != null && cursor.moveToNext()) {
                        String filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        File file = new File(filePath);
                        if (file.exists()) {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            install.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
                            NoteAppImpl.getContext().startActivity(install);
                        }
                    }
                } catch (ActivityNotFoundException e) {
                }
            }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            startDownloadsActivity(context);
        }
    }

    private void startDownloadsActivity(Context context) {
        try {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
        }
    }

}
