package com.pri.factorytest.OTG;

import android.content.Context;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class OTG extends PrizeBaseActivity {

    private TextView mTextView;
    private StorageManager mStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.otg);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mStorageManager.registerListener(mListener);
        mTextView = (TextView) findViewById(R.id.otg_hint);
        mTextView.setTextSize(20);
        mTextView.setText(getString(R.string.otg_insert));
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        //showOtgStorage();
    }


    private final StorageEventListener mListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            onVolumeStateChangedInternal(vol);
        }

        @Override
        public void onVolumeRecordChanged(VolumeRecord rec) {

        }

        @Override
        public void onVolumeForgotten(String fsUuid) {

        }

        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {

        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {

        }
    };

    private void onVolumeStateChangedInternal(VolumeInfo vol) {
        if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
            if (vol.getState() == VolumeInfo.STATE_CHECKING) {
                mTextView.setText(getString(R.string.otg_checking));
                confirmButtonNonEnable();
            }
            if ((vol.getState() == VolumeInfo.STATE_MOUNTED) || (vol.getState() == VolumeInfo.STATE_MOUNTED_READ_ONLY)) {
                showOtgStorage(vol);
            }
        }
    }

    private void showOtgStorage(VolumeInfo vol) {
        final VolumeRecord rec = mStorageManager.findRecordByUuid(vol.getFsUuid());
        final DiskInfo disk = vol.getDisk();

        if (rec.isSnoozed() && disk.isAdoptable()) {
            return;
        }
        if (disk.isAdoptable() && !rec.isInited()) {

        } else {
            final CharSequence title = disk.getDescription();
            mTextView.setText(title.toString());
            confirmButton();
            mButtonPass.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
