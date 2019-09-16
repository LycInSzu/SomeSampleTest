package com.cydroid.ota.ui;

import cyee.app.CyeeAlertDialog;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.provider.Settings;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.R;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by borney on 7/16/15.
 */
@SuppressLint("ValidFragment")
public class SystemUpdateCreater extends AbsDialogCreater {
    private MyCountdown mMyCountdown;

    SystemUpdateCreater(DialogFragment dialogFragment) {
        super(dialogFragment);
    }

    @Override
    CyeeAlertDialog creatDialog(int id) {
        final SystemUpdateAnimActivity systemUpdateAnimActivity = (SystemUpdateAnimActivity) mDialogFragment.getActivity();
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(systemUpdateAnimActivity);
        switch (id) {
            case R.id.DIALOG_ID_AUTODOWNLOAD_HINT:
                builder.setTitle(R.string.gn_su_autodownload_dialog_title);
				//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 begin
                builder.setMessage(SystemPropertiesUtils.isDPFlag()
                        ? R.string.gn_su_autodownload_dialog_message_dp : R.string.gn_su_autodownload_dialog_message);
				//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 end
                builder.setPositiveButton(R.string.gn_su_autodownload_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IStorage wlanAutoStorage = SettingUpdateDataInvoker.getInstance(systemUpdateAnimActivity)
                                .wlanAutoStorage();
                        wlanAutoStorage.putBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, true);
                    }
                });
                builder.setNegativeButton(R.string.gn_su_autodownload_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IStorage wlanAutoStorage = SettingUpdateDataInvoker.getInstance(systemUpdateAnimActivity)
                                .wlanAutoStorage();
                        wlanAutoStorage.putBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, true);
                        Intent intent = new Intent();
                        intent.setClass(systemUpdateAnimActivity, OtaSettingsActivity.class);
                        intent.putExtra("fromguide", true);
                        systemUpdateAnimActivity.startActivity(intent);
                    }
                });
                return builder.create();

            case R.id.DIALOG_ID_MOBILENET_HINT:
				//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 begin
                builder.setTitle(R.string.gn_su_mobilenet_download_dialog_title).setMessage(SystemPropertiesUtils.isDPFlag()
                        ? R.string.gn_su_mobilenet_download_dialog_message_dp : R.string.gn_su_mobilenet_download_dialog_message);
                builder.setPositiveButton(SystemPropertiesUtils.isDPFlag()
                        ? R.string.gn_su_mobilenet_download_dialog_positive : R.string.gn_su_mobilenet_download_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        systemUpdateAnimActivity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
				//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 end
                builder.setNegativeButton(R.string.gn_su_mobilenet_download_dialog_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                systemUpdateAnimActivity.requestDownload();
                            }
                        }
                );
                return builder.create();
            case R.id.DIALOG_ID_REBOOT_TO_UPGRADE:
                builder.setTitle(R.string.gn_su_string_restart_dialog_title).setMessage(mDialogFragment.getString(R.string.gn_su_string_restart_dialog_message, 5));
                builder.setPositiveButton(R.string.gn_su_string_restart_dialog_nowBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCountTime();
                        mDialogFragment.dismiss();
                        writeRecoverUpgradeFlag(true);
                        SystemUpdateFactory.systemUpdate(systemUpdateAnimActivity).installUpdate().install();
                    }
                });
                builder.setNegativeButton(R.string.gn_su_string_restart_dialog_afterBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCountTime();
                        mDialogFragment.dismiss();
                    }
                });

                CyeeAlertDialog dialog = builder.create();
                mMyCountdown = new MyCountdown(Constants.RESTART_DIALOG_COUNTDODWN_TIME, Constants.RESTART_DIALOG_INTERVAL, dialog);
                mMyCountdown.start();
                return dialog;


            case R.id.DIALOG_ID_UPDATE_LOW_POWER_HINT:
                builder.setTitle(R.string.gn_su_battery_title)
                        .setMessage(R.string.gn_su_battery_summery2)
                        .setNegativeButton(R.string.gn_su_dialog_positive_button, null)
                        .create();
                return builder.create();
// Gionee zhouhuiquan 2017-04-01 add for 101983 begin
	    case R.id.DIALOG_ID_UPDATE_TOO_LOW_POWER_HINT:
                builder.setTitle(R.string.gn_su_battery_title)
                        .setMessage(R.string.gn_su_battery_summery3)
                        .setNegativeButton(R.string.gn_su_dialog_positive_button, null)
                        .create();
                return builder.create();
// Gionee zhouhuiquan 2017-04-01 add for 101983 end
            case R.id.DIALOG_ID_MOBILE_PERMISSION:
                builder.setTitle(R.string.gn_su_mobilenet_system_dialog_title).
                        setMessage(R.string.gn_su_mobilenet_system_dialog_content).
                        setNegativeButton(R.string.gn_su_mobilenet_system_dialog_negative_button, null);
                builder.setPositiveButton(R.string.gn_su_mobilenet_system_dialog_positive_button, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(systemUpdateAnimActivity)
                                                .settingStorage();
                                        settingStorage.putBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, true);
                                    }
                                }
                );
                return builder.create();
            case R.id.DIALOG_ID_PRERELEASE_CANCEL:
                builder.setTitle(R.string.gn_su_prelease_cancel_title)
                        .setMessage(R.string.gn_su_prelease_cancel_content)
                        .setNegativeButton(R.string.gn_su_dialog_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                systemUpdateAnimActivity.strartCheck();
                            }
                        })
                        .create();
                return builder.create();

            default:
                return null;
        }

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        final int id = mDialogFragment.getArguments().getInt("id");
        switch (id) {
            case R.id.DIALOG_ID_REBOOT_TO_UPGRADE:
                cancelCountTime();
                break;
            case R.id.DIALOG_ID_AUTODOWNLOAD_HINT:
                IStorage wlanAutoStorage = SettingUpdateDataInvoker.getInstance(mDialogFragment.getActivity())
                        .wlanAutoStorage();
                wlanAutoStorage.putBoolean(
                        Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        final int id = mDialogFragment.getArguments().getInt("id");
        switch (id) {
            case R.id.DIALOG_ID_REBOOT_TO_UPGRADE:
                cancelCountTime();
                mDialogFragment.dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    private void cancelCountTime() {
        if (mMyCountdown != null) {
            mMyCountdown.cancel();
        }
    }

    class MyCountdown extends CountDownTimer {

        private int minute = 5;
        private CyeeAlertDialog dialog;

        public MyCountdown(long millisInFuture, long countDownInterval, CyeeAlertDialog dialog) {
            super(millisInFuture, countDownInterval);
            this.dialog = dialog;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mDialogFragment.isAdded()) {
                dialog.setMessage(mDialogFragment.getString(
                        R.string.gn_su_string_restart_dialog_message, minute));
                minute--;
            }
        }

        @Override
        public void onFinish() {
            mDialogFragment.dismissAllowingStateLoss();
            writeRecoverUpgradeFlag(true);
            SystemUpdateFactory.systemUpdate(mDialogFragment.getActivity()).installUpdate().install();
        }
    }
}
