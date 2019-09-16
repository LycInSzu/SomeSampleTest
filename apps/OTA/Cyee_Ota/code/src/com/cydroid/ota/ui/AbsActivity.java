package com.cydroid.ota.ui;

import android.app.DialogFragment;     
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import cyee.app.CyeeActivity;
import cyee.changecolors.ChameleonColorManager;

/**
 * Created by kangjj on 15-6-15.
 */
public class AbsActivity extends CyeeActivity {
    private DialogFragment mCurrentDialogFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    protected void showFragmentDialog(int id) {
        synchronized (AbsActivity.class) {
            dismissFragementDialog();
            mCurrentDialogFragment = DialogFactory.newInstance(id);
            dialogShow(id);
        }
    }

    private void dismissFragementDialog() {
        synchronized (AbsActivity.class) {
            if (mCurrentDialogFragment != null) {
                mCurrentDialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    private void dialogShow(int id) {
        if (mCurrentDialogFragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(mCurrentDialogFragment, "dailog_" + id);
            ft.commitAllowingStateLoss();
        }
    }
}
