package com.pri.factorytest.SDCard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SDCard extends PrizeBaseActivity {
    private static final String TAG = "SDCard";
    private static final boolean MTK_2SDCARD_SWAP = SystemProperties.get("ro.mtk_2sdcard_swap").equals("1");
    private TextView externalMemoryTextView;
    private TextView sdCard2MemoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdcard);
        confirmButton();
        externalMemoryTextView = (TextView) findViewById(R.id.inside_sd);
        sdCard2MemoryTextView = (TextView) findViewById(R.id.outside_sd);
        showStorageVolume();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        Log.d(TAG, "------getExtSDCardPath()=" + getExtSDCardPath());
    }

    private void showStorageVolume() {
        mButtonPass.setEnabled(false);
        if (MTK_2SDCARD_SWAP) {
            if ((getTotalExternalMemorySize() != 0)
                    && (getTotalSDCard2MemorySize() != 0)) {
                mButtonPass.setEnabled(true);
                externalMemoryTextView.setText(getString(R.string.sdcard) + getString(R.string.detected) + "\n" + getString(R.string.total_volume)
                        + getTotalExternalMemorySize() + "GB" + "\n" + getString(R.string.available_volume)
                        + getAvailableExternalMemorySize() + "GB");
                sdCard2MemoryTextView.setText(getString(R.string.internal_storage) + getString(R.string.detected) + "\n" + getString(R.string.total_volume)
                        + getTotalSDCard2MemorySize() + "GB" + "\n" + getString(R.string.available_volume)
                        + getAvailableSDCard2MemorySize() + "GB");
            } else {
                if (getTotalExternalMemorySize() != 0) {
                    externalMemoryTextView
                            .setText(getString(R.string.internal_storage) + getString(R.string.detected) + "\n" + getString(R.string.total_volume)
                                    + getTotalExternalMemorySize()
                                    + "GB" + "\n" + getString(R.string.available_volume)
                                    + getAvailableExternalMemorySize() + "GB");
                    sdCard2MemoryTextView.setText(getString(R.string.internal_storage) + getString(R.string.not_detected));
                }
            }
        } else {
            if (getTotalSDCard2MemorySize() != 0 && getTotalExternalMemorySize() != 0) {
                mButtonPass.setEnabled(true);
            }
            if (getTotalSDCard2MemorySize() != 0) {
                sdCard2MemoryTextView.setText(getString(R.string.sdcard) + getString(R.string.detected) + "\n" + getString(R.string.total_volume)
                        + getTotalSDCard2MemorySize() + "GB" + "\n" + getString(R.string.available_volume)
                        + getAvailableSDCard2MemorySize() + "GB");
            } else {
                sdCard2MemoryTextView.setText(getString(R.string.sdcard) + getString(R.string.not_detected));
            }
            if (getTotalExternalMemorySize() != 0) {
                externalMemoryTextView.setText(getString(R.string.internal_storage) + getString(R.string.detected) + "\n" + getString(R.string.total_volume)
                        + getTotalExternalMemorySize() + "GB" + "\n" + getString(R.string.available_volume)
                        + getAvailableExternalMemorySize() + "GB");
            } else {
                externalMemoryTextView.setText(getString(R.string.internal_storage) + getString(R.string.not_detected));
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(broadcastRec, intentFilter);
        doPass2NextTest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastRec);
    }

    private final BroadcastReceiver broadcastRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String imagepath = new String();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED))// SD
            {
                showStorageVolume();
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                    ) {
                while (getTotalSDCard2MemorySize() != 0) ;
                showStorageVolume();
            }
            doPass2NextTest();
        }
    };

    private String getExternalMemoryPath() {
        return "/mnt/sdcard";
    }

    private String getSDCard2MemoryPath() {
        return getExtSDCardPath();//"/mnt/media_rw";//mnt/sdcard2
    }

    private StatFs getStatFs(String path) {
        try {
            return new StatFs(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public /*List<*/String/*>*/ getExtSDCardPath() {
        //List<string> lResult = new ArrayList<string>();
        String SDCardPath = "";
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("media_rw")) {
                    Log.d(TAG, "getExtSDCardPath()-line=" + line);
                    String[] arr = line.split(" ");
                    String path = arr[0];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        //lResult.add(path);
                        SDCardPath = path;
                        return SDCardPath;
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return SDCardPath;
    }


    @SuppressWarnings("deprecation")
    private float calculateAvailableSizeInGB(StatFs stat) {
        if (stat != null)
            return (float) Math.round((stat.getAvailableBlocks()
                    * (stat.getBlockSize() / (1024f * 1024f * 1024f))) * 100) / 100;

        return 0;
    }

    @SuppressWarnings("deprecation")
    private float calculateTotalSizeInGB(StatFs stat) {
        if (stat != null) {
            float realsize = (float) Math.round((stat.getBlockCount() * (stat.getBlockSize() / (1024f * 1024f * 1024f))) * 100) / 100;
            if (realsize <= 0.128f) {
                return 0.128f;
            }else if (realsize > 0.128f && realsize <= 0.256f) {
                return 0.256f;
            }else if (realsize > 0.256f && realsize <= 0.512f) {
                return 0.512f;
            } else if (realsize > 0.512f && realsize <= 1f) {
                return 1.0f;
            } else if (realsize > 1f && realsize <= 2f) {
                return 2.0f;
            } else if (realsize > 2f && realsize <= 4f) {
                return 4.0f;
            } else if (realsize > 4f && realsize <= 8f) {
                return 8.0f;
            } else if (realsize > 8f && realsize <= 16f) {
                return 16.0f;
            } else if (realsize > 16f && realsize <= 32f) {
                return 32.0f;
            } else if (realsize > 32f && realsize <= 64f) {
                return 64.0f;
            } else if (realsize > 64f && realsize <= 128f) {
                return 128.0f;
            } else if (realsize > 128f && realsize <= 256f) {
                return 256.0f;
            }else if (realsize > 256f && realsize <= 512f) {
                return 512.0f;
            }
        }

        return 0;
    }

    private float getTotalExternalMemorySize() {
        String path = getExternalMemoryPath();
        StatFs stat = getStatFs(path);
        return calculateTotalSizeInGB(stat);
    }

    private float getTotalSDCard2MemorySize() {
        String path = getSDCard2MemoryPath();
        StatFs stat = getStatFs(path);
        return calculateTotalSizeInGB(stat);
    }

    private float getAvailableExternalMemorySize() {
        String path = getExternalMemoryPath();
        StatFs stat = getStatFs(path);
        return calculateAvailableSizeInGB(stat);
    }

    private float getAvailableSDCard2MemorySize() {
        String path = getSDCard2MemoryPath();
        StatFs stat = getStatFs(path);
        return calculateAvailableSizeInGB(stat);
    }
}