package com.pri.factorytest.RAM;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class RAM extends PrizeBaseActivity {
    private static long one_G = 1 * 1024 * 1024 * 1024L;
    private static long two_G = 2 * 1024 * 1024 * 1024L;
    private static long three_G = 3 * 1024 * 1024 * 1024L;
    private static long four_G = 4 * 1024 * 1024 * 1024L;
    private static long six_G = 6 * 1024 * 1024 * 1024L;
    private static long eight_G = 8 * 1024 * 1024 * 1024L;
    private ActivityManager mAm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ram);
        mAm = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        TextView totalRam = (TextView) findViewById(R.id.ram_total);
        TextView freeRam = (TextView) findViewById(R.id.ram_free);
        totalRam.setText(getString(R.string.total_memory) + formatFileSize(getTotalMemory()));
        freeRam.setText(getString(R.string.available_memory) + formatFileSize(getAvailableMemory()));

        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    private long getTotalMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memoryInfo);
        long totalMem = memoryInfo.totalMem;
        long totalCustom = 0L;
        if (totalMem <= one_G) {
            totalCustom = one_G;
        } else if (totalMem > one_G && totalMem <= two_G) {
            totalCustom = two_G;
        } else if (totalMem > two_G && totalMem <= three_G) {
            totalCustom = three_G;
        } else if (totalMem > three_G && totalMem <= four_G) {
            totalCustom = four_G;
        } else if (totalMem > four_G && totalMem <= six_G) {
            totalCustom = six_G;
        } else if (totalMem > six_G && totalMem <= eight_G) {
            totalCustom = eight_G;
        }
        return totalCustom;
    }

    private long getAvailableMemory() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mAm.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    private String formatFileSize(long size) {
        String sizeString;
        if (size < 1024 && size > 0) {
            sizeString = size + "B";
        } else if (size < 1024 * 1024) {
            sizeString = Math.round(size * 1.0 / 1024L) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            sizeString = Math.round(size * 1.0 / (1024 * 1024L)) + "MB";
        } else {
            sizeString = Math.round(size * 1.0 / (1024 * 1024 * 1024L)) + "GB";
        }
        return sizeString;
    }
}