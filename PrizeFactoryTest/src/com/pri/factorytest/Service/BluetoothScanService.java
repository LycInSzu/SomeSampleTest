package com.pri.factorytest.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import com.pri.factorytest.FactoryTestApplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.pri.factorytest.BlueTooth.DeviceInfo;

import android.text.format.Time;

public class BluetoothScanService extends Service {
    private static final String TAG = "BluetoothScanService";
    Context mContext;
    FactoryTestApplication mApp;

    /*bluetooth*/
    BluetoothAdapter mBluetoothAdapter = null;
    List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
    Time time = new Time();
    long startTime;
    long endTime;
    boolean recordTime = false;
    private boolean bluetoothScanFinish = false;
    /*bluetooth*/

    public class ScanServiceBinder extends Binder {
        public BluetoothScanService getService() {
            return BluetoothScanService.this;
        }
    }

    private ScanServiceBinder mBinder = new ScanServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mApp = (FactoryTestApplication) getApplication();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothDeviceReceiver, filter);
        Log.i(TAG, "BluetoothScanService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startBluetoothScan();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startBluetoothScan() {
        mApp.setIsBluetoothScanning(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || mBluetoothAdapter.isEnabled()) {
            scanDevice();
        } else if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
            time.setToNow();
            startTime = time.toMillis(true);
            recordTime = true;
            mBluetoothAdapter.enable();
        }
    }

    private void scanDevice() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private void cancelScan() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "BluetoothScanService onDestroy");
        unregisterReceiver(mBluetoothDeviceReceiver);
        mApp.setIsBluetoothScanning(false);
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
        cancelScan();
        super.onDestroy();
    }

    public boolean getBluetoothScanState() {
        return bluetoothScanFinish;
    }

    BroadcastReceiver mBluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(TAG, "BluetoothScanService ACTION_FOUND size =" + mDeviceList.size());
                Log.e(TAG, "BluetoothScanService" + " name =" + device.getName() + " addr = " + device.getAddress());
                mDeviceList.add(new DeviceInfo(device.getName(), device
                        .getAddress(), intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)));
                mApp.setBluetoothDeviceList(mDeviceList);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                Log.e(TAG, "BluetoothScanService ACTION_DISCOVERY_FINISHED size =" + mDeviceList.size());
                bluetoothScanFinish = true;
                mApp.setIsBluetoothScanning(false);
                if (mDeviceList != null && mDeviceList.size() > 0) {
                    mApp.setBluetoothDeviceList(mDeviceList);
                }
                BluetoothScanService.this.stopSelf();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e(TAG, "BluetoothScanService ACTION_DISCOVERY_STARTED size =" + mDeviceList.size());
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.e(TAG, "BluetoothScanService ACTION_STATE_CHANGED size =" + mDeviceList.size());
                if (BluetoothAdapter.STATE_ON == intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, 0)) {
                    scanDevice();
                    if (recordTime) {
                        time.setToNow();
                        endTime = time.toMillis(true);
                        recordTime = false;

                    } else if (BluetoothAdapter.STATE_OFF == intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        mBluetoothAdapter.enable();
                    }
                } else if (BluetoothAdapter.STATE_TURNING_ON == intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    Log.e(TAG, "BluetoothScanService STATE_TURNING_ON size =" + mDeviceList.size());
                } else if (BluetoothAdapter.STATE_TURNING_OFF == intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    Log.e(TAG, "BluetoothScanService STATE_TURNING_OFF size =" + mDeviceList.size());
                }
            }

        }

    };
}
