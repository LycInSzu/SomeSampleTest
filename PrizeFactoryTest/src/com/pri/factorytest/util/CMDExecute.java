package com.pri.factorytest.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CMDExecute {

    private static final String TAG = "CMDExecute";

    public synchronized String run(String[] cmd, String workdirectory) throws IOException{
        StringBuilder result = new StringBuilder();
        InputStream in = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
                builder.redirectErrorStream(true);
                Process process = builder.start();
                in = process.getInputStream();
                byte[] re = new byte[3];
                while (in.read(re) != -1) {
                    //result = result + new String(re);
                    result.append(StringFactory.newStringFromBytes(re));
                }
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            for (String str : cmd) {
                result.append(str + " ");
            }
            Log.e(TAG, "PrizeFactoryTest-execute'" + result.toString() + "'command error!!!!!!!!!!!!!");
            return "";
        }
        return result.toString();
    }
}