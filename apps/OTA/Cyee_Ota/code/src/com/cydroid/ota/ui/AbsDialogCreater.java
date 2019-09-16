package com.cydroid.ota.ui;

import android.app.DialogFragment;

import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.storage.IDataInvoker;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;

import cyee.app.CyeeAlertDialog;

/**
 * Created by borney on 7/16/15.
 */
public abstract class AbsDialogCreater extends DialogFragment {
    protected DialogFragment mDialogFragment;

    AbsDialogCreater(DialogFragment dialogFragment) {
        mDialogFragment = dialogFragment;
    }

    abstract CyeeAlertDialog creatDialog(int id);


    protected void writeRecoverUpgradeFlag(boolean isOnlineUpgrade) {
        IContextState contextState = SystemUpdateFactory
            .systemUpdate(mDialogFragment.getActivity()).getContextState();
        boolean isRoot = contextState.isRoot();
        IDataInvoker dataInvoker = SettingUpdateDataInvoker
            .getInstance(mDialogFragment.getActivity());
        if (isRoot) {
            dataInvoker.settingStorage().putBoolean(Key.Setting.KEY_UPGRADE_RECOVER_UPGRADE_FLAG, true);
        }
        if (isOnlineUpgrade) {
            dataInvoker.settingStorage().putBoolean(Key.Setting.KEY_UPGRADE_UPGRADE_ONLINE_FLAG, true);
        } else {
            dataInvoker.settingStorage().putBoolean(Key.Setting.KEY_UPGRADE_UPGRADE_ONLINE_FLAG, false);
        }

    }
}
