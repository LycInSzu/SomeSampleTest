package com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.lyc.newtestapplication.newtestapplication.R;

import java.util.UUID;

public class BlueToothMainActivity extends AppCompatActivity {
    private String TAG = "BlueToothMainActivity";

    private static final int ENABLE_BLUETOOTH = 1 ;


    public static final UUID MY_UUID = UUID.nameUUIDFromBytes("lyc".getBytes());
    public static final String NAME = "lycbluetoothfunctiontest";

    private BluetoothAdapter mBlueToothAdapter;
    private BroadcastReceiver bluetoothStateChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_main);

        if (BlueToothUtils.isSupportBlueTooth()){
            mBlueToothAdapter=BluetoothAdapter.getDefaultAdapter();
            startBlueTooth();
        }else {
            Toast.makeText(BlueToothMainActivity.this,"This device not support bluetooth!",Toast.LENGTH_LONG).show();
            finish();
        }
        registerBlueToothStateChangeReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegistBlueToothStateChangeReceiver();
    }
    public void onOpenBTButtonClick(View view){
        startBlueTooth();
    }

    public void onClientButtonClick(View view){
        Intent clientIntent= new Intent(BlueToothMainActivity.this,BlueToothClientActivity.class);
        startActivity(clientIntent);
    }

    public void onServerButtonClick(View view){
        Intent serverIntent= new Intent(BlueToothMainActivity.this,BlueToothServerActivity.class);
        startActivity(serverIntent);
    }

    private void startBlueTooth() {
        if (!mBlueToothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,ENABLE_BLUETOOTH);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ENABLE_BLUETOOTH:
                if (resultCode==RESULT_OK){
                    Toast.makeText(BlueToothMainActivity.this,"bluetooth is turned on!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BlueToothMainActivity.this,"Enable bluetooth failed!",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private class BlueToothStateChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action==null){
                return;
            }
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
                    int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE,-1);
                    Log.i(TAG,"------ preState= " +preState+"   newState= "+newState);
                    break;
                default:
                    break;
            }

        }
    }
    private void unRegistBlueToothStateChangeReceiver() {
        unregisterReceiver(bluetoothStateChangeReceiver);
    }

    private void registerBlueToothStateChangeReceiver() {
        IntentFilter filter= new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothStateChangeReceiver = new BlueToothStateChangeReceiver();
        registerReceiver(bluetoothStateChangeReceiver,filter);
    }
}
