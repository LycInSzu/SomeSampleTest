package com.cydroid.ota.ui;

import cyee.app.CyeeAlertDialog;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.LocalUpgradeActivity;
import com.cydroid.ota.ui.OtaSettingsActivity;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by borney on 7/16/15.
 */
@SuppressLint("ValidFragment")
public class OtaSettingCreater extends AbsDialogCreater {
    private static final String TAG = "OtaSettingCreater";

    OtaSettingCreater(DialogFragment dialogFragment) {
        super(dialogFragment);
    }

    @Override
    CyeeAlertDialog creatDialog(int id) {
        final OtaSettingsActivity otaSettingsActivity = (OtaSettingsActivity) mDialogFragment.getActivity();
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(otaSettingsActivity);
        switch (id) {
            case R.id.DIALOG_ID_UPGRADE_LOCALSELECTIONS:
                builder.setItems(new String[]{otaSettingsActivity.getString(R.string.gn_su_string_settings_local_upgrade_auto),
                                otaSettingsActivity.getString(R.string.gn_su_string_settings_local_upgrade_select)},
                        getItemClickListener(otaSettingsActivity));
                builder.setTitle(
                        R.string.gn_su_string_settings_local_upgrade);
                return builder.create();
            case R.id.DIALOG_ID_MOBILE_PERMISSION:
                builder.setTitle(R.string.gn_su_mobilenet_system_dialog_title).
                        setMessage(R.string.gn_su_mobilenet_system_dialog_content).
                        setNegativeButton(R.string.gn_su_mobilenet_system_dialog_negative_button, null);
                builder.setPositiveButton(R.string.gn_su_mobilenet_system_dialog_positive_button, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        IStorage settingStorage = SettingUpdateDataInvoker
                                                .getInstance(otaSettingsActivity)
                                                .settingStorage();
                                        settingStorage.putBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, true);
                                    }
                                }
                );
                return builder.create();
            default:
                return null;
        }
    }

    private DialogInterface.OnClickListener getItemClickListener(final OtaSettingsActivity activity) {

        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "which=" + which);
                Intent intent = new Intent();
                intent.setClass(activity, LocalUpgradeActivity.class);
                switch (which) {
                    case 0://auto scanner
                        intent.putExtra(Constants.AUTO_SCANNER, true);
                        break;
                    case 1://select yourself
                        intent.putExtra(Constants.AUTO_SCANNER, false);
                        break;
                    default:
                        break;
                }
                activity.startActivity(intent);
            }
        };

    }
}
