package com.cydroid.ota.logic.sync;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;

import com.cydroid.ota.Log;

/**
 * Created by borney on 1/16/15.
 */
public final class SystemReboot implements ISystemReboot {
    private static final String TAG = "SystemReboot";
    private static final String COMMAND_PART2 = "COMMANDPART2";
    private static final String OTA_PATH_IN_RECOVERY_PRE = "/data/media/0/";

    private static final String REBOOT_METHOD = "reboot_method";
    private static final String NAND_PROJECT = "nand_project";
    private static final int NORMAL_REBOOT = 1;
    private static final int PMS_REBOOT = 2;

    protected SystemReboot() {
    }

    @Override
    public void reboot(Context context, String fileName) {
        InstallPkgThread installThread = new InstallPkgThread(context, fileName, "");
        installThread.start();
    }

    private static boolean setInstallInfo(Context context, String strPkgPath, String strTarVer) {
        Log.i(TAG, "onSetRebootRecoveryFlag");
		//Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-860 begin
        /*try {
            IBinder binder = ServiceManager.getService("GoogleOtaBinder");
            SystemUpdateBinder agent = SystemUpdateBinder.Stub.asInterface(binder);

            if (agent == null) {
                Log.e(TAG, "agent is null");
                return false;
            }

            if (isEmmcSupport()) {
                if (!agent.clearUpdateResult()) {
                    Log.e(TAG, "clearUpdateResult() false");
                    return false;
                }
            }

            Log.i(TAG, "setTargetVer");

            if (!agent.setRebootFlag()) {
                Log.e(TAG, "setRebootFlag() false");
                return false;
            }

            Log.i(TAG, "setRebootFlag");

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.cydroid.systemupdate.sysoper",
                "com.cydroid.systemupdate.sysoper.WriteCommandService"));
            Log.d(TAG, "recoveryPath = " + strPkgPath);
            intent.putExtra(COMMAND_PART2, strPkgPath);
            boolean isNand = !isEmmcSupport();
            intent.putExtra(NAND_PROJECT, isNand);
            context.startService(intent);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }*/
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.cydroid.systemupdate.sysoper",
            "com.cydroid.systemupdate.sysoper.WriteCommandService"));
        Log.d(TAG, "recoveryPath = " + strPkgPath);
        intent.putExtra(COMMAND_PART2, strPkgPath);
        boolean isNand = !isEmmcSupport();
        intent.putExtra(NAND_PROJECT, isNand);
        context.startService(intent);
        return true;
        //Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-860 end
    }

    protected static boolean isEmmcSupport() {
        boolean ret = false;

        ret = SystemProperties.get("ro.mtk_emmc_support").equals("1");

        Log.d(TAG, "isEmmcSupport: " + ret);

        return ret;
    }

    protected static boolean deviceEncrypted() {
        return "encrypted".equals(SystemProperties.get("ro.crypto.state"));
    }

    protected static boolean checkUpgradePackage() {
        return true;
    }

    protected static void notifyUserInstall() {
        return;
    }


    /**
     * create a new thread to systemRestart package.
     */
    static final class InstallPkgThread extends Thread {
        private Context mContext;
        private String mPkgPath;
        private String mTarVer;

        /**
         * @param strPkgPath
         * @param strTarVer
         */
        public InstallPkgThread(Context context, String strPkgPath, String strTarVer) {
            mContext = context;
            mPkgPath = strPkgPath;
            mTarVer = strTarVer;
        }

        /**
         * Main executing function of this thread.
         */
        public void run() {
            if (checkUpgradePackage() && setInstallInfo(mContext, mPkgPath, mTarVer)) {
                notifyUserInstall();
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.cydroid.systemupdate.sysoper",
                        "com.cydroid.systemupdate.sysoper.RebootRecoveryService"));
                mContext.startService(intent);
            } else {
                return;
            }
        }

    }
}
