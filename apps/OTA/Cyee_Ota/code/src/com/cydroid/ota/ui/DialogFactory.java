package com.cydroid.ota.ui;

import cyee.app.CyeeAlertDialog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.cydroid.ota.ui.DetailsInfoActivity;
import com.cydroid.ota.ui.LocalUpgradeActivity;
import com.cydroid.ota.ui.OtaSettingsActivity;
import com.cydroid.ota.ui.SystemUpdateAnimActivity;
import com.cydroid.ota.Log;

/**
 * Created by borney on 7/16/15.
 */
public class DialogFactory {
    private static final String TAG = "DialogFactory";

    DialogFactory() {
        throw new RuntimeException("Stub!");
    }

    public static DialogFragment newInstance(int id) {
        SettingUpdateDialogProxy frag = new SettingUpdateDialogProxy();
        Bundle args = new Bundle();
        args.putInt("id", id);
        frag.setArguments(args);
        return frag;
    }

    @SuppressLint("ValidFragment")
    public static class SettingUpdateDialogProxy extends DialogFragment {
        private AbsDialogCreater dialogCreater;

        @Override
        public CyeeAlertDialog onCreateDialog(Bundle savedInstanceState) {
            final int id = getArguments().getInt("id");
            Log.d(TAG, "SettingUpdateDialogProxy onCreateDialog id = " + id);
            dialogCreater = getDiralogCreater(id);
            if (dialogCreater != null) {
                return dialogCreater.creatDialog(id);
            }
            return null;
        }

        private AbsDialogCreater getDiralogCreater(int id) {
            Activity activity = getActivity();
            Log.d(TAG, "AbsDialogCreater activity = " + activity.getClass().getSimpleName());
            if (activity instanceof OtaSettingsActivity) {
                return new OtaSettingCreater(this);
            } else if (activity instanceof LocalUpgradeActivity) {
                return new LocalUpgradeCreater(this);
            } else if (activity instanceof SystemUpdateAnimActivity) {
                return new SystemUpdateCreater(this);
            }
            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (dialogCreater != null) {
                dialogCreater.onCancel(dialog);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (dialogCreater != null) {
                dialogCreater.onPause();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (dialogCreater != null) {
                dialogCreater.onSaveInstanceState(outState);
            }
        }
    }
}
