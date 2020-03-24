package com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.lyc.newtestapplication.newtestapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest.BlueToothMainActivity.MY_UUID;

public class BlueToothClientActivity extends AppCompatActivity {
    private String TAG = "BlueToothClientActivity";

    private BluetoothAdapter mBlueToothAdapter=BluetoothAdapter.getDefaultAdapter();
    private BlueToothFonundNewDeviceReceiver bluetoothFindNewDeviceReceiver;

    private RecyclerView devicesList;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private int tempContent=1;
    private String stringContent=tempContent+"";
    private BluetoothSocket connectedSocket;
    private ConnectedThread connectedThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_client);
        if (!mBlueToothAdapter.isEnabled()){
            finish();
        }
        devicesList=findViewById(R.id.found_recyclerView);
        devicesList.setLayoutManager(new LinearLayoutManager(BlueToothClientActivity.this));
        devicesList.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(BlueToothClientActivity.this).inflate(R.layout.bluetooth_devices_list,null);
                RecyclerView.ViewHolder viewHolder=new RecyclerView.ViewHolder(view) {
                };
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                ((TextView)holder.itemView.findViewById(R.id.name)).setText(devices.get(position).getName());
                ((TextView)holder.itemView.findViewById(R.id.name)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ConnectThread(devices.get(position)).start();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return devices.size();
            }
        });


        registerBlueToothFindNewDeviceReceiver();
        showPairedDevices();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegistBlueToothFindNewDeviceReceiver();
        connectedThread.cancel();
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices=mBlueToothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            devices.clear();
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(TAG,"------   pairedDevice  Name= "+deviceName+"   HardwareAddress= "+deviceHardwareAddress);
                if (device.getName().equals("")){
                    Log.i(TAG,"------   connecting to the device......     Name= "+deviceName+"   HardwareAddress= "+deviceHardwareAddress);
                }
                devices.add(device);
            }
        }

    }

    private void startScanning() {
        if (!mBlueToothAdapter.isDiscovering())
            mBlueToothAdapter.startDiscovery();
    }
    private void stopScanning(){
        if (mBlueToothAdapter.isDiscovering()){
            mBlueToothAdapter.cancelDiscovery();
        }
    }

    public void onChangeContentButtonClick(View view){
        if (connectedSocket==null){
            return;
        }

        tempContent++;
        stringContent=tempContent+"";

        connectedThread.write(stringContent.getBytes());

    }

    private class BlueToothFonundNewDeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action==null){
                return;
            }
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i(TAG,"------   find new device    Name= "+deviceName+"   HardwareAddress= "+deviceHardwareAddress);
                    break;
                default:
                    break;
            }

        }
    }
    private void unRegistBlueToothFindNewDeviceReceiver() {
        unregisterReceiver(bluetoothFindNewDeviceReceiver);
    }

    private void registerBlueToothFindNewDeviceReceiver() {
        IntentFilter filter= new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFindNewDeviceReceiver = new BlueToothFonundNewDeviceReceiver();
        registerReceiver(bluetoothFindNewDeviceReceiver,filter);
    }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBlueToothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect the client socket", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        connectedSocket=mmSocket;
        connectedThread=  new ConnectedThread(connectedSocket);
        connectedThread.start();
    }

}
