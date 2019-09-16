package com.cydroid.ota.ui;

import cyee.app.CyeeAlertDialog;
import cyee.app.CyeeProgressDialog;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.cydroid.ota.Log;
import com.cydroid.ota.R;

/**
 * Created by borney on 7/16/15.
 */
@SuppressLint("ValidFragment")
public class LocalUpgradeCreater extends AbsDialogCreater {
    private static final String TAG = "LocalUpgradeDialog";
    private MyCoundDown mMyCoundDown;

    LocalUpgradeCreater(DialogFragment dialogFragment) {
        super(dialogFragment);
    }

    @Override
    CyeeAlertDialog creatDialog(int id) {
        Log.d(TAG, "creatDialog() id = " + id);
        final LocalUpgradeActivity localUpgradeActivity = (LocalUpgradeActivity) mDialogFragment.getActivity();
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(localUpgradeActivity);
        switch (id) {
            case R.id.DIALOG_ID_LOCAL_RESTART_UPGRADE:
                builder.setTitle(R.string.gn_su_string_restart_dialog_title)
                        .setMessage(mDialogFragment.getString(R.string.gn_su_string_restart_dialog_message, 5))
                        .setPositiveButton(
                                R.string.gn_su_string_restart_dialog_nowBtn,
                                handleRestartNow(localUpgradeActivity))
                        .setNegativeButton(
                                R.string.gn_su_string_restart_dialog_afterBtn,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        cancelCountTime();
                                        mDialogFragment.dismiss();
                                    }
                                });
                CyeeAlertDialog dialog = builder.create();
                //Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-859 begin
                mMyCoundDown = new MyCoundDown(5000, 1000, dialog, localUpgradeActivity);
              //Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-859 end
                mMyCoundDown.start();
                return dialog;
            case R.id.DIALOG_ID_LOCAL_FILE_VERIFY:
                CyeeProgressDialog progressDialog = new CyeeProgressDialog(localUpgradeActivity);
                progressDialog.setMessage(mDialogFragment.getString(R.string.gn_su_string_file_verifing));
                progressDialog.setProgressStyle(CyeeProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                });
                return progressDialog;
            case R.id.DIALOG_ID_FILE_MD5_CHECK_ERR:
                String title = localUpgradeActivity.getCurrentUpgradeFileName();
                title = TextUtils.isEmpty(title) ? mDialogFragment.getArguments().getString("title") : title;
                builder.setTitle(title);
                builder.setMessage(String.format(mDialogFragment.getString(
                                        R.string.gn_su_string_local_file_verify_failed),
                                title)
                );
                builder.setNegativeButton(android.R.string.ok, null);
                return builder.create();
            case R.id.DIALOG_ID_POWER_LOW:
                builder.setTitle(R.string.gn_su_battery_title)
                        .setMessage(R.string.gn_su_battery_summery2)
                        .setNegativeButton(R.string.gn_su_dialog_positive_button, null)
                        .create();
                return builder.create();
// Gionee zhouhuiquan 2017-04-01 add for 101983 begin
            case R.id.DIALOG_ID_POWER_TOO_LOW:
                builder.setTitle(R.string.gn_su_battery_title)
                        .setMessage(R.string.gn_su_battery_summery3)
                        .setNegativeButton(R.string.gn_su_dialog_positive_button, null)
                        .create();
                return builder.create();
// Gionee zhouhuiquan 2017-04-01 add for 101983 end
            default:
                return null;
        }
    }

    private DialogInterface.OnClickListener handleRestartNow(
            final LocalUpgradeActivity upgradeActivity) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelCountTime();
                mDialogFragment.dismiss();
                writeRecoverUpgradeFlag(false);
                upgradeActivity.systemRestartIm();
            }


        };
    }

    private void cancelCountTime() {
        if (mMyCoundDown != null) {
            mMyCoundDown.cancel();
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        final int id = mDialogFragment.getArguments().getInt("id");
        switch (id) {
            case R.id.DIALOG_ID_LOCAL_RESTART_UPGRADE:
                cancelCountTime();
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
            case R.id.DIALOG_ID_LOCAL_RESTART_UPGRADE:
                cancelCountTime();
                mDialogFragment.dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final int id = mDialogFragment.getArguments().getInt("id");
        switch (id) {
            case R.id.DIALOG_ID_FILE_MD5_CHECK_ERR:
                final LocalUpgradeActivity localUpgradeActivity = (LocalUpgradeActivity) mDialogFragment.getActivity();
                mDialogFragment.getArguments().putString("title", localUpgradeActivity.getCurrentUpgradeFileName());
                break;
            default:
                break;
        }
    }

    class MyCoundDown extends CountDownTimer {

        private int minute = 5;
        private CyeeAlertDialog dialog;
        private LocalUpgradeActivity upgradeActivity;

        public MyCoundDown(long millisInFuture, long countDownInterval,
                           CyeeAlertDialog dialog, LocalUpgradeActivity activity) {
            super(millisInFuture, countDownInterval);
            this.dialog = dialog;
            upgradeActivity = activity;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mDialogFragment.isAdded()) {
                dialog.setMessage(mDialogFragment.getString(R.string.gn_su_string_restart_dialog_message, minute));
                minute--;
            }
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish");
            mDialogFragment.dismissAllowingStateLoss();
            writeRecoverUpgradeFlag(false);
            upgradeActivity.systemRestartIm();
        }
    }
}
