package com.lyc.newtestapplication.newtestapplication.BluetoothConnectionTest;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private String TAG = "ConnectedThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Handler serverHandler=null;

    private boolean isStoped=false;

    public void setServerHandler(Handler handler){
        serverHandler=handler;
    }


    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }


    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (!isStoped) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
//                // Send the obtained bytes to the UI activity.
//                Message readMsg = handler.obtainMessage(
//                        MessageConstants.MESSAGE_READ, numBytes, -1,
//                        mmBuffer);
//                readMsg.sendToTarget();
                if (numBytes<=0){
                    Log.i(TAG,"------    read result is null");
                }else {
//                    StringBuilder readResult=new StringBuilder();
//                    for (int i=0;i<numBytes;i++){
//                        readResult.append(mmBuffer[i]);
//
//                    }
                    String result = new String(mmBuffer);
                    Log.i(TAG,"------    read result is "+result);
                    if (serverHandler!=null){
                        Message message=new Message();
                        message.what=BlueToothServerActivity.MESSAGE_UPDATE_RECEIVEDCONTENT;
                        Bundle bundle=new Bundle();
                        bundle.putCharSequence("content",result);
                        message.setData(bundle);
                        serverHandler.sendMessage(message);
                    }
                }

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            Log.i(TAG,"---------- start write");
            mmOutStream.write(bytes);

//            // Share the sent message with the UI activity.
//            Message writtenMsg = handler.obtainMessage(
//                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

//            // Send a failure message back to the activity.
//            Message writeErrorMsg =
//                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            handler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
            isStoped=true;
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
