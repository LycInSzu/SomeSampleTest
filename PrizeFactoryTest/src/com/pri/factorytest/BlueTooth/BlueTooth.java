package com.pri.factorytest.BlueTooth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.FactoryTestApplication;
import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.Service.BluetoothScanService;
import com.pri.factorytest.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BlueTooth extends PrizeBaseActivity {
    private static final String TAG = "BlueTooth";
    private Context mContext;
    ListView mListView = null;

    LayoutInflater mInflater = null;
    List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
    private final static int MIN_COUNT = 1;
    boolean isUserCanncel = false;

    FactoryTestApplication app;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void init(Context context) {
        mContext = context;
        isUserCanncel = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(this);
        mInflater = LayoutInflater.from(mContext);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bluetooth);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        app = (FactoryTestApplication) getApplication();
        mDeviceList = app.getBluetoothDeviceList();
        bindView();
        if (mDeviceList.size() >= MIN_COUNT) {
            mButtonPass.setEnabled(true);
        } else {
            mButtonPass.setEnabled(false);
        }

        if (!app.getIsBluetoothScanning()) {
            Log.e(TAG, "scan finish, restart Bluetooth ScanService...");
            if (mDeviceList.size() > 0) {
                mHandler.sendEmptyMessage(0);
            }
            startService(new Intent(BlueTooth.this, BluetoothScanService.class));
        } else {
            Log.e(TAG, "Bluetooth scannig");
        }
        startTimer();
    }

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!app.getIsBluetoothScanning()) {
                        stopTimer();
                        if (mDeviceList.size() >= MIN_COUNT) {
                            mHandler.sendEmptyMessage(0);
                        }
                    }
                    mDeviceList = app.getBluetoothDeviceList();
                    mHandler.sendEmptyMessage(0);
                }
            };
        }
        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, 500, 1000);
    }

    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    void bindView() {

        mListView = (ListView) findViewById(R.id.devices_list);
        mListView.setAdapter(mAdapter);
    }

    public void updateAdapter() {

        mAdapter.notifyDataSetChanged();
    }

    BaseAdapter mAdapter = new BaseAdapter() {

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        public View getView(int index, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = mInflater.inflate(R.layout.bluetooth_item, null);
            ImageView image = (ImageView) convertView
                    .findViewById(R.id.bluetooth_image);
            TextView text = (TextView) convertView
                    .findViewById(R.id.bluetooth_text);
            text.setText(getResources().getString(R.string.bt_name) + mDeviceList.get(index).getName() + "\n"
                    + getResources().getString(R.string.bt_address) + mDeviceList.get(index).getAddress() + "\n"
                    + getResources().getString(R.string.bt_rssi) + mDeviceList.get(index).getRssi() + "dBm");
            image.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
            return convertView;
        }

        public int getCount() {
            if (mDeviceList != null) {
                return mDeviceList.size();
            } else {
                return 0;
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopService(new Intent(BlueTooth.this, BluetoothScanService.class));
        stopTimer();
        super.onDestroy();
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mDeviceList.size() >= MIN_COUNT) {
                mButtonPass.setEnabled(true);
            } else {
                mButtonPass.setEnabled(false);
            }
            updateAdapter();
            if (!app.getIsBluetoothScanning() && mDeviceList.size() < MIN_COUNT) {
                Toast.makeText(getBaseContext(), getString(R.string.bluetooth_scan_null), Toast.LENGTH_SHORT).show();
                if (Utils.isNoNFastClick()) {
                    if (Utils.toStartAutoTest == true) {
                        Utils.mItemPosition++;
                    }
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        };
    };
}
