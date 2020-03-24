package com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.lyc.newtestapplication.newtestapplication.R;

import java.io.IOException;

import static com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest.BlueToothMainActivity.MY_UUID;
import static com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest.BlueToothMainActivity.NAME;

public class BlueToothServerActivity extends AppCompatActivity {
    private String TAG = "BlueToothServerActivity";

    private static final int DISCOVERABLE_BLUETOOTH = 2;
    public static final int MESSAGE_UPDATE_RECEIVEDCONTENT = 0x3 << 1;

    private BluetoothAdapter mBlueToothAdapter;
    private TextView receivedContent;
    private AcceptThread acceptThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_server);
        mBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBlueToothAdapter.isEnabled()) {
            finish();
        } else {
            setDiscoverable();
        }
        receivedContent = findViewById(R.id.received_content);
        acceptThread = new AcceptThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        acceptThread.cancel();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private boolean isStop = false;

        AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBlueToothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (!isStop) {
                Log.i(TAG, "------  server is running");
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
                isStop = true;
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
        ConnectedThread connectedThread = new ConnectedThread(socket);
        connectedThread.setServerHandler(serverHandler);
        connectedThread.start();
    }

    private void setDiscoverable() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BLUETOOTH);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DISCOVERABLE_BLUETOOTH:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(BlueToothServerActivity.this, "Enable is not discoverable  :(", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BlueToothServerActivity.this, "Enable is now discoverable  for " + resultCode + " seconds", Toast.LENGTH_SHORT).show();
                    acceptThread.start();
                }
                break;
            default:
                break;
        }
    }

    private Handler serverHandler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_UPDATE_RECEIVEDCONTENT:
                    Bundle bundle = msg.getData();
                    receivedContent.setText(bundle.getCharSequence("content"));
                    break;
                default:
                    break;
            }
        }
    };
}
