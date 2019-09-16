package com.cydroid.ota.logic.sync;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.cydroid.ota.Log;

import java.io.*;
import java.util.List;

/**
 * Created by borney on 5/21/15.
 */
public class QcomSystemReboot implements ISystemReboot {
    private static final String TAG = "QcomSystemReboot";

    protected QcomSystemReboot() {

    }

    @Override
    public void reboot(final Context context, final String fileName) {
//        new AsyncTask<String, Object, Boolean>() {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected void onPostExecute(Boolean result) {
//                super.onPostExecute(result);
//            }
//
//            @Override
//            protected Boolean doInBackground(String... params) {
//                return copyToDeltaFile(new File(fileName))
//                        && writeDeltaCommand()
//                        && rebootInstallDelta(context);
//            }
//
//        }.execute();
        rebootToInstall(context, fileName);
    }

    private boolean copyToDeltaFile(File srcFile) {
        File dstFile = new File("/cache/fota/ipth_package.bin");
        if (dstFile.exists()) {
            dstFile.delete();
        }
        InputStream in = null;
        OutputStream out = null;
        int cnt;
        byte[] buf = new byte[4096];
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
            while ((cnt = in.read(buf)) >= 0) {
                out.write(buf, 0, cnt);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "failed to copy delta file:" + e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private boolean writeDeltaCommand() {
        String filePath = "/cache/fota/ipth_config_dfs.txt";
        String command = "IP_PREVIOUS_UPDATE_IN_PROGRESS";
        boolean res = true;
        FileWriter mFileWriter = null;
        try {
            mFileWriter = new FileWriter(new File(filePath));
            mFileWriter.write(command);
        } catch (IOException e) {
            Log.e(TAG, "write delta command failed:" + e);
            res = false;
        } finally {
            try {
                mFileWriter.close();
            } catch (IOException e) {
            }
        }
        return res;
    }

    private boolean rebootInstallDelta(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.system.agent");
            intent.putExtra("para", "reboot,recovery");
            Intent eintent = new Intent(getExplicitIntent(context,intent));
            context.startService(eintent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "failed to reboot and install delta:" + e);
            return false;
        }
    }

    private void rebootToInstall(Context context, String fileName) {
        Log.d(TAG, "install file:" + fileName);
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.putExtra("packagefile", fileName);
        intent.putExtra("qrdupdate", true);
        context.sendBroadcast(intent);
    }

    private Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        Log.d(TAG, "packageName = " + packageName + "  className = " + className);
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}

