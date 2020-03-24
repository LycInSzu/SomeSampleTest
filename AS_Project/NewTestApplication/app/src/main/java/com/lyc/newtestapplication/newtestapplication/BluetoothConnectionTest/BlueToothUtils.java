package com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest;

import android.bluetooth.BluetoothAdapter;

public class BlueToothUtils {


    public static boolean isSupportBlueTooth(){
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter!=null){
            return true;
        }
        return false;
    }
}
